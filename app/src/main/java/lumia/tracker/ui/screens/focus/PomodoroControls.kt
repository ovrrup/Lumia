package lumia.tracker.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.util.TrueAodManager
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroControls - Primary tactile interaction suite for focus sessions.
 * Features a 64dp primary action button, secondary skip/reset/stop controls,
 * quick utility launcher chips, and True AOD engine selection with live permission indicators.
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
        // 1. Alarm Dismiss Alert Banner (if alarm is firing)
        AnimatedVisibility(
            visible = isAlarmActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Timer Interval Complete!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    BouncyButton(
                        onClick = onStopAlarm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Primary 64dp Action Cluster (Start / Pause / Resume / Skip / Stop)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRunning) {
                // PRIMARY START BUTTON (64dp Height)
                BouncyButton(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Start Focus",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Start Focus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                // RUNNING STATE: 64dp Pause/Resume + 64dp Skip + 64dp Stop
                BouncyButton(
                    onClick = onPauseResume,
                    modifier = Modifier
                        .weight(1.8f)
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isPaused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Skip Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClick(onClick = onSkip)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip Cycle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Stop / Reset Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClick(onClick = onStop)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop Timer",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // 3. Secondary Utility Row (True AOD, Zen Mode, Intervals)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // True AOD Low-Power Mode
            UtilityFilterChip(
                icon = Icons.Rounded.BrightnessLow,
                label = "True AOD",
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
                modifier = Modifier.weight(1f)
            )

            // Zen Fullscreen Mode
            UtilityFilterChip(
                icon = Icons.Rounded.Fullscreen,
                label = "Zen Mode",
                onClick = onOpenZenMode,
                modifier = Modifier.weight(1f)
            )

            // Timer Interval Preferences
            UtilityFilterChip(
                icon = Icons.Rounded.Tune,
                label = "Intervals",
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Interactive True AOD Engine Selector Modal Dialog
        if (showAodEngineDialog) {
            val hasOverlay = Settings.canDrawOverlays(context)
            val hasAccessibility = AodAccessibilityService.isServiceEnabled(context)

            AlertDialog(
                onDismissRequest = { showAodEngineDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.BrightnessLow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "True AOD Engine",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Choose your preferred engine to render True Always-On Display as a 100% pure #000000 black OLED screen with pixel anti-burn-in shifting:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Option 1: Display Over Other Apps
                        val isOverlaySelected = selectedEngine == "overlay"
                        EngineSelectionCard(
                            title = "Display Over Other Apps",
                            subtitle = "System Window Overlay Engine",
                            description = "Renders fullscreen OLED clock over open apps and launchers with zero battery overhead.",
                            icon = Icons.Rounded.ViewQuilt,
                            isSelected = isOverlaySelected,
                            isGranted = hasOverlay,
                            onClick = {
                                selectedEngine = "overlay"
                                viewModel?.updateAodTrueAodMode("overlay")
                            },
                            onGrantPermission = {
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
                        )

                        // Option 2: Accessibility Service
                        val isAccessSelected = selectedEngine == "accessibility"
                        EngineSelectionCard(
                            title = "Accessibility Service",
                            subtitle = "Hardware Screen Lock Integration",
                            description = "Runs behind lock panels and provides automated hardware locking when timeout triggers.",
                            icon = Icons.Rounded.Accessibility,
                            isSelected = isAccessSelected,
                            isGranted = hasAccessibility,
                            onClick = {
                                selectedEngine = "accessibility"
                                viewModel?.updateAodTrueAodMode("accessibility")
                            },
                            onGrantPermission = {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
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
                        enabled = canLaunch,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Launch True AOD", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    BouncyTextButton(onClick = { showAodEngineDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
private fun UtilityFilterChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.bouncyClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EngineSelectionCard(
    title: String,
    subtitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isGranted: Boolean,
    onClick: () -> Unit,
    onGrantPermission: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Permission Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isGranted) "Granted" else "Required",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF34C759) else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted && isSelected) {
                Spacer(Modifier.height(8.dp))
                BouncyTextButton(
                    onClick = onGrantPermission,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Grant Permission in Settings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
