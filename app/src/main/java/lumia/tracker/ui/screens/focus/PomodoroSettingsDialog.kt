package lumia.tracker.ui.screens.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroSettingsDialog - Modern glassmorphic configuration modal for Pomodoro intervals,
 * cycle lengths, capsule toggle pills, and tactile controls.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Modern glassmorphic modal with capsule toggle pills, slider controls, and CircleShape confirm buttons",
    category = "Focus"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsDialog(
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    val workDuration by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val shortBreakDuration by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
    val longBreakDuration by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()
    val autoLog by viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle()
    val enablePeriodTarget by viewModel.pomodoroEnablePeriodTarget.collectAsStateWithLifecycle()

    var tempWork by remember { mutableFloatStateOf(workDuration.toFloat()) }
    var tempShort by remember { mutableFloatStateOf(shortBreakDuration.toFloat()) }
    var tempLong by remember { mutableFloatStateOf(longBreakDuration.toFloat()) }
    var tempSessions by remember { mutableFloatStateOf(periodSessions.toFloat()) }
    var tempAutoLog by remember { mutableStateOf(autoLog) }
    var tempPeriodTarget by remember { mutableStateOf(enablePeriodTarget) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Modern Reset Capsule
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Focus Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.60f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                    modifier = Modifier.bouncyClick {
                        tempWork = 25f
                        tempShort = 5f
                        tempLong = 15f
                        tempSessions = 4f
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

            // 1. Focus Interval Slider Card
            DurationSettingCard(
                title = "Focus Duration",
                icon = Icons.Rounded.Psychology,
                iconTint = MaterialTheme.colorScheme.primary,
                valueText = "${tempWork.toInt()}m",
                sliderValue = tempWork,
                onValueChange = { tempWork = it },
                valueRange = 5f..90f,
                steps = 16,
                presets = listOf(15, 25, 45, 60),
                onSelectPreset = { tempWork = it.toFloat() }
            )

            // 2. Short Break Duration Slider Card
            DurationSettingCard(
                title = "Short Break",
                icon = Icons.Rounded.Coffee,
                iconTint = MaterialTheme.colorScheme.secondary,
                valueText = "${tempShort.toInt()}m",
                sliderValue = tempShort,
                onValueChange = { tempShort = it },
                valueRange = 1f..30f,
                steps = 28,
                presets = listOf(3, 5, 10, 15),
                onSelectPreset = { tempShort = it.toFloat() }
            )

            // 3. Long Break Duration Slider Card
            DurationSettingCard(
                title = "Long Break",
                icon = Icons.Rounded.SelfImprovement,
                iconTint = MaterialTheme.colorScheme.tertiary,
                valueText = "${tempLong.toInt()}m",
                sliderValue = tempLong,
                onValueChange = { tempLong = it },
                valueRange = 5f..60f,
                steps = 10,
                presets = listOf(15, 20, 30, 45),
                onSelectPreset = { tempLong = it.toFloat() }
            )

            // 4. Cycle Count Slider Card
            DurationSettingCard(
                title = "Cycle Length",
                icon = Icons.Rounded.Autorenew,
                iconTint = MaterialTheme.colorScheme.primary,
                valueText = "${tempSessions.toInt()} sessions",
                sliderValue = tempSessions,
                onValueChange = { tempSessions = it },
                valueRange = 1f..10f,
                steps = 8,
                presets = listOf(2, 4, 6, 8),
                onSelectPreset = { tempSessions = it.toFloat() }
            )

            // 5. Automation & Logging Preferences Card with Capsule Toggle Pills
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                glassmorphic = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Auto-Log Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick { tempAutoLog = !tempAutoLog },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HistoryEdu,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Auto-log sessions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        CapsuleTogglePill(
                            checked = tempAutoLog,
                            onCheckedChange = { tempAutoLog = it },
                            activeColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                    // Cycle Target Alert Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick { tempPeriodTarget = !tempPeriodTarget },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Cycle complete alert",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        CapsuleTogglePill(
                            checked = tempPeriodTarget,
                            onCheckedChange = { tempPeriodTarget = it },
                            activeColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Bottom Action Buttons with CircleShape
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyTextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                BouncyButton(
                    onClick = {
                        viewModel.updatePomodoroWorkDuration(tempWork.toInt())
                        viewModel.updatePomodoroShortBreakDuration(tempShort.toInt())
                        viewModel.updatePomodoroLongBreakDuration(tempLong.toInt())
                        viewModel.updatePomodoroPeriodSessions(tempSessions.toInt())
                        viewModel.updateSystemPomodoroAutoLog(tempAutoLog)
                        viewModel.updatePomodoroEnablePeriodTarget(tempPeriodTarget)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * CapsuleTogglePill - Tactile capsule switch pill with animated sliding indicator.
 */
@Composable
fun CapsuleTogglePill(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_thumb_offset"
    )
    val pillBg by animateColorAsState(
        targetValue = if (checked) activeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(durationMillis = 200),
        label = "pill_bg_color"
    )
    val thumbBg by animateColorAsState(
        targetValue = if (checked) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 200),
        label = "pill_thumb_color"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(pillBg)
            .bouncyClick {
                haptic.performHapticFeedback(HapticFeedbackType.LightImpact)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbBg),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * DurationSettingCard - Glassmorphic card for duration and cycle length sliders with capsule preset pills.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Modern glassmorphic slider card with capsule preset pills and crisp status indicator",
    category = "Focus"
)
@Composable
private fun DurationSettingCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    valueText: String,
    sliderValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    presets: List<Int>,
    onSelectPreset: (Int) -> Unit
) {
    ScholarCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        glassmorphic = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(iconTint.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f))
                ) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Slider(
                value = sliderValue,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = iconTint,
                    activeTrackColor = iconTint,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                )
            )

            // Preset Quick Selection Capsule Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = sliderValue.toInt() == preset
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) iconTint else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.50f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) iconTint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClick { onSelectPreset(preset) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

