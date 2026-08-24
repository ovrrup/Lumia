package lumia.tracker.sync.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles end-to-end encryption (AES-256-GCM), mutual challenge-response authentication
 * (HMAC-SHA256), and permanent Pre-Shared Key (PSK) derivation for 1-time handshake P2P synchronization.
 */
object SyncCryptoManager {

    private val secureRandom = SecureRandom()
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Generates a 6-digit numeric PIN for user pairing verification during initial setup.
     */
    fun generatePairingPin(): String {
        val pinNumber = secureRandom.nextInt(900000) + 100000
        return pinNumber.toString()
    }

    /**
     * Generates a random cryptographic nonce in hexadecimal format.
     */
    fun generateNonce(lengthBytes: Int = 16): String {
        val bytes = ByteArray(lengthBytes)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a permanent 256-bit symmetric Pre-Shared Trust Key (PSK) during initial 1-time handshake.
     */
    fun derivePSK(pinOrSeed: String, clientNonce: String, serverNonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update("LUMIA_P2P_PERMANENT_PSK_SALT_".toByteArray(Charsets.UTF_8))
        digest.update(pinOrSeed.toByteArray(Charsets.UTF_8))
        digest.update(clientNonce.toByteArray(Charsets.UTF_8))
        digest.update(serverNonce.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a 256-bit symmetric AES key from the pairing PIN/PSK and session nonce.
     */
    private fun deriveKey(keyOrPin: String, nonce: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update("LUMIA_P2P_SYNC_SALT_".toByteArray(Charsets.UTF_8))
        digest.update(keyOrPin.toByteArray(Charsets.UTF_8))
        digest.update(nonce.toByteArray(Charsets.UTF_8))
        val keyBytes = digest.digest()
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Calculates HMAC-SHA256 signature for mutual authentication challenge-response.
     */
    fun calculateAuthHash(keyOrPin: String, nonce: String, deviceId: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(keyOrPin.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val data = "$nonce:$deviceId:LUMIA_AUTH".toByteArray(Charsets.UTF_8)
        val hashBytes = mac.doFinal(data)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verifies that the peer's authentication response matches the expected HMAC-SHA256.
     */
    fun verifyAuthHash(keyOrPin: String, nonce: String, deviceId: String, receivedHash: String): Boolean {
        return try {
            val expected = calculateAuthHash(keyOrPin, nonce, deviceId)
            expected == receivedHash
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with a newly generated random IV.
     * Returns a Pair of (Base64 Encrypted Ciphertext, Base64 IV).
     */
    fun encryptPayload(plainBytes: ByteArray, keyOrPin: String, nonce: String): Pair<String, String> {
        val keySpec = deriveKey(keyOrPin, nonce)
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val cipherBytes = cipher.doFinal(plainBytes)
        val encryptedBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        return Pair(encryptedBase64, ivBase64)
    }

    /**
     * Decrypts Base64 ciphertext using AES-256-GCM and the derived key.
     */
    fun decryptPayload(encryptedBase64: String, ivBase64: String, keyOrPin: String, nonce: String): ByteArray {
        val keySpec = deriveKey(keyOrPin, nonce)
        val cipherBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        return cipher.doFinal(cipherBytes)
    }
}
