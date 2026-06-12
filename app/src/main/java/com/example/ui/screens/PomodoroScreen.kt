package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.ArrowDropDown
import com.example.model.PracticeAssignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(navController: NavController, viewModel: com.example.viewmodel.ScholarViewModel) {
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
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

    val systemPomodoroAutoLog by viewModel.systemPomodoroAutoLog.collectAsStateWithLifecycle()
    val workDuration by viewModel.pomodoroWorkDuration.collectAsStateWithLifecycle()
    val shortBreakDuration by viewModel.pomodoroShortBreakDuration.collectAsStateWithLifecycle()
    val longBreakDuration by viewModel.pomodoroLongBreakDuration.collectAsStateWithLifecycle()
    
    val totalTime = when (mode) {
        PomodoroMode.WORK -> workDuration * 60
        PomodoroMode.SHORT_BREAK -> shortBreakDuration * 60
        PomodoroMode.LONG_BREAK -> longBreakDuration * 60
    }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: android.content.Intent?) {
                when (intent?.action) {
                    "PomodoroTick" -> {
                        timeLeft = intent.getIntExtra("timeLeft", 0)
                        isRunning = true // in case app was reopened
                    }
                    "PomodoroFinished" -> {
                        isRunning = false
                        timeLeft = 0
                        val wasWork = intent.getBooleanExtra("isWork", true)
                        val originalTime = intent.getIntExtra("originalTime", 25 * 60)
                        
                        val sId = if (intent.hasExtra("subjectId")) intent.getIntExtra("subjectId", -1) else null
                        val cId = if (intent.hasExtra("courseId")) intent.getIntExtra("courseId", -1) else null
                        val aId = if (intent.hasExtra("assignmentId")) intent.getIntExtra("assignmentId", -1) else null
                        val tId = if (intent.hasExtra("taskId")) intent.getIntExtra("taskId", -1) else null

                        if (wasWork && systemPomodoroAutoLog) {
                            viewModel.addPomodoroSession(originalTime / 60, sId, cId, aId, tId)
                        }
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction("PomodoroTick")
            addAction("PomodoroFinished")
        }
        androidx.core.content.ContextCompat.registerReceiver(context, receiver, filter, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                com.example.ui.components.NotificationPermissionPanel()
                com.example.ui.components.ExactAlarmPermissionPanel()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ModeButton(
                        text = "Work",
                        selected = mode == PomodoroMode.WORK,
                        onClick = { 
                            mode = PomodoroMode.WORK; timeLeft = 25 * 60; isRunning = false 
                            context.startService(android.content.Intent(context, com.example.service.PomodoroService::class.java).apply { action = "STOP" })
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        text = "Short Break",
                        selected = mode == PomodoroMode.SHORT_BREAK,
                        onClick = { 
                            mode = PomodoroMode.SHORT_BREAK; timeLeft = 5 * 60; isRunning = false 
                            context.startService(android.content.Intent(context, com.example.service.PomodoroService::class.java).apply { action = "STOP" })
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        text = "Long Break",
                        selected = mode == PomodoroMode.LONG_BREAK,
                        onClick = { 
                            mode = PomodoroMode.LONG_BREAK; timeLeft = 15 * 60; isRunning = false 
                            context.startService(android.content.Intent(context, com.example.service.PomodoroService::class.java).apply { action = "STOP" })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Beautiful pulsing scale animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRunning) 1.03f else 1f,
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
                    val circleColor = MaterialTheme.colorScheme.primary
                    
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
                        Text(
                            text = if (isRunning) "Focusing" else "Paused",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    FloatingActionButton(
                        onClick = { 
                            if (isRunning) {
                                isRunning = false
                                val intent = android.content.Intent(context, com.example.service.PomodoroService::class.java).apply { action = "STOP" }
                                context.startService(intent)
                            } else {
                                isRunning = true
                                val intent = android.content.Intent(context, com.example.service.PomodoroService::class.java).apply {
                                    action = "START"
                                    putExtra("time", timeLeft)
                                    putExtra("isWork", mode == PomodoroMode.WORK)
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
                        containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Stop",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { 
                            isRunning = false
                            timeLeft = totalTime 
                            val intent = android.content.Intent(context, com.example.service.PomodoroService::class.java).apply { action = "STOP" }
                            context.startService(intent)
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
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Link Session Block
                com.example.ui.components.GlassCard(
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
                    com.example.ui.components.GlassCard(
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
                    com.example.ui.components.GlassCard(
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

@Composable
fun ModeButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
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
