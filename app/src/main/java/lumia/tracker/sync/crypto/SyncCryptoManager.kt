package lumia.tracker.sync.crypto

import android.util.Base64
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * SyncCryptoManager - Production-grade Zero-Trust Cryptographic Engine for Lumia P2P Sync.
 * 
 * Handles:
 * 1. End-to-End Encryption (AES-256-GCM) with 128-bit authentication tag and dynamic 12-byte IVs.
 * 2. Mutual challenge-response authentication (HMAC-SHA256).
 * 3. Permanent Pre-Shared Key (PSK) derivation for 1-time handshake pairing.
 * 4. Deterministic Global Live Mesh Relay Channel ID derivation with zero identity leakage.
 * 5. Deterministic Passphrase Relay Channel ID and 256-bit AES Key derivation for zero-cloud room sync.
 * 6. Memorable passphrase generation.
 * 7. Payload integrity checksum verification (SHA-256).
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Cryptographic primitives for AES-256-GCM E2EE, HMAC-SHA256 mutual auth, deterministic mesh channels, PSK key derivation, and passphrase relay sync",
    category = "Security"
)
object SyncCryptoManager {

    private val secureRandom = SecureRandom()
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val PSK_SALT = "LUMIA_P2P_PERMANENT_PSK_SALT_"
    private const val SYNC_KEY_SALT = "LUMIA_P2P_SYNC_SALT_"
    private const val GLOBAL_MESH_CHANNEL_SALT = "LUMIA_GLOBAL_MESH_CHANNEL_SALT_v2_"
    private const val PASSPHRASE_CHANNEL_SALT = "LUMIA_PASSPHRASE_RELAY_CHANNEL_SALT_v1_"
    private const val PASSPHRASE_KEY_SALT = "LUMIA_PASSPHRASE_AES_KEY_SALT_v1_"

    /**
     * Generates a 6-digit numeric PIN for user pairing verification during initial setup.
     */
    @ValueScore(
        score = 88,
        importance = Importance.HIGH,
        description = "Generates secure 6-digit numeric pairing PIN for zero-trust visual verification",
        category = "Security"
    )
    fun generatePairingPin(): String {
        val pinNumber = secureRandom.nextInt(900000) + 100000
        return pinNumber.toString()
    }

