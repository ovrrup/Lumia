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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import lumia.tracker.service.PomodoroActionReceiver
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
 * Accurately determines if a course schedule includes the specified day,
 * supporting full names ('Monday'), abbreviations ('Mon'), single letters, and comma-separated lists.
 */
private fun isCourseScheduledForDay(scheduleDays: String, targetDay: String): Boolean {
    if (scheduleDays.isBlank() || targetDay.isBlank()) return false
    val shortTarget = targetDay.take(3).lowercase(Locale.US)
    return scheduleDays.split(",").any { day ->
        val trimmed = day.trim().lowercase(Locale.US)
        trimmed == targetDay.lowercase(Locale.US) || trimmed.startsWith(shortTarget) || shortTarget.startsWith(trimmed)
    }
}

/**
 * HomeTab - Modern Minimalist Academic Command Center.
 * Features subtle glassmorphism, fluid live blur effects, tactile pills and capsules,
 * decluttered typography, and modern interactive Bento components.
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

    val isFocusRunning = pomodoroState.isRunning

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = bottomPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Focus Station & Split Quick Cards
        item(key = "bento_grid_section") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Modern Glassmorphic Focus Hero Card
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClick(onClick = {
                            navController.navigate("pomodoro") { launchSingleTop = true }
                        }),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Subtle live glowing blur aura when focus session is running
                        if (isFocusRunning) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 40.dp, y = (-30).dp)
                                    .blur(40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                        CircleShape
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header row: status capsule pill + action control pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status capsule pill
                                GlassCapsule(
                                    containerColor = if (isFocusRunning) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                                    },
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isFocusRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isFocusRunning) {
                                            val liveTransition = rememberInfiniteTransition(label = "pulse_hero")
                                            val heroPulse by liveTransition.animateFloat(
                                                initialValue = 0.85f,
                                                targetValue = 1.15f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(800, easing = FastOutSlowInEasing),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "hero_pulse"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .graphicsLayer {
                                                        scaleX = heroPulse
                                                        scaleY = heroPulse
                                                    }
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Timer,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isFocusRunning) {
                                                if (pomodoroState.isPaused) "Paused" else "Focusing"
                                            } else "Focus",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFocusRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Capsule action controls: Stop & Play/Pause buttons
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isFocusRunning) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    context.sendBroadcast(Intent(context, PomodoroActionReceiver::class.java).apply {
                                                        action = "STOP"
                                                    })
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Rounded.Stop,
                                                    contentDescription = "Stop",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (isFocusRunning) {
                                                    context.sendBroadcast(Intent(context, PomodoroActionReceiver::class.java).apply {
                                                        action = "PAUSE_RESUME"
                                                    })
                                                } else {
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
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isFocusRunning) {
                                                    if (pomodoroState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause
                                                } else Icons.Rounded.PlayArrow,
                                                contentDescription = if (isFocusRunning) "Pause/Resume" else "Start Focus",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Large minimalist numerical time display
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
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )

                            // Duration preset capsule pills
                            if (!isFocusRunning) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(15, 25, 45, 60).forEach { mins ->
                                        val isSel = workDurationMin == mins
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSel) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                                            },
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (isSel) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { viewModel.updatePomodoroWorkDuration(mins) }
                                        ) {
                                            Text(
                                                text = "${mins}m",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Minimalist capsule progress indicator
                            val focusProgress = remember(todayFocusMinutes) {
                                (todayFocusMinutes.toFloat() / 120f).coerceIn(0f, 1f)
                            }
                            LinearProgressIndicator(
                                progress = { focusProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // Row 2: Split Cards (Tasks & Courses)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tasks Capsule Card
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        onClick = { onNavigateToTasks() },
                        shape = RoundedCornerShape(24.dp)
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
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.TaskAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable { showQuickAddTaskDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Add,
                                            contentDescription = "Add Task",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = "$pendingTasksCount Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { taskCompletionRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    // Academic Courses Capsule Card
                    ScholarCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        onClick = { viewModel.setSelectedDashboardTab(1) },
                        shape = RoundedCornerShape(24.dp)
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
                                        .background(
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(17.dp)
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

        // 2. Interactive Daily Class Schedule & Timetable Calendar
        item(key = "today_classes_calendar") {
            val baseCalendar = Calendar.getInstance()
            baseCalendar.add(Calendar.DAY_OF_MONTH, selectedDateOffset)
            val currentDayOfWeekStr = SimpleDateFormat("EEEE", Locale.US).format(baseCalendar.time)

            val scheduledCourses = remember(courses, currentDayOfWeekStr) {
                courses.filter { course ->
                    isCourseScheduledForDay(course.scheduleDays, currentDayOfWeekStr)
                }
            }

            val isSelectedDayToday = selectedDateOffset == 0
            val currentTimeMillis = System.currentTimeMillis()

            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with minimalist title, count capsule, and Today reset
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${scheduledCourses.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!isSelectedDayToday) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick { selectedDateOffset = 0 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Today,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
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

                    // 7-Day Rolling Calendar Strip with Vertical Capsule Pills
                    val todayCalendar = Calendar.getInstance()
                    val liveTransition = rememberInfiniteTransition(label = "live_pulse_anim")
                    val livePulseScale by liveTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "live_scale"
                    )
                    val livePulseAlpha by liveTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "live_alpha"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in -2..4) {
                            val calOffset = (todayCalendar.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_MONTH, i)
                            }
                            val targetOffset = i
                            val isSelected = selectedDateOffset == targetOffset
                            val isToday = targetOffset == 0

                            val dayLetter = SimpleDateFormat("EEE", Locale.getDefault()).format(calOffset.time)
                            val dayNumber = calOffset.get(Calendar.DAY_OF_MONTH).toString()
                            val fullDayName = SimpleDateFormat("EEEE", Locale.US).format(calOffset.time)

                            // Find courses scheduled for this specific day
                            val dayCourses = courses.filter { c ->
                                isCourseScheduledForDay(c.scheduleDays, fullDayName)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                    )
                                    .border(
                                        0.5.dp,
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                        },
                                        RoundedCornerShape(22.dp)
                                    )
                                    .clickable { selectedDateOffset = targetOffset }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = dayLetter.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = dayNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(5.dp))

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

                    // Lecture Cards with Live Timing & Inline Attendance Capsules
                    if (scheduledCourses.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.EventBusy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "No classes scheduled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            scheduledCourses.forEach { course ->
                                val courseColor = parseHexColor(course.colorHex, MaterialTheme.colorScheme.primary)

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
                                val liveProgress = if (isLiveNow && startMillis != null && endMillis != null && endMillis > startMillis) {
                                    ((currentTimeMillis - startMillis).toFloat() / (endMillis - startMillis)).coerceIn(0f, 1f)
                                } else 0f

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

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isLiveNow) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    },
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isLiveNow) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        // Main Row
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
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                                            modifier = Modifier.padding(horizontal = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = course.code,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                val scheduleText = when {
                                                    course.scheduleStartTime.isNotBlank() && course.scheduleEndTime.isNotBlank() ->
                                                        "${course.scheduleStartTime} - ${course.scheduleEndTime}"
                                                    course.schedule.isNotBlank() -> course.schedule
                                                    else -> ""
                                                }
                                                if (scheduleText.isNotBlank()) {
                                                    Text(
                                                        text = scheduleText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Modern Attendance Capsule Pill / Action Buttons
                                            if (existingAttendance != null) {
                                                val statusColor = when (existingAttendance.status.lowercase()) {
                                                    "present" -> Color(0xFF34C759)
                                                    "absent" -> MaterialTheme.colorScheme.error
                                                    "late" -> Color(0xFFFF9500)
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                                Surface(
                                                    shape = CircleShape,
                                                    color = statusColor.copy(alpha = 0.16f),
                                                    border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.4f)),
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .clickable {
                                                            val nextStatus = when (existingAttendance.status.lowercase()) {
                                                                "present" -> "Absent"
                                                                "absent" -> "Late"
                                                                else -> "Present"
                                                            }
                                                            viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, nextStatus)
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                    ) {
                                                        Icon(
                                                            if (existingAttendance.status.equals("present", true)) Icons.Rounded.Check else Icons.Rounded.Edit,
                                                            contentDescription = null,
                                                            tint = statusColor,
                                                            modifier = Modifier.size(13.dp)
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
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    // Quick Present Capsule Button
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFF34C759).copy(alpha = 0.15f),
                                                        border = BorderStroke(0.5.dp, Color(0xFF34C759).copy(alpha = 0.35f)),
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, "Present")
                                                            }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Rounded.Check,
                                                                contentDescription = "Present",
                                                                tint = Color(0xFF34C759),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }

                                                    // Quick Absent Capsule Button
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                viewModel.addAttendanceRecord(course.id, selectedDateStartMillis, "Absent")
                                                            }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Rounded.Close,
                                                                contentDescription = "Absent",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Live lecture pill indicator
                                        if (isLiveNow) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF34C759).copy(alpha = 0.12f),
                                                border = BorderStroke(0.5.dp, Color(0xFF34C759).copy(alpha = 0.35f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .graphicsLayer {
                                                                scaleX = livePulseScale
                                                                scaleY = livePulseScale
                                                                alpha = livePulseAlpha
                                                            }
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

                                            if (liveProgress > 0f) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { liveProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(3.dp)
                                                        .clip(CircleShape),
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

        // 3. Upcoming Assignments
        if (upcomingAssigns.isNotEmpty()) {
            item(key = "upcoming_assignments_section") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
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

                            GlassCapsule(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
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
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
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

        // 4. Active Tasks
        if (activeTasks.isNotEmpty()) {
            item(key = "active_tasks_section") {
                ScholarCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
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
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onNavigateToTasks() }
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeTasks.forEach { task ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleTaskCompleted(task) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Circular Checkbox
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    shape = CircleShape
                                                )
                                                .background(
                                                    if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (task.isCompleted) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

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

        // 5. Quick Notes Tool (If Enabled)
        if (betaNotes) {
            item(key = "notes_tool") {
                ScholarCard(
                    onClick = { navController.navigate("notes") { launchSingleTop = true } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
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
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), CircleShape),
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
                                    "Quick notes & equations",
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
