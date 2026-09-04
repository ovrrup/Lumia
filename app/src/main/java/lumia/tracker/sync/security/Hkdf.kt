package lumia.tracker.sync.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

/**
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF) using SHA-256.
 * Provides cryptographically strong key derivation for zero-trust P2P authentication,
 * SAS verification, and transport session encryption.
 */
object Hkdf {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val HASH_LEN = 32 // SHA-256 produces 32 bytes

    /**
     * HKDF-Extract(salt, IKM) -> PRK
     *
     * @param salt Optional salt value (a non-secret random value); if not provided,
     *             it is set to a string of HASH_LEN zeros.
     * @param ikm  Input keying material.
     * @return A pseudorandom key (PRK) of HASH_LEN bytes.
     */
    fun extract(salt: ByteArray?, ikm: ByteArray): ByteArray {
        val actualSalt = if (salt == null || salt.isEmpty()) {
            ByteArray(HASH_LEN)
        } else {
            salt
        }
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(actualSalt, HMAC_ALGORITHM))
        return mac.doFinal(ikm)
    }

    /**
     * HKDF-Expand(PRK, info, L) -> OKM
     *
     * @param prk  A pseudorandom key of at least HASH_LEN bytes (usually output from extract).
     * @param info Optional context and application specific information.
     * @param length The length of output keying material in bytes (<= 255 * HASH_LEN).
     * @return Output keying material (OKM) of length bytes.
     */
    fun expand(prk: ByteArray, info: ByteArray?, length: Int): ByteArray {
        require(length > 0) { "Output length must be positive: $length" }
        require(length <= 255 * HASH_LEN) { "Output length exceeds HKDF limit: $length" }

        val actualInfo = info ?: ByteArray(0)
        val n = ceil(length.toDouble() / HASH_LEN).toInt()
        val okm = ByteArray(length)

        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(prk, HMAC_ALGORITHM))

        var prev = ByteArray(0)
        var offset = 0

        for (i in 1..n) {
            mac.reset()
            if (prev.isNotEmpty()) {
                mac.update(prev)
            }
            if (actualInfo.isNotEmpty()) {
                mac.update(actualInfo)
            }
            mac.update(i.toByte())
            prev = mac.doFinal()

            val bytesToCopy = minOf(prev.size, length - offset)
            System.arraycopy(prev, 0, okm, offset, bytesToCopy)
            offset += bytesToCopy
        }

        return okm
    }

    /**
     * Combined HKDF: Extract then Expand in a single step.
     */
    fun deriveKey(salt: ByteArray?, ikm: ByteArray, info: ByteArray?, length: Int): ByteArray {
        val prk = extract(salt, ikm)
        return expand(prk, info, length)
    }
}
