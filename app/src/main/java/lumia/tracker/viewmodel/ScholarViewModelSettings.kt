package lumia.tracker.viewmodel

// Settings extension methods for ScholarViewModel
fun ScholarViewModel.updateFeatureSubjectEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureSubjectEnabled(enabled)
fun ScholarViewModel.updateFeatureSelfStudyEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureSelfStudyEnabled(enabled)
fun ScholarViewModel.updateFeatureAnalyticsEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureAnalyticsEnabled(enabled)
fun ScholarViewModel.updateFeatureCalendarEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureCalendarEnabled(enabled)
fun ScholarViewModel.updateFeatureQuickNotesEnabled(enabled: Boolean) = featureFlagsPrefs.updateFeatureQuickNotesEnabled(enabled)
fun ScholarViewModel.updateBetaNotes(enabled: Boolean) = featureFlagsPrefs.updateBetaNotes(enabled)

fun ScholarViewModel.updateTabHomeLabel(value: String) = tabConfigPrefs.updateTabHomeLabel(value)
fun ScholarViewModel.updateTabHomeIcon(value: String) = tabConfigPrefs.updateTabHomeIcon(value)
fun ScholarViewModel.updateTabCoursesLabel(value: String) = tabConfigPrefs.updateTabCoursesLabel(value)
fun ScholarViewModel.updateTabCoursesIcon(value: String) = tabConfigPrefs.updateTabCoursesIcon(value)
fun ScholarViewModel.updateTabSubjectsLabel(value: String) = tabConfigPrefs.updateTabSubjectsLabel(value)
fun ScholarViewModel.updateTabSubjectsIcon(value: String) = tabConfigPrefs.updateTabSubjectsIcon(value)
fun ScholarViewModel.updateTabSelfStudyLabel(value: String) = tabConfigPrefs.updateTabSelfStudyLabel(value)
fun ScholarViewModel.updateTabSelfStudyIcon(value: String) = tabConfigPrefs.updateTabSelfStudyIcon(value)
fun ScholarViewModel.updateTabAnalyticsLabel(value: String) = tabConfigPrefs.updateTabAnalyticsLabel(value)
fun ScholarViewModel.updateTabAnalyticsIcon(value: String) = tabConfigPrefs.updateTabAnalyticsIcon(value)
fun ScholarViewModel.updateTabCalendarLabel(value: String) = tabConfigPrefs.updateTabCalendarLabel(value)
fun ScholarViewModel.updateTabCalendarIcon(value: String) = tabConfigPrefs.updateTabCalendarIcon(value)

fun ScholarViewModel.updatePomodoroWorkDuration(duration: Int) = pomodoroDurationSettingsPrefs.updatePomodoroWorkDuration(duration)
fun ScholarViewModel.updatePomodoroShortBreakDuration(duration: Int) = pomodoroDurationSettingsPrefs.updatePomodoroShortBreakDuration(duration)
fun ScholarViewModel.updatePomodoroLongBreakDuration(duration: Int) = pomodoroDurationSettingsPrefs.updatePomodoroLongBreakDuration(duration)
fun ScholarViewModel.updatePomodoroPeriodSessions(sessions: Int) = pomodoroDurationSettingsPrefs.updatePomodoroPeriodSessions(sessions)
fun ScholarViewModel.updatePomodoroEnablePeriodTarget(enabled: Boolean) = pomodoroDurationSettingsPrefs.updatePomodoroEnablePeriodTarget(enabled)

fun ScholarViewModel.updateNotifFormalTone(enabled: Boolean) = notificationSettingsPrefs.updateNotifFormalTone(enabled)
fun ScholarViewModel.updateNotifEnableDeadlines(enabled: Boolean) = notificationSettingsPrefs.updateNotifEnableDeadlines(enabled)
fun ScholarViewModel.updateNotifEnableClasses(enabled: Boolean) = notificationSettingsPrefs.updateNotifEnableClasses(enabled)
fun ScholarViewModel.updateNotifEnableDailyDigest(enabled: Boolean) = notificationSettingsPrefs.updateNotifEnableDailyDigest(enabled)
fun ScholarViewModel.updateSoundEffectsEnabled(enabled: Boolean) = notificationSettingsPrefs.updateSoundEffectsEnabled(enabled)

fun ScholarViewModel.updateAodLockScreenSupport(enabled: Boolean) = aodSettingsPrefs.updateAodLockScreenSupport(enabled)
fun ScholarViewModel.updateAodTrueAodEnabled(enabled: Boolean) = aodSettingsPrefs.updateAodTrueAodEnabled(enabled)
fun ScholarViewModel.updateAodTrueAodMode(mode: String) = aodSettingsPrefs.updateAodTrueAodMode(mode)
fun ScholarViewModel.updateAodSensitivity(sensitivity: String) = aodSettingsPrefs.updateAodSensitivity(sensitivity)
fun ScholarViewModel.updateAodMotionSensitivity(sensitivity: Float) = aodSettingsPrefs.updateAodMotionSensitivity(sensitivity)
fun ScholarViewModel.updateAodDimnessLevel(level: Float) = aodSettingsPrefs.updateAodDimnessLevel(level)
fun ScholarViewModel.updateAodLockTimeout(seconds: Int) = aodSettingsPrefs.updateAodLockTimeout(seconds)
fun ScholarViewModel.updateAodAutoDeactivateTrueBlack(enabled: Boolean) = aodSettingsPrefs.updateAodAutoDeactivateTrueBlack(enabled)
fun ScholarViewModel.updateAodBurnInShiftSpeed(speed: Int) = aodSettingsPrefs.updateAodBurnInShiftSpeed(speed)

