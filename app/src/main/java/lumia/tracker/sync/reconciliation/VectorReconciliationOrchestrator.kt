package lumia.tracker.sync.reconciliation

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lumia.tracker.sync.crdt.AutomergeCrdtDocument
import lumia.tracker.sync.crdt.CrdtBinaryCodec
import lumia.tracker.sync.crdt.VectorClock
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Orchestrator for Anti-Entropy Vector Reconciliation across offline partitions.
 * Automatically initiates vector exchange upon peer connection/reconnection,
 * identifies causality deltas between partitioned nodes, streams missing operations
 * in bounded chunks, and verifies mathematical convergence.
 */
class VectorReconciliationOrchestrator(
    private val crdtDocument: AutomergeCrdtDocument,
    private val sendPacketAction: suspend (ByteArray) -> Boolean,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    companion object {
        private const val TAG = "VectorReconciliation"
        private const val CHUNK_SIZE = 50 // Bounded batch size for missing ops delta
        private const val PERIODIC_AUDIT_INTERVAL_MS = 60_000L // 60s periodic anti-entropy check
    }

    enum class ReconciliationStatus {
        IDLE,
        PARTITION_DETECTED,
        RECONCILING,
        CONVERGED
    }

    data class ReconciliationTelemetry(
        val status: ReconciliationStatus = ReconciliationStatus.IDLE,
        val totalPartitionsResolved: Int = 0,
        val totalOpsReconciled: Int = 0,
        val lastReconciledTimestamp: Long = 0L,
        val isPartitioned: Boolean = false,
        val localVectorClock: VectorClock = VectorClock(),
        val remoteVectorClock: VectorClock = VectorClock()
    )

    private val _telemetry = MutableStateFlow(ReconciliationTelemetry())
    val telemetry: StateFlow<ReconciliationTelemetry> = _telemetry.asStateFlow()

    private var periodicAuditJob: Job? = null
    private val isReconciling = AtomicBoolean(false)

    /**
     * Called when the WebRTC DataChannel connects or reconnects after an offline partition.
     */
    fun onPeerConnected() {
        Log.i(TAG, "Peer connected: triggering anti-entropy vector reconciliation...")
        triggerReconciliation()
        startPeriodicAudit()
    }

    /**
     * Called when peer disconnects (entering an offline partition).
     */
    fun onPeerDisconnected() {
        Log.i(TAG, "Peer disconnected: offline partition active")
        periodicAuditJob?.cancel()
        periodicAuditJob = null
        _telemetry.value = _telemetry.value.copy(
            status = ReconciliationStatus.IDLE,
            isPartitioned = true
        )
    }

    /**
     * Starts anti-entropy handshake by sending our current VectorClock to the peer.
     */
    fun triggerReconciliation() {
        scope.launch {
            try {
                val localClock = crdtDocument.getVectorClock()
                val reqPacket = CrdtBinaryCodec.encodeVectorClockRequest(localClock)
                _telemetry.value = _telemetry.value.copy(
                    status = ReconciliationStatus.RECONCILING,
                    localVectorClock = localClock
                )
                sendPacketAction(reqPacket)
                Log.d(TAG, "Sent VectorClockRequest: $localClock")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send vector reconciliation request: ${e.message}", e)
            }
        }
    }

    /**
     * Processes an incoming CRDT packet for vector reconciliation.
     * Evaluates causality, identifies missing operations, streams deltas, and checks convergence.
     */
    suspend fun handleReconciliationPacket(bytes: ByteArray): Boolean {
        return try {
            when (val packet = CrdtBinaryCodec.decodePacket(bytes)) {
                is CrdtBinaryCodec.DecodedPacket.VectorClockRequest -> {
                    val remoteClock = packet.senderVectorClock
                    val localClock = crdtDocument.getVectorClock()
                    Log.d(TAG, "Received VectorClockRequest. Remote: $remoteClock, Local: $localClock")

                    // Send response with our vector clock
                    val respPacket = CrdtBinaryCodec.encodeVectorClockResponse(localClock)
                    sendPacketAction(respPacket)

                    // Stream missing ops that remote peer is lacking
                    reconcileMissingOpsToRemote(remoteClock)
                    true
                }

                is CrdtBinaryCodec.DecodedPacket.VectorClockResponse -> {
                    val remoteClock = packet.senderVectorClock
                    val localClock = crdtDocument.getVectorClock()
                    Log.d(TAG, "Received VectorClockResponse. Remote: $remoteClock, Local: $localClock")

                    _telemetry.value = _telemetry.value.copy(
                        remoteVectorClock = remoteClock,
                        localVectorClock = localClock
                    )

                    // Reconcile missing ops to remote peer
                    reconcileMissingOpsToRemote(remoteClock)

                    // Check convergence
                    checkConvergence(remoteClock)
                    true
                }

                is CrdtBinaryCodec.DecodedPacket.OpsDelta -> {
                    // Apply incoming operations
                    val opsCount = packet.ops.size
                    crdtDocument.mergeOps(packet.ops, packet.senderVectorClock)

                    val updatedLocalClock = crdtDocument.getVectorClock()
                    val currentTele = _telemetry.value

                    _telemetry.value = currentTele.copy(
                        totalOpsReconciled = currentTele.totalOpsReconciled + opsCount,
                        localVectorClock = updatedLocalClock,
                        remoteVectorClock = packet.senderVectorClock
                    )

                    Log.i(TAG, "Merged $opsCount reconciliation ops. Updated clock: $updatedLocalClock")
                    checkConvergence(packet.senderVectorClock)
                    true
                }

                is CrdtBinaryCodec.DecodedPacket.FullSnapshot -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling reconciliation packet: ${e.message}", e)
            false
        }
    }

    private suspend fun reconcileMissingOpsToRemote(remoteClock: VectorClock) {
        if (!isReconciling.compareAndSet(false, true)) return
        try {
            val missingOps = crdtDocument.getMissingOps(remoteClock)
            if (missingOps.isEmpty()) {
                Log.d(TAG, "Remote peer is up-to-date. Zero missing ops to send.")
                return
            }

            Log.i(TAG, "Partition reconciliation: streaming ${missingOps.size} missing ops to peer in chunks of $CHUNK_SIZE...")
            val localClock = crdtDocument.getVectorClock()

            // Stream in bounded chunks to avoid UDP fragmentation issues
            for (chunk in missingOps.chunked(CHUNK_SIZE)) {
                val deltaPacket = CrdtBinaryCodec.encodeOpsDelta(chunk, localClock)
                sendPacketAction(deltaPacket)
                delay(10) // Small yield for transport buffering
            }

            val currentTele = _telemetry.value
            _telemetry.value = currentTele.copy(
                totalOpsReconciled = currentTele.totalOpsReconciled + missingOps.size,
                totalPartitionsResolved = currentTele.totalPartitionsResolved + 1,
                lastReconciledTimestamp = System.currentTimeMillis()
            )
        } finally {
            isReconciling.set(false)
        }
    }

    private fun checkConvergence(remoteClock: VectorClock) {
        val localClock = crdtDocument.getVectorClock()
        val isConverged = localClock.isEqualTo(remoteClock)

        if (isConverged) {
            Log.i(TAG, "Anti-Entropy Reconciliation Complete! Mathematical convergence reached: $localClock")
            _telemetry.value = _telemetry.value.copy(
                status = ReconciliationStatus.CONVERGED,
                isPartitioned = false,
                lastReconciledTimestamp = System.currentTimeMillis()
            )
        } else {
            _telemetry.value = _telemetry.value.copy(
                status = ReconciliationStatus.PARTITION_DETECTED,
                isPartitioned = true
            )
        }
    }

    private fun startPeriodicAudit() {
        periodicAuditJob?.cancel()
        periodicAuditJob = scope.launch {
            while (isActive) {
                delay(PERIODIC_AUDIT_INTERVAL_MS)
                Log.d(TAG, "Running periodic anti-entropy audit check...")
                triggerReconciliation()
            }
        }
    }

    fun stop() {
        periodicAuditJob?.cancel()
        periodicAuditJob = null
    }
}
