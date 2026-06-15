package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import ovrrup.lumia.ui.theme.bouncyScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.Task
import ovrrup.lumia.ui.components.GlassCard
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SelfStudyTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val advancedTasks by viewModel.systemAdvancedTasks.collectAsStateWithLifecycle()
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var groupBy by remember { mutableStateOf("None") }

    var localTasks by remember(tasks) { mutableStateOf(tasks) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            localTasks = localTasks.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        canDragOver = { draggedOver, _ -> localTasks.any { it.id == draggedOver.key } }
    )
    
    // Save when drag finishes
    LaunchedEffect(reorderableState.draggingItemKey) {
        if (reorderableState.draggingItemKey == null && localTasks != tasks) {
            val updatedTasks = localTasks.mapIndexed { index, task -> task.copy(orderIndex = index, priority = if (groupBy == "Priority") task.priority else task.priority) }
            viewModel.updateTasksOrder(updatedTasks)
        }
    }

    val upcomingAssignments = assignments.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
    val pendingTasks = tasks.filter { !it.isCompleted && (it.dueDateMillis == null || it.dueDateMillis < System.currentTimeMillis()) }
    val futureTasks = tasks.filter { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis > System.currentTimeMillis() }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    // Sleek divider line for clean separation and anchoring
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Self Study & Tasks", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
        floatingActionButton = {
            val src = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding()).bouncyScale(src)
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().reorderable(reorderableState),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, 
                top = padding.calculateTopPadding() + 16.dp, bottom = bottomPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ovrrup.lumia.ui.components.NotificationPermissionPanel()
                ovrrup.lumia.ui.components.ExactAlarmPermissionPanel()
            }
            item {
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassCard(modifier = Modifier.weight(1f).aspectRatio(1f), shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(upcomingAssignments.size.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(8.dp))
                            Text("Upcoming Assignments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    GlassCard(modifier = Modifier.weight(1f).aspectRatio(1f), shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(pendingTasks.size.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Text("Pending Tasks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    GlassCard(modifier = Modifier.weight(1f).aspectRatio(1f), shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(futureTasks.size.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("Future Tasks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        navController.navigate("pomodoro")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pomodoro Timer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Focus and link sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (advancedTasks && tasks.isNotEmpty()) {
                        var expandSort by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expandSort = true }) {
                                Text("Group: $groupBy")
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = expandSort, onDismissRequest = { expandSort = false }) {
                                DropdownMenuItem(text = { Text("None") }, onClick = { groupBy = "None"; expandSort = false })
                                DropdownMenuItem(text = { Text("Tags") }, onClick = { groupBy = "Tags"; expandSort = false })
                                DropdownMenuItem(text = { Text("Priority") }, onClick = { groupBy = "Priority"; expandSort = false })
                            }
                        }
                    }
                }
            }

            if (localTasks.isEmpty()) {
                item {
                    Text("No tasks added. Add a Task to manage your future study plans.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                if (!advancedTasks || groupBy == "None") {
                    items(localTasks, key = { it.id }) { task ->
                        ReorderableItem(reorderableState, key = task.id) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp
                            TaskItemCard(
                                task = task, 
                                viewModel = viewModel, 
                                onEdit = { taskToEdit = task },
                                modifier = Modifier.detectReorderAfterLongPress(reorderableState)
                            )
                        }
                    }
                } else if (groupBy == "Tags") {
                    val grouped = localTasks.groupBy { if (it.tags.isBlank()) "Uncategorized" else it.tags.split(",")[0].trim() }
                    grouped.forEach { (tag, tTasks) ->
                        item {
                            Text(tag, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(tTasks, key = { it.id }) { task ->
                            TaskItemCard(task = task, viewModel = viewModel, onEdit = { taskToEdit = task })
                        }
                    }
                } else if (groupBy == "Priority") {
                    val grouped = localTasks.groupBy { when(it.priority) { 2 -> "High Priority"; 1 -> "Medium Priority"; else -> "Low Priority" } }
                    listOf("High Priority", "Medium Priority", "Low Priority").forEach { pLabel ->
                        val tTasks = grouped[pLabel]
                        if (!tTasks.isNullOrEmpty()) {
                            item {
                                Text(pLabel, style = MaterialTheme.typography.labelLarge, color = if (pLabel.startsWith("High")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 8.dp))
                            }
                            items(tTasks, key = { it.id }) { task ->
                                TaskItemCard(task = task, viewModel = viewModel, onEdit = { taskToEdit = task })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog || taskToEdit != null) {
        val isEdit = taskToEdit != null
        var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
        var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
        var tags by remember { mutableStateOf(taskToEdit?.tags ?: "") }
        var priority by remember { mutableStateOf(taskToEdit?.priority ?: 0) }
        var dueDateMillis by remember { mutableStateOf(taskToEdit?.dueDateMillis) }
        var showDatePicker by remember { mutableStateOf(false) }
        
        // We can add advanced fields in a real app, here we stick to basic and some interconnections
        val subjects by viewModel.subjects.collectAsStateWithLifecycle()
        var selectedSubjectId by remember { mutableStateOf<Int?>(taskToEdit?.subjectId) }
        
        val courses by viewModel.courses.collectAsStateWithLifecycle()
        var selectedCourseId by remember { mutableStateOf<Int?>(taskToEdit?.courseId) }
        
        val assignments by viewModel.assignments.collectAsStateWithLifecycle()
        var selectedAssignmentId by remember { mutableStateOf<Int?>(taskToEdit?.assignmentId) }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = false)
            }
        }

        AlertDialog(
            onDismissRequest = { 
                showAddTaskDialog = false
                taskToEdit = null
            },
            title = { Text(if (isEdit) "Edit Task" else "Add Exclusive Task") },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Due Date:", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { showDatePicker = true }) {
                            val df = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            Text(if (dueDateMillis != null) df.format(java.util.Date(dueDateMillis!!)) else "Not set")
                        }
                        if (dueDateMillis != null) {
                            var showTimePicker by remember { mutableStateOf(false) }
                            if (showTimePicker) {
                                val timePickerState = androidx.compose.material3.rememberTimePickerState(
                                    initialHour = java.util.Calendar.getInstance().apply { timeInMillis = dueDateMillis!! }.get(java.util.Calendar.HOUR_OF_DAY),
                                    initialMinute = java.util.Calendar.getInstance().apply { timeInMillis = dueDateMillis!! }.get(java.util.Calendar.MINUTE)
                                )
                                androidx.compose.material3.DatePickerDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            val cal = java.util.Calendar.getInstance()
                                            cal.timeInMillis = dueDateMillis!!
                                            cal.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                                            cal.set(java.util.Calendar.MINUTE, timePickerState.minute)
                                            dueDateMillis = cal.timeInMillis
                                            showTimePicker = false
                                        }) { Text("OK") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        androidx.compose.material3.TimePicker(state = timePickerState)
                                    }
                                }
                            }
                            TextButton(onClick = { showTimePicker = true }) {
                                val tf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                Text(tf.format(java.util.Date(dueDateMillis!!)))
                            }
                            IconButton(onClick = { dueDateMillis = null }) {
                                Icon(Icons.Rounded.Cancel, contentDescription = "Clear Date", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Priority:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = priority == 0, onClick = { priority = 0 }, label = { Text("Low") }) }
                        item { FilterChip(selected = priority == 1, onClick = { priority = 1 }, label = { Text("Medium") }, colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer)) }
                        item { FilterChip(selected = priority == 2, onClick = { priority = 2 }, label = { Text("High") }, colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer, selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer)) }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Link with Subject:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(selected = selectedSubjectId == null, onClick = { selectedSubjectId = null }, label = { Text("None") })
                        }
                        items(subjects) { subj ->
                            FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id }, label = { Text(subj.name) })
                        }
                    }
                    if (advancedTasks) {
                        Spacer(Modifier.height(12.dp))
                        Text("Link with Course:", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(selected = selectedCourseId == null, onClick = { selectedCourseId = null }, label = { Text("None") }) }
                            items(courses) { course ->
                                FilterChip(selected = selectedCourseId == course.id, onClick = { selectedCourseId = course.id }, label = { Text(course.name) })
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        val courseAssignments = if (selectedCourseId != null) assignments.filter { it.courseId == selectedCourseId } else assignments
                        Text("Link with Assignment:", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(selected = selectedAssignmentId == null, onClick = { selectedAssignmentId = null }, label = { Text("None") }) }
                            items(courseAssignments) { assignment ->
                                FilterChip(selected = selectedAssignmentId == assignment.id, onClick = { selectedAssignmentId = assignment.id }, label = { Text(assignment.title) })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        if (isEdit) {
                            taskToEdit?.copy(title = title, description = description, subjectId = selectedSubjectId, courseId = selectedCourseId, assignmentId = selectedAssignmentId, tags = tags, priority = priority, dueDateMillis = dueDateMillis)?.let { viewModel.updateTask(it) }
                        } else {
                            viewModel.addTask(Task(title = title, description = description, subjectId = selectedSubjectId, courseId = selectedCourseId, assignmentId = selectedAssignmentId, tags = tags, priority = priority, dueDateMillis = dueDateMillis))
                        }
                        showAddTaskDialog = false
                        taskToEdit = null
                    }
                }) { Text(if (isEdit) "Save" else "Add") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddTaskDialog = false
                    taskToEdit = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TaskItemCard(task: Task, viewModel: ScholarViewModel, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(onClick = onEdit, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { _ ->
                    viewModel.toggleTaskCompleted(task)
                }
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f, fill = false))
                    if (task.priority > 0) {
                        Spacer(Modifier.width(8.dp))
                        val pColor = when (task.priority) { 1 -> MaterialTheme.colorScheme.secondary 2 -> MaterialTheme.colorScheme.error else -> MaterialTheme.colorScheme.onSurfaceVariant }
                        Box(modifier = Modifier.size(10.dp).background(pColor, androidx.compose.foundation.shape.CircleShape))
                    }
                }
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (task.dueDateMillis != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DateRange, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        val df = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
                        Text(df.format(java.util.Date(task.dueDateMillis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Show links if any
                if (task.tags.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Sell, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(task.tags, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (task.subjectId != null || task.courseId != null || task.assignmentId != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        val linkText = listOfNotNull(
                            if (task.subjectId != null) "Subject" else null,
                            if (task.courseId != null) "Course" else null,
                            if (task.assignmentId != null) "Assignment" else null
                        ).joinToString(", ")
                        Text("$linkText Linked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            IconButton(onClick = { viewModel.deleteTask(task) }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
