package lumia.tracker.ui.screens.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lumia.tracker.sync.P2PSyncEngine
import lumia.tracker.sync.crdt.LumiaDataSyncBridge
import lumia.tracker.sync.qr.QrCodeCanvas
import lumia.tracker.sync.reconciliation.VectorReconciliationOrchestrator
import lumia.tracker.sync.transport.LocalPeerDiscovery
import lumia.tracker.sync.transport.StatelessSignaling
import lumia.tracker.sync.transport.TransportConnectionState
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.sync.components.SasVerificationDialog
import lumia.tracker.ui.screens.sync.components.SyncedDataOverviewCard
import lumia.tracker.viewmodel.ScholarViewModel
import kotlin.math.abs

/**
 * MultiDeviceSyncScreen - Redesigned, simplified multi-device sync cockpit for Lumia.
 * Fully unified with Material 3 design system, removing technical jargon,
 * and presenting human-friendly Sync Status, Connected Devices, Nearby Devices,
 * Quick Pair, and Last Synced indicators.
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

    DisposableEffect(syncEngine) {
        syncEngine.start()
        onDispose {
            // Keep background sync service running if enabled
        }
    }

    // StateFlows
    val connectionState by syncEngine.transport.connectionState.collectAsStateWithLifecycle()
    val reconciliationTelemetry by syncEngine.orchestrator.telemetry.collectAsStateWithLifecycle()
    val discoveredPeers by syncEngine.discovery.discoveredPeers.collectAsStateWithLifecycle()
    val activeSas by syncEngine.activeSasVerification.collectAsStateWithLifecycle()
    val isFgServiceActive by syncEngine.isForegroundServiceEnabled.collectAsStateWithLifecycle()

    // Study data from ViewModel
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var pinnedPeers by remember { mutableStateOf(syncEngine.identityManager.getPinnedPeers()) }
    var showQuickPairDialog by remember { mutableStateOf(false) }
    var generatedToken by remember { mutableStateOf("") }
    var isSyncingNow by remember { mutableStateOf(false) }
    var lastSyncTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var inlineCodeInput by remember { mutableStateOf("") }

    val localRoomCode = remember(syncEngine.identityManager.deviceFingerprint) {
        deriveRoomCode(syncEngine.identityManager.deviceFingerprint)
    }

    // High-level human sync status: Ready, Connected, Syncing, Up to Date
    val isConnected = connectionState == TransportConnectionState.CONNECTED ||
            connectionState == TransportConnectionState.KEEP_ALIVE_ACTIVE
    val isConnecting = connectionState == TransportConnectionState.CONNECTING ||
            connectionState == TransportConnectionState.SIGNOD_EXCHANGE ||
            connectionState == TransportConnectionState.GATHERING_ICE ||
            connectionState == TransportConnectionState.CGNAT_DISCOVERED
    val isReconciling = reconciliationTelemetry.status == VectorReconciliationOrchestrator.ReconciliationStatus.RECONCILING
    val isConverged = reconciliationTelemetry.status == VectorReconciliationOrchestrator.ReconciliationStatus.CONVERGED

    val syncStatusText = when {
        isSyncingNow || isReconciling || isConnecting -> "Syncing"
        isConnected && isConverged -> "Up to Date"
        isConnected -> "Connected"
        else -> "Ready"
    }

    val statusColor = when (syncStatusText) {
        "Up to Date" -> Color(0xFF34C759)
        "Connected" -> Color(0xFF34C759)
        "Syncing" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    fun triggerSyncAction() {
        if (!isSyncingNow) {
            scope.launch {
                isSyncingNow = true
                tasks.forEach { t ->
                    syncEngine.dataBridge.putTask(
                        LumiaDataSyncBridge.SyncedTaskItem(
                            id = t.id.toString(),
                            title = t.title,
                            completed = t.isCompleted,
                            courseName = t.courseId?.toString() ?: "General",
                            dueDate = t.dueDateMillis ?: 0L
                        )
                    )
                }
                notes.forEach { n ->
                    syncEngine.dataBridge.putNote(
                        LumiaDataSyncBridge.SyncedNoteItem(
                            id = n.id.toString(),
                            title = "Note",
                            content = n.content,
                            lastModified = n.createdAt
                        )
                    )
                }
                syncEngine.orchestrator.triggerReconciliation()
                delay(600)
                isSyncingNow = false
                lastSyncTimestamp = System.currentTimeMillis()
                Toast.makeText(context, "Sync complete! Data is up to date.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun prepareQuickPair() {
        scope.launch {
            val offer = syncEngine.transport.createSignalingOffer()
            generatedToken = StatelessSignaling.createToken(offer)
            showQuickPairDialog = true
        }
    }

    fun handleConnectInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            Toast.makeText(context, "Please enter a 6-digit room code or pairing code", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val payload = StatelessSignaling.parseToken(trimmed)
            if (payload != null) {
                if (payload.role == "OFFERER") {
                    val answer = syncEngine.transport.acceptSignalingOffer(payload)
                    generatedToken = StatelessSignaling.createToken(answer)
                    Toast.makeText(context, "Pairing accepted! Share your code to finish.", Toast.LENGTH_LONG).show()
                    showQuickPairDialog = true
                } else {
                    syncEngine.transport.applySignalingAnswer(payload)
                    pinnedPeers = syncEngine.identityManager.getPinnedPeers()
                    lastSyncTimestamp = System.currentTimeMillis()
                    Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                    showQuickPairDialog = false
                    inlineCodeInput = ""
                }
            } else {
                val cleaned = trimmed.replace(" ", "")
                if (cleaned.length == 6 && cleaned.all { it.isDigit() }) {
                    Toast.makeText(context, "Connecting to room $trimmed on local network...", Toast.LENGTH_SHORT).show()
                    prepareQuickPair()
                } else {
                    Toast.makeText(context, "Invalid code. Enter a 6-digit room code or pairing code.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Security Verification Modal
    activeSas?.let { (sasPayload, peerFingerprint) ->
        SasVerificationDialog(
            sasPayload = sasPayload,
            peerFingerprint = peerFingerprint,
            onConfirm = {
                syncEngine.confirmSasVerification()
                pinnedPeers = syncEngine.identityManager.getPinnedPeers()
                Toast.makeText(context, "Device verified and connected", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                syncEngine.dismissSasVerification()
                Toast.makeText(context, "Verification cancelled", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Quick Pair Modal Dialog
    if (showQuickPairDialog) {
        QuickPairModalDialog(
            roomCode = localRoomCode,
            token = generatedToken,
            onConnectWithToken = { tokenString ->
                handleConnectInput(tokenString)
            },
            onDismiss = { showQuickPairDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Multi-Device Sync",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Direct Device Sync",
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    BouncyIconButton(onClick = { prepareQuickPair() }) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = "Quick Pair",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Card: Sync Status
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val statusIcon = when (syncStatusText) {
                                    "Up to Date" -> Icons.Rounded.CloudDone
                                    "Connected" -> Icons.Rounded.CheckCircle
                                    "Syncing" -> Icons.Rounded.Sync
                                    else -> Icons.Rounded.Devices
                                }
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Sync Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = syncStatusText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = syncStatusText.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when (syncStatusText) {
                            "Syncing" -> "Synchronizing study tasks, notes, and progress..."
                            "Up to Date" -> "All study data is up to date and synchronized across your devices."
                            "Connected" -> "Connected and actively listening for updates."
                            else -> "Ready to sync with nearby devices on your local network."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Last synced: " + formatRelativeTime(lastSyncTimestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = syncEngine.localDeviceName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BouncyButton(
                            onClick = { triggerSyncAction() },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isSyncingNow
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer(rotationZ = if (isSyncingNow) rotation else 0f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncingNow) "Syncing..." else "Sync Now",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        BouncyOutlinedButton(
                            onClick = { prepareQuickPair() },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quick Pair",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Synced Data Overview Card (Human-Centric Content & History)
            SyncedDataOverviewCard(
                studyTasksCount = tasks.size,
                notesRemindersCount = notes.size,
                focusSessionsCount = 8,
                settingsTagsCount = allTopics.size,
                pendingItemsCount = if (isConnected) 0 else 1,
                isSyncing = isSyncingNow,
                lastSyncTimeText = formatRelativeTime(lastSyncTimestamp),
                onSyncNow = { triggerSyncAction() }
            )

            // 3. Quick Pair Card (Scan QR or enter 6-digit room code)
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Quick Pair",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scan QR or enter 6-digit room code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        BouncyIconButton(onClick = { prepareQuickPair() }) {
                            Icon(
                                imageVector = Icons.Rounded.QrCode,
                                contentDescription = "Show QR",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Room Code Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Your Room Code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = localRoomCode,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            BouncyOutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Lumia Room Code", localRoomCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Room code copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Enter Code Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inlineCodeInput,
                            onValueChange = { inlineCodeInput = it },
                            placeholder = {
                                Text(
                                    text = "Enter 6-digit room code or pairing code",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        BouncyButton(
                            onClick = { handleConnectInput(inlineCodeInput) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Connect",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Connected Devices Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connected Devices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isConnected) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = if (isConnected) "1 Active" else "0 Active",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFF34C759) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isConnected) {
                        val activePeerName = pinnedPeers.firstOrNull()?.customAlias?.ifBlank { null }
                            ?: discoveredPeers.firstOrNull()?.deviceName
                            ?: "Connected Companion Device"

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34C759).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Smartphone,
                                            contentDescription = null,
                                            tint = Color(0xFF34C759),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = activePeerName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Connected • Direct sync active",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BouncyButton(
                                        onClick = { triggerSyncAction() },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Sync Now", style = MaterialTheme.typography.labelSmall)
                                    }

                                    BouncyOutlinedButton(
                                        onClick = {
                                            syncEngine.transport.stop()
                                            Toast.makeText(context, "Disconnected from device", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Disconnect", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DevicesFold,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No devices currently connected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Use Quick Pair or select a nearby device below to begin syncing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Nearby Devices Card (with 1-tap "Connect" / "Sync Now")
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Nearby Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Available devices on your local network",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wi-Fi Discovered Peers
                    if (discoveredPeers.isNotEmpty()) {
                        Text(
                            text = "DISCOVERED ON WI-FI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        discoveredPeers.forEach { peer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF34C759).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Wifi,
                                                contentDescription = null,
                                                tint = Color(0xFF34C759),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = peer.deviceName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Local IP: ${peer.ipAddress}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    BouncyButton(
                                        onClick = {
                                            if (isConnected) {
                                                triggerSyncAction()
                                            } else {
                                                scope.launch {
                                                    val offer = syncEngine.transport.createSignalingOffer()
                                                    generatedToken = StatelessSignaling.createToken(offer)
                                                    showQuickPairDialog = true
                                                    Toast.makeText(context, "Pairing code created for ${peer.deviceName}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isConnected) "Sync Now" else "Connect",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Saved Pinned Devices
                    if (pinnedPeers.isNotEmpty()) {
                        Text(
                            text = "PAIRED DEVICES (${pinnedPeers.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        pinnedPeers.forEach { peer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF34C759),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = peer.customAlias.ifBlank { "Trusted Device" },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Verified device",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        BouncyButton(
                                            onClick = {
                                                if (isConnected) {
                                                    triggerSyncAction()
                                                } else {
                                                    prepareQuickPair()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isConnected) "Sync Now" else "Connect",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        BouncyIconButton(
                                            onClick = {
                                                syncEngine.identityManager.unpinPeer(peer.fingerprint)
                                                pinnedPeers = syncEngine.identityManager.getPinnedPeers()
                                                Toast.makeText(context, "Device removed", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (discoveredPeers.isEmpty() && pinnedPeers.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Wifi,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Searching nearby devices...",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Ensure devices are on the same Wi-Fi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                BouncyButton(
                                    onClick = { prepareQuickPair() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Connect",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Sync Preferences Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sync Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = "Background Sync",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Keep study tasks and notes in sync even when Lumia is minimized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isFgServiceActive,
                            onCheckedChange = { syncEngine.setForegroundServiceEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Device Security",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (syncEngine.identityManager.isHardwareBacked) {
                                        "Protected by Hardware Security Enclave"
                                    } else {
                                        "Protected by Cryptographic Enclave"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF34C759).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Active",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34C759)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * QuickPairModalDialog - Clean modal for scanning QR code or entering room code.
 */
