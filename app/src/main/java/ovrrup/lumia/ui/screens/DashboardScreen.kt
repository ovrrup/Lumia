package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.*
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Courses", "Tasks", "Assignments", "Analytics")

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LUMIA ACADEMIC",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$currentStreak Days",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    BouncyIconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier.height(72.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            val icon = when (index) {
                                0 -> Icons.Rounded.Book
                                1 -> Icons.Rounded.CheckCircleOutline
                                2 -> Icons.Rounded.Assignment
                                else -> Icons.Rounded.BarChart
                            }
                            Icon(icon, contentDescription = title)
                        },
                        label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab < 3) {
                BouncyFloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> showAddCourseDialog = true
                            1 -> showAddTaskDialog = true
                            2 -> showAddAssignmentDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create New")
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> { // Courses Tab
                    if (courses.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.MenuBook,
                            title = "No Courses Enrolled",
                            description = "Start by enrolling in a university course to track tasks and notes.",
                            buttonText = "Add First Course",
                            onClick = { showAddCourseDialog = true }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(courses) { course ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("courseDetail/${course.id}") }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Instructor: ${course.instructor}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Schedule: ${course.schedule}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (course.tags.isNotBlank()) {
                                            Spacer(Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                course.tags.split(",").forEach { tag ->
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                MaterialTheme.colorScheme.secondaryContainer,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            tag.trim(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Tasks Tab
                    if (tasks.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.FactCheck,
                            title = "All Done!",
                            description = "You have no active tasks currently. Relax or schedule a study session.",
                            buttonText = "Add a Task",
                            onClick = { showAddTaskDialog = true }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(tasks) { task ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.isCompleted,
                                            onCheckedChange = { viewModel.toggleTaskCompleted(task) }
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (task.description.isNotBlank()) {
                                                Text(
                                                    text = task.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        BouncyIconButton(onClick = { viewModel.deleteTask(task) }) {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Assignments Tab
                    if (assignments.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Task,
                            title = "No Assignments",
                            description = "Keep your university essays, lab writeups, and homework in checklist focus.",
                            buttonText = "Add Assignment",
                            onClick = { showAddAssignmentDialog = true }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(assignments) { assignment ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = assignment.isCompleted,
                                                    onCheckedChange = { viewModel.toggleAssignmentCompleted(assignment) }
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = assignment.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Color(android.graphics.Color.parseColor(assignment.categoryColor)).copy(alpha = 0.15f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    assignment.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(android.graphics.Color.parseColor(assignment.categoryColor))
                                                )
                                            }
                                        }
                                        if (assignment.description.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                assignment.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dateString = if (assignment.dueDateMillis > 0) {
                                                android.text.format.DateFormat.format("MMM dd, yyyy", assignment.dueDateMillis).toString()
                                            } else {
                                                "No Due Date"
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.CalendarToday,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "Due: $dateString",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            BouncyIconButton(
                                                onClick = { viewModel.deleteAssignment(assignment) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> { // Analytics Tab (uses pre-built beautiful AnalyticsTab)
                    AnalyticsTab(
                        viewModel = viewModel,
                        paddingValues = PaddingValues(top = 0.dp, bottom = 0.dp)
                    )
                }
            }
        }
    }

    // Add Course Dialog
    if (showAddCourseDialog) {
        var courseName by remember { mutableStateOf("") }
        var instructorName by remember { mutableStateOf("") }
        var scheduleString by remember { mutableStateOf("") }
        var courseDesc by remember { mutableStateOf("") }
        var courseTags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Enroll in Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instructorName,
                        onValueChange = { instructorName = it },
                        label = { Text("Instructor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scheduleString,
                        onValueChange = { scheduleString = it },
                        label = { Text("Schedule (e.g. Mon/Wed 10:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = courseDesc,
                        onValueChange = { courseDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = courseTags,
                        onValueChange = { courseTags = it },
                        label = { Text("Commas separated tags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (courseName.isNotBlank()) {
                            viewModel.addCourse(
                                name = courseName,
                                instructor = instructorName,
                                schedule = scheduleString,
                                description = courseDesc,
                                tags = courseTags
                            )
                            showAddCourseDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Schedule Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Notes / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(
                                task = Task(
                                    title = taskTitle,
                                    description = taskDesc,
                                    dueDateMillis = System.currentTimeMillis() + 86400000L // default tomorrow
                                )
                            )
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Assignment Dialog
    if (showAddAssignmentDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Homework") }
        val categoryOptions = listOf("Homework", "Exam", "Lab writeup", "Project")

        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("Create Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Assignment Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryOptions.forEach { option ->
                            FilterChip(
                                selected = category == option,
                                onClick = { category = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (title.isNotBlank() && courses.isNotEmpty()) {
                            viewModel.addAssignment(
                                courseId = courses.first().id,
                                title = title,
                                desc = description,
                                dueDate = System.currentTimeMillis() + (86400000L * 3), // default 3 days
                                category = category,
                                categoryColor = when (category) {
                                    "Exam" -> "#F14668"
                                    "Lab writeup" -> "#00A859"
                                    "Project" -> "#9B51E0"
                                    else -> "#3197D6"
                                }
                            )
                            showAddAssignmentDialog = false
                        } else if (courses.isEmpty()) {
                            android.widget.Toast.makeText(context, "Enroll in a course first!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddAssignmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            if (buttonText != null && onClick != null) {
                Spacer(Modifier.height(24.dp))
                BouncyButton(onClick = onClick) {
                    Text(buttonText)
                }
            }
        }
    }
}
