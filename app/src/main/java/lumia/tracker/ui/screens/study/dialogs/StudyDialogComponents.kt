package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.util.Locale

val STUDY_DAYS_OF_WEEK = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
val STUDY_COLOR_PALETTE = listOf("#3197D6", "#2ECC71", "#E74C3C", "#F1C40F", "#9B59B6", "#E67E22", "#34495E")

/**
 * Standardized dialog header component with circular icon surface and title/subtitle.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Standardized header with rounded icon container, title, and subtitle for study dialogs",
    category = "Dialog"
)
@Composable
fun StudyDialogHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Reusable weekday selector flow row of filter chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@ValueScore(
    score = 70,
    importance = Importance.HIGH,
    description = "Reusable weekday multi-selector chips for course and study scheduling",
    category = "Dialog"
)
@Composable
fun WeekdaySelectorChips(
    selectedDays: List<String>,
    onDaysSelectedChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Schedule Days",
    daysList: List<String> = STUDY_DAYS_OF_WEEK
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            daysList.forEach { day ->
                val isSelected = selectedDays.contains(day)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newDays = if (isSelected) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                        onDaysSelectedChange(newDays)
                    },
                    label = { Text(day.take(3), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Circular color picker palette with selection indicator ring and checkmark.
 */
@ValueScore(
    score = 70,
    importance = Importance.HIGH,
    description = "Reusable circular color theme picker palette with checkmark selection for study entities",
    category = "Dialog"
)
@Composable
fun ColorPickerPalette(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Course Color Theme",
    colors: List<String> = STUDY_COLOR_PALETTE
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { col ->
                val c = try {
                    Color(android.graphics.Color.parseColor(col))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                val isSelected = selectedColor.equals(col, ignoreCase = true)
                Surface(
                    shape = CircleShape,
                    color = c,
                    border = if (isSelected) {
                        BorderStroke(2.5.dp, MaterialTheme.colorScheme.onSurface)
                    } else {
                        BorderStroke(1.dp, Color.Transparent)
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onColorSelected(col) }
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dual start and end time selector surfaces for schedule configuration.
 */
@ValueScore(
    score = 68,
    importance = Importance.MEDIUM,
    description = "Reusable dual-button time range selector for start and end class times",
    category = "Dialog"
)
@Composable
fun ScheduleTimeRangePicker(
    startTime: String,
    endTime: String,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Schedule Time Range"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (startTime.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onStartTimeClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (startTime.isBlank()) "Start Time" else startTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (endTime.isNotBlank()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onEndTimeClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (endTime.isBlank()) "End Time" else endTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Material3 TimePickerDialog for selecting 12-hour formatted time strings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@ValueScore(
    score = 72,
    importance = Importance.MEDIUM,
    description = "Time picker modal dialog parsing and formatting 12-hour AM/PM time strings",
    category = "Dialog"
)
@Composable
fun StudyTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = remember(initialTime) {
        try {
            var parsedHour = initialTime.substringBefore(":").toInt()
            if (initialTime.contains("PM", ignoreCase = true) && parsedHour < 12) parsedHour += 12
            if (initialTime.contains("AM", ignoreCase = true) && parsedHour == 12) parsedHour = 0
            parsedHour
        } catch (e: Exception) { 12 }
    }
    val initialMinute = remember(initialTime) {
        try {
            initialTime.substringAfter(":").substringBefore(" ").toInt()
        } catch (e: Exception) { 0 }
    }
    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {
            BouncyButton(
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val formatHour = if (hour % 12 == 0) 12 else hour % 12
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", formatHour, minute, amPm)
                    onTimeSelected(formattedTime)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

/**
 * Subject linkage chip selector with support for multi-selection and creating new subjects.
 */
@OptIn(ExperimentalLayoutApi::class)
@ValueScore(
    score = 70,
    importance = Importance.HIGH,
    description = "Reusable study subject linker flow row with multi-select chips and create subject assist chip",
    category = "Dialog"
)
@Composable
fun SubjectSelectorChips(
    subjects: List<Subject>,
    selectedSubjectIds: Set<Int>,
    onSubjectSelectionChanged: (Set<Int>) -> Unit,
    onAddNewSubjectClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Link to Study Subject (Optional)"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedSubjectIds.isEmpty(),
                onClick = { onSubjectSelectionChanged(emptySet()) },
                label = { Text("None") },
                shape = RoundedCornerShape(12.dp)
            )
            subjects.forEach { subj ->
                val isSelected = selectedSubjectIds.contains(subj.id)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedSubjectIds - subj.id
                        } else {
                            selectedSubjectIds + subj.id
                        }
                        onSubjectSelectionChanged(newSelection)
                    },
                    label = { Text(subj.name) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            AssistChip(
                onClick = onAddNewSubjectClick,
                label = { Text("+ New Subject", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
