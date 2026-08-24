package lumia.tracker.sync

import android.content.Context
import android.os.Build
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lumia.tracker.data.ProfileManager
import lumia.tracker.sync.crypto.SyncCryptoManager
import lumia.tracker.sync.discovery.P2PDiscoveryManager
import lumia.tracker.sync.model.*
import lumia.tracker.sync.p2p.P2PDataChannelEngine
import java.util.UUID

/**
 * Main coordinator for Lumia's Multi-Device P2P / WebRTC Synchronization System.
 * Supports permanent 1-time mutual handshake pairing and all-time continuous background auto-sync.
 */
class SyncManager(private val context: Context) {

    private val profileManager = ProfileManager(context)
    private val discoveryManager = P2PDiscoveryManager(context)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val tokenAdapter = moshi.adapter(SyncPairingToken::class.java)
    private val historyAdapter = moshi.adapter<List<SyncHistoryRecord>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, SyncHistoryRecord::class.java)
    )
    private val trustedPeersAdapter = moshi.adapter<List<TrustedPeer>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, TrustedPeer::class.java)
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

    private val _pairingPin = MutableStateFlow(SyncCryptoManager.generatePairingPin())
    val pairingPin: StateFlow<String> = _pairingPin.asStateFlow()

    private val _pairingToken = MutableStateFlow("")
    val pairingToken: StateFlow<String> = _pairingToken.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<SyncDevice>>(emptyList())
    val discoveredPeers: StateFlow<List<SyncDevice>> = _discoveredPeers.asStateFlow()

    private val _trustedPeers = MutableStateFlow<List<TrustedPeer>>(loadTrustedPeers())
    val trustedPeers: StateFlow<List<TrustedPeer>> = _trustedPeers.asStateFlow()

    private val _continuousAutoSyncEnabled = MutableStateFlow(prefs.getBoolean("continuous_auto_sync", true))
    val continuousAutoSyncEnabled: StateFlow<Boolean> = _continuousAutoSyncEnabled.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncHistory = MutableStateFlow<List<SyncHistoryRecord>>(loadHistory())
    val syncHistory: StateFlow<List<SyncHistoryRecord>> = _syncHistory.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private var lastAutoSyncTimestamps = mutableMapOf<String, Long>()

    private val dataChannelEngine = P2PDataChannelEngine(
        context = context,
        profileManager = profileManager,
        trustedPeerProvider = { peerId -> getTrustedPeer(peerId) },
        onPeerPaired = { newPeer -> saveTrustedPeer(newPeer) },
        onPeerSynced = { peerId -> updateTrustedPeerSyncTime(peerId) },
        onStateChanged = { newState ->
            scope.launch {
                _syncState.value = newState
                if (newState is SyncState.Success) {
                    addHistoryRecord(
                        SyncHistoryRecord(
                            peerDeviceName = newState.report.peerDeviceName,
                            summary = buildReportSummary(newState.report),
                            isSuccess = true
                        )
                    )
                } else if (newState is SyncState.Error) {
                    addHistoryRecord(
                        SyncHistoryRecord(
                            peerDeviceName = "Sync Operation",
                            summary = newState.message,
                            isSuccess = false
                        )
                    )
                }
            }
        }
    )

    init {
        updatePairingToken()
        startHosting()
        if (_continuousAutoSyncEnabled.value) {
            startDiscovery()
        }
    }

    fun getLocalDevice(): SyncDevice {
        val activeProfile = profileManager.getActiveProfile()
        return SyncDevice(
            id = deviceId,
            name = deviceName,
            ipAddress = P2PDiscoveryManager.getLocalIpAddress(),
            port = dataChannelEngine.activePort,
            avatarEmoji = if (activeProfile.avatarEmoji.length <= 3 && !activeProfile.avatarEmoji.startsWith("/")) activeProfile.avatarEmoji else "DEV"
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
            deviceId = deviceId,
            deviceName = deviceName,
            ip = ip,
            port = dataChannelEngine.activePort,
            pin = _pairingPin.value,
            nonce = SyncCryptoManager.generateNonce(8),
            avatarEmoji = getLocalDevice().avatarEmoji
        )
        val json = tokenAdapter.toJson(token)
        _pairingToken.value = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    /**
     * Starts hosting the local P2P sync server and advertises on local Wi-Fi.
     */
    fun startHosting() {
        if (_isServerRunning.value) return
        val local = getLocalDevice()
        dataChannelEngine.startServer({ _pairingPin.value }, local)
        discoveryManager.startAdvertising(local.name, local.id, dataChannelEngine.activePort, local.avatarEmoji)
        _isServerRunning.value = true
        updatePairingToken()
    }

    /**
     * Starts active scan for nearby Lumia devices and triggers auto-sync for trusted peers.
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
                        if (existingIdx >= 0) {
                            current[existingIdx] = peer
                        } else {
                            current.add(peer)
                        }
                        _discoveredPeers.value = current
                        if (_syncState.value is SyncState.Discovering) {
                            _syncState.value = SyncState.Discovering(current.size)
                        }

                        // Check for automatic silent background sync if peer is permanently trusted
                        checkAndTriggerAutoSync(peer)
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

    /**
     * Evaluates whether to automatically synchronize in background with a discovered trusted peer.
     */
    private fun checkAndTriggerAutoSync(peer: SyncDevice) {
        if (!_continuousAutoSyncEnabled.value) return
        val trusted = getTrustedPeer(peer.id) ?: return
        if (!trusted.autoSyncEnabled) return

        // 30-second cooldown per peer to avoid excessive re-syncing loops
        val lastSync = lastAutoSyncTimestamps[peer.id] ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastSync < 30_000) return

        // Only trigger if idle or discovering
        val state = _syncState.value
        if (state is SyncState.Idle || state is SyncState.Discovering) {
            lastAutoSyncTimestamps[peer.id] = now
            connectToTrustedPeer(peer, trusted)
        }
    }

    /**
     * Connects to a permanently trusted peer using stored PSK (no PIN / zero interaction).
     */
    fun connectToTrustedPeer(peer: SyncDevice, trusted: TrustedPeer? = null, mode: SyncMode = SyncMode.SMART_MERGE) {
        val targetTrusted = trusted ?: getTrustedPeer(peer.id) ?: return
        val local = getLocalDevice()
        dataChannelEngine.connectAndSync(
            peer = peer,
            pinOrPsk = targetTrusted.preSharedKey,
            localDevice = local,
            isTrustedAuth = true,
            mode = mode
        ) {
            // Handled via state callbacks
        }
    }

    /**
     * Manually triggers sync to all currently discovered online trusted peers.
     */
    fun triggerAutoSyncToAllTrustedPeers() {
        val onlineTrusted = _discoveredPeers.value.filter { isPeerTrusted(it.id) }
        onlineTrusted.forEach { peer ->
            val trusted = getTrustedPeer(peer.id)
            if (trusted != null) {
                connectToTrustedPeer(peer, trusted)
            }
        }
    }

    /**
     * Stops peer discovery.
     */
    fun stopDiscovery() {
        discoveryManager.stop()
        if (_syncState.value is SyncState.Discovering) {
            _syncState.value = SyncState.Idle
        }
    }

    /**
     * Connects to a discovered peer using 1-time PIN authentication and establishes permanent trust.
     */
    fun connectToPeer(peer: SyncDevice, pin: String, mode: SyncMode = SyncMode.SMART_MERGE) {
        val local = getLocalDevice()
        dataChannelEngine.connectAndSync(
            peer = peer,
            pinOrPsk = pin,
            localDevice = local,
            isTrustedAuth = false,
            mode = mode
        ) {
            // Handled via state callback
        }
    }

    /**
     * Connects to a peer using a scanned QR code or shared pairing token string.
     */
    fun pairWithToken(rawToken: String, mode: SyncMode = SyncMode.SMART_MERGE) {
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
            _syncState.value = SyncState.Error("Invalid pairing token format: ${e.localizedMessage}")
        }
    }

    // --- Trusted Peer Management ---

    fun getTrustedPeer(deviceId: String): TrustedPeer? {
        return _trustedPeers.value.find { it.deviceId == deviceId }
    }

    fun isPeerTrusted(deviceId: String): Boolean {
        return _trustedPeers.value.any { it.deviceId == deviceId }
    }

    fun saveTrustedPeer(peer: TrustedPeer) {
        val current = _trustedPeers.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == peer.deviceId }
        if (idx >= 0) {
            current[idx] = peer
        } else {
            current.add(peer)
        }
        _trustedPeers.value = current
        saveTrustedPeersList(current)
    }

    fun removeTrustedPeer(deviceId: String) {
        val current = _trustedPeers.value.filterNot { it.deviceId == deviceId }
        _trustedPeers.value = current
        saveTrustedPeersList(current)
    }

    fun updateTrustedPeerSyncTime(deviceId: String) {
        val current = _trustedPeers.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(lastSyncAt = System.currentTimeMillis())
            _trustedPeers.value = current
            saveTrustedPeersList(current)
        }
    }

    fun toggleTrustedPeerAutoSync(deviceId: String, enabled: Boolean) {
        val current = _trustedPeers.value.toMutableList()
        val idx = current.indexOfFirst { it.deviceId == deviceId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(autoSyncEnabled = enabled)
            _trustedPeers.value = current
            saveTrustedPeersList(current)
        }
    }

    fun setContinuousAutoSyncEnabled(enabled: Boolean) {
        _continuousAutoSyncEnabled.value = enabled
        prefs.edit().putBoolean("continuous_auto_sync", enabled).apply()
        if (enabled) {
            startDiscovery()
        }
    }

    private fun loadTrustedPeers(): List<TrustedPeer> {
        val json = prefs.getString("trusted_peers_json", null) ?: return emptyList()
        return try {
            trustedPeersAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveTrustedPeersList(list: List<TrustedPeer>) {
        try {
            val json = trustedPeersAdapter.toJson(list)
            prefs.edit().putString("trusted_peers_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    private fun buildReportSummary(report: SyncMergeReport): String {
        val parts = mutableListOf<String>()
        if (report.coursesMerged > 0) parts.add("+${report.coursesMerged} Courses")
        if (report.subjectsMerged > 0) parts.add("+${report.subjectsMerged} Subjects")
        if (report.tasksMerged > 0) parts.add("+${report.tasksMerged} Tasks")
        if (report.assignmentsMerged > 0) parts.add("+${report.assignmentsMerged} Exercises")
        if (report.notesMerged > 0) parts.add("+${report.notesMerged} Notes")
        if (report.pomodoroSessionsMerged > 0) parts.add("+${report.pomodoroSessionsMerged} Focuses")
        return if (parts.isEmpty()) "Synced with 100% up-to-date data" else parts.joinToString(", ")
    }

    private fun addHistoryRecord(record: SyncHistoryRecord) {
        val current = _syncHistory.value.toMutableList()
        current.add(0, record)
        val trimmed = current.take(20)
        _syncHistory.value = trimmed
        saveHistory(trimmed)
    }

    private fun loadHistory(): List<SyncHistoryRecord> {
        val json = prefs.getString("sync_history_json", null) ?: return emptyList()
        return try {
            historyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(list: List<SyncHistoryRecord>) {
        try {
            val json = historyAdapter.toJson(list)
            prefs.edit().putString("sync_history_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanup() {
        discoveryManager.stop()
        dataChannelEngine.stop()
        _isServerRunning.value = false
    }
}
