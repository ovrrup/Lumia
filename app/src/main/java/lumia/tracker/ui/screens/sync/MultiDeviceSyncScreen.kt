package lumia.tracker.ui.screens.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.sync.SyncManager
import lumia.tracker.sync.model.SyncDevice
import lumia.tracker.sync.model.SyncMode
import lumia.tracker.sync.model.SyncState
import lumia.tracker.sync.model.TrustedPeer
import lumia.tracker.sync.qr.QrCodeCanvas
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.sync.components.SyncProgressDialog
import lumia.tracker.ui.screens.sync.components.SyncRadarView
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * MultiDeviceSyncScreen - P2P Multi-Device Sync Hub supporting:
 * 1. Permanent 1-Time Mutual Handshake (PSK storage).
 * 2. All-Time Continuous Background Auto-Sync on local Wi-Fi / Hotspot.
 * 3. 1-Click Instant Sync with trusted peers without repeating PINs.
 * 4. Zero-Trust AES-256-GCM encryption & HMAC verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDeviceSyncScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val context = LocalContext.current
    val syncManager = remember { SyncManager(context.applicationContext) }

    DisposableEffect(Unit) {
        syncManager.startHosting()
        onDispose {
            syncManager.cleanup()
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Trusted & Radar, 1: QR & PIN, 2: History
    var showPinDialogForPeer by remember { mutableStateOf<SyncDevice?>(null) }
    var showDirectConnectDialog by remember { mutableStateOf(false) }
    var showTokenInputDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var peerToUnpair by remember { mutableStateOf<TrustedPeer?>(null) }

    val isScanning = syncState is SyncState.Discovering

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Multi-Device Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Persistent P2P 1-Time Handshake Protocol",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Rounded.HelpOutline, contentDescription = "How it works")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Local Device Status Header
            LocalDeviceHeader(
                syncManager = syncManager,
                isServerRunning = isServerRunning
            )

            // Navigation Tab Bar
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Peers & Radar") },
                    icon = { Icon(Icons.Rounded.Devices, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pair New (QR/PIN)") },
                    icon = { Icon(Icons.Rounded.QrCode, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("History (${syncHistory.size})") },
                    icon = { Icon(Icons.Rounded.History, contentDescription = null) }
                )
            }

            // Tab View Area
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> TrustedAndRadarTab(
                        isScanning = isScanning,
                        discoveredPeers = discoveredPeers,
                        trustedPeers = trustedPeers,
                        continuousAutoSync = continuousAutoSync,
                        onToggleContinuousAutoSync = { syncManager.setContinuousAutoSyncEnabled(it) },
                        onToggleScan = {
                            if (isScanning) syncManager.stopDiscovery() else syncManager.startDiscovery()
                        },
                        onSyncTrustedPeer = { peer ->
                            val matchingDiscovered = discoveredPeers.find { it.id == peer.deviceId }
                            if (matchingDiscovered != null) {
                                syncManager.connectToTrustedPeer(matchingDiscovered, peer)
                            } else {
                                Toast.makeText(context, "${peer.deviceName} is not currently detected on this Wi-Fi network.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTogglePeerAutoSync = { peerId, enabled ->
                            syncManager.toggleTrustedPeerAutoSync(peerId, enabled)
                        },
                        onRequestUnpair = { peer -> peerToUnpair = peer },
                        onConnectDiscoveredPeer = { peer ->
                            if (syncManager.isPeerTrusted(peer.id)) {
                                syncManager.connectToTrustedPeer(peer)
                            } else {
                                showPinDialogForPeer = peer
                            }
                        },
                        onDirectConnect = { showDirectConnectDialog = true },
                        onPasteToken = { showTokenInputDialog = true }
                    )
                    1 -> QrAndPinTab(
                        pairingPin = pairingPin,
                        pairingToken = pairingToken,
                        onRegeneratePin = { syncManager.regeneratePin() },
                        onShareToken = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Lumia Sync Token", pairingToken))
                            Toast.makeText(context, "Pairing token copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onPasteToken = { showTokenInputDialog = true }
                    )
                    2 -> SyncHistoryTab(history = syncHistory)
                }
            }
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
            title = { Text("Unpair ${peer.deviceName}?") },
            text = {
                Text("Removing this device will revoke automatic synchronization. You will need to perform a new 1-time handshake to sync in the future.")
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

    // Peer PIN & Sync Mode Selection Dialog (For Initial 1-Time Handshake)
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

    // Direct IP Connect Dialog
    if (showDirectConnectDialog) {
        DirectIpConnectDialog(
            onDismiss = { showDirectConnectDialog = false },
            onConnect = { ip, port, pin, mode ->
                showDirectConnectDialog = false
                val peer = SyncDevice(id = "manual_$ip", name = "Manual Peer ($ip)", ipAddress = ip, port = port)
                syncManager.connectToPeer(peer, pin, mode)
            }
        )
    }

    // Token / QR Paste Dialog
    if (showTokenInputDialog) {
        TokenInputDialog(
            onDismiss = { showTokenInputDialog = false },
            onConnect = { token, mode ->
                showTokenInputDialog = false
                syncManager.pairWithToken(token, mode)
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        SyncHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
private fun LocalDeviceHeader(
    syncManager: SyncManager,
    isServerRunning: Boolean
) {
    val localDevice = remember(isServerRunning) { syncManager.getLocalDevice() }

    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localDevice.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isServerRunning) Color(0xFF4CAF50) else Color(0xFFFFA000), CircleShape)
                    )
                    Text(
                        text = if (isServerRunning) "P2P Server Active (Local Wi-Fi)" else "Initializing Server...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "${localDevice.ipAddress}:${localDevice.port}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrustedAndRadarTab(
    isScanning: Boolean,
    discoveredPeers: List<SyncDevice>,
    trustedPeers: List<TrustedPeer>,
    continuousAutoSync: Boolean,
    onToggleContinuousAutoSync: (Boolean) -> Unit,
    onToggleScan: () -> Unit,
    onSyncTrustedPeer: (TrustedPeer) -> Unit,
    onTogglePeerAutoSync: (String, Boolean) -> Unit,
    onRequestUnpair: (TrustedPeer) -> Unit,
    onConnectDiscoveredPeer: (SyncDevice) -> Unit,
    onDirectConnect: () -> Unit,
    onPasteToken: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Continuous Auto-Sync Master Toggle
        item {
            ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Continuous Auto-Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "All-Time Background Synchronization",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Switch(
                            checked = continuousAutoSync,
                            onCheckedChange = onToggleContinuousAutoSync
                        )
                    }

                    Text(
                        text = "Once paired via 1-time handshake, Lumia will continuously and silently sync your courses, tasks, notes, and focus sessions whenever paired devices are on the same Wi-Fi, hotspot, or local network without requiring any PIN entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Trusted Paired Devices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PAIRED TRUSTED DEVICES (${trustedPeers.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (trustedPeers.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DevicesFold,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No paired devices yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Pair another phone or tablet once using QR Code or PIN from the 'Pair New' tab. After pairing, synchronization is 100% automatic forever.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(trustedPeers) { peer ->
                val isOnline = discoveredPeers.any { it.id == peer.deviceId }
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TabletAndroid,
                                        contentDescription = null,
                                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(peer.deviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (isOnline) "Online" else "Offline",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (peer.lastSyncAt > 0) "Last synced ${formatRelativeTime(peer.lastSyncAt)}" else "Never synced yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 1-Click Instant Sync Button
                            BouncyButton(
                                onClick = { onSyncTrustedPeer(peer) },
                                enabled = isOnline
                            ) {
                                Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sync Now")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Controls Row: Auto-Sync Toggle & Unpair Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Switch(
                                    checked = peer.autoSyncEnabled,
                                    onCheckedChange = { onTogglePeerAutoSync(peer.deviceId, it) },
                                    modifier = Modifier.size(width = 44.dp, height = 24.dp)
                                )
                                Text("Auto-Sync on Connect", style = MaterialTheme.typography.labelMedium)
                            }

                            BouncyTextButton(
                                onClick = { onRequestUnpair(peer) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Rounded.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Unpair", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // 3. Radar Discovery Card
        item {
            ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                                imageVector = if (isScanning) Icons.Rounded.Search else Icons.Rounded.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (isScanning) "Scanning local Wi-Fi / Hotspot..." else "Radar Discovery Ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isScanning) "Searching for Lumia peers broadcasting on the network" else "Tap below to scan for nearby devices automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BouncyButton(
                            onClick = onToggleScan,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(if (isScanning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isScanning) "Stop Radar" else "Start Radar Scan")
                        }
                    }
                }
            }
        }

        // 4. Quick Manual Actions
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDirectConnect() },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Cable, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Direct IP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPasteToken() },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Paste Token", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Discovered Devices Heading
        item {
            Text(
                text = "NEARBY DISCOVERED DEVICES (${discoveredPeers.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (discoveredPeers.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.DevicesOther, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No nearby devices detected", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Ensure both devices are connected to the same Wi-Fi network or mobile hotspot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(discoveredPeers) { peer ->
                val isTrusted = trustedPeers.any { it.deviceId == peer.id }
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(peer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (isTrusted) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(
                                            "Trusted",
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
                        BouncyButton(onClick = { onConnectDiscoveredPeer(peer) }) {
                            Text(if (isTrusted) "1-Click Sync" else "Pair & Sync")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrAndPinTab(
    pairingPin: String,
    pairingToken: String,
    onRegeneratePin: () -> Unit,
    onShareToken: () -> Unit,
    onPasteToken: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // QR Code Container
        ScholarCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("1-Time Handshake QR Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Scan this QR code once from another device to establish permanent mutual trust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.size(220.dp)) {
                    QrCodeCanvas(
                        content = pairingToken,
                        darkColor = MaterialTheme.colorScheme.primary,
                        lightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Security PIN Card
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
                            text = "${pairingPin.take(3)} ${pairingPin.takeLast(3)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BouncyTextButton(onClick = onRegeneratePin) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Regenerate PIN")
                            }
                            BouncyTextButton(onClick = onShareToken) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy Token")
                            }
                        }
                    }
                }
            }
        }

        // Action button to enter remote token
        BouncyButton(
            onClick = onPasteToken,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Key, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Enter Peer Token / Scan Code")
        }
    }
}

@Composable
private fun SyncHistoryTab(history: List<lumia.tracker.sync.model.SyncHistoryRecord>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.HistoryToggleOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                Text("No sync history recorded yet", fontWeight = FontWeight.Bold)
                Text("Your completed multi-device synchronization sessions will appear here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(history) { record ->
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.peerDeviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(record.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = formatTimestamp(record.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

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
                    "Enter the 6-digit security PIN displayed on ${peer.name}'s screen. Once paired, you will never need to enter a PIN again for this device:",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("6-Digit Security PIN") },
                    placeholder = { Text("e.g. 482910") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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

@Composable
private fun DirectIpConnectDialog(
    onDismiss: () -> Unit,
    onConnect: (String, Int, String, SyncMode) -> Unit
) {
    var ip by remember { mutableStateOf("") }
    var portString by remember { mutableStateOf("52934") }
    var pin by remember { mutableStateOf("") }
    var syncMode by remember { mutableStateOf(SyncMode.SMART_MERGE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Direct IP Connection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("Peer IP Address") },
                    placeholder = { Text("e.g. 192.168.1.50") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = portString,
                    onValueChange = { portString = it },
                    label = { Text("Port") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Pairing PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    val port = portString.toIntOrNull() ?: 52934
                    if (ip.isNotBlank() && pin.isNotBlank()) onConnect(ip, port, pin, syncMode)
                },
                enabled = ip.isNotBlank() && pin.isNotBlank()
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TokenInputDialog(
    onDismiss: () -> Unit,
    onConnect: (String, SyncMode) -> Unit
) {
    var rawToken by remember { mutableStateOf("") }
    var syncMode by remember { mutableStateOf(SyncMode.SMART_MERGE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pair with Token") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Paste the Base64 pairing token or QR payload string from the peer device:")
                OutlinedTextField(
                    value = rawToken,
                    onValueChange = { rawToken = it },
                    label = { Text("Pairing Token") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = { if (rawToken.isNotBlank()) onConnect(rawToken, syncMode) },
                enabled = rawToken.isNotBlank()
            ) {
                Text("Authenticate & Sync")
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SyncHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Multi-Device Sync") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Lumia Multi-Device Sync allows you to synchronize your courses, tasks, subjects, notes, and pomodoros directly between phones and tablets without sending any data to third-party cloud servers.",
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
