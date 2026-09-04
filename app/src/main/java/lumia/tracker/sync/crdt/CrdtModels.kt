package lumia.tracker.sync.crdt

/**
 * Automerge-style CRDT Data Models for zero-trust, database-free P2P sync.
 * Implements Conflict-free Replicated Data Types (CRDTs) with Lamport Clocks,
 * Vector Clocks, and deterministic Last-Write-Wins (LWW) conflict resolution.
 */

typealias ActorId = String

/**
 * Causality tracking Vector Clock mapping each ActorId to its maximum contiguous sequence number.
 */
data class VectorClock(
    val clockMap: Map<ActorId, Long> = emptyMap()
) {
    fun get(actorId: ActorId): Long = clockMap[actorId] ?: 0L

    fun increment(actorId: ActorId): VectorClock {
        val nextSeq = get(actorId) + 1L
        return copy(clockMap = clockMap + (actorId to nextSeq))
    }

    fun set(actorId: ActorId, seq: Long): VectorClock {
        val current = get(actorId)
        return if (seq > current) {
            copy(clockMap = clockMap + (actorId to seq))
        } else {
            this
        }
    }

    fun merge(other: VectorClock): VectorClock {
        val merged = HashMap(clockMap)
        for ((actor, seq) in other.clockMap) {
            val existing = merged[actor] ?: 0L
            if (seq > existing) {
                merged[actor] = seq
            }
        }
        return VectorClock(merged)
    }

    /**
     * Returns true if this vector clock is strictly greater than or equal to [other]
     * across all actors, and strictly greater for at least one actor.
     */
    fun dominates(other: VectorClock): Boolean {
        var strictlyGreater = false
        val allActors = clockMap.keys + other.clockMap.keys
        for (actor in allActors) {
            val s1 = get(actor)
            val s2 = other.get(actor)
            if (s1 < s2) return false
            if (s1 > s2) strictlyGreater = true
        }
        return strictlyGreater
    }

    fun isEqualTo(other: VectorClock): Boolean {
        val allActors = clockMap.keys + other.clockMap.keys
        for (actor in allActors) {
            if (get(actor) != other.get(actor)) return false
        }
        return true
    }
}

enum class OpAction(val code: Byte) {
    SET_FIELD(0x01),
    DELETE_FIELD(0x02),
    LIST_INSERT(0x03),
    LIST_DELETE(0x04);

    companion object {
        fun fromCode(code: Byte): OpAction = entries.firstOrNull { it.code == code } ?: SET_FIELD
    }
}

sealed class CrdtValue {
    data object NullValue : CrdtValue()
    data class StringValue(val value: String) : CrdtValue()
    data class LongValue(val value: Long) : CrdtValue()
    data class DoubleValue(val value: Double) : CrdtValue()
    data class BooleanValue(val value: Boolean) : CrdtValue()
    data class BinaryValue(val value: ByteArray) : CrdtValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BinaryValue) return false
            return value.contentEquals(other.value)
        }
        override fun hashCode(): Int = value.contentHashCode()
    }
    data class MapValue(val entries: Map<String, CrdtValue>) : CrdtValue()
    data class ListValue(val items: List<CrdtValue>) : CrdtValue()

    fun toPrimitive(): Any? = when (this) {
        is NullValue -> null
        is StringValue -> value
        is LongValue -> value
        is DoubleValue -> value
        is BooleanValue -> value
        is BinaryValue -> value
        is MapValue -> entries.mapValues { it.value.toPrimitive() }
        is ListValue -> items.map { it.toPrimitive() }
    }

    companion object {
        fun fromAny(obj: Any?): CrdtValue = when (obj) {
            null -> NullValue
            is String -> StringValue(obj)
            is Long -> LongValue(obj)
            is Int -> LongValue(obj.toLong())
            is Short -> LongValue(obj.toLong())
            is Byte -> LongValue(obj.toLong())
            is Double -> DoubleValue(obj)
            is Float -> DoubleValue(obj.toDouble())
            is Boolean -> BooleanValue(obj)
            is ByteArray -> BinaryValue(obj)
            is Map<*, *> -> MapValue(obj.entries.associate { it.key.toString() to fromAny(it.value) })
            is List<*> -> ListValue(obj.map { fromAny(it) })
            else -> StringValue(obj.toString())
        }
    }
}

/**
 * An immutable operation in the CRDT change log.
 */
data class CrdtOp(
    val actorId: ActorId,
    val seq: Long,
    val lamport: Long,
    val action: OpAction,
    val path: String,       // e.g. "root", "tasks", "settings.profile"
    val key: String,        // key in map, or element id
    val value: CrdtValue = CrdtValue.NullValue,
    val wallClockTimestamp: Long = System.currentTimeMillis()
) : Comparable<CrdtOp> {
    /**
     * Deterministic total ordering based on Lamport clock with ActorId tie-breaker.
     */
    override fun compareTo(other: CrdtOp): Int {
        val cmpLamport = lamport.compareTo(other.lamport)
        if (cmpLamport != 0) return cmpLamport
        val cmpActor = actorId.compareTo(other.actorId)
        if (cmpActor != 0) return cmpActor
        return seq.compareTo(other.seq)
    }
}

/**
 * Immutable snapshot of the materialized document state and its causality metadata.
 */
data class CrdtDocumentSnapshot(
    val documentData: Map<String, Any?>,
    val vectorClock: VectorClock,
    val totalOpsCount: Int,
    val maxLamportClock: Long,
    val lastModifiedTimestamp: Long
)
