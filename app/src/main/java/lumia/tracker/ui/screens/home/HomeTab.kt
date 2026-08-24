package lumia.tracker.ui.screens.home

import android.content.Intent
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.model.Task
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.*
import lumia.tracker.ui.screens.study.dialogs.AddTaskDialog
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeTab - Interactive Academic Command Center with modern Bento Grid layout.
 * Combines 1-tap focus session launching, interactive task objectives,
 * academic standing pulse, dynamic timetable browser, and prioritized study tasks.
 */
@Composable
fun HomeTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onAddCourseClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val pomodoros by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()
    val workDurationMin by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()

    var selectedDateOffset by remember { mutableIntStateOf(0) }
    var showQuickAddTaskDialog by remember { mutableStateOf(false) }

    // Calculate today's study focus minutes
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayFocusMinutes = remember(pomodoros, todayStartMillis) {
        pomodoros.filter { it.dateMillis >= todayStartMillis }.sumOf { it.durationMinutes }
    }

    val pendingTasksCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val completedTasksCount = remember(tasks) { tasks.count { it.isCompleted } }
    val taskCompletionRate = remember(tasks, completedTasksCount) {
        if (tasks.isNotEmpty()) (completedTasksCount.toFloat() / tasks.size).coerceIn(0f, 1f) else 0f
    }

    val upcomingAssigns = remember(assignments) {
        assignments
            .filter { !it.isCompleted }
            .sortedBy { if (it.dueDateMillis > 0) it.dueDateMillis else Long.MAX_VALUE }
            .take(3)
    }

    val activeTasks = remember(tasks) {
        tasks.filter { !it.isCompleted }.take(4)
    }

    // Next scheduled lecture for today
    val todayDayOfWeekStr = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()) }
    val scheduledCoursesToday = remember(courses, todayDayOfWeekStr) {
        courses.filter { it.scheduleDays.contains(todayDayOfWeekStr, ignoreCase = true) }
    }
    val nextCourseToday = scheduledCoursesToday.firstOrNull()

    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = bottomPadding.calculateTopPadding() + 8.dp,
                bottom = bottomPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Permission Health Check Panels
            item(key = "permissions") {
                NotificationPermissionPanel()
                ExactAlarmPermissionPanel()
                BatteryOptimizationPermissionPanel()
            }

            // 2. Interactive Bento Grid Layout
            item(key = "bento_grid_section") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bento 1: HERO FOCUS COMMAND (Large interactive hero tile)
                    val isFocusRunning = pomodoroState.isRunning
                    ScholarCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClick { navController.navigate("pomodoro") { launchSingleTop = true } },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isFocusRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = if (isFocusRunning) "Focus in Progress" else "Focus Command Space",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFocusRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (todayFocusMinutes >= 60) "${todayFocusMinutes / 60}h ${todayFocusMinutes % 60}m" else "${todayFocusMinutes}m",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isFocusRunning) "Tap to open active timer • ${pomodoroState.timeLeft / 60}m remaining" else "Daily Focus Target: 120m • Tap to enter Focus Space",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Interactive Quick Start Play Button
                            FilledIconButton(
                                onClick = {
                                    if (!isFocusRunning) {
                                        val intent = Intent(context, PomodoroService::class.java).apply {
                                            action = "START"
                                            putExtra("workDuration", workDurationMin * 60)
                                            putExtra("shortBreakDuration", 5 * 60)
                                            putExtra("longBreakDuration", 15 * 60)
                                            putExtra("periodSessions", 4)
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    }
                                    navController.navigate("pomodoro") { launchSingleTop = true }
                                },
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isFocusRunning) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                                    contentDescription = "Start Focus",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Bento Row 2: Split Cards (Tasks Objectives & Academic Courses)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bento 2: Tasks & Study Goals Tile
                        ScholarCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .bouncyClick { onNavigateToTasks() },
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.TaskAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showQuickAddTaskDialog = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.AddCircleOutline,
                                            contentDescription = "Quick Add",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "$pendingTasksCount Left",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$completedTasksCount completed (${(taskCompletionRate * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { taskCompletionRate },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }

                        // Bento 3: Academic Courses Tile
                        ScholarCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .bouncyClick { viewModel.setSelectedDashboardTab(1) },
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${courses.size} Courses",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${subjects.size} study subjects linked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        courses.take(4).forEach { course ->
                                            val cColor = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                            Box(modifier = Modifier.size(8.dp).background(cColor, CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bento 4: Next Lecture Pulse Tile (if scheduled today)
                    if (nextCourseToday != null) {
                        val nextColor = try { Color(android.graphics.Color.parseColor(nextCourseToday.colorHex.ifBlank { "#3197D6" })) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = nextColor.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, nextColor.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClick { navController.navigate("courseDetail/${nextCourseToday.id}") { launchSingleTop = true } }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(nextColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = nextColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Today's Class: ${nextCourseToday.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (nextCourseToday.schedule.isNotBlank()) {
                                        Text(
                                            text = nextCourseToday.schedule,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = nextColor)
                            }
                        }
                    }
                }
            }

            // 3. Daily Class Schedule & Timetable Browser
            item(key = "today_classes_calendar") {
                val baseCalendar = Calendar.getInstance()
                baseCalendar.add(Calendar.DAY_OF_MONTH, selectedDateOffset)
                val currentDayOfWeekStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(baseCalendar.time)
                val todayDayNum = baseCalendar.get(Calendar.DAY_OF_MONTH)
                val todayMonthStr = SimpleDateFormat("MMM", Locale.getDefault()).format(baseCalendar.time)

                val scheduledCourses = courses.filter { course ->
                    course.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true)
                }

                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = when (selectedDateOffset) {
                                            0 -> "Today's Timetable"
                                            1 -> "Tomorrow's Timetable"
                                            -1 -> "Yesterday's Timetable"
                                            else -> "$currentDayOfWeekStr's Timetable"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$currentDayOfWeekStr, $todayMonthStr $todayDayNum",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (selectedDateOffset != 0) {
                                BouncyTextButton(onClick = { selectedDateOffset = 0 }) {
                                    Text("Reset to Today", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5-Day selector row
                        val todayCalendar = Calendar.getInstance()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                        // iOS-Style 5-Day Segmented selector row
                        val todayCalendar = Calendar.getInstance()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (i in -2..2) {
                                    val calOffset = todayCalendar.clone() as Calendar
                                    val targetOffset = i
                                    calOffset.add(Calendar.DAY_OF_MONTH, targetOffset)
                                    val isSelected = selectedDateOffset == targetOffset
                                    val dayLetter = SimpleDateFormat("E", Locale.getDefault()).format(calOffset.time).take(3)
                                    val dayNumber = calOffset.get(Calendar.DAY_OF_MONTH).toString()

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDateOffset = targetOffset }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = dayLetter,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = dayNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (scheduledCourses.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "No academic lectures scheduled for this day.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                scheduledCourses.forEach { course ->
                                    val courseColor = try {
                                        Color(android.graphics.Color.parseColor(course.colorHex.ifBlank { "#3197D6" }))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    ScholarCard(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            navController.navigate("courseDetail/${course.id}") { launchSingleTop = true }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(courseColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    course.name.take(2).uppercase(),
                                                    color = courseColor,
                                                    fontWeight = FontWeight.Black,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = course.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (course.schedule.isNotBlank()) {
                                                    Text(
                                                        text = course.schedule,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Icon(
                                                Icons.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }   }
                    }
                }
            }

            // 4. Urgent Upcoming Assignments
            if (upcomingAssigns.isNotEmpty()) {
                item(key = "upcoming_assignments_section") {
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        Icons.Rounded.AssignmentLate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Assignments & Projects",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                upcomingAssigns.forEach { assignment ->
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(assignment.categoryColor.ifEmpty { "#3197D6" }))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    ScholarCard(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(catColor, CircleShape)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = assignment.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (assignment.dueDateMillis > 0) {
                                                    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                    Text(
                                                        text = "Due ${df.format(Date(assignment.dueDateMillis))}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
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

            // 5. Active Study Tasks with 1-tap completion
            if (activeTasks.isNotEmpty()) {
                item(key = "active_tasks_section") {
                    ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Prioritized Tasks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                BouncyTextButton(onClick = onNavigateToTasks) {
                                    Text("View All", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Spacer(Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeTasks.forEach { task ->
                                    ScholarCard(
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { viewModel.toggleTaskCompleted(task) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = 2.dp,
                                                        color = if (task.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    )
                                                    .background(
                                                        if (task.isCompleted) MaterialTheme.colorScheme.tertiary else Color.Transparent
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (task.isCompleted) {
                                                    Icon(
                                                        Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (task.tags.isNotBlank()) {
                                                    Spacer(Modifier.height(2.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                    ) {
                                                        Text(
                                                            text = task.tags,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
            }

            // 6. Quick Notes Tool (If Enabled)
            if (betaNotes) {
                item(key = "notes_tool") {
                    ScholarCard(
                        onClick = { navController.navigate("notes") { launchSingleTop = true } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Notes,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text("Academic Scratchpad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Draft equations, quick formulas & thoughts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

    if (showQuickAddTaskDialog) {
        AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddTaskDialog = false }
        )
    }
}
