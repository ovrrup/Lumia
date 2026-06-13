package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.*
import android.widget.RemoteViews

enum class PomodoroMode { WORK, SHORT_BREAK, LONG_BREAK }

class PomodoroService : Service() {

    companion object {
        var isServiceRunning = false
        var currentStateStr = ""
    }

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private var timeLeft = 0
    private var isWork = true
    private var originalTime = 0
    private var isPaused = false
    
    // Period tracking
    private var sessionsCompleted = 0 // 0 to periodSessions
    private var currentMode = PomodoroMode.WORK
    
    // Configurations
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var assignmentId: Int? = null
    private var taskId: Int? = null
    
    private var workDuration = 25 * 60
    private var shortBreakDuration = 5 * 60
    private var longBreakDuration = 15 * 60
    private var periodSessions = 4
    private var maxPeriods = -1
    private var periodsCompleted = 0

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        job?.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        isServiceRunning = true

        if (action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (action == "PAUSE_RESUME") {
            isPaused = !isPaused
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(2002, buildNotification(timeLeft))
            sendTick()
            return START_NOT_STICKY
        }
        
        if (action == "SKIP") {
            job?.cancel()
            finishSession(skipped = true)
            return START_NOT_STICKY
        }

        if (action == "START" || action == "RESET") {
            workDuration = intent?.getIntExtra("workDuration", 25 * 60) ?: (25 * 60)
            shortBreakDuration = intent?.getIntExtra("shortBreakDuration", 5 * 60) ?: (5 * 60)
            longBreakDuration = intent?.getIntExtra("longBreakDuration", 15 * 60) ?: (15 * 60)
            periodSessions = intent?.getIntExtra("periodSessions", 4) ?: 4
            maxPeriods = intent?.getIntExtra("maxPeriods", -1) ?: -1
            
            subjectId = if (intent?.hasExtra("subjectId") == true) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null
            courseId = if (intent?.hasExtra("courseId") == true) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
            assignmentId = if (intent?.hasExtra("assignmentId") == true) intent.getIntExtra("assignmentId", -1).takeIf { it != -1 } else null
            taskId = if (intent?.hasExtra("taskId") == true) intent.getIntExtra("taskId", -1).takeIf { it != -1 } else null
            
            sessionsCompleted = 0
            periodsCompleted = 0
            currentMode = PomodoroMode.WORK
            startCurrentMode()
            startForegroundService()
        }

        return START_NOT_STICKY
    }
    
    private fun startCurrentMode() {
        isWork = currentMode == PomodoroMode.WORK
        originalTime = when (currentMode) {
            PomodoroMode.WORK -> workDuration
            PomodoroMode.SHORT_BREAK -> shortBreakDuration
            PomodoroMode.LONG_BREAK -> longBreakDuration
        }
        timeLeft = originalTime
        isPaused = false
        currentStateStr = currentMode.name
        startTimer()
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("pomodoro_service", "Pomodoro Foreground", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Ongoing Pomodoro Timer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = buildNotification(timeLeft)
        startForeground(2002, notification)
    }

    private fun buildNotification(time: Int): android.app.Notification {
        val minutes = time / 60
        val seconds = time % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        val title = when (currentMode) {
            PomodoroMode.WORK -> "Focusing (Session ${sessionsCompleted + 1}/$periodSessions)"
            PomodoroMode.SHORT_BREAK -> "Short Rest"
            PomodoroMode.LONG_BREAK -> "Long Rest (Period Complete!)"
        }

        val stopIntent = Intent(this, PomodoroService::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, PomodoroService::class.java).apply { action = "PAUSE_RESUME" }
        val pausePending = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val skipIntent = Intent(this, PomodoroService::class.java).apply { action = "SKIP" }
        val skipPending = PendingIntent.getService(this, 2, skipIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK 
        }
        val mainPending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // Progress bar in notification
        val progressMax = originalTime
        val progressNow = originalTime - time

        return NotificationCompat.Builder(this, "pomodoro_service")
            .setSmallIcon(R.drawable.ic_notification_scholar)
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr" + if (isPaused) " (PAUSED)" else "")
            .setProgress(progressMax, progressNow, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mainPending)
            .addAction(if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause, if (isPaused) "Resume" else "Pause", pausePending)
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit", stopPending)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun sendTick() {
        val broadcastIntent = Intent("PomodoroTick").apply { setPackage(packageName) }
        broadcastIntent.putExtra("timeLeft", timeLeft)
        broadcastIntent.putExtra("originalTime", originalTime)
        broadcastIntent.putExtra("mode", currentMode.name)
        broadcastIntent.putExtra("isPaused", isPaused)
        broadcastIntent.putExtra("sessionsCompleted", sessionsCompleted)
        sendBroadcast(broadcastIntent)
    }

    private fun startTimer() {
        job?.cancel()
        job = scope.launch {
            while (timeLeft > 0) {
                if (!isPaused) {
                    delay(1000)
                    timeLeft--
                    
                    if (timeLeft % 5 == 0 || timeLeft < 10) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(2002, buildNotification(timeLeft))
                    }
                    sendTick()
                } else {
                    delay(100) // Sleep minimally when paused but loop
                }
            }
            
            finishSession(skipped = false)
        }
    }
    
    private fun finishSession(skipped: Boolean) {
        // Send finished log broadcast if work
        if (currentMode == PomodoroMode.WORK && !skipped) {
            val finishedIntent = Intent("PomodoroLogSession").apply { setPackage(packageName) }
            finishedIntent.putExtra("isWork", true)
            finishedIntent.putExtra("originalTime", originalTime)
            subjectId?.let { finishedIntent.putExtra("subjectId", it) }
            courseId?.let { finishedIntent.putExtra("courseId", it) }
            assignmentId?.let { finishedIntent.putExtra("assignmentId", it) }
            taskId?.let { finishedIntent.putExtra("taskId", it) }
            sendBroadcast(finishedIntent)
        }

        // Advance Period Logic
        if (currentMode == PomodoroMode.WORK) {
            sessionsCompleted++
            if (sessionsCompleted >= periodSessions) {
                currentMode = PomodoroMode.LONG_BREAK
                sessionsCompleted = 0
            } else {
                currentMode = PomodoroMode.SHORT_BREAK
            }
        } else if (currentMode == PomodoroMode.LONG_BREAK) {
            periodsCompleted++
            if (maxPeriods > 0 && periodsCompleted >= maxPeriods) {
                stopSelf()
                return
            }
            currentMode = PomodoroMode.WORK
        } else {
            // Short break -> Work
            currentMode = PomodoroMode.WORK
        }
        
        startCurrentMode()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
