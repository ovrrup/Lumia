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

class PomodoroService : Service() {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var timeLeft = 0
    private var isWork = true
    private var originalTime = 0
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var assignmentId: Int? = null
    private var taskId: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == "START") {
            timeLeft = intent.getIntExtra("time", 25 * 60)
            originalTime = timeLeft
            isWork = intent.getBooleanExtra("isWork", true)
            subjectId = if (intent.hasExtra("subjectId")) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null
            courseId = if (intent.hasExtra("courseId")) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
            assignmentId = if (intent.hasExtra("assignmentId")) intent.getIntExtra("assignmentId", -1).takeIf { it != -1 } else null
            taskId = if (intent.hasExtra("taskId")) intent.getIntExtra("taskId", -1).takeIf { it != -1 } else null
            
            startForegroundService()
            startTimer()
        }

        return START_NOT_STICKY
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
        val title = if (isWork) "Focusing" else "Taking a break"

        val stopIntent = Intent(this, PomodoroService::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK 
        }
        val mainPending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "pomodoro_service")
            .setSmallIcon(R.drawable.ic_notification_scholar)
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mainPending)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun startTimer() {
        job?.cancel()
        job = scope.launch {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2002, buildNotification(timeLeft))
                
                val broadcastIntent = Intent("PomodoroTick").apply { setPackage(packageName) }
                broadcastIntent.putExtra("timeLeft", timeLeft)
                sendBroadcast(broadcastIntent)
            }
            
            // Finished
            val finishedIntent = Intent("PomodoroFinished").apply { setPackage(packageName) }
            finishedIntent.putExtra("isWork", isWork)
            finishedIntent.putExtra("originalTime", originalTime)
            subjectId?.let { finishedIntent.putExtra("subjectId", it) }
            courseId?.let { finishedIntent.putExtra("courseId", it) }
            assignmentId?.let { finishedIntent.putExtra("assignmentId", it) }
            taskId?.let { finishedIntent.putExtra("taskId", it) }
            sendBroadcast(finishedIntent)
            
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
