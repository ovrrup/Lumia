package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AddTaskDialog - Modern gesture-driven modal bottom sheet for adding or editing tasks with
 * priorities, deadline picker, tags, and subject/course linkages.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Comprehensive gesture-dismissible task bottom sheet with deadline, priority, and linkages",
    category = "Dialog"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    viewModel: ScholarViewModel,
    taskToEdit: Task? = null,
    initialSubjectId: Int? = null,
    initialCourseId: Int? = null,
    onDismiss: () -> Unit
) {
    val isEdit = taskToEdit != null
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var tags by remember { mutableStateOf(taskToEdit?.tags ?: "") }
    var priority by remember { mutableIntStateOf(taskToEdit?.priority ?: 0) }
    var selectedSubjectId by remember { mutableStateOf(taskToEdit?.subjectId ?: initialSubjectId) }
    var selectedCourseId by remember { mutableStateOf(taskToEdit?.courseId ?: initialCourseId) }
    var selectedAssignmentId by remember { mutableStateOf(taskToEdit?.assignmentId) }
    var dueDateMillis by remember { mutableStateOf(taskToEdit?.dueDateMillis) }
    var titleTouched by remember { mutableStateOf(false) }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isEdit) Icons.Rounded.EditCalendar else Icons.Rounded.AddTask,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isEdit) "Edit Task" else "Add New Task",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEdit) "Update task details" else "Create a new study task",
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
                // Task Title
                StudyDialogTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = "Task Title *",
                    placeholder = "What needs to be done?",
                    leadingIcon = Icons.Rounded.TaskAlt,
                    isError = titleTouched && title.trim().isBlank(),
                    errorMessage = "Task title is required",
                    singleLine = true
                )

                // Description
                StudyDialogTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (Optional)",
                    placeholder = "Add instructions or notes...",
                    leadingIcon = Icons.Rounded.Notes,
                    singleLine = false,
                    maxLines = 3,
                    minLines = 2
                )

                // Tags
                StudyDialogTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags (Optional)",
                    placeholder = "homework, exam, revision",
                    leadingIcon = Icons.Rounded.Tag,
                    singleLine = true
                )

                // Deadline / Date Chips
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(
                        title = "Deadline",
                        icon = Icons.Rounded.Event
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val todayStart = remember {
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                    val todayEnd = remember(todayStart) { todayStart + 86400000L }
                    val tomorrowEnd = remember(todayEnd) { todayEnd + 86400000L }

                    val currentDueDate = dueDateMillis
                    val isToday = currentDueDate != null && currentDueDate in todayStart until todayEnd
                    val isTomorrow = currentDueDate != null && currentDueDate in todayEnd until tomorrowEnd
                    val isCustom = currentDueDate != null && !isToday && !isTomorrow

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Today capsule pill
                        Surface(
                            shape = CircleShape,
                            color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = if (isToday) 1.2.dp else 0.8.dp,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .bouncyClick {
                                    dueDateMillis = if (isToday) null else {
                                        Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, 23)
                                            set(Calendar.MINUTE, 59)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Tomorrow capsule pill
                        Surface(
                            shape = CircleShape,
                            color = if (isTomorrow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = if (isTomorrow) 1.2.dp else 0.8.dp,
                                color = if (isTomorrow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .bouncyClick {
                                    dueDateMillis = if (isTomorrow) null else {
                                        Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                            set(Calendar.HOUR_OF_DAY, 23)
                                            set(Calendar.MINUTE, 59)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isTomorrow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tomorrow",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isTomorrow) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTomorrow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Custom Date capsule pill
                        val dateFormatted = remember(dueDateMillis) {
                            if (dueDateMillis != null) SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(dueDateMillis ?: 0L)) else null
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isCustom) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = if (isCustom) 1.2.dp else 0.8.dp,
                                color = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(CircleShape)
                                .bouncyClick { showDatePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCustom) (dateFormatted ?: "Date") else "Pick Date",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                if (dueDateMillis != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear Deadline",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .bouncyClick { dueDateMillis = null }
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Selection Capsule Pills
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(
                        title = "Priority",
                        icon = Icons.Rounded.Flag
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Triple(0, "Low", Icons.Rounded.Flag),
                            Triple(1, "Medium", Icons.Rounded.Flag),
                            Triple(2, "High", Icons.Rounded.PriorityHigh)
                        ).forEach { (pLevel, pName, pIcon) ->
                            val isSelected = priority == pLevel
                            val (selectedBg, selectedBorderColor, selectedTint) = when (pLevel) {
                                2 -> Triple(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.colorScheme.error,
                                    MaterialTheme.colorScheme.onErrorContainer
                                )
                                1 -> Triple(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) selectedBg else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) selectedBorderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .bouncyClick { priority = pLevel }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = pIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) selectedTint else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = pName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) selectedTint else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Link with Subject
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(
                        title = "Subject",
                        icon = Icons.Rounded.AutoStories
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("None") },
                                shape = CircleShape
                            )
                        }
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(subj.name) },
                                shape = CircleShape
                            )
                        }
                    }
                }

                // Advanced Task Course & Assignment Linkages
                if (advancedTasks) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        StudyDialogSectionHeader(
                            title = "Course",
                            icon = Icons.Rounded.School
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCourseId == null,
                                    onClick = { selectedCourseId = null },
                                    label = { Text("None") },
                                    shape = CircleShape
                                )
                            }
                            items(courses) { course ->
                                FilterChip(
                                    selected = selectedCourseId == course.id,
                                    onClick = { selectedCourseId = course.id },
                                    label = { Text(course.name) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }

                    val courseAssignments = if (selectedCourseId != null) assignments.filter { it.courseId == selectedCourseId } else assignments
                    if (courseAssignments.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            StudyDialogSectionHeader(
                                title = "Assignment",
                                icon = Icons.Rounded.Assignment
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedAssignmentId == null,
                                        onClick = { selectedAssignmentId = null },
                                        label = { Text("None") },
                                        shape = CircleShape
                                    )
                                }
                                items(courseAssignments) { assignment ->
                                    FilterChip(
                                        selected = selectedAssignmentId == assignment.id,
                                        onClick = { selectedAssignmentId = assignment.id },
                                        label = { Text(assignment.title) },
                                        shape = CircleShape
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Bottom Action Button
            BouncyButton(
                onClick = {
                    if (title.trim().isNotBlank()) {
                        if (isEdit) {
                            taskToEdit?.copy(
                                title = title.trim(),
                                description = description.trim(),
                                subjectId = selectedSubjectId,
                                courseId = selectedCourseId,
                                assignmentId = selectedAssignmentId,
                                tags = tags.trim(),
                                priority = priority,
                                dueDateMillis = dueDateMillis
                            )?.let { viewModel.updateTask(it) }
                        } else {
                            viewModel.addTask(
                                Task(
                                    title = title.trim(),
                                    description = description.trim(),
                                    subjectId = selectedSubjectId,
                                    courseId = selectedCourseId,
                                    assignmentId = selectedAssignmentId,
                                    tags = tags.trim(),
                                    priority = priority,
                                    dueDateMillis = dueDateMillis
                                )
                            )
                        }
                        onDismiss()
                    } else {
                        titleTouched = true
                    }
                },
                enabled = title.trim().isNotBlank(),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isEdit) "Save Changes" else "Add Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
