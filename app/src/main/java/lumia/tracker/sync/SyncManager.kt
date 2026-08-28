package lumia.tracker.sync

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.*
import lumia.tracker.sync.crypto.SyncCryptoManager
import lumia.tracker.sync.discovery.P2PDiscoveryManager
import lumia.tracker.sync.merge.SyncMergeEngine
import lumia.tracker.sync.model.*
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main coordinator for Lumia's Persistent Background Live Mesh Sync Engine.
 * Supports:
 * 1. One-Time Pairing with persistent cryptographic relationships (channelId + AES-GCM session key).
 * 2. Real-time differential CRDT Delta Sync with Last-Write-Wins and natural keys.
 * 3. Offline Buffer Queue (Outbox) with automatic replay upon peer discovery/reconnection.
 * 4. Silent, zero-interaction background synchronization across local Wi-Fi / Hotspot.
 * 5. Clean, reactive StateFlows for UI status, connectivity, and mesh topology.
 */
@ValueScore(
    score = 99,
    importance = Importance.CRITICAL,
    description = "Persistent Background Live Mesh Sync Coordinator and CRDT transport manager",
    category = "SYNC"
)
class SyncManager(private val context: Context) {

    private val profileManager = ProfileManager(context)
    private val discoveryManager = P2PDiscoveryManager(context)
    private val pairedDeviceStore = PairedDeviceStore.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val messageAdapter = moshi.adapter(SyncMessage::class.java)
    private val deltaPacketAdapter = moshi.adapter(SyncDeltaPacket::class.java)
    private val tokenAdapter = moshi.adapter(SyncPairingToken::class.java)
    private val backupAdapter = moshi.adapter(ScholarBackup::class.java)
    private val fullBackupAdapter = moshi.adapter(FullAppBackup::class.java)
    private val reportAdapter = moshi.adapter(SyncMergeReport::class.java)
    private val historyAdapter = moshi.adapter<List<SyncHistoryRecord>>(
        Types.newParameterizedType(List::class.java, SyncHistoryRecord::class.java)
    )
    private val trustedPeersAdapter = moshi.adapter<List<TrustedPeer>>(
        Types.newParameterizedType(List::class.java, TrustedPeer::class.java)
    )
    private val outboxAdapter = moshi.adapter<List<OfflineMutationEntry>>(
        Types.newParameterizedType(List::class.java, OfflineMutationEntry::class.java)
    )

    private val prefs = context.getSharedPreferences("lumia_sync_prefs", Context.MODE_PRIVATE)

