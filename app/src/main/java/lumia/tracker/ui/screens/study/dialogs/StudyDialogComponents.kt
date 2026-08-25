package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.model.Course
import lumia.tracker.model.Note
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val STUDY_DAYS_OF_WEEK = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
val STUDY_COLOR_PALETTE = listOf("#3197D6", "#2ECC71", "#E74C3C", "#F1C40F", "#9B59B6", "#E67E22", "#34495E")
val ASSIGNMENT_COLOR_PALETTE = listOf("#E52F28", "#E65100", "#FBC02D", "#2CAF5F", "#3197D6", "#7B2CBF", "#78909C")

/**
 * Standardized dialog header component with circular icon surface and title/subtitle.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
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
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.size(44.dp)
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
        Column(modifier = Modifier.weight(1f)) {
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
    score = 72,
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
    score = 75,
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
                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    label = "color_swatch_border"
                )
                Surface(
                    shape = CircleShape,
                    color = c,
                    border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, animatedBorderColor),
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
    score = 70,
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
    score = 74,
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
    score = 72,
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

/**
 * Standardized Add/Edit Assignment modal dialog with full category presets, color palettes,
 * date picker, subject linking, and form validation.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@ValueScore(
    score = 86,
    importance = Importance.HIGH,
    description = "Standardized modal dialog for adding and editing course and subject assignments",
    category = "Dialog"
)
@Composable
fun AddEditAssignmentDialog(
    assignmentToEdit: PracticeAssignment? = null,
    initialCourseId: Int = 0,
    initialSubjectId: Int? = null,
    subjects: List<Subject> = emptyList(),
    courses: List<Course> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (PracticeAssignment) -> Unit
) {
    val isEdit = assignmentToEdit != null
    var title by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.title ?: "") }
    var desc by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.description ?: "") }
    var dueDateMillis by remember(assignmentToEdit) {
        mutableStateOf(if ((assignmentToEdit?.dueDateMillis ?: 0L) > 0L) assignmentToEdit!!.dueDateMillis else System.currentTimeMillis() + 86400000L)
    }
    var category by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.category ?: "Homework") }
    var categoryColor by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.categoryColor ?: "#3197D6") }
    var tags by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.tags ?: "") }
    var selectedSubjectId by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.subjectId ?: initialSubjectId) }
    var selectedCourseId by remember(assignmentToEdit) { mutableStateOf(if ((assignmentToEdit?.courseId ?: 0) > 0) assignmentToEdit!!.courseId else initialCourseId) }

    var titleTouched by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categoriesPresetList = listOf("Homework", "Exam", "Project", "Quiz", "Lab", "Custom")
    var isCustomCategory by remember { mutableStateOf(!categoriesPresetList.dropLast(1).contains(category)) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                BouncyTextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            StudyDialogHeader(
                icon = if (isEdit) Icons.Rounded.EditNote else Icons.AutoMirrored.Rounded.LibraryBooks,
                title = if (isEdit) "Edit Assignment" else "Add Assignment",
                subtitle = if (isEdit) "Update assignment details & deadline" else "Create assignment, category, & deadline",
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

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = { Text("Assignment Title *") },
                    placeholder = { Text("e.g. Problem Set 4, Research Paper") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Rounded.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    isError = titleTouched && title.isBlank(),
                    supportingText = if (titleTouched && title.isBlank()) {
                        { Text("Assignment title is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Instructions, guidelines, or reference pages") },
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
                    label = { Text("Tags (Optional, comma separated)") },
                    placeholder = { Text("e.g. graded, midterm, report") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Presets
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category Preset",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesPresetList.forEach { cat ->
                            val isSelected = (cat == "Custom" && isCustomCategory) || (cat == category && !isCustomCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "Custom") {
                                        isCustomCategory = true
                                        category = ""
                                    } else {
                                        isCustomCategory = false
                                        category = cat
                                        categoryColor = when (cat) {
                                            "Exam" -> "#E52F28"
                                            "Homework" -> "#3197D6"
                                            "Project" -> "#2CAF5F"
                                            "Quiz" -> "#7B2CBF"
                                            "Lab" -> "#E65100"
                                            else -> "#78909C"
                                        }
                                    }
                                },
                                label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            placeholder = { Text("e.g. Seminar, Presentation") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Category Color Swatches
                ColorPickerPalette(
                    selectedColor = categoryColor,
                    onColorSelected = { categoryColor = it },
                    title = "Category Color Theme",
                    colors = ASSIGNMENT_COLOR_PALETTE
                )

                // Link to Study Subject
                if (subjects.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Link to Study Subject (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("None") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            subjects.forEach { subj ->
                                val isSelected = selectedSubjectId == subj.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubjectId = if (isSelected) null else subj.id },
                                    label = { Text(subj.name) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Course selector if multiple courses available
                if (courses.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Course Linkage",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            courses.forEach { c ->
                                val isSelected = selectedCourseId == c.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCourseId = c.id },
                                    label = { Text(c.name) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Due Date Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Due Date / Deadline",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BouncyOutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(
                            text = "Due Date: ${dateFormat.format(Date(dueDateMillis))}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalCategory = if (isCustomCategory) category.ifBlank { "Other" } else category.ifBlank { "Homework" }
                        val record = assignmentToEdit?.copy(
                            title = title.trim(),
                            description = desc.trim(),
                            dueDateMillis = dueDateMillis,
                            category = finalCategory,
                            categoryColor = categoryColor,
                            tags = tags.trim(),
                            subjectId = selectedSubjectId,
                            courseId = if (selectedCourseId > 0) selectedCourseId else initialCourseId
                        ) ?: PracticeAssignment(
                            courseId = if (selectedCourseId > 0) selectedCourseId else initialCourseId,
                            title = title.trim(),
                            description = desc.trim(),
                            dueDateMillis = dueDateMillis,
                            category = finalCategory,
                            categoryColor = categoryColor,
                            tags = tags.trim(),
                            subjectId = selectedSubjectId
                        )
                        onSave(record)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (isEdit) "Save Changes" else "Add Assignment", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Standardized Add/Edit Note modal dialog.
 */
