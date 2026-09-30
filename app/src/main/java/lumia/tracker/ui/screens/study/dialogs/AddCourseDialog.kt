package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Modern gesture-driven modal bottom sheet for creating academic courses with schedule, color palette, and subject links",
    category = "Dialog"
)
@OptIn(ExperimentalMaterial3Api::class)
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
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedSubjectIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header Row: Icon + Title/Subtitle + Circular Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Add Course",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Schedule, color, and subject links",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .bouncyClick(onClick = onDismiss),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Course Identification (Code, Name, Instructor)
                StudyDialogTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Course Code",
                    placeholder = "CS101",
                    leadingIcon = Icons.Rounded.QrCode
                )

                StudyDialogTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = "Course Name *",
                    placeholder = "Course Name",
                    leadingIcon = Icons.Rounded.MenuBook,
                    isError = nameTouched && name.trim().isBlank(),
                    errorMessage = "Course name is required"
                )

                StudyDialogTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = "Instructor (Optional)",
                    placeholder = "Instructor",
                    leadingIcon = Icons.Rounded.Person
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
                StudyDialogTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (Optional)",
                    placeholder = "Notes or details",
                    leadingIcon = Icons.Rounded.Notes,
                    singleLine = false,
                    maxLines = 3
                )

                StudyDialogTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags (Optional)",
                    placeholder = "core, mandatory",
                    leadingIcon = Icons.Rounded.Tag
                )

                // Link to Subject(s)
                SubjectSelectorChips(
                    subjects = subjects,
                    selectedSubjectIds = selectedSubjectIds,
                    onSubjectSelectionChanged = { selectedSubjectIds = it },
                    onAddNewSubjectClick = { showAddSubjectDialog = true },
                    title = "Link to Study Subject (Optional)"
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Bottom Action Button
            BouncyButton(
                onClick = {
                    if (name.trim().isNotBlank()) {
                        val computedSchedule = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else ""
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
                    } else {
                        nameTouched = true
                    }
                },
                enabled = name.trim().isNotBlank(),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Add Course", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

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
