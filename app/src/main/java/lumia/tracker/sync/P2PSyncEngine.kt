package lumia.tracker.sync

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lumia.tracker.sync.crdt.AutomergeCrdtDocument
import lumia.tracker.sync.reconciliation.VectorReconciliationOrchestrator
import lumia.tracker.sync.security.KeyStoreIdentityManager
import lumia.tracker.sync.security.SasVerification
import lumia.tracker.sync.service.P2PSyncForegroundService
import lumia.tracker.sync.transport.TransportConnectionState
import lumia.tracker.sync.transport.WebRtcDataChannelTransport

/**
 * Master coordinator for Lumia's Zero-Trust, Database-Free P2P Sync Architecture.
 * Strictly decouples the Native WebRTC DataChannel Transport from the Automerge-style
 * CRDT document engine, communicating solely through ByteArray Kotlin flows.
 */
class P2PSyncEngine private constructor(private val context: Context) {

    companion object {
        private const val TAG = "P2PSyncEngine"

        @Volatile
        private var INSTANCE: P2PSyncEngine? = null

        fun getInstance(context: Context): P2PSyncEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: P2PSyncEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 1. Hardware-Backed Identity
    val identityManager = KeyStoreIdentityManager(context)

    // 2. Database-Free Automerge-Style CRDT Document Engine
    val crdtDocument = AutomergeCrdtDocument(localActorId = identityManager.deviceFingerprint, scope = scope)

    // 3. Native WebRTC DataChannel Transport
    val transport = WebRtcDataChannelTransport(context, identityManager, scope)

    // 4. Anti-Entropy Vector Reconciliation Orchestrator for Offline Partitions
    val orchestrator = VectorReconciliationOrchestrator(
        crdtDocument = crdtDocument,
        sendPacketAction = { bytes -> transport.sendPacket(bytes) },
        scope = scope
    )

    // UI SAS Verification State: Pair(SasPayload, PeerFingerprint)
    private val _activeSasVerification = MutableStateFlow<Pair<SasVerification.SasPayload, String>?>(null)
    val activeSasVerification: StateFlow<Pair<SasVerification.SasPayload, String>?> = _activeSasVerification.asStateFlow()

    private var confirmSasAction: (() -> Unit)? = null

    private val prefs = context.getSharedPreferences("lumia_p2p_sync_config", Context.MODE_PRIVATE)
    private val _isForegroundServiceEnabled = MutableStateFlow(prefs.getBoolean("fg_service_enabled", false))
    val isForegroundServiceEnabled: StateFlow<Boolean> = _isForegroundServiceEnabled.asStateFlow()

    init {
        Log.i(TAG, "Initializing Lumia Zero-Trust P2P Sync Engine...")
        bindDecoupledFlows()
        setupSasCallback()

        if (_isForegroundServiceEnabled.value) {
            startForegroundService()
        }
    }

    /**
     * Strictly binds the WebRTC DataChannel transport and the Automerge CRDT engine
     * solely through ByteArray Kotlin flows.
     */
    private fun bindDecoupledFlows() {
        // Outbound Flow: CRDT Document -> WebRTC DataChannel Transport
        scope.launch {
            crdtDocument.outgoingPackets.collect { packetBytes ->
                val success = transport.sendPacket(packetBytes)
                if (!success) {
                    Log.d(TAG, "Queued or dropped packet: transport not currently connected")
                }
            }
        }

        // Inbound Flow: WebRTC DataChannel Transport -> Vector Reconciliation & CRDT Document
        scope.launch {
            transport.incomingPackets.collect { incomingBytes ->
                // First let the Anti-Entropy Orchestrator inspect the packet for vector reconciliation
                val handledByOrchestrator = orchestrator.handleReconciliationPacket(incomingBytes)
                if (!handledByOrchestrator) {
                    // Otherwise pass to CRDT document engine to merge
                    crdtDocument.receiveSyncBytes(incomingBytes)
                }
            }
        }

        // Connection Lifecycle -> Vector Reconciliation Orchestrator
        scope.launch {
            transport.connectionState.collect { state ->
                when (state) {
                    TransportConnectionState.CONNECTED -> orchestrator.onPeerConnected()
                    TransportConnectionState.DISCONNECTED, TransportConnectionState.FAILED -> orchestrator.onPeerDisconnected()
                    else -> Unit
                }
            }
        }
    }

    private fun setupSasCallback() {
        transport.onSasDerived = { sasPayload, peerFingerprint, onConfirm ->
            Log.i(TAG, "SAS derived for peer $peerFingerprint: ${sasPayload.numericCode}")
            confirmSasAction = onConfirm
            _activeSasVerification.value = Pair(sasPayload, peerFingerprint)
        }
    }

    fun confirmSasVerification() {
        confirmSasAction?.invoke()
        confirmSasAction = null
        _activeSasVerification.value = null
    }

    fun dismissSasVerification() {
        confirmSasAction = null
        _activeSasVerification.value = null
    }

    fun setForegroundServiceEnabled(enabled: Boolean) {
        _isForegroundServiceEnabled.value = enabled
        prefs.edit().putBoolean("fg_service_enabled", enabled).apply()
        if (enabled) {
            startForegroundService()
        } else {
            stopForegroundService()
        }
    }

    private fun startForegroundService() {
        try {
            P2PSyncForegroundService.start(context)
        } catch (e: Exception) {
            Log.e(TAG, "Could not start P2PSyncForegroundService: ${e.message}")
        }
    }

    private fun stopForegroundService() {
        try {
            P2PSyncForegroundService.stop(context)
        } catch (e: Exception) {
            Log.e(TAG, "Could not stop P2PSyncForegroundService: ${e.message}")
        }
    }

    fun start() {
        transport.start()
    }

    fun stop() {
        transport.stop()
        orchestrator.stop()
    }
}
