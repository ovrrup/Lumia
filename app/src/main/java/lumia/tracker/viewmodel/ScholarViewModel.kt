package lumia.tracker.viewmodel

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScholarViewModel(application: Application) : ScholarBaseViewModel(application) {

    // Flow Forwarding from Settings Prefs
    val betaFloatingNav = navBarSettingsPrefs.betaFloatingNav; val navBarHeight = navBarSettingsPrefs.navBarHeight
    val navBarPaddingHorizontal = navBarSettingsPrefs.navBarPaddingHorizontal; val navBarPaddingBottom = navBarSettingsPrefs.navBarPaddingBottom
    val navBarCornerRadius = navBarSettingsPrefs.navBarCornerRadius; val navBarLabelMode = navBarSettingsPrefs.navBarLabelMode
    val navBarGlassForceEnabled = navBarSettingsPrefs.navBarGlassForceEnabled; val navBarIndicatorAlpha = navBarSettingsPrefs.navBarIndicatorAlpha
    val betaNavBarSizeControls = navBarSettingsPrefs.betaNavBarSizeControls; val navBarGlassLinkedToMain = navBarSettingsPrefs.navBarGlassLinkedToMain
    val navBarGlassBackdropStyle = navBarSettingsPrefs.navBarGlassBackdropStyle; val navBarGlassDynamic = navBarSettingsPrefs.navBarGlassDynamic
    val navBarGlassOpacityValue = navBarSettingsPrefs.navBarGlassOpacityValue

    val featureSubjectEnabled = featureFlagsPrefs.featureSubjectEnabled; val featureSelfStudyEnabled = featureFlagsPrefs.featureSelfStudyEnabled
    val featureAnalyticsEnabled = featureFlagsPrefs.featureAnalyticsEnabled; val featureCalendarEnabled = featureFlagsPrefs.featureCalendarEnabled
    val featureQuickNotesEnabled = featureFlagsPrefs.featureQuickNotesEnabled; val betaNotes = featureFlagsPrefs.betaNotes

    val tabHomeLabel = tabConfigPrefs.tabHomeLabel; val tabHomeIcon = tabConfigPrefs.tabHomeIcon
    val tabCoursesLabel = tabConfigPrefs.tabCoursesLabel; val tabCoursesIcon = tabConfigPrefs.tabCoursesIcon
    val tabSubjectsLabel = tabConfigPrefs.tabSubjectsLabel; val tabSubjectsIcon = tabConfigPrefs.tabSubjectsIcon
    val tabSelfStudyLabel = tabConfigPrefs.tabSelfStudyLabel; val tabSelfStudyIcon = tabConfigPrefs.tabSelfStudyIcon
    val tabAnalyticsLabel = tabConfigPrefs.tabAnalyticsLabel; val tabAnalyticsIcon = tabConfigPrefs.tabAnalyticsIcon
    val tabCalendarLabel = tabConfigPrefs.tabCalendarLabel; val tabCalendarIcon = tabConfigPrefs.tabCalendarIcon

    val pomodoroWorkDuration = pomodoroDurationSettingsPrefs.pomodoroWorkDuration; val pomodoroShortBreakDuration = pomodoroDurationSettingsPrefs.pomodoroShortBreakDuration
    val pomodoroLongBreakDuration = pomodoroDurationSettingsPrefs.pomodoroLongBreakDuration; val pomodoroPeriodSessions = pomodoroDurationSettingsPrefs.pomodoroPeriodSessions
    val pomodoroEnablePeriodTarget = pomodoroDurationSettingsPrefs.pomodoroEnablePeriodTarget

    val notifFormalTone = notificationSettingsPrefs.notifFormalTone; val notifEnableDeadlines = notificationSettingsPrefs.notifEnableDeadlines
    val notifEnableClasses = notificationSettingsPrefs.notifEnableClasses; val notifEnableDailyDigest = notificationSettingsPrefs.notifEnableDailyDigest
    val soundEffectsEnabled = notificationSettingsPrefs.soundEffectsEnabled

    val aodTrueBlackOled = aodSettingsPrefs.aodTrueBlackOled; val aodAutoDeactivateTrueBlack = aodSettingsPrefs.aodAutoDeactivateTrueBlack
    val aodBurnInShiftSpeed = aodSettingsPrefs.aodBurnInShiftSpeed; val aodLockScreenSupport = aodSettingsPrefs.aodLockScreenSupport
    val aodTrueAodEnabled = aodSettingsPrefs.aodTrueAodEnabled; val aodTrueAodMode = aodSettingsPrefs.aodTrueAodMode
    val aodSensitivity = aodSettingsPrefs.aodSensitivity; val aodMotionSensitivity = aodSettingsPrefs.aodMotionSensitivity
    val aodDimnessLevel = aodSettingsPrefs.aodDimnessLevel; val aodLockTimeout = aodSettingsPrefs.aodLockTimeout

    val systemAutoLinkByName = systemBehaviorSettingsPrefs.systemAutoLinkByName; val systemEnableSynergy = systemBehaviorSettingsPrefs.systemEnableSynergy
    val systemAutoCreateSubject = systemBehaviorSettingsPrefs.systemAutoCreateSubject; val systemFuseSubjectsCourses = systemBehaviorSettingsPrefs.systemFuseSubjectsCourses
    val systemAdvancedTasks = systemBehaviorSettingsPrefs.systemAdvancedTasks; val systemPomodoroAutoLog = systemBehaviorSettingsPrefs.systemPomodoroAutoLog

    val glassBackdropStyle = glassMiscSettingsPrefs.glassBackdropStyle; val glassOpacityValue = glassMiscSettingsPrefs.glassOpacityValue
    val betaGlassDynamic = glassMiscSettingsPrefs.betaGlassDynamic; val betaFrostGlass = glassMiscSettingsPrefs.betaFrostGlass

    val streakTotalNormal = streakManager.streakTotalNormal; val streakTotalComplete = streakManager.streakTotalComplete
    val streakIsCompleteToday = streakManager.streakIsCompleteToday; val streakPercentage = streakManager.streakPercentage
    val streakCurrent = streakManager.streakCurrent; val streakLongest = streakManager.streakLongest
    val streakRequirementTasks = streakManager.streakRequirementTasks; val streakRequirementAssignments = streakManager.streakRequirementAssignments
    val streakRequirementStudyMins = streakManager.streakRequirementStudyMins; val streakPartialThreshold = streakManager.streakPartialThreshold
    val streakProgressColor = streakManager.streakProgressColor; val streakBrightness = streakManager.streakBrightness
    val streakAnimationOverride = streakManager.streakAnimationOverride; val streakNotificationTone = streakManager.streakNotificationTone

    val safetyPinEnabled = safetyPinManager.safetyPinEnabled; val safetyPinConflictWarning = safetyPinManager.safetyPinConflictWarning
    val safetyPinRecommendations = safetyPinManager.safetyPinRecommendations; val safetyPinDialogData = safetyPinManager.safetyPinDialogData

    val dbStatistics = backupRestoreManager.dbStatistics; val defragStatus = backupRestoreManager.defragStatus; val importExportStatus = backupRestoreManager.importExportStatus

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

    override fun updateThemeMode(mode: String) { _themeMode.value = mode; super.updateThemeMode(mode) }
    override fun updateThemeColor(color: String) { _themeColor.value = color; super.updateThemeColor(color) }
    override fun updatePureBlackMode(e: Boolean) { _pureBlackMode.value = e; super.updatePureBlackMode(e) }
    override fun updateBetaGlassUi(e: Boolean) { _betaGlassUi.value = e; super.updateBetaGlassUi(e) }
    override fun updateBetaDynamicBackground(e: Boolean) { _betaDynamicBackground.value = e; super.updateBetaDynamicBackground(e) }
    override fun updateBetaEnhancedHeader(e: Boolean) { _betaEnhancedHeader.value = e; super.updateBetaEnhancedHeader(e) }
    fun updateCustomColor(type: String, hex: String) {
        when (type) {
            "primary" -> { _customPrimary.value = hex; prefs.edit().putString("custom_primary", hex).apply() }
            "container" -> { _customPrimaryContainer.value = hex; prefs.edit().putString("custom_primary_container", hex).apply() }
            "background" -> { _customBackground.value = hex; prefs.edit().putString("custom_background", hex).apply() }
            "surface" -> { _customSurface.value = hex; prefs.edit().putString("custom_surface", hex).apply() }
            "text" -> { _customText.value = hex; prefs.edit().putString("custom_text", hex).apply() }
        }
    }
}
