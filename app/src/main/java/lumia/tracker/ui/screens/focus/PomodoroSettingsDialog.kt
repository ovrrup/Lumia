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
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroSettingsDialog - Interactive configuration modal for Pomodoro intervals,
 * cycle lengths, automation auto-logging, and target alerts.
 */
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Focus Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
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
                    Text("Reset Defaults", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 1. Focus Interval Slider Card
            DurationSettingCard(
                title = "Focus Interval",
                subtitle = "Length of individual concentrated study sprints",
                icon = Icons.Rounded.Psychology,
                iconTint = MaterialTheme.colorScheme.primary,
                valueText = "${tempWork.toInt()} min",
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
                subtitle = "Quick breathing rest between active study sprints",
                icon = Icons.Rounded.Coffee,
                iconTint = MaterialTheme.colorScheme.secondary,
                valueText = "${tempShort.toInt()} min",
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
                subtitle = "Extended recharge rest after completing a full cycle",
                icon = Icons.Rounded.SelfImprovement,
                iconTint = MaterialTheme.colorScheme.tertiary,
                valueText = "${tempLong.toInt()} min",
                sliderValue = tempLong,
                onValueChange = { tempLong = it },
                valueRange = 5f..60f,
                steps = 10,
                presets = listOf(15, 20, 30, 45),
                onSelectPreset = { tempLong = it.toFloat() }
            )

            // 4. Cycle Count Slider Card
            DurationSettingCard(
                title = "Sessions per Cycle",
                subtitle = "Number of focus intervals before triggering a long break",
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
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AUTOMATION & LOGGING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Auto-Log Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Log Focus Sessions",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatically record completed sessions to study analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = tempAutoLog,
                            onCheckedChange = { tempAutoLog = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                    // Cycle Target Alert Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cycle Target Alert",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Notify when a full multi-session Pomodoro cycle is achieved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Apply Preferences", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DurationSettingCard(
    title: String,
    subtitle: String,
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
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                            .size(34.dp)
                            .background(iconTint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconTint.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
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
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )

            // Preset Quick Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = sliderValue.toInt() == preset
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) iconTint else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) iconTint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