    /**
     * Generates a random cryptographic nonce in hexadecimal format.
     */
    @ValueScore(
        score = 92,
        importance = Importance.CRITICAL,
        description = "Generates cryptographically secure random nonces for replay prevention",
        category = "Security"
    )
    fun generateNonce(lengthBytes: Int = 16): String {
        val bytes = ByteArray(lengthBytes)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a permanent 256-bit symmetric Pre-Shared Trust Key (PSK) during initial 1-time handshake.
     */
    @ValueScore(
        score = 96,
        importance = Importance.CRITICAL,
        description = "Derives permanent 256-bit symmetric Pre-Shared Trust Key (PSK) via SHA-256 with domain separation",
        category = "Security"
    )
    fun derivePSK(pinOrSeed: String, clientNonce: String, serverNonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(PSK_SALT.toByteArray(Charsets.UTF_8))
        digest.update(pinOrSeed.toByteArray(Charsets.UTF_8))
        digest.update(clientNonce.toByteArray(Charsets.UTF_8))
        digest.update(serverNonce.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a deterministic Global Live Mesh channel ID from a Pre-Shared Key (PSK).
     * This allows paired devices anywhere in the world to subscribe to the exact same
     * live relay topic over WebSocket/SSE without exposing device identifiers or metadata.
     */
    @ValueScore(
        score = 97,
        importance = Importance.CRITICAL,
        description = "Derives deterministic 32-character global live mesh channel ID from PSK with zero identity leakage",
        category = "Security"
    )
    fun deriveMeshChannelId(psk: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(GLOBAL_MESH_CHANNEL_SALT.toByteArray(Charsets.UTF_8))
        digest.update(psk.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Derives a deterministic pairwise Global Live Mesh channel ID for two specific device IDs and PSK.
     * Sorts device IDs lexicographically so both devices compute the identical channel identifier.
     */
    @ValueScore(
        score = 95,
        importance = Importance.CRITICAL,
        description = "Derives deterministic pairwise 32-character global live mesh channel ID for lexicographically sorted device pair",
        category = "Security"
    )
    fun derivePairwiseChannelId(deviceIdA: String, deviceIdB: String, psk: String): String {
        val sortedIds = if (deviceIdA <= deviceIdB) "$deviceIdA:$deviceIdB" else "$deviceIdB:$deviceIdA"
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(GLOBAL_MESH_CHANNEL_SALT.toByteArray(Charsets.UTF_8))
        digest.update(sortedIds.toByteArray(Charsets.UTF_8))
        digest.update(psk.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Derives a deterministic 32-character hexadecimal Global Live Mesh relay channel ID
     * from a user-configured synchronization passphrase.
     * Allows zero-cloud, non-database multi-device synchronization over anonymous relay WebSocket.
     */
    @ValueScore(
        score = 96,
        importance = Importance.CRITICAL,
        description = "Derives deterministic 32-character hex relay room channel ID from passphrase for anonymous zero-cloud sync",
        category = "Security"
    )
    fun derivePassphraseChannelId(passphrase: String): String {
        val normalized = passphrase.trim().lowercase()
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(PASSPHRASE_CHANNEL_SALT.toByteArray(Charsets.UTF_8))
        digest.update(normalized.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Derives a deterministic 256-bit symmetric AES key (64 hex characters) from a passphrase.
     * Used for authenticated AES-256-GCM End-to-End Encryption in relay mesh rooms.
     */
    @ValueScore(
        score = 96,
        importance = Importance.CRITICAL,
        description = "Derives deterministic 256-bit symmetric AES key from passphrase for zero-knowledge E2EE",
        category = "Security"
    )
    fun derivePassphraseKey(passphrase: String): String {
        val normalized = passphrase.trim().lowercase()
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(PASSPHRASE_KEY_SALT.toByteArray(Charsets.UTF_8))
        digest.update(normalized.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a memorable, human-friendly hyphenated passphrase (e.g. 'lunar-focus-atlas-42')
     * for easy cross-device room setup without cloud databases or accounts.
     */
    @ValueScore(
        score = 90,
        importance = Importance.HIGH,
        description = "Generates human-memorable hyphenated passphrase for zero-cloud room sync",
        category = "Security"
    )
    fun generateMemorablePassphrase(): String {
        val adjectives = listOf(
            "lunar", "solar", "cosmic", "quantum", "stellar", "astral", "zenith", "hyper",
            "neon", "amber", "velvet", "crystal", "silent", "prime", "swift", "calm",
            "noble", "vivid", "golden", "shadow", "radiant", "mystic", "bright", "emerald"
        )
        val concepts = listOf(
            "focus", "flow", "pulse", "orbit", "prism", "nexus", "matrix", "beacon",
            "vector", "spark", "echo", "vertex", "strata", "flux", "horizon", "tempo",
            "logic", "cipher", "haven", "zen", "stride", "spark", "rhythm", "signal"
        )
        val nouns = listOf(
            "atlas", "phoenix", "falcon", "voyager", "aurora", "comet", "nebula", "titan",
            "orion", "chronos", "glider", "ranger", "pioneer", "drifter", "sentry", "monarch",
            "vanguard", "apex", "harbor", "solace", "sentinel", "scout", "summit", "orbit"
        )
        val adj = adjectives[secureRandom.nextInt(adjectives.size)]
        val concept = concepts[secureRandom.nextInt(concepts.size)]
        val noun = nouns[secureRandom.nextInt(nouns.size)]
        val number = secureRandom.nextInt(90) + 10 // 10..99
        return "$adj-$concept-$noun-$number"
    }

    /**
     * Derives a 256-bit symmetric AES key from the pairing PIN/PSK and session nonce.
     */
    private fun deriveKey(keyOrPin: String, nonce: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(SYNC_KEY_SALT.toByteArray(Charsets.UTF_8))
        digest.update(keyOrPin.toByteArray(Charsets.UTF_8))
        digest.update(nonce.toByteArray(Charsets.UTF_8))
        val keyBytes = digest.digest()
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Calculates HMAC-SHA256 signature for mutual authentication challenge-response.
     */
    @ValueScore(
        score = 94,
        importance = Importance.CRITICAL,
        description = "Calculates HMAC-SHA256 signature for mutual challenge-response authentication",
        category = "Security"
    )
    fun calculateAuthHash(keyOrPin: String, nonce: String, deviceId: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(keyOrPin.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val data = "$nonce:$deviceId:LUMIA_AUTH".toByteArray(Charsets.UTF_8)
        val hashBytes = mac.doFinal(data)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verifies that the peer's authentication response matches the expected HMAC-SHA256
     * using constant-time comparison to prevent timing side-channel vulnerabilities.
     */
    @ValueScore(
        score = 96,
        importance = Importance.CRITICAL,
        description = "Constant-time HMAC-SHA256 verification preventing timing side-channel attacks",
        category = "Security"
    )
    fun verifyAuthHash(keyOrPin: String, nonce: String, deviceId: String, receivedHash: String): Boolean {
        return try {
            val expected = calculateAuthHash(keyOrPin, nonce, deviceId)
            MessageDigest.isEqual(
                expected.toByteArray(Charsets.UTF_8),
                receivedHash.toByteArray(Charsets.UTF_8)
            )
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with a newly generated random 12-byte IV.
     * Returns a Pair of (Base64 Encrypted Ciphertext, Base64 IV).
     */
    @ValueScore(
        score = 98,
        importance = Importance.CRITICAL,
        description = "AES-256-GCM authenticated encryption with dynamic 12-byte IV and 128-bit authentication tag",
        category = "Security"
    )
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
    @ValueScore(
        score = 98,
        importance = Importance.CRITICAL,
        description = "AES-256-GCM authenticated decryption verifying 128-bit tag and 12-byte IV",
        category = "Security"
    )
    fun decryptPayload(encryptedBase64: String, ivBase64: String, keyOrPin: String, nonce: String): ByteArray {
        val keySpec = deriveKey(keyOrPin, nonce)
        val cipherBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        return cipher.doFinal(cipherBytes)
    }

    /**
     * Encrypts a UTF-8 string using AES-256-GCM.
     */
    @ValueScore(
        score = 92,
        importance = Importance.HIGH,
        description = "AES-256-GCM string encryption helper",
        category = "Security"
    )
    fun encryptString(plainText: String, keyOrPin: String, nonce: String): Pair<String, String> {
        return encryptPayload(plainText.toByteArray(Charsets.UTF_8), keyOrPin, nonce)
    }

    /**
     * Decrypts AES-256-GCM Base64 ciphertext into a UTF-8 string.
     */
    @ValueScore(
        score = 92,
        importance = Importance.HIGH,
        description = "AES-256-GCM string decryption helper",
        category = "Security"
    )
    fun decryptString(encryptedBase64: String, ivBase64: String, keyOrPin: String, nonce: String): String {
        val decryptedBytes = decryptPayload(encryptedBase64, ivBase64, keyOrPin, nonce)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Calculates SHA-256 checksum for payload integrity verification.
     */
    @ValueScore(
        score = 90,
        importance = Importance.HIGH,
        description = "SHA-256 checksum calculation for data integrity verification",
        category = "Security"
    )
    fun calculatePayloadChecksum(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies SHA-256 checksum of data against an expected checksum in constant time.
     */
    @ValueScore(
        score = 92,
        importance = Importance.HIGH,
        description = "Constant-time verification of payload SHA-256 integrity checksums",
        category = "Security"
    )
    fun verifyPayloadChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        return try {
            val calculated = calculatePayloadChecksum(data)
            MessageDigest.isEqual(
                calculated.lowercase().toByteArray(Charsets.UTF_8),
                expectedChecksum.lowercase().toByteArray(Charsets.UTF_8)
            )
        } catch (e: Exception) {
            false
        }
    }
}
