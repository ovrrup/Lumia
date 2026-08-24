package lumia.tracker.sync.model

import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Represents a remote device discovered on the local network or paired via QR/PIN.
 */
@JsonClass(generateAdapter = true)
data class SyncDevice(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 52934,
    val lastSeen: Long = System.currentTimeMillis(),
    val avatarEmoji: String = "DEV",
    val isPaired: Boolean = false
) : Serializable

/**
 * Represents a permanently paired trusted device stored locally after 1-time handshake.
 * Eliminates the need for any repeated PIN entry or QR scanning.
 */
@JsonClass(generateAdapter = true)
data class TrustedPeer(
    val deviceId: String,
    val deviceName: String,
    val preSharedKey: String, // 256-bit Hex PSK derived during initial 1-time handshake
    val pairedAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long = 0L,
    val autoSyncEnabled: Boolean = true,
    val avatarEmoji: String = "DEV"
) : Serializable

/**
 * Sync modes available during peer-to-peer data exchange.
 */
enum class SyncMode {
    /**
     * Smart bi-directional merge: combines courses, subjects, tasks, notes, pomodoro
     * records and profiles without duplicate entries or data loss.
     */
    SMART_MERGE,

    /**
     * Overwrite peer: Sends this device's full data state to the peer device.
     */
    PUSH_TO_PEER,

    /**
     * Pull remote: Replaces this device's data state with the peer device's data.
     */
    PULL_FROM_PEER
}

/**
 * Real-time state of the synchronization engine.
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Discovering(val foundCount: Int = 0) : SyncState()
    data class Connecting(val peerName: String) : SyncState()
    data class Authenticating(val peerName: String) : SyncState()
    data class ExchangingData(val peerName: String, val progress: Float, val stageText: String) : SyncState()
    data class Merging(val stageText: String) : SyncState()
    data class Success(val report: SyncMergeReport) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Detailed report generated after a successful synchronization cycle.
 */
@JsonClass(generateAdapter = true)
data class SyncMergeReport(
    val coursesMerged: Int = 0,
    val subjectsMerged: Int = 0,
    val tasksMerged: Int = 0,
    val assignmentsMerged: Int = 0,
    val notesMerged: Int = 0,
    val pomodoroSessionsMerged: Int = 0,
    val profilesSynced: Int = 0,
    val tagCustomizationsMerged: Int = 0,
    val peerDeviceName: String = "",
    val syncTimestamp: Long = System.currentTimeMillis(),
    val syncMode: String = "SMART_MERGE"
) : Serializable

/**
 * Compact pairing token encoded inside QR codes or shared via text.
 */
@JsonClass(generateAdapter = true)
data class SyncPairingToken(
    val v: Int = 1,
    val deviceId: String,
    val deviceName: String,
    val ip: String,
    val port: Int,
    val pin: String,
    val nonce: String,
    val avatarEmoji: String = "DEV"
) : Serializable

/**
 * P2P Protocol frame envelope exchanged between Lumia devices.
 */
@JsonClass(generateAdapter = true)
data class SyncMessage(
    val type: String, // HELLO, CHALLENGE, AUTH, AUTH_OK, AUTH_FAIL, SYNC_DATA, SYNC_ACK, ERROR, DISCONNECT
    val deviceId: String = "",
    val deviceName: String = "",
    val avatarEmoji: String = "DEV",
    val nonce: String = "",
    val authHash: String = "",
    val syncMode: String = "SMART_MERGE",
    val isTrustedAuth: Boolean = false, // True when utilizing 1-time handshake PSK
    val psk: String? = null, // Derived PSK sent during 1-time pairing setup
    val payloadEncryptedBase64: String? = null,
    val ivBase64: String? = null,
    val reportJson: String? = null,
    val errorMessage: String? = null
) : Serializable

/**
 * Historical record of a completed sync session.
 */
@JsonClass(generateAdapter = true)
data class SyncHistoryRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val peerDeviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String,
    val isSuccess: Boolean = true
) : Serializable
