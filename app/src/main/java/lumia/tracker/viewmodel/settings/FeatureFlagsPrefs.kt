package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeatureFlagsPrefs(private val prefs: SharedPreferences) {

    private val _featureSubjectEnabled = MutableStateFlow(prefs.getBoolean("feature_subject_enabled", true))
    val featureSubjectEnabled: StateFlow<Boolean> = _featureSubjectEnabled.asStateFlow()

    private val _featureSelfStudyEnabled = MutableStateFlow(prefs.getBoolean("feature_self_study_enabled", true))
    val featureSelfStudyEnabled: StateFlow<Boolean> = _featureSelfStudyEnabled.asStateFlow()

    private val _featureAnalyticsEnabled = MutableStateFlow(prefs.getBoolean("feature_analytics_enabled", true))
    val featureAnalyticsEnabled: StateFlow<Boolean> = _featureAnalyticsEnabled.asStateFlow()

    private val _featureCalendarEnabled = MutableStateFlow(prefs.getBoolean("feature_calendar_enabled", true))
    val featureCalendarEnabled: StateFlow<Boolean> = _featureCalendarEnabled.asStateFlow()

    private val _featureQuickNotesEnabled = MutableStateFlow(prefs.getBoolean("feature_quick_notes_enabled", true))
    val featureQuickNotesEnabled: StateFlow<Boolean> = _featureQuickNotesEnabled.asStateFlow()

    private val _betaNotes = MutableStateFlow(prefs.getBoolean("beta_notes", false))
    val betaNotes: StateFlow<Boolean> = _betaNotes.asStateFlow()

    fun updateFeatureSubjectEnabled(enabled: Boolean) {
        _featureSubjectEnabled.value = enabled
        prefs.edit().putBoolean("feature_subject_enabled", enabled).apply()
    }

    fun updateFeatureSelfStudyEnabled(enabled: Boolean) {
        _featureSelfStudyEnabled.value = enabled
        prefs.edit().putBoolean("feature_self_study_enabled", enabled).apply()
    }

    fun updateFeatureAnalyticsEnabled(enabled: Boolean) {
        _featureAnalyticsEnabled.value = enabled
        prefs.edit().putBoolean("feature_analytics_enabled", enabled).apply()
    }

    fun updateFeatureCalendarEnabled(enabled: Boolean) {
        _featureCalendarEnabled.value = enabled
        prefs.edit().putBoolean("feature_calendar_enabled", enabled).apply()
    }

    fun updateFeatureQuickNotesEnabled(enabled: Boolean) {
        _featureQuickNotesEnabled.value = enabled
        prefs.edit().putBoolean("feature_quick_notes_enabled", enabled).apply()
    }

    fun updateBetaNotes(enabled: Boolean) {
        _betaNotes.value = enabled
        prefs.edit().putBoolean("beta_notes", enabled).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "feature_subject_enabled" -> _featureSubjectEnabled.value = value.toBooleanStrictOrNull() ?: true
            "feature_self_study_enabled" -> _featureSelfStudyEnabled.value = value.toBooleanStrictOrNull() ?: true
            "feature_analytics_enabled" -> _featureAnalyticsEnabled.value = value.toBooleanStrictOrNull() ?: true
            "feature_calendar_enabled" -> _featureCalendarEnabled.value = value.toBooleanStrictOrNull() ?: true
            "feature_quick_notes_enabled" -> _featureQuickNotesEnabled.value = value.toBooleanStrictOrNull() ?: true
            "beta_notes" -> _betaNotes.value = value.toBooleanStrictOrNull() ?: false
        }
    }
}