@ValueScore(
    score = 75,
    importance = Importance.MEDIUM,
    description = "Standardized modal dialog for adding and editing study notes",
    category = "Dialog"
)
@Composable
fun AddEditNoteDialog(
    noteToEdit: Note? = null,
    onDismiss: () -> Unit,
    onSave: (content: String, tag: String) -> Unit
) {
    val isEdit = noteToEdit != null
    var content by remember(noteToEdit) { mutableStateOf(noteToEdit?.content ?: "") }
    var tag by remember(noteToEdit) { mutableStateOf(noteToEdit?.tag ?: "Theory") }
    var contentTouched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            StudyDialogHeader(
                icon = if (isEdit) Icons.Rounded.EditNote else Icons.Rounded.Notes,
                title = if (isEdit) "Edit Note" else "Add Note",
                subtitle = if (isEdit) "Update note contents and concept tag" else "Capture key takeaways and concepts",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        contentTouched = true
                    },
                    label = { Text("Note Content *") },
                    placeholder = { Text("Enter lecture points, formulas, or summaries") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    isError = contentTouched && content.isBlank(),
                    supportingText = if (contentTouched && content.isBlank()) {
                        { Text("Note content is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 4
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Concept Tag (Optional)") },
                    placeholder = { Text("Theory, Lab, Formula, Key Concept") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(content.trim(), tag.trim().ifBlank { "Theory" })
                        onDismiss()
                    }
                },
                enabled = content.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (isEdit) "Save Changes" else "Add Note", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Standardized AI Study Guide & PDF Export modal dialog.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Standardized modal dialog for previewing, customizing, and generating AI PDF study guides",
    category = "Dialog"
)
@Composable
fun AiStudyGuideExportDialog(
    initialTitle: String,
    initialContent: String,
    targetName: String,
    onDismiss: () -> Unit,
    onGenerate: (title: String, content: String) -> Unit
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var content by remember(initialContent) { mutableStateOf(initialContent) }
    var titleTouched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            StudyDialogHeader(
                icon = Icons.Rounded.AutoAwesome,
                title = "Generate Study Guide PDF",
                subtitle = "Compile a formatted PDF study booklet for $targetName",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Text(
                    text = "Review or customize the automatic study booklet content before compiling your PDF slip.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = { Text("Document Title *") },
                    placeholder = { Text("e.g. Study Guide - $targetName") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    isError = titleTouched && title.isBlank(),
                    supportingText = if (titleTouched && title.isBlank()) {
                        { Text("Document title is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Study Guide & Lecture Content") },
                    placeholder = { Text("Study notes, syllabus checklist, and key concepts...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Article, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 6
                )
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onGenerate(title.trim(), content.trim())
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Generate PDF", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
