package lumia.tracker.sync.model

import com.squareup.moshi.JsonClass
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.io.Serializable

/**
 * Enumeration of all domain entity types supported by Lumia CRDT Live Mesh Synchronization.
 */
@ValueScore(
    score = 94,
    importance = Importance.CRITICAL,
    description = "Domain entity discriminator for differential CRDT live delta replication",
    category = "SYNC"
)
enum class SyncEntityType {
    COURSE,
    SUBJECT,
    TOPIC,
    TASK,
    ASSIGNMENT,
    ATTENDANCE,
    POMODORO,
    NOTE,
    TEST_RECORD,
    CHAPTER,
    ATTACHMENT,
    TAG_CUSTOMIZATION,
    PROFILE
}

/**
 * Mutation operation type for CRDT delta packets.
 */
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "CRDT mutation operation discriminator (UPSERT / DELETE)",
    category = "SYNC"
)
enum class SyncOperation {
    UPSERT,
    DELETE
}

/**
 * Represents a single atomic differential CRDT mutation for any domain entity in Lumia.
 * Carries Last-Write-Wins (LWW) Lamport timestamps and natural key identifiers for conflict-free resolution.
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Atomic differential CRDT mutation unit with Lamport timestamps for real-time live mesh synchronization",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncDelta(
    val mutationId: String = java.util.UUID.randomUUID().toString(),
    val entityType: SyncEntityType,
    val operation: SyncOperation = SyncOperation.UPSERT,
    val entityGlobalId: String, // Deterministic natural key or UUID
    val payloadJson: String? = null, // Serialized entity JSON (null on DELETE)
    val timestamp: Long = System.currentTimeMillis(), // Physical wall-clock timestamp
    val lamportTimestamp: Long = 0L, // Lamport logical timestamp for strict causal ordering
    val originDeviceId: String,
    val version: Long = 1L
) : Serializable

/**
 * Encapsulated bundle of CRDT deltas dispatched across an active Live Mesh channel.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Batch envelope of CRDT deltas exchanged over secure live mesh data channels",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncDeltaPacket(
    val packetId: String = java.util.UUID.randomUUID().toString(),
    val channelId: String,
    val senderDeviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lamportTimestamp: Long = 0L,
    val deltas: List<SyncDelta> = emptyList(),
    val vectorClock: Map<String, Long> = emptyMap()
) : Serializable

/**
 * Represents an unacknowledged offline mutation queued in the outbox buffer.
 */
@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Offline buffer entry for queueing deltas when paired peers are disconnected",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class OfflineMutationEntry(
    val entryId: String = java.util.UUID.randomUUID().toString(),
    val targetDeviceId: String, // Specific device ID or "*" for mesh broadcast
    val delta: SyncDelta,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
) : Serializable

/**
 * Represents a remote device discovered on the local network or paired via QR/PIN.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Model representing discovered or actively connected local network P2P devices",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncDevice(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 52934,
    val lastSeen: Long = System.currentTimeMillis(),
    val avatarEmoji: String = "DEV",
    val isPaired: Boolean = false,
    val isConnected: Boolean = false
) : Serializable

/**
 * Represents a permanently paired trusted device stored locally after 1-time handshake.
 * Maintains persistent cryptographic relationships with shared channel ID and AES-GCM session key.
 */
