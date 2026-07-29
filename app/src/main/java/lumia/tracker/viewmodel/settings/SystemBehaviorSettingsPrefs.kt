package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SystemBehaviorSettingsPrefs(private val prefs: SharedPreferences) {

    private val _systemAutoLinkByName = MutableStateFlow(prefs.getBoolean("system_auto_link_by_name", true))
    val systemAutoLinkByName: StateFlow<Boolean> = _systemAutoLinkByName.asStateFlow()

    private val _systemEnableSynergy = MutableStateFlow(prefs.getBoolean("system_enable_synergy", true))
    val systemEnableSynergy: StateFlow<Boolean> = _systemEnableSynergy.asStateFlow()

    private val _systemAutoCreateSubject = MutableStateFlow(prefs.getBoolean("system_auto_create_subject", false))
    val systemAutoCreateSubject: StateFlow<Boolean> = _systemAutoCreateSubject.asStateFlow()

    private val _systemFuseSubjectsCourses = MutableStateFlow(prefs.getBoolean("system_fuse_subjects_courses", true))
    val systemFuseSubjectsCourses: StateFlow<Boolean> = _systemFuseSubjectsCourses.asStateFlow()

    private val _systemAdvancedTasks = MutableStateFlow(prefs.getBoolean("system_advanced_tasks", true))
    val systemAdvancedTasks: StateFlow<Boolean> = _systemAdvancedTasks.asStateFlow()

    private val _systemPomodoroAutoLog = MutableStateFlow(prefs.getBoolean("system_pomodoro_auto_log", true))
    val systemPomodoroAutoLog: StateFlow<Boolean> = _systemPomodoroAutoLog.asStateFlow()

    fun updateSystemAutoLinkByName(enabled: Boolean) {
        _systemAutoLinkByName.value = enabled
        prefs.edit().putBoolean("system_auto_link_by_name", enabled).apply()
    }

    fun updateSystemEnableSynergy(enabled: Boolean) {
        _systemEnableSynergy.value = enabled
        prefs.edit().putBoolean("system_enable_synergy", enabled).apply()
    }

    fun updateSystemAutoCreateSubject(enabled: Boolean) {
        _systemAutoCreateSubject.value = enabled
        prefs.edit().putBoolean("system_auto_create_subject", enabled).apply()
    }

    fun updateSystemFuseSubjectsCourses(enabled: Boolean) {
        _systemFuseSubjectsCourses.value = enabled
        prefs.edit().putBoolean("system_fuse_subjects_courses", enabled).apply()
    }

    fun updateSystemAdvancedTasks(enabled: Boolean) {
        _systemAdvancedTasks.value = enabled
        prefs.edit().putBoolean("system_advanced_tasks", enabled).apply()
    }

    fun updateSystemPomodoroAutoLog(enabled: Boolean) {
        _systemPomodoroAutoLog.value = enabled
        prefs.edit().putBoolean("system_pomodoro_auto_log", enabled).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "system_auto_link_by_name" -> _systemAutoLinkByName.value = value.toBooleanStrictOrNull() ?: true
            "system_enable_synergy" -> _systemEnableSynergy.value = value.toBooleanStrictOrNull() ?: true
            "system_auto_create_subject" -> _systemAutoCreateSubject.value = value.toBooleanStrictOrNull() ?: false
            "system_fuse_subjects_courses" -> _systemFuseSubjectsCourses.value = value.toBooleanStrictOrNull() ?: true
            "system_advanced_tasks" -> _systemAdvancedTasks.value = value.toBooleanStrictOrNull() ?: true
            "system_pomodoro_auto_log" -> _systemPomodoroAutoLog.value = value.toBooleanStrictOrNull() ?: true
        }
    }
}
