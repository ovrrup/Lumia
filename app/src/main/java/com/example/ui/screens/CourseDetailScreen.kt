package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.ScholarViewModel

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(navController: NavController, viewModel: ScholarViewModel, courseId: Int) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val course = courses.find { it.id == courseId }
    val assignments by viewModel.getAssignmentsForCourse(courseId).collectAsStateWithLifecycle()
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var assignmentToEdit by remember { mutableStateOf<com.example.model.PracticeAssignment?>(null) }

    if (course == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(course.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddAssignmentDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (course.instructor.isNotBlank() || course.schedule.isNotBlank() || course.description.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                if (course.instructor.isNotBlank()) {
                                    Text("Instructor: ${course.instructor}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (course.schedule.isNotBlank()) {
                                    Text("Schedule: ${course.schedule}", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (course.description.isNotBlank()) {
                                    Text(course.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Attendance Card
                item {
                    val attendanceRecords by viewModel.getAttendanceForCourse(courseId).collectAsStateWithLifecycle()
                    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
                    var showAttendanceDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Attendance Tracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Mini Calendar (Last 7 days)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = System.currentTimeMillis()
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                calendar.set(java.util.Calendar.MINUTE, 0)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                val todayMillis = calendar.timeInMillis
                                
                                for (i in 6 downTo 0) {
                                    val dateMillis = todayMillis - (i * 86400000L)
                                    val dateCal = java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }
                                    val dayOfWeek = java.text.SimpleDateFormat("E", java.util.Locale.getDefault()).format(dateCal.time)
                                    val dayOfMonth = java.text.SimpleDateFormat("d", java.util.Locale.getDefault()).format(dateCal.time)
                                    
                                    val record = attendanceRecords.find { it.dateMillis == dateMillis }
                                    val statusColor = when (record?.status) {
                                        "Present" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                        "Absent" -> androidx.compose.ui.graphics.Color(0xFFF44336)
                                        "Late" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                                        "Cancelled" -> androidx.compose.ui.graphics.Color.Gray
                                        else -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedDate = dateMillis
                                                showAttendanceDialog = true
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Text(dayOfWeek.take(1), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(statusColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                dayOfMonth,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (record != null) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val presentCount = attendanceRecords.count { it.status == "Present" || it.status == "Late" }
                            val totalCount = attendanceRecords.size
                            Text(
                                "Attended: $presentCount / $totalCount marked classes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f)
                            )
                        }
                    }

                    if (showAttendanceDialog) {
                        val record = attendanceRecords.find { it.dateMillis == selectedDate }
                        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        AlertDialog(
                            onDismissRequest = { showAttendanceDialog = false },
                            title = { Text("Mark Attendance - ${dateFormat.format(java.util.Date(selectedDate))}") },
                            text = {
                                Column {
                                    val options = listOf("Present", "Absent", "Late", "Cancelled")
                                    options.forEach { option ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (record != null) {
                                                        viewModel.updateAttendanceRecord(record.copy(status = option))
                                                    } else {
                                                        viewModel.addAttendanceRecord(courseId, selectedDate, option)
                                                    }
                                                    showAttendanceDialog = false
                                                }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = record?.status == option,
                                                onClick = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(option, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                    if (record != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        TextButton(
                                            onClick = {
                                                viewModel.deleteAttendanceRecord(record)
                                                showAttendanceDialog = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Clear Record", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showAttendanceDialog = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Assignments",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (assignments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(32.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "No assignments yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    items(assignments, key = { it.id }) { assignment ->
                        val cardColor by androidx.compose.animation.animateColorAsState(
                            if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = assignment.isCompleted,
                                    onCheckedChange = { viewModel.toggleAssignmentCompleted(assignment) }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        assignment.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textDecoration = if(assignment.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    if (assignment.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            assignment.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = { assignmentToEdit = assignment }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteAssignment(assignment) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAssignmentDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var dueDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
        var showDatePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("Add Assignment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        Text("Due: ${dateFormat.format(java.util.Date(dueDateMillis))}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addAssignment(courseId, title, desc, dueDateMillis)
                        showAddAssignmentDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssignmentDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (assignmentToEdit != null) {
        var title by remember { mutableStateOf(assignmentToEdit!!.title) }
        var desc by remember { mutableStateOf(assignmentToEdit!!.description) }
        var dueDateMillis by remember { mutableStateOf(assignmentToEdit!!.dueDateMillis) }
        var showDatePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { assignmentToEdit = null },
            title = { Text("Edit Assignment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        Text("Due: ${dateFormat.format(java.util.Date(dueDateMillis))}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.updateAssignmentDetails(
                            assignmentToEdit!!.copy(
                                title = title,
                                description = desc,
                                dueDateMillis = dueDateMillis
                            )
                        )
                        assignmentToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { assignmentToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
