package lumia.tracker.ui.screens.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import lumia.tracker.sync.SyncManager
import lumia.tracker.sync.model.SyncDevice
import lumia.tracker.sync.model.SyncHistoryRecord
import lumia.tracker.sync.model.SyncMode
import lumia.tracker.sync.model.SyncState
import lumia.tracker.sync.model.TrustedPeer
import lumia.tracker.sync.qr.QrCodeCanvas
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.sync.components.SyncProgressDialog
import lumia.tracker.ui.screens.sync.components.SyncRadarView
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * MultiDeviceSyncScreen - P2P Multi-Device Sync Cockpit supporting:
 * 1. Permanent 1-Time Mutual Handshake (PSK storage).
 * 2. All-Time Continuous Background Auto-Sync on local Wi-Fi / Hotspot.
 * 3. 1-Click Instant Sync with trusted peers without repeating PINs.
 * 4. Zero-Trust AES-256-GCM encryption & HMAC verification.
 * 5. Real-time Outbox buffer tracking & telemetry telemetry.
 */
@ValueScore(
    score = 95,
    importance = Importance.HIGH,
    description = "One-time pair live mesh multi-device synchronization cockpit with telemetry and frictionless pairing",
    category = "Sync"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDeviceSyncScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val context = LocalContext.current
    val syncManager = remember { SyncManager.getInstance(context.applicationContext) }
    val prefs = remember { context.getSharedPreferences("lumia_sync_prefs", Context.MODE_PRIVATE) }

    DisposableEffect(Unit) {
        syncManager.startHosting()
        if (syncManager.continuousAutoSyncEnabled.value) {
            syncManager.startDiscovery()
        }
        onDispose {
            if (!syncManager.continuousAutoSyncEnabled.value) {
                syncManager.stopDiscovery()
            }
        }
    }

    val discoveredPeers by syncManager.discoveredPeers.collectAsStateWithLifecycle()
    val trustedPeers by syncManager.trustedPeers.collectAsStateWithLifecycle()
    val continuousAutoSync by syncManager.continuousAutoSyncEnabled.collectAsStateWithLifecycle()
    val syncState by syncManager.syncState.collectAsStateWithLifecycle()
    val pairingPin by syncManager.pairingPin.collectAsStateWithLifecycle()
    val pairingToken by syncManager.pairingToken.collectAsStateWithLifecycle()
    val syncHistory by syncManager.syncHistory.collectAsStateWithLifecycle()
    val isServerRunning by syncManager.isServerRunning.collectAsStateWithLifecycle()
    val isSyncing by syncManager.isSyncing.collectAsStateWithLifecycle()
    val pendingOutboxCount by syncManager.pendingOutboxCount.collectAsStateWithLifecycle()

    var wifiOnlySync by remember { mutableStateOf(prefs.getBoolean("wifi_only_sync", true)) }
    var showPairNewSheet by remember { mutableStateOf(false) }
    var showPinDialogForPeer by remember { mutableStateOf<SyncDevice?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var peerToUnpair by remember { mutableStateOf<TrustedPeer?>(null) }

    val isScanning = syncState is SyncState.Discovering
    val onlineTrustedCount = trustedPeers.count { peer -> discoveredPeers.any { it.id == peer.deviceId } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Device Sync",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    BouncyIconButton(onClick = {
                        if (isScanning) syncManager.stopDiscovery() else syncManager.startDiscovery()
                    }) {
                        Icon(
                            if (isScanning) Icons.Rounded.Sensors else Icons.Rounded.SensorsOff,
                            contentDescription = "Toggle Radar",
                            tint = if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BouncyIconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            Icons.Rounded.HelpOutline,
                            contentDescription = "Help & Architecture",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPairNewSheet = true },
                icon = { Icon(Icons.Rounded.AddLink, contentDescription = null) },
                text = { Text("Pair New Device", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Sync Status Pill & Cockpit Hero
            item {
                MeshCockpitHeader(
                    onlineCount = onlineTrustedCount,
                    totalPaired = trustedPeers.size,
                    isScanning = isScanning,
                    isServerRunning = isServerRunning,
                    pendingOutboxCount = pendingOutboxCount,
                    continuousAutoSync = continuousAutoSync,
                    isSyncing = isSyncing,
                    syncManager = syncManager,
                    onOpenPairSheet = { showPairNewSheet = true }
                )
            }

            // 2. Paired Devices Fleet Card Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PAIRED FLEET (${trustedPeers.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                    if (trustedPeers.isNotEmpty() && onlineTrustedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Text(
                                    text = "$onlineTrustedCount Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            if (trustedPeers.isEmpty()) {
                item {
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Devices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                "No Paired Devices",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pair with a phone or tablet using QR Code or 6-digit PIN for automatic, silent local Wi-Fi sync.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            BouncyButton(onClick = { showPairNewSheet = true }) {
                                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pair Your First Device")
                            }
                        }
                    }
                }
            } else {
                items(trustedPeers, key = { it.deviceId }) { peer ->
                    val isOnline = discoveredPeers.any { it.id == peer.deviceId }
                    val matchingDiscovered = discoveredPeers.find { it.id == peer.deviceId }
                    PairedDeviceFleetCard(
                        peer = peer,
                        isOnline = isOnline,
                        onSyncNow = {
                            if (matchingDiscovered != null) {
                                syncManager.connectToTrustedPeer(matchingDiscovered, peer)
                                Toast.makeText(context, "Initiated sync with ${peer.deviceName}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "${peer.deviceName} is offline or not detected on local Wi-Fi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onToggleAutoSync = { enabled ->
                            syncManager.toggleTrustedPeerAutoSync(peer.deviceId, enabled)
                        },
                        onRemove = { peerToUnpair = peer }
                    )
                }
            }

            // 3. Live Radar & Discovered Devices Card
            item {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SyncRadarView(
                            isScanning = isScanning,
                            primaryColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isScanning) Icons.Rounded.Sensors else Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isScanning) "Scanning local Wi-Fi & Hotspot..." else "Radar Discovery Ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isScanning) "Listening for nearby Lumia mesh peers broadcasting on the network" else "Start the radar scan to discover nearby unpaired Lumia devices",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BouncyButton(
                                onClick = {
                                    if (isScanning) syncManager.stopDiscovery() else syncManager.startDiscovery()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    if (isScanning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (isScanning) "Stop Radar" else "Start Radar Scan")
                            }
                        }
                    }
                }
            }

            // 4. Discovered Nearby Devices Section
            if (discoveredPeers.isNotEmpty()) {
                item {
                    Text(
                        text = "NEARBY DETECTED DEVICES (${discoveredPeers.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                }

                items(discoveredPeers, key = { it.id }) { peer ->
                    val isTrusted = syncManager.isPeerTrusted(peer.id)
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getDeviceIcon(peer.name),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(peer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    if (isTrusted) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                "Paired",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "${peer.ipAddress}:${peer.port}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BouncyButton(
                                onClick = {
                                    if (isTrusted) {
                                        syncManager.connectToTrustedPeer(peer)
                                    } else {
                                        showPinDialogForPeer = peer
                                    }
                                }
                            ) {
                                Text(if (isTrusted) "1-Click Sync" else "Pair & Sync")
                            }
                        }
                    }
                }
            }

            // 5. Settings / Preferences Section
            item {
                Text(
                    text = "MESH PREFERENCES & CONTROLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.1.sp
                )
            }

            item {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Background Auto-Sync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Sync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text("Background Auto-Sync", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Continuously syncs in background when paired devices connect to local Wi-Fi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = continuousAutoSync,
                                onCheckedChange = { syncManager.setContinuousAutoSyncEnabled(it) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Wi-Fi Only Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Wifi,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text("Wi-Fi Only Sync", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Restrict P2P sync traffic to unmetered local Wi-Fi or hotspot connections.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = wifiOnlySync,
                                onCheckedChange = {
                                    wifiOnlySync = it
                                    prefs.edit().putBoolean("wifi_only_sync", it).apply()
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Force Full Sync Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text("Force Full Mesh Sync", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Trigger an immediate bi-directional smart-merge with all active online mesh peers.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            BouncyOutlinedButton(
                                onClick = {
                                    syncManager.triggerAutoSyncToAllTrustedPeers()
                                    Toast.makeText(context, "Triggered full sync with all online peers", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Rounded.SyncLock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sync All")
                            }
                        }
                    }
                }
            }

            // 6. Sync History Log
            if (syncHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "FLEET SYNC ACTIVITY (${syncHistory.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp
                    )
                }

                items(syncHistory.take(5), key = { it.id }) { record ->
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (record.isSuccess) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (record.isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = if (record.isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.peerDeviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(record.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = formatRelativeTime(record.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for "Pair New Device"
    if (showPairNewSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPairNewSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            PairNewDeviceBottomSheetContent(
                pairingPin = pairingPin,
                pairingToken = pairingToken,
                discoveredPeers = discoveredPeers,
                onRegeneratePin = { syncManager.regeneratePin() },
                onShareToken = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Lumia Sync Token", pairingToken))
                    Toast.makeText(context, "Pairing token copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                onCopyPin = { pinToCopy ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Lumia Pairing PIN", pinToCopy))
                    Toast.makeText(context, "Pairing PIN ($pinToCopy) copied!", Toast.LENGTH_SHORT).show()
                },
                onPairDiscoveredPeer = { peer, pin, mode ->
                    showPairNewSheet = false
                    syncManager.connectToPeer(peer, pin, mode)
                },
                onPairToken = { token, mode ->
                    showPairNewSheet = false
                    syncManager.pairWithToken(token, mode)
                },
                onDirectConnect = { ip, port, pin, mode ->
                    showPairNewSheet = false
                    val peer = SyncDevice(id = "manual_$ip", name = "Manual Peer ($ip)", ipAddress = ip, port = port)
                    syncManager.connectToPeer(peer, pin, mode)
                }
            )
        }
    }

    // Active Sync Progress Modal
    SyncProgressDialog(
        syncState = syncState,
        onDismiss = { syncManager.resetSyncState() }
    )

    // Unpair Confirmation Dialog
    peerToUnpair?.let { peer ->
        AlertDialog(
            onDismissRequest = { peerToUnpair = null },
            icon = { Icon(Icons.Rounded.LinkOff, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Unpair ${peer.deviceName}?") },
            text = {
                Text("Removing this device will revoke continuous synchronization and delete the stored Pre-Shared Key (PSK). You will need to perform a new 1-time handshake to sync again in the future.")
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        syncManager.removeTrustedPeer(peer.deviceId)
                        peerToUnpair = null
                        Toast.makeText(context, "Device unpaired successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Unpair Device")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { peerToUnpair = null }) { Text("Cancel") }
            }
        )
    }

    // Peer PIN Auth Dialog
    showPinDialogForPeer?.let { peer ->
        PeerPinAuthDialog(
            peer = peer,
            onDismiss = { showPinDialogForPeer = null },
            onConnect = { pin, mode ->
                showPinDialogForPeer = null
                syncManager.connectToPeer(peer, pin, mode)
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        SyncHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

/**
 * Cockpit Header with Animated Live Beacon, Host Details, Connection Telemetry, and Outbox Status.
 */
@ValueScore(
    score = 95,
    importance = Importance.HIGH,
    description = "Animated live beacon radar header displaying connected mesh fleet status, outbox telemetry, and host daemon endpoint",
    category = "UI"
)
@Composable
private fun MeshCockpitHeader(
    onlineCount: Int,
    totalPaired: Int,
    isScanning: Boolean,
    isServerRunning: Boolean,
    pendingOutboxCount: Int,
    continuousAutoSync: Boolean,
    isSyncing: Boolean,
    syncManager: SyncManager,
    onOpenPairSheet: () -> Unit
) {
    val context = LocalContext.current
    val localDevice = remember(isServerRunning) { syncManager.getLocalDevice() }

    val infiniteTransition = rememberInfiniteTransition(label = "BeaconAura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraScale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraAlpha"
    )

    val beaconColor = when {
        onlineCount > 0 -> Color(0xFF4CAF50)
        isScanning -> Color(0xFF00B0FF)
        isServerRunning -> Color(0xFFFFA000)
        else -> Color(0xFF9E9E9E)
    }

    val statusText = when {
        onlineCount > 0 -> "Online Mesh Active • $onlineCount Connected"
        isScanning -> "Radar Searching • Scanning Mesh"
        isServerRunning -> "Local Wi-Fi Ready • Standby"
        else -> "Mesh Standby • 0 Online"
    }

    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(beaconColor.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                        if (isServerRunning || isScanning) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp * auraScale)
                                    .background(beaconColor.copy(alpha = auraAlpha), CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(beaconColor, CircleShape)
                        )
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = beaconColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "Zero-Cloud",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Cockpit Telemetry Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Outbox status chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (pendingOutboxCount == 0) Icons.Rounded.DoneAll else Icons.Rounded.ScheduleSend,
                            contentDescription = null,
                            tint = if (pendingOutboxCount == 0) Color(0xFF4CAF50) else Color(0xFFFFA000),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "OUTBOX",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (pendingOutboxCount == 0) "All Synced" else "$pendingOutboxCount Queued",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Auto-Sync Status chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (continuousAutoSync) Icons.Rounded.Sync else Icons.Rounded.SyncDisabled,
                            contentDescription = null,
                            tint = if (continuousAutoSync) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "AUTO-SYNC",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (continuousAutoSync) "Continuous" else "Manual",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Local Host Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getDeviceIcon(localDevice.name),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = localDevice.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "This Device",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isServerRunning) "P2P Daemon Active • Port ${localDevice.port}" else "Starting Daemon...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Local Endpoint", "${localDevice.ipAddress}:${localDevice.port}"))
                        Toast.makeText(context, "Host endpoint copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${localDevice.ipAddress}:${localDevice.port}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Endpoint",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions in Cockpit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BouncyButton(
                    onClick = {
                        syncManager.triggerAutoSyncToAllTrustedPeers()
                        Toast.makeText(context, "Synchronizing mesh fleet...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (isSyncing) Icons.Rounded.Autorenew else Icons.Rounded.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isSyncing) "Syncing..." else "Sync Fleet Now")
                }

                BouncyOutlinedButton(
                    onClick = onOpenPairSheet,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pair Device")
                }
            }
        }
    }
}

/**
 * Paired Device Fleet Card Item with Crisp Online/Offline Badges, Relative Timestamps, and Instant Sync.
 */
@ValueScore(
    score = 94,
    importance = Importance.HIGH,
    description = "Fleet device card rendering paired peer status, 1-click instant sync action, relative timestamps, and auto-sync toggles",
    category = "UI"
)
@Composable
private fun PairedDeviceFleetCard(
    peer: TrustedPeer,
    isOnline: Boolean,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getDeviceIcon(peer.deviceName),
                            contentDescription = null,
                            tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(peer.deviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Text(
                                        text = if (isOnline) "Online" else "Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (peer.lastSyncAt > 0) "Last synced ${formatRelativeTime(peer.lastSyncAt)}" else "Never synced yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 1-Click Instant Sync Button
                BouncyButton(
                    onClick = onSyncNow,
                    enabled = isOnline
                ) {
                    Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sync Now")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Switch(
                        checked = peer.autoSyncEnabled,
                        onCheckedChange = onToggleAutoSync,
                        modifier = Modifier.size(width = 44.dp, height = 24.dp)
                    )
                    Text("Auto-Sync on Connect", style = MaterialTheme.typography.labelMedium)
                }

                BouncyTextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Unpair", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Frictionless Modal Bottom Sheet Content for Pairing New Devices with Segmented PIN and High-Contrast QR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairNewDeviceBottomSheetContent(
    pairingPin: String,
    pairingToken: String,
    discoveredPeers: List<SyncDevice>,
    onRegeneratePin: () -> Unit,
    onShareToken: () -> Unit,
    onCopyPin: (String) -> Unit,
    onPairDiscoveredPeer: (SyncDevice, String, SyncMode) -> Unit,
    onPairToken: (String, SyncMode) -> Unit,
    onDirectConnect: (String, Int, String, SyncMode) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) } // 0: My QR & PIN, 1: Enter Peer PIN, 2: Direct IP
    var inputPin by remember { mutableStateOf("") }
    var rawTokenInput by remember { mutableStateOf("") }
    var selectedPeerId by remember { mutableStateOf<String?>(discoveredPeers.firstOrNull()?.id) }
    var manualIp by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("52934") }
    var syncMode by remember { mutableStateOf(SyncMode.SMART_MERGE) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pair New Device",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "1-Time Handshake",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        PrimaryTabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("My QR & PIN") }, icon = { Icon(Icons.Rounded.QrCode, contentDescription = null) })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Enter PIN") }, icon = { Icon(Icons.Rounded.Key, contentDescription = null) })
            Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }, text = { Text("Direct IP") }, icon = { Icon(Icons.Rounded.Cable, contentDescription = null) })
        }

        when (tabIndex) {
            0 -> {
                // Host QR Code & PIN Display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Scan this QR code from Lumia on your second device to establish permanent mutual trust:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // High-Contrast QR Code Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(220.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            QrCodeCanvas(
                                content = pairingToken,
                                darkColor = Color.Black,
                                lightColor = Color.White
                            )
                        }
                    }

                    // PIN Container
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "ONE-TIME PAIRING PIN",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${pairingPin.take(3)}  ${pairingPin.takeLast(3)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BouncyButton(
                                    onClick = { onCopyPin(pairingPin) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy PIN")
                                }
                                Spacer(Modifier.width(8.dp))
                                BouncyOutlinedButton(
                                    onClick = onShareToken,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy Token")
                                }
                                Spacer(Modifier.width(8.dp))
                                BouncyIconButton(onClick = onRegeneratePin) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = "Regenerate PIN")
                                }
                            }
                        }
                    }

                    Text(
                        "Zero-Trust E2EE • Cryptographic PSK stored on-device only",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            1 -> {
                // Client PIN Input for Discovered or Target Peer
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Enter the 6-digit security PIN displayed on the other device's screen:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Frictionless Segmented 6-Digit PIN Box Input
                    PinCodeInputView(
                        pin = inputPin,
                        onPinChange = { inputPin = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (discoveredPeers.isNotEmpty()) {
                        Text("Select Target Discovered Device:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        discoveredPeers.forEach { peer ->
                            val isSelected = selectedPeerId == peer.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPeerId = peer.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = getDeviceIcon(peer.name),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Column {
                                            Text(peer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("${peer.ipAddress}:${peer.port}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Text("Sync Strategy Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = syncMode == SyncMode.SMART_MERGE,
                            onClick = { syncMode = SyncMode.SMART_MERGE },
                            label = { Text("Smart Merge") }
                        )
                        FilterChip(
                            selected = syncMode == SyncMode.PUSH_TO_PEER,
                            onClick = { syncMode = SyncMode.PUSH_TO_PEER },
                            label = { Text("Push to Peer") }
                        )
                        FilterChip(
                            selected = syncMode == SyncMode.PULL_FROM_PEER,
                            onClick = { syncMode = SyncMode.PULL_FROM_PEER },
                            label = { Text("Pull from Peer") }
                        )
                    }

                    BouncyButton(
                        onClick = {
                            val targetPeer = discoveredPeers.find { it.id == selectedPeerId } ?: discoveredPeers.firstOrNull()
                            if (targetPeer != null && inputPin.length >= 4) {
                                onPairDiscoveredPeer(targetPeer, inputPin, syncMode)
                            }
                        },
                        enabled = inputPin.length >= 4 && (selectedPeerId != null || discoveredPeers.isNotEmpty()),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pair & Establish Trust")
                    }
                }
            }
            2 -> {
                // Direct IP & Token Input
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Paste Token String:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rawTokenInput,
                        onValueChange = { rawTokenInput = it },
                        label = { Text("Base64 Pairing Token") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BouncyButton(
                        onClick = { if (rawTokenInput.isNotBlank()) onPairToken(rawTokenInput, syncMode) },
                        enabled = rawTokenInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Authenticate Token")
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Text("Or Direct IP Connect:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = manualIp,
                        onValueChange = { manualIp = it },
                        label = { Text("Peer IP Address") },
                        placeholder = { Text("e.g. 192.168.1.50") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualPort,
                        onValueChange = { manualPort = it },
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Device PIN for Direct IP:")
                    PinCodeInputView(
                        pin = inputPin,
                        onPinChange = { inputPin = it }
                    )

                    BouncyOutlinedButton(
                        onClick = {
                            val port = manualPort.toIntOrNull() ?: 52934
                            if (manualIp.isNotBlank() && inputPin.isNotBlank()) {
                                onDirectConnect(manualIp, port, inputPin, syncMode)
                            }
                        },
                        enabled = manualIp.isNotBlank() && inputPin.length >= 4,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect by IP")
                    }
                }
            }
        }
    }
}

/**
 * Frictionless Segmented 6-Digit PIN Code Input View with auto-focus and tactile box styling.
 */
@Composable
fun PinCodeInputView(
    pin: String,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    pinLength: Int = 6,
    isError: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        contentAlignment = Alignment.Center
    ) {
        // Hidden BasicTextField driving numeric input
        BasicTextField(
            value = pin,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }.take(pinLength)
                onPinChange(filtered)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .size(1.dp)
                .alpha(0f)
                .focusRequester(focusRequester)
        )

        // Visual 6-digit boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until pinLength) {
                val isFocused = pin.length == i || (i == pinLength - 1 && pin.length == pinLength)
                val digit = pin.getOrNull(i)?.toString() ?: ""
                val boxBorderColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused -> MaterialTheme.colorScheme.primary
                    digit.isNotEmpty() -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                }

                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(if (isFocused) 2.dp else 1.dp, boxBorderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = digit,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Peer PIN Auth Dialog with Segmented 6-digit PIN entry.
 */
@Composable
private fun PeerPinAuthDialog(
    peer: SyncDevice,
    onDismiss: () -> Unit,
    onConnect: (String, SyncMode) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var syncMode by remember { mutableStateOf(SyncMode.SMART_MERGE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("1-Time Pair with ${peer.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter the 6-digit security PIN displayed on ${peer.name}'s screen. Once paired, mutual PSKs are stored and future syncs are 100% automatic:",
                    style = MaterialTheme.typography.bodyMedium
                )

                PinCodeInputView(
                    pin = pin,
                    onPinChange = { pin = it }
                )

                Text("Sync Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = syncMode == SyncMode.SMART_MERGE,
                        onClick = { syncMode = SyncMode.SMART_MERGE },
                        label = { Text("Smart Merge") }
                    )
                    FilterChip(
                        selected = syncMode == SyncMode.PUSH_TO_PEER,
                        onClick = { syncMode = SyncMode.PUSH_TO_PEER },
                        label = { Text("Push") }
                    )
                    FilterChip(
                        selected = syncMode == SyncMode.PULL_FROM_PEER,
                        onClick = { syncMode = SyncMode.PULL_FROM_PEER },
                        label = { Text("Pull") }
                    )
                }
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = { if (pin.length >= 4) onConnect(pin, syncMode) },
                enabled = pin.length >= 4
            ) {
                Text("Pair & Start Sync")
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Sync Architecture & Help Dialog.
 */
@Composable
private fun SyncHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Live Mesh Sync") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Lumia Multi-Device Sync allows you to synchronize your courses, tasks, subjects, notes, and pomodoro sessions directly between phones and tablets without sending any data to third-party cloud servers.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Text("1-Time Handshake & Permanent Trust:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Pair two devices once via QR code or 6-digit PIN. Both devices securely derive and store a cryptographic Pre-Shared Key (PSK). After this initial pairing, subsequent syncs occur automatically and silently with zero PIN entry.", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(6.dp))
                Text("End-to-End Encryption:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("All sync payloads are protected with AES-256-GCM encryption with keys derived from zero-trust HMAC challenge-response.", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(6.dp))
                Text("Smart Data Merge:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Combines datasets without deleting existing records. Tasks and assignments maintain their latest completion state.", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(6.dp))
                Text("Local Peer Discovery:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Uses local mDNS / NSD over Wi-Fi, Ethernet, or mobile hotspot for instant peer detection.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            BouncyButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}

private fun getDeviceIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("tab") || lower.contains("pad") -> Icons.Rounded.TabletAndroid
        lower.contains("pc") || lower.contains("mac") || lower.contains("laptop") || lower.contains("desktop") -> Icons.Rounded.Laptop
        else -> Icons.Rounded.PhoneAndroid
    }
}

private fun formatTimestamp(timeMillis: Long): String {
    val date = java.util.Date(timeMillis)
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return sdf.format(date)
}

private fun formatRelativeTime(timeMillis: Long): String {
    val diff = System.currentTimeMillis() - timeMillis
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}