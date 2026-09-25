package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCardDefaults
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
    val isDark = isSystemInDarkTheme()
    val dialogShape = RoundedCornerShape(28.dp)

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

    val accentColor = remember(selectedColor) {
        try {
            Color(android.graphics.Color.parseColor(selectedColor))
        } catch (e: Exception) {
            Color(0xFF3197D6)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = dialogShape,
        containerColor = ScholarCardDefaults.glassContainerColor(isDark, alpha = if (isDark) 0.88f else 0.94f),
        modifier = Modifier.border(
            border = ScholarCardDefaults.glassBorder(isDark, accentColor = accentColor),
            shape = dialogShape
        ),
        tonalElevation = 0.dp,
        title = {
            StudyDialogHeader(
                icon = Icons.Rounded.EditNote,
                title = "Edit Course",
                subtitle = "Schedule, color, and links",
                containerColor = accentColor.copy(alpha = 0.16f),
                contentColor = accentColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Course Code & Name
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    placeholder = { Text("CS101") },
                    leadingIcon = {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    shape = CircleShape,
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
                        Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    isError = nameTouched && name.isBlank(),
                    supportingText = if (nameTouched && name.isBlank()) {
                        { Text("Course name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = CircleShape,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor (Optional)") },
                    placeholder = { Text("Instructor") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = CircleShape,
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
                    placeholder = { Text("Notes or details") },
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
                    placeholder = { Text("core, mandatory") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = CircleShape,
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
                shape = CircleShape
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
