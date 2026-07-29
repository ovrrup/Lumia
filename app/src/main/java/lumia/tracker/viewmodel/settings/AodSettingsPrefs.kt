package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AodSettingsPrefs(private val prefs: SharedPreferences) {

    private val _aodTrueBlackOled = MutableStateFlow(prefs.getBoolean("aod_true_black_oled", true))
    val aodTrueBlackOled: StateFlow<Boolean> = _aodTrueBlackOled.asStateFlow()

    private val _aodAutoDeactivateTrueBlack = MutableStateFlow(prefs.getBoolean("aod_auto_deactivate_true_black", true))
    val aodAutoDeactivateTrueBlack: StateFlow<Boolean> = _aodAutoDeactivateTrueBlack.asStateFlow()

    private val _aodBurnInShiftSpeed = MutableStateFlow(prefs.getInt("aod_burn_in_shift_speed", 10))
    val aodBurnInShiftSpeed: StateFlow<Int> = _aodBurnInShiftSpeed.asStateFlow()

    private val _aodLockScreenSupport = MutableStateFlow(prefs.getBoolean("aod_lock_screen_support", false))
    val aodLockScreenSupport: StateFlow<Boolean> = _aodLockScreenSupport.asStateFlow()

    private val _aodTrueAodEnabled = MutableStateFlow(prefs.getBoolean("aod_true_aod_enabled", false))
    val aodTrueAodEnabled: StateFlow<Boolean> = _aodTrueAodEnabled.asStateFlow()

    private val _aodTrueAodMode = MutableStateFlow(prefs.getString("aod_true_aod_mode", "overlay") ?: "overlay")
    val aodTrueAodMode: StateFlow<String> = _aodTrueAodMode.asStateFlow()

    private val _aodSensitivity = MutableStateFlow(prefs.getString("aod_sensitivity", "highest") ?: "highest")
    val aodSensitivity: StateFlow<String> = _aodSensitivity.asStateFlow()

    private val _aodMotionSensitivity = MutableStateFlow(prefs.getFloat("aod_motion_sensitivity", 1.2f))
    val aodMotionSensitivity: StateFlow<Float> = _aodMotionSensitivity.asStateFlow()

    private val _aodDimnessLevel = MutableStateFlow(prefs.getFloat("aod_dimness_level", 0.95f))
    val aodDimnessLevel: StateFlow<Float> = _aodDimnessLevel.asStateFlow()

    private val _aodLockTimeout = MutableStateFlow(prefs.getInt("aod_lock_timeout", 30))
    val aodLockTimeout: StateFlow<Int> = _aodLockTimeout.asStateFlow()

    fun updateAodLockScreenSupport(enabled: Boolean) {
        _aodLockScreenSupport.value = enabled
        prefs.edit().putBoolean("aod_lock_screen_support", enabled).apply()
    }

    fun updateAodTrueAodEnabled(enabled: Boolean) {
        _aodTrueAodEnabled.value = enabled
        prefs.edit().putBoolean("aod_true_aod_enabled", enabled).apply()
    }

    fun updateAodTrueAodMode(mode: String) {
        _aodTrueAodMode.value = mode
        prefs.edit().putString("aod_true_aod_mode", mode).apply()
    }

    fun updateAodSensitivity(sensitivity: String) {
        _aodSensitivity.value = sensitivity
        prefs.edit().putString("aod_sensitivity", sensitivity).apply()
    }

    fun updateAodMotionSensitivity(sensitivity: Float) {
        _aodMotionSensitivity.value = sensitivity
        prefs.edit().putFloat("aod_motion_sensitivity", sensitivity).apply()
    }

    fun updateAodDimnessLevel(level: Float) {
        _aodDimnessLevel.value = level
        prefs.edit().putFloat("aod_dimness_level", level).apply()
    }

    fun updateAodLockTimeout(seconds: Int) {
        _aodLockTimeout.value = seconds
        prefs.edit().putInt("aod_lock_timeout", seconds).apply()
    }

    fun updateAodTrueBlackOledDirect(enabled: Boolean) {
        _aodTrueBlackOled.value = enabled
        prefs.edit().putBoolean("aod_true_black_oled", enabled).apply()
    }

    fun updateAodAutoDeactivateTrueBlack(enabled: Boolean) {
        _aodAutoDeactivateTrueBlack.value = enabled
        prefs.edit().putBoolean("aod_auto_deactivate_true_black", enabled).apply()
    }

    fun updateAodBurnInShiftSpeed(speed: Int) {
        _aodBurnInShiftSpeed.value = speed
        prefs.edit().putInt("aod_burn_in_shift_speed", speed).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "aod_true_black_oled" -> _aodTrueBlackOled.value = value.toBooleanStrictOrNull() ?: true
            "aod_auto_deactivate_true_black" -> _aodAutoDeactivateTrueBlack.value = value.toBooleanStrictOrNull() ?: true
            "aod_burn_in_shift_speed" -> _aodBurnInShiftSpeed.value = value.toIntOrNull() ?: 10
            "aod_lock_screen_support" -> _aodLockScreenSupport.value = value.toBooleanStrictOrNull() ?: false
            "aod_true_aod_enabled" -> _aodTrueAodEnabled.value = value.toBooleanStrictOrNull() ?: false
            "aod_true_aod_mode" -> _aodTrueAodMode.value = value
            "aod_sensitivity" -> _aodSensitivity.value = value
            "aod_motion_sensitivity" -> _aodMotionSensitivity.value = value.toFloatOrNull() ?: 1.2f
            "aod_dimness_level" -> _aodDimnessLevel.value = value.toFloatOrNull() ?: 0.95f
            "aod_lock_timeout" -> _aodLockTimeout.value = value.toIntOrNull() ?: 30
        }
    }
}