@ValueScore(
    score = 97,
    importance = Importance.CRITICAL,
    description = "Permanently paired peer credentials and metadata for zero-interaction live sync",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class TrustedPeer(
    val deviceId: String,
    val deviceName: String,
    val channelId: String = java.util.UUID.randomUUID().toString(), // Persistent shared cryptographic channel ID
    val preSharedKey: String, // 256-bit Hex AES-GCM session key derived during 1-time handshake
    val pairedAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long = 0L,
    val autoSyncEnabled: Boolean = true,
    val avatarEmoji: String = "DEV",
    val ipAddress: String? = null,
    val port: Int = 52934,
    val isOnline: Boolean = false,
    val pendingOutboxCount: Int = 0
) : Serializable

/**
 * Sync modes available during peer-to-peer data exchange.
 */
@ValueScore(
    score = 85,
    importance = Importance.MEDIUM,
    description = "Synchronization strategy mode selector",
    category = "SYNC"
)
enum class SyncMode {
    /**
     * Live continuous differential CRDT mesh synchronization.
     */
    LIVE_MESH_CRDT,

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
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Reactive status and state machine for sync engine lifecycle",
    category = "SYNC"
)
sealed class SyncState {
    object Idle : SyncState()
    data class Discovering(val foundCount: Int = 0) : SyncState()
    data class Connecting(val peerName: String) : SyncState()
    data class Authenticating(val peerName: String) : SyncState()
    data class ExchangingData(val peerName: String, val progress: Float, val stageText: String) : SyncState()
    data class Merging(val stageText: String) : SyncState()
    data class Success(val report: SyncMergeReport) : SyncState()
    data class Error(val message: String) : SyncState()
    data class LiveMeshActive(val connectedCount: Int) : SyncState()
}

/**
 * Detailed report generated after a successful synchronization cycle or delta merge.
 */
@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Summary report containing entity mutation metrics and merge results",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncMergeReport(
    val coursesMerged: Int = 0,
    val subjectsMerged: Int = 0,
    val tasksMerged: Int = 0,
    val assignmentsMerged: Int = 0,
    val notesMerged: Int = 0,
    val pomodoroSessionsMerged: Int = 0,
    val attendanceMerged: Int = 0,
    val testRecordsMerged: Int = 0,
    val profilesSynced: Int = 0,
    val tagCustomizationsMerged: Int = 0,
    val deltasApplied: Int = 0,
    val peerDeviceName: String = "",
    val syncTimestamp: Long = System.currentTimeMillis(),
    val syncMode: String = "LIVE_MESH_CRDT"
) : Serializable

/**
 * Compact pairing token encoded inside QR codes or shared via text for initial 1-time setup.
 */
@ValueScore(
    score = 91,
    importance = Importance.HIGH,
    description = "Compact QR and textual payload for initial 1-time mutual pairing handshake",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncPairingToken(
    val v: Int = 2,
    val channelId: String = java.util.UUID.randomUUID().toString(),
    val deviceId: String,
    val deviceName: String,
    val ip: String,
    val port: Int,
    val pin: String,
    val nonce: String,
    val avatarEmoji: String = "DEV",
    val pskSeed: String = ""
) : Serializable

/**
 * P2P Protocol frame envelope exchanged across Lumia mesh sockets.
 */
@ValueScore(
    score = 95,
    importance = Importance.CRITICAL,
    description = "Binary / JSON framed wire envelope for live mesh protocol commands and payloads",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncMessage(
    val type: String, // HELLO, CHALLENGE, AUTH, AUTH_OK, AUTH_FAIL, DELTA_SYNC, DELTA_ACK, FULL_SYNC_REQ, SYNC_DATA, SYNC_ACK, HEARTBEAT, HEARTBEAT_ACK, DISCONNECT, ERROR
    val deviceId: String = "",
    val deviceName: String = "",
    val avatarEmoji: String = "DEV",
    val channelId: String = "",
    val nonce: String = "",
    val authHash: String = "",
    val syncMode: String = "LIVE_MESH_CRDT",
    val isTrustedAuth: Boolean = false, // True when utilizing 1-time handshake PSK
    val psk: String? = null, // Derived PSK sent during 1-time pairing setup
    val payloadEncryptedBase64: String? = null,
    val ivBase64: String? = null,
    val deltaPacketEncryptedBase64: String? = null,
    val reportJson: String? = null,
    val errorMessage: String? = null,
    val ackMutationIds: List<String>? = null,
    val vectorClock: Map<String, Long>? = null
) : Serializable

/**
 * Historical record of a completed sync session.
 */
@ValueScore(
    score = 80,
    importance = Importance.MEDIUM,
    description = "Persistent history record of completed synchronization operations",
    category = "SYNC"
)
@JsonClass(generateAdapter = true)
data class SyncHistoryRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val peerDeviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String,
    val isSuccess: Boolean = true
) : Serializable