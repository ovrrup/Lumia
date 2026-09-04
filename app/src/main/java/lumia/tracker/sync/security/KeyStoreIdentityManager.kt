package lumia.tracker.sync.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages zero-trust, hardware-backed device identity using AndroidKeyStore.
 * Generates and stores EC keypairs inside the device's hardware enclave (TEE / StrongBox),
 * generates cryptographically verifiable device fingerprints, signs ephemeral ECDH handshakes,
 * and derives authenticated session keys and SAS verification tokens.
 */
class KeyStoreIdentityManager(private val context: Context) {

    companion object {
        private const val TAG = "KeyStoreIdentity"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val IDENTITY_KEY_ALIAS = "lumia_p2p_identity_key_v1"
        private const val EC_CURVE = "secp256r1"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        private const val HKDF_SALT = "LUMIA_P2P_ZERO_TRUST_SALT_v1"
        private const val HKDF_INFO_SESSION = "LUMIA_P2P_SESSION_ENC_v1"
        private const val HKDF_INFO_SAS = "LUMIA_P2P_SAS_VERIFICATION_v1"

        private const val PREFS_NAME = "lumia_p2p_identity_prefs"
        private const val PREF_TRUSTED_PEERS = "trusted_peers"
    }

    private val secureRandom = SecureRandom()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Hardware Identity KeyPair
    val identityPublicKey: PublicKey
    val isHardwareBacked: Boolean
    val deviceFingerprint: String

    init {
        ensureIdentityKeyPair()
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val cert = keyStore.getCertificate(IDENTITY_KEY_ALIAS)
            ?: throw IllegalStateException("Failed to retrieve identity certificate from AndroidKeyStore")
        identityPublicKey = cert.publicKey
        isHardwareBacked = checkHardwareBacking(keyStore)
        deviceFingerprint = computeFingerprint(identityPublicKey)
        Log.i(TAG, "Initialized KeyStore identity: $deviceFingerprint (Hardware-backed: $isHardwareBacked)")
    }

