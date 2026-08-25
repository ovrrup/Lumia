package lumia.tracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(AodAccessibilityService.isServiceEnabled(context)) }

    val lifecycleOwnerForPermissions = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwnerForPermissions) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = AodAccessibilityService.isServiceEnabled(context)
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
    val aodLockScreenSupport by viewModel.aodLockScreenSupport.collectAsStateWithLifecycle()

    val aodTrueAodEnabled by viewModel.aodTrueAodEnabled.collectAsStateWithLifecycle()
    val aodTrueAodMode by viewModel.aodTrueAodMode.collectAsStateWithLifecycle()
    val aodSensitivity by viewModel.aodSensitivity.collectAsStateWithLifecycle()
    val aodMotionSensitivity by viewModel.aodMotionSensitivity.collectAsStateWithLifecycle()
    val aodDimnessLevel by viewModel.aodDimnessLevel.collectAsStateWithLifecycle()
    val aodLockTimeout by viewModel.aodLockTimeout.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Safety Guard & AOD",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Safety Guard Monitor
            SettingsGroupCard(title = "Safety Guard Monitor", icon = Icons.Rounded.Shield) {
                SettingsToggleItem(
                    title = "System Safety Watch",
                    subtitle = "Monitor settings state conflicts and offer smart ecosystem guidelines",
                    checked = safetyPinEnabled,
                    icon = Icons.Rounded.Shield,
                    onCheckedChange = { viewModel.updateSafetyPinEnabled(it) }
                )

                AnimatedVisibility(
                    visible = safetyPinEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        SettingsToggleItem(
                            title = "Active Conflict Warnings",
                            subtitle = "Immediate warning banner if toggles physically oppose each other structurally",
                            checked = safetyPinConflictWarning,
                            onCheckedChange = { viewModel.updateSafetyPinConflictWarning(it) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        SettingsToggleItem(
                            title = "Aesthetic Recommendations",
                            subtitle = "Suggest complementary layout features whenever core styles change",
                            checked = safetyPinRecommendations,
                            onCheckedChange = { viewModel.updateSafetyPinRecommendations(it) }
                        )
                    }
                }
            }

            // 2. Focus & OLED Safety Rules
            SettingsGroupCard(title = "Focus & OLED Safety Rules", icon = Icons.Rounded.BrightnessLow) {
                SettingsToggleItem(
                    title = "True Black OLED Focus",
                    subtitle = "AOD focus screen will use solid #000000 pixels to conserve battery on OLED hardware",
                    checked = aodTrueBlackOled,
                    icon = Icons.Rounded.Contrast,
                    onCheckedChange = { viewModel.updateAodTrueBlackOled(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Auto-Deactivate with Bright Themes",
                    subtitle = "Automatically replace True Black with a dimmed themed focus screen when using Light theme",
                    checked = aodAutoDeactivateTrueBlack,
                    onCheckedChange = { viewModel.updateAodAutoDeactivateTrueBlack(it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        "Pixel Burn-In Shift Interval",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Period taken before slightly shifting always-on focus layout items to protect screen pixels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 20, 30).forEach { seconds ->
                            val isSelected = aodBurnInShiftSpeed == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateAodBurnInShiftSpeed(seconds) },
                                label = { Text("$seconds sec", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SettingsToggleItem(
                    title = "Lock Screen Protection",
                    subtitle = "Allows AOD to safely bypass system lock screen without permanently waking display",
                    checked = aodLockScreenSupport,
                    icon = Icons.Rounded.Lock,
                    onCheckedChange = { viewModel.updateAodLockScreenSupport(it) }
                )
            }

            // 3. True Always-On Display (Advanced Mode)
            SettingsGroupCard(title = "True Always-On Display", icon = Icons.Rounded.CropFree) {
                SettingsToggleItem(
                    title = "True Always-On Display",
                    subtitle = "Draw a full-screen system overlay clock directly over lockscreens, launchers, and apps",
                    checked = aodTrueAodEnabled,
                    icon = Icons.Rounded.CropFree,
                    onCheckedChange = { viewModel.updateAodTrueAodEnabled(it) }
                )

                AnimatedVisibility(
                    visible = aodTrueAodEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Integration Mode Selection
                        Text(
                            text = "Integration Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mode 1: System Overlay
                            val isOverlaySelected = aodTrueAodMode == "overlay"
                            Surface(
                                onClick = { viewModel.updateAodTrueAodMode("overlay") },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isOverlaySelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    width = if (isOverlaySelected) 1.5.dp else 0.8.dp,
                                    color = if (isOverlaySelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.ViewQuilt,
                                            contentDescription = null,
                                            tint = if (isOverlaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "System Overlay",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Fast responsive system clock layer.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Mode 2: Accessibility Overlay
                            val isAccessSelected = aodTrueAodMode == "accessibility"
                            Surface(
                                onClick = { viewModel.updateAodTrueAodMode("accessibility") },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isAccessSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    width = if (isAccessSelected) 1.5.dp else 0.8.dp,
                                    color = if (isAccessSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Accessibility,
                                            contentDescription = null,
                                            tint = if (isAccessSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Accessibility",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Safely behind lock controls.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Permission Warning if missing
                        val currentPermissionGranted = if (aodTrueAodMode == "overlay") hasOverlayPermission else hasAccessibilityPermission
                        if (!currentPermissionGranted) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (aodTrueAodMode == "overlay") "Overlay Authorization Required" else "Accessibility Authorization Required",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (aodTrueAodMode == "overlay") {
                                            "Enable 'Draw over other apps' to allow Lumia to overlay a pure black OLED focus clock."
                                        } else {
                                            "Enable Lumia's Accessibility Service to render the AOD safely behind system lock panels."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
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
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Grant Authorization", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Accessibility mode secure lock option
                        if (aodTrueAodMode == "accessibility" && hasAccessibilityPermission) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            val isSecureLockEnabled = aodLockTimeout > 0
                            SettingsToggleItem(
                                title = "Secure Lock Fallback",
                                subtitle = "Increase security: programmatically locks the system after AOD commences",
                                checked = isSecureLockEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateAodLockTimeout(if (isChecked) 30 else 0)
                                }
                            )

                            if (isSecureLockEnabled) {
                                Text(
                                    text = "Screen Lock Timeout",
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
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.updateAodLockTimeout(seconds) },
                                            label = { Text("${seconds}s", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // AOD Wake-up sensitivity
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "AOD Wake-Up Sensitivity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Control physical contact and motion thresholds required to wake up from Always-On Display",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val sensitivityOptions = listOf(
                                Pair("motion", "Motion & Tap"),
                                Pair("highest", "Single Tap"),
                                Pair("medium", "Double Tap"),
                                Pair("secure", "Hold (1s)")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sensitivityOptions.forEach { (sens, label) ->
                                    val isSelected = aodSensitivity == sens
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateAodSensitivity(sens) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            if (aodSensitivity == "motion") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Motion Sensor Sensitivity",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val motionOptions = listOf(
                                    Pair(0.5f, "Very High"),
                                    Pair(1.2f, "High"),
                                    Pair(2.5f, "Medium"),
                                    Pair(4.0f, "Low"),
                                    Pair(0f, "Tap only")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    motionOptions.forEach { (threshold, label) ->
                                        val isSelected = aodMotionSensitivity == threshold
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.updateAodMotionSensitivity(threshold) },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Dimness override level
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Screen Dimness Override",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Controls pixel dimness to save battery power and enhance eye comfort",
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
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateAodDimnessLevel(level) },
                                        label = { Text(percentString, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
