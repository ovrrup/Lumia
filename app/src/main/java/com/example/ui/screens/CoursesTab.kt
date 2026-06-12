package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.ui.theme.glassBar
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.model.Course
import com.example.ui.components.GlassCard
import com.example.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditCourse: (Course) -> Unit,
    onAddCourseClick: () -> Unit
) {
    var courseToEdit by remember { mutableStateOf<com.example.model.Course?>(null) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                }
                CenterAlignedTopAppBar(
                    title = { Text("Your Courses", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha=0.5f)
                    )
                )
            }
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onAddCourseClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, 
                top = padding.calculateTopPadding() + 16.dp, bottom = bottomPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (courses.isEmpty()) {
                item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No courses yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            items(courses, key = { it.id }) { course ->
                var expanded by remember { mutableStateOf(false) }
                GlassCard(
                    onClick = { navController.navigate("courseDetail/${course.id}") },
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (course.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = course.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            expanded = false
                                            courseToEdit = course
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            expanded = false
                                            viewModel.deleteCourse(course)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
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

    if (courseToEdit != null) {
        var name by remember(courseToEdit) { mutableStateOf(courseToEdit?.name ?: "") }
        var instructor by remember(courseToEdit) { mutableStateOf(courseToEdit?.instructor ?: "") }
        var schedule by remember(courseToEdit) { mutableStateOf(courseToEdit?.schedule ?: "") }
        var description by remember(courseToEdit) { mutableStateOf(courseToEdit?.description ?: "") }
        var tags by remember(courseToEdit) { mutableStateOf(courseToEdit?.tags ?: "") }
        var showTimePicker by remember { mutableStateOf(false) }
        var selectedSubjectId by remember(courseToEdit) { mutableStateOf<Int?>(courseToEdit?.subjectId) }

        if (showTimePicker) {
            val initialHour = remember {
                try {
                    var parsedHour = schedule.substringBefore(":").toInt()
                    if (schedule.contains("PM", ignoreCase = true) && parsedHour < 12) parsedHour += 12
                    if (schedule.contains("AM", ignoreCase = true) && parsedHour == 12) parsedHour = 0
                    parsedHour
                } catch (e: Exception) { 12 }
            }
            val initialMinute = remember {
                try {
                    schedule.substringAfter(":").substringBefore(" ").toInt()
                } catch (e: Exception) { 0 }
            }
            val timePickerState = androidx.compose.material3.rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val amPm = if (hour >= 12) "PM" else "AM"
                        val formatHour = if (hour % 12 == 0) 12 else hour % 12
                        schedule = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", formatHour, minute, amPm)
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    androidx.compose.material3.TimePicker(state = timePickerState)
                }
            )
        }

        AlertDialog(
            onDismissRequest = { courseToEdit = null },
            title = { Text("Edit Course") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (schedule.isEmpty()) "Select Schedule Time" else "Schedule: $schedule")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                    if (subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Link to Study Subject (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                androidx.compose.material3.InputChip(
                                    selected = selectedSubjectId == null,
                                    onClick = { selectedSubjectId = null },
                                    label = { Text("None") }
                                )
                            }
                            items(subjects, key = { "edit_chips_${it.id}" }) { subj ->
                                androidx.compose.material3.InputChip(
                                    selected = selectedSubjectId == subj.id,
                                    onClick = { selectedSubjectId = subj.id },
                                    label = { Text(subj.name) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        courseToEdit?.copy(
                            name = name,
                            instructor = instructor,
                            schedule = schedule,
                            description = description,
                            subjectId = selectedSubjectId,
                            tags = tags
                        )?.let { viewModel.updateCourse(it) }
                        courseToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { courseToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
