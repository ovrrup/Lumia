package ovrrup.lumia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ovrrup.lumia.MainActivity
import ovrrup.lumia.R
import kotlinx.coroutines.*
import android.widget.RemoteViews

enum class PomodoroMode { WORK, SHORT_BREAK, LONG_BREAK }

data class PomodoroState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val timeLeft: Int = 25 * 60,
    val originalTime: Int = 25 * 60,
    val modeString: String = "WORK",
    val sessionsCompleted: Int = 0,
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val taskId: Int? = null
)

class PomodoroActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val serviceIntent = Intent(context, PomodoroService::class.java).apply { 
            this.action = action 
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class PomodoroService : Service() {

    companion object {
        private val _state = kotlinx.coroutines.flow.MutableStateFlow(PomodoroState())
        val state: kotlinx.coroutines.flow.StateFlow<PomodoroState> = _state

        var isServiceRunning: Boolean
            get() = _state.value.isRunning
            set(value) {
                _state.value = _state.value.copy(isRunning = value)
            }

        var currentStateStr: String
            get() = _state.value.modeString
            set(value) {
                _state.value = _state.value.copy(modeString = value)
            }

        var timeLeft: Int
            get() = _state.value.timeLeft
            set(value) {
                _state.value = _state.value.copy(timeLeft = value)
            }

        var originalTime: Int
            get() = _state.value.originalTime
            set(value) {
                _state.value = _state.value.copy(originalTime = value)
            }

        var isPaused: Boolean
            get() = _state.value.isPaused
            set(value) {
                _state.value = _state.value.copy(isPaused = value)
            }

        var sessionsCompleted: Int
            get() = _state.value.sessionsCompleted
            set(value) {
                _state.value = _state.value.copy(sessionsCompleted = value)
            }

        var subjectId: Int?
            get() = _state.value.subjectId
            set(value) {
                _state.value = _state.value.copy(subjectId = value)
            }

        var courseId: Int?
            get() = _state.value.courseId
            set(value) {
                _state.value = _state.value.copy(courseId = value)
            }

        var assignmentId: Int?
            get() = _state.value.assignmentId
            set(value) {
                _state.value = _state.value.copy(assignmentId = value)
            }

        var taskId: Int?
            get() = _state.value.taskId
            set(value) {
                _state.value = _state.value.copy(taskId = value)
            }

        fun updateState(block: (PomodoroState) -> PomodoroState) {
            _state.value = block(_state.value)
        }
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

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Lumia::PomodoroWakeLock").apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(35 * 60 * 1000L) // Safe maximum time limit representing any standard focus interval
            }
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Error during WakeLock acquire", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Error during WakeLock release", e)
        }
    }

    private fun sendSessionCompleteAlert(finishedMode: PomodoroMode) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel("pomodoro_alerts", "Pomodoro Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts for completed Pomodoro sessions"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }

        val title = if (finishedMode == PomodoroMode.WORK) {
            "Focus Interval Done!"
        } else {
            "Rest Finished!"
        }
        val text = if (finishedMode == PomodoroMode.WORK) {
            "Amazing effort! Take a quick break to refresh."
        } else {
            "Rest interval is over. Ready to dive back in?"
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPending = PendingIntent.getActivity(this, 10, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alertNotification = NotificationCompat.Builder(this, "pomodoro_alerts")
            .setSmallIcon(ovrrup.lumia.util.NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ovrrup.lumia.util.NotificationHelper.getColor(this))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(mainPending)
            .build()
        
        notificationManager.notify(2003, alertNotification)

        // Trigger physical haptic alarm sequence
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 350, 150, 350), -1)
            }
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Vibration failed", e)
        }

        // Play loud custom ringtone or notification beep
        try {
            val notificationSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = android.media.RingtoneManager.getRingtone(applicationContext, notificationSoundUri)
            ringtone.play()
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Sound playback failed", e)
        }
    }

    private fun syncToState() {
        updateState {
            it.copy(
                isRunning = isServiceRunning,
                isPaused = isPaused,
                timeLeft = timeLeft,
                originalTime = originalTime,
                modeString = currentMode.name,
                sessionsCompleted = sessionsCompleted,
                subjectId = subjectId,
                courseId = courseId,
                assignmentId = assignmentId,
                taskId = taskId
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        job?.cancel()
        releaseWakeLock()
        syncToState()
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
            syncToState()
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

        syncToState()
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
        syncToState()
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

        val stopIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "PAUSE_RESUME" }
        val pausePending = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val skipIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "SKIP" }
        val skipPending = PendingIntent.getBroadcast(this, 2, skipIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val mainIntent = Intent(this, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK 
        }
        val mainPending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        
        // Progress bar in notification
        val progressMax = originalTime
        val progressNow = originalTime - time
        
        val notificationColor = when (currentMode) {
            PomodoroMode.WORK -> 0xFF4285F4.toInt()
            PomodoroMode.SHORT_BREAK -> 0xFF3DDC84.toInt()
            PomodoroMode.LONG_BREAK -> 0xFFFABB05.toInt()
        }

        val builder = NotificationCompat.Builder(this, "pomodoro_service")
            .setSmallIcon(ovrrup.lumia.util.NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr" + if (isPaused) " (PAUSED)" else "")
            .setProgress(progressMax, progressNow, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainPending)
            .setOngoing(true)
            .setColor(ovrrup.lumia.util.NotificationHelper.getColor(this))
            .setUsesChronometer(!isPaused)
            .setWhen(System.currentTimeMillis() + time * 1000L)
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setChronometerCountDown(true)
        }
            
        return builder.addAction(if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause, if (isPaused) "Resume" else "Pause", pausePending)
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit", stopPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
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
            syncToState()
            var lastTickTime = System.currentTimeMillis()
            acquireWakeLock()
            while (timeLeft > 0) {
                if (!isPaused) {
                    delay(1000)
                    if (!isPaused) {
                        val currentTime = System.currentTimeMillis()
                        val elapsed = ((currentTime - lastTickTime) / 1000).toInt()
                        if (elapsed > 0) {
                            timeLeft = maxOf(0, timeLeft - elapsed)
                            lastTickTime = currentTime
                        }
                        
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(2002, buildNotification(timeLeft))
                        sendTick()
                        syncToState()
                    }
                } else {
                    delay(200)
                    lastTickTime = System.currentTimeMillis()
                }
            }
            releaseWakeLock()
            finishSession(skipped = false)
        }
    }
    
    private fun finishSession(skipped: Boolean) {
        val finishedMode = currentMode
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
            
            val prefs = getSharedPreferences("lumia_prefs", Context.MODE_PRIVATE)
            val autoLog = prefs.getBoolean("system_pomodoro_auto_log", true)
            if (autoLog) {
                scope.launch {
                    try {
                        val db = ovrrup.lumia.data.AppDatabase.getDatabase(applicationContext)
                        db.scholarDao().insertPomodoroSession(
                            ovrrup.lumia.model.PomodoroSession(
                                dateMillis = System.currentTimeMillis(),
                                durationMinutes = originalTime / 60,
                                subjectId = subjectId,
                                courseId = courseId,
                                assignmentId = assignmentId,
                                taskId = taskId
                            )
                        )
                        db.scholarDao().insertActionLog(
                            ovrrup.lumia.model.ActionLog(actionText = "Completed Pomodoro Session (${originalTime / 60} min)")
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("PomodoroService", "Failed to auto-log pomodoro session", e)
                    }
                }
            }
        }

        if (!skipped) {
            sendSessionCompleteAlert(finishedMode)
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
