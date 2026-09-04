package lumia.tracker.sync.crdt

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * High-performance binary codec for Automerge-style CRDT packets.
 * Encodes and decodes CRDT operations and vector clocks into compact ByteArrays
 * without JSON overhead or database dependencies.
 */
object CrdtBinaryCodec {

    // Magic identifier: 'L', 'U', 'M', 'C' (Lumia CRDT)
    const val MAGIC_HEADER: Int = 0x4C554D43
    const val PROTOCOL_VERSION: Byte = 1

    const val PACKET_TYPE_OPS_DELTA: Byte = 0x01
    const val PACKET_TYPE_VECTOR_CLOCK_REQ: Byte = 0x02
    const val PACKET_TYPE_VECTOR_CLOCK_RESP: Byte = 0x03
    const val PACKET_TYPE_FULL_SNAPSHOT: Byte = 0x04

    sealed class DecodedPacket {
        data class OpsDelta(val ops: List<CrdtOp>, val senderVectorClock: VectorClock) : DecodedPacket()
        data class VectorClockRequest(val senderVectorClock: VectorClock) : DecodedPacket()
        data class VectorClockResponse(val senderVectorClock: VectorClock) : DecodedPacket()
        data class FullSnapshot(val snapshot: CrdtDocumentSnapshot, val allOps: List<CrdtOp>) : DecodedPacket()
    }

    // --- Encoding Methods ---

    fun encodeOpsDelta(ops: List<CrdtOp>, localVectorClock: VectorClock): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeInt(MAGIC_HEADER)
        dos.writeByte(PROTOCOL_VERSION.toInt())
        dos.writeByte(PACKET_TYPE_OPS_DELTA.toInt())

        // Encode Vector Clock
        writeVectorClock(dos, localVectorClock)

