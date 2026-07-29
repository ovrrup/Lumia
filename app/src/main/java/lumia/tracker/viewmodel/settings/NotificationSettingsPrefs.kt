package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationSettingsPrefs(private val prefs: SharedPreferences) {

    private val _notifFormalTone = MutableStateFlow(prefs.getBoolean("notif_formal_tone", true))
    val notifFormalTone: StateFlow<Boolean> = _notifFormalTone.asStateFlow()

    private val _notifEnableDeadlines = MutableStateFlow(prefs.getBoolean("notif_enable_deadlines", true))
    val notifEnableDeadlines: StateFlow<Boolean> = _notifEnableDeadlines.asStateFlow()

    private val _notifEnableClasses = MutableStateFlow(prefs.getBoolean("notif_enable_classes", true))
    val notifEnableClasses: StateFlow<Boolean> = _notifEnableClasses.asStateFlow()

    private val _notifEnableDailyDigest = MutableStateFlow(prefs.getBoolean("notif_enable_daily_digest", true))
    val notifEnableDailyDigest: StateFlow<Boolean> = _notifEnableDailyDigest.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(prefs.getBoolean("sound_effects_enabled", true))
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    fun updateNotifFormalTone(enabled: Boolean) {
        _notifFormalTone.value = enabled
        prefs.edit().putBoolean("notif_formal_tone", enabled).apply()
    }

    fun updateNotifEnableDeadlines(enabled: Boolean) {
        _notifEnableDeadlines.value = enabled
        prefs.edit().putBoolean("notif_enable_deadlines", enabled).apply()
    }

    fun updateNotifEnableClasses(enabled: Boolean) {
        _notifEnableClasses.value = enabled
        prefs.edit().putBoolean("notif_enable_classes", enabled).apply()
    }

    fun updateNotifEnableDailyDigest(enabled: Boolean) {
        _notifEnableDailyDigest.value = enabled
        prefs.edit().putBoolean("notif_enable_daily_digest", enabled).apply()
    }

    fun updateSoundEffectsEnabled(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
        prefs.edit().putBoolean("sound_effects_enabled", enabled).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "notif_formal_tone" -> _notifFormalTone.value = value.toBooleanStrictOrNull() ?: true
            "notif_enable_deadlines" -> _notifEnableDeadlines.value = value.toBooleanStrictOrNull() ?: true
            "notif_enable_classes" -> _notifEnableClasses.value = value.toBooleanStrictOrNull() ?: true
            "notif_enable_daily_digest" -> _notifEnableDailyDigest.value = value.toBooleanStrictOrNull() ?: true
            "sound_effects_enabled" -> _soundEffectsEnabled.value = value.toBooleanStrictOrNull() ?: true
        }
    }
}
