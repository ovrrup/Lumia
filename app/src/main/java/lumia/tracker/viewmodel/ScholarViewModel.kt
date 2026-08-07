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

class ScholarViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ScholarRepository(AppDatabase.getDatabase(application).scholarDao())
    val crud = ScholarDataCrud(repository)
    val accountManager = ScholarProfileAccountManager(application, lumia.tracker.data.ProfileManager(application))
    val profileManager = accountManager.profileManager
    val prefs = profileManager.getProfilePrefs()
    val safetyPinManager = ScholarSafetyPinManager(prefs)
    val backupRestoreManager = ScholarBackupRestoreManager(application, repository)

    // Data Flows forwarded to UI
    val courses = crud.courses
    val subjects = crud.subjects
    val assignments = crud.assignments
    val tasks = crud.tasks
    val notes = crud.notes
    val attendanceRecords = crud.attendanceRecords
    val pomodoroSessions = crud.pomodoroSessions
    val attachments = crud.attachments
    val testRecords = crud.testRecords
    val chapters = crud.chapters
    val topics = crud.topics
    val flashcards = crud.flashcards
    val tagCustomizations = crud.tagCustomizations
    val actionLogs = crud.actionLogs

    val streakManager = ScholarStreakManager(repository, prefs) { channelId, notifId, title, text, iconRes, color, openScreen ->
        lumia.tracker.util.StreakNotifications.sendInstantNotification(getApplication(), channelId, notifId, title, text, iconRes, color, openScreen)
    }

    val activeProfile = accountManager.activeProfile
    val allProfiles = accountManager.allProfiles
    val isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false)).asStateFlow()

    fun completeOnboarding() { prefs.edit().putBoolean("onboarding_completed", true).apply() }

    private val _selectedDashboardTab = MutableStateFlow(0)
    val selectedDashboardTab = _selectedDashboardTab.asStateFlow()
    fun setSelectedDashboardTab(tab: Int) { _selectedDashboardTab.value = tab }

    private val _systemBarVisible = MutableStateFlow(true)
    val systemBarVisible = _systemBarVisible.asStateFlow()
    fun setSystemBarVisible(visible: Boolean) { _systemBarVisible.value = visible }

    // Settings Pref Helpers
    val navBarSettingsPrefs = NavBarSettingsPrefs(prefs)
    val featureFlagsPrefs = FeatureFlagsPrefs(prefs)
    val tabConfigPrefs = TabConfigPrefs(prefs)
    val notificationSettingsPrefs = NotificationSettingsPrefs(prefs)
    val pomodoroDurationSettingsPrefs = PomodoroDurationSettingsPrefs(prefs)
    val aodSettingsPrefs = AodSettingsPrefs(prefs)
    val systemBehaviorSettingsPrefs = SystemBehaviorSettingsPrefs(prefs)
    val glassMiscSettingsPrefs = GlassMiscSettingsPrefs(prefs)

    // Dynamic Bg Brightness & Custom Colors
    private val _dynamicBgLightBrightness = MutableStateFlow(prefs.getFloat("dynamic_bg_light_brightness", 0.15f))
    val dynamicBgLightBrightness = _dynamicBgLightBrightness.asStateFlow()

    private val _dynamicBgDarkBrightness = MutableStateFlow(prefs.getFloat("dynamic_bg_dark_brightness", 0.25f))
    val dynamicBgDarkBrightness = _dynamicBgDarkBrightness.asStateFlow()

    private val _customPrimaryContainer = MutableStateFlow(prefs.getString("custom_primary_container", "#DAF1FF") ?: "#DAF1FF")
    val customPrimaryContainer = _customPrimaryContainer.asStateFlow()

    private val _customBackground = MutableStateFlow(prefs.getString("custom_background", "#FAFAFA") ?: "#FAFAFA")
    val customBackground = _customBackground.asStateFlow()

    private val _customSurface = MutableStateFlow(prefs.getString("custom_surface", "#FFFFFF") ?: "#FFFFFF")
    val customSurface = _customSurface.asStateFlow()

    private val _customText = MutableStateFlow(prefs.getString("custom_text", "#1A1C1A") ?: "#1A1C1A")
    val customText = _customText.asStateFlow()

    private val _moreRoundsMode = MutableStateFlow(prefs.getString("more_rounds_mode", "Pill") ?: "Pill")
    val moreRoundsMode = _moreRoundsMode.asStateFlow()

    private val _betaBetterTexts = MutableStateFlow(prefs.getBoolean("beta_better_texts", false))
    val betaBetterTexts = _betaBetterTexts.asStateFlow()

    private val _betaBetterTextsPalette = MutableStateFlow(prefs.getBoolean("beta_better_texts_palette", false))
    val betaBetterTextsPalette = _betaBetterTextsPalette.asStateFlow()

    // Flow Forwarding from Settings Prefs
    val betaFloatingNav = navBarSettingsPrefs.betaFloatingNav
    val navBarHeight = navBarSettingsPrefs.navBarHeight
    val navBarPaddingHorizontal = navBarSettingsPrefs.navBarPaddingHorizontal
    val navBarPaddingBottom = navBarSettingsPrefs.navBarPaddingBottom
    val navBarCornerRadius = navBarSettingsPrefs.navBarCornerRadius
    val navBarLabelMode = navBarSettingsPrefs.navBarLabelMode
    val navBarGlassForceEnabled = navBarSettingsPrefs.navBarGlassForceEnabled
    val navBarIndicatorAlpha = navBarSettingsPrefs.navBarIndicatorAlpha
    val betaNavBarSizeControls = navBarSettingsPrefs.betaNavBarSizeControls
    val navBarGlassLinkedToMain = navBarSettingsPrefs.navBarGlassLinkedToMain
    val navBarGlassBackdropStyle = navBarSettingsPrefs.navBarGlassBackdropStyle
    val navBarGlassDynamic = navBarSettingsPrefs.navBarGlassDynamic
    val navBarGlassOpacityValue = navBarSettingsPrefs.navBarGlassOpacityValue

    val featureSubjectEnabled = featureFlagsPrefs.featureSubjectEnabled
    val featureSelfStudyEnabled = featureFlagsPrefs.featureSelfStudyEnabled
    val featureAnalyticsEnabled = featureFlagsPrefs.featureAnalyticsEnabled
    val featureCalendarEnabled = featureFlagsPrefs.featureCalendarEnabled
    val featureQuickNotesEnabled = featureFlagsPrefs.featureQuickNotesEnabled
    val betaNotes = featureFlagsPrefs.betaNotes

    val tabHomeLabel = tabConfigPrefs.tabHomeLabel
    val tabHomeIcon = tabConfigPrefs.tabHomeIcon
    val tabCoursesLabel = tabConfigPrefs.tabCoursesLabel
    val tabCoursesIcon = tabConfigPrefs.tabCoursesIcon
    val tabSubjectsLabel = tabConfigPrefs.tabSubjectsLabel
    val tabSubjectsIcon = tabConfigPrefs.tabSubjectsIcon
    val tabSelfStudyLabel = tabConfigPrefs.tabSelfStudyLabel
    val tabSelfStudyIcon = tabConfigPrefs.tabSelfStudyIcon
    val tabAnalyticsLabel = tabConfigPrefs.tabAnalyticsLabel
    val tabAnalyticsIcon = tabConfigPrefs.tabAnalyticsIcon
    val tabCalendarLabel = tabConfigPrefs.tabCalendarLabel
    val tabCalendarIcon = tabConfigPrefs.tabCalendarIcon

    val pomodoroWorkDuration = pomodoroDurationSettingsPrefs.pomodoroWorkDuration
    val pomodoroShortBreakDuration = pomodoroDurationSettingsPrefs.pomodoroShortBreakDuration
    val pomodoroLongBreakDuration = pomodoroDurationSettingsPrefs.pomodoroLongBreakDuration
    val pomodoroPeriodSessions = pomodoroDurationSettingsPrefs.pomodoroPeriodSessions
    val pomodoroEnablePeriodTarget = pomodoroDurationSettingsPrefs.pomodoroEnablePeriodTarget

    val notifFormalTone = notificationSettingsPrefs.notifFormalTone
    val notifEnableDeadlines = notificationSettingsPrefs.notifEnableDeadlines
    val notifEnableClasses = notificationSettingsPrefs.notifEnableClasses
    val notifEnableDailyDigest = notificationSettingsPrefs.notifEnableDailyDigest
    val soundEffectsEnabled = notificationSettingsPrefs.soundEffectsEnabled

    val aodTrueBlackOled = aodSettingsPrefs.aodTrueBlackOled
    val aodAutoDeactivateTrueBlack = aodSettingsPrefs.aodAutoDeactivateTrueBlack
    val aodBurnInShiftSpeed = aodSettingsPrefs.aodBurnInShiftSpeed
    val aodLockScreenSupport = aodSettingsPrefs.aodLockScreenSupport
    val aodTrueAodEnabled = aodSettingsPrefs.aodTrueAodEnabled
    val aodTrueAodMode = aodSettingsPrefs.aodTrueAodMode
    val aodSensitivity = aodSettingsPrefs.aodSensitivity
    val aodMotionSensitivity = aodSettingsPrefs.aodMotionSensitivity
    val aodDimnessLevel = aodSettingsPrefs.aodDimnessLevel
    val aodLockTimeout = aodSettingsPrefs.aodLockTimeout

    val systemAutoLinkByName = systemBehaviorSettingsPrefs.systemAutoLinkByName
    val systemEnableSynergy = systemBehaviorSettingsPrefs.systemEnableSynergy
    val systemAutoCreateSubject = systemBehaviorSettingsPrefs.systemAutoCreateSubject
    val systemFuseSubjectsCourses = systemBehaviorSettingsPrefs.systemFuseSubjectsCourses
    val systemAdvancedTasks = systemBehaviorSettingsPrefs.systemAdvancedTasks
    val systemPomodoroAutoLog = systemBehaviorSettingsPrefs.systemPomodoroAutoLog

    val glassBackdropStyle = glassMiscSettingsPrefs.glassBackdropStyle
    val glassOpacityValue = glassMiscSettingsPrefs.glassOpacityValue
    val betaGlassDynamic = glassMiscSettingsPrefs.betaGlassDynamic
    val betaFrostGlass = glassMiscSettingsPrefs.betaFrostGlass

    val streakTotalNormal = streakManager.streakTotalNormal
    val streakTotalComplete = streakManager.streakTotalComplete
    val streakIsCompleteToday = streakManager.streakIsCompleteToday
    val streakPercentage = streakManager.streakPercentage
    val streakCurrent = streakManager.streakCurrent
    val streakLongest = streakManager.streakLongest
    val streakRequirementTasks = streakManager.streakRequirementTasks
    val streakRequirementAssignments = streakManager.streakRequirementAssignments
    val streakRequirementStudyMins = streakManager.streakRequirementStudyMins
    val streakPartialThreshold = streakManager.streakPartialThreshold
    val streakProgressColor = streakManager.streakProgressColor
    val streakBrightness = streakManager.streakBrightness
    val streakAnimationOverride = streakManager.streakAnimationOverride
    val streakNotificationTone = streakManager.streakNotificationTone

    val safetyPinEnabled = safetyPinManager.safetyPinEnabled
    val safetyPinConflictWarning = safetyPinManager.safetyPinConflictWarning
    val safetyPinRecommendations = safetyPinManager.safetyPinRecommendations
    val safetyPinDialogData = safetyPinManager.safetyPinDialogData

    val dbStatistics = backupRestoreManager.dbStatistics
    val defragStatus = backupRestoreManager.defragStatus
    val importExportStatus = backupRestoreManager.importExportStatus

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    val themeMode = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "Ocean") ?: "Ocean")
    val themeColor = _themeColor.asStateFlow()

    private val _customPrimary = MutableStateFlow(prefs.getString("custom_primary", "#3197D6") ?: "#3197D6")
    val customPrimary = _customPrimary.asStateFlow()

    private val _pureBlackMode = MutableStateFlow(prefs.getBoolean("pure_black_mode", false))
    val pureBlackMode = _pureBlackMode.asStateFlow()

    private val _betaGlassUi = MutableStateFlow(prefs.getBoolean("beta_glass_ui", false))
    val betaGlassUi = _betaGlassUi.asStateFlow()

    private val _betaDynamicBackground = MutableStateFlow(prefs.getBoolean("beta_dynamic_background", false))
    val betaDynamicBackground = _betaDynamicBackground.asStateFlow()

    private val _betaEnhancedHeader = MutableStateFlow(prefs.getBoolean("beta_enhanced_header", true))
    val betaEnhancedHeader = _betaEnhancedHeader.asStateFlow()

    private val _appAnimationMode = MutableStateFlow(prefs.getString("app_animation_mode", "Normal") ?: "Normal")
    val appAnimationMode = _appAnimationMode.asStateFlow()

    private val _moreRounds = MutableStateFlow(prefs.getBoolean("more_rounds", false))
    val moreRounds = _moreRounds.asStateFlow()

    private val _displayLayoutMode = MutableStateFlow(prefs.getString("display_layout_mode", "Normal") ?: "Normal")
    val displayLayoutMode = _displayLayoutMode.asStateFlow()

    private val _betaMinimalistMode = MutableStateFlow(prefs.getBoolean("beta_minimalist_mode", false))
    val betaMinimalistMode = _betaMinimalistMode.asStateFlow()

    init { calculateTodayStreakProgress() }

    fun calculateTodayStreakProgress() { viewModelScope.launch(Dispatchers.IO) { streakManager.calculateTodayStreakProgress() } }
    fun loadDBStatistics() { viewModelScope.launch { backupRestoreManager.loadDBStatistics() } }
    fun defragmentDatabase() { viewModelScope.launch { backupRestoreManager.defragmentDatabase() } }

    fun clearStatus() { backupRestoreManager.clearStatus() }
    fun clearImportExportStatus() { backupRestoreManager.clearStatus() }

    fun switchProfileAndRestart(context: Context, id: String) = accountManager.switchProfileAndRestart(context, id)
    fun createProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String = accountManager.createProfile(name, avatar, alias, starterTheme)
    fun setupFirstProfile(name: String, avatar: String, alias: String, starterTheme: String) = accountManager.setupFirstProfile(name, avatar, alias, starterTheme) { updateThemeColor(it) }
    fun updateProfile(name: String, avatar: String, alias: String = "") = accountManager.updateProfile(name, avatar, alias)
    fun deleteProfile(context: Context, id: String) = accountManager.deleteProfile(context, id)

    // CRUD Helper Methods & Overloads
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

    fun generatePaletteFromPrimaryHex(hex: String) {}

    fun updateThemeMode(mode: String) { _themeMode.value = mode; prefs.edit().putString("theme_mode", mode).apply() }
    fun updateThemeColor(color: String) { _themeColor.value = color; prefs.edit().putString("theme_color", color).apply() }
    fun updatePureBlackMode(enabled: Boolean) { _pureBlackMode.value = enabled; prefs.edit().putBoolean("pure_black_mode", enabled).apply() }
    fun updateBetaGlassUi(enabled: Boolean) { _betaGlassUi.value = enabled; prefs.edit().putBoolean("beta_glass_ui", enabled).apply() }
    fun updateBetaDynamicBackground(enabled: Boolean) { _betaDynamicBackground.value = enabled; prefs.edit().putBoolean("beta_dynamic_background", enabled).apply() }
    fun updateBetaEnhancedHeader(enabled: Boolean) { _betaEnhancedHeader.value = enabled; prefs.edit().putBoolean("beta_enhanced_header", enabled).apply() }

    fun updateCustomColor(type: String, hex: String) {
        when (type) {
            "primary" -> { _customPrimary.value = hex; prefs.edit().putString("custom_primary", hex).apply() }
            "container" -> { _customPrimaryContainer.value = hex; prefs.edit().putString("custom_primary_container", hex).apply() }
            "background" -> { _customBackground.value = hex; prefs.edit().putString("custom_background", hex).apply() }
            "surface" -> { _customSurface.value = hex; prefs.edit().putString("custom_surface", hex).apply() }
            "text" -> { _customText.value = hex; prefs.edit().putString("custom_text", hex).apply() }
        }
    }

    fun exportData(uri: Uri) {}
    fun importData(uri: Uri) {}
}