        // Encode Ops Count & Ops
        dos.writeInt(ops.size)
        for (op in ops) {
            writeOp(dos, op)
        }
        dos.flush()
        return baos.toByteArray()
    }

    fun encodeVectorClockRequest(localVectorClock: VectorClock): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeInt(MAGIC_HEADER)
        dos.writeByte(PROTOCOL_VERSION.toInt())
        dos.writeByte(PACKET_TYPE_VECTOR_CLOCK_REQ.toInt())

        writeVectorClock(dos, localVectorClock)
        dos.flush()
        return baos.toByteArray()
    }

    fun encodeVectorClockResponse(localVectorClock: VectorClock): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeInt(MAGIC_HEADER)
        dos.writeByte(PROTOCOL_VERSION.toInt())
        dos.writeByte(PACKET_TYPE_VECTOR_CLOCK_RESP.toInt())

        writeVectorClock(dos, localVectorClock)
        dos.flush()
        return baos.toByteArray()
    }

    fun encodeFullSnapshot(snapshot: CrdtDocumentSnapshot, allOps: List<CrdtOp>): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeInt(MAGIC_HEADER)
        dos.writeByte(PROTOCOL_VERSION.toInt())
        dos.writeByte(PACKET_TYPE_FULL_SNAPSHOT.toInt())

        writeVectorClock(dos, snapshot.vectorClock)
        dos.writeLong(snapshot.maxLamportClock)
        dos.writeLong(snapshot.lastModifiedTimestamp)

        dos.writeInt(allOps.size)
        for (op in allOps) {
            writeOp(dos, op)
        }
        dos.flush()
        return baos.toByteArray()
    }

    // --- Decoding Methods ---

    fun decodePacket(bytes: ByteArray): DecodedPacket {
        require(bytes.size >= 6) { "Packet too small to be valid CRDT binary" }
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)

        val magic = dis.readInt()
        if (magic != MAGIC_HEADER) {
            throw IllegalArgumentException("Invalid CRDT magic header: 0x${Integer.toHexString(magic)}")
        }

        val version = dis.readByte()
        if (version != PROTOCOL_VERSION) {
            throw IllegalArgumentException("Unsupported CRDT protocol version: $version")
        }

        return when (val type = dis.readByte()) {
            PACKET_TYPE_OPS_DELTA -> {
                val vectorClock = readVectorClock(dis)
                val opsCount = dis.readInt()
                val ops = ArrayList<CrdtOp>(opsCount)
                for (i in 0 until opsCount) {
                    ops.add(readOp(dis))
                }
                DecodedPacket.OpsDelta(ops, vectorClock)
            }
            PACKET_TYPE_VECTOR_CLOCK_REQ -> {
                val vectorClock = readVectorClock(dis)
                DecodedPacket.VectorClockRequest(vectorClock)
            }
            PACKET_TYPE_VECTOR_CLOCK_RESP -> {
                val vectorClock = readVectorClock(dis)
                DecodedPacket.VectorClockResponse(vectorClock)
            }
            PACKET_TYPE_FULL_SNAPSHOT -> {
                val vectorClock = readVectorClock(dis)
                val maxLamport = dis.readLong()
                val lastModified = dis.readLong()
                val opsCount = dis.readInt()
                val ops = ArrayList<CrdtOp>(opsCount)
                for (i in 0 until opsCount) {
                    ops.add(readOp(dis))
                }
                val snapshot = CrdtDocumentSnapshot(
                    documentData = emptyMap(), // Reconstructed by engine on import
                    vectorClock = vectorClock,
                    totalOpsCount = opsCount,
                    maxLamportClock = maxLamport,
                    lastModifiedTimestamp = lastModified
                )
                DecodedPacket.FullSnapshot(snapshot, ops)
            }
            else -> throw IllegalArgumentException("Unknown CRDT packet type: $type")
        }
    }

    // --- Helper Primitives ---

    private fun writeVectorClock(dos: DataOutputStream, clock: VectorClock) {
        dos.writeInt(clock.clockMap.size)
        for ((actor, seq) in clock.clockMap) {
            dos.writeUTF(actor)
            dos.writeLong(seq)
        }
    }

    private fun readVectorClock(dis: DataInputStream): VectorClock {
        val size = dis.readInt()
        val map = HashMap<ActorId, Long>(size)
        for (i in 0 until size) {
            val actor = dis.readUTF()
            val seq = dis.readLong()
            map[actor] = seq
        }
        return VectorClock(map)
    }

    private fun writeOp(dos: DataOutputStream, op: CrdtOp) {
        dos.writeUTF(op.actorId)
        dos.writeLong(op.seq)
        dos.writeLong(op.lamport)
        dos.writeByte(op.action.code.toInt())
        dos.writeUTF(op.path)
        dos.writeUTF(op.key)
        dos.writeLong(op.wallClockTimestamp)
        writeValue(dos, op.value)
    }

    private fun readOp(dis: DataInputStream): CrdtOp {
        val actorId = dis.readUTF()
        val seq = dis.readLong()
        val lamport = dis.readLong()
        val action = OpAction.fromCode(dis.readByte())
        val path = dis.readUTF()
        val key = dis.readUTF()
        val timestamp = dis.readLong()
        val value = readValue(dis)

        return CrdtOp(
            actorId = actorId,
            seq = seq,
            lamport = lamport,
            action = action,
            path = path,
            key = key,
            value = value,
            wallClockTimestamp = timestamp
        )
    }

    private fun writeValue(dos: DataOutputStream, value: CrdtValue) {
        when (value) {
            is CrdtValue.NullValue -> dos.writeByte(0)
            is CrdtValue.StringValue -> {
                dos.writeByte(1)
                dos.writeUTF(value.value)
            }
            is CrdtValue.LongValue -> {
                dos.writeByte(2)
                dos.writeLong(value.value)
            }
            is CrdtValue.DoubleValue -> {
                dos.writeByte(3)
                dos.writeDouble(value.value)
            }
            is CrdtValue.BooleanValue -> {
                dos.writeByte(4)
                dos.writeBoolean(value.value)
            }
            is CrdtValue.BinaryValue -> {
                dos.writeByte(5)
                dos.writeInt(value.value.size)
                dos.write(value.value)
            }
            is CrdtValue.MapValue -> {
                dos.writeByte(6)
                dos.writeInt(value.entries.size)
                for ((k, v) in value.entries) {
                    dos.writeUTF(k)
                    writeValue(dos, v)
                }
            }
            is CrdtValue.ListValue -> {
                dos.writeByte(7)
                dos.writeInt(value.items.size)
                for (item in value.items) {
                    writeValue(dos, item)
                }
            }
        }
    }

    private fun readValue(dis: DataInputStream): CrdtValue {
        return when (val typeTag = dis.readByte().toInt()) {
            0 -> CrdtValue.NullValue
            1 -> CrdtValue.StringValue(dis.readUTF())
            2 -> CrdtValue.LongValue(dis.readLong())
            3 -> CrdtValue.DoubleValue(dis.readDouble())
            4 -> CrdtValue.BooleanValue(dis.readBoolean())
            5 -> {
                val len = dis.readInt()
                val bytes = ByteArray(len)
                dis.readFully(bytes)
                CrdtValue.BinaryValue(bytes)
            }
            6 -> {
                val size = dis.readInt()
                val map = HashMap<String, CrdtValue>(size)
                for (i in 0 until size) {
                    val k = dis.readUTF()
                    val v = readValue(dis)
                    map[k] = v
                }
                CrdtValue.MapValue(map)
            }
            7 -> {
                val size = dis.readInt()
                val list = ArrayList<CrdtValue>(size)
                for (i in 0 until size) {
                    list.add(readValue(dis))
                }
                CrdtValue.ListValue(list)
            }
            else -> throw IllegalArgumentException("Unknown value type tag: $typeTag")
        }
    }
}
