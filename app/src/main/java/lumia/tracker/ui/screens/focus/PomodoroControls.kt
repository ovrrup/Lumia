package lumia.tracker.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.util.TrueAodManager
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroControls - Primary tactile interaction suite for focus sessions.
 * Features start/pause/resume/skip/stop actions alongside True AOD engine selection,
 * fullscreen zen, and timer settings.
 */
@Composable
fun PomodoroControls(
    isRunning: Boolean,
    isPaused: Boolean,
    isAlarmActive: Boolean,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onStopAlarm: () -> Unit,
    onStartAod: () -> Unit,
    onOpenZenMode: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ScholarViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAodEngineDialog by remember { mutableStateOf(false) }

    // Read AOD preferences if viewModel is provided
    val aodMode = viewModel?.aodTrueAodMode?.collectAsStateWithLifecycle()?.value ?: "overlay"
    val aodDimness = viewModel?.aodDimnessLevel?.collectAsStateWithLifecycle()?.value ?: 0.95f
    val aodSensitivity = viewModel?.aodSensitivity?.collectAsStateWithLifecycle()?.value ?: "medium"
    val aodMotionSens = viewModel?.aodMotionSensitivity?.collectAsStateWithLifecycle()?.value ?: 1.2f
    val aodLockTimeout = viewModel?.aodLockTimeout?.collectAsStateWithLifecycle()?.value ?: 30
    val aodBurnInShiftSpeed = viewModel?.aodBurnInShiftSpeed?.collectAsStateWithLifecycle()?.value ?: 10

    var selectedEngine by remember(aodMode) { mutableStateOf(aodMode) }

    fun launchTrueAod(useAccessibility: Boolean) {
        TrueAodManager.showAodOverlay(
            context = context,
            useAccessibility = useAccessibility,
            dimnessLevel = aodDimness,
            sensitivity = aodSensitivity,
            motionSensitivity = aodMotionSens,
            lockTimeoutSeconds = if (useAccessibility) aodLockTimeout else 0,
            burnInShiftIntervalSeconds = aodBurnInShiftSpeed,
            onExit = { }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Alarm Dismiss Button (if alarm is firing)
        if (isAlarmActive) {
            Button(
                onClick = onStopAlarm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Rounded.NotificationsOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dismiss Alarm", fontWeight = FontWeight.Bold)
            }
        }

        // 2. Primary Play / Pause & Navigation Action Cluster
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRunning) {
                // START BUTTON
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Start Timer", modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                // PAUSE / RESUME BUTTON
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isPaused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // SKIP CYCLE BUTTON
                FilledTonalIconButton(
                    onClick = onSkip,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Cycle")
                }

                // STOP / RESET BUTTON
                FilledTonalIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = "Stop Timer")
                }
            }
        }

        // 3. Secondary Utility Row: True AOD, Zen Fullscreen Mode, Timer Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // True AOD Low-Power Mode
            FilterChip(
                selected = false,
                onClick = {
                    val hasOverlay = Settings.canDrawOverlays(context)
                    val hasAccessibility = AodAccessibilityService.isServiceEnabled(context)

                    if (selectedEngine == "accessibility" && hasAccessibility) {
                        launchTrueAod(useAccessibility = true)
                    } else if (selectedEngine == "overlay" && hasOverlay) {
                        launchTrueAod(useAccessibility = false)
                    } else {
                        showAodEngineDialog = true
                    }
                },
                label = { Text("True AOD", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessLow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )

            // Zen Fullscreen Immersion Mode
            FilterChip(
                selected = false,
                onClick = onOpenZenMode,
                label = { Text("Zen Mode", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )

            // Timer Settings
            FilterChip(
                selected = false,
                onClick = onOpenSettings,
                label = { Text("Intervals", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Interactive True AOD Engine Selector Modal
        if (showAodEngineDialog) {
            val hasOverlay = Settings.canDrawOverlays(context)
            val hasAccessibility = AodAccessibilityService.isServiceEnabled(context)

            AlertDialog(
                onDismissRequest = { showAodEngineDialog = false },
                icon = {
                    Icon(
                        Icons.Rounded.BrightnessLow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        "True AOD Engine",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Choose your preferred engine to render True Always-On Display as a physical #000000 black OLED screen with anti-burn-in protection:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Option 1: Display Over Other Apps (System Window Overlay)
                        val isOverlaySelected = selectedEngine == "overlay"
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isOverlaySelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = if (isOverlaySelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEngine = "overlay"
                                    viewModel?.updateAodTrueAodMode("overlay")
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(if (isOverlaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.ViewQuilt,
                                                contentDescription = null,
                                                tint = if (isOverlaySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Display Over Apps",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                "System Window Overlay",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (hasOverlay) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = if (hasOverlay) "Granted" else "Required",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasOverlay) Color(0xFF34C759) else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Renders fullscreen OLED black clock over launchers and active apps with zero battery overhead.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!hasOverlay && isOverlaySelected) {
                                    Spacer(Modifier.height(8.dp))
                                    BouncyTextButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Grant Overlay Permission")
                                    }
                                }
                            }
                        }

                        // Option 2: Accessibility Service Overlay
                        val isAccessSelected = selectedEngine == "accessibility"
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isAccessSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = if (isAccessSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEngine = "accessibility"
                                    viewModel?.updateAodTrueAodMode("accessibility")
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(if (isAccessSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Accessibility,
                                                contentDescription = null,
                                                tint = if (isAccessSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Accessibility Service",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                "System Lock Integration",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (hasAccessibility) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = if (hasAccessibility) "Active" else "Required",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasAccessibility) Color(0xFF34C759) else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Renders behind system lock panels and provides automated hardware screen locking when timeout triggers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!hasAccessibility && isAccessSelected) {
                                    Spacer(Modifier.height(8.dp))
                                    BouncyTextButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Enable in Accessibility")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val canLaunch = (selectedEngine == "overlay" && hasOverlay) ||
                                    (selectedEngine == "accessibility" && hasAccessibility)
                    BouncyButton(
                        onClick = {
                            showAodEngineDialog = false
                            launchTrueAod(useAccessibility = selectedEngine == "accessibility")
                        },
                        enabled = canLaunch
                    ) {
                        Text("Start True AOD")
                    }
                },
                dismissButton = {
                    BouncyTextButton(onClick = { showAodEngineDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
