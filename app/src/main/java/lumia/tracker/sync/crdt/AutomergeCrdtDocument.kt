package lumia.tracker.sync.crdt

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Automerge-style database-free CRDT Document Engine.
 * Operates purely in-memory with deterministic LWW conflict resolution,
 * causality-tracking vector clocks, and Lamport timestamps.
 *
 * Strictly communicates with the transport layer solely through ByteArray Kotlin flows.
 */
class AutomergeCrdtDocument(
    val localActorId: ActorId,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {

    companion object {
        private const val TAG = "AutomergeCRDT"
    }

    private val lock = ReentrantLock()

    // Append-only operation log
    private val opsLog = mutableListOf<CrdtOp>()
    private val opsIndex = HashSet<String>() // Key: "${actorId}:${seq}" to prevent duplicate ops

    // Causality clocks
    private var currentLamportClock: Long = 0L
    private var localSequenceNumber: Long = 0L
    private var vectorClock = VectorClock()

    // Materialized document state: Nested Map<String, Any?>
    private val materializedRoot = ConcurrentHashMap<String, Any?>()

    // --- Decoupled Flow Interfaces ---

    // Outgoing binary packets (deltas, vector clock responses) to transport
    private val _outgoingPackets = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 64)
    val outgoingPackets: SharedFlow<ByteArray> = _outgoingPackets.asSharedFlow()

    // Current materialized document snapshot for the UI
    private val _documentState = MutableStateFlow(createSnapshot())
    val documentState: StateFlow<CrdtDocumentSnapshot> = _documentState.asStateFlow()

    // Reactive VectorClock state
    private val _vectorClockState = MutableStateFlow(vectorClock)
    val vectorClockState: StateFlow<VectorClock> = _vectorClockState.asStateFlow()

    init {
        Log.i(TAG, "Initialized CRDT document for actor: $localActorId")
    }

    /**
     * Sets or updates a value at a specified path and key.
     * Generates a new CRDT op, updates local Lamport and sequence, materializes state,
     * and broadcasts encoded binary delta packet over [outgoingPackets].
     */
    fun set(path: String, key: String, value: Any?) {
        val op = lock.withLock {
            localSequenceNumber++
            currentLamportClock++
            val crdtVal = CrdtValue.fromAny(value)
            val newOp = CrdtOp(
                actorId = localActorId,
                seq = localSequenceNumber,
                lamport = currentLamportClock,
                action = OpAction.SET_FIELD,
                path = path,
                key = key,
                value = crdtVal
            )
            applyOpInternal(newOp)
            vectorClock = vectorClock.set(localActorId, localSequenceNumber)
            _vectorClockState.value = vectorClock
            _documentState.value = createSnapshot()
            newOp
        }

        // Broadcast binary packet via Flow
        val packetBytes = CrdtBinaryCodec.encodeOpsDelta(listOf(op), vectorClock)
        scope.launch {
            _outgoingPackets.emit(packetBytes)
        }
    }

    /**
     * Deletes a key at a specified path.
     */
    fun delete(path: String, key: String) {
        val op = lock.withLock {
            localSequenceNumber++
            currentLamportClock++
            val newOp = CrdtOp(
                actorId = localActorId,
                seq = localSequenceNumber,
                lamport = currentLamportClock,
                action = OpAction.DELETE_FIELD,
                path = path,
                key = key,
                value = CrdtValue.NullValue
            )
            applyOpInternal(newOp)
            vectorClock = vectorClock.set(localActorId, localSequenceNumber)
            _vectorClockState.value = vectorClock
            _documentState.value = createSnapshot()
            newOp
        }

        val packetBytes = CrdtBinaryCodec.encodeOpsDelta(listOf(op), vectorClock)
        scope.launch {
            _outgoingPackets.emit(packetBytes)
        }
    }

    /**
     * Appends or inserts an item into a list at a given path.
     */
    fun listAppend(path: String, item: Any?) {
        val currentList = getNestedList(path)
        val newIndex = currentList.size
        set(path = "$path/items", key = newIndex.toString(), value = item)
    }

    /**
     * Receives and processes a binary packet from the transport layer.
     * Decodes the packet, checks for unknown ops, applies conflict resolution,
     * and updates causality clocks.
     */
    fun receiveSyncBytes(bytes: ByteArray): Boolean {
        return try {
            when (val packet = CrdtBinaryCodec.decodePacket(bytes)) {
                is CrdtBinaryCodec.DecodedPacket.OpsDelta -> {
                    mergeOps(packet.ops, packet.senderVectorClock)
                    true
                }
                is CrdtBinaryCodec.DecodedPacket.VectorClockRequest -> {
                    // Respond with our vector clock
                    val respBytes = CrdtBinaryCodec.encodeVectorClockResponse(vectorClock)
                    scope.launch {
                        _outgoingPackets.emit(respBytes)
                    }
                    true
                }
                is CrdtBinaryCodec.DecodedPacket.VectorClockResponse -> {
                    // Check if we have ops the sender is missing, and send them
                    val missingOps = getMissingOps(packet.senderVectorClock)
                    if (missingOps.isNotEmpty()) {
                        val deltaBytes = CrdtBinaryCodec.encodeOpsDelta(missingOps, vectorClock)
                        scope.launch {
                            _outgoingPackets.emit(deltaBytes)
                        }
                    }
                    true
                }
                is CrdtBinaryCodec.DecodedPacket.FullSnapshot -> {
                    lock.withLock {
                        for (op in packet.allOps) {
                            applyOpInternal(op)
                        }
                        vectorClock = vectorClock.merge(packet.snapshot.vectorClock)
                        currentLamportClock = maxOf(currentLamportClock, packet.snapshot.maxLamportClock)
                        _vectorClockState.value = vectorClock
                        _documentState.value = createSnapshot()
                    }
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding incoming CRDT packet: ${e.message}", e)
            false
        }
    }

    /**
     * Merges a batch of operations received from a remote peer.
     */
    fun mergeOps(ops: List<CrdtOp>, remoteClock: VectorClock) {
        if (ops.isEmpty()) return

        lock.withLock {
            var appliedAny = false
            for (op in ops) {
                val opKey = "${op.actorId}:${op.seq}"
                if (!opsIndex.contains(opKey)) {
                    applyOpInternal(op)
                    appliedAny = true
                }
            }
            if (appliedAny) {
                vectorClock = vectorClock.merge(remoteClock)
                _vectorClockState.value = vectorClock
                _documentState.value = createSnapshot()
            }
        }
    }

    /**
     * Returns all operations in local log where op.seq > remoteVectorClock[op.actorId].
     * Used by the anti-entropy vector reconciliation orchestrator.
     */
    fun getMissingOps(remoteVectorClock: VectorClock): List<CrdtOp> {
        return lock.withLock {
            opsLog.filter { op ->
                val remoteSeq = remoteVectorClock.get(op.actorId)
                op.seq > remoteSeq
            }
        }
    }

    fun getAllOps(): List<CrdtOp> {
        return lock.withLock { ArrayList(opsLog) }
    }

    fun getVectorClock(): VectorClock {
        return lock.withLock { vectorClock }
    }

    fun getSnapshot(): CrdtDocumentSnapshot {
        return lock.withLock { createSnapshot() }
    }

    fun exportBinarySnapshot(): ByteArray {
        return lock.withLock {
            CrdtBinaryCodec.encodeFullSnapshot(createSnapshot(), ArrayList(opsLog))
        }
    }

    // --- Internal State Materialization ---

    private fun applyOpInternal(op: CrdtOp) {
        val opKey = "${op.actorId}:${op.seq}"
        if (opsIndex.contains(opKey)) return

        opsIndex.add(opKey)
        opsLog.add(op)
        opsLog.sort() // Maintain deterministic Lamport total order

        // Update Lamport clock: L = max(L_local, L_msg) + 1
        currentLamportClock = maxOf(currentLamportClock, op.lamport) + 1L
        vectorClock = vectorClock.set(op.actorId, op.seq)

        // Materialize onto in-memory tree
        materializeAllOps()
    }

    private fun materializeAllOps() {
        materializedRoot.clear()

        // Replay sorted ops to ensure deterministic Last-Write-Wins
        for (op in opsLog) {
            when (op.action) {
                OpAction.SET_FIELD -> {
                    val targetMap = navigateToMap(op.path)
                    targetMap[op.key] = op.value.toPrimitive()
                }
                OpAction.DELETE_FIELD -> {
                    val targetMap = navigateToMap(op.path)
                    targetMap.remove(op.key)
                }
                OpAction.LIST_INSERT, OpAction.LIST_DELETE -> {
                    val targetMap = navigateToMap(op.path)
                    targetMap[op.key] = op.value.toPrimitive()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun navigateToMap(path: String): MutableMap<String, Any?> {
        if (path.isEmpty() || path == "root" || path == "/") {
            return materializedRoot
        }
        val segments = path.split("/").filter { it.isNotBlank() }
        var current: MutableMap<String, Any?> = materializedRoot
        for (seg in segments) {
            val next = current.getOrPut(seg) { HashMap<String, Any?>() }
            if (next is MutableMap<*, *>) {
                current = next as MutableMap<String, Any?>
            } else {
                val newMap = HashMap<String, Any?>()
                current[seg] = newMap
                current = newMap
            }
        }
        return current
    }

    @Suppress("UNCHECKED_CAST")
    private fun getNestedList(path: String): List<Any?> {
        val map = navigateToMap(path)
        val itemsMap = map["items"] as? Map<String, Any?> ?: return emptyList()
        return itemsMap.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.map { it.value }
    }

    private fun createSnapshot(): CrdtDocumentSnapshot {
        return CrdtDocumentSnapshot(
            documentData = HashMap(materializedRoot),
            vectorClock = vectorClock,
            totalOpsCount = opsLog.size,
            maxLamportClock = currentLamportClock,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
    }
}
