package lumia.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.ActionLog
import lumia.tracker.model.PomodoroSession
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.util.NotificationHelper
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

@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Production-grade Pomodoro foreground service with monotonic zero-drift ticker, context persistence, and robust auto-logging",
    category = "Service"
)
class PomodoroService : Service() {

    companion object {
        private const val TAG = "PomodoroService"
        private const val NOTIFICATION_ID = 2002
        private const val COMPLETION_NOTIFICATION_ID = 2003
        private const val CHANNEL_ID = "pomodoro_service"
        private const val ALARM_CHANNEL_ID = "pomodoro_alarm"

        private val _state = MutableStateFlow(PomodoroState())
        val state: StateFlow<PomodoroState> = _state

        @Volatile
        var instance: PomodoroService? = null
            private set

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

        /**
         * Directly dispatches intent actions to the active service instance without IPC latency.
         * Returns true if directly handled.
         */
        fun handleActionDirectly(context: Context, action: String, intent: Intent?): Boolean {
            val service = instance ?: return false
            Handler(Looper.getMainLooper()).post {
                try {
                    service.processIntentAction(action, intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Direct action handling failed for $action", e)
                }
            }
            return true
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null
    
    // Monotonic time tracking for zero-drift guarantee
    private var targetEndTimeElapsedRealtime: Long = 0L
    private var timeLeftSeconds: Int = 25 * 60
    private var isWork: Boolean = true
    private var originalDurationSeconds: Int = 25 * 60
    private var paused: Boolean = false
    
    private var isAlarmActive: Boolean = false
    private var endedModeStr: String = ""
    private var mediaPlayer: MediaPlayer? = null
    private var hasSavedCurrentSession: Boolean = false
    
    // Period & Cycle tracking
    private var sessionsCompletedCount: Int = 0
    private var currentMode: PomodoroMode = PomodoroMode.WORK
    private var periodsCompleted: Int = 0
    
    // Configured parameters
    private var workDuration: Int = 25 * 60
    private var shortBreakDuration: Int = 5 * 60
    private var longBreakDuration: Int = 15 * 60
    private var periodSessions: Int = 4
    private var maxPeriods: Int = -1
    
    // Academic Context
    private var activeSubjectId: Int? = null
    private var activeCourseId: Int? = null
    private var activeAssignmentId: Int? = null
    private var activeTaskId: Int? = null
    private var activeTopicId: Int? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        isServiceRunning = true
        val action = intent?.action

        processIntentAction(action, intent)
        return START_NOT_STICKY
    }

    /**
     * Centralized action processing engine, supporting both onStartCommand and direct dispatch.
     */
    fun processIntentAction(action: String?, intent: Intent?) {
        when (action) {
            "STOP" -> {
                saveElapsedWorkSessionIfNeeded()
                stopAlarmSound()
                isAlarmActive = false
                endedModeStr = ""
                isServiceRunning = false
                tickerJob?.cancel()
                tickerJob = null
                syncToState()
                stopForeground(true)
                stopSelf()
            }

            "STOP_ALARM" -> {
                stopAlarmSound()
                isAlarmActive = false
                endedModeStr = ""
                syncToState()
                updateForegroundNotification(timeLeftSeconds)
            }

            "PAUSE_RESUME" -> {
                togglePauseResume()
            }

            "SKIP" -> {
                saveElapsedWorkSessionIfNeeded()
                stopAlarmSound()
                isAlarmActive = false
                tickerJob?.cancel()
                tickerJob = null
                finishSession(skipped = true)
            }

            "UPDATE_CONTEXT" -> {
                if (intent != null) {
                    if (intent.hasExtra("subjectId")) {
                        activeSubjectId = intent.getIntExtra("subjectId", -1).takeIf { it != -1 }
                    }
                    if (intent.hasExtra("courseId")) {
                        activeCourseId = intent.getIntExtra("courseId", -1).takeIf { it != -1 }
                    }
                    if (intent.hasExtra("assignmentId")) {
                        activeAssignmentId = intent.getIntExtra("assignmentId", -1).takeIf { it != -1 }
                    }
                    if (intent.hasExtra("taskId")) {
                        activeTaskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
                    }
                    if (intent.hasExtra("topicId")) {
                        activeTopicId = intent.getIntExtra("topicId", -1).takeIf { it != -1 }
                    }
                    syncToState()
                }
            }

            "ADJUST_TIME" -> {
                val delta = intent?.getIntExtra("deltaSeconds", 0) ?: 0
                if (delta != 0) {
                    timeLeftSeconds = (timeLeftSeconds + delta).coerceAtLeast(10)
                    if (timeLeftSeconds > originalDurationSeconds) {
                        originalDurationSeconds = timeLeftSeconds
                    }
                    targetEndTimeElapsedRealtime = SystemClock.elapsedRealtime() + (timeLeftSeconds * 1000L)
                    syncToState()
                    updateForegroundNotification(timeLeftSeconds)
                    sendTickBroadcast()
                }
            }

            "SWITCH_MODE" -> {
                val targetModeStr = intent?.getStringExtra("targetMode") ?: "WORK"
                val targetMode = try { PomodoroMode.valueOf(targetModeStr) } catch (e: Exception) { PomodoroMode.WORK }
                currentMode = targetMode
                hasSavedCurrentSession = false
                startCurrentMode(startPaused = paused)
            }

            "START", "RESET" -> {
                stopAlarmSound()
                isAlarmActive = false
                endedModeStr = ""

                workDuration = intent?.getIntExtra("workDuration", 25 * 60) ?: (25 * 60)
                shortBreakDuration = intent?.getIntExtra("shortBreakDuration", 5 * 60) ?: (5 * 60)
                longBreakDuration = intent?.getIntExtra("longBreakDuration", 15 * 60) ?: (15 * 60)
                periodSessions = intent?.getIntExtra("periodSessions", 4) ?: 4
                maxPeriods = intent?.getIntExtra("maxPeriods", -1) ?: -1

                if (intent?.hasExtra("subjectId") == true) {
                    activeSubjectId = intent.getIntExtra("subjectId", -1).takeIf { it != -1 }
                }
                if (intent?.hasExtra("courseId") == true) {
                    activeCourseId = intent.getIntExtra("courseId", -1).takeIf { it != -1 }
                }
                if (intent?.hasExtra("assignmentId") == true) {
                    activeAssignmentId = intent.getIntExtra("assignmentId", -1).takeIf { it != -1 }
                }
                if (intent?.hasExtra("taskId") == true) {
                    activeTaskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
                }
                if (intent?.hasExtra("topicId") == true) {
                    activeTopicId = intent.getIntExtra("topicId", -1).takeIf { it != -1 }
                }

                sessionsCompletedCount = 0
                periodsCompleted = 0
                currentMode = PomodoroMode.WORK
                hasSavedCurrentSession = false
                startCurrentMode(startPaused = false)
                startAsForeground()
            }

            else -> {
                syncToState()
            }
        }
    }

    private fun togglePauseResume() {
        paused = !paused
        if (!paused) {
            targetEndTimeElapsedRealtime = SystemClock.elapsedRealtime() + (timeLeftSeconds * 1000L)
            startMonotonicTicker()
        } else {
            tickerJob?.cancel()
            tickerJob = null
        }
        syncToState()
        updateForegroundNotification(timeLeftSeconds)
        sendTickBroadcast()
    }

    private fun startCurrentMode(startPaused: Boolean = false) {
        isWork = currentMode == PomodoroMode.WORK
        originalDurationSeconds = when (currentMode) {
            PomodoroMode.WORK -> workDuration
            PomodoroMode.SHORT_BREAK -> shortBreakDuration
            PomodoroMode.LONG_BREAK -> longBreakDuration
        }
        timeLeftSeconds = originalDurationSeconds
        paused = startPaused
        hasSavedCurrentSession = false
        targetEndTimeElapsedRealtime = SystemClock.elapsedRealtime() + (timeLeftSeconds * 1000L)

        syncToState()
        updateForegroundNotification(timeLeftSeconds)
        sendTickBroadcast()

        if (!startPaused) {
            startMonotonicTicker()
        } else {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    /**
     * Monotonic high-precision zero-drift ticker.
     * Uses elapsedRealtime() to eliminate cumulative coroutine delay drift.
     */
    private fun startMonotonicTicker() {
        tickerJob?.cancel()
        targetEndTimeElapsedRealtime = SystemClock.elapsedRealtime() + (timeLeftSeconds * 1000L)

        tickerJob = serviceScope.launch {
            while (isActive && !paused && timeLeftSeconds > 0) {
                val now = SystemClock.elapsedRealtime()
                val remainingMillis = targetEndTimeElapsedRealtime - now

                if (remainingMillis <= 0L) {
                    timeLeftSeconds = 0
                    syncToState()
                    sendTickBroadcast()
                    updateForegroundNotification(0)
                    break
                }

                // Integer division with ceiling logic for crisp, exact 1-second ticks
                val computedSeconds = ((remainingMillis + 999L) / 1000L).toInt()
                if (computedSeconds != timeLeftSeconds) {
                    timeLeftSeconds = computedSeconds
                    syncToState()
                    sendTickBroadcast()
                    updateForegroundNotification(timeLeftSeconds)
                }

                // Re-align dynamically with next sub-second boundary to prevent any drift
                val delayToNextSecond = (remainingMillis % 1000L).let { if (it <= 0L) 1000L else it }
                delay(delayToNextSecond.coerceIn(50L, 1000L))
            }

            if (timeLeftSeconds <= 0 && !paused && isActive) {
                withContext(Dispatchers.Main) {
                    finishSession(skipped = false)
                }
            }
        }
    }

    private fun finishSession(skipped: Boolean) {
        val completedMode = currentMode
        endedModeStr = if (!skipped) completedMode.name else ""

        if (!skipped) {
            isAlarmActive = true
            playAlarmSound(isWorkEnd = (completedMode == PomodoroMode.WORK))
        }

        // Auto-log work session if not already saved
        if (completedMode == PomodoroMode.WORK && !skipped && !hasSavedCurrentSession) {
            hasSavedCurrentSession = true
            val fullDurationMins = maxOf(1, originalDurationSeconds / 60)
            serviceScope.launch(NonCancellable + Dispatchers.IO) {
                logAndAwardSession(
                    durationMinutes = fullDurationMins,
                    isFullCompletion = true,
                    isWorkSession = true
                )
            }
        }

        // Advance Period & Cycle Progression
        if (completedMode == PomodoroMode.WORK) {
            sessionsCompletedCount++
            if (sessionsCompletedCount >= periodSessions) {
                currentMode = PomodoroMode.LONG_BREAK
                sessionsCompletedCount = 0
            } else {
                currentMode = PomodoroMode.SHORT_BREAK
            }
        } else if (completedMode == PomodoroMode.LONG_BREAK) {
            periodsCompleted++
            if (maxPeriods > 0 && periodsCompleted >= maxPeriods) {
                isServiceRunning = false
                syncToState()
                stopForeground(false)
                stopSelf()
                return
            }
            currentMode = PomodoroMode.WORK
        } else {
            currentMode = PomodoroMode.WORK
        }

        startCurrentMode(startPaused = !skipped)
    }

    private fun saveElapsedWorkSessionIfNeeded() {
        if (currentMode != PomodoroMode.WORK || hasSavedCurrentSession) return
        val elapsedSeconds = originalDurationSeconds - timeLeftSeconds
        if (elapsedSeconds >= 120) {
            val mins = elapsedSeconds / 60
            hasSavedCurrentSession = true
            serviceScope.launch(NonCancellable + Dispatchers.IO) {
                logAndAwardSession(
                    durationMinutes = mins,
                    isFullCompletion = false,
                    isWorkSession = true
                )
            }
        }
    }

    private suspend fun logAndAwardSession(
        durationMinutes: Int,
        isFullCompletion: Boolean,
        isWorkSession: Boolean
    ) {
        if (!isWorkSession || durationMinutes <= 0) return
        try {
            val profMgr = ProfileManager(applicationContext)
            val isAutoLogEnabled = profMgr.getProfilePrefs().getBoolean("system_pomodoro_auto_log", true)
            if (!isAutoLogEnabled && isFullCompletion) {
                Log.d(TAG, "Auto-logging disabled in settings.")
                return
            }

            val db = AppDatabase.getDatabase(applicationContext)
            val session = PomodoroSession(
                dateMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                subjectId = activeSubjectId,
                courseId = activeCourseId,
                assignmentId = activeAssignmentId,
                taskId = activeTaskId,
                topicId = activeTopicId
            )
            db.scholarDao().insertPomodoroSession(session)

            val actionLabel = if (isFullCompletion) "Completed" else "Focused partially on"
            db.scholarDao().insertActionLog(
                ActionLog(actionText = "$actionLabel Pomodoro Session ($durationMinutes min)")
            )
            Log.d(TAG, "Auto-logged session: $durationMinutes min (courseId=$activeCourseId, subjectId=$activeSubjectId)")

            // Show completion summary notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mainIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("OPEN_POMODORO", true)
            }
            val mainPending = PendingIntent.getActivity(
                applicationContext,
                COMPLETION_NOTIFICATION_ID,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val completionNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(NotificationHelper.getSmallIcon())
                .setContentTitle(if (isFullCompletion) "Focus Session Completed!" else "Focus Progress Saved!")
                .setContentText("Locked in $durationMinutes min study with context preserved.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(mainPending)
                .setColor(NotificationHelper.getColor(applicationContext))
                .build()

            notificationManager.notify(COMPLETION_NOTIFICATION_ID, completionNotification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to auto-log pomodoro session", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val fgChannel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing Pomodoro countdown and controls"
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Pomodoro Session Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Audible and visual alerts when a focus or rest session finishes"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(fgChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    private fun startAsForeground() {
        val notification = buildNotification(timeLeftSeconds)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateForegroundNotification(time: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val notification = buildNotification(time)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(time: Int): android.app.Notification {
        val minutes = time / 60
        val seconds = time % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        val title = when (currentMode) {
            PomodoroMode.WORK -> "Focusing (Session ${sessionsCompletedCount + 1}/$periodSessions)"
            PomodoroMode.SHORT_BREAK -> "Short Break"
            PomodoroMode.LONG_BREAK -> "Long Break (Cycle Complete!)"
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_POMODORO", true)
        }
        val mainPending = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (isAlarmActive) {
            val stopAlarmIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "STOP_ALARM" }
            val stopAlarmPending = PendingIntent.getBroadcast(
                this,
                1004,
                stopAlarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                .setSmallIcon(NotificationHelper.getSmallIcon())
                .setContentTitle(if (currentMode == PomodoroMode.WORK) "Break Finished! Time to Focus" else "Focus Session Complete!")
                .setContentText("Alarm sounding. Tap to stop sound.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(mainPending)
                .setOngoing(true)
                .setColor(NotificationHelper.getColor(this))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", stopAlarmPending)
                .build()
        }

        // Notification Control Pending Intents with distinct request codes
        val pauseIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "PAUSE_RESUME" }
        val pausePending = PendingIntent.getBroadcast(
            this,
            1001,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "SKIP" }
        val skipPending = PendingIntent.getBroadcast(
            this,
            1002,
            skipIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, PomodoroActionReceiver::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getBroadcast(
            this,
            1003,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val progressMax = originalDurationSeconds
        val progressNow = originalDurationSeconds - time

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr" + if (paused) " (PAUSED)" else "")
            .setProgress(progressMax, progressNow, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainPending)
            .setOngoing(true)
            .setColor(NotificationHelper.getColor(this))
            .setUsesChronometer(!paused)
            .setWhen(System.currentTimeMillis() + time * 1000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setChronometerCountDown(true)
        }

        return builder
            .addAction(
                if (paused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (paused) "Resume" else "Pause",
                pausePending
            )
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun sendTickBroadcast() {
        val tickIntent = Intent("PomodoroTick").apply { setPackage(packageName) }
        tickIntent.putExtra("timeLeft", timeLeftSeconds)
        tickIntent.putExtra("originalTime", originalDurationSeconds)
        tickIntent.putExtra("mode", currentMode.name)
        tickIntent.putExtra("isPaused", paused)
        tickIntent.putExtra("sessionsCompleted", sessionsCompletedCount)
        sendBroadcast(tickIntent)
        updatePomodoroWidget()
    }

    private fun syncToState() {
        updateState {
            it.copy(
                isRunning = isServiceRunning,
                isPaused = paused,
                timeLeft = timeLeftSeconds,
                originalTime = originalDurationSeconds,
                modeString = currentMode.name,
                sessionsCompleted = sessionsCompletedCount,
                subjectId = activeSubjectId,
                courseId = activeCourseId,
                assignmentId = activeAssignmentId,
                taskId = activeTaskId,
                topicId = activeTopicId,
                isAlarmActive = isAlarmActive,
                endedModeStr = endedModeStr
            )
        }
        updatePomodoroWidget()
    }

    private fun playAlarmSound(isWorkEnd: Boolean) {
        stopAlarmSound()
        try {
            val soundUri = if (isWorkEnd) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioStreamType(if (isWorkEnd) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION)
                isLooping = isWorkEnd
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            try {
                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000)
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
            Log.e(TAG, "Error stopping alarm sound", e)
        }
    }

    private fun updatePomodoroWidget() {
        try {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(applicationContext)
            val componentName = android.content.ComponentName(
                applicationContext,
                ScholarPomodoroWidgetProvider::class.java
            )
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(applicationContext, ScholarPomodoroWidgetProvider::class.java).apply {
                    action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating Pomodoro widget", e)
        }
    }

    override fun onDestroy() {
        saveElapsedWorkSessionIfNeeded()
        isServiceRunning = false
        stopAlarmSound()
        tickerJob?.cancel()
        serviceScope.cancel()
        if (instance == this) {
            instance = null
        }
        syncToState()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
