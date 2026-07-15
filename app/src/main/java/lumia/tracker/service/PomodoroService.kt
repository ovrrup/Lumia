package lumia.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import lumia.tracker.MainActivity
import lumia.tracker.R
import kotlinx.coroutines.*
import android.widget.RemoteViews
import lumia.tracker.util.ScholarPomodoroWidgetProvider

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
    val taskId: Int? = null,
    val topicId: Int? = null,
    val isAlarmActive: Boolean = false,
    val endedModeStr: String = ""
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

        var topicId: Int?
            get() = _state.value.topicId
            set(value) {
                _state.value = _state.value.copy(topicId = value)
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
    
    private var isAlarmActive = false
    private var endedModeStr = ""
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var hasSavedCurrentSession = false
    
    // Period tracking
    private var sessionsCompleted = 0 // 0 to periodSessions
    private var currentMode = PomodoroMode.WORK
    
    // Configurations
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var assignmentId: Int? = null
    private var taskId: Int? = null
    private var topicId: Int? = null
    
    private var workDuration = 25 * 60
    private var shortBreakDuration = 5 * 60
    private var longBreakDuration = 15 * 60
    private var periodSessions = 4
    private var maxPeriods = -1
    private var periodsCompleted = 0

    private fun playAlarmSound(isWorkEnd: Boolean) {
        stopAlarmSound()
        try {
            val soundUri = if (isWorkEnd) {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
            } else {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            }
            
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioStreamType(if (isWorkEnd) android.media.AudioManager.STREAM_ALARM else android.media.AudioManager.STREAM_NOTIFICATION)
                isLooping = isWorkEnd
                prepare()
                start()
            }
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Error playing alarm sound", e)
            try {
                val toneG = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                toneG.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Error stopping alarm sound", e)
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
                taskId = taskId,
                topicId = topicId,
                isAlarmActive = isAlarmActive,
                endedModeStr = endedModeStr
            )
        }
        updatePomodoroWidget()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveElapsedWorkSessionIfNeeded()
        isServiceRunning = false
        stopAlarmSound()
        job?.cancel()
        syncToState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        isServiceRunning = true

        if (action == "STOP") {
            val alreadySaved = intent?.getBooleanExtra("alreadySaved", false) ?: false
            if (alreadySaved) {
                hasSavedCurrentSession = true
            }
            saveElapsedWorkSessionIfNeeded()
            stopAlarmSound()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == "STOP_ALARM") {
            stopAlarmSound()
            isAlarmActive = false
            endedModeStr = ""
            syncToState()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(2002, buildNotification(timeLeft))
            return START_NOT_STICKY
        }
        
        if (action == "PAUSE_RESUME") {
            isPaused = !isPaused
            if (!isPaused && (job == null || job?.isActive != true)) {
                startTimer()
            } else {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2002, buildNotification(timeLeft))
                sendTick()
                syncToState()
            }
            return START_NOT_STICKY
        }
        
        if (action == "SKIP") {
            saveElapsedWorkSessionIfNeeded()
            stopAlarmSound()
            isAlarmActive = false
            job?.cancel()
            finishSession(skipped = true)
            return START_NOT_STICKY
        }
        
        if (action == "UPDATE_CONTEXT") {
            if (intent != null) {
                if (intent.hasExtra("subjectId")) {
                    subjectId = intent.getIntExtra("subjectId", -1).takeIf { it != -1 }
                }
                if (intent.hasExtra("courseId")) {
                    courseId = intent.getIntExtra("courseId", -1).takeIf { it != -1 }
                }
                if (intent.hasExtra("assignmentId")) {
                    assignmentId = intent.getIntExtra("assignmentId", -1).takeIf { it != -1 }
                }
                if (intent.hasExtra("taskId")) {
                    taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
                }
                if (intent.hasExtra("topicId")) {
                    topicId = intent.getIntExtra("topicId", -1).takeIf { it != -1 }
                }
                syncToState()
            }
            return START_NOT_STICKY
        }

        if (action == "START" || action == "RESET") {
            stopAlarmSound()
            isAlarmActive = false
            workDuration = intent?.getIntExtra("workDuration", 25 * 60) ?: (25 * 60)
            shortBreakDuration = intent?.getIntExtra("shortBreakDuration", 5 * 60) ?: (5 * 60)
            longBreakDuration = intent?.getIntExtra("longBreakDuration", 15 * 60) ?: (15 * 60)
            periodSessions = intent?.getIntExtra("periodSessions", 4) ?: 4
            maxPeriods = intent?.getIntExtra("maxPeriods", -1) ?: -1
            
            subjectId = if (intent?.hasExtra("subjectId") == true) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null
            courseId = if (intent?.hasExtra("courseId") == true) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
            assignmentId = if (intent?.hasExtra("assignmentId") == true) intent.getIntExtra("assignmentId", -1).takeIf { it != -1 } else null
            taskId = if (intent?.hasExtra("taskId") == true) intent.getIntExtra("taskId", -1).takeIf { it != -1 } else null
            topicId = if (intent?.hasExtra("topicId") == true) intent.getIntExtra("topicId", -1).takeIf { it != -1 } else null
            
            sessionsCompleted = 0
            periodsCompleted = 0
            currentMode = PomodoroMode.WORK
            startCurrentMode()
            startForegroundService()
        }

        syncToState()
        return START_NOT_STICKY
    }
    
    private fun startCurrentMode(startPaused: Boolean = false) {
        isWork = currentMode == PomodoroMode.WORK
        originalTime = when (currentMode) {
            PomodoroMode.WORK -> workDuration
            PomodoroMode.SHORT_BREAK -> shortBreakDuration
            PomodoroMode.LONG_BREAK -> longBreakDuration
        }
        timeLeft = originalTime
        isPaused = startPaused
        currentStateStr = currentMode.name
        hasSavedCurrentSession = false
        syncToState()
        if (!startPaused) {
            startTimer()
        } else {
            job?.cancel()
            job = null
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(2002, buildNotification(timeLeft))
        }
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

        val mainIntent = Intent(this, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP 
            putExtra("OPEN_POMODORO", true)
        }
        val mainPending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        if (isAlarmActive) {
            val stopAlarmIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "STOP_ALARM" }
            val stopAlarmPending = PendingIntent.getBroadcast(this, 3, stopAlarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            
            return NotificationCompat.Builder(this, "pomodoro_service")
                .setSmallIcon(lumia.tracker.util.NotificationHelper.getSmallIcon())
                .setContentTitle(if (currentMode == PomodoroMode.WORK) "Rest Break Finished!" else "Focus Session Finished!")
                .setContentText("Alarm active! Tap to stop sound.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(mainPending)
                .setOngoing(true)
                .setColor(lumia.tracker.util.NotificationHelper.getColor(this))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", stopAlarmPending)
                .build()
        }

        val stopIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "PAUSE_RESUME" }
        val pausePending = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val skipIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "SKIP" }
        val skipPending = PendingIntent.getBroadcast(this, 2, skipIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Progress bar in notification
        val progressMax = originalTime
        val progressNow = originalTime - time
        
        val builder = NotificationCompat.Builder(this, "pomodoro_service")
            .setSmallIcon(lumia.tracker.util.NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr" + if (isPaused) " (PAUSED)" else "")
            .setProgress(progressMax, progressNow, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainPending)
            .setOngoing(true)
            .setColor(lumia.tracker.util.NotificationHelper.getColor(this))
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
        updatePomodoroWidget()
    }

    private fun startTimer() {
        job?.cancel()
        job = scope.launch {
            syncToState()
            while (timeLeft > 0) {
                if (!isPaused) {
                    delay(1000)
                    timeLeft--
                    
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(2002, buildNotification(timeLeft))
                    sendTick()
                    syncToState()
                } else {
                    delay(100)
                }
            }
            
            finishSession(skipped = false)
        }
    }
    
    private fun saveElapsedWorkSessionIfNeeded() {
        if (currentMode != PomodoroMode.WORK || hasSavedCurrentSession) return
        val elapsedSeconds = originalTime - timeLeft
        if (elapsedSeconds >= 60) {
            val mins = Math.max(1, elapsedSeconds / 60)
            hasSavedCurrentSession = true
            scope.launch(Dispatchers.IO) {
                logAndAwardSession(durationMinutes = mins, isFullCompletion = false, isWorkSession = (currentMode == PomodoroMode.WORK))
            }
        }
    }

    private suspend fun logAndAwardSession(durationMinutes: Int, isFullCompletion: Boolean, isWorkSession: Boolean) {
        if (!isWorkSession) return
        try {
            // Check preference "system_pomodoro_auto_log" in profile prefs
            val profMgr = lumia.tracker.data.ProfileManager(applicationContext)
            val autoLogPrefs = profMgr.getProfilePrefs()
            val isAutoLogEnabled = autoLogPrefs.getBoolean("system_pomodoro_auto_log", true)
            if (!isAutoLogEnabled && isFullCompletion) {
                android.util.Log.d("PomodoroService", "Auto-logging disabled by user prefix settings.")
                return
            }

            // 1. Save session in local database & SQLite action log
            val db = lumia.tracker.data.AppDatabase.getDatabase(applicationContext)
            db.scholarDao().insertPomodoroSession(
                lumia.tracker.model.PomodoroSession(
                    dateMillis = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    subjectId = subjectId,
                    courseId = courseId,
                    assignmentId = assignmentId,
                    taskId = taskId,
                    topicId = topicId
                )
            )
            val actionLabel = if (isFullCompletion) "Completed" else "Focused partially on"
            db.scholarDao().insertActionLog(
                lumia.tracker.model.ActionLog(actionText = "$actionLabel Pomodoro Session ($durationMinutes min)")
            )
            android.util.Log.d("PomodoroService", "SAVED AUTOMATIC FOCUS SESSION TO DB: $durationMinutes mins")

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mainIntent = Intent(applicationContext, MainActivity::class.java).apply { 
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP 
                putExtra("OPEN_POMODORO", true)
            }
            val mainPending = PendingIntent.getActivity(applicationContext, 101, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val completionNotification = NotificationCompat.Builder(applicationContext, "pomodoro_service")
                .setSmallIcon(lumia.tracker.util.NotificationHelper.getSmallIcon())
                .setContentTitle(if (isFullCompletion) "Focus Completed!" else "Focus Saved!")
                .setContentText("Locked in $durationMinutes min study.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(mainPending)
                .setColor(lumia.tracker.util.NotificationHelper.getColor(applicationContext))
                .build()
            notificationManager.notify(2003, completionNotification)
            
        } catch (e: Exception) {
            android.util.Log.e("PomodoroService", "Failed to log and award pomodoro session", e)
        }
    }

    private fun finishSession(skipped: Boolean) {
        val completedMode = currentMode
        endedModeStr = if (!skipped) completedMode.name else ""

        if (!skipped) {
            isAlarmActive = true
            playAlarmSound(isWorkEnd = (completedMode == PomodoroMode.WORK))
        }

        // Send finished log broadcast if work
        if (completedMode == PomodoroMode.WORK && !skipped && !hasSavedCurrentSession) {
            hasSavedCurrentSession = true
            val finishedIntent = Intent("PomodoroLogSession").apply { setPackage(packageName) }
            finishedIntent.putExtra("isWork", true)
            finishedIntent.putExtra("originalTime", originalTime)
            subjectId?.let { finishedIntent.putExtra("subjectId", it) }
            courseId?.let { finishedIntent.putExtra("courseId", it) }
            assignmentId?.let { finishedIntent.putExtra("assignmentId", it) }
            taskId?.let { finishedIntent.putExtra("taskId", it) }
            topicId?.let { finishedIntent.putExtra("topicId", it) }
            sendBroadcast(finishedIntent)
            
            val mins = Math.max(1, originalTime / 60)
            scope.launch(Dispatchers.IO) {
                logAndAwardSession(durationMinutes = mins, isFullCompletion = true, isWorkSession = (completedMode == PomodoroMode.WORK))
            }
        }

        // Advance Period Logic
        if (completedMode == PomodoroMode.WORK) {
            sessionsCompleted++
            if (sessionsCompleted >= periodSessions) {
                currentMode = PomodoroMode.LONG_BREAK
                sessionsCompleted = 0
            } else {
                currentMode = PomodoroMode.SHORT_BREAK
            }
        } else if (completedMode == PomodoroMode.LONG_BREAK) {
            periodsCompleted++
            if (maxPeriods > 0 && periodsCompleted >= maxPeriods) {
                stopSelf()
                return
            }
            currentMode = PomodoroMode.WORK
        } else {
            currentMode = PomodoroMode.WORK
        }
        
        startCurrentMode(startPaused = !skipped)
    }

    private fun updatePomodoroWidget() {
        try {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(applicationContext)
            val componentName = android.content.ComponentName(applicationContext, lumia.tracker.util.ScholarPomodoroWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(applicationContext, lumia.tracker.util.ScholarPomodoroWidgetProvider::class.java).apply {
                    action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
