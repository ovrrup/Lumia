package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Shape
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
import lumia.tracker.ui.components.ScholarCardDefaults
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
    subtitle: String = "",
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
            border = BorderStroke(0.8.dp, contentColor.copy(alpha = 0.25f)),
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
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Modernized capsule section header for study dialog sections.
 */
@Composable
fun StudyDialogSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 12.dp)
                .background(color, CircleShape)
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (!badgeText.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Reusable dialog confirm button following capsule design tokens.
 */
@Composable
fun StudyDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    BouncyButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Reusable dialog dismiss button following capsule design tokens.
 */
@Composable
fun StudyDialogDismissButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Cancel"
) {
    BouncyTextButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(text)
    }
}

/**
 * Modernized capsule outlined text field adhering to Lumia's capsule design tokens.
 * Single-line fields use CircleShape; multi-line fields use a smooth 20.dp capsule curvature.
 */
@Composable
fun StudyDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    minLines: Int = 1,
    shape: Shape = if (singleLine) CircleShape else RoundedCornerShape(20.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = when {
            leadingContent != null -> leadingContent
            leadingIcon != null -> {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            else -> null
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrBlank()) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        shape = shape,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    )
}

/**
 * Reusable weekday selector flow row of CircleShape capsule pills with specular frosted borders.
 */
