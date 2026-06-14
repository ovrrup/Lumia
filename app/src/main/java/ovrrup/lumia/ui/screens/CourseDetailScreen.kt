package ovrrup.lumia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.*
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    courseId: Int
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val course = remember(courses, courseId) { courses.find { it.id == courseId } }

    val assignmentsFlow = remember(courseId) { viewModel.getAssignmentsForCourse(courseId) }
    val assignments by assignmentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val attendanceFlow = remember(courseId) { viewModel.getAttendanceForCourse(courseId) }
    val attendanceRecords by attendanceFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val courseNotes = remember(allNotes, courseId) { allNotes.filter { it.courseId == courseId } }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Syllabus/Info, 1: Assignments, 2: Attendance, 3: Notes

    if (course == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            course.name,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Instructor: ${course.instructor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    BouncyIconButton(
                        onClick = {
                            viewModel.deleteCourse(course)
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = "Unenroll", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {}
            ) {
                listOf("Overview", "Assignments", "Attendance", "Notes").forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> { // Overview Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Academic Class Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(course.schedule, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        if (course.description.isNotBlank()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Syllabus & Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(course.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item {
                            // Attendance Status Summary
                            val presentCount = attendanceRecords.count { it.status == "Present" }
                            val totalCount = attendanceRecords.size
                            val percentage = if (totalCount > 0) (presentCount.getPercentOf(totalCount)) else 100

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Attendance Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Present: $presentCount / $totalCount Lectures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$percentage%",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Assignments
                    if (assignments.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Assignment,
                            title = "No Assignments",
                            description = "Enroll assignments to track coursework of this specific class."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(assignments) { assignment ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = assignment.isCompleted,
                                            onCheckedChange = { viewModel.toggleAssignmentCompleted(assignment) }
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                assignment.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                assignment.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Attendance Records Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Register Attendance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BouncyButton(
                                            onClick = { viewModel.addAttendanceRecord(courseId, System.currentTimeMillis(), "Present") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Present")
                                        }
                                        BouncyButton(
                                            onClick = { viewModel.addAttendanceRecord(courseId, System.currentTimeMillis(), "Absent") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Absent")
                                        }
                                    }
                                }
                            }
                        }

                        if (attendanceRecords.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                    Text("No attendance recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(attendanceRecords) { record ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dateString = android.text.format.DateFormat.format("MMM dd, yyyy", record.dateMillis).toString()
                                        Column {
                                            Text(dateString, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Text(record.status, color = if (record.status == "Present") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                        }
                                        BouncyIconButton(
                                            onClick = { viewModel.deleteAttendanceRecord(record) }
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> { // Notes Tab
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (courseNotes.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Rounded.EditNote,
                                title = "No Course Notes",
                                description = "Take offline lectures notes, scribble notes, or assignments draft details.",
                                buttonText = "Write Note",
                                onClick = { showAddNoteDialog = true }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(courseNotes) { note ->
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                note.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val dateString = android.text.format.DateFormat.format("MMM dd, HH:mm", note.dateMillis).toString()
                                                Text(dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                BouncyIconButton(
                                                    onClick = { viewModel.deleteNote(note) },
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

                        BouncyFloatingActionButton(
                            onClick = { showAddNoteDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Rounded.NoteAdd, contentDescription = "Add note")
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Take Course Note") },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Write note content...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (noteContent.isNotBlank()) {
                            viewModel.addNote(
                                content = noteContent,
                                courseId = courseId
                            )
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun Int.getPercentOf(total: Int): Int {
    if (total == 0) return 0
    return ((this.toFloat() / total.toFloat()) * 100f).toInt()
}
