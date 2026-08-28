package lumia.tracker.ui.screens.focus

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
import androidx.compose.ui.graphics.Color
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
 * PomodoroSettingsDialog - Clean configuration modal for Pomodoro intervals,
 * cycle lengths, and automation preferences.
 */
@ValueScore(
    score = 86,
    importance = Importance.HIGH,
    description = "Modal configuration sheet for focus intervals, breaks, and auto-logging preferences",
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
            // Header Row with Title and Reset Button
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

                TextButton(
                    onClick = {
                        tempWork = 25f
                        tempShort = 5f
                        tempLong = 15f
                        tempSessions = 4f
                    }
                ) {
                    Text("Reset", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

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

            // 5. Automation & Logging Preferences
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PREFERENCES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    // Auto-Log Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-Log Focus Sessions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = tempAutoLog,
                            onCheckedChange = { tempAutoLog = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // Cycle Target Alert Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cycle Complete Notification",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = tempPeriodTarget,
                            onCheckedChange = { tempPeriodTarget = it }
                        )
                    }
                }
            }

            // Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyTextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
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
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@ValueScore(
    score = 70,
    importance = Importance.HIGH,
    description = "Clean slider and preset chip card for duration settings",
    category = "Focus"
)
@Composable
private fun DurationSettingCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(iconTint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconTint.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            )

            // Preset Quick Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = sliderValue.toInt() == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) iconTint else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) iconTint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClick { onSelectPreset(preset) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

