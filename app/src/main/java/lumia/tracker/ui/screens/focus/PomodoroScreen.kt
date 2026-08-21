package lumia.tracker.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.service.PomodoroService
import lumia.tracker.ui.screens.focus.PomodoroControls
import lumia.tracker.ui.screens.focus.PomodoroCourseSelector
import lumia.tracker.ui.screens.focus.PomodoroTimerArc
import lumia.tracker.viewmodel.ScholarViewModel

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
    
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(courses, initialCourseId, pomodoroState.courseId) {
        val targetId = pomodoroState.courseId ?: initialCourseId
        if (targetId != null && courses.isNotEmpty()) {
            selectedCourse = courses.find { it.id == targetId }
        }
    }

    val ringColor = selectedCourse?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.primary

    val statusLabel = when {
        pomodoroState.isAlarmActive -> "Alarm Active"
        !pomodoroState.isRunning -> "Ready"
        pomodoroState.isPaused -> "Paused"
        pomodoroState.modeString == "SHORT_BREAK" -> "Short Break"
        pomodoroState.modeString == "LONG_BREAK" -> "Long Break"
        else -> "Stay Focused"
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FOCUS SPACE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            PomodoroTimerArc(
                timeLeftSeconds = if (pomodoroState.isRunning) pomodoroState.timeLeft else 25 * 60,
                originalTimeSeconds = if (pomodoroState.isRunning) pomodoroState.originalTime else 25 * 60,
                statusLabel = statusLabel,
                ringColor = ringColor
            )

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

            PomodoroControls(
                isRunning = pomodoroState.isRunning,
                isPaused = pomodoroState.isPaused,
                isAlarmActive = pomodoroState.isAlarmActive,
                onStart = {
                    sendServiceAction("START") {
                        putExtra("workDuration", 25 * 60)
                        putExtra("shortBreakDuration", 5 * 60)
                        putExtra("longBreakDuration", 15 * 60)
                        selectedCourse?.let {
                            putExtra("courseId", it.id)
                            it.subjectId?.let { sId -> putExtra("subjectId", sId) }
                        }
                    }
                },
                onPauseResume = { sendServiceAction("PAUSE_RESUME") },
                onSkip = { sendServiceAction("SKIP") },
                onStop = { sendServiceAction("STOP") },
                onStopAlarm = { sendServiceAction("STOP_ALARM") }
            )
        }
    }
}
