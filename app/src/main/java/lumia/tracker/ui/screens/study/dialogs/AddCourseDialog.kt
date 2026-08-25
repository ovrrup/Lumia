package lumia.tracker.ui.screens.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCourseDialog(
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3197D6") }
    var selectedDaysList by remember { mutableStateOf<List<String>>(emptyList()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var pickerTargetIsStart by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    var instructor by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedSubjectIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val daysOfWeekList = remember { listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday") }
    val colorsList = remember { listOf("#3197D6", "#2ECC71", "#E74C3C", "#F1C40F", "#9B59B6", "#E67E22", "#34495E") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Add New Course",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set up schedule, color, and subject links",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Course Code & Name
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    placeholder = { Text("e.g. CS101, MATH204") },
                    leadingIcon = {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name *") },
                    placeholder = { Text("e.g. Data Structures & Algorithms") },
                    leadingIcon = {
                        Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor") },
                    placeholder = { Text("e.g. Prof. Alexander") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Course Color Tag
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Course Color Theme",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorsList.forEach { col ->
                            val c = Color(android.graphics.Color.parseColor(col))
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
                                    .clickable { selectedColor = col }
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

                // Schedule Days
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Schedule Days",
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
                        daysOfWeekList.forEach { day ->
                            val isSelected = selectedDaysList.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDaysList = if (isSelected) {
                                        selectedDaysList - day
                                    } else {
                                        selectedDaysList + day
                                    }
                                },
                                label = { Text(day.take(3), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Schedule Time Range
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Schedule Time Range",
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
                                .clickable {
                                    pickerTargetIsStart = true
                                    showTimePicker = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .clickable {
                                    pickerTargetIsStart = false
                                    showTimePicker = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

                // TimePicker Dialog Modal
                if (showTimePicker) {
                    val initialHour = remember {
                        try {
                            val activeStr = if (pickerTargetIsStart) startTime else endTime
                            var parsedHour = activeStr.substringBefore(":").toInt()
                            if (activeStr.contains("PM", ignoreCase = true) && parsedHour < 12) parsedHour += 12
                            if (activeStr.contains("AM", ignoreCase = true) && parsedHour == 12) parsedHour = 0
                            parsedHour
                        } catch (e: Exception) { 12 }
                    }
                    val initialMinute = remember {
                        try {
                            val activeStr = if (pickerTargetIsStart) startTime else endTime
                            activeStr.substringAfter(":").substringBefore(" ").toInt()
                        } catch (e: Exception) { 0 }
                    }
                    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
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
                                    if (pickerTargetIsStart) {
                                        startTime = formattedTime
                                    } else {
                                        endTime = formattedTime
                                    }
                                    showTimePicker = false
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Set Time")
                            }
                        },
                        dismissButton = {
                            BouncyTextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }

                // Description & Tags
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Add course syllabus, objectives, or lecture room info") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (Optional)") },
                    placeholder = { Text("core, semester1, mandatory") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Link to Subject(s)
                val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Link to Study Subject (Optional)",
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
                            onClick = { selectedSubjectIds = emptySet() },
                            label = { Text("None") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        subjects.forEach { subj ->
                            val isSelected = selectedSubjectIds.contains(subj.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSubjectIds = if (isSelected) {
                                        selectedSubjectIds - subj.id
                                    } else {
                                        selectedSubjectIds + subj.id
                                    }
                                },
                                label = { Text(subj.name) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        AssistChip(
                            onClick = { showAddSubjectDialog = true },
                            label = { Text("+ New Subject", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val computedSchedule = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else schedule
                        viewModel.addCourse(
                            name = name.trim(),
                            code = code.trim(),
                            colorHex = selectedColor,
                            scheduleDays = selectedDaysList.joinToString(","),
                            scheduleStartTime = startTime,
                            scheduleEndTime = endTime,
                            instructor = instructor.trim(),
                            schedule = computedSchedule,
                            description = description.trim(),
                            subjectId = selectedSubjectIds.firstOrNull(),
                            tags = tags.trim(),
                            subjectIds = selectedSubjectIds.joinToString(",")
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add Course", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            viewModel = viewModel,
            onDismiss = { showAddSubjectDialog = false }
        )
    }
}
