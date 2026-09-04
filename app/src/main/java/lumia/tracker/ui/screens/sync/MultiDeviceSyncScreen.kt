package lumia.tracker.ui.screens.sync

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lumia.tracker.sync.P2PSyncEngine
import lumia.tracker.sync.reconciliation.VectorReconciliationOrchestrator
import lumia.tracker.sync.transport.StatelessSignaling
import lumia.tracker.sync.transport.TransportConnectionState
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.sync.components.SasVerificationDialog
import lumia.tracker.ui.screens.sync.components.StatelessTokenDialog
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * MultiDeviceSyncScreen - Master Cockpit for Lumia's Zero-Trust, Database-Free P2P Sync Engine.
 * Provides live telemetry for WebRTC DataChannel (CGNAT/STUN/ICE, 20s keep-alives),
 * Automerge-style CRDT Document inspection via ByteArray Kotlin flows,
 * and hardware-backed AndroidKeyStore identity with HKDF-derived SAS MITM verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDeviceSyncScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncEngine = remember { P2PSyncEngine.getInstance(context) }

    // Reactive StateFlows
    val connectionState by syncEngine.transport.connectionState.collectAsStateWithLifecycle()
    val transportTelemetry by syncEngine.transport.telemetry.collectAsStateWithLifecycle()
    val crdtSnapshot by syncEngine.crdtDocument.documentState.collectAsStateWithLifecycle()
    val vectorClock by syncEngine.crdtDocument.vectorClockState.collectAsStateWithLifecycle()
    val reconciliationTelemetry by syncEngine.orchestrator.telemetry.collectAsStateWithLifecycle()
    val activeSas by syncEngine.activeSasVerification.collectAsStateWithLifecycle()
    val isFgServiceActive by syncEngine.isForegroundServiceEnabled.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSignalingDialog by remember { mutableStateOf(false) }
    var generatedToken by remember { mutableStateOf("") }

    // CRDT Sandbox inputs
    var inputPath by remember { mutableStateOf("study/tasks") }
    var inputKey by remember { mutableStateOf("task_title") }
    var inputValue by remember { mutableStateOf("Advanced Calculus Revision") }

    // Dialog: SAS Verification Modal
    activeSas?.let { (sasPayload, peerFingerprint) ->
        SasVerificationDialog(
            sasPayload = sasPayload,
            peerFingerprint = peerFingerprint,
            onConfirm = {
                syncEngine.confirmSasVerification()
                Toast.makeText(context, "Peer verified and pinned!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                syncEngine.dismissSasVerification()
                Toast.makeText(context, "SAS rejected", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Stateless Signaling Dialog
    if (showSignalingDialog) {
        StatelessTokenDialog(
            myToken = generatedToken,
            onConnectWithToken = { tokenString ->
                showSignalingDialog = false
                scope.launch {
                    val payload = StatelessSignaling.parseToken(tokenString)
                    if (payload != null) {
                        if (payload.role == "OFFERER") {
                            val answer = syncEngine.transport.acceptSignalingOffer(payload)
                            generatedToken = StatelessSignaling.createToken(answer)
                            Toast.makeText(context, "Offer accepted! Share your answer token.", Toast.LENGTH_LONG).show()
                            showSignalingDialog = true
                        } else {
                            syncEngine.transport.applySignalingAnswer(payload)
                            Toast.makeText(context, "Answer applied! WebRTC DataChannel connected.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Invalid signaling token", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showSignalingDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Zero-Trust P2P Sync",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "WebRTC DataChannel • Automerge CRDT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val offer = syncEngine.transport.createSignalingOffer()
                            generatedToken = StatelessSignaling.createToken(offer)
                            showSignalingDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.QrCode,
                            contentDescription = "Signaling QR",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Pill Switcher
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "WebRTC",
                                fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "CRDT Engine",
                                fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Security & SAS",
                                fontWeight = if (selectedTab == 2) FontWeight.Black else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> WebRtcTransportTab(
                    connectionState = connectionState,
                    telemetry = transportTelemetry,
                    isFgServiceActive = isFgServiceActive,
                    onToggleFgService = { syncEngine.setForegroundServiceEnabled(it) },
                    onInitiatePairing = {
                        scope.launch {
                            val offer = syncEngine.transport.createSignalingOffer()
                            generatedToken = StatelessSignaling.createToken(offer)
                            showSignalingDialog = true
                        }
                    },
                    onRestartTransport = {
                        syncEngine.transport.stop()
                        syncEngine.transport.start()
                        Toast.makeText(context, "Restarted WebRTC transport", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> CrdtDocumentTab(
                    snapshot = crdtSnapshot,
                    vectorClock = vectorClock,
                    reconciliation = reconciliationTelemetry,
                    inputPath = inputPath,
                    onPathChange = { inputPath = it },
                    inputKey = inputKey,
                    onKeyChange = { inputKey = it },
                    inputValue = inputValue,
                    onValueChange = { inputValue = it },
                    onApplyMutation = {
                        syncEngine.crdtDocument.set(inputPath, inputKey, inputValue)
                        Toast.makeText(context, "Applied CRDT Mutation (ByteArray Flow)", Toast.LENGTH_SHORT).show()
                    },
                    onTriggerReconciliation = {
                        syncEngine.orchestrator.triggerReconciliation()
                        Toast.makeText(context, "Triggered Anti-Entropy Reconciliation", Toast.LENGTH_SHORT).show()
                    }
                )
                2 -> SecurityIdentityTab(
                    identityManager = syncEngine.identityManager,
                    onUnpinPeer = { fp ->
                        syncEngine.identityManager.unpinPeer(fp)
                        Toast.makeText(context, "Unpinned peer: $fp", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// =========================================================================
// TAB 0: WebRTC DataChannel & Transport Telemetry
// =========================================================================

@Composable
private fun WebRtcTransportTab(
    connectionState: TransportConnectionState,
    telemetry: lumia.tracker.sync.transport.TransportTelemetry,
    isFgServiceActive: Boolean,
    onToggleFgService: (Boolean) -> Unit,
    onInitiatePairing: () -> Unit,
    onRestartTransport: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Hero Connection Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WebRTC DataChannel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Native SCTP over DTLS transport",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Connection State Badge
                    val (badgeText, badgeColor) = when (connectionState) {
                        TransportConnectionState.CONNECTED -> "CONNECTED" to Color(0xFF34C759)
                        TransportConnectionState.KEEP_ALIVE_ACTIVE -> "20s KEEP-ALIVE" to Color(0xFF34C759)
                        TransportConnectionState.CGNAT_DISCOVERED -> "CGNAT MAPPED" to Color(0xFF007AFF)
                        TransportConnectionState.CONNECTING, TransportConnectionState.SIGNOD_EXCHANGE -> "CONNECTING" to Color(0xFFFF9500)
                        TransportConnectionState.GATHERING_ICE -> "GATHERING ICE" to Color(0xFFAF52DE)
                        else -> "STANDBY" to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(16.dp))

                // Network Telemetry Matrix
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TelemetryRow(label = "Local Host Endpoint", value = telemetry.localHostAddress ?: "Querying...")
                    TelemetryRow(label = "Public srflx Endpoint (STUN)", value = telemetry.publicReflexiveAddress ?: "Traversing CGNAT...")
                    TelemetryRow(label = "Remote Peer Endpoint", value = telemetry.remotePeerAddress ?: "None connected")
                    TelemetryRow(label = "Selected ICE Type", value = telemetry.selectedIceCandidateType)
                    TelemetryRow(label = "20s UDP Keep-Alive Interval", value = "${telemetry.keepAliveIntervalSeconds}s (Active)")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(16.dp))

                // Bandwidth & Packets Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCounter(label = "Packets Sent", value = telemetry.packetsSent.toString())
                    MetricCounter(label = "Packets Recv", value = telemetry.packetsReceived.toString())
                    MetricCounter(label = "Bytes In", value = formatBytes(telemetry.bytesReceived))
                    MetricCounter(label = "Bytes Out", value = formatBytes(telemetry.bytesSent))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Action Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Stateless Pairing & Signaling",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Establish a zero-trust WebRTC connection without database or centralized coordination",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BouncyButton(
                        onClick = onInitiatePairing,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pair via QR / Token", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRestartTransport,
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Foreground Service & Keep-Alives Toggle Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Persistent 20s Keep-Alives",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Maintains UDP NAT port bindings in background via Android Foreground Service and partial WakeLock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isFgServiceActive,
                    onCheckedChange = onToggleFgService
                )
            }
        }
    }
}

// =========================================================================
// TAB 1: Automerge CRDT Document Studio
// =========================================================================

@Composable
private fun CrdtDocumentTab(
    snapshot: lumia.tracker.sync.crdt.CrdtDocumentSnapshot,
    vectorClock: lumia.tracker.sync.crdt.VectorClock,
    reconciliation: VectorReconciliationOrchestrator.ReconciliationTelemetry,
    inputPath: String,
    onPathChange: (String) -> Unit,
    inputKey: String,
    onKeyChange: (String) -> Unit,
    inputValue: String,
    onValueChange: (String) -> Unit,
    onApplyMutation: () -> Unit,
    onTriggerReconciliation: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Explainer Banner
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AccountTree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Database-Free Automerge CRDT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Communicating solely via ByteArray Kotlin flows",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "State is tracked purely in memory with Lamport total ordering, causality vector clocks, and Last-Write-Wins (LWW). Zero SQLite or Room database queries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Anti-Entropy Vector Reconciliation Status Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anti-Entropy Reconciliation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val (reconText, reconColor) = when (reconciliation.status) {
                        VectorReconciliationOrchestrator.ReconciliationStatus.CONVERGED -> "CONVERGED" to Color(0xFF34C759)
                        VectorReconciliationOrchestrator.ReconciliationStatus.RECONCILING -> "RECONCILING" to Color(0xFF007AFF)
                        VectorReconciliationOrchestrator.ReconciliationStatus.PARTITION_DETECTED -> "PARTITION ACTIVE" to Color(0xFFFF9500)
                        else -> "IDLE" to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = reconColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, reconColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = reconText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = reconColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCounter(label = "Total Ops", value = snapshot.totalOpsCount.toString())
                    MetricCounter(label = "Lamport Clock", value = snapshot.maxLamportClock.toString())
                    MetricCounter(label = "Reconciled Ops", value = reconciliation.totalOpsReconciled.toString())
                    MetricCounter(label = "Partitions", value = reconciliation.totalPartitionsResolved.toString())
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onTriggerReconciliation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Anti-Entropy Reconciliation")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Mutation Sandbox Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CRDT Mutation Sandbox",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apply local mutations to test real-time ByteArray flow broadcasting",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputPath,
                    onValueChange = onPathChange,
                    label = { Text("Object Path") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = onKeyChange,
                        label = { Text("Field Key") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = onValueChange,
                        label = { Text("Value") },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                BouncyButton(
                    onClick = onApplyMutation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply CRDT Mutation", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Document Tree Inspector Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Live In-Memory Document Tree",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Materialized hierarchical view of the conflict-free state",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (snapshot.documentData.isEmpty()) {
                            Text(
                                text = "{ } (Empty CRDT Document)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            RenderDocumentMap(snapshot.documentData, indent = 0)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Active Vector Clock Matrix:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (vectorClock.clockMap.isEmpty()) {
                    Text(
                        text = "Vector clock is unseeded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    vectorClock.clockMap.forEach { (actor, seq) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Replica: ${actor.take(8)}...",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Seq: $seq",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: Hardware Identity & SAS MITM Verification
// =========================================================================

@Composable
private fun SecurityIdentityTab(
    identityManager: lumia.tracker.sync.security.KeyStoreIdentityManager,
    onUnpinPeer: (String) -> Unit
) {
    val pinnedPeers = remember { identityManager.getPinnedPeers() }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Local Device Hardware Identity Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hardware KeyStore Identity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (identityManager.isHardwareBacked) "TEE / StrongBox Enclave Protected" else "Software Fallback Enclave",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (identityManager.isHardwareBacked) Color(0xFF34C759) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TelemetryRow(label = "Device Fingerprint", value = identityManager.deviceFingerprint)
                    TelemetryRow(label = "Algorithm", value = "EC secp256r1 (SHA256withECDSA)")
                    TelemetryRow(label = "Key Agreement", value = "ECDH + HKDF (RFC 5869)")
                    TelemetryRow(label = "Frame Encryption", value = "AES-256-GCM (128-bit Tag)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pinned Trusted Peers Card
        ScholarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pinned Trusted Peers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Devices verified via HKDF Short Authentication String (SAS)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (pinnedPeers.isEmpty()) {
                    Text(
                        text = "No pinned peers yet. Connect and verify SAS to establish zero-trust pairing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    pinnedPeers.forEach { peer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = peer.customAlias,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "FP: ${peer.fingerprint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { onUnpinPeer(peer.fingerprint) }) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Unpin peer",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Helper Composable Components
// =========================================================================

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MetricCounter(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RenderDocumentMap(map: Map<String, Any?>, indent: Int) {
    val indentSpace = "  ".repeat(indent)
    map.forEach { (k, v) ->
        if (v is Map<*, *>) {
            Text(
                text = "$indentSpace$k: {",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            @Suppress("UNCHECKED_CAST")
            RenderDocumentMap(v as Map<String, Any?>, indent + 1)
            Text(
                text = "$indentSpace}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Text(
                text = "$indentSpace$k: \"$v\"",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
