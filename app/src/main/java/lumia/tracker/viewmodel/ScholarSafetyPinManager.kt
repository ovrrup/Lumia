package lumia.tracker.viewmodel

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SafetyPinDialogData(
    val title: String,
    val description: String,
    val isConflict: Boolean = true,
    val onConfirm: () -> Unit,
    val onIgnore: () -> Unit
)

class ScholarSafetyPinManager(private val prefs: SharedPreferences) {
    private val _safetyPinEnabled = MutableStateFlow(prefs.getBoolean("safety_pin_enabled", true))
    val safetyPinEnabled = _safetyPinEnabled.asStateFlow()

    private val _safetyPinConflictWarning = MutableStateFlow(prefs.getBoolean("safety_pin_conflict_warning", true))
    val safetyPinConflictWarning = _safetyPinConflictWarning.asStateFlow()

    private val _safetyPinRecommendations = MutableStateFlow(prefs.getBoolean("safety_pin_recommendations", true))
    val safetyPinRecommendations = _safetyPinRecommendations.asStateFlow()

    private val _safetyPinDialogData = MutableStateFlow<SafetyPinDialogData?>(null)
    val safetyPinDialogData = _safetyPinDialogData.asStateFlow()

    fun dismissSafetyPinDialog() {
        _safetyPinDialogData.value = null
    }

    fun setSafetyPinDialog(data: SafetyPinDialogData?) {
        _safetyPinDialogData.value = data
    }

    fun updateSafetyPinEnabled(enabled: Boolean) {
        _safetyPinEnabled.value = enabled
        prefs.edit().putBoolean("safety_pin_enabled", enabled).apply()
    }

    fun updateSafetyPinConflictWarning(enabled: Boolean) {
        _safetyPinConflictWarning.value = enabled
        prefs.edit().putBoolean("safety_pin_conflict_warning", enabled).apply()
    }

    fun updateSafetyPinRecommendations(enabled: Boolean) {
        _safetyPinRecommendations.value = enabled
        prefs.edit().putBoolean("safety_pin_recommendations", enabled).apply()
    }
}
