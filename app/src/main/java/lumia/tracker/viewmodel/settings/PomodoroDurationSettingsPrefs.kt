package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PomodoroDurationSettingsPrefs(private val prefs: SharedPreferences) {

    private val _pomodoroWorkDuration = MutableStateFlow(prefs.getInt("pomodoro_work_duration", 25))
    val pomodoroWorkDuration: StateFlow<Int> = _pomodoroWorkDuration.asStateFlow()

    private val _pomodoroShortBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_short_break_duration", 5))
    val pomodoroShortBreakDuration: StateFlow<Int> = _pomodoroShortBreakDuration.asStateFlow()

    private val _pomodoroLongBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_long_break_duration", 15))
    val pomodoroLongBreakDuration: StateFlow<Int> = _pomodoroLongBreakDuration.asStateFlow()

    private val _pomodoroPeriodSessions = MutableStateFlow(prefs.getInt("pomodoro_period_sessions", 4))
    val pomodoroPeriodSessions: StateFlow<Int> = _pomodoroPeriodSessions.asStateFlow()

    private val _pomodoroEnablePeriodTarget = MutableStateFlow(prefs.getBoolean("pomodoro_enable_period_target", false))
    val pomodoroEnablePeriodTarget: StateFlow<Boolean> = _pomodoroEnablePeriodTarget.asStateFlow()

    fun updatePomodoroWorkDuration(duration: Int) {
        _pomodoroWorkDuration.value = duration
        prefs.edit().putInt("pomodoro_work_duration", duration).apply()
    }

    fun updatePomodoroShortBreakDuration(duration: Int) {
        _pomodoroShortBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_short_break_duration", duration).apply()
    }

    fun updatePomodoroLongBreakDuration(duration: Int) {
        _pomodoroLongBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_long_break_duration", duration).apply()
    }

    fun updatePomodoroPeriodSessions(sessions: Int) {
        _pomodoroPeriodSessions.value = sessions
        prefs.edit().putInt("pomodoro_period_sessions", sessions).apply()
    }

    fun updatePomodoroEnablePeriodTarget(enabled: Boolean) {
        _pomodoroEnablePeriodTarget.value = enabled
        prefs.edit().putBoolean("pomodoro_enable_period_target", enabled).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "pomodoro_work_duration" -> _pomodoroWorkDuration.value = value.toIntOrNull() ?: 25
            "pomodoro_short_break_duration" -> _pomodoroShortBreakDuration.value = value.toIntOrNull() ?: 5
            "pomodoro_long_break_duration" -> _pomodoroLongBreakDuration.value = value.toIntOrNull() ?: 15
            "pomodoro_period_sessions" -> _pomodoroPeriodSessions.value = value.toIntOrNull() ?: 4
            "pomodoro_enable_period_target" -> _pomodoroEnablePeriodTarget.value = value.toBooleanStrictOrNull() ?: false
        }
    }
}