    val deviceId: String = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }

    val deviceName: String = prefs.getString("device_name", null) ?: run {
        val model = Build.MODEL ?: "Android"
        val brand = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Lumia"
        "$brand $model"
    }

    // --- Reactive StateFlows (Prompt Requirements) ---

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _connectedDevicesCount = MutableStateFlow(0)
    val connectedDevicesCount: StateFlow<Int> = _connectedDevicesCount.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow(prefs.getLong("last_synced_timestamp", 0L))
    val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<TrustedPeer>>(loadTrustedPeers())
    val pairedDevices: StateFlow<List<TrustedPeer>> = _pairedDevices.asStateFlow()
    val trustedPeers: StateFlow<List<TrustedPeer>> = _pairedDevices.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<SyncDevice>>(emptyList())
    val discoveredPeers: StateFlow<List<SyncDevice>> = _discoveredPeers.asStateFlow()

    private val _continuousAutoSyncEnabled = MutableStateFlow(prefs.getBoolean("continuous_auto_sync", true))
    val continuousAutoSyncEnabled: StateFlow<Boolean> = _continuousAutoSyncEnabled.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _pairingPin = MutableStateFlow(SyncCryptoManager.generatePairingPin())
    val pairingPin: StateFlow<String> = _pairingPin.asStateFlow()

    private val _pairingToken = MutableStateFlow("")
    val pairingToken: StateFlow<String> = _pairingToken.asStateFlow()

    private val _syncHistory = MutableStateFlow<List<SyncHistoryRecord>>(loadHistory())
    val syncHistory: StateFlow<List<SyncHistoryRecord>> = _syncHistory.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _outboxQueue = MutableStateFlow<List<OfflineMutationEntry>>(loadOutbox())
    val pendingOutboxCount: StateFlow<Int> = _outboxQueue.map { it.size }.stateIn(scope, SharingStarted.Eagerly, 0)

    // --- Mesh Networking & Sessions ---

    var activePort: Int = DEFAULT_PORT
        private set

    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var serverJob: Job? = null
    private var dbObservationJob: Job? = null
    private val activeSessions = ConcurrentHashMap<String, LiveMeshSession>()
    private val connectingPeers = ConcurrentHashMap.newKeySet<String>()
    private val isApplyingRemoteMerge = AtomicBoolean(false)

    companion object {
        private const val TAG = "SyncManager"
        private const val DEFAULT_PORT = 52934

        @Volatile
        private var instance: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private data class LiveMeshSession(
        val peerId: String,
        val peerName: String,
        val socket: Socket,
        val output: DataOutputStream,
        val sessionKey: String,
        val channelId: String,
        val job: Job
    )

    init {
        updatePairingToken()
        startHosting()
        setupNetworkWatcher()
        if (_continuousAutoSyncEnabled.value) {
            startDiscovery()
        }
        startDatabaseObservation()
    }

    private fun setupNetworkWatcher() {
        discoveryManager.onNetworkStateChanged = { isOnline, isWifiOrEthernet ->
            if (isOnline) {
                updatePairingToken()
                if (_continuousAutoSyncEnabled.value) {
                    startDiscovery()
                    triggerAutoSyncToAllTrustedPeers()
                }
            }
        }
    }

    fun getLocalDevice(): SyncDevice {
        val activeProfile = profileManager.getActiveProfile()
        return SyncDevice(
            id = deviceId,
            name = deviceName,
            ipAddress = P2PDiscoveryManager.getLocalIpAddress(),
            port = activePort,
            avatarEmoji = if (activeProfile.avatarEmoji.length <= 3 && !activeProfile.avatarEmoji.startsWith("/")) activeProfile.avatarEmoji else "DEV",
            isConnected = _connectedDevicesCount.value > 0
        )
    }

    /**
     * Regenerates the one-time security pairing PIN and QR token.
     */
    fun regeneratePin() {
        _pairingPin.value = SyncCryptoManager.generatePairingPin()
        updatePairingToken()
    }

    private fun updatePairingToken() {
        val ip = P2PDiscoveryManager.getLocalIpAddress()
        val token = SyncPairingToken(
            v = 2,
            channelId = UUID.randomUUID().toString(),
            deviceId = deviceId,
            deviceName = deviceName,
            ip = ip,
            port = activePort,
            pin = _pairingPin.value,
            nonce = SyncCryptoManager.generateNonce(8),
            avatarEmoji = getLocalDevice().avatarEmoji
        )
        val json = tokenAdapter.toJson(token)
        _pairingToken.value = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    /**
     * Starts hosting the local P2P Live Mesh Server and advertises via mDNS.
     */
    fun startHosting() {
        if (_isServerRunning.value) return

        serverJob = scope.launch {
            try {
                serverSocket = try {
                    ServerSocket(DEFAULT_PORT).also { activePort = DEFAULT_PORT }
                } catch (e: Exception) {
                    ServerSocket(0).also { activePort = it.localPort }
                }
                _isServerRunning.value = true
                isRunning.set(true)

                val local = getLocalDevice()
                discoveryManager.startAdvertising(local.name, local.id, activePort, local.avatarEmoji)
                updatePairingToken()
                Log.i(TAG, "Live Mesh Sync Server listening on port $activePort")

                while (isRunning.get() && serverSocket?.isClosed == false) {
                    val clientSocket = serverSocket?.accept() ?: break
                    launch { handleIncomingMeshConnection(clientSocket) }
                }
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Server socket error", e)
                }
            }
        }
    }

    /**
     * Starts mDNS discovery for active Lumia devices and triggers automatic mesh connection.
     */
    fun startDiscovery() {
        startHosting()
        _discoveredPeers.value = emptyList()
        _syncState.value = SyncState.Discovering(0)

        discoveryManager.startDiscovery(
            onPeerFound = { peer ->
                if (peer.id != deviceId) {
                    scope.launch {
                        val current = _discoveredPeers.value.toMutableList()
                        val existingIdx = current.indexOfFirst { it.id == peer.id || it.ipAddress == peer.ipAddress }
                        val isPaired = isPeerTrusted(peer.id)
                        val updatedPeer = peer.copy(isPaired = isPaired, isConnected = activeSessions.containsKey(peer.id))

                        if (existingIdx >= 0) {
                            current[existingIdx] = updatedPeer
                        } else {
                            current.add(updatedPeer)
                        }
                        _discoveredPeers.value = current
                        if (_syncState.value is SyncState.Discovering) {
                            _syncState.value = SyncState.Discovering(current.size)
                        }

                        // Automatic zero-touch background mesh connection for permanently paired trusted peers
                        if (_continuousAutoSyncEnabled.value && isPaired) {
                            val trusted = getTrustedPeer(peer.id)
                            if (trusted != null && trusted.autoSyncEnabled) {
                                if (!activeSessions.containsKey(peer.id) && !activeSessions.containsKey(trusted.deviceId)) {
                                    if (connectingPeers.add(trusted.deviceId)) {
                                        try {
                                            connectToTrustedPeer(peer, trusted)
                                        } finally {
                                            connectingPeers.remove(trusted.deviceId)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            onPeerLost = { serviceName ->
                scope.launch {
                    val current = _discoveredPeers.value.filterNot {
                        it.name == serviceName || it.id.startsWith(serviceName) || serviceName.startsWith(it.name)
                    }
                    _discoveredPeers.value = current
                    if (_syncState.value is SyncState.Discovering) {
                        _syncState.value = SyncState.Discovering(current.size)
                    }
                }
            }
        )
    }

    fun stopDiscovery() {
        discoveryManager.stop()
        if (_syncState.value is SyncState.Discovering) {
            _syncState.value = SyncState.Idle
        }
    }

    // --- Live Mesh Connection & Handshake Handling ---

    private suspend fun handleIncomingMeshConnection(socket: Socket) = withContext(Dispatchers.IO) {
        var peerId = ""
        var peerName = ""
        var sessionJob: Job? = null
        try {
            socket.soTimeout = 45000
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())

            val helloMsg = receiveMessage(input) ?: return@withContext
            if (helloMsg.type != "HELLO") return@withContext

            peerId = helloMsg.deviceId
            peerName = helloMsg.deviceName
            val clientNonce = helloMsg.nonce
            val clientRequestsTrusted = helloMsg.isTrustedAuth
            val serverNonce = SyncCryptoManager.generateNonce()

            val challengeMsg = SyncMessage(
                type = "CHALLENGE",
                deviceId = deviceId,
                deviceName = deviceName,
                avatarEmoji = getLocalDevice().avatarEmoji,
                nonce = serverNonce,
                isTrustedAuth = clientRequestsTrusted
            )
            sendMessage(output, challengeMsg)

            val authMsg = receiveMessage(input) ?: return@withContext
            if (authMsg.type != "AUTH") return@withContext

            val trustedPeer = getTrustedPeer(peerId)
            val isUsingPsk = clientRequestsTrusted && trustedPeer != null
            val expectedAuthKey = if (isUsingPsk) trustedPeer!!.preSharedKey else _pairingPin.value

            val isClientValid = SyncCryptoManager.verifyAuthHash(expectedAuthKey, serverNonce, peerId, authMsg.authHash)
            if (!isClientValid) {
                sendMessage(output, SyncMessage(type = "AUTH_FAIL", errorMessage = "Authentication failed"))
                return@withContext
            }

            val serverAuthHash = SyncCryptoManager.calculateAuthHash(expectedAuthKey, clientNonce, deviceId)
            val authOkMsg = SyncMessage(type = "AUTH_OK", authHash = serverAuthHash, isTrustedAuth = isUsingPsk)
            sendMessage(output, authOkMsg)

            val sessionEncryptionKey = if (isUsingPsk) {
                expectedAuthKey
            } else {
                val derivedPsk = SyncCryptoManager.derivePSK(_pairingPin.value, clientNonce, serverNonce)
                val newChannelId = if (helloMsg.channelId.isNotBlank()) helloMsg.channelId else UUID.randomUUID().toString()
                val newTrustedPeer = TrustedPeer(
                    deviceId = peerId,
                    deviceName = peerName,
                    channelId = newChannelId,
                    preSharedKey = derivedPsk,
                    pairedAt = System.currentTimeMillis(),
                    lastSyncAt = System.currentTimeMillis(),
                    autoSyncEnabled = true,
                    avatarEmoji = helloMsg.avatarEmoji
                )
                saveTrustedPeer(newTrustedPeer)
                derivedPsk
            }

            val channelId = trustedPeer?.channelId ?: helloMsg.channelId.ifBlank { UUID.randomUUID().toString() }

            // Clean up any existing session for this peer
            activeSessions.remove(peerId)?.let { oldSession ->
                try { oldSession.socket.close() } catch (ignored: Exception) {}
                oldSession.job.cancel()
            }

            // Register active live mesh session
            sessionJob = Job()
            val session = LiveMeshSession(peerId, peerName, socket, output, sessionEncryptionKey, channelId, sessionJob!!)
            activeSessions[peerId] = session
            updateConnectedDevicesCount()

            // Launch keep-alive heartbeat loop
            sessionJob!!.launch {
                while (isActive && isRunning.get() && socket.isConnected && !socket.isClosed) {
                    delay(25_000L)
                    try {
                        sendMessage(output, SyncMessage(type = "HEARTBEAT", deviceId = deviceId))
                    } catch (e: Exception) {
                        break
                    }
                }
            }

            // Flush offline buffer queue for this peer
            flushOutboxQueueForPeer(session)

            // Start listening on live stream for deltas, acks, and heartbeats
            readMeshStream(input, output, session)
        } catch (e: Exception) {
            Log.e(TAG, "Incoming connection error with $peerName", e)
        } finally {
            sessionJob?.cancel()
            if (peerId.isNotBlank()) {
                activeSessions.remove(peerId)
                updateConnectedDevicesCount()
            }
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    /**
     * Connects as client to a peer device (1-time handshake or trusted auto-reconnect).
     */
    fun connectToPeer(peer: SyncDevice, pin: String, mode: SyncMode = SyncMode.LIVE_MESH_CRDT) {
        connectInternal(peer, pin, isTrustedAuth = false, mode = mode)
    }

    fun connectToTrustedPeer(peer: SyncDevice, trusted: TrustedPeer? = null, mode: SyncMode = SyncMode.LIVE_MESH_CRDT) {
        val targetTrusted = trusted ?: getTrustedPeer(peer.id) ?: return
        connectInternal(peer, targetTrusted.preSharedKey, isTrustedAuth = true, channelId = targetTrusted.channelId, mode = mode)
    }

    private fun connectInternal(
        peer: SyncDevice,
        pinOrPsk: String,
        isTrustedAuth: Boolean,
        channelId: String = "",
        mode: SyncMode = SyncMode.LIVE_MESH_CRDT
    ) {
        if (activeSessions.containsKey(peer.id)) {
            Log.d(TAG, "Session already active with ${peer.name}, skipping connect")
            return
        }

        scope.launch {
            var socket: Socket? = null
            var sessionJob: Job? = null
            try {
                _syncState.value = SyncState.Connecting(peer.name)
                socket = Socket()
                socket.connect(InetSocketAddress(peer.ipAddress, peer.port), 8000)
                socket.soTimeout = 45000

                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                val clientNonce = SyncCryptoManager.generateNonce()

                val helloMsg = SyncMessage(
                    type = "HELLO",
                    deviceId = deviceId,
                    deviceName = deviceName,
                    avatarEmoji = getLocalDevice().avatarEmoji,
                    channelId = channelId,
                    nonce = clientNonce,
                    syncMode = mode.name,
                    isTrustedAuth = isTrustedAuth
                )
                sendMessage(output, helloMsg)

                val challengeMsg = receiveMessage(input) ?: throw IllegalStateException("No challenge response from peer")
                if (challengeMsg.type != "CHALLENGE") throw IllegalStateException("Unexpected frame: ${challengeMsg.type}")
                val serverNonce = challengeMsg.nonce

                _syncState.value = SyncState.Authenticating(peer.name)

                val authHash = SyncCryptoManager.calculateAuthHash(pinOrPsk, serverNonce, deviceId)
                val authMsg = SyncMessage(type = "AUTH", deviceId = deviceId, deviceName = deviceName, authHash = authHash, isTrustedAuth = isTrustedAuth)
                sendMessage(output, authMsg)

                val authOkMsg = receiveMessage(input) ?: throw IllegalStateException("Missing auth confirmation")
                if (authOkMsg.type != "AUTH_OK") throw IllegalStateException("Authentication failed")

                val isServerValid = SyncCryptoManager.verifyAuthHash(pinOrPsk, clientNonce, peer.id, authOkMsg.authHash)
                if (!isServerValid) throw IllegalStateException("Peer cryptographic proof failed")

                val sessionKey = if (isTrustedAuth) {
                    pinOrPsk
                } else {
                    val derivedPsk = SyncCryptoManager.derivePSK(pinOrPsk, clientNonce, serverNonce)
                    val persistentChannelId = if (channelId.isNotBlank()) channelId else UUID.randomUUID().toString()
                    val newTrusted = TrustedPeer(
                        deviceId = peer.id,
                        deviceName = peer.name,
                        channelId = persistentChannelId,
                        preSharedKey = derivedPsk,
                        pairedAt = System.currentTimeMillis(),
                        lastSyncAt = System.currentTimeMillis(),
                        autoSyncEnabled = true,
                        avatarEmoji = peer.avatarEmoji
                    )
                    saveTrustedPeer(newTrusted)
                    derivedPsk
                }

                val activeChannelId = channelId.ifBlank { getTrustedPeer(peer.id)?.channelId ?: UUID.randomUUID().toString() }

                // Clean up any existing session for this peer
                activeSessions.remove(peer.id)?.let { oldSession ->
                    try { oldSession.socket.close() } catch (ignored: Exception) {}
                    oldSession.job.cancel()
                }

                sessionJob = Job()
                val session = LiveMeshSession(peer.id, peer.name, socket, output, sessionKey, activeChannelId, sessionJob!!)
                activeSessions[peer.id] = session
                updateConnectedDevicesCount()
                _syncState.value = SyncState.LiveMeshActive(activeSessions.size)

                // Launch keep-alive heartbeat loop
                sessionJob!!.launch {
                    while (isActive && isRunning.get() && socket.isConnected && !socket.isClosed) {
                        delay(25_000L)
                        try {
                            sendMessage(output, SyncMessage(type = "HEARTBEAT", deviceId = deviceId))
                        } catch (e: Exception) {
                            break
                        }
                    }
                }

                // Flush pending outbox entries
                flushOutboxQueueForPeer(session)

                // Start continuous reader loop
                readMeshStream(input, output, session)
            } catch (e: Exception) {
                Log.e(TAG, "Client connection to ${peer.name} failed", e)
                _syncState.value = SyncState.Error(e.message ?: "Connection error")
            } finally {
                sessionJob?.cancel()
                activeSessions.remove(peer.id)
                updateConnectedDevicesCount()
                try { socket?.close() } catch (ignored: Exception) {}
            }
        }
    }

    private suspend fun readMeshStream(
        input: DataInputStream,
        output: DataOutputStream,
        session: LiveMeshSession
    ) = withContext(Dispatchers.IO) {
        while (isRunning.get() && session.socket.isConnected && !session.socket.isClosed) {
            try {
                val message = receiveMessage(input) ?: break
                when (message.type) {
                    "DELTA_SYNC" -> {
                        val encryptedBase64 = message.deltaPacketEncryptedBase64 ?: continue
                        val ivBase64 = message.ivBase64 ?: continue
                        val decryptNonce = message.nonce.ifBlank { session.channelId }
                        val decryptedBytes = SyncCryptoManager.decryptPayload(encryptedBase64, ivBase64, session.sessionKey, decryptNonce)
                        val packetJson = String(decryptedBytes, Charsets.UTF_8)
                        val packet = deltaPacketAdapter.fromJson(packetJson) ?: continue

                        _isSyncing.value = true
                        isApplyingRemoteMerge.set(true)
                        val report = try {
                            val db = AppDatabase.getDatabase(context, profileManager.getActiveProfileId())
                            SyncMergeEngine.mergeDeltaPacket(db.scholarDao(), packet, session.peerName)
                        } finally {
                            scope.launch {
                                delay(600)
                                isApplyingRemoteMerge.set(false)
                            }
                        }

                        val ackMsg = SyncMessage(
                            type = "DELTA_ACK",
                            deviceId = deviceId,
                            ackMutationIds = packet.deltas.map { it.mutationId }
                        )
                        sendMessage(output, ackMsg)

                        val now = System.currentTimeMillis()
                        _lastSyncedTimestamp.value = now
                        prefs.edit().putLong("last_synced_timestamp", now).apply()
                        updateTrustedPeerSyncTime(session.peerId)
                        _isSyncing.value = false

                        addHistoryRecord(
                            SyncHistoryRecord(
                                peerDeviceName = session.peerName,
                                summary = "CRDT Delta Merge: +${report.deltasApplied} entities synchronized",
                                isSuccess = true
                            )
                        )
                    }
                    "DELTA_ACK" -> {
                        message.ackMutationIds?.let { ackIds ->
                            pruneAcknowledgedOutboxEntries(session.peerId, ackIds)
                        }
                    }
                    "FULL_SYNC_REQ" -> {
                        val backup = createFullLocalBackup()
                        val backupJson = backupAdapter.toJson(backup)
                        val snapNonce = SyncCryptoManager.generateNonce()
                        val (encrypted, iv) = SyncCryptoManager.encryptPayload(backupJson.toByteArray(Charsets.UTF_8), session.sessionKey, snapNonce)
                        val syncDataMsg = SyncMessage(
                            type = "SYNC_DATA",
                            deviceId = deviceId,
                            nonce = snapNonce,
                            payloadEncryptedBase64 = encrypted,
                            ivBase64 = iv
                        )
                        sendMessage(output, syncDataMsg)
                    }
                    "SYNC_DATA" -> {
                        val encrypted = message.payloadEncryptedBase64 ?: continue
                        val iv = message.ivBase64 ?: continue
                        val snapNonce = message.nonce.ifBlank { session.channelId }
                        val decryptedBytes = SyncCryptoManager.decryptPayload(encrypted, iv, session.sessionKey, snapNonce)
                        val backup = backupAdapter.fromJson(String(decryptedBytes, Charsets.UTF_8)) ?: continue

                        isApplyingRemoteMerge.set(true)
                        val report = try {
                            SyncMergeEngine.mergeFullApp(context, profileManager, backup, session.peerName, "SMART_MERGE")
                        } finally {
                            scope.launch {
                                delay(600)
                                isApplyingRemoteMerge.set(false)
                            }
                        }

                        val ackMsg = SyncMessage(
                            type = "SYNC_ACK",
                            deviceId = deviceId,
                            reportJson = reportAdapter.toJson(report)
                        )
                        sendMessage(output, ackMsg)

                        val now = System.currentTimeMillis()
                        _lastSyncedTimestamp.value = now
                        prefs.edit().putLong("last_synced_timestamp", now).apply()
                        updateTrustedPeerSyncTime(session.peerId)

                        addHistoryRecord(
                            SyncHistoryRecord(
                                peerDeviceName = session.peerName,
                                summary = "Snapshot Sync: +${report.coursesMerged} courses, +${report.tasksMerged} tasks",
                                isSuccess = true
                            )
                        )
                    }
                    "SYNC_ACK" -> {
                        val now = System.currentTimeMillis()
                        _lastSyncedTimestamp.value = now
                        prefs.edit().putLong("last_synced_timestamp", now).apply()
                        updateTrustedPeerSyncTime(session.peerId)
                    }
                    "HEARTBEAT" -> {
                        sendMessage(output, SyncMessage(type = "HEARTBEAT_ACK", deviceId = deviceId))
                    }
                    "HEARTBEAT_ACK" -> {
                        // Keep-alive acknowledged
                    }
                    "DISCONNECT" -> {
                        break
                    }
                }
            } catch (e: Exception) {
                break
            }
        }
    }

    // --- Real-Time CRDT Delta Dispatch & Offline Outbox Queue ---

    /**
     * Broadcasts a local entity mutation in real-time to all paired peers in the live mesh.
     * If a paired peer is offline, queues the mutation in the persistent outbox buffer.
     */
    fun broadcastEntityMutation(entityType: SyncEntityType, operation: SyncOperation, entity: Any, entityId: String) {
        if (isApplyingRemoteMerge.get()) return
        val globalId = SyncMergeEngine.generateGlobalId(entityType, entity).ifBlank { entityId }
        val delta = SyncMergeEngine.createDelta(
            entityType = entityType,
            operation = operation,
            entity = entity,
            entityGlobalId = globalId,
            originDeviceId = deviceId
        )
        broadcastDelta(delta)
    }

    /**
     * Dispatches a SyncDelta to active sessions or queues in outbox for offline peers.
     */
    fun broadcastDelta(delta: SyncDelta) {
        val peers = _pairedDevices.value
        if (peers.isEmpty()) return

        for (peer in peers) {
            val session = activeSessions[peer.deviceId]
            if (session != null) {
                scope.launch {
                    val packet = SyncDeltaPacket(
                        channelId = session.channelId,
                        senderDeviceId = deviceId,
                        deltas = listOf(delta)
                    )
                    dispatchPacketToSession(session, packet)
                }
            } else {
                queueOutboxEntry(peer.deviceId, delta)
            }
        }
    }

    private suspend fun dispatchPacketToSession(session: LiveMeshSession, packet: SyncDeltaPacket) = withContext(Dispatchers.IO) {
        try {
            val json = deltaPacketAdapter.toJson(packet)
            val packetNonce = SyncCryptoManager.generateNonce()
            val (encrypted, iv) = SyncCryptoManager.encryptPayload(json.toByteArray(Charsets.UTF_8), session.sessionKey, packetNonce)
            val msg = SyncMessage(
                type = "DELTA_SYNC",
                deviceId = deviceId,
                channelId = session.channelId,
                nonce = packetNonce,
                deltaPacketEncryptedBase64 = encrypted,
                ivBase64 = iv
            )
            sendMessage(session.output, msg)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to dispatch delta packet to ${session.peerName}, queueing in outbox", e)
            packet.deltas.forEach { queueOutboxEntry(session.peerId, it) }
        }
    }

    private fun queueOutboxEntry(targetDeviceId: String, delta: SyncDelta) {
        val current = _outboxQueue.value.toMutableList()
        // Deduplicate identical pending mutations
        val existingIdx = current.indexOfFirst { it.targetDeviceId == targetDeviceId && it.delta.entityGlobalId == delta.entityGlobalId }
        if (existingIdx >= 0) {
            current[existingIdx] = OfflineMutationEntry(targetDeviceId = targetDeviceId, delta = delta)
        } else {
            current.add(OfflineMutationEntry(targetDeviceId = targetDeviceId, delta = delta))
        }
        _outboxQueue.value = current
        saveOutbox(current)
    }

    private fun pruneAcknowledgedOutboxEntries(targetDeviceId: String, ackIds: List<String>) {
        val ackSet = ackIds.toSet()
        val current = _outboxQueue.value.filterNot { it.targetDeviceId == targetDeviceId && ackSet.contains(it.delta.mutationId) }
        _outboxQueue.value = current
        saveOutbox(current)
    }

    private suspend fun flushOutboxQueueForPeer(session: LiveMeshSession) = withContext(Dispatchers.IO) {
        val pendingForPeer = _outboxQueue.value.filter { it.targetDeviceId == session.peerId || it.targetDeviceId == "*" }
        if (pendingForPeer.isEmpty()) return@withContext

        val deltas = pendingForPeer.map { it.delta }
        val packet = SyncDeltaPacket(
            channelId = session.channelId,
            senderDeviceId = deviceId,
            deltas = deltas
        )
        dispatchPacketToSession(session, packet)
    }

    // --- Reactive Room Database Change Observer across All Entities ---

    private fun startDatabaseObservation() {
        dbObservationJob?.cancel()
        dbObservationJob = scope.launch {
            val activeId = profileManager.getActiveProfileId()
            val db = AppDatabase.getDatabase(context, activeId)
            val dao = db.scholarDao()

            var lastCourses = dao.exportAllCourses().associateBy { it.id }
            var lastSubjects = dao.exportAllSubjects().associateBy { it.id }
            var lastChapters = dao.exportAllChapters().associateBy { it.id }
            var lastTopics = dao.exportAllTopics().associateBy { it.id }
            var lastAssignments = dao.exportAllAssignments().associateBy { it.id }
            var lastTasks = dao.exportAllTasks().associateBy { it.id }
            var lastAttendance = dao.exportAllAttendance().associateBy { "${it.courseId}_${it.dateMillis}" }
            var lastPomodoro = dao.exportAllPomodoro().associateBy { "${it.dateMillis}_${it.durationMinutes}" }
            var lastNotes = dao.exportAllNotes().associateBy { it.id }
            var lastTests = dao.exportAllTestRecords().associateBy { it.id }
            var lastTags = dao.exportAllTagCustomizations().associateBy { it.tagName }
            var lastAttachments = dao.exportAllAttachments().associateBy { it.id }

            // 1. Courses
            launch {
                dao.getAllCourses().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastCourses.isNotEmpty()) {
                        currentList.forEach { c ->
                            if (lastCourses[c.id] != c) {
                                broadcastEntityMutation(SyncEntityType.COURSE, SyncOperation.UPSERT, c, c.name)
                            }
                        }
                        lastCourses.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastCourses[oldId]?.let { broadcastEntityMutation(SyncEntityType.COURSE, SyncOperation.DELETE, it, it.name) }
                            }
                        }
                    }
                    lastCourses = currentMap
                }
            }

            // 2. Subjects
            launch {
                dao.getAllSubjects().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastSubjects.isNotEmpty()) {
                        currentList.forEach { s ->
                            if (lastSubjects[s.id] != s) {
                                broadcastEntityMutation(SyncEntityType.SUBJECT, SyncOperation.UPSERT, s, s.name)
                            }
                        }
                        lastSubjects.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastSubjects[oldId]?.let { broadcastEntityMutation(SyncEntityType.SUBJECT, SyncOperation.DELETE, it, it.name) }
                            }
                        }
                    }
                    lastSubjects = currentMap
                }
            }

            // 3. Chapters
            launch {
                dao.getAllChaptersFlow().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastChapters.isNotEmpty()) {
                        currentList.forEach { ch ->
                            if (lastChapters[ch.id] != ch) {
                                broadcastEntityMutation(SyncEntityType.CHAPTER, SyncOperation.UPSERT, ch, ch.name)
                            }
                        }
                        lastChapters.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastChapters[oldId]?.let { broadcastEntityMutation(SyncEntityType.CHAPTER, SyncOperation.DELETE, it, it.name) }
                            }
                        }
                    }
                    lastChapters = currentMap
                }
            }

            // 4. Topics
            launch {
                dao.getAllTopicsReactive().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastTopics.isNotEmpty()) {
                        currentList.forEach { tp ->
                            if (lastTopics[tp.id] != tp) {
                                broadcastEntityMutation(SyncEntityType.TOPIC, SyncOperation.UPSERT, tp, tp.title)
                            }
                        }
                        lastTopics.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastTopics[oldId]?.let { broadcastEntityMutation(SyncEntityType.TOPIC, SyncOperation.DELETE, it, it.title) }
                            }
                        }
                    }
                    lastTopics = currentMap
                }
            }

            // 5. Assignments
            launch {
                dao.getAllAssignments().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastAssignments.isNotEmpty()) {
                        currentList.forEach { a ->
                            if (lastAssignments[a.id] != a) {
                                broadcastEntityMutation(SyncEntityType.ASSIGNMENT, SyncOperation.UPSERT, a, a.title)
                            }
                        }
                        lastAssignments.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastAssignments[oldId]?.let { broadcastEntityMutation(SyncEntityType.ASSIGNMENT, SyncOperation.DELETE, it, it.title) }
                            }
                        }
                    }
                    lastAssignments = currentMap
                }
            }

            // 6. Tasks
            launch {
                dao.getAllTasks().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastTasks.isNotEmpty()) {
                        currentList.forEach { t ->
                            if (lastTasks[t.id] != t) {
                                broadcastEntityMutation(SyncEntityType.TASK, SyncOperation.UPSERT, t, t.title)
                            }
                        }
                        lastTasks.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastTasks[oldId]?.let { broadcastEntityMutation(SyncEntityType.TASK, SyncOperation.DELETE, it, it.title) }
                            }
                        }
                    }
                    lastTasks = currentMap
                }
            }

            // 7. Attendance
            launch {
                dao.getAllAttendanceRecords().collect { currentList ->
                    val currentMap = currentList.associateBy { "${it.courseId}_${it.dateMillis}" }
                    if (!isApplyingRemoteMerge.get() && lastAttendance.isNotEmpty()) {
                        currentList.forEach { att ->
                            val key = "${att.courseId}_${att.dateMillis}"
                            if (lastAttendance[key] != att) {
                                broadcastEntityMutation(SyncEntityType.ATTENDANCE, SyncOperation.UPSERT, att, key)
                            }
                        }
                        lastAttendance.keys.forEach { oldKey ->
                            if (!currentMap.containsKey(oldKey)) {
                                lastAttendance[oldKey]?.let { broadcastEntityMutation(SyncEntityType.ATTENDANCE, SyncOperation.DELETE, it, oldKey) }
                            }
                        }
                    }
                    lastAttendance = currentMap
                }
            }

            // 8. Pomodoro Focus Sessions
            launch {
                dao.getAllPomodoroSessions().collect { currentList ->
                    val currentMap = currentList.associateBy { "${it.dateMillis}_${it.durationMinutes}" }
                    if (!isApplyingRemoteMerge.get() && lastPomodoro.isNotEmpty()) {
                        currentList.forEach { pomo ->
                            val key = "${pomo.dateMillis}_${pomo.durationMinutes}"
                            if (!lastPomodoro.containsKey(key)) {
                                broadcastEntityMutation(SyncEntityType.POMODORO, SyncOperation.UPSERT, pomo, key)
                            }
                        }
                    }
                    lastPomodoro = currentMap
                }
            }

            // 9. Notes
            launch {
                dao.getAllNotes().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastNotes.isNotEmpty()) {
                        currentList.forEach { n ->
                            if (lastNotes[n.id] != n) {
                                broadcastEntityMutation(SyncEntityType.NOTE, SyncOperation.UPSERT, n, n.content)
                            }
                        }
                        lastNotes.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastNotes[oldId]?.let { broadcastEntityMutation(SyncEntityType.NOTE, SyncOperation.DELETE, it, it.content) }
                            }
                        }
                    }
                    lastNotes = currentMap
                }
            }

            // 10. Test Records
            launch {
                dao.getAllTestRecordsReactive().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastTests.isNotEmpty()) {
                        currentList.forEach { tr ->
                            if (lastTests[tr.id] != tr) {
                                broadcastEntityMutation(SyncEntityType.TEST_RECORD, SyncOperation.UPSERT, tr, tr.title)
                            }
                        }
                        lastTests.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastTests[oldId]?.let { broadcastEntityMutation(SyncEntityType.TEST_RECORD, SyncOperation.DELETE, it, it.title) }
                            }
                        }
                    }
                    lastTests = currentMap
                }
            }

            // 11. Tag Customizations
            launch {
                dao.getAllTagCustomizations().collect { currentList ->
                    val currentMap = currentList.associateBy { it.tagName }
                    if (!isApplyingRemoteMerge.get() && lastTags.isNotEmpty()) {
                        currentList.forEach { tag ->
                            if (lastTags[tag.tagName] != tag) {
                                broadcastEntityMutation(SyncEntityType.TAG_CUSTOMIZATION, SyncOperation.UPSERT, tag, tag.tagName)
                            }
                        }
                        lastTags.keys.forEach { oldTag ->
                            if (!currentMap.containsKey(oldTag)) {
                                lastTags[oldTag]?.let { broadcastEntityMutation(SyncEntityType.TAG_CUSTOMIZATION, SyncOperation.DELETE, it, oldTag) }
                            }
                        }
                    }
                    lastTags = currentMap
                }
            }

            // 12. Attachments
            launch {
                dao.getAllAttachments().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (!isApplyingRemoteMerge.get() && lastAttachments.isNotEmpty()) {
                        currentList.forEach { att ->
                            if (lastAttachments[att.id] != att) {
                                broadcastEntityMutation(SyncEntityType.ATTACHMENT, SyncOperation.UPSERT, att, att.name)
                            }
                        }
                        lastAttachments.keys.forEach { oldId ->
                            if (!currentMap.containsKey(oldId)) {
                                lastAttachments[oldId]?.let { broadcastEntityMutation(SyncEntityType.ATTACHMENT, SyncOperation.DELETE, it, it.name) }
                            }
                        }
                    }
                    lastAttachments = currentMap
                }
            }
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

    // --- Pairing Token Scanning / Paste ---

    fun pairWithToken(rawToken: String, mode: SyncMode = SyncMode.LIVE_MESH_CRDT) {
        try {
            // Check if encrypted/plain QR metadata format from PairedDeviceStore
            val qrMeta = pairedDeviceStore.parseEncryptedQrPayload(rawToken)
            if (qrMeta != null) {
                if (qrMeta.secretKey.isNotBlank()) {
                    val trustedPeer = TrustedPeer(
                        deviceId = qrMeta.deviceId,
                        deviceName = qrMeta.deviceName,
                        channelId = qrMeta.channelId,
                        preSharedKey = qrMeta.secretKey,
                        pairedAt = System.currentTimeMillis(),
                        lastSyncAt = 0L,
                        autoSyncEnabled = true,
                        avatarEmoji = qrMeta.avatarEmoji
                    )
                    saveTrustedPeer(trustedPeer)
                    val peer = SyncDevice(
                        id = qrMeta.deviceId,
                        name = qrMeta.deviceName,
                        ipAddress = qrMeta.ip.ifBlank { P2PDiscoveryManager.getLocalIpAddress() },
                        port = qrMeta.port,
                        avatarEmoji = qrMeta.avatarEmoji
                    )
                    connectToTrustedPeer(peer, trustedPeer, mode)
                    return
                } else if (qrMeta.pin.isNotBlank()) {
                    val peer = SyncDevice(
                        id = qrMeta.deviceId,
                        name = qrMeta.deviceName,
                        ipAddress = qrMeta.ip.ifBlank { P2PDiscoveryManager.getLocalIpAddress() },
                        port = qrMeta.port,
                        avatarEmoji = qrMeta.avatarEmoji
                    )
                    connectToPeer(peer, qrMeta.pin, mode)
                    return
                }
            }

            // Fallback to Base64 SyncPairingToken parse
            val jsonString = String(Base64.decode(rawToken.trim(), Base64.DEFAULT), Charsets.UTF_8)
            val token = tokenAdapter.fromJson(jsonString) ?: throw IllegalArgumentException("Invalid QR / Pairing Token")

            val peer = SyncDevice(
                id = token.deviceId,
                name = token.deviceName,
                ipAddress = token.ip,
                port = token.port,
                avatarEmoji = token.avatarEmoji
            )
            connectToPeer(peer, token.pin, mode)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Invalid pairing token: ${e.localizedMessage}")
        }
    }

    fun triggerAutoSyncToAllTrustedPeers() {
        val onlineTrusted = _discoveredPeers.value.filter { isPeerTrusted(it.id) }
        onlineTrusted.forEach { peer ->
            val trusted = getTrustedPeer(peer.id)
            if (trusted != null && trusted.autoSyncEnabled && !activeSessions.containsKey(peer.id) && !activeSessions.containsKey(trusted.deviceId)) {
                if (connectingPeers.add(trusted.deviceId)) {
                    try {
                        connectToTrustedPeer(peer, trusted)
                    } finally {
                        connectingPeers.remove(trusted.deviceId)
                    }
                }
            }
        }
    }

    // --- Trusted Peer Management ---

    fun getTrustedPeer(deviceId: String): TrustedPeer? = _pairedDevices.value.find {
        it.deviceId == deviceId ||
        it.deviceId.equals(deviceId, ignoreCase = true) ||
        deviceId.startsWith("Lumia-${it.deviceId.take(6)}") ||
        it.deviceId.startsWith(deviceId.removePrefix("Lumia-"))
    } ?: pairedDeviceStore.getTrustedPeer(deviceId)

    fun isPeerTrusted(deviceId: String): Boolean = _pairedDevices.value.any {
        it.deviceId == deviceId ||
        it.deviceId.equals(deviceId, ignoreCase = true) ||
        deviceId.startsWith("Lumia-${it.deviceId.take(6)}") ||
        it.deviceId.startsWith(deviceId.removePrefix("Lumia-"))
    } || pairedDeviceStore.isDevicePaired(deviceId)

    fun saveTrustedPeer(peer: TrustedPeer) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == peer.deviceId }
        if (idx >= 0) current[idx] = peer else current.add(peer)
        _pairedDevices.value = current
        saveTrustedPeersList(current)
        pairedDeviceStore.saveTrustedPeer(peer, peer.channelId)
    }

    fun removeTrustedPeer(deviceId: String) {
        val current = _pairedDevices.value.filterNot { it.deviceId == deviceId }
        _pairedDevices.value = current
        saveTrustedPeersList(current)
        pairedDeviceStore.removeTrustedPeer(deviceId)
        activeSessions[deviceId]?.socket?.close()
        activeSessions.remove(deviceId)
        updateConnectedDevicesCount()
    }

    fun updateTrustedPeerSyncTime(deviceId: String) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            val now = System.currentTimeMillis()
            current[idx] = current[idx].copy(lastSyncAt = now)
            _pairedDevices.value = current
            saveTrustedPeersList(current)
            pairedDeviceStore.updateLastSyncTime(deviceId, now)
        }
    }

    fun toggleTrustedPeerAutoSync(deviceId: String, enabled: Boolean) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(autoSyncEnabled = enabled)
            _pairedDevices.value = current
            saveTrustedPeersList(current)
            pairedDeviceStore.updateAutoSync(deviceId, enabled)
        }
    }

    fun setContinuousAutoSyncEnabled(enabled: Boolean) {
        _continuousAutoSyncEnabled.value = enabled
        prefs.edit().putBoolean("continuous_auto_sync", enabled).apply()
        pairedDeviceStore.isGlobalContinuousAutoSyncEnabled = enabled
        if (enabled) {
            startDiscovery()
            triggerAutoSyncToAllTrustedPeers()
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    private fun updateConnectedDevicesCount() {
        _connectedDevicesCount.value = activeSessions.size
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
        if (length <= 0 || length > 50 * 1024 * 1024) return null
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return messageAdapter.fromJson(String(bytes, Charsets.UTF_8))
    }

    private fun loadTrustedPeers(): List<TrustedPeer> {
        val storePeers = pairedDeviceStore.getAllTrustedPeers()
        val json = prefs.getString("trusted_peers_json", null)
        val legacyPeers = if (!json.isNullOrBlank()) {
            try { trustedPeersAdapter.fromJson(json) ?: emptyList() } catch (e: Exception) { emptyList() }
        } else emptyList()

        val combinedMap = mutableMapOf<String, TrustedPeer>()
        legacyPeers.forEach { combinedMap[it.deviceId] = it }
        storePeers.forEach { combinedMap[it.deviceId] = it }
        return combinedMap.values.toList()
    }

    private fun saveTrustedPeersList(list: List<TrustedPeer>) {
        try { prefs.edit().putString("trusted_peers_json", trustedPeersAdapter.toJson(list)).apply() } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadOutbox(): List<OfflineMutationEntry> {
        val json = prefs.getString("lumia_sync_outbox_json", null) ?: return emptyList()
        return try { outboxAdapter.fromJson(json) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    private fun saveOutbox(list: List<OfflineMutationEntry>) {
        try { prefs.edit().putString("lumia_sync_outbox_json", outboxAdapter.toJson(list)).apply() } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadHistory(): List<SyncHistoryRecord> {
        val json = prefs.getString("sync_history_json", null) ?: return emptyList()
        return try { historyAdapter.fromJson(json) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    private fun addHistoryRecord(record: SyncHistoryRecord) {
        val current = _syncHistory.value.toMutableList()
        current.add(0, record)
        val trimmed = current.take(20)
        _syncHistory.value = trimmed
        try { prefs.edit().putString("sync_history_json", historyAdapter.toJson(trimmed)).apply() } catch (e: Exception) { e.printStackTrace() }
    }

    fun cleanup() {
        discoveryManager.stop()
        isRunning.set(false)
        serverJob?.cancel()
        dbObservationJob?.cancel()
        activeSessions.values.forEach { session ->
            session.job.cancel()
            try { session.socket.close() } catch (ignored: Exception) {}
        }
        activeSessions.clear()
        connectingPeers.clear()
        try { serverSocket?.close() } catch (ignored: Exception) {}
        serverSocket = null
        _isServerRunning.value = false
        updateConnectedDevicesCount()
    }
}