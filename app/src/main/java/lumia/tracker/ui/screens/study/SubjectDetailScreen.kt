package lumia.tracker.ui.screens.study

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Task
import lumia.tracker.model.Topic
import lumia.tracker.model.Note
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(navController: NavController, viewModel: ScholarViewModel, subjectId: Int) {
    val context = LocalContext.current
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val allCourses by viewModel.courses.collectAsStateWithLifecycle()
    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())
    val allAssignments by viewModel.assignments.collectAsStateWithLifecycle()
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()

    // Filter elements
    val linkedCourses = remember(allCourses, subjectId) {
        allCourses.filter { course ->
            val splitIds = course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }
            course.subjectId == subjectId || splitIds.contains(subjectId)
        }
    }
    val subjectNotes = remember(allNotes, subjectId) {
        allNotes.filter { it.subjectId == subjectId }
    }
    val subjectTopics = remember(allTopics, subjectId) {
        allTopics.filter { it.subjectId == subjectId }
    }
    val subjectAssignments = remember(allAssignments, subjectId) {
        allAssignments.filter { it.subjectId == subjectId }
    }
    val subjectTasks = remember(allTasks, subjectId) {
        allTasks.filter { it.subjectId == subjectId }
    }

    // Dialog trigger states
    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLinkCourseDialog by remember { mutableStateOf(false) }

    var topicToEdit by remember { mutableStateOf<Topic?>(null) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var assignmentToEdit by remember { mutableStateOf<PracticeAssignment?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    var expandedMenu by remember { mutableStateOf(false) }

    if (subject == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Subject not found", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Subject") },
                                onClick = {
                                    expandedMenu = false
                                    showEditSubjectDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expandedMenu = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Stats Banner Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Column {
                                Text(subject.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                if (subject.tags.isNotBlank()) {
                                    Text("Tags: ${subject.tags}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Courses", count = linkedCourses.size)
                            val completedTopics = subjectTopics.count { it.isCompleted }
                            StatItem(label = "Topics", count = subjectTopics.size, detail = "$completedTopics Done")
                            val pendingAssignments = subjectAssignments.count { !it.isCompleted }
                            StatItem(label = "Assignments", count = subjectAssignments.size, detail = "$pendingAssignments Left")
                            val pendingTasks = subjectTasks.count { !it.isCompleted }
                            StatItem(label = "Tasks", count = subjectTasks.size, detail = "$pendingTasks Left")
                        }
                    }
                }
            }

            // 1. Linked Courses Section
            item {
                SectionHeader(
                    title = "Linked Courses",
                    icon = Icons.Rounded.Book,
                    onAddClick = { showLinkCourseDialog = true }
                )
            }
            if (linkedCourses.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No linked courses yet. Connect courses to track class schedules and attendance together.",
                        buttonText = "Link Course",
                        onClick = { showLinkCourseDialog = true }
                    )
                }
            } else {
                items(linkedCourses, key = { "course_${it.id}" }) { course ->
                    val color = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                    GlassCard(
                        onClick = { navController.navigate("courseDetail/${course.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (course.code.isNotBlank()) {
                                        Text(course.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            BouncyIconButton(
                                onClick = {
                                    // Unlink Course logic
                                    val currentIds = course.subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != subjectId.toString() }
                                    val updatedIds = currentIds.joinToString(",")
                                    val newMainId = currentIds.firstOrNull()?.toIntOrNull()
                                    viewModel.updateCourse(course.copy(subjectIds = updatedIds, subjectId = newMainId))
                                }
                            ) {
                                Icon(Icons.Rounded.LinkOff, contentDescription = "Unlink Course", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // 2. Study Topics Section
            item {
                SectionHeader(
                    title = "Study Outline & Topics",
                    icon = Icons.Rounded.List,
                    onAddClick = { showAddTopicDialog = true }
                )
            }
            if (subjectTopics.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No study topics added yet. Map out your chapters, concepts, or exam portions here.",
                        buttonText = "Add Topic",
                        onClick = { showAddTopicDialog = true }
                    )
                }
            } else {
                items(subjectTopics, key = { "topic_${it.id}" }) { topic ->
                    var showTopicMenu by remember { mutableStateOf(false) }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTopicCompleted(topic) }) {
                                Icon(
                                    imageVector = if (topic.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (topic.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = topic.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                                if (topic.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tags: ${topic.tags}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Box {
                                IconButton(onClick = { showTopicMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Topic Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showTopicMenu,
                                    onDismissRequest = { showTopicMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Topic") },
                                        onClick = {
                                            showTopicMenu = false
                                            topicToEdit = topic
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Topic", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showTopicMenu = false
                                            viewModel.deleteTopic(topic)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Subject Tasks Section
            item {
                SectionHeader(
                    title = "Tasks",
                    icon = Icons.Rounded.Assignment,
                    onAddClick = { showAddTaskDialog = true }
                )
            }
            if (subjectTasks.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No direct tasks created for this subject yet. Create small actionable steps here.",
                        buttonText = "Add Task",
                        onClick = { showAddTaskDialog = true }
                    )
                }
            } else {
                items(subjectTasks, key = { "task_${it.id}" }) { task ->
                    var showTaskMenu by remember { mutableStateOf(false) }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTaskCompleted(task) }) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(task.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (task.dueDateMillis != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(task.dueDateMillis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { showTaskMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Task Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showTaskMenu,
                                    onDismissRequest = { showTaskMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Task") },
                                        onClick = {
                                            showTaskMenu = false
                                            taskToEdit = task
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Task", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showTaskMenu = false
                                            viewModel.deleteTask(task)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Assignments & Exams Section
            item {
                SectionHeader(
                    title = "Assignments & Exams",
                    icon = Icons.Rounded.Task,
                    onAddClick = { showAddAssignmentDialog = true }
                )
            }
            if (subjectAssignments.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No assignments, homework, or exam preparations found for this subject. Create one now.",
                        buttonText = "Add Assignment",
                        onClick = { showAddAssignmentDialog = true }
                    )
                }
            } else {
                items(subjectAssignments, key = { "assignment_${it.id}" }) { assignment ->
                    var showAssignmentMenu by remember { mutableStateOf(false) }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleAssignmentCompleted(assignment) }) {
                                Icon(
                                    imageVector = if (assignment.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (assignment.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    val catColor = try { Color(android.graphics.Color.parseColor(assignment.categoryColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(catColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(assignment.category, style = MaterialTheme.typography.bodySmall, color = catColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (assignment.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(assignment.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (assignment.dueDateMillis > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(assignment.dueDateMillis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.Alarm, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                                        Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { showAssignmentMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Assignment Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showAssignmentMenu,
                                    onDismissRequest = { showAssignmentMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Assignment") },
                                        onClick = {
                                            showAssignmentMenu = false
                                            assignmentToEdit = assignment
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Assignment", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showAssignmentMenu = false
                                            viewModel.deleteAssignment(assignment)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Subject Notes Section
            item {
                SectionHeader(
                    title = "Subject Notes",
                    icon = Icons.Rounded.EditNote,
                    onAddClick = { showAddNoteDialog = true }
                )
            }
            if (subjectNotes.isEmpty()) {
                item {
                    EmptySectionCard(
                        text = "No quick notes added yet. Keep formulas, code snippets, or definitions safe right inside the subject.",
                        buttonText = "Add Note",
                        onClick = { showAddNoteDialog = true }
                    )
                }
            } else {
                items(subjectNotes, key = { "note_${it.id}" }) { note ->
                    var isExpanded by remember { mutableStateOf(false) }
                    GlassCard(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth().animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                val formattedDate = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()).format(note.dateMillis)
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { noteToEdit = note }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Note", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- DIALOGS ---

    // Edit Subject Dialog
    if (showEditSubjectDialog) {
        EditSubjectDialog(
            subject = subject,
            viewModel = viewModel,
            onDismiss = { showEditSubjectDialog = false }
        )
    }

    // Delete Subject Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Subject?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${subject.name}? All linked topics, notes, tasks, and assignments will stay, but they won't be mapped to this subject anymore.") },
            confirmButton = {
                BouncyTextButton(
                    onClick = {
                        viewModel.deleteSubject(subject)
                        showDeleteConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Link Course Dialog (Multi-select list)
    if (showLinkCourseDialog) {
        AlertDialog(
            onDismissRequest = { showLinkCourseDialog = false },
            title = { Text("Link Courses", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select which courses belong to this subject:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allCourses, key = { it.id }) { course ->
                            val currentIds = course.subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val isLinked = course.subjectId == subjectId || currentIds.contains(subjectId.toString())
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val updatedIds = if (isLinked) {
                                            currentIds.filter { it != subjectId.toString() }
                                        } else {
                                            (currentIds + subjectId.toString()).distinct()
                                        }
                                        val updatedIdsStr = updatedIds.joinToString(",")
                                        val firstId = updatedIds.firstOrNull()?.toIntOrNull()
                                        viewModel.updateCourse(course.copy(subjectIds = updatedIdsStr, subjectId = firstId))
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isLinked,
                                    onCheckedChange = {
                                        val updatedIds = if (isLinked) {
                                            currentIds.filter { it != subjectId.toString() }
                                        } else {
                                            (currentIds + subjectId.toString()).distinct()
                                        }
                                        val updatedIdsStr = updatedIds.joinToString(",")
                                        val firstId = updatedIds.firstOrNull()?.toIntOrNull()
                                        viewModel.updateCourse(course.copy(subjectIds = updatedIdsStr, subjectId = firstId))
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    if (course.code.isNotBlank()) {
                                        Text(course.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = { showLinkCourseDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Add / Edit Topic Dialog
    val currentTopic = topicToEdit
    if (showAddTopicDialog || currentTopic != null) {
        var topicTitle by remember(currentTopic) { mutableStateOf(currentTopic?.title ?: "") }
        var topicTags by remember(currentTopic) { mutableStateOf(currentTopic?.tags ?: "") }
        AlertDialog(
            onDismissRequest = {
                showAddTopicDialog = false
                topicToEdit = null
            },
            title = { Text(if (currentTopic == null) "Add Topic" else "Edit Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topicTags,
                        onValueChange = { topicTags = it },
                        label = { Text("Tags (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (topicTitle.isNotBlank()) {
                        if (currentTopic == null) {
                            viewModel.addTopic(
                                subjectId = subjectId,
                                title = topicTitle,
                                tags = topicTags
                            )
                        } else {
                            viewModel.updateTopic(
                                currentTopic.copy(
                                    title = topicTitle,
                                    tags = topicTags
                                )
                            )
                        }
                    }
                    showAddTopicDialog = false
                    topicToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddTopicDialog = false
                    topicToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Note Dialog
    val currentNote = noteToEdit
    if (showAddNoteDialog || currentNote != null) {
        var noteContent by remember(currentNote) { mutableStateOf(currentNote?.content ?: "") }
        AlertDialog(
            onDismissRequest = {
                showAddNoteDialog = false
                noteToEdit = null
            },
            title = { Text(if (currentNote == null) "Add Note" else "Edit Note") },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (noteContent.isNotBlank()) {
                        if (currentNote == null) {
                            viewModel.addNote(
                                content = noteContent,
                                subjectId = subjectId
                            )
                        } else {
                            viewModel.updateNote(
                                currentNote.copy(
                                    content = noteContent
                                )
                            )
                        }
                    }
                    showAddNoteDialog = false
                    noteToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddNoteDialog = false
                    noteToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Task Dialog
    val currentTask = taskToEdit
    if (showAddTaskDialog || currentTask != null) {
        var taskTitle by remember(currentTask) { mutableStateOf(currentTask?.title ?: "") }
        var taskDescription by remember(currentTask) { mutableStateOf(currentTask?.description ?: "") }
        var taskDueDate by remember(currentTask) { mutableStateOf(currentTask?.dueDateMillis) }
        
        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                taskToEdit = null
            },
            title = { Text(if (currentTask == null) "Add Task" else "Edit Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val dateLabel = if (taskDueDate != null) {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(taskDueDate!!)
                    } else {
                        "No Due Date"
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val cal = Calendar.getInstance()
                                if (taskDueDate != null) cal.timeInMillis = taskDueDate!!
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59)
                                        taskDueDate = selectedCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Due Date", fontWeight = FontWeight.Bold)
                        }
                        Text(dateLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (taskTitle.isNotBlank()) {
                        if (currentTask == null) {
                            viewModel.addTask(
                                Task(
                                    title = taskTitle,
                                    description = taskDescription,
                                    dueDateMillis = taskDueDate,
                                    subjectId = subjectId,
                                    isCompleted = false
                                )
                            )
                        } else {
                            viewModel.updateTask(
                                currentTask.copy(
                                    title = taskTitle,
                                    description = taskDescription,
                                    dueDateMillis = taskDueDate
                                )
                            )
                        }
                    }
                    showAddTaskDialog = false
                    taskToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddTaskDialog = false
                    taskToEdit = null
                }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Assignment Dialog
    val currentAssignment = assignmentToEdit
    if (showAddAssignmentDialog || currentAssignment != null) {
        var assignmentTitle by remember(currentAssignment) { mutableStateOf(currentAssignment?.title ?: "") }
        var assignmentDesc by remember(currentAssignment) { mutableStateOf(currentAssignment?.description ?: "") }
        var assignmentCategory by remember(currentAssignment) { mutableStateOf(currentAssignment?.category ?: "Homework") }
        var assignmentDueDate by remember(currentAssignment) { mutableStateOf(currentAssignment?.dueDateMillis ?: Calendar.getInstance().timeInMillis) }
        var assignmentTags by remember(currentAssignment) { mutableStateOf(currentAssignment?.tags ?: "") }

        val categories = listOf("Homework", "Quiz", "Project", "Exam", "Lab")

        AlertDialog(
            onDismissRequest = {
                showAddAssignmentDialog = false
                assignmentToEdit = null
            },
            title = { Text(if (currentAssignment == null) "Add Assignment" else "Edit Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = assignmentTitle,
                        onValueChange = { assignmentTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignmentDesc,
                        onValueChange = { assignmentDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignmentTags,
                        onValueChange = { assignmentTags = it },
                        label = { Text("Tags (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = assignmentCategory == cat
                            val color = when (cat) {
                                "Homework" -> Color(0xFF3197D6)
                                "Quiz" -> Color(0xFFE91E63)
                                "Project" -> Color(0xFF4CAF50)
                                "Exam" -> Color(0xFFFF5722)
                                else -> Color(0xFF9C27B0)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color else color.copy(alpha = 0.1f))
                                    .clickable { assignmentCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color.White else color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val dateLabel = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(assignmentDueDate)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = assignmentDueDate
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59)
                                        assignmentDueDate = selectedCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Due Date", fontWeight = FontWeight.Bold)
                        }
                        Text(dateLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = {
                    if (assignmentTitle.isNotBlank()) {
                        val categoryColor = when (assignmentCategory) {
                            "Homework" -> "#3197D6"
                            "Quiz" -> "#E91E63"
                            "Project" -> "#4CAF50"
                            "Exam" -> "#FF5722"
                            else -> "#9C27B0"
                        }
                        // Default to first linked course, or 0 if none
                        val courseId = linkedCourses.firstOrNull()?.id ?: 0

                        if (currentAssignment == null) {
                            viewModel.addAssignment(
                                courseId = courseId,
                                title = assignmentTitle,
                                desc = assignmentDesc,
                                dueDate = assignmentDueDate,
                                category = assignmentCategory,
                                categoryColor = categoryColor,
                                tags = assignmentTags,
                                subjectId = subjectId
                            )
                        } else {
                            viewModel.updateAssignmentDetails(
                                currentAssignment.copy(
                                    title = assignmentTitle,
                                    description = assignmentDesc,
                                    category = assignmentCategory,
                                    categoryColor = categoryColor,
                                    dueDateMillis = assignmentDueDate,
                                    tags = assignmentTags,
                                    courseId = if (currentAssignment.courseId > 0) currentAssignment.courseId else courseId
                                )
                            )
                        }
                    }
                    showAddAssignmentDialog = false
                    assignmentToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = {
                    showAddAssignmentDialog = false
                    assignmentToEdit = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
        BouncyIconButton(
            onClick = onAddClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, detail: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun EmptySectionCard(
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BouncyTextButton(onClick = onClick) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
