package lumia.tracker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

class PomodoroService : Service() {

    companion object {
        val state: StateFlow<PomodoroState> get() = PomodoroStateHolder.state
        var isServiceRunning: Boolean get() = PomodoroStateHolder.isServiceRunning; set(v) { PomodoroStateHolder.isServiceRunning = v }
        var currentStateStr: String get() = PomodoroStateHolder.currentStateStr; set(v) { PomodoroStateHolder.currentStateStr = v }
        var timeLeft: Int get() = PomodoroStateHolder.timeLeft; set(v) { PomodoroStateHolder.timeLeft = v }
        var originalTime: Int get() = PomodoroStateHolder.originalTime; set(v) { PomodoroStateHolder.originalTime = v }
        var isPaused: Boolean get() = PomodoroStateHolder.isPaused; set(v) { PomodoroStateHolder.isPaused = v }
        var sessionsCompleted: Int get() = PomodoroStateHolder.sessionsCompleted; set(v) { PomodoroStateHolder.sessionsCompleted = v }
        var subjectId: Int? get() = PomodoroStateHolder.subjectId; set(v) { PomodoroStateHolder.subjectId = v }
        var courseId: Int? get() = PomodoroStateHolder.courseId; set(v) { PomodoroStateHolder.courseId = v }
        var assignmentId: Int? get() = PomodoroStateHolder.assignmentId; set(v) { PomodoroStateHolder.assignmentId = v }
        var taskId: Int? get() = PomodoroStateHolder.taskId; set(v) { PomodoroStateHolder.taskId = v }
        var topicId: Int? get() = PomodoroStateHolder.topicId; set(v) { PomodoroStateHolder.topicId = v }
        fun updateState(block: (PomodoroState) -> PomodoroState) = PomodoroStateHolder.updateState(block)
    }

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var timeLeft = 0
    private var isWork = true
    private var originalTime = 0
    private var isPaused = false

    private var isAlarmActive = false
    private var endedModeStr = ""
    private var hasSavedCurrentSession = false

    private var sessionsCompleted = 0
    private var currentMode = PomodoroMode.WORK

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

