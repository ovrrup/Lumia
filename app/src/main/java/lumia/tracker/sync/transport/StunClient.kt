package lumia.tracker.sync.transport

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.SecureRandom

/**
 * RFC 5389 / RFC 8489 Session Traversal Utilities for NAT (STUN) Client in pure Kotlin.
 * Discovers server-reflexive (srflx) public endpoints through Carrier-Grade NATs (CGNAT)
 * and generates 20s UDP keep-alive binding indications.
 */
object StunClient {

    private const val TAG = "StunClient"

    // Default STUN servers
    val PUBLIC_STUN_SERVERS = listOf(
        "stun.l.google.com" to 19302,
        "stun1.l.google.com" to 19302,
        "stun.cloudflare.com" to 3478
    )

    private const val STUN_MAGIC_COOKIE = 0x2112A442
    private const val BINDING_REQUEST = 0x0001
    private const val BINDING_RESPONSE = 0x0101
    private const val ATTR_MAPPED_ADDRESS = 0x0001
    private const val ATTR_XOR_MAPPED_ADDRESS = 0x0020

    private val secureRandom = SecureRandom()

    data class StunDiscoveryResult(
        val publicAddress: InetSocketAddress,
        val localBoundAddress: InetSocketAddress,
        val stunServerUsed: String
    )

    /**
     * Queries STUN servers over an existing or new DatagramSocket to discover the
     * public mapped IP and port through CGNAT / firewalls.
     */
    suspend fun discoverPublicEndpoint(socket: DatagramSocket): StunDiscoveryResult? = withContext(Dispatchers.IO) {
        for ((host, port) in PUBLIC_STUN_SERVERS) {
            try {
                val serverAddr = InetAddress.getByName(host)
                val txId = ByteArray(12).also { secureRandom.nextBytes(it) }

                // Build STUN Binding Request (20 bytes header)
                val requestBuffer = ByteBuffer.allocate(20)
                requestBuffer.putShort(BINDING_REQUEST.toShort())
                requestBuffer.putShort(0.toShort()) // Message Length: 0
                requestBuffer.putInt(STUN_MAGIC_COOKIE)
                requestBuffer.put(txId)

                val requestData = requestBuffer.array()
                val sendPacket = DatagramPacket(requestData, requestData.size, serverAddr, port)
                socket.send(sendPacket)

                // Wait for response with timeout
                val prevTimeout = socket.soTimeout
                socket.soTimeout = 2500 // 2.5s timeout
                val recvBuffer = ByteArray(512)
                val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)

                socket.receive(recvPacket)
                socket.soTimeout = prevTimeout

                val mapped = parseStunResponse(recvBuffer, recvPacket.length, txId)
                if (mapped != null) {
                    Log.i(TAG, "CGNAT Traversal Success! Discovered public endpoint: $mapped via $host:$port")
                    return@withContext StunDiscoveryResult(
                        publicAddress = mapped,
                        localBoundAddress = InetSocketAddress(socket.localAddress, socket.localPort),
                        stunServerUsed = "$host:$port"
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "STUN query to $host:$port failed: ${e.message}")
            }
        }
        null
    }

    /**
     * Creates a raw STUN Binding Request packet to be sent as a 20s UDP keep-alive ping.
     */
    fun createKeepAlivePingPacket(): ByteArray {
        val txId = ByteArray(12).also { secureRandom.nextBytes(it) }
        val buffer = ByteBuffer.allocate(20)
        buffer.putShort(BINDING_REQUEST.toShort())
        buffer.putShort(0.toShort()) // Length: 0
        buffer.putInt(STUN_MAGIC_COOKIE)
        buffer.put(txId)
        return buffer.array()
    }

    private fun parseStunResponse(data: ByteArray, length: Int, expectedTxId: ByteArray): InetSocketAddress? {
        if (length < 20) return null
        val buffer = ByteBuffer.wrap(data, 0, length)

        val msgType = buffer.short.toInt() and 0xFFFF
        val msgLength = buffer.short.toInt() and 0xFFFF
        val magicCookie = buffer.int

        if (msgType != BINDING_RESPONSE || magicCookie != STUN_MAGIC_COOKIE) {
            return null
        }

        // Verify transaction ID
        val txId = ByteArray(12)
        buffer.get(txId)
        if (!txId.contentEquals(expectedTxId)) {
            return null
        }

        // Parse attributes
        var bytesLeft = msgLength
        while (bytesLeft >= 4 && buffer.hasRemaining()) {
            val attrType = buffer.short.toInt() and 0xFFFF
            val attrLength = buffer.short.toInt() and 0xFFFF
            bytesLeft -= 4

            if (attrType == ATTR_XOR_MAPPED_ADDRESS && attrLength >= 8) {
                buffer.get() // Reserved 0x00
                val family = buffer.get().toInt() and 0xFF
                val xorPort = buffer.short.toInt() and 0xFFFF
                val port = xorPort xor (STUN_MAGIC_COOKIE ushr 16)

                if (family == 0x01) { // IPv4
                    val xorIp = buffer.int
                    val ip = xorIp xor STUN_MAGIC_COOKIE
                    val ipBytes = byteArrayOf(
                        (ip ushr 24).toByte(),
                        (ip ushr 16).toByte(),
                        (ip ushr 8).toByte(),
                        ip.toByte()
                    )
                    val inetAddress = InetAddress.getByAddress(ipBytes)
                    return InetSocketAddress(inetAddress, port)
                }
            } else if (attrType == ATTR_MAPPED_ADDRESS && attrLength >= 8) {
                buffer.get() // Reserved 0x00
                val family = buffer.get().toInt() and 0xFF
                val port = buffer.short.toInt() and 0xFFFF
                if (family == 0x01) { // IPv4
                    val ipBytes = ByteArray(4)
                    buffer.get(ipBytes)
                    val inetAddress = InetAddress.getByAddress(ipBytes)
                    return InetSocketAddress(inetAddress, port)
                }
            }

            // Skip remaining attribute bytes with padding to 4-byte boundary
            val padding = (4 - (attrLength % 4)) % 4
            val toSkip = minOf(attrLength + padding, bytesLeft)
            for (i in 0 until minOf(toSkip, buffer.remaining())) {
                buffer.get()
            }
            bytesLeft -= toSkip
        }

        return null
    }
}
