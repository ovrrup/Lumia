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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val messageAdapter = moshi.adapter(SyncMessage::class.java)
    private val deltaPacketAdapter = moshi.adapter(SyncDeltaPacket::class.java)
    private val tokenAdapter = moshi.adapter(SyncPairingToken::class.java)
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
    private val activeSessions = ConcurrentHashMap<String, LiveMeshSession>()

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
        if (_continuousAutoSyncEnabled.value) {
            startDiscovery()
        }
        startDatabaseObservation()
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

                        // Automatic background mesh connection for permanently paired trusted peers
                        if (_continuousAutoSyncEnabled.value && isPaired) {
                            val trusted = getTrustedPeer(peer.id)
                            if (trusted != null && trusted.autoSyncEnabled && !activeSessions.containsKey(peer.id)) {
                                connectToTrustedPeer(peer, trusted)
                            }
                        }
                    }
                }
            },
            onPeerLost = { serviceName ->
                scope.launch {
                    val current = _discoveredPeers.value.filterNot { it.name == serviceName || it.id.startsWith(serviceName) }
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

            // Register active live mesh session
            val sessionJob = Job()
            val session = LiveMeshSession(peerId, peerName, socket, output, sessionEncryptionKey, channelId, sessionJob)
            activeSessions[peerId] = session
            updateConnectedDevicesCount()

            // Flush offline buffer queue for this peer
            flushOutboxQueueForPeer(session)

            // Start listening on live stream for deltas, acks, and heartbeats
            readMeshStream(input, output, session)
        } catch (e: Exception) {
            Log.e(TAG, "Incoming connection error with $peerName", e)
        } finally {
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
        scope.launch {
            var socket: Socket? = null
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

                val sessionJob = Job()
                val session = LiveMeshSession(peer.id, peer.name, socket, output, sessionKey, activeChannelId, sessionJob)
                activeSessions[peer.id] = session
                updateConnectedDevicesCount()
                _syncState.value = SyncState.LiveMeshActive(activeSessions.size)

                // Flush pending outbox entries
                flushOutboxQueueForPeer(session)

                // Start continuous reader loop
                readMeshStream(input, output, session)
            } catch (e: Exception) {
                Log.e(TAG, "Client connection to ${peer.name} failed", e)
                _syncState.value = SyncState.Error(e.message ?: "Connection error")
            } finally {
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
                        val decryptedBytes = SyncCryptoManager.decryptPayload(encryptedBase64, ivBase64, session.sessionKey, session.peerId)
                        val packetJson = String(decryptedBytes, Charsets.UTF_8)
                        val packet = deltaPacketAdapter.fromJson(packetJson) ?: continue

                        _isSyncing.value = true
                        val db = AppDatabase.getDatabase(context, profileManager.getActiveProfileId())
                        val report = SyncMergeEngine.mergeDeltaPacket(db.scholarDao(), packet, session.peerName)

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
                    "HEARTBEAT" -> {
                        sendMessage(output, SyncMessage(type = "HEARTBEAT_ACK", deviceId = deviceId))
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
            val (encrypted, iv) = SyncCryptoManager.encryptPayload(json.toByteArray(Charsets.UTF_8), session.sessionKey, session.peerId)
            val msg = SyncMessage(
                type = "DELTA_SYNC",
                deviceId = deviceId,
                channelId = session.channelId,
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
        current.add(OfflineMutationEntry(targetDeviceId = targetDeviceId, delta = delta))
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

    // --- Reactive Room Database Change Observer ---

    private fun startDatabaseObservation() {
        scope.launch {
            val activeId = profileManager.getActiveProfileId()
            val db = AppDatabase.getDatabase(context, activeId)
            val dao = db.scholarDao()

            var lastCourses = dao.exportAllCourses().associateBy { it.id }
            var lastSubjects = dao.exportAllSubjects().associateBy { it.id }
            var lastTasks = dao.exportAllTasks().associateBy { it.id }
            var lastAssignments = dao.exportAllAssignments().associateBy { it.id }

            launch {
                dao.getAllCourses().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (lastCourses.isNotEmpty()) {
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

            launch {
                dao.getAllTasks().collect { currentList ->
                    val currentMap = currentList.associateBy { it.id }
                    if (lastTasks.isNotEmpty()) {
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
        }
    }

    // --- Pairing Token Scanning / Paste ---

    fun pairWithToken(rawToken: String, mode: SyncMode = SyncMode.LIVE_MESH_CRDT) {
        try {
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
            if (trusted != null && !activeSessions.containsKey(peer.id)) {
                connectToTrustedPeer(peer, trusted)
            }
        }
    }

    // --- Trusted Peer Management ---

    fun getTrustedPeer(deviceId: String): TrustedPeer? = _pairedDevices.value.find { it.deviceId == deviceId }

    fun isPeerTrusted(deviceId: String): Boolean = _pairedDevices.value.any { it.deviceId == deviceId }

    fun saveTrustedPeer(peer: TrustedPeer) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == peer.deviceId }
        if (idx >= 0) current[idx] = peer else current.add(peer)
        _pairedDevices.value = current
        saveTrustedPeersList(current)
    }

    fun removeTrustedPeer(deviceId: String) {
        val current = _pairedDevices.value.filterNot { it.deviceId == deviceId }
        _pairedDevices.value = current
        saveTrustedPeersList(current)
        activeSessions[deviceId]?.socket?.close()
        activeSessions.remove(deviceId)
        updateConnectedDevicesCount()
    }

    fun updateTrustedPeerSyncTime(deviceId: String) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(lastSyncAt = System.currentTimeMillis())
            _pairedDevices.value = current
            saveTrustedPeersList(current)
        }
    }

    fun toggleTrustedPeerAutoSync(deviceId: String, enabled: Boolean) {
        val current = _pairedDevices.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(autoSyncEnabled = enabled)
            _pairedDevices.value = current
            saveTrustedPeersList(current)
        }
    }

    fun setContinuousAutoSyncEnabled(enabled: Boolean) {
        _continuousAutoSyncEnabled.value = enabled
        prefs.edit().putBoolean("continuous_auto_sync", enabled).apply()
        if (enabled) startDiscovery()
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
        val json = prefs.getString("trusted_peers_json", null) ?: return emptyList()
        return try { trustedPeersAdapter.fromJson(json) ?: emptyList() } catch (e: Exception) { emptyList() }
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
        activeSessions.values.forEach { it.socket.close() }
        activeSessions.clear()
        try { serverSocket?.close() } catch (ignored: Exception) {}
        serverSocket = null
        _isServerRunning.value = false
        updateConnectedDevicesCount()
    }
}