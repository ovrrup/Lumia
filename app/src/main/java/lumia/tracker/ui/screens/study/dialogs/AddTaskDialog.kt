package lumia.tracker.ui.screens.study.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Task
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AddTaskDialog - Redesigned modern modal for adding or editing tasks with
 * priorities, deadline picker, tags, and subject/course linkages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    viewModel: ScholarViewModel,
    taskToEdit: Task? = null,
    initialSubjectId: Int? = null,
    initialCourseId: Int? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = taskToEdit != null
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var tags by remember { mutableStateOf(taskToEdit?.tags ?: "") }
    var priority by remember { mutableIntStateOf(taskToEdit?.priority ?: 0) }
    var selectedSubjectId by remember { mutableStateOf(taskToEdit?.subjectId ?: initialSubjectId) }
    var selectedCourseId by remember { mutableStateOf(taskToEdit?.courseId ?: initialCourseId) }
    var selectedAssignmentId by remember { mutableStateOf(taskToEdit?.assignmentId) }
    var dueDateMillis by remember { mutableStateOf(taskToEdit?.dueDateMillis) }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                dueDateMillis = calendar.timeInMillis
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

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
                            imageVector = if (isEdit) Icons.Rounded.EditCalendar else Icons.Rounded.AddTask,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        text = "Manage priority, deadline, and links",
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

                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Complete Problem Set 3") },
                    leadingIcon = {
                        Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Add instructions, page numbers, or submission notes") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    placeholder = { Text("homework, exam, revision") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Date Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Due Date / Deadline",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (dueDateMillis != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            Text(
                                text = if (dueDateMillis != null) df.format(Date(dueDateMillis ?: 0L)) else "Set Deadline Date",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (dueDateMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (dueDateMillis != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (dueDateMillis != null) {
                                BouncyIconButton(
                                    onClick = { dueDateMillis = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Cancel,
                                        contentDescription = "Clear Deadline",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Low
                        FilterChip(
                            selected = priority == 0,
                            onClick = { priority = 0 },
                            label = { Text("Low", fontWeight = if (priority == 0) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        // Medium
                        FilterChip(
                            selected = priority == 1,
                            onClick = { priority = 1 },
                            label = { Text("Medium", fontWeight = if (priority == 1) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        // High
                        FilterChip(
                            selected = priority == 2,
                            onClick = { priority = 2 },
                            label = { Text("High", fontWeight = if (priority == 2) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Rounded.PriorityHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Link with Subject
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Link with Subject",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("None") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(subj.name) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Advanced Task Course & Assignment Linkages
                if (advancedTasks) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Link with Course",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCourseId == null,
                                    onClick = { selectedCourseId = null },
                                    label = { Text("None") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            items(courses) { course ->
                                FilterChip(
                                    selected = selectedCourseId == course.id,
                                    onClick = { selectedCourseId = course.id },
                                    label = { Text(course.name) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    val courseAssignments = if (selectedCourseId != null) assignments.filter { it.courseId == selectedCourseId } else assignments
                    if (courseAssignments.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Link with Assignment",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedAssignmentId == null,
                                        onClick = { selectedAssignmentId = null },
                                        label = { Text("None") },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                items(courseAssignments) { assignment ->
                                    FilterChip(
                                        selected = selectedAssignmentId == assignment.id,
                                        onClick = { selectedAssignmentId = assignment.id },
                                        label = { Text(assignment.title) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            BouncyButton(
                onClick = {
                    if (title.isNotBlank()) {
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
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isEdit) Icons.Rounded.Save else Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(if (isEdit) "Save Changes" else "Add Task", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
