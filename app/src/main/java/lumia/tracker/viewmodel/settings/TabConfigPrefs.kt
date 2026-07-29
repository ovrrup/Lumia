package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TabConfigPrefs(private val prefs: SharedPreferences) {

    private val _tabHomeLabel = MutableStateFlow(prefs.getString("tab_home_label", "Home") ?: "Home")
    val tabHomeLabel: StateFlow<String> = _tabHomeLabel.asStateFlow()

    private val _tabHomeIcon = MutableStateFlow(prefs.getString("tab_home_icon", "Home") ?: "Home")
    val tabHomeIcon: StateFlow<String> = _tabHomeIcon.asStateFlow()

    private val _tabCoursesLabel = MutableStateFlow(prefs.getString("tab_courses_label", "Courses") ?: "Courses")
    val tabCoursesLabel: StateFlow<String> = _tabCoursesLabel.asStateFlow()

    private val _tabCoursesIcon = MutableStateFlow(prefs.getString("tab_courses_icon", "MenuBook") ?: "MenuBook")
    val tabCoursesIcon: StateFlow<String> = _tabCoursesIcon.asStateFlow()

    private val _tabSubjectsLabel = MutableStateFlow(prefs.getString("tab_subjects_label", "Subjects") ?: "Subjects")
    val tabSubjectsLabel: StateFlow<String> = _tabSubjectsLabel.asStateFlow()

    private val _tabSubjectsIcon = MutableStateFlow(prefs.getString("tab_subjects_icon", "FolderOpen") ?: "FolderOpen")
    val tabSubjectsIcon: StateFlow<String> = _tabSubjectsIcon.asStateFlow()

    private val _tabSelfStudyLabel = MutableStateFlow(prefs.getString("tab_self_study_label", "Self Study") ?: "Self Study")
    val tabSelfStudyLabel: StateFlow<String> = _tabSelfStudyLabel.asStateFlow()

    private val _tabSelfStudyIcon = MutableStateFlow(prefs.getString("tab_self_study_icon", "AutoStories") ?: "AutoStories")
    val tabSelfStudyIcon: StateFlow<String> = _tabSelfStudyIcon.asStateFlow()

    private val _tabAnalyticsLabel = MutableStateFlow(prefs.getString("tab_analytics_label", "Analytics") ?: "Analytics")
    val tabAnalyticsLabel: StateFlow<String> = _tabAnalyticsLabel.asStateFlow()

    private val _tabAnalyticsIcon = MutableStateFlow(prefs.getString("tab_analytics_icon", "Analytics") ?: "Analytics")
    val tabAnalyticsIcon: StateFlow<String> = _tabAnalyticsIcon.asStateFlow()

    private val _tabCalendarLabel = MutableStateFlow(prefs.getString("tab_calendar_label", "Calendar") ?: "Calendar")
    val tabCalendarLabel: StateFlow<String> = _tabCalendarLabel.asStateFlow()

    private val _tabCalendarIcon = MutableStateFlow(prefs.getString("tab_calendar_icon", "CalendarMonth") ?: "CalendarMonth")
    val tabCalendarIcon: StateFlow<String> = _tabCalendarIcon.asStateFlow()

    fun updateTabHomeLabel(value: String) {
        _tabHomeLabel.value = value
        prefs.edit().putString("tab_home_label", value).apply()
    }
    fun updateTabHomeIcon(value: String) {
        _tabHomeIcon.value = value
        prefs.edit().putString("tab_home_icon", value).apply()
    }
    fun updateTabCoursesLabel(value: String) {
        _tabCoursesLabel.value = value
        prefs.edit().putString("tab_courses_label", value).apply()
    }
    fun updateTabCoursesIcon(value: String) {
        _tabCoursesIcon.value = value
        prefs.edit().putString("tab_courses_icon", value).apply()
    }
    fun updateTabSubjectsLabel(value: String) {
        _tabSubjectsLabel.value = value
        prefs.edit().putString("tab_subjects_label", value).apply()
    }
    fun updateTabSubjectsIcon(value: String) {
        _tabSubjectsIcon.value = value
        prefs.edit().putString("tab_subjects_icon", value).apply()
    }
    fun updateTabSelfStudyLabel(value: String) {
        _tabSelfStudyLabel.value = value
        prefs.edit().putString("tab_self_study_label", value).apply()
    }
    fun updateTabSelfStudyIcon(value: String) {
        _tabSelfStudyIcon.value = value
        prefs.edit().putString("tab_self_study_icon", value).apply()
    }
    fun updateTabAnalyticsLabel(value: String) {
        _tabAnalyticsLabel.value = value
        prefs.edit().putString("tab_analytics_label", value).apply()
    }
    fun updateTabAnalyticsIcon(value: String) {
        _tabAnalyticsIcon.value = value
        prefs.edit().putString("tab_analytics_icon", value).apply()
    }
    fun updateTabCalendarLabel(value: String) {
        _tabCalendarLabel.value = value
        prefs.edit().putString("tab_calendar_label", value).apply()
    }
    fun updateTabCalendarIcon(value: String) {
        _tabCalendarIcon.value = value
        prefs.edit().putString("tab_calendar_icon", value).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "tab_home_label" -> _tabHomeLabel.value = value
            "tab_home_icon" -> _tabHomeIcon.value = value
            "tab_courses_label" -> _tabCoursesLabel.value = value
            "tab_courses_icon" -> _tabCoursesIcon.value = value
            "tab_subjects_label" -> _tabSubjectsLabel.value = value
            "tab_subjects_icon" -> _tabSubjectsIcon.value = value
            "tab_self_study_label" -> _tabSelfStudyLabel.value = value
            "tab_self_study_icon" -> _tabSelfStudyIcon.value = value
            "tab_analytics_label" -> _tabAnalyticsLabel.value = value
            "tab_analytics_icon" -> _tabAnalyticsIcon.value = value
            "tab_calendar_label" -> _tabCalendarLabel.value = value
            "tab_calendar_icon" -> _tabCalendarIcon.value = value
        }
    }
}
