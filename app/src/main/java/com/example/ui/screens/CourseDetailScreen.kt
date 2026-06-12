package com.example.ui.screens

import com.example.ui.theme.liquidGlass
import com.example.ui.theme.glassBar
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.MoreVert

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
    
    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    // Sleek divider line for clean separation and anchoring
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                }
                LargeTopAppBar(
                    title = { Text(course.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddAssignmentDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                val rotation by androidx.compose.animation.core.animateFloatAsState(targetValue = if (showAddAssignmentDialog) 45f else 0f)
                Icon(Icons.Rounded.Add, contentDescription = "Add Assignment", modifier = Modifier.graphicsLayer { rotationZ = rotation })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (course.instructor.isNotBlank() || course.schedule.isNotBlank() || course.description.isNotBlank()) {
                    item {
                        com.example.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth(),
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
                    var isMonthlyView by remember { mutableStateOf(false) }
                    var displayMonthOffset by remember { mutableIntStateOf(0) }

                    com.example.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Attendance Tracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                IconButton(onClick = { isMonthlyView = !isMonthlyView; displayMonthOffset = 0 }) {
                                    Icon(
                                        imageVector = if (isMonthlyView) Icons.Rounded.ViewModule else Icons.Rounded.DateRange,
                                        contentDescription = "Toggle View",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isMonthlyView) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = System.currentTimeMillis()
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                calendar.add(java.util.Calendar.MONTH, displayMonthOffset)
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                calendar.set(java.util.Calendar.MINUTE, 0)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                val monthStartMillis = calendar.timeInMillis
                                val startDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
                                val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                                val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { displayMonthOffset-- }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                    Text(monthName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    IconButton(onClick = { displayMonthOffset++ }) {
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    daysOfWeek.forEach { day ->
                                        Text(day, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val totalCells = kotlin.math.ceil((daysInMonth + startDayOfWeek) / 7.0).toInt() * 7
                                var renderDay = 1
                                for (row in 0 until (totalCells/7)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        for (col in 0 until 7) {
                                            if (row == 0 && col < startDayOfWeek || renderDay > daysInMonth) {
                                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                            } else {
                                                val dateCal = java.util.Calendar.getInstance().apply { 
                                                    timeInMillis = monthStartMillis 
                                                    set(java.util.Calendar.DAY_OF_MONTH, renderDay)
                                                }
                                                val dateMillis = dateCal.timeInMillis
                                                val displayDay = renderDay
                                                renderDay++
                                                
                                                val record = attendanceRecords.find { it.dateMillis == dateMillis }
                                                val statusColor = when (record?.status) {
                                                    "Present" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                                    "Absent" -> androidx.compose.ui.graphics.Color(0xFFF44336)
                                                    "Late" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                                                    "Cancelled" -> androidx.compose.ui.graphics.Color.Gray
                                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                                }

                                                val todayCal = java.util.Calendar.getInstance()
                                                val isToday = todayCal.get(java.util.Calendar.YEAR) == dateCal.get(java.util.Calendar.YEAR) &&
                                                              todayCal.get(java.util.Calendar.DAY_OF_YEAR) == dateCal.get(java.util.Calendar.DAY_OF_YEAR)
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .padding(2.dp)
                                                        .clip(CircleShape)
                                                        .background(statusColor)
                                                        .then(if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                                        .clickable {
                                                            selectedDate = dateMillis
                                                            showAttendanceDialog = true
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "$displayDay",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal,
                                                        color = if (record != null) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
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
                                        val dateCal = java.util.Calendar.getInstance().apply {
                                            timeInMillis = todayMillis
                                            add(java.util.Calendar.DAY_OF_YEAR, -i)
                                        }
                                        val dateMillis = dateCal.timeInMillis
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
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val cancelled = attendanceRecords.count { it.status == "Cancelled" || it.status == "Holiday" }
                            val effectiveTotal = attendanceRecords.size - cancelled
                            val presentCount = attendanceRecords.count { it.status == "Present" || it.status == "Late" }
                            val attendancePct = if (effectiveTotal > 0) (presentCount * 100) / effectiveTotal else 0
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Attendance: $presentCount / $effectiveTotal classes ($attendancePct%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f)
                                )
                                if (effectiveTotal > 0 && attendancePct < 75) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Rounded.Warning,
                                        contentDescription = "Low Attendance",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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
                    item(key = "assignments_empty") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                            modifier = Modifier.animateItem()
                        ) {
                            com.example.ui.components.GlassCard(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                shape = RoundedCornerShape(32.dp)
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
                                            imageVector = Icons.AutoMirrored.Rounded.LibraryBooks,
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
                    }
                } else {
                    items(assignments, key = { it.id }) { assignment ->
                        val cardColor by androidx.compose.animation.animateColorAsState(
                            if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        )
                        com.example.ui.components.GlassCard(
                            modifier = Modifier.animateItem().fillMaxWidth().animateContentSize(),
                            shape = RoundedCornerShape(24.dp),
                            containerColor = cardColor
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            assignment.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = if(assignment.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (assignment.category.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(assignment.categoryColor)).copy(alpha = 0.15f) } catch(e: Exception) { MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(assignment.categoryColor)).copy(alpha = 0.4f) } catch(e: Exception) { MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = assignment.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(assignment.categoryColor)) } catch(e: Exception) { MaterialTheme.colorScheme.primary }
                                                )
                                            }
                                        }
                                    }
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
                                    if (assignment.dueDateMillis > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DateRange,
                                                contentDescription = "Due Date",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                            )
                                            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                            Text(
                                                text = "Due: ${dateFormat.format(java.util.Date(assignment.dueDateMillis))}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                Row {
                                    IconButton(onClick = { assignmentToEdit = assignment }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteAssignment(assignment) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
        var category by remember { mutableStateOf("Homework") }
        var categoryColor by remember { mutableStateOf("#3197D6") }

        if (showDatePicker) {
            val datePickerColors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
                DatePicker(
                    state = datePickerState,
                    colors = datePickerColors,
                    showModeToggle = false
                )
            }
        }

        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("Add Assignment") },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
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
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Category Preset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val categoriesPresetList = listOf("Homework", "Exam", "Project", "Quiz", "Lab", "Custom")
                    var isCustomCategory by remember { mutableStateOf(!categoriesPresetList.dropLast(1).contains(category)) }
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesPresetList.forEach { cat ->
                            val isSelected = (cat == "Custom" && isCustomCategory) || (cat == category && !isCustomCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "Custom") {
                                        isCustomCategory = true
                                        category = ""
                                    } else {
                                        isCustomCategory = false
                                        category = cat
                                        categoryColor = when (cat) {
                                            "Exam" -> "#E52F28"
                                            "Homework" -> "#3197D6"
                                            "Project" -> "#2CAF5F"
                                            "Quiz" -> "#7B2CBF"
                                            "Lab" -> "#E65100"
                                            else -> "#78909C"
                                        }
                                    }
                                },
                                label = { Text(cat) }
                            )
                        }
                    }
                    
                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category Theme Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    
                    val presetColorsList = listOf(
                        "#E52F28", // Red/Rose
                        "#E65100", // Orange
                        "#FBC02D", // Yellow/Gold
                        "#2CAF5F", // Green/Emerald
                        "#3197D6", // Blue/Ocean
                        "#7B2CBF", // Purple/Amethyst
                        "#78909C"  // Slate/Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColorsList.forEach { hex ->
                            val colorObj = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) } catch(e: Exception) { MaterialTheme.colorScheme.primary }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colorObj, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (categoryColor == hex) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { categoryColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (categoryColor == hex) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    
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
                        viewModel.addAssignment(courseId, title, desc, dueDateMillis, category.ifBlank { "Other" }, categoryColor)
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
        var title by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit!!.title) }
        var desc by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit!!.description) }
        var dueDateMillis by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit!!.dueDateMillis) }
        var category by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit!!.category) }
        var categoryColor by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit!!.categoryColor) }
        var showDatePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            val datePickerColors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
                DatePicker(
                    state = datePickerState,
                    colors = datePickerColors,
                    showModeToggle = false
                )
            }
        }

        AlertDialog(
            onDismissRequest = { assignmentToEdit = null },
            title = { Text("Edit Assignment") },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
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
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Category Preset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    val categoriesPresetList = listOf("Homework", "Exam", "Project", "Quiz", "Lab", "Custom")
                    var isCustomCategory by remember { mutableStateOf(!categoriesPresetList.dropLast(1).contains(category)) }
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesPresetList.forEach { cat ->
                            val isSelected = (cat == "Custom" && isCustomCategory) || (cat == category && !isCustomCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "Custom") {
                                        isCustomCategory = true
                                        category = ""
                                    } else {
                                        isCustomCategory = false
                                        category = cat
                                        categoryColor = when (cat) {
                                            "Exam" -> "#E52F28"
                                            "Homework" -> "#3197D6"
                                            "Project" -> "#2CAF5F"
                                            "Quiz" -> "#7B2CBF"
                                            "Lab" -> "#E65100"
                                            else -> "#78909C"
                                        }
                                    }
                                },
                                label = { Text(cat) }
                            )
                        }
                    }
                    
                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category Theme Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    
                    val presetColorsList = listOf(
                        "#E52F28", // Red/Rose
                        "#E65100", // Orange
                        "#FBC02D", // Yellow/Gold
                        "#2CAF5F", // Green/Emerald
                        "#3197D6", // Blue/Ocean
                        "#7B2CBF", // Purple/Amethyst
                        "#78909C"  // Slate/Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColorsList.forEach { hex ->
                            val colorObj = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) } catch(e: Exception) { MaterialTheme.colorScheme.primary }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colorObj, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (categoryColor == hex) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { categoryColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (categoryColor == hex) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    
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
                                dueDateMillis = dueDateMillis,
                                category = category.ifBlank { "Other" },
                                categoryColor = categoryColor
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
