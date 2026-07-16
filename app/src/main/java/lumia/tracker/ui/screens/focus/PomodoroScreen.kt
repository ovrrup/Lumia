package lumia.tracker.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.ArrowDropDown
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: lumia.tracker.viewmodel.ScholarViewModel,
    initialSubjectId: Int? = null,
    initialCourseId: Int? = null,
    initialAssignmentId: Int? = null,
    initialTaskId: Int? = null,
    initialTopicId: Int? = null
) {
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isAodMode by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(PomodoroMode.WORK) }
    val context = LocalContext.current
    
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.Subject>())
    val courses by viewModel.courses.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.Course>())
    val assignmentsList by viewModel.assignments.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.PracticeAssignment>())
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.Task>())
    val allTopicsGlobal by viewModel.allTopics.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.Topic>())

    var selectedSubjectId by remember(initialSubjectId) { mutableStateOf<Int?>(initialSubjectId) }
    var selectedCourseId by remember(initialCourseId) { mutableStateOf<Int?>(initialCourseId) }
    var selectedAssignmentId by remember(initialAssignmentId) { mutableStateOf<Int?>(initialAssignmentId) }
    var selectedTaskId by remember(initialTaskId) { mutableStateOf<Int?>(initialTaskId) }
    var selectedTopicId by remember(initialTopicId) { mutableStateOf<Int?>(initialTopicId) }

    val updateServiceContext = { key: String, value: Int? ->
        if (isRunning) {
            val updateIntent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply {
                action = "UPDATE_CONTEXT"
                putExtra(key, value ?: -1)
            }
            context.startService(updateIntent)
        }
    }

    val relevantTopics = remember(allTopicsGlobal, selectedSubjectId, selectedCourseId, courses) {
        val finalSubjectId = selectedSubjectId ?: selectedCourseId?.let { cid -> courses.find { it.id == cid }?.subjectId }
        if (finalSubjectId != null) {
            allTopicsGlobal.filter { it.subjectId == finalSubjectId }
        } else {
            allTopicsGlobal
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var sessionsCompleted by remember { mutableStateOf(0) }
    var modeString by remember { mutableStateOf("WORK") }
    var isExitButtonShown by remember { mutableStateOf(false) }

    val systemPomodoroAutoLog by viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle()
    val workDuration by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val shortBreakDuration by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
    val longBreakDuration by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
    val periodSessions by viewModel.pomodoroPeriodSessions.collectAsStateWithLifecycle()
    val enablePeriodTarget by viewModel.pomodoroEnablePeriodTarget.collectAsStateWithLifecycle()
    
    var totalTime by remember { mutableStateOf(workDuration * 60) }

    val serviceState by lumia.tracker.service.PomodoroService.state.collectAsStateWithLifecycle()

    LaunchedEffect(serviceState, workDuration) {
        if (serviceState.isRunning) {
            isRunning = true
            timeLeft = serviceState.timeLeft
            totalTime = serviceState.originalTime
            modeString = serviceState.modeString
            isPaused = serviceState.isPaused
            sessionsCompleted = serviceState.sessionsCompleted
            selectedSubjectId = serviceState.subjectId
            selectedCourseId = serviceState.courseId
            selectedAssignmentId = serviceState.assignmentId
            selectedTaskId = serviceState.taskId
            selectedTopicId = serviceState.topicId
        } else {
            isRunning = false
            timeLeft = workDuration * 60
            totalTime = workDuration * 60
            isPaused = false
            sessionsCompleted = 0
            modeString = "WORK"
            isExitButtonShown = false
        }
    }

    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle(emptyList<lumia.tracker.model.PomodoroSession>())

    val aodTrueBlackOled by viewModel.aodTrueBlackOled.collectAsStateWithLifecycle()
    val aodAutoDeactivateTrueBlack by viewModel.aodAutoDeactivateTrueBlack.collectAsStateWithLifecycle()
    val aodBurnInShiftSpeed by viewModel.aodBurnInShiftSpeed.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicBg by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
    val isGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
    val aodLockScreenSupport by viewModel.aodLockScreenSupport.collectAsStateWithLifecycle()

    val aodTrueAodEnabled by viewModel.aodTrueAodEnabled.collectAsStateWithLifecycle()
    val aodTrueAodMode by viewModel.aodTrueAodMode.collectAsStateWithLifecycle()
    val aodSensitivity by viewModel.aodSensitivity.collectAsStateWithLifecycle()
    val aodDimnessLevel by viewModel.aodDimnessLevel.collectAsStateWithLifecycle()
    val aodLockTimeout by viewModel.aodLockTimeout.collectAsStateWithLifecycle()

    val isTrueBlack = remember(aodTrueBlackOled, aodAutoDeactivateTrueBlack, themeMode, isDynamicBg, isGlassUi) {
        aodTrueBlackOled && !(aodAutoDeactivateTrueBlack && (themeMode == "Light" || isDynamicBg || isGlassUi))
    }

    LaunchedEffect(isAodMode, aodLockScreenSupport) {
        val activity = context as? android.app.Activity
        if (activity != null) {
            if (isAodMode) {
                if (aodLockScreenSupport) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        activity.setShowWhenLocked(true)
                        activity.setTurnScreenOn(true)
                    } else {
                        activity.window.addFlags(
                            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        )
                    }
                }
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(false)
                    activity.setTurnScreenOn(false)
                } else {
                    activity.window.clearFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    )
                }
                activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    LaunchedEffect(isAodMode, aodTrueAodEnabled, aodTrueAodMode, aodDimnessLevel, aodSensitivity, aodLockTimeout, viewModel.aodMotionSensitivity.value) {
        if (isAodMode && aodTrueAodEnabled) {
            val hasPerm = if (aodTrueAodMode == "overlay") {
                android.provider.Settings.canDrawOverlays(context)
            } else {
                lumia.tracker.service.AodAccessibilityService.isServiceEnabled(context)
            }
            if (hasPerm) {
                lumia.tracker.util.TrueAodManager.showAodOverlay(
                    context = context,
                    useAccessibility = (aodTrueAodMode == "accessibility"),
                    dimnessLevel = aodDimnessLevel,
                    sensitivity = aodSensitivity,
                    motionSensitivity = viewModel.aodMotionSensitivity.value,
                    lockTimeoutSeconds = if (aodTrueAodMode == "accessibility") aodLockTimeout else 0,
                    onExit = {
                        isAodMode = false
                    }
                )
            } else {
                android.widget.Toast.makeText(context, "Permission for True AOD not granted. Falling back to in-app AOD.", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            lumia.tracker.util.TrueAodManager.dismissAodOverlay()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            lumia.tracker.util.TrueAodManager.dismissAodOverlay()
        }
    }

    val isTrueAodActive = isAodMode && aodTrueAodEnabled && (if (aodTrueAodMode == "overlay") android.provider.Settings.canDrawOverlays(context) else lumia.tracker.service.AodAccessibilityService.isServiceEnabled(context))
    if (isAodMode && !isTrueAodActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isTrueBlack) Color.Black else MaterialTheme.colorScheme.background)
                .clickable { isAodMode = false },
            contentAlignment = Alignment.Center
        ) {
            if (!isTrueBlack) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                )
            }
            
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            
            val burnInOffset = remember(timeLeft, aodBurnInShiftSpeed) {
                val shiftSpeed = if (aodBurnInShiftSpeed > 0) aodBurnInShiftSpeed else 10
                val tickFraction = timeLeft / shiftSpeed
                val tick = (tickFraction % 10)
                val x = if (tick % 2 == 0) (tick - 5).dp else 0.dp
                val y = if (tick % 2 != 0) (tick - 5).dp else 0.dp
                Pair(x, y)
            }

             Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp).offset(x = burnInOffset.first, y = burnInOffset.second)
            ) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 110.sp),
                    fontWeight = FontWeight.Light,
                    color = if (isTrueBlack) Color.DarkGray.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val statusText = if (isRunning) {
                    val contextName = subjects.find { it.id == selectedSubjectId }?.name ?: "Focus Block"
                    "Focusing on $contextName"
                } else {
                    "Session Paused"
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isTrueBlack) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = "Always-On Focus Active • Tap to Exit",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTrueBlack) Color.DarkGray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        Scaffold(
            containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                lumia.tracker.ui.components.UniversalCapsuleHeader(
                    title = "Pomodoro Timer",
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    lumia.tracker.ui.components.NotificationPermissionPanel()
                    lumia.tracker.ui.components.ExactAlarmPermissionPanel()
                    lumia.tracker.ui.components.BatteryOptimizationPermissionPanel()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Period Structure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val modeFancy = when(modeString) { "SHORT_BREAK" -> "Short Rest"; "LONG_BREAK" -> "Long Rest"; else -> "Focusing" }
                            Text(
                                text = "Session ${sessionsCompleted + 1}/$periodSessions • $modeFancy",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            val activeCourse = selectedCourseId?.let { cid -> courses.find { it.id == cid } }
                            val activeSubject = selectedSubjectId?.let { sid -> subjects.find { it.id == sid } }
                            val activeTopic = selectedTopicId?.let { tid -> allTopicsGlobal.find { it.id == tid } }
                            val activeAssignment = selectedAssignmentId?.let { aid -> assignmentsList.find { it.id == aid } }
                            val activeTask = selectedTaskId?.let { tid -> tasks.find { it.id == tid } }
                            
                            val connectionText = when {
                                activeTask != null -> "Task: ${activeTask.title}"
                                activeAssignment != null -> "Assignment: ${activeAssignment.title}"
                                activeTopic != null -> "Topic: ${activeTopic.title}"
                                activeSubject != null && activeCourse != null -> "${activeCourse.name} - ${activeSubject.name}"
                                activeCourse != null -> "Course: ${activeCourse.name}"
                                activeSubject != null -> "Subject: ${activeSubject.name}"
                                else -> null
                            }
                            
                            if (connectionText != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Connected Context",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = connectionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Beautiful pulsing scale animation
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isRunning && !isPaused) 1.03f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.Transparent)
                    ) {
                        val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
                        val circleColor = when (modeString) { "SHORT_BREAK" -> MaterialTheme.colorScheme.secondary; "LONG_BREAK" -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.primary }
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = circleColor.copy(alpha = 0.15f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx())
                            )
                            drawArc(
                                color = circleColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }

                        val minutes = timeLeft / 60
                        val seconds = timeLeft % 60
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                        ) {
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val statusStr = if (!isRunning) "Stopped" else if (isPaused) "Paused" else when(modeString) { "SHORT_BREAK" -> "Short Rest"; "LONG_BREAK" -> "Long Rest"; else -> "Focusing" }
                            Text(
                                text = statusStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = circleColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf(
                            Pair(15, "15m"),
                            Pair(25, "25m"),
                            Pair(45, "45m"),
                            Pair(60, "60m")
                        )
                        presets.forEach { (time, label) ->
                            val isSelected = workDuration == time
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isRunning) {
                                        android.widget.Toast.makeText(context, "Stop the active timer to change presets", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updatePomodoroWorkDuration(time)
                                        android.widget.Toast.makeText(context, "Focus preset updated to $time minutes", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    var lastClickTime by remember { mutableStateOf(0L) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            BouncyFloatingActionButton(
                                onClick = { 
                                    val now = System.currentTimeMillis()
                                    if (isRunning) {
                                        if (isExitButtonShown) {
                                            // Exit and Save study progress safely!
                                            val studiedSeconds = totalTime - timeLeft
                                            val studiedMinutes = studiedSeconds / 60
                                            if (studiedMinutes > 0 && modeString == "WORK") {
                                                viewModel.addPomodoroSession(
                                                    durationMinutes = studiedMinutes,
                                                    subjectId = selectedSubjectId,
                                                    courseId = selectedCourseId,
                                                    assignmentId = selectedAssignmentId,
                                                    taskId = selectedTaskId,
                                                    topicId = selectedTopicId
                                                )
                                            } else if (studiedMinutes == 0 && modeString == "WORK") {
                                                // Session cancelled
                                            }
                                            isRunning = false
                                            isExitButtonShown = false
                                            val intent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply { 
                                                action = "STOP"
                                                putExtra("alreadySaved", true)
                                            }
                                            context.startService(intent)
                                            timeLeft = workDuration * 60
                                            totalTime = workDuration * 60
                                        } else {
                                            if (now - lastClickTime < 500) {
                                                isExitButtonShown = true
                                                android.widget.Toast.makeText(context, "Click the Stop button again to Save & Exit.", android.widget.Toast.LENGTH_SHORT).show()
                                                lastClickTime = now
                                            } else {
                                                val intent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply { action = "PAUSE_RESUME" }
                                                context.startService(intent)
                                                lastClickTime = now
                                            }
                                        }
                                    } else {
                                        isRunning = true
                                        isExitButtonShown = false
                                        val intent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply {
                                            action = "START"
                                            putExtra("workDuration", workDuration * 60)
                                            putExtra("shortBreakDuration", shortBreakDuration * 60)
                                            putExtra("longBreakDuration", longBreakDuration * 60)
                                            putExtra("periodSessions", periodSessions)
                                            putExtra("maxPeriods", if (enablePeriodTarget) 1 else -1)
                                            selectedSubjectId?.let { putExtra("subjectId", it) }
                                            selectedCourseId?.let { putExtra("courseId", it) }
                                            selectedAssignmentId?.let { putExtra("assignmentId", it) }
                                            selectedTaskId?.let { putExtra("taskId", it) }
                                            selectedTopicId?.let { putExtra("topicId", it) }
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    }
                                },
                                containerColor = if (isExitButtonShown) {
                                    Color(0xFFF97316)
                                } else if (isRunning && !isPaused) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                modifier = Modifier.size(72.dp).testTag("play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isExitButtonShown) {
                                        Icons.Rounded.CheckCircle
                                    } else if (isRunning && !isPaused) {
                                        Icons.Rounded.Pause
                                    } else {
                                        Icons.Rounded.PlayArrow
                                    },
                                    contentDescription = if (isExitButtonShown) "Save and Exit" else "Play/Pause",
                                    modifier = Modifier.size(36.dp),
                                    tint = if (isExitButtonShown) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Skip Button
                            if (isRunning) {
                                BouncyFloatingActionButton(
                                    onClick = { 
                                        isExitButtonShown = false
                                        val intent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply { action = "SKIP" }
                                        context.startService(intent)
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(72.dp).testTag("skip_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Skip",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            lumia.tracker.ui.components.BouncyFloatingActionButton(
                                onClick = { 
                                    isRunning = false
                                    isExitButtonShown = false
                                    val intent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply { action = "STOP" }
                                    context.startService(intent)
                                    timeLeft = workDuration * 60
                                    totalTime = workDuration * 60
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(72.dp).testTag("reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            lumia.tracker.ui.components.BouncyFloatingActionButton(
                                onClick = { isAodMode = true },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(72.dp).testTag("aod_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NightlightRound,
                                    contentDescription = "Always On Display focus mode",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        if (isExitButtonShown) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFF97316).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Double Tap Registered! Click the Orange check button to Save and Exit, writing study progress (${Math.max(1, (totalTime - timeLeft + 30) / 60)} min) safely to history.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Link Session Block
                lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    var expandedLink by remember { 
                        mutableStateOf(
                            initialSubjectId != null || 
                            initialCourseId != null || 
                            initialAssignmentId != null || 
                            initialTaskId != null || 
                            initialTopicId != null
                        ) 
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expandedLink = !expandedLink },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Link Session With Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (expandedLink) 180f else 0f
                                }
                            )
                        }
                        if (expandedLink) {
                            Spacer(Modifier.height(16.dp))
                            Text("Subject (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedSubjectId == null, onClick = { selectedSubjectId = null; updateServiceContext("subjectId", null) }, label = { Text("None") }) }
                                items(subjects) { subj ->
                                    FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id; updateServiceContext("subjectId", subj.id) }, label = { Text(subj.name) })
                                }
                            }

                            Text("Course (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedCourseId == null, onClick = { selectedCourseId = null; updateServiceContext("courseId", null) }, label = { Text("None") }) }
                                items(courses) { course ->
                                    FilterChip(selected = selectedCourseId == course.id, onClick = { selectedCourseId = course.id; updateServiceContext("courseId", course.id) }, label = { Text(course.name) })
                                }
                            }

                            Text("Topic (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedTopicId == null, onClick = { selectedTopicId = null; updateServiceContext("topicId", null) }, label = { Text("None") }) }
                                items(relevantTopics) { topic ->
                                    FilterChip(selected = selectedTopicId == topic.id, onClick = { selectedTopicId = topic.id; updateServiceContext("topicId", topic.id) }, label = { Text(topic.title) })
                                }
                            }

                            val courseAssignments = if (selectedCourseId != null) assignmentsList.filter { it.courseId == selectedCourseId } else assignmentsList
                            Text("Assignment (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedAssignmentId == null, onClick = { selectedAssignmentId = null; updateServiceContext("assignmentId", null) }, label = { Text("None") }) }
                                items(courseAssignments) { assignment ->
                                    FilterChip(selected = selectedAssignmentId == assignment.id, onClick = { selectedAssignmentId = assignment.id; updateServiceContext("assignmentId", assignment.id) }, label = { Text(assignment.title) })
                                }
                            }

                            Text("Task (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedTaskId == null, onClick = { selectedTaskId = null; updateServiceContext("taskId", null) }, label = { Text("None") }) }
                                items(tasks) { task ->
                                    FilterChip(selected = selectedTaskId == task.id, onClick = { selectedTaskId = task.id; updateServiceContext("taskId", task.id) }, label = { Text(task.title) })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Productivity History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (pomodoroSessions.isEmpty()) {
                item {
                    lumia.tracker.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No sessions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(pomodoroSessions.sortedByDescending { it.dateMillis }.take(5)) { session ->
                    lumia.tracker.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                val courseName = session.courseId?.let { cid -> courses.find { it.id == cid }?.name }
                                val subjectName = session.subjectId?.let { sid -> subjects.find { it.id == sid }?.name }
                                val assignmentName = session.assignmentId?.let { aid -> assignmentsList.find { it.id == aid }?.title }
                                val taskName = session.taskId?.let { tid -> tasks.find { it.id == tid }?.title }
                                val topicName = session.topicId?.let { tid -> allTopicsGlobal.find { it.id == tid }?.title }
                                val contextualText = when {
                                    topicName != null && courseName != null -> "Topic: $topicName ($courseName)"
                                    topicName != null -> "Topic: $topicName"
                                    assignmentName != null -> "Assignment: $assignmentName"
                                    taskName != null -> "Task: $taskName"
                                    courseName != null && subjectName != null -> "$courseName - $subjectName"
                                    courseName != null -> "Course: $courseName"
                                    subjectName != null -> "Subject: $subjectName"
                                    else -> "Focus Session"
                                }
                                Text(
                                    text = contextualText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                val dateStr = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(
                                    java.util.Date(session.dateMillis)
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${session.durationMinutes}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (serviceState.isAlarmActive) {
        val endedMode = serviceState.endedModeStr.ifBlank { "WORK" }
        val nextMode = serviceState.modeString
        
        val titleText = when {
            endedMode == "WORK" && nextMode == "LONG_BREAK" -> "Deep Focus Period Complete!"
            endedMode == "WORK" -> "Focus Session Completed!"
            endedMode == "SHORT_BREAK" -> "Short Break Finished!"
            endedMode == "LONG_BREAK" -> "Long Break Finished!"
            else -> "Timer Completed!"
        }
        
        val bodyText = when {
            endedMode == "WORK" && nextMode == "LONG_BREAK" -> 
                "Incredible dedication! You have successfully completed this entire focus period. You've earned a longer, well-deserved break to fully recharge your energy."
            endedMode == "WORK" -> 
                "Spectacular job! Your focused work session is over. It's time to rest your eyes, stand up, stretch, and let your mind unwind for a bit."
            endedMode == "SHORT_BREAK" -> 
                "Break time is up! Hope you feel refreshed and relaxed. Let's redirect our focus and crush the next study block together!"
            endedMode == "LONG_BREAK" -> 
                "Welcome back! Your extended downtime is complete, and your mind is fully restored. Let's step back into deep focus mode and make amazing progress!"
            else -> 
                "Fantastic effort! Your focus timer has completed."
        }

        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val serviceIntent = android.content.Intent(context, lumia.tracker.service.PomodoroService::class.java).apply {
                            action = "STOP_ALARM"
                        }
                        context.startService(serviceIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("stop_alarm_button")
                ) {
                    Text("Stop Alarm", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
}

@Composable
fun ModeButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    lumia.tracker.ui.components.BouncyButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    ) {
        Text(text, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodySmall)
    }
}

enum class PomodoroMode { WORK, SHORT_BREAK, LONG_BREAK }
