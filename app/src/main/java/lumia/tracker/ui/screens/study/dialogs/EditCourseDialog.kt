package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 85,
    importance = Importance.HIGH,
    description = "Standardized modal dialog for editing existing academic courses",
    category = "Dialog"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseDialog(
    course: Course,
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(course) { mutableStateOf(course.name) }
    var code by remember(course) { mutableStateOf(course.code) }
    var selectedColor by remember(course) { mutableStateOf(course.colorHex) }
    var selectedDaysList by remember(course) { mutableStateOf(course.scheduleDays.split(",").map { it.trim() }.filter { it.isNotBlank() }) }
    var startTime by remember(course) { mutableStateOf(course.scheduleStartTime) }
    var endTime by remember(course) { mutableStateOf(course.scheduleEndTime) }
    var pickerTargetIsStart by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    var instructor by remember(course) { mutableStateOf(course.instructor) }
    var description by remember(course) { mutableStateOf(course.description) }
    var tags by remember(course) { mutableStateOf(course.tags) }
    var selectedSubjectIds by remember(course) {
        mutableStateOf(
            course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().let { ids ->
                if (ids.isEmpty() && course.subjectId != null) setOf(course.subjectId) else ids
            }
        )
    }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            StudyDialogHeader(
                icon = Icons.Rounded.EditNote,
                title = "Edit Course",
                subtitle = "Update schedule, color, and subject links",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
                    placeholder = { Text("e.g. CS101") },
                    leadingIcon = {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Course Name *") },
                    placeholder = { Text("Course Name") },
                    leadingIcon = {
                        Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    isError = nameTouched && name.isBlank(),
                    supportingText = if (nameTouched && name.isBlank()) {
                        { Text("Course name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor (Optional)") },
                    placeholder = { Text("Instructor Name") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Course Color Palette
                ColorPickerPalette(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    title = "Course Color Theme"
                )

                // Weekday Selector Chips
                WeekdaySelectorChips(
                    selectedDays = selectedDaysList,
                    onDaysSelectedChange = { selectedDaysList = it },
                    title = "Schedule Days"
                )

                // Schedule Time Range Picker
                ScheduleTimeRangePicker(
                    startTime = startTime,
                    endTime = endTime,
                    onStartTimeClick = {
                        pickerTargetIsStart = true
                        showTimePicker = true
                    },
                    onEndTimeClick = {
                        pickerTargetIsStart = false
                        showTimePicker = true
                    },
                    title = "Schedule Time Range"
                )

                // Description & Tags
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Link to Subject(s)
                SubjectSelectorChips(
                    subjects = subjects,
                    selectedSubjectIds = selectedSubjectIds,
                    onSubjectSelectionChanged = { selectedSubjectIds = it },
                    onAddNewSubjectClick = { showAddSubjectDialog = true },
                    title = "Link to Study Subject (Optional)"
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val computedSchedule = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else course.schedule
                        val updatedCourse = course.copy(
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
                        viewModel.updateCourse(updatedCourse)
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
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showTimePicker) {
        StudyTimePickerDialog(
            initialTime = if (pickerTargetIsStart) startTime else endTime,
            onTimeSelected = { formattedTime ->
                if (pickerTargetIsStart) {
                    startTime = formattedTime
                } else {
                    endTime = formattedTime
                }
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            viewModel = viewModel,
            onDismiss = { showAddSubjectDialog = false }
        )
    }
}
