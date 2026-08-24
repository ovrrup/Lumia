package lumia.tracker.ui.screens.study.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Task
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AddTaskDialog - Unified modal for adding or editing tasks with
 * priorities, deadline picker, tags, and subject/course linkage.
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
        title = { Text(if (isEdit) "Edit Task" else "Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    placeholder = { Text("homework, exam, revision") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Due Date:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showDatePicker = true }) {
                        val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(if (dueDateMillis != null) df.format(Date(dueDateMillis ?: System.currentTimeMillis())) else "Set Deadline")
                    }
                    if (dueDateMillis != null) {
                        IconButton(onClick = { dueDateMillis = null }) {
                            Icon(Icons.Rounded.Cancel, contentDescription = "Clear Date", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Text("Priority:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = priority == 0,
                            onClick = { priority = 0 },
                            label = { Text("Low") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = priority == 1,
                            onClick = { priority = 1 },
                            label = { Text("Medium") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = priority == 2,
                            onClick = { priority = 2 },
                            label = { Text("High") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }

                Text("Link with Subject:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedSubjectId == null,
                            onClick = { selectedSubjectId = null },
                            label = { Text("None") }
                        )
                    }
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedSubjectId == subj.id,
                            onClick = { selectedSubjectId = subj.id },
                            label = { Text(subj.name) }
                        )
                    }
                }

                if (advancedTasks) {
                    Text("Link with Course:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCourseId == null,
                                onClick = { selectedCourseId = null },
                                label = { Text("None") }
                            )
                        }
                        items(courses) { course ->
                            FilterChip(
                                selected = selectedCourseId == course.id,
                                onClick = { selectedCourseId = course.id },
                                label = { Text(course.name) }
                            )
                        }
                    }

                    val courseAssignments = if (selectedCourseId != null) assignments.filter { it.courseId == selectedCourseId } else assignments
                    if (courseAssignments.isNotEmpty()) {
                        Text("Link with Assignment:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedAssignmentId == null,
                                    onClick = { selectedAssignmentId = null },
                                    label = { Text("None") }
                                )
                            }
                            items(courseAssignments) { assignment ->
                                FilterChip(
                                    selected = selectedAssignmentId == assignment.id,
                                    onClick = { selectedAssignmentId = assignment.id },
                                    label = { Text(assignment.title) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
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
                enabled = title.isNotBlank()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
