package lumia.tracker.sync.transport

import android.util.Base64
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import lumia.tracker.sync.security.KeyStoreIdentityManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Manages zero-database, stateless signaling tokens for WebRTC DataChannel connection setup.
 * Packs ICE candidates, session IDs, and hardware-signed ephemeral handshake credentials into
 * compact Base64url tokens that can be shared via QR code, clipboard, or ephemeral stateless relays.
 */
object StatelessSignaling {

    private const val TAG = "StatelessSignaling"

    data class IceCandidateInfo(
        val ip: String,
        val port: Int,
        val type: String // "host" or "srflx"
    )

    data class StatelessSignalingPayload(
        val sessionId: String,
        val role: String, // "OFFERER" or "ANSWERER"
        val candidates: List<IceCandidateInfo>,
        val handshakePacket: KeyStoreIdentityManager.EphemeralHandshakePacket,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(StatelessSignalingPayload::class.java)

    /**
     * Compresses and encodes the signaling payload into a compact URL-safe Base64 token.
     */
    fun createToken(payload: StatelessSignalingPayload): String {
        val json = adapter.toJson(payload)
        val jsonBytes = json.toByteArray(Charsets.UTF_8)

        // Compress via Deflate
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(jsonBytes)
        deflater.finish()

        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(512)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            baos.write(buffer, 0, count)
        }
        deflater.end()

        val compressedBytes = baos.toByteArray()
        return Base64.encodeToString(compressedBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Decodes and decompresses a stateless signaling token.
     */
    fun parseToken(token: String): StatelessSignalingPayload? {
        return try {
            val compressedBytes = Base64.decode(token.trim(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val inflater = Inflater()
            inflater.setInput(compressedBytes)

            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(512)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                baos.write(buffer, 0, count)
            }
            inflater.end()

            val json = baos.toString(Charsets.UTF_8.name())
            adapter.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stateless signaling token: ${e.message}")
            null
        }
    }
}
