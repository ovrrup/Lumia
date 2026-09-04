package lumia.tracker.sync.transport

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Strictly decoupled transport contract for zero-trust P2P synchronization.
 * Transport layer communicates solely through ByteArray Kotlin flows,
 * completely decoupled from CRDT, documents, and databases.
 */
interface P2PTransport {

    /**
     * Inbound packet stream from the remote peer.
     */
    val incomingPackets: SharedFlow<ByteArray>

    /**
     * Current connection and ICE lifecycle state.
     */
    val connectionState: StateFlow<TransportConnectionState>

    /**
     * Live transport telemetry for network diagnostics and UI feedback.
     */
    val telemetry: StateFlow<TransportTelemetry>

    /**
     * Sends a raw binary packet over the DataChannel.
     */
    suspend fun sendPacket(data: ByteArray): Boolean

    /**
     * Starts listening, gathering ICE candidates, and preparing DataChannel transport.
     */
    fun start()

    /**
     * Gracefully stops transport, closes sockets, and cancels keep-alives.
     */
    fun stop()
}

enum class TransportConnectionState {
    DISCONNECTED,
    GATHERING_ICE,
    CGNAT_DISCOVERED,
    SIGNOD_EXCHANGE,
    CONNECTING,
    CONNECTED,
    KEEP_ALIVE_ACTIVE,
    FAILED
}

data class TransportTelemetry(
    val connectionState: TransportConnectionState = TransportConnectionState.DISCONNECTED,
    val localHostAddress: String? = null,
    val publicReflexiveAddress: String? = null,
    val remotePeerAddress: String? = null,
    val selectedIceCandidateType: String = "Unknown", // "host" or "srflx"
    val isCgnatTraversed: Boolean = false,
    val packetsSent: Long = 0L,
    val packetsReceived: Long = 0L,
    val bytesSent: Long = 0L,
    val bytesReceived: Long = 0L,
    val roundTripTimeMs: Long = 0L,
    val lastKeepAliveTimestamp: Long = 0L,
    val keepAliveIntervalSeconds: Int = 20
)