@OptIn(ExperimentalLayoutApi::class)
@ValueScore(
    score = 78,
    importance = Importance.HIGH,
    description = "Reusable weekday multi-selector CircleShape capsule pills for course and study scheduling",
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
    val isDark = isSystemInDarkTheme()
    Column(modifier = modifier.fillMaxWidth()) {
        StudyDialogSectionHeader(title = title, icon = Icons.Rounded.DateRange)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            daysList.forEach { day ->
                val isSelected = selectedDays.contains(day)
                val targetBg = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.50f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                }
                val targetBorder = if (isSelected) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                } else {
                    ScholarCardDefaults.glassBorder(isDark = isDark, width = 0.8.dp)
                }
                val targetTextColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    shape = CircleShape,
                    color = targetBg,
                    border = targetBorder,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            val newDays = if (isSelected) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                            onDaysSelectedChange(newDays)
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = targetTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable CircleShape capsule pill color theme picker with specular frosted border and checkmark selection.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Reusable CircleShape capsule pill color theme picker with specular frosted border and checkmark selection",
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
    val isDark = isSystemInDarkTheme()
    Column(modifier = modifier.fillMaxWidth()) {
        StudyDialogSectionHeader(title = title, icon = Icons.Rounded.Palette)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { col ->
                val c = try {
                    Color(android.graphics.Color.parseColor(col))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                val isSelected = selectedColor.equals(col, ignoreCase = true)
                val targetBorder = if (isSelected) {
                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.onSurface)
                } else {
                    ScholarCardDefaults.glassBorder(isDark = isDark, width = 1.dp)
                }
                Surface(
                    shape = CircleShape,
                    color = c,
                    border = targetBorder,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onColorSelected(col) }
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
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
        StudyDialogSectionHeader(title = title, icon = Icons.Rounded.Schedule)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = if (startTime.isNotBlank()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(0.8.dp, if (startTime.isNotBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .clickable { onStartTimeClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (startTime.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (startTime.isBlank()) "Start Time" else startTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (startTime.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = if (endTime.isNotBlank()) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(0.8.dp, if (endTime.isNotBlank()) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .clickable { onEndTimeClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (endTime.isNotBlank()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (endTime.isBlank()) "End Time" else endTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (endTime.isNotBlank()) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
            StudyDialogConfirmButton(
                text = "Set Time",
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val formatHour = if (hour % 12 == 0) 12 else hour % 12
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", formatHour, minute, amPm)
                    onTimeSelected(formattedTime)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            StudyDialogDismissButton(onClick = onDismiss)
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
    title: String = "Link to Subject"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StudyDialogSectionHeader(title = title, icon = Icons.Rounded.AutoStories)
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
                shape = CircleShape
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
                    shape = CircleShape
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
                shape = CircleShape
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
                subtitle = if (isEdit) "Update assignment details" else "Create assignment & deadline",
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

                StudyDialogTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = "Assignment Title *",
                    placeholder = "e.g. Problem Set 4",
                    leadingIcon = Icons.AutoMirrored.Rounded.LibraryBooks,
                    isError = titleTouched && title.isBlank(),
                    errorMessage = "Assignment title is required"
                )

                StudyDialogTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "Description (Optional)",
                    placeholder = "Instructions or reference pages...",
                    leadingIcon = Icons.Rounded.Notes,
                    singleLine = false,
                    maxLines = 3,
                    minLines = 2
                )

                StudyDialogTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags (Optional)",
                    placeholder = "graded, midterm, report",
                    leadingIcon = Icons.Rounded.Tag,
                    singleLine = true
                )

                // Category Presets
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(title = "Category", icon = Icons.Rounded.Category)
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
                                shape = CircleShape
                            )
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(8.dp))
                        StudyDialogTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = "Custom Category",
                            placeholder = "Seminar, Presentation, etc.",
                            singleLine = true
                        )
                    }
                }

                // Category Color Swatches
                ColorPickerPalette(
                    selectedColor = categoryColor,
                    onColorSelected = { categoryColor = it },
                    title = "Color Theme",
                    colors = ASSIGNMENT_COLOR_PALETTE
                )

                // Link to Study Subject
                if (subjects.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        StudyDialogSectionHeader(title = "Subject", icon = Icons.Rounded.AutoStories)
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
                                shape = CircleShape
                            )
                            subjects.forEach { subj ->
                                val isSelected = selectedSubjectId == subj.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubjectId = if (isSelected) null else subj.id },
                                    label = { Text(subj.name) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                // Course selector if multiple courses available
                if (courses.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        StudyDialogSectionHeader(title = "Course", icon = Icons.Rounded.School)
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
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                // Due Date Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(title = "Deadline", icon = Icons.Rounded.Event)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            Text(
                                text = "Due: ${dateFormat.format(Date(dueDateMillis))}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            StudyDialogConfirmButton(
                text = if (isEdit) "Save Changes" else "Add Assignment",
                icon = if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add,
                enabled = title.isNotBlank(),
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
                }
            )
        },
        dismissButton = {
            StudyDialogDismissButton(onClick = onDismiss)
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
                subtitle = if (isEdit) "Update note content" else "Capture key takeaways",
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

                StudyDialogTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        contentTouched = true
                    },
                    label = "Note Content *",
                    placeholder = "Key points, formulas, summaries...",
                    leadingIcon = Icons.Rounded.Notes,
                    isError = contentTouched && content.isBlank(),
                    errorMessage = "Note content is required",
                    singleLine = false,
                    minLines = 4
                )

                StudyDialogTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = "Concept Tag",
                    placeholder = "Theory, Lab, Formula, etc.",
                    leadingIcon = Icons.Rounded.Tag,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            StudyDialogConfirmButton(
                text = if (isEdit) "Save Changes" else "Add Note",
                icon = if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add,
                enabled = content.isNotBlank(),
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(content.trim(), tag.trim().ifBlank { "Theory" })
                        onDismiss()
                    }
                }
            )
        },
        dismissButton = {
            StudyDialogDismissButton(onClick = onDismiss)
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
                title = "Study Guide PDF",
                subtitle = "Compile booklet for $targetName",
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

                StudyDialogTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = "Document Title *",
                    placeholder = "Study Guide - $targetName",
                    leadingIcon = Icons.Rounded.Description,
                    isError = titleTouched && title.isBlank(),
                    errorMessage = "Document title is required",
                    singleLine = true
                )

                StudyDialogTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = "Content",
                    placeholder = "Notes, topic checklist, and key ideas...",
                    leadingIcon = Icons.Rounded.Article,
                    singleLine = false,
                    minLines = 6
                )
            }
        },
        confirmButton = {
            StudyDialogConfirmButton(
                text = "Generate PDF",
                icon = Icons.Rounded.PictureAsPdf,
                enabled = title.isNotBlank() && content.isNotBlank(),
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onGenerate(title.trim(), content.trim())
                        onDismiss()
                    }
                }
            )
        },
        dismissButton = {
            StudyDialogDismissButton(onClick = onDismiss)
        }
    )
}
