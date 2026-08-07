package lumia.tracker.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ScholarRepository
import lumia.tracker.model.*
import lumia.tracker.viewmodel.settings.*

open class ScholarBaseViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ScholarRepository(AppDatabase.getDatabase(application).scholarDao())
    val crud = ScholarDataCrud(repository)
    val accountManager = ScholarProfileAccountManager(application, lumia.tracker.data.ProfileManager(application))
    val profileManager = accountManager.profileManager
    val prefs = profileManager.getProfilePrefs()
    val safetyPinManager = ScholarSafetyPinManager(prefs)
    val backupRestoreManager = ScholarBackupRestoreManager(application, repository)

    // Data Flows
    val courses = crud.courses; val subjects = crud.subjects; val assignments = crud.assignments
    val tasks = crud.tasks; val notes = crud.notes; val attendanceRecords = crud.attendanceRecords
    val pomodoroSessions = crud.pomodoroSessions; val attachments = crud.attachments
    val testRecords = crud.testRecords; val chapters = crud.chapters; val topics = crud.topics
    val flashcards = crud.flashcards; val tagCustomizations = crud.tagCustomizations; val actionLogs = crud.actionLogs

    val streakManager = ScholarStreakManager(repository, prefs) { channelId, notifId, title, text, iconRes, color, openScreen ->
        lumia.tracker.util.StreakNotifications.sendInstantNotification(getApplication(), channelId, notifId, title, text, iconRes, color, openScreen)
    }

    val activeProfile = accountManager.activeProfile; val allProfiles = accountManager.allProfiles
    val isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false)).asStateFlow()
    fun completeOnboarding() { prefs.edit().putBoolean("onboarding_completed", true).apply() }

    protected val _selectedDashboardTab = MutableStateFlow(0)
    val selectedDashboardTab = _selectedDashboardTab.asStateFlow()
    fun setSelectedDashboardTab(tab: Int) { _selectedDashboardTab.value = tab }

    protected val _systemBarVisible = MutableStateFlow(true)
    val systemBarVisible = _systemBarVisible.asStateFlow()
    fun setSystemBarVisible(visible: Boolean) { _systemBarVisible.value = visible }

    // Settings Pref Helpers
    val navBarSettingsPrefs = NavBarSettingsPrefs(prefs); val featureFlagsPrefs = FeatureFlagsPrefs(prefs)
    val tabConfigPrefs = TabConfigPrefs(prefs); val notificationSettingsPrefs = NotificationSettingsPrefs(prefs)
    val pomodoroDurationSettingsPrefs = PomodoroDurationSettingsPrefs(prefs); val aodSettingsPrefs = AodSettingsPrefs(prefs)
    val systemBehaviorSettingsPrefs = SystemBehaviorSettingsPrefs(prefs); val glassMiscSettingsPrefs = GlassMiscSettingsPrefs(prefs)

    fun calculateTodayStreakProgress() { viewModelScope.launch(Dispatchers.IO) { streakManager.calculateTodayStreakProgress() } }
    fun loadDBStatistics() { viewModelScope.launch { backupRestoreManager.loadDBStatistics() } }
    fun defragmentDatabase() { viewModelScope.launch { backupRestoreManager.defragmentDatabase() } }
    fun clearStatus() { backupRestoreManager.clearStatus() }; fun clearImportExportStatus() { backupRestoreManager.clearStatus() }

    fun switchProfileAndRestart(context: Context, id: String) = accountManager.switchProfileAndRestart(context, id)
    fun createProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String = accountManager.createProfile(name, avatar, alias, starterTheme)
    fun setupFirstProfile(name: String, avatar: String, alias: String, starterTheme: String) = accountManager.setupFirstProfile(name, avatar, alias, starterTheme) { updateThemeColor(it) }
    fun updateProfile(name: String, avatar: String, alias: String = "") = accountManager.updateProfile(name, avatar, alias)
    fun deleteProfile(context: Context, id: String) = accountManager.deleteProfile(context, id)

    open fun updateThemeColor(color: String) { prefs.edit().putString("theme_color", color).apply() }
    open fun updateThemeMode(mode: String) { prefs.edit().putString("theme_mode", mode).apply() }
    open fun updatePureBlackMode(e: Boolean) { prefs.edit().putBoolean("pure_black_mode", e).apply() }
    open fun updateBetaGlassUi(e: Boolean) { prefs.edit().putBoolean("beta_glass_ui", e).apply() }
    open fun updateBetaDynamicBackground(e: Boolean) { prefs.edit().putBoolean("beta_dynamic_background", e).apply() }
    open fun updateBetaEnhancedHeader(e: Boolean) { prefs.edit().putBoolean("beta_enhanced_header", e).apply() }

    // CRUD Methods
    fun addCourse(course: Course) = viewModelScope.launch { crud.insertCourse(course) }
    fun addCourse(name: String, code: String = "", subjectId: Int? = null, colorHex: String = "#3197D6") = addCourse(Course(name = name, code = code, subjectId = subjectId, colorHex = colorHex))
    fun updateCourse(course: Course) = viewModelScope.launch { crud.updateCourse(course) }
    fun deleteCourse(course: Course) = viewModelScope.launch { crud.deleteCourse(course) }
    fun addSubject(subject: Subject) = viewModelScope.launch { crud.insertSubject(subject) }
    fun addSubject(name: String, code: String = "", colorHex: String = "#3197D6") = addSubject(Subject(name = name, tags = if (code.isNotEmpty()) "code:$code" else ""))
    fun updateSubject(subject: Subject) = viewModelScope.launch { crud.updateSubject(subject) }
    fun deleteSubject(subject: Subject) = viewModelScope.launch { crud.deleteSubject(subject) }
    fun addAssignment(assignment: PracticeAssignment) = viewModelScope.launch { crud.insertAssignment(assignment) }
    fun updateAssignment(assignment: PracticeAssignment) = viewModelScope.launch { crud.updateAssignment(assignment) }
    fun deleteAssignment(assignment: PracticeAssignment) = viewModelScope.launch { crud.deleteAssignment(assignment) }
    fun addTask(task: Task) = viewModelScope.launch { crud.insertTask(task) }
    fun updateTask(task: Task) = viewModelScope.launch { crud.updateTask(task) }
    fun deleteTask(task: Task) = viewModelScope.launch { crud.deleteTask(task) }
    fun toggleTaskCompleted(task: Task) = viewModelScope.launch { crud.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    fun addNote(note: Note) = viewModelScope.launch { crud.insertNote(note) }
    fun addNote(content: String, tag: String = "") = addNote(Note(content = content, tag = tag, dateMillis = System.currentTimeMillis()))
    fun updateNote(note: Note) = viewModelScope.launch { crud.updateNote(note) }
    fun deleteNote(note: Note) = viewModelScope.launch { crud.deleteNote(note) }
    fun addTestRecord(record: TestRecord) = viewModelScope.launch { crud.insertTestRecord(record) }
    fun updateTestRecord(record: TestRecord) = viewModelScope.launch { crud.updateTestRecord(record) }
    fun deleteTestRecord(record: TestRecord) = viewModelScope.launch { crud.deleteTestRecord(record) }
    fun getTestRecordsForCourse(courseId: Int) = repository.dao.getTestRecordsForCourse(courseId)

    // Settings update methods
    fun updateFeatureSubjectEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureSubjectEnabled(enabled)
    fun updateFeatureSelfStudyEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureSelfStudyEnabled(enabled)
    fun updateFeatureAnalyticsEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureAnalyticsEnabled(enabled)
    fun updateFeatureCalendarEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureCalendarEnabled(enabled)
    fun updateFeatureQuickNotesEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureQuickNotesEnabled(enabled)
    fun updateBetaNotes(enabled: Boolean) = featureFlagsPrefs.updateBetaNotes(enabled)
    fun updateTabHomeLabel(value: String) = tabConfigPrefs.updateTabHomeLabel(value); fun updateTabHomeIcon(value: String) = tabConfigPrefs.updateTabHomeIcon(value)
    fun updateTabCoursesLabel(value: String) = tabConfigPrefs.updateTabCoursesLabel(value); fun updateTabCoursesIcon(value: String) = tabConfigPrefs.updateTabCoursesIcon(value)
    fun updateTabSubjectsLabel(value: String) = tabConfigPrefs.updateTabSubjectsLabel(value); fun updateTabSubjectsIcon(value: String) = tabConfigPrefs.updateTabSubjectsIcon(value)
    fun updateTabSelfStudyLabel(value: String) = tabConfigPrefs.updateTabSelfStudyLabel(value); fun updateTabSelfStudyIcon(value: String) = tabConfigPrefs.updateTabSelfStudyIcon(value)
    fun updateTabAnalyticsLabel(value: String) = tabConfigPrefs.updateTabAnalyticsLabel(value); fun updateTabAnalyticsIcon(value: String) = tabConfigPrefs.updateTabAnalyticsIcon(value)
    fun updateTabCalendarLabel(value: String) = tabConfigPrefs.updateTabCalendarLabel(value); fun updateTabCalendarIcon(value: String) = tabConfigPrefs.updateTabCalendarIcon(value)
    fun updatePomodoroWorkDuration(d: Int) = pomodoroDurationSettingsPrefs.updatePomodoroWorkDuration(d); fun updatePomodoroShortBreakDuration(d: Int) = pomodoroDurationSettingsPrefs.updatePomodoroShortBreakDuration(d)
    fun updatePomodoroLongBreakDuration(d: Int) = pomodoroDurationSettingsPrefs.updatePomodoroLongBreakDuration(d); fun updatePomodoroPeriodSessions(s: Int) = pomodoroDurationSettingsPrefs.updatePomodoroPeriodSessions(s)
    fun updatePomodoroEnablePeriodTarget(e: Boolean) = pomodoroDurationSettingsPrefs.updatePomodoroEnablePeriodTarget(e)
    fun updateNotifFormalTone(e: Boolean) = notificationSettingsPrefs.updateNotifFormalTone(e); fun updateNotifEnableDeadlines(e: Boolean) = notificationSettingsPrefs.updateNotifEnableDeadlines(e)
    fun updateNotifEnableClasses(e: Boolean) = notificationSettingsPrefs.updateNotifEnableClasses(e); fun updateNotifEnableDailyDigest(e: Boolean) = notificationSettingsPrefs.updateNotifEnableDailyDigest(e)
    fun updateSoundEffectsEnabled(e: Boolean) = notificationSettingsPrefs.updateSoundEffectsEnabled(e)
    fun updateAodLockScreenSupport(e: Boolean) = aodSettingsPrefs.updateAodLockScreenSupport(e); fun updateAodTrueAodEnabled(e: Boolean) = aodSettingsPrefs.updateAodTrueAodEnabled(e)
    fun updateAodTrueAodMode(m: String) = aodSettingsPrefs.updateAodTrueAodMode(m); fun updateAodSensitivity(s: String) = aodSettingsPrefs.updateAodSensitivity(s)
    fun updateAodMotionSensitivity(s: Float) = aodSettingsPrefs.updateAodMotionSensitivity(s); fun updateAodDimnessLevel(l: Float) = aodSettingsPrefs.updateAodDimnessLevel(l)
    fun updateAodLockTimeout(s: Int) = aodSettingsPrefs.updateAodLockTimeout(s); fun updateAodAutoDeactivateTrueBlack(e: Boolean) = aodSettingsPrefs.updateAodAutoDeactivateTrueBlack(e)
    fun updateAodBurnInShiftSpeed(s: Int) = aodSettingsPrefs.updateAodBurnInShiftSpeed(s)
    fun updateSystemAutoLinkByName(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemAutoLinkByName(e); fun updateSystemEnableSynergy(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemEnableSynergy(e)
    fun updateSystemAutoCreateSubject(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemAutoCreateSubject(e); fun updateSystemFuseSubjectsCourses(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemFuseSubjectsCourses(e)
    fun updateSystemAdvancedTasks(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemAdvancedTasks(e); fun updateSystemPomodoroAutoLog(e: Boolean) = systemBehaviorSettingsPrefs.updateSystemPomodoroAutoLog(e)
    fun updateGlassBackdropStyle(s: String) = glassMiscSettingsPrefs.updateGlassBackdropStyle(s); fun updateGlassOpacityValue(v: Float) = glassMiscSettingsPrefs.updateGlassOpacityValue(v)
    fun updateBetaGlassDynamic(e: Boolean) = glassMiscSettingsPrefs.updateBetaGlassDynamic(e); fun updateBetaFrostGlass(e: Boolean) = glassMiscSettingsPrefs.updateBetaFrostGlass(e)
    fun updateBetaFloatingNav(e: Boolean) = navBarSettingsPrefs.updateBetaFloatingNav(e); fun updateNavBarHeight(h: Float) = navBarSettingsPrefs.updateNavBarHeight(h)
    fun updateNavBarPaddingHorizontal(p: Float) = navBarSettingsPrefs.updateNavBarPaddingHorizontal(p); fun updateNavBarPaddingBottom(p: Float) = navBarSettingsPrefs.updateNavBarPaddingBottom(p)
    fun updateNavBarCornerRadius(r: Float) = navBarSettingsPrefs.updateNavBarCornerRadius(r); fun updateNavBarLabelMode(m: String) = navBarSettingsPrefs.updateNavBarLabelMode(m)
    fun updateNavBarGlassForceEnabled(e: Boolean) = navBarSettingsPrefs.updateNavBarGlassForceEnabled(e); fun updateNavBarIndicatorAlpha(a: Float) = navBarSettingsPrefs.updateNavBarIndicatorAlpha(a)
    fun updateBetaNavBarSizeControls(e: Boolean) = navBarSettingsPrefs.updateBetaNavBarSizeControls(e); fun updateNavBarGlassLinkedToMain(e: Boolean) = navBarSettingsPrefs.updateNavBarGlassLinkedToMain(e)
    fun updateNavBarGlassBackdropStyle(s: String) = navBarSettingsPrefs.updateNavBarGlassBackdropStyle(s); fun updateNavBarGlassDynamic(e: Boolean) = navBarSettingsPrefs.updateNavBarGlassDynamic(e)
    fun updateNavBarGlassOpacityValue(v: Float, alias: String, isDark: Boolean) = navBarSettingsPrefs.updateNavBarGlassOpacityValue(v, alias, isDark)
    fun refreshNavBarGlassOpacity(alias: String, isDark: Boolean) = navBarSettingsPrefs.refreshNavBarGlassOpacity(alias, isDark)
    fun updateStreakNotificationTone(t: String) = streakManager.updateStreakNotificationTone(t); fun updateStreakReqTasks(c: Int) = streakManager.updateStreakReqTasks(c) { calculateTodayStreakProgress() }
    fun updateStreakReqAssignments(c: Int) = streakManager.updateStreakReqAssignments(c) { calculateTodayStreakProgress() }; fun updateStreakReqStudyMins(m: Int) = streakManager.updateStreakReqStudyMins(m) { calculateTodayStreakProgress() }
    fun updateStreakPartialThreshold(t: Float) = streakManager.updateStreakPartialThreshold(t) { calculateTodayStreakProgress() }; fun updateStreakProgressColor(c: String) = streakManager.updateStreakProgressColor(c)
    fun updateStreakBrightness(b: Float) = streakManager.updateStreakBrightness(b); fun updateStreakAnimationOverride(a: String) = streakManager.updateStreakAnimationOverride(a)
    fun updateSafetyPinEnabled(e: Boolean) = safetyPinManager.updateSafetyPinEnabled(e); fun updateSafetyPinConflictWarning(e: Boolean) = safetyPinManager.updateSafetyPinConflictWarning(e)
    fun updateSafetyPinRecommendations(e: Boolean) = safetyPinManager.updateSafetyPinRecommendations(e)

    fun generatePaletteFromPrimaryHex(hex: String) {}
    fun exportData(uri: Uri) {}; fun importData(uri: Uri) {}
}