    private fun syncToState() {
        updateState {
            it.copy(
                isRunning = isServiceRunning, isPaused = isPaused, timeLeft = timeLeft,
                originalTime = originalTime, modeString = currentMode.name,
                sessionsCompleted = sessionsCompleted, subjectId = subjectId,
                courseId = courseId, assignmentId = assignmentId, taskId = taskId,
                topicId = topicId, isAlarmActive = isAlarmActive, endedModeStr = endedModeStr
            )
        }
        PomodoroServiceBroadcaster.updatePomodoroWidget(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy(); saveElapsedWorkSessionIfNeeded(); isServiceRunning = false
        PomodoroNotificationHelper.stopAlarmSound(); job?.cancel(); syncToState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        when (intent?.action) {
            "STOP" -> {
                if (intent.getBooleanExtra("alreadySaved", false)) hasSavedCurrentSession = true
                saveElapsedWorkSessionIfNeeded(); PomodoroNotificationHelper.stopAlarmSound(); stopSelf(); return START_NOT_STICKY
            }
            "STOP_ALARM" -> {
                PomodoroNotificationHelper.stopAlarmSound(); isAlarmActive = false; endedModeStr = ""; syncToState(); updateNotification(); return START_NOT_STICKY
            }
            "PAUSE_RESUME" -> {
                isPaused = !isPaused; if (!isPaused && job?.isActive != true) startTimer() else { updateNotification(); sendTick(); syncToState() }; return START_NOT_STICKY
            }
            "SKIP" -> {
                saveElapsedWorkSessionIfNeeded(); PomodoroNotificationHelper.stopAlarmSound(); isAlarmActive = false; job?.cancel(); finishSession(skipped = true); return START_NOT_STICKY
            }
            "UPDATE_CONTEXT" -> {
                intent.let {
                    if (it.hasExtra("subjectId")) subjectId = it.getIntExtra("subjectId", -1).takeIf { id -> id != -1 }
                    if (it.hasExtra("courseId")) courseId = it.getIntExtra("courseId", -1).takeIf { id -> id != -1 }
                    if (it.hasExtra("assignmentId")) assignmentId = it.getIntExtra("assignmentId", -1).takeIf { id -> id != -1 }
                    if (it.hasExtra("taskId")) taskId = it.getIntExtra("taskId", -1).takeIf { id -> id != -1 }
                    if (it.hasExtra("topicId")) topicId = it.getIntExtra("topicId", -1).takeIf { id -> id != -1 }
                    syncToState()
                }
                return START_NOT_STICKY
            }
            "START", "RESET" -> handleStartIntent(intent)
        }
        syncToState()
        return START_NOT_STICKY
    }

    private fun handleStartIntent(intent: Intent?) {
        PomodoroNotificationHelper.stopAlarmSound(); isAlarmActive = false
        workDuration = intent?.getIntExtra("workDuration", 25 * 60) ?: (25 * 60)
        shortBreakDuration = intent?.getIntExtra("shortBreakDuration", 5 * 60) ?: (5 * 60)
        longBreakDuration = intent?.getIntExtra("longBreakDuration", 15 * 60) ?: (15 * 60)
        periodSessions = intent?.getIntExtra("periodSessions", 4) ?: 4
        maxPeriods = intent?.getIntExtra("maxPeriods", -1) ?: -1
        subjectId = intent?.getIntExtra("subjectId", -1)?.takeIf { it != -1 }
        courseId = intent?.getIntExtra("courseId", -1)?.takeIf { it != -1 }
        assignmentId = intent?.getIntExtra("assignmentId", -1)?.takeIf { it != -1 }
        taskId = intent?.getIntExtra("taskId", -1)?.takeIf { it != -1 }
        topicId = intent?.getIntExtra("topicId", -1)?.takeIf { it != -1 }
        sessionsCompleted = 0; periodsCompleted = 0; currentMode = PomodoroMode.WORK
        startCurrentMode()
        startForeground(2002, PomodoroNotificationHelper.buildNotification(this, currentMode, timeLeft, originalTime, isPaused, isAlarmActive, sessionsCompleted, periodSessions))
    }

    private fun startCurrentMode(startPaused: Boolean = false) {
        isWork = currentMode == PomodoroMode.WORK
        originalTime = when (currentMode) {
            PomodoroMode.WORK -> workDuration
            PomodoroMode.SHORT_BREAK -> shortBreakDuration
            PomodoroMode.LONG_BREAK -> longBreakDuration
        }
        timeLeft = originalTime; isPaused = startPaused; currentStateStr = currentMode.name; hasSavedCurrentSession = false
        syncToState()
        if (!startPaused) startTimer() else { job?.cancel(); job = null; updateNotification() }
    }

    private fun updateNotification() = PomodoroServiceBroadcaster.updateNotification(this, currentMode, timeLeft, originalTime, isPaused, isAlarmActive, sessionsCompleted, periodSessions)
    private fun sendTick() = PomodoroServiceBroadcaster.sendTick(this, timeLeft, originalTime, currentMode.name, isPaused, sessionsCompleted)

    private fun startTimer() {
        job?.cancel()
        job = scope.launch {
            syncToState()
            while (timeLeft > 0) {
                if (!isPaused) { delay(1000); timeLeft--; updateNotification(); sendTick(); syncToState() } else delay(100)
            }
            finishSession(skipped = false)
        }
    }

    private fun saveElapsedWorkSessionIfNeeded() {
        if (currentMode != PomodoroMode.WORK || hasSavedCurrentSession) return
        val elapsedSeconds = originalTime - timeLeft
        if (elapsedSeconds >= 60) {
            val mins = Math.max(1, elapsedSeconds / 60); hasSavedCurrentSession = true
            scope.launch(Dispatchers.IO) {
                PomodoroSessionLogger.logAndAwardSession(applicationContext, mins, isFullCompletion = false, isWorkSession = true, subjectId, courseId, assignmentId, taskId, topicId)
            }
        }
    }

    private fun finishSession(skipped: Boolean) {
        val completedMode = currentMode; endedModeStr = if (!skipped) completedMode.name else ""
        if (!skipped) { isAlarmActive = true; PomodoroNotificationHelper.playAlarmSound(this, isWorkEnd = (completedMode == PomodoroMode.WORK)) }
        if (completedMode == PomodoroMode.WORK && !skipped && !hasSavedCurrentSession) {
            hasSavedCurrentSession = true; PomodoroSessionHandler.handleWorkSessionCompletion(this, scope, originalTime, subjectId, courseId, assignmentId, taskId, topicId)
        }
        if (completedMode == PomodoroMode.WORK) {
            sessionsCompleted++; if (sessionsCompleted >= periodSessions) { currentMode = PomodoroMode.LONG_BREAK; sessionsCompleted = 0 } else { currentMode = PomodoroMode.SHORT_BREAK }
        } else if (completedMode == PomodoroMode.LONG_BREAK) {
            periodsCompleted++; if (maxPeriods > 0 && periodsCompleted >= maxPeriods) { stopSelf(); return }; currentMode = PomodoroMode.WORK
        } else { currentMode = PomodoroMode.WORK }
        startCurrentMode(startPaused = !skipped)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
