package ovrrup.lumia.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.model.Course
import ovrrup.lumia.model.Subject
import ovrrup.lumia.service.PomodoroService
import ovrrup.lumia.service.PomodoroState
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    val context = LocalContext.current
    val serviceState by PomodoroService.state.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var focusedWorkDurationMins by remember { mutableStateOf(25) }

    val formattedTime = remember(serviceState.timeLeft) {
        val mins = serviceState.timeLeft / 60
        val secs = serviceState.timeLeft % 60
        String.format("%02d:%02d", mins, secs)
    }

    val progressFraction = remember(serviceState.timeLeft, serviceState.originalTime) {
        if (serviceState.originalTime > 0) {
            1f - (serviceState.timeLeft.toFloat() / serviceState.originalTime.toFloat())
        } else {
            0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "pomodoro_progress"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Study Focus Room", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Focus Circle
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 20.dp.toPx()
                        
                        // Track ring
                        drawArc(
                            color = outlineColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        // Filled indicator ring
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (serviceState.isRunning) serviceState.modeString else "READY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Completed: ${serviceState.sessionsCompleted} intervals",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!serviceState.isRunning) {
                // Choose Course & Study Duration before starting
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Configure Focus Session",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            
                            // Course Selector
                            Text("Linked Course (Optional)", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            if (courses.isEmpty()) {
                                Text(
                                    "No courses enrolled yet. You can still focus offline.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    courses.take(4).forEach { course ->
                                        val selected = selectedCourseId == course.id
                                        FilterChip(
                                            selected = selected,
                                            onClick = { selectedCourseId = if (selected) null else course.id },
                                            label = { Text(course.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            
                            // Timer Duration Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Interval Length", style = MaterialTheme.typography.titleSmall)
                                Text("$focusedWorkDurationMins mins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = focusedWorkDurationMins.toFloat(),
                                onValueChange = { focusedWorkDurationMins = it.toInt() },
                                valueRange = 5f..60f,
                                steps = 11
                            )
                        }
                    }
                }
            }

            item {
                // Focus Controls Row
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!serviceState.isRunning) {
                            // Start Button
                            BouncyButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    val startIntent = Intent(context, PomodoroService::class.java).apply {
                                        action = "START"
                                        putExtra("workDuration", focusedWorkDurationMins * 60)
                                        selectedCourseId?.let { putExtra("courseId", it) }
                                    }
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(startIntent)
                                    } else {
                                        context.startService(startIntent)
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Start Focus")
                                Spacer(Modifier.width(6.dp))
                                Text("Start Study Session")
                            }
                        } else {
                            // Pause / Resume
                            BouncyButton(
                                onClick = {
                                    val pauseIntent = Intent(context, PomodoroService::class.java).apply {
                                        action = "PAUSE_RESUME"
                                    }
                                    context.startService(pauseIntent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (serviceState.isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(
                                    if (serviceState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = "Pause/Resume"
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (serviceState.isPaused) "Resume" else "Pause")
                            }

                            // Skip Button
                            BouncyIconButton(
                                onClick = {
                                    val skipIntent = Intent(context, PomodoroService::class.java).apply {
                                        action = "SKIP"
                                    }
                                    context.startService(skipIntent)
                                }
                            ) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Session", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Stop / Reset Button
                            BouncyIconButton(
                                onClick = {
                                    val stopIntent = Intent(context, PomodoroService::class.java).apply {
                                        action = "STOP"
                                    }
                                    context.startService(stopIntent)
                                }
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
