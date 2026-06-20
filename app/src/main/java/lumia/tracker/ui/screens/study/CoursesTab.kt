package lumia.tracker.ui.screens

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
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.bouncyScale
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CoursesTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditCourse: (Course) -> Unit,
    onAddCourseClick: () -> Unit
) {
    var courseToEdit by remember { mutableStateOf<lumia.tracker.model.Course?>(null) }
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            val src = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            lumia.tracker.ui.components.BouncyFloatingActionButton(
                onClick = onAddCourseClick,
                interactionSource = src,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding()).bouncyScale(src)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, 
                top = bottomPadding.calculateTopPadding() + 16.dp, bottom = bottomPadding.calculateBottomPadding() + 16.dp
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
                            val colorCourse = remember(course.colorHex) {
                                try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { null }
                            }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(colorCourse ?: MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = if (colorCourse != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val courseDisplayTitle = if (course.code.isNotBlank()) "${course.code} – ${course.name}" else course.name
                                Text(
                                    text = courseDisplayTitle,
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
                                BouncyIconButton(onClick = { expanded = true }) {
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
        var code by remember(courseToEdit) { mutableStateOf(courseToEdit?.code ?: "") }
        var selectedColor by remember(courseToEdit) { mutableStateOf(courseToEdit?.colorHex ?: "#3197D6") }
        var selectedDaysList by remember(courseToEdit) { mutableStateOf(courseToEdit?.scheduleDays?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList<String>()) }
        var startTime by remember(courseToEdit) { mutableStateOf(courseToEdit?.scheduleStartTime ?: "") }
        var endTime by remember(courseToEdit) { mutableStateOf(courseToEdit?.scheduleEndTime ?: "") }
        var pickerTargetIsStart by remember { mutableStateOf(true) }

        var instructor by remember(courseToEdit) { mutableStateOf(courseToEdit?.instructor ?: "") }
        var schedule by remember(courseToEdit) { mutableStateOf(courseToEdit?.schedule ?: "") }
        var description by remember(courseToEdit) { mutableStateOf(courseToEdit?.description ?: "") }
        var tags by remember(courseToEdit) { mutableStateOf(courseToEdit?.tags ?: "") }
        var showTimePicker by remember { mutableStateOf(false) }
        var selectedSubjectId by remember(courseToEdit) { mutableStateOf<Int?>(courseToEdit?.subjectId) }

        val daysOfWeekList = remember { listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday") }
        val colorsList = remember { listOf("#3197D6", "#2ECC71", "#E74C3C", "#F1C40F", "#9B59B6", "#E67E22", "#34495E") }

        if (showTimePicker) {
            val initialHour = remember {
                try {
                    val activeStr = if (pickerTargetIsStart) startTime else endTime
                    var parsedHour = activeStr.substringBefore(":").toInt()
                    if (activeStr.contains("PM", ignoreCase = true) && parsedHour < 12) parsedHour += 12
                    if (activeStr.contains("AM", ignoreCase = true) && parsedHour == 12) parsedHour = 0
                    parsedHour
                } catch (e: Exception) { 12 }
            }
            val initialMinute = remember {
                try {
                    val activeStr = if (pickerTargetIsStart) startTime else endTime
                    activeStr.substringAfter(":").substringBefore(" ").toInt()
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
                        val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", formatHour, minute, amPm)
                        if (pickerTargetIsStart) {
                            startTime = formattedTime
                        } else {
                            endTime = formattedTime
                        }
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
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Course Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Course Color Tag", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorsList.forEach { col ->
                            val c = Color(android.graphics.Color.parseColor(col))
                            val isSelected = selectedColor.equals(col, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = col }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Schedule Days", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        daysOfWeekList.forEach { day ->
                            val isSelected = selectedDaysList.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDaysList = if (isSelected) {
                                        selectedDaysList - day
                                    } else {
                                        selectedDaysList + day
                                    }
                                },
                                label = { Text(day.substring(0, 3)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Schedule Time Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { 
                                pickerTargetIsStart = true
                                showTimePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (startTime.isBlank()) "Start Time" else "Start: $startTime")
                        }
                        Button(
                            onClick = { 
                                pickerTargetIsStart = false
                                showTimePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (endTime.isBlank()) "End Time" else "End: $endTime")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

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
                BouncyTextButton(onClick = {
                    if (name.isNotBlank()) {
                        val computedSchedule = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime - $endTime" else schedule
                        courseToEdit?.copy(
                            name = name,
                            code = code,
                            colorHex = selectedColor,
                            scheduleDays = selectedDaysList.joinToString(","),
                            scheduleStartTime = startTime,
                            scheduleEndTime = endTime,
                            instructor = instructor,
                            schedule = computedSchedule,
                            description = description,
                            subjectId = selectedSubjectId,
                            tags = tags
                        )?.let { viewModel.updateCourse(it) }
                        courseToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { courseToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
