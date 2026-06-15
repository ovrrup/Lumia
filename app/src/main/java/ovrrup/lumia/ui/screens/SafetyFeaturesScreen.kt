package ovrrup.lumia.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewQuilt
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.components.BouncyButton
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context)) }

    val lifecycleOwnerForPermissions = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwnerForPermissions) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context)
            }
        }
        lifecycleOwnerForPermissions.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwnerForPermissions.lifecycle.removeObserver(observer)
        }
    }

    val safetyPinEnabled by viewModel.safetyPinEnabled.collectAsStateWithLifecycle()
    val safetyPinConflictWarning by viewModel.safetyPinConflictWarning.collectAsStateWithLifecycle()
    val safetyPinRecommendations by viewModel.safetyPinRecommendations.collectAsStateWithLifecycle()

    val aodTrueBlackOled by viewModel.aodTrueBlackOled.collectAsStateWithLifecycle()
    val aodAutoDeactivateTrueBlack by viewModel.aodAutoDeactivateTrueBlack.collectAsStateWithLifecycle()
    val aodBurnInShiftSpeed by viewModel.aodBurnInShiftSpeed.collectAsStateWithLifecycle()

    val aodTrueAodEnabled by viewModel.aodTrueAodEnabled.collectAsStateWithLifecycle()
    val aodTrueAodMode by viewModel.aodTrueAodMode.collectAsStateWithLifecycle()
    val aodSensitivity by viewModel.aodSensitivity.collectAsStateWithLifecycle()
    val aodDimnessLevel by viewModel.aodDimnessLevel.collectAsStateWithLifecycle()
    val aodLockTimeout by viewModel.aodLockTimeout.collectAsStateWithLifecycle()
    val isSentinelActive by viewModel.isSentinelActive.collectAsStateWithLifecycle()

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (betaEnhancedHeader || isGlass) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Safety Guard", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Safety Guard Monitor", icon = Icons.Rounded.Lock) {
                SettingsPremiumToggleItem(
                    title = "System Safety Watch",
                    subtitle = "Monitor settings state conflicts and offer smart ecosystem guidelines",
                    checked = safetyPinEnabled,
                    icon = Icons.Rounded.Lock,
                    onCheckedChange = { viewModel.updateSafetyPinEnabled(it) }
                )

                AnimatedVisibility(visible = safetyPinEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsPremiumToggleItem(
                            title = "Active Conflict Warnings",
                            subtitle = "Immediate warning banner if toggles physically oppose each other structurally",
                            checked = safetyPinConflictWarning,
                            onCheckedChange = { viewModel.updateSafetyPinConflictWarning(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsPremiumToggleItem(
                            title = "Aesthetic Recommendations",
                            subtitle = "Suggest complementary layout features whenever core styles change",
                            checked = safetyPinRecommendations,
                            onCheckedChange = { viewModel.updateSafetyPinRecommendations(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "Focus & AOD Safety Rules", icon = Icons.Rounded.Timer) {
                SettingsPremiumToggleItem(
                    title = "True Black OLED Focus",
                    subtitle = "AOD focus screen will use solid #000000 pixels to conserve battery power on OLED hardware",
                    checked = aodTrueBlackOled,
                    icon = Icons.Rounded.Timer,
                    onCheckedChange = { viewModel.updateAodTrueBlackOled(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsPremiumToggleItem(
                    title = "Auto-Deactivate with Bright Themes",
                    subtitle = "Automatically replace True Black with a beautifully dimmed themed focus screen when using Light theme, dynamic layouts, or Glass UI",
                    checked = aodAutoDeactivateTrueBlack,
                    onCheckedChange = { viewModel.updateAodAutoDeactivateTrueBlack(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                if (!isSentinelActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Extension,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lumia Sentinel Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Advanced screen-off Always-On display optimizations require the Lumia Sentinel dynamic micro-APK package.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BouncyButton(
                            onClick = { navController.navigate("settings/plugins") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Sentinel Plugin", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Pixel Burn-In Shift Interval",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Period taken before slightly shifting always-on focus layout items slightly to protect modern screen pixels from permanent burn-in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf(5, 10, 20, 30)) { seconds ->
                                FilterChip(
                                    selected = aodBurnInShiftSpeed == seconds,
                                    onClick = { viewModel.updateAodBurnInShiftSpeed(seconds) },
                                    label = { Text("$seconds sec") }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    val aodLockScreenSupport by viewModel.aodLockScreenSupport.collectAsStateWithLifecycle()
                    SettingsPremiumToggleItem(
                        title = "Lock Screen Protection",
                        subtitle = "Allows AOD to safely bypass system lock screen without permanently turning screen on, ideal for extended focus",
                        checked = aodLockScreenSupport,
                        icon = Icons.Rounded.Lock,
                        onCheckedChange = { viewModel.updateAodLockScreenSupport(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    SettingsPremiumToggleItem(
                        title = "True Always-On Display",
                        subtitle = "Draw a full-screen SYSTEM OVERLAY clock directly over lockscreens, launchers and other apps for ultimate aesthetic hardware preservation.",
                        checked = aodTrueAodEnabled,
                        icon = Icons.Rounded.CropFree,
                        onCheckedChange = { viewModel.updateAodTrueAodEnabled(it) }
                    )

                    AnimatedVisibility(visible = aodTrueAodEnabled) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "True AOD Integration Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Mode 1: System Overlay
                                val isOverlaySelected = aodTrueAodMode == "overlay"
                                Card(
                                    onClick = { viewModel.updateAodTrueAodMode("overlay") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isOverlaySelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isOverlaySelected) 2.dp else 1.dp,
                                        color = if (isOverlaySelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isOverlaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ViewQuilt,
                                                contentDescription = null,
                                                tint = if (isOverlaySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("System Overlay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text(
                                                "Ideal for physical battery optimization and fast system responses.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Mode 2: Accessibility Overlay
                                val isAccessSelected = aodTrueAodMode == "accessibility"
                                Card(
                                    onClick = { viewModel.updateAodTrueAodMode("accessibility") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAccessSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isAccessSelected) 2.dp else 1.dp,
                                        color = if (isAccessSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isAccessSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Accessibility,
                                                contentDescription = null,
                                                tint = if (isAccessSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Accessibility Overlay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text(
                                                "Safely render screen-saver behind system notification and secure lock controls.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Check permissions
                            val currentPermissionGranted = if (aodTrueAodMode == "overlay") hasOverlayPermission else hasAccessibilityPermission
                            if (!currentPermissionGranted) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (aodTrueAodMode == "overlay") "Overlay Authorization Required" else "Accessibility Authorization Required",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (aodTrueAodMode == "overlay") {
                                                "Enable 'Draw over other apps' to allow Lumia to overlay a pure black fullscreen layout matching real OLED clocks."
                                            } else {
                                                "Enable Lumia's Accessibility Service to render the AOD safely behind system lock panels."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                if (aodTrueAodMode == "overlay") {
                                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                        data = Uri.parse("package:${context.packageName}")
                                                    }
                                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                                } else {
                                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Grant Authorization", color = androidx.compose.ui.graphics.Color.White)
                                        }
                                    }
                                }
                            }

                            // If accessibility is selected & active, show secure lock option
                            if (aodTrueAodMode == "accessibility" && hasAccessibilityPermission) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                
                                val isSecureLockEnabled = aodLockTimeout > 0
                                SettingsPremiumToggleItem(
                                    title = "Secure Lock Fallback",
                                    subtitle = "Increase security and protect your physical screen: programmatically locks the system after AOD commences.",
                                    checked = isSecureLockEnabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateAodLockTimeout(if (isChecked) 30 else 0)
                                    }
                                )

                                if (isSecureLockEnabled) {
                                    Text(
                                        text = "Screen Lock Timeout Timer",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(5, 15, 30, 60, 120).forEach { seconds ->
                                            val isSelected = aodLockTimeout == seconds
                                            Card(
                                                onClick = { viewModel.updateAodLockTimeout(seconds) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${seconds}s",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "⚠️ FORMAL COMPLIANCE WARNINGS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "• PRIVACY NOTATION: Accessibility services are strictly utilized locally to issue standard lock actions. Lumia guarantees 100% data offline processing and never logs user touch inputs or credentials.\n" +
                                                       "• BIOMETRIC COOLDOWN: Android OS rules state programmatic accessibility locking might sometimes prompt lock PIN fallback during your device's next unlock instead of fingerprint recognition.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                            // AOD Wakeup / Deactivation Sensitivity
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "AOD Wake-Up Sensitivity",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Control physical contact and motion thresholds required to wake up from Always-On Display.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                val sensitivityOptions = listOf(
                                    Pair("motion", "Max (Motion & Tap)"),
                                    Pair("highest", "High (Single Tap)"),
                                    Pair("medium", "Mid (Double Tap)"),
                                    Pair("secure", "Hold (Secure)")
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    sensitivityOptions.forEach { (sens, label) ->
                                        val isSelected = aodSensitivity == sens
                                        Card(
                                            onClick = { viewModel.updateAodSensitivity(sens) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label.split(" ").firstOrNull() ?: label,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Speed,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = when (aodSensitivity) {
                                                "motion" -> "Maximum: Accelerometer movement or single screen tap instantly wakes up Lumia."
                                                "highest" -> "High sensitivity: Any single tap anywhere on the dark screen will instantly wake it up."
                                                "medium" -> "Balanced sensitivity: Restricts wake-up to intentional double-taps to fully avoid pocket triggers."
                                                "secure" -> "Fortified safety: Requires holding touch down anywhere for 1 second to unlock back."
                                                else -> "Standard touch gesture control."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                if (aodSensitivity == "motion") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val aodMotionSensitivity by viewModel.aodMotionSensitivity.collectAsStateWithLifecycle()
                                    Text(
                                        text = "Motion Sensor Sensitivity",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Adjust the required movement threshold. Turn off to only use single-tap instead of accelerometer tracking.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val motionOptions = listOf(
                                        Pair(0.5f, "Very High"),
                                        Pair(1.2f, "High"),
                                        Pair(2.5f, "Medium"),
                                        Pair(4.0f, "Low"),
                                        Pair(0f, "Off (Tap only)")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        motionOptions.forEach { (threshold, label) ->
                                            val isSelected = aodMotionSensitivity == threshold
                                            Card(
                                                onClick = { viewModel.updateAodMotionSensitivity(threshold) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label.split(" ").firstOrNull() ?: label,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                            // Controllable dimness override level
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Screen Dimness Override Level",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Controls absolute pixel dimness. High darkness values scale back hardware output to support OLED and eye comfort.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                val dimnessLevels = listOf(
                                    Pair(0.50f, "50%"),
                                    Pair(0.75f, "75%"),
                                    Pair(0.90f, "90%"),
                                    Pair(0.95f, "95%"),
                                    Pair(0.99f, "99%")
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    dimnessLevels.forEach { (level, percentString) ->
                                        val isSelected = aodDimnessLevel == level
                                        Card(
                                            onClick = { viewModel.updateAodDimnessLevel(level) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = percentString,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                            else MaterialTheme.colorScheme.onSurfaceVariant
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
        }
    }
}
