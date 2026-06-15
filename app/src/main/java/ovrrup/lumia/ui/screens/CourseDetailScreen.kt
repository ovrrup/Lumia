package ovrrup.lumia.ui.screens

import ovrrup.lumia.ui.theme.liquidGlass
import ovrrup.lumia.ui.theme.glassBar
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import ovrrup.lumia.ui.theme.bouncyClick
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Sell
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
import ovrrup.lumia.viewmodel.ScholarViewModel

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.MoreVert

import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.ReorderableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(navController: NavController, viewModel: ScholarViewModel, courseId: Int) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val course = courses.find { it.id == courseId }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val systemAutoLinkByName by viewModel.systemAutoLinkByName.collectAsStateWithLifecycle()
    val enableSynergy by viewModel.systemEnableSynergy.collectAsStateWithLifecycle()
    val fuseSubjectsCourses by viewModel.systemFuseSubjectsCourses.collectAsStateWithLifecycle()

    val linkedSubject = remember(course, subjects, systemAutoLinkByName) {
        if (course != null) {
            if (course.subjectId != null) {
                subjects.find { it.id == course.subjectId }
            } else if (systemAutoLinkByName) {
                subjects.find { it.name.trim().lowercase() == course.name.trim().lowercase() }
                    ?: subjects.find {
                        val subName = it.name.trim().lowercase()
                        val crsName = course.name.trim().lowercase()
                        subName.isNotEmpty() && crsName.isNotEmpty() && (subName.contains(crsName) || crsName.contains(subName))
                    }
            } else {
                null
            }
        } else {
            null
        }
    }

    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val courseTasks = remember(allTasks, course) {
        if (course != null) allTasks.filter { it.courseId == course.id } else emptyList()
    }

    val topicsFlow = remember(linkedSubject) {
        if (linkedSubject != null) viewModel.getTopicsForSubject(linkedSubject.id)
        else kotlinx.coroutines.flow.MutableStateFlow(emptyList())
    }
    val topics by topicsFlow.collectAsStateWithLifecycle(emptyList())

    val assignments by viewModel.getAssignmentsForCourse(courseId).collectAsStateWithLifecycle(emptyList())
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var localAssignments by remember(assignments) { mutableStateOf(assignments) }
    var localTasks by remember(courseTasks) { mutableStateOf(courseTasks) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            if (from.key is String && to.key is String) {
                val fromStr = from.key as String
                val toStr = to.key as String
                if (fromStr.startsWith("assignment_") && toStr.startsWith("assignment_")) {
                    localAssignments = localAssignments.toMutableList().apply {
                        val fromId = fromStr.removePrefix("assignment_").toInt()
                        val toId = toStr.removePrefix("assignment_").toInt()
                        val fromIndex = indexOfFirst { it.id == fromId }
                        val toIndex = indexOfFirst { it.id == toId }
                        if (fromIndex != -1 && toIndex != -1) {
                            add(toIndex, removeAt(fromIndex))
                        }
                    }
                } else if (fromStr.startsWith("task_") && toStr.startsWith("task_")) {
                    localTasks = localTasks.toMutableList().apply {
                        val fromId = fromStr.removePrefix("task_").toInt()
                        val toId = toStr.removePrefix("task_").toInt()
                        val fromIndex = indexOfFirst { it.id == fromId }
                        val toIndex = indexOfFirst { it.id == toId }
                        if (fromIndex != -1 && toIndex != -1) {
                            add(toIndex, removeAt(fromIndex))
                        }
                    }
                }
            }
        },
        canDragOver = { draggedOver, dragged -> 
            val typeOver = (draggedOver.key as? String)?.substringBefore("_")
            val typeVal = (dragged.key as? String)?.substringBefore("_")
            typeOver == typeVal && typeOver != null
        }
    )

    LaunchedEffect(reorderableState.draggingItemKey) {
        if (reorderableState.draggingItemKey == null) {
            if (localAssignments != assignments) {
                val updatedAssignments = localAssignments.mapIndexed { index, assignment -> assignment.copy(orderIndex = index) }
                viewModel.updateAssignmentsOrder(updatedAssignments)
            }
            if (localTasks != courseTasks) {
                val updatedTasks = localTasks.mapIndexed { index, task -> task.copy(orderIndex = index) }
                viewModel.updateTasksOrder(updatedTasks)
            }
        }
    }
    var showLinkSubjectDialog by remember { mutableStateOf(false) }
    var assignmentToEdit by remember { mutableStateOf<ovrrup.lumia.model.PracticeAssignment?>(null) }

    val allNotes by viewModel.notes.collectAsStateWithLifecycle()

    val courseNotes = remember(allNotes, course, linkedSubject, subjects, courses, systemAutoLinkByName) {
        if (course != null) {
            val linkedSubjectId = linkedSubject?.id
            val linkedCourseIds = if (linkedSubjectId != null) {
                courses.filter { it.subjectId == linkedSubjectId || (systemAutoLinkByName && it.name.trim().lowercase() == linkedSubject.name.trim().lowercase()) }
                    .map { it.id }
            } else {
                emptyList()
            }

            allNotes.filter { note ->
                note.courseId == course.id || 
                (linkedSubjectId != null && note.subjectId == linkedSubjectId) ||
                (note.courseId != null && linkedCourseIds.contains(note.courseId))
            }
        } else {
            emptyList()
        }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<ovrrup.lumia.model.Note?>(null) }
    var noteText by remember { mutableStateOf("") }
    var noteCustomTag by remember { mutableStateOf("Core") }

    if (course == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
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
                LargeTopAppBar(
                    title = { Text(course.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().reorderable(reorderableState)
            ) {
                if (course.instructor.isNotBlank() || course.schedule.isNotBlank() || course.description.isNotBlank()) {
                    item {
                        ovrrup.lumia.ui.components.GlassCard(
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

                // Interconnected Study Subject & Synergy Score Section
                if (linkedSubject != null) {
                    item {
                        ovrrup.lumia.ui.components.GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Interconnected Study Subject",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = linkedSubject.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (linkedSubject.tags.isNotBlank()) {
                                            Text("Tags: ${linkedSubject.tags}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    
                                    if (!fuseSubjectsCourses) {
                                        androidx.compose.material3.IconButton(
                                            onClick = { navController.navigate("subjectDetail/${linkedSubject.id}") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = "Go to Study Subject",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        androidx.compose.material3.IconButton(
                                            onClick = { navController.navigate("subjectDetail/${linkedSubject.id}") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Manage Subject",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (enableSynergy) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    val completedTopics = topics.count { it.isCompleted }
                                    val totalTopics = topics.size
                                    val topicPercent = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
                                    
                                    val avgScore = topicPercent
                                    val synergyScore = (avgScore * 100).toInt()

                                    val ratingClass = when {
                                        synergyScore >= 85 -> "Gold Synergy (Elite Alignment)"
                                        synergyScore >= 60 -> "Silver Synergy (Healthy Connection)"
                                        synergyScore >= 30 -> "Bronze Synergy (Moderate Progress)"
                                        else -> "Basic Synergy (Awaiting Action)"
                                    }

                                    Row(
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            progress = { avgScore },
                                            modifier = Modifier.size(50.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            strokeWidth = 5.dp
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Dynamic Synergy Index",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                            Text(
                                                text = ratingClass,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                if (fuseSubjectsCourses) {
                                    Spacer(Modifier.height(16.dp))
                                    Text("Embedded Subject Hub", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.height(8.dp))
                                    ovrrup.lumia.ui.components.BouncyButton(onClick = { navController.navigate("subjectDetail/${linkedSubject.id}") }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Open Subject Workspace")
                                    }
                                }
                            }
                        }
                    }
                } else if (fuseSubjectsCourses) {
                    item {
                        ovrrup.lumia.ui.components.GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No Subject Linked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Fuse a subject to unlock synergy and topics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(onClick = { showLinkSubjectDialog = true }) {
                                    Text("Link or Create Subject")
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

                    ovrrup.lumia.ui.components.GlassCard(
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
                                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onTertiaryContainer)
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
                                                
                                                val record = attendanceRecords.find { rec ->
                                                    val recCal = java.util.Calendar.getInstance().apply { timeInMillis = rec.dateMillis }
                                                    recCal.get(java.util.Calendar.YEAR) == dateCal.get(java.util.Calendar.YEAR) &&
                                                    recCal.get(java.util.Calendar.DAY_OF_YEAR) == dateCal.get(java.util.Calendar.DAY_OF_YEAR)
                                                }
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
                                                        .bouncyClick {
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
                                        
                                        val record = attendanceRecords.find { rec ->
                                            val recCal = java.util.Calendar.getInstance().apply { timeInMillis = rec.dateMillis }
                                            recCal.get(java.util.Calendar.YEAR) == dateCal.get(java.util.Calendar.YEAR) &&
                                            recCal.get(java.util.Calendar.DAY_OF_YEAR) == dateCal.get(java.util.Calendar.DAY_OF_YEAR)
                                        }
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
                                                .bouncyClick {
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
                        val selCal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDate }
                        val record = attendanceRecords.find { rec ->
                            val recCal = java.util.Calendar.getInstance().apply { timeInMillis = rec.dateMillis }
                            recCal.get(java.util.Calendar.YEAR) == selCal.get(java.util.Calendar.YEAR) &&
                            recCal.get(java.util.Calendar.DAY_OF_YEAR) == selCal.get(java.util.Calendar.DAY_OF_YEAR)
                        }
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
                                                .bouncyClick {
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

                // Course Notes Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Course Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        ovrrup.lumia.ui.components.BouncyButton(
                            onClick = { 
                                noteToEdit = null
                                noteText = ""
                                noteCustomTag = "Theory"
                                showAddNoteDialog = true 
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Note", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Note")
                        }
                    }
                }

                if (courseNotes.isEmpty()) {
                    item {
                        ovrrup.lumia.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No notes for this course yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(courseNotes, key = { "cn_${it.id}" }) { note ->
                        ovrrup.lumia.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tag/Capsule
                                    val isCurrentCourse = note.courseId == course.id
                                    val pillColor = if (isCurrentCourse) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                    val onPillColor = if (isCurrentCourse) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(pillColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = note.tag.ifBlank { if (isCurrentCourse) "Course" else "Linked" },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = onPillColor
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            noteToEdit = note
                                            noteText = note.content
                                            noteCustomTag = note.tag
                                            showAddNoteDialog = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Rounded.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = {
                                            viewModel.deleteNote(note)
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val formattedDate = remember(note.dateMillis) {
                                    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy · hh:mm a", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(note.dateMillis))
                                }
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Assignments",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showAddAssignmentDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Assignment")
                        }
                    }
                }

                if (localAssignments.isEmpty()) {
                    item(key = "assignments_empty") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                            modifier = Modifier.animateItem()
                        ) {
                            ovrrup.lumia.ui.components.GlassCard(
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
                    items(localAssignments, key = { "assignment_${it.id}" }) { assignment ->
                        ReorderableItem(reorderableState, key = "assignment_${assignment.id}") { isDragging ->
                            val cardColor by androidx.compose.animation.animateColorAsState(
                                if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            )
                            ovrrup.lumia.ui.components.GlassCard(
                                modifier = Modifier.detectReorderAfterLongPress(reorderableState).animateItem().fillMaxWidth().animateContentSize(),
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
                                    if (assignment.tags.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Sell, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(4.dp))
                                            Text(assignment.tags, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                if (localTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Related Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(localTasks, key = { "task_${it.id}" }) { task ->
                        ReorderableItem(reorderableState, key = "task_${task.id}") { isDragging ->
                            ovrrup.lumia.ui.components.GlassCard(
                                modifier = Modifier.detectReorderAfterLongPress(reorderableState).animateItem().fillMaxWidth().animateContentSize()
                                    .graphicsLayer {
                                        shadowElevation = if (isDragging) 16f else 0f
                                    },
                                shape = RoundedCornerShape(24.dp)
                            ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { ch -> viewModel.updateTask(task.copy(isCompleted = ch)) }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (task.description.isNotBlank()) {
                                        Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (showLinkSubjectDialog) {
        var createNew by remember { mutableStateOf(false) }
        var newSubjectName by remember { mutableStateOf("") }
        var newSubjectTags by remember { mutableStateOf("") }
        var selectedExistingId by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { showLinkSubjectDialog = false },
            title = { Text("Link Subject to Course") },
            text = {
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = !createNew, onClick = { createNew = false })
                        Text("Link Existing Subject")
                    }
                    if (!createNew) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (subjects.isEmpty()) {
                            Text("No existing subjects.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subjects) { subj ->
                                    FilterChip(
                                        selected = selectedExistingId == subj.id,
                                        onClick = { selectedExistingId = subj.id },
                                        label = { Text(subj.name) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = createNew, onClick = { createNew = true })
                        Text("Create New Subject")
                    }
                    if (createNew) {
                        OutlinedTextField(
                            value = newSubjectName,
                            onValueChange = { newSubjectName = it },
                            label = { Text("New Subject Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSubjectTags,
                            onValueChange = { newSubjectTags = it },
                            label = { Text("Tags (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (createNew && newSubjectName.isNotBlank()) {
                        viewModel.addSubject(newSubjectName, newSubjectTags)
                        viewModel.updateCourse(course.copy(subjectId = subjects.maxOfOrNull { it.id }?.plus(1) ?: 1))
                        showLinkSubjectDialog = false
                    } else if (!createNew && selectedExistingId != null) {
                        viewModel.updateCourse(course.copy(subjectId = selectedExistingId))
                        showLinkSubjectDialog = false
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showLinkSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddAssignmentDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var dueDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
        var showDatePicker by remember { mutableStateOf(false) }
        var category by remember { mutableStateOf("Homework") }
        var categoryColor by remember { mutableStateOf("#3197D6") }
        var tags by remember { mutableStateOf("") }
        var selectedSubjectId by remember { mutableStateOf<Int?>(linkedSubject?.id) }

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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
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
                    Text("Link to Study Subject (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                        viewModel.addAssignment(courseId, title, desc, dueDateMillis, category.ifBlank { "Other" }, categoryColor, tags, selectedSubjectId)
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
        var title by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.title ?: "") }
        var desc by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.description ?: "") }
        var dueDateMillis by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.dueDateMillis ?: System.currentTimeMillis()) }
        var category by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.category ?: "") }
        var categoryColor by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.categoryColor ?: "#3197D6") }
        var tags by remember(assignmentToEdit) { mutableStateOf(assignmentToEdit?.tags ?: "") }
        var showDatePicker by remember { mutableStateOf(false) }
        var selectedSubjectId by remember(assignmentToEdit) { mutableStateOf<Int?>(assignmentToEdit?.subjectId) }

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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Optional, comma separated)") },
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
                    Text("Link to Study Subject (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                        assignmentToEdit?.copy(
                                title = title,
                                description = desc,
                                dueDateMillis = dueDateMillis,
                                category = category.ifBlank { "Other" },
                                categoryColor = categoryColor,
                                tags = tags,
                                subjectId = selectedSubjectId
                        )?.let { viewModel.updateAssignmentDetails(it) }
                        assignmentToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { assignmentToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (noteToEdit == null) "Add Note" else "Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = noteCustomTag,
                        onValueChange = { noteCustomTag = it },
                        label = { Text("Note Tag (e.g. Theory, Lab, Formula)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteText.isNotBlank()) {
                        val activeNote = noteToEdit
                        if (activeNote != null) {
                            viewModel.updateNote(
                                activeNote.copy(
                                    content = noteText,
                                    tag = noteCustomTag
                                )
                            )
                        } else {
                            viewModel.addNote(
                                content = noteText,
                                courseId = course.id,
                                tag = noteCustomTag
                            )
                        }
                        showAddNoteDialog = false
                        noteText = ""
                        noteCustomTag = "Theory"
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