fun ScholarViewModel.updateSystemAutoLinkByName(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemAutoLinkByName(enabled)
fun ScholarViewModel.updateSystemEnableSynergy(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemEnableSynergy(enabled)
fun ScholarViewModel.updateSystemAutoCreateSubject(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemAutoCreateSubject(enabled)
fun ScholarViewModel.updateSystemFuseSubjectsCourses(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemFuseSubjectsCourses(enabled)
fun ScholarViewModel.updateSystemAdvancedTasks(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemAdvancedTasks(enabled)
fun ScholarViewModel.updateSystemPomodoroAutoLog(enabled: Boolean) = systemBehaviorSettingsPrefs.updateSystemPomodoroAutoLog(enabled)

fun ScholarViewModel.updateGlassBackdropStyle(style: String) = glassMiscSettingsPrefs.updateGlassBackdropStyle(style)
fun ScholarViewModel.updateGlassOpacityValue(value: Float) = glassMiscSettingsPrefs.updateGlassOpacityValue(value)
fun ScholarViewModel.updateBetaGlassDynamic(enabled: Boolean) = glassMiscSettingsPrefs.updateBetaGlassDynamic(enabled)
fun ScholarViewModel.updateBetaFrostGlass(enabled: Boolean) = glassMiscSettingsPrefs.updateBetaFrostGlass(enabled)

fun ScholarViewModel.updateBetaFloatingNav(enabled: Boolean) = navBarSettingsPrefs.updateBetaFloatingNav(enabled)
fun ScholarViewModel.updateNavBarHeight(height: Float) = navBarSettingsPrefs.updateNavBarHeight(height)
fun ScholarViewModel.updateNavBarPaddingHorizontal(padding: Float) = navBarSettingsPrefs.updateNavBarPaddingHorizontal(padding)
fun ScholarViewModel.updateNavBarPaddingBottom(padding: Float) = navBarSettingsPrefs.updateNavBarPaddingBottom(padding)
fun ScholarViewModel.updateNavBarCornerRadius(radius: Float) = navBarSettingsPrefs.updateNavBarCornerRadius(radius)
fun ScholarViewModel.updateNavBarLabelMode(mode: String) = navBarSettingsPrefs.updateNavBarLabelMode(mode)
fun ScholarViewModel.updateNavBarGlassForceEnabled(enabled: Boolean) = navBarSettingsPrefs.updateNavBarGlassForceEnabled(enabled)
fun ScholarViewModel.updateNavBarIndicatorAlpha(alpha: Float) = navBarSettingsPrefs.updateNavBarIndicatorAlpha(alpha)
fun ScholarViewModel.updateBetaNavBarSizeControls(enabled: Boolean) = navBarSettingsPrefs.updateBetaNavBarSizeControls(enabled)
fun ScholarViewModel.updateNavBarGlassLinkedToMain(enabled: Boolean) = navBarSettingsPrefs.updateNavBarGlassLinkedToMain(enabled)
fun ScholarViewModel.updateNavBarGlassBackdropStyle(style: String) = navBarSettingsPrefs.updateNavBarGlassBackdropStyle(style)
fun ScholarViewModel.updateNavBarGlassDynamic(enabled: Boolean) = navBarSettingsPrefs.updateNavBarGlassDynamic(enabled)
fun ScholarViewModel.updateNavBarGlassOpacityValue(value: Float, alias: String, isDark: Boolean) = navBarSettingsPrefs.updateNavBarGlassOpacityValue(value, alias, isDark)
fun ScholarViewModel.refreshNavBarGlassOpacity(alias: String, isDark: Boolean) = navBarSettingsPrefs.refreshNavBarGlassOpacity(alias, isDark)

fun ScholarViewModel.updateStreakNotificationTone(tone: String) = streakManager.updateStreakNotificationTone(tone)
fun ScholarViewModel.updateStreakReqTasks(count: Int) = streakManager.updateStreakReqTasks(count) { calculateTodayStreakProgress() }
fun ScholarViewModel.updateStreakReqAssignments(count: Int) = streakManager.updateStreakReqAssignments(count) { calculateTodayStreakProgress() }
fun ScholarViewModel.updateStreakReqStudyMins(mins: Int) = streakManager.updateStreakReqStudyMins(mins) { calculateTodayStreakProgress() }
fun ScholarViewModel.updateStreakPartialThreshold(thresh: Float) = streakManager.updateStreakPartialThreshold(thresh) { calculateTodayStreakProgress() }
fun ScholarViewModel.updateStreakProgressColor(colorHex: String) = streakManager.updateStreakProgressColor(colorHex)
fun ScholarViewModel.updateStreakBrightness(brightness: Float) = streakManager.updateStreakBrightness(brightness)
fun ScholarViewModel.updateStreakAnimationOverride(anim: String) = streakManager.updateStreakAnimationOverride(anim)

fun ScholarViewModel.updateSafetyPinEnabled(enabled: Boolean) = safetyPinManager.updateSafetyPinEnabled(enabled)
fun ScholarViewModel.updateSafetyPinConflictWarning(enabled: Boolean) = safetyPinManager.updateSafetyPinConflictWarning(enabled)
fun ScholarViewModel.updateSafetyPinRecommendations(enabled: Boolean) = safetyPinManager.updateSafetyPinRecommendations(enabled)
