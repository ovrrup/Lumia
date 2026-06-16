package ovrrup.lumia.ui.screens

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

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.ArrowDropDown
import ovrrup.lumia.model.PracticeAssignment
import ovrrup.lumia.ui.components.BouncyIconButton
import ovrrup.lumia.ui.components.BouncyFloatingActionButton
import ovrrup.lumia.ui.components.BouncyButton
import ovrrup.lumia.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(navController: NavController, viewModel: ovrrup.lumia.viewmodel.ScholarViewModel) {
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isAodMode by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(PomodoroMode.WORK) }
    val context = LocalContext.current
    
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(emptyList())
    val courses by viewModel.courses.collectAsStateWithLifecycle(emptyList())
    val assignmentsList by viewModel.assignments.collectAsStateWithLifecycle(emptyList())
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())

    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var selectedAssignmentId by remember { mutableStateOf<Int?>(null) }
    var selectedTaskId by remember { mutableStateOf<Int?>(null) }

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

    val serviceState by ovrrup.lumia.service.PomodoroService.state.collectAsStateWithLifecycle()

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

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle(emptyList())

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
                ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context)
            }
            if (hasPerm) {
                ovrrup.lumia.util.TrueAodManager.showAodOverlay(
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
            ovrrup.lumia.util.TrueAodManager.dismissAodOverlay()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ovrrup.lumia.util.TrueAodManager.dismissAodOverlay()
        }
    }

    val isTrueAodActive = isAodMode && aodTrueAodEnabled && (if (aodTrueAodMode == "overlay") android.provider.Settings.canDrawOverlays(context) else ovrrup.lumia.service.AodAccessibilityService.isServiceEnabled(context))
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
                TopAppBar(
                    title = { Text("Pomodoro Timer", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        BouncyIconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    ovrrup.lumia.ui.components.NotificationPermissionPanel()
                    ovrrup.lumia.ui.components.ExactAlarmPermissionPanel()
                    ovrrup.lumia.ui.components.BatteryOptimizationPermissionPanel()
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
                                            val studiedMinutes = if (studiedSeconds > 0) {
                                                Math.max(1, (studiedSeconds + 30) / 60)
                                            } else {
                                                0
                                            }
                                            if (studiedMinutes > 0 && modeString == "WORK") {
                                                viewModel.addPomodoroSession(
                                                    durationMinutes = studiedMinutes,
                                                    subjectId = selectedSubjectId,
                                                    courseId = selectedCourseId,
                                                    assignmentId = selectedAssignmentId,
                                                    taskId = selectedTaskId
                                                )
                                                android.widget.Toast.makeText(context, "Saved session: $studiedMinutes min of focused study!", android.widget.Toast.LENGTH_LONG).show()
                                            } else if (studiedMinutes == 0 && modeString == "WORK") {
                                                android.widget.Toast.makeText(context, "Session too brief to log to history.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            isRunning = false
                                            isExitButtonShown = false
                                            val intent = android.content.Intent(context, ovrrup.lumia.service.PomodoroService::class.java).apply { action = "STOP" }
                                            context.startService(intent)
                                            timeLeft = workDuration * 60
                                            totalTime = workDuration * 60
                                        } else {
                                            if (now - lastClickTime < 500) {
                                                isExitButtonShown = true
                                                android.widget.Toast.makeText(context, "Transformed! Click the Orange button to Save & Exit.", android.widget.Toast.LENGTH_SHORT).show()
                                                lastClickTime = now
                                            } else {
                                                val intent = android.content.Intent(context, ovrrup.lumia.service.PomodoroService::class.java).apply { action = "PAUSE_RESUME" }
                                                context.startService(intent)
                                                lastClickTime = now
                                            }
                                        }
                                    } else {
                                        isRunning = true
                                        isExitButtonShown = false
                                        val intent = android.content.Intent(context, ovrrup.lumia.service.PomodoroService::class.java).apply {
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
                                modifier = Modifier.size(72.dp)
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
                                        val intent = android.content.Intent(context, ovrrup.lumia.service.PomodoroService::class.java).apply { action = "SKIP" }
                                        context.startService(intent)
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Skip",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            ovrrup.lumia.ui.components.BouncyFloatingActionButton(
                                onClick = { 
                                    isRunning = false
                                    isExitButtonShown = false
                                    val intent = android.content.Intent(context, ovrrup.lumia.service.PomodoroService::class.java).apply { action = "STOP" }
                                    context.startService(intent)
                                    timeLeft = workDuration * 60
                                    totalTime = workDuration * 60
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            ovrrup.lumia.ui.components.BouncyFloatingActionButton(
                                onClick = { isAodMode = true },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(72.dp)
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
                ovrrup.lumia.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    var expandedLink by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expandedLink = !expandedLink },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Link Session With Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(if (expandedLink) Icons.Rounded.ArrowDropDown else Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }
                        if (expandedLink) {
                            Spacer(Modifier.height(16.dp))
                            Text("Subject (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedSubjectId == null, onClick = { selectedSubjectId = null }, label = { Text("None") }) }
                                items(subjects) { subj ->
                                    FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id }, label = { Text(subj.name) })
                                }
                            }

                            Text("Course (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedCourseId == null, onClick = { selectedCourseId = null }, label = { Text("None") }) }
                                items(courses) { course ->
                                    FilterChip(selected = selectedCourseId == course.id, onClick = { selectedCourseId = course.id }, label = { Text(course.name) })
                                }
                            }

                            val courseAssignments = if (selectedCourseId != null) assignmentsList.filter { it.courseId == selectedCourseId } else assignmentsList
                            Text("Assignment (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedAssignmentId == null, onClick = { selectedAssignmentId = null }, label = { Text("None") }) }
                                items(courseAssignments) { assignment ->
                                    FilterChip(selected = selectedAssignmentId == assignment.id, onClick = { selectedAssignmentId = assignment.id }, label = { Text(assignment.title) })
                                }
                            }

                            Text("Task (Optional)", style = MaterialTheme.typography.labelSmall)
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(selected = selectedTaskId == null, onClick = { selectedTaskId = null }, label = { Text("None") }) }
                                items(tasks) { task ->
                                    FilterChip(selected = selectedTaskId == task.id, onClick = { selectedTaskId = task.id }, label = { Text(task.title) })
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
                    ovrrup.lumia.ui.components.GlassCard(
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
                    ovrrup.lumia.ui.components.GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Focus Session",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
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
}
}

@Composable
fun ModeButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ovrrup.lumia.ui.components.BouncyButton(
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
