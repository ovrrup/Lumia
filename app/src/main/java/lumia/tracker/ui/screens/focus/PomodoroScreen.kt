package lumia.tracker.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.service.PomodoroMode
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.focus.*
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * PomodoroScreen - Completely redesigned, feature-packed Focus Space.
 * Provides interactive mode selection (Work / Short Break / Long Break), circular glowing gauge,
 * dynamic academic context linking, fullscreen OLED zen mode, session metrics, and customizable timer intervals.
 */
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
    val pomodoroState by PomodoroService.state.collectAsStateWithLifecycle()

    // Configured Durations from ViewModel
    val workDurationMin by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val shortBreakDurationMin by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
    val longBreakDurationMin by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()

    // UI State & Sheets
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

    // Context / Course Linking
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    LaunchedEffect(courses, initialCourseId, pomodoroState.courseId) {
        val targetId = pomodoroState.courseId ?: initialCourseId
        if (targetId != null && courses.isNotEmpty()) {
            selectedCourse = courses.find { it.id == targetId }
        }
    }

    // Dynamic Ring Color Branding
    val ringColor = selectedCourse?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.primary

    // Current Mode & Status Label
    val currentMode = remember(pomodoroState.modeString) {
        try { PomodoroMode.valueOf(pomodoroState.modeString) } catch (e: Exception) { PomodoroMode.WORK }
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

    // Zen Fullscreen Mode Overlay
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

    // Settings Modal Sheet
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
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cycle Session ${(pomodoroState.sessionsCompleted % periodSessions) + 1} of $periodSessions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
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
                    // Keep Screen Awake Toggle
                    BouncyIconButton(onClick = { keepScreenAwake = !keepScreenAwake }) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Keep Screen On",
                            tint = if (keepScreenAwake) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    // Settings Button
                    BouncyIconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Interactive Mode Switcher Tabs (Work / Short Break / Long Break)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModeTabItem(
                        title = "Focus (${workDurationMin}m)",
                        isSelected = currentMode == PomodoroMode.WORK,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            sendServiceAction("SWITCH_MODE") { putExtra("targetMode", "WORK") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeTabItem(
                        title = "Short (${shortBreakDurationMin}m)",
                        isSelected = currentMode == PomodoroMode.SHORT_BREAK,
                        activeColor = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            sendServiceAction("SWITCH_MODE") { putExtra("targetMode", "SHORT_BREAK") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeTabItem(
                        title = "Long (${longBreakDurationMin}m)",
                        isSelected = currentMode == PomodoroMode.LONG_BREAK,
                        activeColor = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            sendServiceAction("SWITCH_MODE") { putExtra("targetMode", "LONG_BREAK") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Main Countdown Gauge Arc with Cycle Dots & Quick Time Nudges
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
                onAdjustTime = if (pomodoroState.isRunning) {
                    { delta -> sendServiceAction("ADJUST_TIME") { putExtra("deltaSeconds", delta) } }
                } else null
            )

            // 3. Course & Academic Study Linking Selector
            PomodoroCourseSelector(
                courses = courses,
                selectedCourse = selectedCourse,
                onSelectCourse = { course ->
                    selectedCourse = course
                    if (pomodoroState.isRunning) {
                        sendServiceAction("UPDATE_CONTEXT") {
                            putExtra("courseId", course?.id ?: -1)
                            putExtra("subjectId", course?.subjectId ?: -1)
                        }
                    }
                }
            )

            // 4. Primary & Secondary Control Buttons
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
                        }
                    }
                },
                onPauseResume = { sendServiceAction("PAUSE_RESUME") },
                onSkip = { sendServiceAction("SKIP") },
                onStop = { sendServiceAction("STOP") },
                onStopAlarm = { sendServiceAction("STOP_ALARM") },
                onStartAod = {
                    isAodModeActive = true
                },
                onOpenZenMode = { isZenModeActive = true },
                onOpenSettings = { showSettingsSheet = true }
            )

            // 5. Today's Focus Metrics Summary Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FocusMetricItem(
                        icon = Icons.Rounded.Timer,
                        value = "${pomodoroState.sessionsCompleted * workDurationMin}",
                        unit = "mins",
                        label = "Focus Time"
                    )
                    VerticalDivider(
                        modifier = Modifier.height(36.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    FocusMetricItem(
                        icon = Icons.Rounded.CheckCircle,
                        value = "${pomodoroState.sessionsCompleted}",
                        unit = "sessions",
                        label = "Completed"
                    )
                    VerticalDivider(
                        modifier = Modifier.height(36.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    FocusMetricItem(
                        icon = Icons.Rounded.Autorenew,
                        value = "${(pomodoroState.sessionsCompleted / periodSessions)}",
                        unit = "cycles",
                        label = "Full Cycles"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ModeTabItem - Animated tab pill for selecting Pomodoro study/break modes.
 */
@Composable
private fun ModeTabItem(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeColor else Color.Transparent,
        shadowElevation = if (isSelected) 1.5.dp else 0.dp,
        modifier = modifier
            .bouncyClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    unit: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