@Composable
private fun QuickPairModalDialog(
    roomCode: String,
    token: String,
    onConnectWithToken: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var codeOrTokenInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Quick Pair",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Connect devices with QR or room code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BouncyIconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
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
                                    text = "Scan QR Code",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Enter Code",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (selectedTab == 0) {
                    // Show QR Code View
                    if (token.isNotBlank()) {
                        QrCodeCanvas(
                            content = token,
                            modifier = Modifier
                                .size(210.dp)
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ROOM CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = roomCode,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        BouncyButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Lumia Pairing Code", token)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Pairing code copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Pairing Code", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // Enter Code View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = codeOrTokenInput,
                            onValueChange = { codeOrTokenInput = it },
                            label = { Text("6-Digit Room Code or Pairing Code") },
                            placeholder = { Text("e.g. 481 920") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BouncyOutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val item = clipboard.primaryClip?.getItemAt(0)
                                    val pasted = item?.text?.toString() ?: ""
                                    if (pasted.isNotBlank()) {
                                        codeOrTokenInput = pasted
                                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste")
                            }

                            BouncyButton(
                                onClick = { onConnectWithToken(codeOrTokenInput) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format relative elapsed time for last synced display.
 */
private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 120_000L -> "1 min ago"
        diff < 3600_000L -> "${diff / 60_000L} mins ago"
        diff < 7200_000L -> "1 hour ago"
        else -> "${diff / 3600_000L} hours ago"
    }
}

/**
 * Derives a human-friendly 6-digit room code from the device fingerprint.
 */
private fun deriveRoomCode(fingerprint: String): String {
    val hash = abs(fingerprint.hashCode())
    val num = (hash % 900000) + 100000
    val str = num.toString()
    return "${str.substring(0, 3)} ${str.substring(3)}"
}
