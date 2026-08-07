package lumia.tracker.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PomodoroStateHolder {
    val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state

    var isServiceRunning: Boolean
        get() = _state.value.isRunning
        set(value) { _state.value = _state.value.copy(isRunning = value) }

    var currentStateStr: String
        get() = _state.value.modeString
        set(value) { _state.value = _state.value.copy(modeString = value) }

    var timeLeft: Int
        get() = _state.value.timeLeft
        set(value) { _state.value = _state.value.copy(timeLeft = value) }

    var originalTime: Int
        get() = _state.value.originalTime
        set(value) { _state.value = _state.value.copy(originalTime = value) }

    var isPaused: Boolean
        get() = _state.value.isPaused
        set(value) { _state.value = _state.value.copy(isPaused = value) }

    var sessionsCompleted: Int
        get() = _state.value.sessionsCompleted
        set(value) { _state.value = _state.value.copy(sessionsCompleted = value) }

    var subjectId: Int?
        get() = _state.value.subjectId
        set(value) { _state.value = _state.value.copy(subjectId = value) }

    var courseId: Int?
        get() = _state.value.courseId
        set(value) { _state.value = _state.value.copy(courseId = value) }

    var assignmentId: Int?
        get() = _state.value.assignmentId
        set(value) { _state.value = _state.value.copy(assignmentId = value) }

    var taskId: Int?
        get() = _state.value.taskId
        set(value) { _state.value = _state.value.copy(taskId = value) }

    var topicId: Int?
        get() = _state.value.topicId
        set(value) { _state.value = _state.value.copy(topicId = value) }

    fun updateState(block: (PomodoroState) -> PomodoroState) {
        _state.value = block(_state.value)
    }
}
