package com.example.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(navController: NavController) {
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(PomodoroMode.WORK) }
    val context = LocalContext.current
    
    val totalTime = when (mode) {
        PomodoroMode.WORK -> 25 * 60
        PomodoroMode.SHORT_BREAK -> 5 * 60
        PomodoroMode.LONG_BREAK -> 15 * 60
    }

    LaunchedEffect(isRunning, mode) {
        while (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        if (isRunning && timeLeft == 0) {
            isRunning = false
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("pomodoro_channel", "Pomodoro Timer", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications for when your pomodoro timer is finished"
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 500, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val title = when (mode) {
                PomodoroMode.WORK -> "Work Session Complete!"
                PomodoroMode.SHORT_BREAK -> "Short Break Over"
                PomodoroMode.LONG_BREAK -> "Long Break Over"
            }
            val text = when (mode) {
                PomodoroMode.WORK -> "Great job! Time to take a break."
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> "Break is over! Ready to get back to work?"
            }
            
            val notification = NotificationCompat.Builder(context, "pomodoro_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(android.graphics.Color.parseColor("#E91E63"))
                .setAutoCancel(true)
                .build()
                
            notificationManager.notify(2001, notification)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            ) {
                ModeButton(
                    text = "Work",
                    selected = mode == PomodoroMode.WORK,
                    onClick = { mode = PomodoroMode.WORK; timeLeft = 25 * 60; isRunning = false },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    text = "Short Break",
                    selected = mode == PomodoroMode.SHORT_BREAK,
                    onClick = { mode = PomodoroMode.SHORT_BREAK; timeLeft = 5 * 60; isRunning = false },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    text = "Long Break",
                    selected = mode == PomodoroMode.LONG_BREAK,
                    onClick = { mode = PomodoroMode.LONG_BREAK; timeLeft = 15 * 60; isRunning = false },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
                val circleColor = MaterialTheme.colorScheme.primary
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = circleColor.copy(alpha = 0.2f),
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
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop",
                        modifier = Modifier.size(40.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { 
                        isRunning = false
                        timeLeft = totalTime 
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(32.dp)
                    )
                }
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
