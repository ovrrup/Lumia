package lumia.tracker.sync.p2p

import android.content.Context
import android.util.Base64
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.FullAppBackup
import lumia.tracker.model.ScholarBackup
import lumia.tracker.sync.crypto.SyncCryptoManager
import lumia.tracker.sync.merge.SyncMergeEngine
import lumia.tracker.sync.model.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance P2P DataChannel transport engine over secure local TCP framing.
 * Supports both initial 1-time mutual handshake (PIN/QR) with permanent PSK derivation,
 * and continuous anytime zero-interaction synchronization with trusted peers.
 */
class P2PDataChannelEngine(
    private val context: Context,
    private val profileManager: ProfileManager,
    private val trustedPeerProvider: (deviceId: String) -> TrustedPeer?,
    private val onPeerPaired: (TrustedPeer) -> Unit,
    private val onPeerSynced: (deviceId: String) -> Unit,
    private val onStateChanged: (SyncState) -> Unit
) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val messageAdapter = moshi.adapter(SyncMessage::class.java)
    private val backupAdapter = moshi.adapter(ScholarBackup::class.java)
    private val fullBackupAdapter = moshi.adapter(FullAppBackup::class.java)
    private val reportAdapter = moshi.adapter(SyncMergeReport::class.java)

    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var activePort: Int = 52934
        private set

    companion object {
        private const val TAG = "P2PDataChannelEngine"
        private const val DEFAULT_PORT = 52934
    }

    /**
     * Starts the local P2P Sync Server.
     */
    fun startServer(pairingPin: () -> String, localDevice: SyncDevice) {
        if (isRunning.get()) return

        serverJob = scope.launch {
            try {
                serverSocket = try {
                    ServerSocket(DEFAULT_PORT).also { activePort = DEFAULT_PORT }
                } catch (e: Exception) {
                    ServerSocket(0).also { activePort = it.localPort }
                }
                isRunning.set(true)
                Log.i(TAG, "P2P Sync Server listening on port $activePort")

                while (isRunning.get() && serverSocket?.isClosed == false) {
                    val clientSocket = serverSocket?.accept() ?: break
                    launch {
                        handleIncomingConnection(clientSocket, pairingPin(), localDevice)
                    }
                }
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Server socket error", e)
                }
            }
        }
    }

    /**
     * Connects as client to a peer device and executes synchronization.
     * If [trustedPSK] is provided, uses the permanent 1-time handshake key (no PIN prompt required).
     */
    fun connectAndSync(
        peer: SyncDevice,
        pinOrPsk: String,
        localDevice: SyncDevice,
        isTrustedAuth: Boolean = false,
        mode: SyncMode = SyncMode.SMART_MERGE,
        onComplete: (SyncMergeReport?) -> Unit
    ) {
        scope.launch {
            var socket: Socket? = null
            try {
                onStateChanged(SyncState.Connecting(peer.name))
                socket = Socket()
                socket.connect(InetSocketAddress(peer.ipAddress, peer.port), 8000)
                socket.soTimeout = 30000

                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())

                val clientNonce = SyncCryptoManager.generateNonce()

                // 1. Send HELLO
                val helloMsg = SyncMessage(
                    type = "HELLO",
                    deviceId = localDevice.id,
                    deviceName = localDevice.name,
                    avatarEmoji = localDevice.avatarEmoji,
                    nonce = clientNonce,
                    syncMode = mode.name,
                    isTrustedAuth = isTrustedAuth
                )
                sendMessage(output, helloMsg)

                // 2. Receive CHALLENGE
                val challengeMsg = receiveMessage(input) ?: throw IllegalStateException("No response from peer")
                if (challengeMsg.type != "CHALLENGE") throw IllegalStateException("Unexpected handshake: ${challengeMsg.type}")
                val serverNonce = challengeMsg.nonce

                onStateChanged(SyncState.Authenticating(peer.name))

                // 3. Calculate AUTH response
                val keyForAuth = pinOrPsk
                val authHash = SyncCryptoManager.calculateAuthHash(keyForAuth, serverNonce, localDevice.id)
                val authMsg = SyncMessage(
                    type = "AUTH",
                    deviceId = localDevice.id,
                    deviceName = localDevice.name,
                    authHash = authHash,
                    isTrustedAuth = isTrustedAuth
                )
                sendMessage(output, authMsg)

                // 4. Receive AUTH_OK
                val authOkMsg = receiveMessage(input) ?: throw IllegalStateException("Auth response missing")
                if (authOkMsg.type != "AUTH_OK") {
                    throw IllegalStateException("Authentication failed: Incorrect PIN, token, or trust key")
                }

                // Verify server's mutual auth hash
                val isServerValid = SyncCryptoManager.verifyAuthHash(keyForAuth, clientNonce, peer.id, authOkMsg.authHash)
                if (!isServerValid) {
                    throw IllegalStateException("Mutual authentication failed on peer verification")
                }

                // If this was an initial 1-time pairing (non-trusted PIN auth), derive and store permanent PSK
                val sessionEncryptionKey = if (isTrustedAuth) {
                    keyForAuth
                } else {
                    val derivedPsk = SyncCryptoManager.derivePSK(pinOrPsk, clientNonce, serverNonce)
                    val newTrustedPeer = TrustedPeer(
                        deviceId = peer.id,
                        deviceName = peer.name,
                        preSharedKey = derivedPsk,
                        pairedAt = System.currentTimeMillis(),
                        lastSyncAt = System.currentTimeMillis(),
                        autoSyncEnabled = true,
                        avatarEmoji = peer.avatarEmoji
                    )
                    onPeerPaired(newTrustedPeer)
                    derivedPsk
                }

                onStateChanged(SyncState.ExchangingData(peer.name, 0.3f, "Packaging secure payload..."))

                // 5. Gather local payload and encrypt
                val localBackup = createFullLocalBackup()
                val localBackupJson = backupAdapter.toJson(localBackup)
                val (encryptedLocal, localIv) = SyncCryptoManager.encryptPayload(
                    localBackupJson.toByteArray(Charsets.UTF_8),
                    sessionEncryptionKey,
                    serverNonce
                )

                // 6. Send SYNC_DATA
                val syncDataMsg = SyncMessage(
                    type = "SYNC_DATA",
                    deviceId = localDevice.id,
                    syncMode = mode.name,
                    payloadEncryptedBase64 = encryptedLocal,
                    ivBase64 = localIv
                )
                sendMessage(output, syncDataMsg)

                onStateChanged(SyncState.ExchangingData(peer.name, 0.6f, "Receiving remote data stream..."))

                // 7. Receive peer's SYNC_DATA
                val peerSyncData = receiveMessage(input) ?: throw IllegalStateException("Missing peer sync payload")
                if (peerSyncData.type != "SYNC_DATA") throw IllegalStateException("Unexpected sync packet: ${peerSyncData.type}")

                val remoteEncrypted = peerSyncData.payloadEncryptedBase64 ?: throw IllegalStateException("Empty peer payload")
                val remoteIv = peerSyncData.ivBase64 ?: throw IllegalStateException("Missing IV from peer")

                val decryptedBytes = SyncCryptoManager.decryptPayload(remoteEncrypted, remoteIv, sessionEncryptionKey, clientNonce)
                val remoteBackup = backupAdapter.fromJson(String(decryptedBytes, Charsets.UTF_8))
                    ?: throw IllegalStateException("Failed to parse remote backup")

                onStateChanged(SyncState.Merging("Smart merging database entities..."))

                // 8. Apply merge according to sync mode
                val report = when (mode) {
                    SyncMode.SMART_MERGE -> {
                        SyncMergeEngine.mergeFullApp(context, profileManager, remoteBackup, peer.name, mode.name)
                    }
                    SyncMode.PULL_FROM_PEER -> {
                        val db = AppDatabase.getDatabase(context, profileManager.getActiveProfileId())
                        db.scholarDao().restoreBackup(remoteBackup)
                        SyncMergeReport(peerDeviceName = peer.name, syncMode = mode.name)
                    }
                    SyncMode.PUSH_TO_PEER -> {
                        SyncMergeReport(peerDeviceName = peer.name, syncMode = mode.name)
                    }
                }

                // 9. Send SYNC_ACK
                val ackMsg = SyncMessage(
                    type = "SYNC_ACK",
                    reportJson = reportAdapter.toJson(report)
                )
                sendMessage(output, ackMsg)

                onPeerSynced(peer.id)
                onStateChanged(SyncState.Success(report))
                onComplete(report)
            } catch (e: Exception) {
                Log.e(TAG, "Sync client failure", e)
                val err = e.message ?: "Sync connection failed"
                onStateChanged(SyncState.Error(err))
                onComplete(null)
            } finally {
                try { socket?.close() } catch (ignored: Exception) {}
            }
        }
    }

    private suspend fun handleIncomingConnection(socket: Socket, pin: String, localDevice: SyncDevice) = withContext(Dispatchers.IO) {
        try {
            socket.soTimeout = 30000
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())

            // 1. Receive HELLO
            val helloMsg = receiveMessage(input) ?: return@withContext
            if (helloMsg.type != "HELLO") return@withContext

            val peerName = helloMsg.deviceName
            val peerId = helloMsg.deviceId
            val clientNonce = helloMsg.nonce
            val mode = try { SyncMode.valueOf(helloMsg.syncMode) } catch (e: Exception) { SyncMode.SMART_MERGE }
            val clientRequestsTrusted = helloMsg.isTrustedAuth

            onStateChanged(SyncState.Connecting(peerName))

            val serverNonce = SyncCryptoManager.generateNonce()

            // 2. Send CHALLENGE
            val challengeMsg = SyncMessage(
                type = "CHALLENGE",
                deviceId = localDevice.id,
                deviceName = localDevice.name,
                avatarEmoji = localDevice.avatarEmoji,
                nonce = serverNonce,
                isTrustedAuth = clientRequestsTrusted
            )
            sendMessage(output, challengeMsg)

            onStateChanged(SyncState.Authenticating(peerName))

            // 3. Receive AUTH
            val authMsg = receiveMessage(input) ?: return@withContext
            if (authMsg.type != "AUTH") return@withContext

            // Determine if using saved PSK or temporary pairing PIN
            val trustedPeer = trustedPeerProvider(peerId)
            val isUsingPsk = clientRequestsTrusted && trustedPeer != null
            val expectedAuthKey = if (isUsingPsk) trustedPeer!!.preSharedKey else pin

            val isClientValid = SyncCryptoManager.verifyAuthHash(expectedAuthKey, serverNonce, peerId, authMsg.authHash)
            if (!isClientValid) {
                sendMessage(output, SyncMessage(type = "AUTH_FAIL", errorMessage = "Invalid PIN or trust key"))
                onStateChanged(SyncState.Error("Peer authentication failed"))
                return@withContext
            }

            // 4. Send AUTH_OK with mutual proof
            val serverAuthHash = SyncCryptoManager.calculateAuthHash(expectedAuthKey, clientNonce, localDevice.id)
            val authOkMsg = SyncMessage(
                type = "AUTH_OK",
                authHash = serverAuthHash,
                isTrustedAuth = isUsingPsk
            )
            sendMessage(output, authOkMsg)

            // If this was initial 1-time handshake (PIN auth), derive permanent PSK and register trusted peer
            val sessionEncryptionKey = if (isUsingPsk) {
                expectedAuthKey
            } else {
                val derivedPsk = SyncCryptoManager.derivePSK(pin, clientNonce, serverNonce)
                val newTrustedPeer = TrustedPeer(
                    deviceId = peerId,
                    deviceName = peerName,
                    preSharedKey = derivedPsk,
                    pairedAt = System.currentTimeMillis(),
                    lastSyncAt = System.currentTimeMillis(),
                    autoSyncEnabled = true,
                    avatarEmoji = helloMsg.avatarEmoji
                )
                onPeerPaired(newTrustedPeer)
                derivedPsk
            }

            onStateChanged(SyncState.ExchangingData(peerName, 0.4f, "Packaging encrypted payload..."))

            // 5. Create and encrypt local payload
            val localBackup = createFullLocalBackup()
            val localBackupJson = backupAdapter.toJson(localBackup)
            val (encryptedLocal, localIv) = SyncCryptoManager.encryptPayload(
                localBackupJson.toByteArray(Charsets.UTF_8),
                sessionEncryptionKey,
                clientNonce
            )

            // 6. Receive client's SYNC_DATA
            val clientSyncData = receiveMessage(input) ?: return@withContext
            val clientEncrypted = clientSyncData.payloadEncryptedBase64 ?: return@withContext
            val clientIv = clientSyncData.ivBase64 ?: return@withContext

            // 7. Send server's SYNC_DATA
            val serverSyncData = SyncMessage(
                type = "SYNC_DATA",
                deviceId = localDevice.id,
                payloadEncryptedBase64 = encryptedLocal,
                ivBase64 = localIv
            )
            sendMessage(output, serverSyncData)

            onStateChanged(SyncState.Merging("Smart merging remote dataset..."))

            // 8. Decrypt client's payload and merge
            val decryptedBytes = SyncCryptoManager.decryptPayload(clientEncrypted, clientIv, sessionEncryptionKey, serverNonce)
            val remoteBackup = backupAdapter.fromJson(String(decryptedBytes, Charsets.UTF_8)) ?: return@withContext

            val report = when (mode) {
                SyncMode.SMART_MERGE -> {
                    SyncMergeEngine.mergeFullApp(context, profileManager, remoteBackup, peerName, mode.name)
                }
                SyncMode.PUSH_TO_PEER -> {
                    val db = AppDatabase.getDatabase(context, profileManager.getActiveProfileId())
                    db.scholarDao().restoreBackup(remoteBackup)
                    SyncMergeReport(peerDeviceName = peerName, syncMode = mode.name)
                }
                SyncMode.PULL_FROM_PEER -> {
                    SyncMergeReport(peerDeviceName = peerName, syncMode = mode.name)
                }
            }

            // 9. Receive ACK
            val clientAck = receiveMessage(input)
            onPeerSynced(peerId)
            onStateChanged(SyncState.Success(report))
        } catch (e: Exception) {
            Log.e(TAG, "Server connection handling error", e)
            onStateChanged(SyncState.Error(e.message ?: "Incoming sync failed"))
        } finally {
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    private suspend fun createFullLocalBackup(): ScholarBackup = withContext(Dispatchers.IO) {
        val allProfs = profileManager.getAllProfiles()
        val profileBackupsJson = mutableMapOf<String, String>()

        for (prof in allProfs) {
            val db = AppDatabase.getDatabase(context, prof.id)
            val dao = db.scholarDao()
            val pBackup = ScholarBackup(
                courses = dao.exportAllCourses(),
                subjects = dao.exportAllSubjects(),
                topics = dao.exportAllTopics(),
                assignments = dao.exportAllAssignments(),
                attendance = dao.exportAllAttendance(),
                pomodoro = dao.exportAllPomodoro(),
                actionLogs = dao.exportAllActionLogs(),
                notes = dao.exportAllNotes(),
                chapters = dao.exportAllChapters(),
                tasks = dao.exportAllTasks(),
                attachments = dao.exportAllAttachments(),
                testRecords = dao.exportAllTestRecords(),
                tagCustomizations = dao.exportAllTagCustomizations(),
                profile = prof
            )
            profileBackupsJson[prof.id] = backupAdapter.toJson(pBackup)
        }

        val fullApp = FullAppBackup(
            profiles = allProfs,
            activeProfileId = profileManager.getActiveProfileId(),
            profileBackupsJson = profileBackupsJson
        )

        ScholarBackup(
            isFullAppBackup = true,
            fullAppBackupJson = fullBackupAdapter.toJson(fullApp)
        )
    }

    private fun sendMessage(output: DataOutputStream, msg: SyncMessage) {
        val json = messageAdapter.toJson(msg)
        val bytes = json.toByteArray(Charsets.UTF_8)
        output.writeInt(bytes.size)
        output.write(bytes)
        output.flush()
    }

    private fun receiveMessage(input: DataInputStream): SyncMessage? {
        val length = input.readInt()
        if (length <= 0 || length > 100 * 1024 * 1024) return null
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return messageAdapter.fromJson(String(bytes, Charsets.UTF_8))
    }

    fun stop() {
        isRunning.set(false)
        serverJob?.cancel()
        try { serverSocket?.close() } catch (ignored: Exception) {}
        serverSocket = null
    }
}
