package lumia.tracker.ui.screens.home

import android.content.Intent
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Task
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.*
import lumia.tracker.ui.screens.study.dialogs.AddTaskDialog
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Safely parses a hex color string with a fallback Color if parsing fails.
 */
private fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val cleanHex = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Parses time string like '10:00 AM' or '14:30' into epoch milliseconds for the target calendar date.
 */
private fun parseTimeToDayMillis(timeStr: String, baseCal: Calendar): Long? {
    if (timeStr.isBlank()) return null
    val trimmed = timeStr.trim().uppercase(Locale.US)
    val formats = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.isLenient = true
            val date = sdf.parse(trimmed)
            if (date != null) {
                val timeCal = Calendar.getInstance().apply { time = date }
                return (baseCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        } catch (_: Exception) {}
    }
    return null
}

/**
 * HomeTab - Interactive Academic Command Center with modern Bento Grid layout.
 * Combines personalized time-aware greeting, 1-tap focus session launching,
 * interactive task objectives, dynamic timetable browser, real-time attendance recording,
 * and prioritized study tasks.
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
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val pomodoros by viewModel.pomodoroSessions.collectAsStateWithLifecycle()
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()
    val workDurationMin by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val allAttendanceRecords by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

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

    // Pending scheduled lectures for today where attendance has not yet been marked
    val todayDayOfWeekStr = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()) }
    val pendingAttendanceCoursesToday = remember(courses, todayDayOfWeekStr, allAttendanceRecords) {
        val todayCourses = courses.filter { it.scheduleDays.contains(todayDayOfWeekStr, ignoreCase = true) }
        todayCourses.filter { course ->
            allAttendanceRecords.none { record ->
                record.courseId == course.id && record.dateMillis == todayStartMillis
            }
        }
    }

    val isFocusRunning = pomodoroState.isRunning

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = bottomPadding.calculateTopPadding() + 8.dp,
            bottom = bottomPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Personalized Time-Aware Greeting Section
        item(key = "greeting_section") {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val timeGreeting = when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..21 -> "Good evening"
                else -> "Good night"
            }
            val rawName = activeProfile.alias.ifBlank { activeProfile.name.ifBlank { "" } }
            val greetingHeadline = if (rawName.isNotBlank()) "$timeGreeting, $rawName" else timeGreeting
            val todayDateFormatted = remember {
                SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = greetingHeadline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = todayDateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Permission Health Check Panels
        item(key = "permissions") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                NotificationPermissionPanel()
                ExactAlarmPermissionPanel()
                BatteryOptimizationPermissionPanel()
            }
        }

        // 3. Bento Grid Section (Focus Hero + Split Cards)
        item(key = "bento_grid_section") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento 1: Focus Hero Card
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClick(onClick = {
                            navController.navigate("pomodoro") { launchSingleTop = true }
                        }),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isFocusRunning) "Focusing" else "Focus",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                val minsLeft = pomodoroState.timeLeft / 60
                                val secsLeft = pomodoroState.timeLeft % 60
                                val focusDisplayTime = if (isFocusRunning) {
                                    String.format(Locale.US, "%02d:%02d", minsLeft, secsLeft)
                                } else {
                                    if (todayFocusMinutes >= 60) {
                                        "${todayFocusMinutes / 60}h ${todayFocusMinutes % 60}m"
                                    } else {
                                        "${todayFocusMinutes}m"
                                    }
                                }

                                Text(
                                    text = focusDisplayTime,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = if (isFocusRunning) "Session in progress" else "Today's focus time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

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
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isFocusRunning) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isFocusRunning) "Focus in progress" else "Start Focus",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val focusProgress = remember(todayFocusMinutes) {
                            (todayFocusMinutes.toFloat() / 120f).coerceIn(0f, 1f)
                        }
                        LinearProgressIndicator(
                            progress = { focusProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Bento Row 2: Split Cards (Tasks Objectives & Academic Courses)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bento 2: Tasks Objectives Tile
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(136.dp),
                        onClick = { onNavigateToTasks() },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.TaskAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showQuickAddTaskDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.AddCircleOutline,
                                        contentDescription = "Quick Add Task",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "$pendingTasksCount Pending",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(taskCompletionRate * 100).toInt()}% completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { taskCompletionRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
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
                            .height(136.dp),
                        onClick = { viewModel.setSelectedDashboardTab(1) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "${courses.size} Courses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${subjects.size} subjects",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (courses.isEmpty()) {
                                        Text(
                                            text = "None added",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    } else {
                                        courses.take(4).forEach { course ->
                                            val cColor = parseHexColor(course.colorHex, MaterialTheme.colorScheme.primary)
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .background(cColor, CircleShape)
                                            )
                                        }
                                        if (courses.size > 4) {
                                            Text(
                                                text = "+${courses.size - 4}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
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

        // 4. Interactive Daily Class Schedule & Timetable Calendar
        item(key = "today_classes_calendar") {
            val baseCalendar = Calendar.getInstance()
            baseCalendar.add(Calendar.DAY_OF_MONTH, selectedDateOffset)
            val currentDayOfWeekStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(baseCalendar.time)
            val currentShortDayStr = SimpleDateFormat("EEE", Locale.getDefault()).format(baseCalendar.time)

            val scheduledCourses = remember(courses, currentDayOfWeekStr, currentShortDayStr) {
                courses.filter { course ->
                    course.scheduleDays.isNotBlank() && (
                        course.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true) ||
                        course.scheduleDays.contains(currentShortDayStr, ignoreCase = true)
                    )
                }
            }

            val isSelectedDayToday = selectedDateOffset == 0
            val currentTimeMillis = System.currentTimeMillis()

            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 1. Header with Title, Summary, and Quick Today Reset
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Timetable & Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = when {
                                    scheduledCourses.isEmpty() -> "No classes scheduled"
                                    scheduledCourses.size == 1 -> "1 class on ${SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(baseCalendar.time)}"
                                    else -> "${scheduledCourses.size} classes • ${SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(baseCalendar.time)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isSelectedDayToday) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDateOffset = 0 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Today,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. 7-Day Rolling Calendar Strip with Course Color Dots
                    val todayCalendar = Calendar.getInstance()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in -2..4) {
                            val calOffset = todayCalendar.clone() as Calendar
                            val targetOffset = i
                            calOffset.add(Calendar.DAY_OF_MONTH, targetOffset)
                            val isSelected = selectedDateOffset == targetOffset
                            val isToday = targetOffset == 0

                            val dayLetter = SimpleDateFormat("EEE", Locale.getDefault()).format(calOffset.time)
                            val dayNumber = calOffset.get(Calendar.DAY_OF_MONTH).toString()
                            val fullDayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calOffset.time)

                            // Find courses scheduled for this specific day to render dots
                            val dayCourses = remember(courses, fullDayName, dayLetter) {
                                courses.filter { c ->
                                    c.scheduleDays.isNotBlank() && (
                                        c.scheduleDays.contains(fullDayName, ignoreCase = true) ||
                                        c.scheduleDays.contains(dayLetter, ignoreCase = true)
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                    )
                                    .clickable { selectedDateOffset = targetOffset }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = dayLetter.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dayNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Course Color Dots Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(5.dp)
                                ) {
                                    if (dayCourses.isEmpty()) {
                                        Spacer(modifier = Modifier.size(4.dp))
                                    } else {
                                        dayCourses.take(3).forEach { dc ->
                                            val dotColor = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                parseHexColor(dc.colorHex, MaterialTheme.colorScheme.primary)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(dotColor, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Dynamic Lecture Cards with Live Timing & Inline Attendance
                    if (scheduledCourses.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "No Lectures Scheduled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Enjoy your free day or prepare ahead for upcoming assignments.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            scheduledCourses.forEach { course ->
                                val courseColor = parseHexColor(course.colorHex, MaterialTheme.colorScheme.primary)

                                // Parse lecture start and end millis for today
                                val startMillis = remember(course.scheduleStartTime, isSelectedDayToday) {
                                    if (isSelectedDayToday && course.scheduleStartTime.isNotBlank()) {
                                        parseTimeToDayMillis(course.scheduleStartTime, baseCalendar)
                                    } else null
                                }
                                val endMillis = remember(course.scheduleEndTime, isSelectedDayToday) {
                                    if (isSelectedDayToday && course.scheduleEndTime.isNotBlank()) {
                                        parseTimeToDayMillis(course.scheduleEndTime, baseCalendar)
                                    } else null
                                }

                                val isLiveNow = isSelectedDayToday && startMillis != null && endMillis != null &&
                                        currentTimeMillis in startMillis..endMillis
                                val isUpcoming = isSelectedDayToday && startMillis != null && currentTimeMillis < startMillis
                                val isPast = isSelectedDayToday && endMillis != null && currentTimeMillis > endMillis

                                val liveProgress = if (isLiveNow && startMillis != null && endMillis != null && endMillis > startMillis) {
                                    ((currentTimeMillis - startMillis).toFloat() / (endMillis - startMillis)).coerceIn(0f, 1f)
                                } else 0f

                                // Check attendance record for this course on selected date
                                val selectedDateStartMillis = remember(selectedDateOffset) {
                                    (baseCalendar.clone() as Calendar).apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                }
                                val existingAttendance = allAttendanceRecords.firstOrNull {
                                    it.courseId == course.id && it.dateMillis == selectedDateStartMillis
                                }

                                // Check schedule conflict with other courses on this day
                                val hasConflict = remember(scheduledCourses, course) {
                                    scheduledCourses.any { other ->
                                        other.id != course.id &&
                                        other.scheduleStartTime.isNotBlank() &&
                                        other.scheduleStartTime.equals(course.scheduleStartTime, ignoreCase = true)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isLiveNow) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    },
                                    border = if (isLiveNow) {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                    } else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        // Main Row: Color stripe, Title, Schedule, and Actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(courseColor, CircleShape)
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        navController.navigate("courseDetail/${course.id}") { launchSingleTop = true }
                                                    }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = course.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (course.code.isNotBlank()) {
                                                        Text(
                                                            text = course.code,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                val scheduleText = when {
                                                    course.scheduleStartTime.isNotBlank() && course.scheduleEndTime.isNotBlank() ->
                                                        "${course.scheduleStartTime} - ${course.scheduleEndTime}"
                                                    course.schedule.isNotBlank() -> course.schedule
                                                    else -> ""
                                                }
                                                val details = listOfNotNull(
                                                    scheduleText.ifBlank { null },
                                                    course.instructor.ifBlank { null }
                                                ).joinToString(" • ")

                                                if (details.isNotBlank()) {
                                                    Text(
                                                        text = details,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Quick Attendance Buttons or Status Badge
                                            if (existingAttendance != null) {
                                                val statusColor = when (existingAttendance.status.lowercase()) {
                                                    "present" -> Color(0xFF34C759)
                                                    "absent" -> MaterialTheme.colorScheme.error
                                                    "late" -> Color(0xFFFF9500)
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = statusColor.copy(alpha = 0.15f),
                                                    modifier = Modifier.clickable {
                                                        // Toggle between Present -> Absent -> Late
                                                        val nextStatus = when (existingAttendance.status.lowercase()) {
                                                            "present" -> "Absent"
                                                            "absent" -> "Late"
                                                            else -> "Present"
                                                        }
                                                        viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, nextStatus)
                                                    }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            if (existingAttendance.status.equals("present", true)) Icons.Rounded.Check else Icons.Rounded.Edit,
                                                            contentDescription = null,
                                                            tint = statusColor,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = existingAttendance.status,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = statusColor
                                                        )
                                                    }
                                                }
                                            } else {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    // Quick 1-tap Present
                                                    FilledTonalIconButton(
                                                        onClick = {
                                                            viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, "Present")
                                                        },
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = Color(0xFF34C759).copy(alpha = 0.15f),
                                                            contentColor = Color(0xFF34C759)
                                                        ),
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.Check,
                                                            contentDescription = "Present",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }

                                                    // Quick 1-tap Absent
                                                    FilledTonalIconButton(
                                                        onClick = {
                                                            viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, "Absent")
                                                        },
                                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                            contentColor = MaterialTheme.colorScheme.error
                                                        ),
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.Close,
                                                            contentDescription = "Absent",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Status Pill / Live lecture countdown / Conflict Alert
                                        if (isLiveNow || hasConflict) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isLiveNow) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(Color(0xFF34C759), CircleShape)
                                                        )
                                                        val minutesLeft = if (endMillis != null) {
                                                            ((endMillis - currentTimeMillis) / 60000).coerceAtLeast(1)
                                                        } else 0
                                                        Text(
                                                            text = "Live Now • $minutesLeft min left",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF34C759)
                                                        )
                                                    }
                                                }

                                                if (hasConflict) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFF9500).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "Time Overlap",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFF9500),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (isLiveNow && liveProgress > 0f) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { liveProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = Color(0xFF34C759),
                                                    trackColor = Color(0xFF34C759).copy(alpha = 0.2f)
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

        // 6. Urgent Upcoming Assignments
        if (upcomingAssigns.isNotEmpty()) {
            item(key = "upcoming_assignments_section") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${upcomingAssigns.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcomingAssigns.forEach { assignment ->
                                val catColor = parseHexColor(assignment.categoryColor, MaterialTheme.colorScheme.primary)
                                val isOverdue = assignment.dueDateMillis in 1 until System.currentTimeMillis()

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(catColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = assignment.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (assignment.dueDateMillis > 0) {
                                                val df = SimpleDateFormat("MMM d", Locale.getDefault())
                                                val dueFormatted = df.format(Date(assignment.dueDateMillis))
                                                Text(
                                                    text = if (isOverdue) "Overdue • $dueFormatted" else "Due $dueFormatted",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
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

        // 7. Active Prioritized Study Tasks
        if (activeTasks.isNotEmpty()) {
            item(key = "active_tasks_section") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onNavigateToTasks() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeTasks.forEach { task ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleTaskCompleted(task) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Minimal Checkbox
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 1.5.dp,
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
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (task.tags.isNotBlank()) {
                                                Text(
                                                    text = task.tags,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // 8. Quick Notes Tool (If Enabled)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Scratchpad",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Draft equations, formulas & thoughts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }

    if (showQuickAddTaskDialog) {
        AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddTaskDialog = false }
        )
    }
}
