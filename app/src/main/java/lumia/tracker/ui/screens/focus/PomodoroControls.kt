package lumia.tracker.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.service.PomodoroMode
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.util.TrueAodManager
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroControls - Modern capsule action suite with frosted specular glass borders and tactile bouncy clicks.
 * Features capsule action pills (CircleShape), fluid pause/resume states, minimalist utility chips,
 * and OLED True AOD engine launcher.
 */
@ValueScore(
    score = 95,
    importance = Importance.CRITICAL,
    description = "Modern capsule action suite with frosted specular glass borders, fluid animations, and True AOD launcher",
    category = "Focus"
)
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
    currentMode: PomodoroMode = PomodoroMode.WORK,
    isAtFullDuration: Boolean = false,
    viewModel: ScholarViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Alarm Dismiss Alert Banner (Sleek Floating Capsule)
        AnimatedVisibility(
            visible = isAlarmActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.85f else 0.95f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Timer Complete",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onError.copy(alpha = 0.25f)),
                        modifier = Modifier.bouncyClick(onClick = onStopAlarm)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 2. Primary Action Controls (Capsule Action Pills with Frosted Specular Borders)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modeColor = when (currentMode) {
                PomodoroMode.WORK -> MaterialTheme.colorScheme.primary
                PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                PomodoroMode.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
            }

            val startLabel = when (currentMode) {
                PomodoroMode.WORK -> "Start Focus"
                PomodoroMode.SHORT_BREAK -> "Start Break"
                PomodoroMode.LONG_BREAK -> "Start Long Break"
            }

            if (!isRunning) {
                // PRIMARY START CAPSULE PILL
                val startBorder = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.20f)
                )

                Surface(
                    shape = CircleShape,
                    color = modeColor,
                    border = startBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bouncyClick(onClick = onStart)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = startLabel,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                // RUNNING STATE: Modern Capsule Action Pills (Pause/Resume + Skip + Stop)
                val isResumingBreakOrWork = isPaused && isAtFullDuration
                val currentActionText = if (isPaused) {
                    if (isResumingBreakOrWork) startLabel else "Resume"
                } else {
                    "Pause"
                }

                val pauseButtonBg by animateColorAsState(
                    targetValue = if (isPaused) modeColor else MaterialTheme.colorScheme.secondaryContainer,
                    animationSpec = tween(220),
                    label = "pause_btn_bg"
                )
                val pauseButtonContent by animateColorAsState(
                    targetValue = if (isPaused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                    animationSpec = tween(220),
                    label = "pause_btn_content"
                )
                val pauseBorder = BorderStroke(
                    width = 1.dp,
                    color = pauseButtonContent.copy(alpha = 0.20f)
                )

                // Pause / Resume Capsule Action Pill
                Surface(
                    shape = CircleShape,
                    color = pauseButtonBg,
                    border = pauseBorder,
                    modifier = Modifier
                        .weight(1.8f)
                        .height(56.dp)
                        .bouncyClick(onClick = onPauseResume)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = currentActionText,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)).togetherWith(fadeOut(animationSpec = tween(120)))
                            },
                            label = "pause_resume_animated_content"
                        ) { label ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = label,
                                    tint = pauseButtonContent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pauseButtonContent
                                )
                            }
                        }
                    }
                }

                // Skip Capsule Pill
                Surface(
                    shape = CircleShape,
                    color = if (isDark) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                    border = ScholarCardDefaults.border(),
                    modifier = Modifier
                        .size(56.dp)
                        .bouncyClick(onClick = onSkip)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip Interval",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Stop / Reset Capsule Pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .size(56.dp)
                        .bouncyClick(onClick = onStop)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop Timer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 3. Secondary Utility Row (Capsule Action Chips with Frosted Specular Borders)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
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

        // 4. True AOD Engine Selector Modal Dialog
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "True AOD Engine",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose OLED display engine:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Option 1: Display Over Other Apps
                        val isOverlaySelected = selectedEngine == "overlay"
                        EngineSelectionCard(
                            title = "Window Overlay",
                            subtitle = "Display over applications",
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
                            subtitle = "Hardware screen lock",
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
                    Surface(
                        shape = CircleShape,
                        color = if (canLaunch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (canLaunch) BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)) else null,
                        modifier = Modifier.bouncyClick(
                            enabled = canLaunch,
                            onClick = {
                                showAodEngineDialog = false
                                launchTrueAod(useAccessibility = selectedEngine == "accessibility")
                            }
                        )
                    ) {
                        Text(
                            text = "Launch",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (canLaunch) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                },
                dismissButton = {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.bouncyClick(onClick = { showAodEngineDialog = false })
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(26.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@ValueScore(
    score = 68,
    importance = Importance.MEDIUM,
    description = "Clean modern capsule utility chip for launching Zen mode, True AOD, or interval configuration",
    category = "Focus"
)
@Composable
private fun UtilityFilterChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        shape = CircleShape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        border = ScholarCardDefaults.border(),
        modifier = modifier
            .height(42.dp)
            .bouncyClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@ValueScore(
    score = 72,
    importance = Importance.HIGH,
    description = "Clean engine selection card for True AOD overlay and accessibility modes",
    category = "Focus"
)
@Composable
private fun EngineSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isGranted: Boolean,
    onClick: () -> Unit,
    onGrantPermission: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val cardBorder = if (isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f))
    } else {
        ScholarCardDefaults.border()
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerBg,
        border = cardBorder,
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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

                // Live Permission Badge Capsule
                Surface(
                    shape = CircleShape,
                    color = if (isGranted) Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (isGranted) Color(0xFF34C759).copy(alpha = 0.35f) else MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (isGranted) "Granted" else "Required",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF34C759) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (!isGranted && isSelected) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.bouncyClick(onClick = onGrantPermission)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Grant Permission",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


