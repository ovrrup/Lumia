package lumia.tracker.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import lumia.tracker.model.Subject
import lumia.tracker.service.PomodoroMode
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.focus.*
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroScreen - Redesigned, feature-packed Focus Space.
 * Provides interactive fluid pill-sliding mode selector (Work / Short Break / Long Break),
 * sweeping 280dp glowing timer arc, contextual academic course/subject linking,
 * OLED Zen mode, True AOD launcher, keep-screen-on toggle, and daily focus metrics.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Main focus and Pomodoro workspace screen with dynamic theme, fluid mode switching, and daily statistics",
    category = "Focus"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: ScholarViewModel,
    initialSubjectId: Int? = null,
    initialCourseId: Int? = null,
    initialAssignmentId: Int? = null,
    initialTaskId: Int? = null,
    initialTopicId: Int? = null
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(initialValue = emptyList())
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakCurrent.collectAsStateWithLifecycle(initialValue = 0)

    // Configured Durations from ViewModel
    val workDurationMin by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val shortBreakDurationMin by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
    val longBreakDurationMin by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()

    // UI State & Overlays
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isZenModeActive by remember { mutableStateOf(false) }
    var isAodModeActive by remember { mutableStateOf(false) }
    var keepScreenAwake by remember { mutableStateOf(false) }

    // Screen Keep Awake (Wake Lock)
    DisposableEffect(keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Context / Course / Subject Linking
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    LaunchedEffect(courses, subjects, initialCourseId, initialSubjectId, pomodoroState.courseId, pomodoroState.subjectId) {
        val targetCourseId = pomodoroState.courseId ?: initialCourseId
        if (targetCourseId != null && courses.isNotEmpty()) {
            selectedCourse = courses.find { it.id == targetCourseId }
        }
        val targetSubjectId = pomodoroState.subjectId ?: initialSubjectId
        if (targetSubjectId != null && subjects.isNotEmpty()) {
            selectedSubject = subjects.find { it.id == targetSubjectId }
        }
    }

    // Current Mode & Status
    val currentMode = remember(pomodoroState.modeString) {
        try { PomodoroMode.valueOf(pomodoroState.modeString) } catch (e: Exception) { PomodoroMode.WORK }
    }

    // Dynamic Color Branding (Mode or Course Theme)
    val ringColor = when {
        selectedCourse?.colorHex != null -> {
            try { Color(android.graphics.Color.parseColor(selectedCourse!!.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
        }
        currentMode == PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
        currentMode == PomodoroMode.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val statusLabel = when {
        pomodoroState.isAlarmActive -> "Alarm Firing"
        !pomodoroState.isRunning -> "Ready to Focus"
        pomodoroState.isPaused -> "Paused"
        currentMode == PomodoroMode.SHORT_BREAK -> "Short Break"
        currentMode == PomodoroMode.LONG_BREAK -> "Long Break"
        else -> "Focus Session"
    }

    // Helper: Send Intent actions to PomodoroService
    fun sendServiceAction(action: String, extras: (Intent.() -> Unit)? = null) {
        val intent = Intent(context, PomodoroService::class.java).apply {
            this.action = action
            extras?.invoke(this)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && action == "START") {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // True AOD Low-Power Mode Overlay
    if (isAodModeActive) {
        PomodoroAodOverlay(
            timeLeftSeconds = if (pomodoroState.isRunning) pomodoroState.timeLeft else workDurationMin * 60,
            sessionsCompleted = pomodoroState.sessionsCompleted,
            modeString = currentMode.name,
            isRunning = pomodoroState.isRunning,
            isPaused = pomodoroState.isPaused,
            onPauseResume = { sendServiceAction("PAUSE_RESUME") },
            onClose = { isAodModeActive = false }
        )
        return
    }

    // Zen Fullscreen AMOLED Mode Overlay
    if (isZenModeActive) {
        PomodoroZenOverlay(
            timeLeftSeconds = if (pomodoroState.isRunning) pomodoroState.timeLeft else workDurationMin * 60,
            originalTimeSeconds = if (pomodoroState.isRunning) pomodoroState.originalTime else workDurationMin * 60,
            statusLabel = statusLabel,
            ringColor = ringColor,
            isRunning = pomodoroState.isRunning,
            isPaused = pomodoroState.isPaused,
            onPauseResume = { sendServiceAction("PAUSE_RESUME") },
            onClose = { isZenModeActive = false }
        )
        return
    }

    // Settings Modal Bottom Sheet
    if (showSettingsSheet) {
        PomodoroSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsSheet = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FOCUS SPACE",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            letterSpacing = 1.8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val cycleIndex = (pomodoroState.sessionsCompleted % periodSessions) + 1
                        Text(
                            text = "Session $cycleIndex of $periodSessions • Cycle ${(pomodoroState.sessionsCompleted / periodSessions) + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ringColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Zen Mode Quick Button
                    BouncyIconButton(onClick = { isZenModeActive = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Zen Immersion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Keep Screen Awake Toggle
                    BouncyIconButton(onClick = { keepScreenAwake = !keepScreenAwake }) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Keep Screen On",
                            tint = if (keepScreenAwake) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    // Settings Button
                    BouncyIconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subtle ambient mode background glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ringColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Fluid Sliding Pill Mode Switcher (Work / Short Break / Long Break)
                FluidPillModeSelector(
                    currentMode = currentMode,
                    workDurationMin = workDurationMin,
                    shortBreakDurationMin = shortBreakDurationMin,
                    longBreakDurationMin = longBreakDurationMin,
                    onSelectMode = { mode ->
                        sendServiceAction("SWITCH_MODE") { putExtra("targetMode", mode.name) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Centered Sweeping Timer Arc Gauge
                val defaultDurationSec = when (currentMode) {
                    PomodoroMode.WORK -> workDurationMin * 60
                    PomodoroMode.SHORT_BREAK -> shortBreakDurationMin * 60
                    PomodoroMode.LONG_BREAK -> longBreakDurationMin * 60
                }

                PomodoroTimerArc(
                    timeLeftSeconds = if (pomodoroState.isRunning) pomodoroState.timeLeft else defaultDurationSec,
                    originalTimeSeconds = if (pomodoroState.isRunning) pomodoroState.originalTime else defaultDurationSec,
                    statusLabel = statusLabel,
                    ringColor = ringColor,
                    sessionsCompleted = pomodoroState.sessionsCompleted,
                    periodSessions = periodSessions,
                    isRunning = pomodoroState.isRunning,
                    isPaused = pomodoroState.isPaused,
                    onAdjustTime = if (pomodoroState.isRunning) {
                        { delta -> sendServiceAction("ADJUST_TIME") { putExtra("deltaSeconds", delta) } }
                    } else null
                )

                // 3. Contextual Course & Subject Linking
                PomodoroCourseSelector(
                    courses = courses,
                    selectedCourse = selectedCourse,
                    onSelectCourse = { course ->
                        selectedCourse = course
                        if (pomodoroState.isRunning) {
                            sendServiceAction("UPDATE_CONTEXT") {
                                putExtra("courseId", course?.id ?: -1)
                                putExtra("subjectId", course?.subjectId ?: selectedSubject?.id ?: -1)
                            }
                        }
                    },
                    subjects = subjects,
                    selectedSubject = selectedSubject,
                    onSelectSubject = { subject ->
                        selectedSubject = subject
                        if (pomodoroState.isRunning) {
                            sendServiceAction("UPDATE_CONTEXT") {
                                putExtra("subjectId", subject?.id ?: -1)
                                putExtra("courseId", selectedCourse?.id ?: -1)
                            }
                        }
                    }
                )

                // 4. Primary and Secondary Action Controls
                PomodoroControls(
                    isRunning = pomodoroState.isRunning,
                    isPaused = pomodoroState.isPaused,
                    isAlarmActive = pomodoroState.isAlarmActive,
                    onStart = {
                        sendServiceAction("START") {
                            putExtra("workDuration", workDurationMin * 60)
                            putExtra("shortBreakDuration", shortBreakDurationMin * 60)
                            putExtra("longBreakDuration", longBreakDurationMin * 60)
                            putExtra("periodSessions", periodSessions)
                            if (selectedCourse != null) {
                                putExtra("courseId", selectedCourse?.id ?: -1)
                                putExtra("subjectId", selectedCourse?.subjectId ?: -1)
                            } else if (selectedSubject != null) {
                                putExtra("subjectId", selectedSubject?.id ?: -1)
                            }
                            if (initialAssignmentId != null) putExtra("assignmentId", initialAssignmentId)
                            if (initialTaskId != null) putExtra("taskId", initialTaskId)
                            if (initialTopicId != null) putExtra("topicId", initialTopicId)
                        }
                    },
                    onPauseResume = { sendServiceAction("PAUSE_RESUME") },
                    onSkip = { sendServiceAction("SKIP") },
                    onStop = { sendServiceAction("STOP") },
                    onStopAlarm = { sendServiceAction("STOP_ALARM") },
                    onStartAod = { isAodModeActive = true },
                    onOpenZenMode = { isZenModeActive = true },
                    onOpenSettings = { showSettingsSheet = true },
                    viewModel = viewModel
                )

                // 5. Daily Metrics Row (Sessions Today, Total Focus, Streak)
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Sessions Today
                        MetricBadgeItem(
                            icon = Icons.Rounded.CheckCircle,
                            value = "${pomodoroState.sessionsCompleted}",
                            label = "Sessions Today",
                            tintColor = MaterialTheme.colorScheme.primary
                        )

                        VerticalDivider(
                            modifier = Modifier.height(34.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // 2. Total Focus Time
                        val totalMins = pomodoroState.sessionsCompleted * workDurationMin
                        val timeDisplay = if (totalMins >= 60) {
                            String.format("%.1fh", totalMins / 60.0f)
                        } else {
                            "${totalMins}m"
                        }
                        MetricBadgeItem(
                            icon = Icons.Rounded.Timer,
                            value = timeDisplay,
                            label = "Total Focus",
                            tintColor = MaterialTheme.colorScheme.secondary
                        )

                        VerticalDivider(
                            modifier = Modifier.height(34.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // 3. Current Streak
                        MetricBadgeItem(
                            icon = Icons.Rounded.LocalFireDepartment,
                            value = "$streakDays Days",
                            label = "Streak",
                            tintColor = Color(0xFFFF9500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * FluidPillModeSelector - Mode selector with fluid animated sliding pill indicator.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Fluid sliding pill mode switcher for Work, Short Break, and Long Break intervals",
    category = "Focus"
)
@Composable
fun FluidPillModeSelector(
    currentMode: PomodoroMode,
    workDurationMin: Int,
    shortBreakDurationMin: Int,
    longBreakDurationMin: Int,
    onSelectMode: (PomodoroMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = when (currentMode) {
        PomodoroMode.WORK -> 0
        PomodoroMode.SHORT_BREAK -> 1
        PomodoroMode.LONG_BREAK -> 2
    }

    val activeColor by animateColorAsState(
        targetValue = when (currentMode) {
            PomodoroMode.WORK -> MaterialTheme.colorScheme.primary
            PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
            PomodoroMode.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(300),
        label = "pill_active_color"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            val tabWidth = (maxWidth - 8.dp) / 3
            val animatedOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex + (selectedIndex * 4).dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "pill_sliding_offset"
            )

            // Sliding Indicator Background Pill
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(activeColor)
            )

            // Interactive Tab Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PillModeTabItem(
                    title = "Focus",
                    durationText = "${workDurationMin}m",
                    icon = Icons.Rounded.Psychology,
                    isSelected = currentMode == PomodoroMode.WORK,
                    onClick = { onSelectMode(PomodoroMode.WORK) },
                    modifier = Modifier.weight(1f)
                )
                PillModeTabItem(
                    title = "Short Break",
                    durationText = "${shortBreakDurationMin}m",
                    icon = Icons.Rounded.Coffee,
                    isSelected = currentMode == PomodoroMode.SHORT_BREAK,
                    onClick = { onSelectMode(PomodoroMode.SHORT_BREAK) },
                    modifier = Modifier.weight(1f)
                )
                PillModeTabItem(
                    title = "Long Break",
                    durationText = "${longBreakDurationMin}m",
                    icon = Icons.Rounded.SelfImprovement,
                    isSelected = currentMode == PomodoroMode.LONG_BREAK,
                    onClick = { onSelectMode(PomodoroMode.LONG_BREAK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * PillModeTabItem - Content layer for individual mode tab with animated text/icon colors.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Individual mode tab item for the fluid pill mode switcher",
    category = "Focus"
)
@Composable
fun PillModeTabItem(
    title: String,
    durationText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "pill_tab_content_color"
    )
    val subtitleColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
        animationSpec = tween(250),
        label = "pill_tab_sub_color"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = durationText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = subtitleColor
        )
    }
}

@ValueScore(
    score = 62,
    importance = Importance.LOW,
    description = "Daily metric badge item for completed sessions, total focus time, and streaks",
    category = "Focus"
)
@Composable
private fun MetricBadgeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tintColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tintColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