    /**
     * Ensures an EC keypair exists in AndroidKeyStore for persistent hardware device identity.
     */
    private fun ensureIdentityKeyPair() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(IDENTITY_KEY_ALIAS)) {
            Log.i(TAG, "Generating new hardware-backed EC identity keypair in AndroidKeyStore...")
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
            val builder = KeyGenParameterSpec.Builder(
                IDENTITY_KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).apply {
                setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                setDigests(KeyProperties.DIGEST_SHA256)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(false) // Default to standard TEE for maximum device compatibility
                }
            }
            kpg.initialize(builder.build())
            kpg.generateKeyPair()
        }
    }

    /**
     * Checks whether the identity key is stored in secure hardware (TEE / StrongBox).
     */
    private fun checkHardwareBacking(keyStore: KeyStore): Boolean {
        return try {
            val privateKey = keyStore.getKey(IDENTITY_KEY_ALIAS, null) as? PrivateKey ?: return false
            val factory = KeyFactory.getInstance(privateKey.algorithm, ANDROID_KEYSTORE)
            val keyInfo = factory.getKeySpec(privateKey, KeyInfo::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
                        keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX
            } else {
                keyInfo.isInsideSecureHardware
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine hardware backing status: ${e.message}")
            false
        }
    }

    /**
     * Computes a formatted, human-readable SHA-256 fingerprint of a public key.
     * Example: "4F9A-71C2-88E0-59B1"
     */
    fun computeFingerprint(key: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(key.encoded)
        return hash.take(8).joinToString("-") { "%02X".format(it) }
    }

    /**
     * Signs data using the hardware-backed identity private key.
     */
    fun signWithIdentityKey(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = keyStore.getKey(IDENTITY_KEY_ALIAS, null) as PrivateKey
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    /**
     * Verifies data signed by a peer's identity public key.
     */
    fun verifyPeerSignature(publicKey: PublicKey, data: ByteArray, signatureBytes: ByteArray): Boolean {
        return try {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initVerify(publicKey)
            signature.update(data)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Peer signature verification failed: ${e.message}")
            false
        }
    }

    // --- Ephemeral ECDH & SAS Handshake ---

    data class EphemeralHandshakePacket(
        val identityPublicKeyBase64: String,
        val ephemeralPublicKeyBase64: String,
        val ephemeralSignatureBase64: String,
        val deviceFingerprint: String
    )

    data class HandshakeResult(
        val sharedSessionKey: ByteArray,
        val sasPayload: SasVerification.SasPayload,
        val peerFingerprint: String,
        val peerIdentityPublicKey: PublicKey
    )

    /**
     * Generates a new ephemeral ECDH keypair and signs the ephemeral public key with the hardware identity key.
     */
    fun createEphemeralHandshake(): Pair<KeyPair, EphemeralHandshakePacket> {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(EC_CURVE), secureRandom)
        val ephemeralKeyPair = kpg.generateKeyPair()

        // Sign the ephemeral public key bytes using hardware-backed identity key
        val ephemeralPubBytes = ephemeralKeyPair.public.encoded
        val signature = signWithIdentityKey(ephemeralPubBytes)

        val packet = EphemeralHandshakePacket(
            identityPublicKeyBase64 = Base64.encodeToString(identityPublicKey.encoded, Base64.NO_WRAP),
            ephemeralPublicKeyBase64 = Base64.encodeToString(ephemeralPubBytes, Base64.NO_WRAP),
            ephemeralSignatureBase64 = Base64.encodeToString(signature, Base64.NO_WRAP),
            deviceFingerprint = deviceFingerprint
        )

        return Pair(ephemeralKeyPair, packet)
    }

    /**
     * Processes remote peer's handshake packet, verifies their signature against their identity key,
     * performs ECDH key agreement, and computes SAS verification code and session encryption keys.
     */
    fun completeHandshake(
        myEphemeralKeyPair: KeyPair,
        remotePacket: EphemeralHandshakePacket
    ): HandshakeResult {
        // 1. Decode remote identity public key
        val peerIdentityPubBytes = Base64.decode(remotePacket.identityPublicKeyBase64, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance("EC")
        val peerIdentityPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(peerIdentityPubBytes))

        // 2. Decode remote ephemeral public key & signature
        val peerEphemeralPubBytes = Base64.decode(remotePacket.ephemeralPublicKeyBase64, Base64.NO_WRAP)
        val peerEphemeralPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(peerEphemeralPubBytes))
        val signatureBytes = Base64.decode(remotePacket.ephemeralSignatureBase64, Base64.NO_WRAP)

        // 3. Authenticate: Verify that remote ephemeral key is signed by remote identity key
        val isValid = verifyPeerSignature(peerIdentityPublicKey, peerEphemeralPubBytes, signatureBytes)
        if (!isValid) {
            throw SecurityException("Zero-Trust Handshake Failed: Remote peer signature verification rejected! Possible MITM attack.")
        }

        val peerFingerprint = computeFingerprint(peerIdentityPublicKey)

        // 4. Perform ECDH Key Agreement
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(myEphemeralKeyPair.private)
        keyAgreement.doPhase(peerEphemeralPublicKey, true)
        val rawSharedSecret = keyAgreement.generateSecret()

        // 5. Derive SAS Verification material via HKDF
        val saltBytes = HKDF_SALT.toByteArray(Charsets.UTF_8)
        val sasBytes = Hkdf.deriveKey(
            salt = saltBytes,
            ikm = rawSharedSecret,
            info = HKDF_INFO_SAS.toByteArray(Charsets.UTF_8),
            length = 32
        )
        val sasPayload = SasVerification.deriveSas(sasBytes)

        // 6. Derive 256-bit AES-GCM Session Key via HKDF
        val sessionKey = Hkdf.deriveKey(
            salt = saltBytes,
            ikm = rawSharedSecret,
            info = HKDF_INFO_SESSION.toByteArray(Charsets.UTF_8),
            length = 32
        )

        Log.i(TAG, "Completed ECDH handshake with peer $peerFingerprint. SAS: ${sasPayload.numericCode}")

        return HandshakeResult(
            sharedSessionKey = sessionKey,
            sasPayload = sasPayload,
            peerFingerprint = peerFingerprint,
            peerIdentityPublicKey = peerIdentityPublicKey
        )
    }

    // --- AES-256-GCM Authenticated Encryption for DataChannel Frames ---

    /**
     * Encrypts plaintext using AES-256-GCM with a dynamic 12-byte IV and 128-bit authentication tag.
     * Output format: [12 bytes IV] + [Ciphertext + 16 bytes GCM Tag]
     */
    fun encryptFrame(sessionKey: ByteArray, plaintext: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sessionKey, "AES"), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(plaintext)

        val output = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(ciphertext, 0, output, iv.size, ciphertext.size)
        return output
    }

    /**
     * Decrypts AES-256-GCM encrypted frame and verifies the 128-bit authentication tag.
     */
    fun decryptFrame(sessionKey: ByteArray, encryptedFrame: ByteArray): ByteArray {
        require(encryptedFrame.size > GCM_IV_LENGTH) { "Ciphertext too short for GCM frame" }
        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(encryptedFrame, 0, iv, 0, GCM_IV_LENGTH)

        val ciphertext = ByteArray(encryptedFrame.size - GCM_IV_LENGTH)
        System.arraycopy(encryptedFrame, GCM_IV_LENGTH, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sessionKey, "AES"), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    // --- Trusted Peer Pinning Storage ---

    data class PinnedPeer(
        val fingerprint: String,
        val identityPublicKeyBase64: String,
        val customAlias: String,
        val verifiedAtTimestamp: Long,
        val isHardwareBacked: Boolean
    )

    fun getPinnedPeers(): List<PinnedPeer> {
        val raw = prefs.getString(PREF_TRUSTED_PEERS, null) ?: return emptyList()
        return raw.split(";").filter { it.isNotBlank() }.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 5) {
                PinnedPeer(
                    fingerprint = parts[0],
                    identityPublicKeyBase64 = parts[1],
                    customAlias = parts[2],
                    verifiedAtTimestamp = parts[3].toLongOrNull() ?: 0L,
                    isHardwareBacked = parts[4].toBoolean()
                )
            } else null
        }
    }

    fun pinPeer(peer: PinnedPeer) {
        val current = getPinnedPeers().filter { it.fingerprint != peer.fingerprint }.toMutableList()
        current.add(peer)
        savePinnedPeers(current)
        Log.i(TAG, "Pinned trusted peer: ${peer.fingerprint} (${peer.customAlias})")
    }

    fun unpinPeer(fingerprint: String) {
        val updated = getPinnedPeers().filter { it.fingerprint != fingerprint }
        savePinnedPeers(updated)
        Log.i(TAG, "Unpinned peer: $fingerprint")
    }

    fun isPeerPinned(fingerprint: String): Boolean {
        return getPinnedPeers().any { it.fingerprint == fingerprint }
    }

    private fun savePinnedPeers(peers: List<PinnedPeer>) {
        val serialized = peers.joinToString(";") {
            "${it.fingerprint}|${it.identityPublicKeyBase64}|${it.customAlias}|${it.verifiedAtTimestamp}|${it.isHardwareBacked}"
        }
        prefs.edit().putString(PREF_TRUSTED_PEERS, serialized).apply()
    }
}
