package lumia.tracker.ui.screens.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        title = { Text("Add New Course") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Course Color Tag", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorsList.forEach { col ->
                        val c = Color(android.graphics.Color.parseColor(col))
                        val isSelected = selectedColor.equals(col, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = col }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Schedule Days", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                            label = { Text(day.substring(0, 3)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Schedule Time Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { 
                            pickerTargetIsStart = true
                            showTimePicker = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (startTime.isBlank()) "Start Time" else "Start: $startTime")
                    }
                    Button(
                        onClick = { 
                            pickerTargetIsStart = false
                            showTimePicker = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (endTime.isBlank()) "End Time" else "End: $endTime")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                        confirmButton = {
                            BouncyTextButton(onClick = {
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
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            BouncyTextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (Optional, comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Link to Study Subject (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedSubjectIds.isEmpty(),
                        onClick = { selectedSubjectIds = emptySet() },
                        label = { Text("None") }
                    )
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubjectIds.contains(subj.id),
                            onClick = {
                                selectedSubjectIds = if (selectedSubjectIds.contains(subj.id)) {
                                    selectedSubjectIds - subj.id
                                } else {
                                    selectedSubjectIds + subj.id
                                }
                            },
                            label = { Text(subj.name) }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showAddSubjectDialog = true },
                        label = { Text("+ New Subject", color = MaterialTheme.colorScheme.tertiary) }
                    )
                }
            }
        },
        confirmButton = {
            BouncyTextButton(onClick = {
                if (name.isNotBlank()) {
                    val computedSchedule = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else schedule
                    viewModel.addCourse(
                        name = name,
                        code = code,
                        colorHex = selectedColor,
                        scheduleDays = selectedDaysList.joinToString(","),
                        scheduleStartTime = startTime,
                        scheduleEndTime = endTime,
                        instructor = instructor,
                        schedule = computedSchedule,
                        description = description,
                        subjectId = selectedSubjectIds.firstOrNull(),
                        tags = tags,
                        subjectIds = selectedSubjectIds.joinToString(",")
                    )
                    onDismiss()
                }
            }) { Text("Add") }
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
