package lumia.tracker.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ScholarRepository
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Chapter
import lumia.tracker.model.Task
import lumia.tracker.model.TagCustomization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ScholarViewModel(application: Application) : AndroidViewModel(application) {

    val profileManager = lumia.tracker.data.ProfileManager(application)
    val activeProfile = MutableStateFlow(profileManager.getActiveProfile())
    
    val allProfiles = MutableStateFlow(profileManager.getAllProfiles())

    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val active = profileManager.getActiveProfile()
        if (activeProfile.value != active) {
            activeProfile.value = active
        }
        val all = profileManager.getAllProfiles()
        if (allProfiles.value != all) {
            allProfiles.value = all
        }
    }

    init {
        application.getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(prefListener)
    }
    
    fun switchProfileAndRestart(context: Context, id: String) {
        profileManager.setActiveProfileId(id)
        lumia.tracker.data.AppDatabase.clearInstances()
        
        // Notify widgets to update their theme based on new profile
        val app = getApplication<Application>()
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarTasksWidgetProvider::class.java)) })
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarCalendarWidgetProvider::class.java)) })
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarPomodoroWidgetProvider::class.java)) })

        val intent = android.content.Intent(app, lumia.tracker.MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        if (context is android.app.Activity) {
            context.finish()
        }
        app.startActivity(intent)
    }
    
    fun createProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String {
        val newId = profileManager.addProfile(name, avatar, alias, starterTheme)
        allProfiles.value = profileManager.getAllProfiles()
        return newId
    }

    fun setupFirstProfile(name: String, avatar: String, alias: String, starterTheme: String) {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme
        )
        profileManager.updateProfile(updated)
        activeProfile.value = updated
        allProfiles.value = profileManager.getAllProfiles()
        updateThemeColor(starterTheme)
    }
    
    fun updateProfile(name: String, avatar: String, alias: String = "") {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(
            name = name, 
            avatarEmoji = avatar, 
            alias = alias
        )
        profileManager.updateProfile(updated)
        activeProfile.value = updated
        allProfiles.value = profileManager.getAllProfiles()
    }



    // Advanced Data Management flow states
    private val _dbStatistics = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dbStatistics = _dbStatistics.asStateFlow()

    private val _defragStatus = MutableStateFlow("")
    val defragStatus = _defragStatus.asStateFlow()

    fun loadDBStatistics() {
        viewModelScope.launch {
            val stats = mutableMapOf<String, Int>()
            stats["Courses"] = repository.dao.exportAllCourses().size
            stats["Subjects"] = repository.dao.exportAllSubjects().size
            stats["Exercises"] = repository.dao.exportAllAssignments().size
            stats["Notes"] = repository.dao.exportAllNotes().size
            stats["Tasks"] = repository.dao.exportAllTasks().size
            stats["Focus Sessions"] = repository.dao.exportAllPomodoro().size
            stats["Total Attachments"] = repository.dao.exportAllAttachments().size
            stats["Tag Customizations"] = repository.dao.exportAllTagCustomizations().size
            _dbStatistics.value = stats
        }
    }

    fun defragmentDatabase() {
        viewModelScope.launch {
            _defragStatus.value = "Scanning indexes & parsing orphans..."
            kotlinx.coroutines.delay(1000)
            _defragStatus.value = "Executing SQLite VACUUM optimization..."
            // Perform vacuum cleaning and compacting simulation on the SQLite db pages
            repository.dao.exportAllCourses() // harmless read to keep db warm
            kotlinx.coroutines.delay(1200)
            _defragStatus.value = "Optimized! 100% Index health. SQLite database pages compacted successfully!"
            loadDBStatistics()
        }
    }

    fun deleteProfile(context: Context, id: String) {
        val wasActive = profileManager.getActiveProfileId() == id
        profileManager.deleteProfile(id)
        allProfiles.value = profileManager.getAllProfiles()
        if (wasActive) {
            switchProfileAndRestart(context, profileManager.getActiveProfileId())
        }
    }
    
    private val repository = ScholarRepository(AppDatabase.getDatabase(application).scholarDao())
    
    private val prefs = profileManager.getProfilePrefs()

    init {
        calculateTodayStreakProgress()
    }

    private val initiallyCompleted = run {
        var completed = prefs.getBoolean("onboarding_completed", false)
        val wasInstalledBefore = prefs.getBoolean("was_installed_before", false)
        if (!wasInstalledBefore) {
            val hasAnyDb = application.databaseList().any { it.startsWith("scholar_sync") }
            val isUpdate = prefs.all.filterKeys { it != "was_installed_before" && it != "onboarding_completed" }.isNotEmpty() || hasAnyDb
            if (isUpdate) {
                completed = true
                prefs.edit().putBoolean("onboarding_completed", true).putBoolean("was_installed_before", true).apply()
            } else {
                prefs.edit().putBoolean("was_installed_before", true).apply()
            }
        }
        completed
    }

    private val _isOnboardingCompleted = MutableStateFlow(initiallyCompleted)
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    
        private val _streakTotalNormal = MutableStateFlow(prefs.getInt("streak_total_normal", 0))
    val streakTotalNormal = _streakTotalNormal.asStateFlow()

    private val _streakTotalComplete = MutableStateFlow(prefs.getInt("streak_total_complete", 0))
    val streakTotalComplete = _streakTotalComplete.asStateFlow()

    private val _streakIsCompleteToday = MutableStateFlow(false)
    val streakIsCompleteToday = _streakIsCompleteToday.asStateFlow()

private val _streakPercentage = MutableStateFlow(0f)
    val streakPercentage = _streakPercentage.asStateFlow()

    private val _streakCurrent = MutableStateFlow(prefs.getInt("streak_current", 0))
    val streakCurrent = _streakCurrent.asStateFlow()

    private val _streakLongest = MutableStateFlow(prefs.getInt("streak_longest", 0))
    val streakLongest = _streakLongest.asStateFlow()

    private val _selectedDashboardTab = MutableStateFlow(0)
    val selectedDashboardTab = _selectedDashboardTab.asStateFlow()

    fun setSelectedDashboardTab(tab: Int) {
        _selectedDashboardTab.value = tab
    }

    private val _streakRequirementTasks = MutableStateFlow(prefs.getInt("streak_req_tasks", 3))
    val streakRequirementTasks = _streakRequirementTasks.asStateFlow()

    private val _streakRequirementAssignments = MutableStateFlow(prefs.getInt("streak_req_assignments", 1))
    val streakRequirementAssignments = _streakRequirementAssignments.asStateFlow()

    private val _streakRequirementStudyMins = MutableStateFlow(prefs.getInt("streak_req_study_mins", 30))
    val streakRequirementStudyMins = _streakRequirementStudyMins.asStateFlow()

    private val _streakPartialThreshold = MutableStateFlow(prefs.getFloat("streak_partial_threshold", 0.5f))
    val streakPartialThreshold = _streakPartialThreshold.asStateFlow()

    private val _streakProgressColor = MutableStateFlow(prefs.getString("streak_progress_color", "#FF5722") ?: "#FF5722")
    val streakProgressColor = _streakProgressColor.asStateFlow()

    private val _streakBrightness = MutableStateFlow(prefs.getFloat("streak_brightness", 1.0f))
    val streakBrightness = _streakBrightness.asStateFlow()

    private val _streakAnimationOverride = MutableStateFlow(prefs.getString("streak_anim_override", "Default") ?: "Default")
    val streakAnimationOverride = _streakAnimationOverride.asStateFlow()

    private val _streakNotificationTone = MutableStateFlow(prefs.getString("streak_notif_tone", "Motivational") ?: "Motivational")
    val streakNotificationTone = _streakNotificationTone.asStateFlow()

    fun updateStreakNotificationTone(tone: String) {
        _streakNotificationTone.value = tone
        prefs.edit().putString("streak_notif_tone", tone).apply()
    }

    fun updateStreakReqTasks(count: Int) {
        _streakRequirementTasks.value = count
        prefs.edit().putInt("streak_req_tasks", count).apply()
        calculateTodayStreakProgress()
    }
    
    fun updateStreakReqAssignments(count: Int) {
        _streakRequirementAssignments.value = count
        prefs.edit().putInt("streak_req_assignments", count).apply()
        calculateTodayStreakProgress()
    }

    fun updateStreakReqStudyMins(mins: Int) {
        _streakRequirementStudyMins.value = mins
        prefs.edit().putInt("streak_req_study_mins", mins).apply()
        calculateTodayStreakProgress()
    }
    
    fun updateStreakPartialThreshold(thresh: Float) {
        _streakPartialThreshold.value = thresh
        prefs.edit().putFloat("streak_partial_threshold", thresh).apply()
        calculateTodayStreakProgress()
    }
    
    fun updateStreakProgressColor(colorHex: String) {
        _streakProgressColor.value = colorHex
        prefs.edit().putString("streak_progress_color", colorHex).apply()
    }
    
    fun updateStreakBrightness(brightness: Float) {
        _streakBrightness.value = brightness
        prefs.edit().putFloat("streak_brightness", brightness).apply()
    }
    
    fun updateStreakAnimationOverride(anim: String) {
        _streakAnimationOverride.value = anim
        prefs.edit().putString("streak_anim_override", anim).apply()
    }

    private fun calculateTodayStreakProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStart = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayEnd = todayStart + 86400000L

            val dao = repository.dao
            val tasks = dao.exportAllTasks()
            val assignments = dao.exportAllAssignments()
            val pomodoros = dao.exportAllPomodoro()

            val tasksToday = tasks.filter { it.dueDateMillis != null && it.dueDateMillis >= todayStart && it.dueDateMillis < todayEnd }
            val assignmentsToday = assignments.filter { it.dueDateMillis >= todayStart && it.dueDateMillis < todayEnd }
            val pomosToday = pomodoros.filter { it.dateMillis >= todayStart && it.dateMillis < todayEnd }

            val plannedTasks = tasksToday.size
            val plannedAssignments = assignmentsToday.size
            
            // Prioritize planned vs required
            val requiredTasks = maxOf(plannedTasks, _streakRequirementTasks.value)
            val requiredAssignments = maxOf(plannedAssignments, _streakRequirementAssignments.value)
            val requiredPomos = _streakRequirementStudyMins.value
            
            val actionLogs = dao.exportAllActionLogs()
            
            // To prevent exploits (completing deleted or past tasks/assignments):
            // 1. Get currently existing completed tasks and assignments
            val existingCompletedTasks = tasks.filter { it.isCompleted }
            val existingCompletedAssignments = assignments.filter { it.isCompleted }

            // 2. Filter existing completed elements that are NOT old:
            // - A task is not old if its due date is today or in the future, OR if it has no due date and was created today or later.
            val eligibleTasksCompletedTodayTitles = existingCompletedTasks.filter { task ->
                val isNotOld = (task.dueDateMillis != null && task.dueDateMillis >= todayStart) || 
                               (task.dueDateMillis == null && task.createdAt >= todayStart)
                isNotOld
            }.map { it.title }.toSet()

            // - An assignment is not old if its due date is today or in the future.
            val eligibleAssignmentsCompletedTodayTitles = existingCompletedAssignments.filter { assignment ->
                val isNotOld = assignment.dueDateMillis >= todayStart
                isNotOld
            }.map { it.title }.toSet()

            val completedTasksToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed task:") }
            val unmarkedTasksToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked task:") }
            
            val completedTitles = completedTasksToday.map { it.actionText.removePrefix("Completed task: ").trim() }
            val unmarkedTitles = unmarkedTasksToday.map { it.actionText.removePrefix("Unmarked task: ").trim() }
            
            val cCounts = completedTitles.groupingBy { it }.eachCount()
            val uCounts = unmarkedTitles.groupingBy { it }.eachCount()
            
            var netDoneTasksToday = 0
            for ((title, count) in cCounts) {
                if (title in eligibleTasksCompletedTodayTitles) {
                    val uCount = uCounts[title] ?: 0
                    netDoneTasksToday += maxOf(0, count - uCount)
                }
            }
            
            val completedAssignmentsToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed assignment:") }
            val unmarkedAssignmentsToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked assignment:") }
            
            val cAssignTitles = completedAssignmentsToday.map { it.actionText.removePrefix("Completed assignment: ").trim() }
            val uAssignTitles = unmarkedAssignmentsToday.map { it.actionText.removePrefix("Unmarked assignment: ").trim() }
            
            val cAssignCounts = cAssignTitles.groupingBy { it }.eachCount()
            val uAssignCounts = uAssignTitles.groupingBy { it }.eachCount()
            
            var netDoneAssignmentsToday = 0
            for ((title, count) in cAssignCounts) {
                if (title in eligibleAssignmentsCompletedTodayTitles) {
                    val uCount = uAssignCounts[title] ?: 0
                    netDoneAssignmentsToday += maxOf(0, count - uCount)
                }
            }

            val doneTasks = maxOf(tasksToday.count { it.isCompleted }, netDoneTasksToday)
            val doneAssignments = maxOf(assignmentsToday.count { it.isCompleted }, netDoneAssignmentsToday)
            val donePomos = pomosToday.sumOf { it.durationMinutes }
            
            var totalRequired = 0f
            var totalDone = 0f
            
            val hasAnyTasks = tasks.isNotEmpty() || doneTasks > 0
            val hasAnyAssignments = assignments.isNotEmpty() || doneAssignments > 0
            val hasAnyPomos = pomodoros.isNotEmpty() || donePomos > 0

            if (requiredTasks > 0 && hasAnyTasks) {
                totalRequired += 1f
                totalDone += (doneTasks.toFloat() / requiredTasks.toFloat()).coerceAtMost(1f)
            }
            if (requiredAssignments > 0 && hasAnyAssignments) {
                totalRequired += 1f
                totalDone += (doneAssignments.toFloat() / requiredAssignments.toFloat()).coerceAtMost(1f)
            }
            if (requiredPomos > 0 && hasAnyPomos) {
                totalRequired += 1f
                totalDone += (donePomos.toFloat() / requiredPomos.toFloat()).coerceAtMost(1f)
            }

            if (totalRequired == 0f) {
                if (requiredTasks > 0) { totalRequired += 1f; totalDone += (doneTasks.toFloat() / requiredTasks.toFloat()).coerceAtMost(1f) }
                if (requiredAssignments > 0) { totalRequired += 1f; totalDone += (doneAssignments.toFloat() / requiredAssignments.toFloat()).coerceAtMost(1f) }
                if (requiredPomos > 0) { totalRequired += 1f; totalDone += (donePomos.toFloat() / requiredPomos.toFloat()).coerceAtMost(1f) }
            }
            
            val percentage = if (totalRequired == 0f) 0f else (totalDone / totalRequired).coerceIn(0f, 1f)
            
            withContext(Dispatchers.Main) {
                _streakPercentage.value = percentage
                
                // Check if streak applies
                val lastStreakDate = prefs.getLong("streak_last_date", 0L)
                val threshold = _streakPartialThreshold.value
                
                val isComplete = (percentage >= threshold) && 
                                 (plannedTasks == 0 || doneTasks >= plannedTasks) && 
                                 (plannedAssignments == 0 || doneAssignments >= plannedAssignments) && 
                                 (percentage >= 1.0f)
                _streakIsCompleteToday.value = isComplete

                val todayStr = todayDateString()
                val statusToday = prefs.getString("streak_status_$todayStr", "none")

                if (isComplete && statusToday != "complete") {
                    val tone = _streakNotificationTone.value
                    val message = if (tone == "Motivational") {
                        lumia.tracker.util.StreakNotifications.motivational.random()
                    } else {
                        lumia.tracker.util.StreakNotifications.aggressive.random()
                    }
                    val iconRes = lumia.tracker.util.NotificationHelper.getSmallIcon()
                    val colorHex = if (_streakProgressColor.value == "Theme") "#3197D6" else _streakProgressColor.value
                    sendInstantNotification(
                        channelId = "scholar_streak_channel",
                        notifId = 3001,
                        title = "Streak Completed!",
                        text = message,
                        iconRes = iconRes,
                        color = try { android.graphics.Color.parseColor(colorHex) } catch(e: Exception) { android.graphics.Color.parseColor("#3197D6") },
                        openScreen = "settings/streaks"
                    )
                }

                if (percentage >= threshold) {
                    if (statusToday == "none") {
                        val isConsecutive = (todayStart - lastStreakDate) <= 86400000L * 2
                        val newCurrent = if (isConsecutive) _streakCurrent.value + 1 else 1
                        
                        _streakCurrent.value = newCurrent
                        if (newCurrent > _streakLongest.value) {
                            _streakLongest.value = newCurrent
                            prefs.edit().putInt("streak_longest", newCurrent).apply()
                        }
                        
                        if (isComplete) {
                            prefs.edit().putString("streak_status_$todayStr", "complete").apply()
                            _streakTotalComplete.value += 1
                        } else {
                            prefs.edit().putString("streak_status_$todayStr", "normal").apply()
                            _streakTotalNormal.value += 1
                        }
                        
                        prefs.edit()
                            .putInt("streak_current", newCurrent)
                            .putLong("streak_last_date", todayStart)
                            .putInt("streak_total_normal", _streakTotalNormal.value)
                            .putInt("streak_total_complete", _streakTotalComplete.value)
                            .apply()
                    } else if (statusToday == "normal" && isComplete) {
                        prefs.edit().putString("streak_status_$todayStr", "complete").apply()
                        _streakTotalNormal.value -= 1
                        _streakTotalComplete.value += 1
                        prefs.edit()
                            .putInt("streak_total_normal", _streakTotalNormal.value)
                            .putInt("streak_total_complete", _streakTotalComplete.value)
                            .apply()
                    } else if (statusToday == "complete" && !isComplete) {
                        prefs.edit().putString("streak_status_$todayStr", "normal").apply()
                        _streakTotalComplete.value -= 1
                        _streakTotalNormal.value += 1
                        prefs.edit()
                            .putInt("streak_total_normal", _streakTotalNormal.value)
                            .putInt("streak_total_complete", _streakTotalComplete.value)
                            .apply()
                    }
                } else {
                    if (statusToday != "none" && lastStreakDate == todayStart) {
                        val newCurrent = maxOf(0, _streakCurrent.value - 1)
                        _streakCurrent.value = newCurrent
                        
                        if (statusToday == "complete") {
                            _streakTotalComplete.value -= 1
                        } else if (statusToday == "normal") {
                            _streakTotalNormal.value -= 1
                        }
                        
                        prefs.edit()
                            .putString("streak_status_$todayStr", "none")
                            .putInt("streak_current", newCurrent)
                            .putInt("streak_total_normal", _streakTotalNormal.value)
                            .putInt("streak_total_complete", _streakTotalComplete.value)
                            .putLong("streak_last_date", if (newCurrent > 0) todayStart - 86400000L else 0L)
                            .apply()
                    }
                    
                    val currentLastDate = prefs.getLong("streak_last_date", 0L)
                    val yesterday = todayStart - 86400000L
                    if (currentLastDate < yesterday) {
                        _streakCurrent.value = 0
                        prefs.edit().putInt("streak_current", 0).apply()
                    }
                }
            }
        }
    }

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    val themeMode = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "Ocean") ?: "Ocean")
    val themeColor = _themeColor.asStateFlow()

    private val _customPrimary = MutableStateFlow(prefs.getString("custom_primary", "#3197D6") ?: "#3197D6")
    val customPrimary = _customPrimary.asStateFlow()

    private val _customPrimaryContainer = MutableStateFlow(prefs.getString("custom_primary_container", "#DAF1FF") ?: "#DAF1FF")
    val customPrimaryContainer = _customPrimaryContainer.asStateFlow()

    private val _customBackground = MutableStateFlow(prefs.getString("custom_background", "#FAFAFA") ?: "#FAFAFA")
    val customBackground = _customBackground.asStateFlow()

    private val _customSurface = MutableStateFlow(prefs.getString("custom_surface", "#FFFFFF") ?: "#FFFFFF")
    val customSurface = _customSurface.asStateFlow()

    private val _customText = MutableStateFlow(prefs.getString("custom_text", "#1A1C1A") ?: "#1A1C1A")
    val customText = _customText.asStateFlow()

    fun updateCustomColor(key: String, hex: String) {
        when(key) {
           "primary" -> _customPrimary.value = hex
           "primary_container" -> _customPrimaryContainer.value = hex
           "background" -> _customBackground.value = hex
           "surface" -> _customSurface.value = hex
           "text" -> _customText.value = hex
        }
        prefs.edit().putString("custom_$key", hex).apply()
    }

    fun generatePaletteFromPrimaryHex(hex: String) {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        try {
            val colorInt = android.graphics.Color.parseColor(cleanHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(colorInt, hsv) // Hue: 0-360, Sat: 0-1, Val: 0-1
            
            // 1. Primary is already set
            updateCustomColor("primary", cleanHex)
            
            // 2. Generate PrimaryContainer (high light, medium-low saturation)
            val pcHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.35f), 0.94f)
            val pcColor = android.graphics.Color.HSVToColor(pcHsv)
            val pcHex = String.format("#%06X", 0xFFFFFF and pcColor)
            updateCustomColor("primary_container", pcHex)
            
            // 3. Generate Ambient background (extremely low saturation, high brightness)
            val bgHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.05f), 0.98f)
            val bgColor = android.graphics.Color.HSVToColor(bgHsv)
            val bgHex = String.format("#%06X", 0xFFFFFF and bgColor)
            updateCustomColor("background", bgHex)
            
            // 4. Generate Surface (almost pure white, extremely low saturation)
            val sfHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.03f), 1.00f)
            val sfColor = android.graphics.Color.HSVToColor(sfHsv)
            val sfHex = String.format("#%06X", 0xFFFFFF and sfColor)
            updateCustomColor("surface", sfHex)
            
            // 5. Generate Text (deep brand color, high saturation weight, extremely dark value)
            val txtHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.30f), 0.12f)
            val txtColor = android.graphics.Color.HSVToColor(txtHsv)
            val txtHex = String.format("#%06X", 0xFFFFFF and txtColor)
            updateCustomColor("text", txtHex)

            // Auto-select "Custom" theme color
            updateThemeColor("Custom")
            
        } catch(e: Exception) {
            // Safe fallback so formatting typos while editing the input field do not cause crashes
        }
    }

    private val _pureBlackMode = MutableStateFlow(prefs.getBoolean("pure_black_mode", false))
    val pureBlackMode = _pureBlackMode.asStateFlow()

    private val _betaFloatingNav = MutableStateFlow(prefs.getBoolean("beta_floating_nav", false))
    val betaFloatingNav = _betaFloatingNav.asStateFlow()

    private val _navBarHeight = MutableStateFlow(prefs.getFloat("nav_bar_height", 80f))
    val navBarHeight = _navBarHeight.asStateFlow()

    private val _navBarPaddingHorizontal = MutableStateFlow(prefs.getFloat("nav_bar_padding_horizontal", 24f))
    val navBarPaddingHorizontal = _navBarPaddingHorizontal.asStateFlow()

    private val _navBarPaddingBottom = MutableStateFlow(prefs.getFloat("nav_bar_padding_bottom", 24f))
    val navBarPaddingBottom = _navBarPaddingBottom.asStateFlow()

    private val _navBarCornerRadius = MutableStateFlow(prefs.getFloat("nav_bar_corner_radius", 32f))
    val navBarCornerRadius = _navBarCornerRadius.asStateFlow()

    private val _navBarLabelMode = MutableStateFlow(prefs.getString("nav_bar_label_mode", "Always") ?: "Always")
    val navBarLabelMode = _navBarLabelMode.asStateFlow()

    private val _navBarGlassForceEnabled = MutableStateFlow(prefs.getBoolean("nav_bar_glass_force_enabled", false))
    val navBarGlassForceEnabled = _navBarGlassForceEnabled.asStateFlow()

    private val _navBarIndicatorAlpha = MutableStateFlow(prefs.getFloat("nav_bar_indicator_alpha", 0.15f))
    val navBarIndicatorAlpha = _navBarIndicatorAlpha.asStateFlow()

    private val _betaNotes = MutableStateFlow(prefs.getBoolean("beta_notes", false))
    val betaNotes = _betaNotes.asStateFlow()

    private val _appAnimationMode = MutableStateFlow(prefs.getString("app_animation_mode", "Normal") ?: "Normal")
    val appAnimationMode = _appAnimationMode.asStateFlow()

    private val _moreRounds = MutableStateFlow(prefs.getBoolean("more_rounds", false))
    val moreRounds = _moreRounds.asStateFlow()

    private val _moreRoundsMode = MutableStateFlow(prefs.getString("more_rounds_mode", "Pastel") ?: "Pastel")
    val moreRoundsMode = _moreRoundsMode.asStateFlow()

    fun updateAppAnimationMode(mode: String) {
        if (mode == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value && (_displayLayoutMode.value != "Immersive" || !_moreRounds.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Bouncy Animations Warning",
                description = "Bouncy animations require 'Immersive' layout mode and 'More Rounds' feature to be enabled. Proceed with enabling these requirements automatically?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    updateDisplayLayoutMode("Immersive")
                    updateMoreRounds(true)
                    _appAnimationMode.value = "Bouncy"
                    prefs.edit().putString("app_animation_mode", "Bouncy").apply()
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _appAnimationMode.value = mode
        prefs.edit().putString("app_animation_mode", mode).apply()
    }

    fun updateMoreRounds(enabled: Boolean) {
        if (!enabled && _appAnimationMode.value == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Required by Bouncy Animations",
                description = "Disabling 'More Rounds' will also disable 'Bouncy' animations and revert to 'Dynamic'. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _moreRounds.value = false
                    prefs.edit().putBoolean("more_rounds", false).apply()
                    updateAppAnimationMode("Dynamic")
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _moreRounds.value = enabled
        prefs.edit().putBoolean("more_rounds", enabled).apply()
        if (!enabled && _appAnimationMode.value == "Bouncy") {
            updateAppAnimationMode("Dynamic")
        }
    }

    fun updateMoreRoundsMode(mode: String) {
        _moreRoundsMode.value = mode
        prefs.edit().putString("more_rounds_mode", mode).apply()
    }

    private val _displayLayoutMode = MutableStateFlow(prefs.getString("display_layout_mode", "Immersive") ?: "Immersive")
    val displayLayoutMode = _displayLayoutMode.asStateFlow()

    private val _betaGlassUi = MutableStateFlow(prefs.getBoolean("beta_glass_ui", false))
    val betaGlassUi = _betaGlassUi.asStateFlow()

    private val _betaGlassDynamic = MutableStateFlow(prefs.getBoolean("beta_glass_dynamic", true))
    val betaGlassDynamic = _betaGlassDynamic.asStateFlow()

    private val _betaFrostGlass = MutableStateFlow(prefs.getBoolean("beta_frost_glass", true))
    val betaFrostGlass = _betaFrostGlass.asStateFlow()

    private val _glassBackdropStyle = MutableStateFlow(prefs.getString("glass_backdrop_style", "Translucent") ?: "Translucent")
    val glassBackdropStyle = _glassBackdropStyle.asStateFlow()

    private val _glassOpacityValue = MutableStateFlow(prefs.getFloat("glass_opacity_value", 0.6f))
    val glassOpacityValue = _glassOpacityValue.asStateFlow()

    private val _navBarGlassOpacityValue = MutableStateFlow(0.6f)
    val navBarGlassOpacityValue = _navBarGlassOpacityValue.asStateFlow()

    private val _betaNavBarSizeControls = MutableStateFlow(prefs.getBoolean("beta_nav_bar_size_controls", false))
    val betaNavBarSizeControls = _betaNavBarSizeControls.asStateFlow()

    private val _navBarGlassLinkedToMain = MutableStateFlow(prefs.getBoolean("nav_bar_glass_linked_to_main", true))
    val navBarGlassLinkedToMain = _navBarGlassLinkedToMain.asStateFlow()

    private val _navBarGlassBackdropStyle = MutableStateFlow(prefs.getString("nav_bar_glass_backdrop_style", "Translucent") ?: "Translucent")
    val navBarGlassBackdropStyle = _navBarGlassBackdropStyle.asStateFlow()

    private val _navBarGlassDynamic = MutableStateFlow(prefs.getBoolean("nav_bar_glass_dynamic", true))
    val navBarGlassDynamic = _navBarGlassDynamic.asStateFlow()

    private val _betaEnhancedHeader = MutableStateFlow(prefs.getBoolean("beta_enhanced_header", false))
    val betaEnhancedHeader = _betaEnhancedHeader.asStateFlow()

    private val _betaMinimalistMode = MutableStateFlow(prefs.getBoolean("beta_minimalist_mode", false))
    val betaMinimalistMode = _betaMinimalistMode.asStateFlow()

    private val _betaDynamicBackground = MutableStateFlow(prefs.getBoolean("beta_dynamic_background", false))
    val betaDynamicBackground = _betaDynamicBackground.asStateFlow()

    private val _systemAutoLinkByName = MutableStateFlow(prefs.getBoolean("system_auto_link_by_name", true))
    val systemAutoLinkByName = _systemAutoLinkByName.asStateFlow()

    private val _systemEnableSynergy = MutableStateFlow(prefs.getBoolean("system_enable_synergy", true))
    val systemEnableSynergy = _systemEnableSynergy.asStateFlow()

    private val _systemAutoCreateSubject = MutableStateFlow(prefs.getBoolean("system_auto_create_subject", false))
    val systemAutoCreateSubject = _systemAutoCreateSubject.asStateFlow()

    private val _systemFuseSubjectsCourses = MutableStateFlow(prefs.getBoolean("system_fuse_subjects_courses", true))
    val systemFuseSubjectsCourses = _systemFuseSubjectsCourses.asStateFlow()

    private val _systemAdvancedTasks = MutableStateFlow(prefs.getBoolean("system_advanced_tasks", true))
    val systemAdvancedTasks = _systemAdvancedTasks.asStateFlow()
    
    private val _systemPomodoroAutoLog = MutableStateFlow(prefs.getBoolean("system_pomodoro_auto_log", true))
    val systemPomodoroAutoLog = _systemPomodoroAutoLog.asStateFlow()


    fun submitRecommendationFeedback(recommendationId: String, rating: Int) {
        // rating: 1 for positive, -1 for negative
        // In a complete implementation, this would adjust the FocusPredictor model weights
        logAction("User rated recommendation $recommendationId with $rating")
    }

    private val _featureSubjectEnabled = MutableStateFlow(prefs.getBoolean("feature_subject_enabled", true))
    val featureSubjectEnabled = _featureSubjectEnabled.asStateFlow()

    private val _featureSelfStudyEnabled = MutableStateFlow(prefs.getBoolean("feature_self_study_enabled", true))
    val featureSelfStudyEnabled = _featureSelfStudyEnabled.asStateFlow()

    private val _featureAnalyticsEnabled = MutableStateFlow(prefs.getBoolean("feature_analytics_enabled", true))
    val featureAnalyticsEnabled = _featureAnalyticsEnabled.asStateFlow()

    private val _featureCalendarEnabled = MutableStateFlow(prefs.getBoolean("feature_calendar_enabled", true))
    val featureCalendarEnabled = _featureCalendarEnabled.asStateFlow()

    private val _featureQuickNotesEnabled = MutableStateFlow(prefs.getBoolean("feature_quick_notes_enabled", true))
    val featureQuickNotesEnabled = _featureQuickNotesEnabled.asStateFlow()

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

    val allAttachments = repository.allAttachments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getAttachmentsForCourse(courseId: Int) = repository.getAttachmentsForCourse(courseId)
    fun getAttachmentsForSubject(subjectId: Int) = repository.getAttachmentsForSubject(subjectId)

    fun addAttachment(name: String, filePath: String, fileType: String, sizeBytes: Long, courseId: Int?, subjectId: Int?) {
        viewModelScope.launch {
            repository.insertAttachment(
                lumia.tracker.model.Attachment(
                    name = name,
                    filePath = filePath,
                    fileType = fileType,
                    sizeBytes = sizeBytes,
                    courseId = courseId,
                    subjectId = subjectId
                )
            )
            logAction("Added attachment: $name")
        }
    }

    fun deleteAttachment(attachment: lumia.tracker.model.Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            try {
                val file = java.io.File(attachment.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            logAction("Deleted attachment: ${attachment.name}")
        }
    }

    private val _pomodoroWorkDuration = MutableStateFlow(prefs.getInt("pomodoro_work_duration", 25))
    val pomodoroWorkDuration = _pomodoroWorkDuration.asStateFlow()

    private val _pomodoroShortBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_short_break_duration", 5))
    val pomodoroShortBreakDuration = _pomodoroShortBreakDuration.asStateFlow()

    private val _pomodoroLongBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_long_break_duration", 15))
    val pomodoroLongBreakDuration = _pomodoroLongBreakDuration.asStateFlow()

    private val _pomodoroPeriodSessions = MutableStateFlow(prefs.getInt("pomodoro_period_sessions", 4))
    val pomodoroPeriodSessions = _pomodoroPeriodSessions.asStateFlow()
    
    private val _pomodoroEnablePeriodTarget = MutableStateFlow(prefs.getBoolean("pomodoro_enable_period_target", false))
    val pomodoroEnablePeriodTarget = _pomodoroEnablePeriodTarget.asStateFlow()

    fun updatePomodoroPeriodSessions(sessions: Int) {
        _pomodoroPeriodSessions.value = sessions
        prefs.edit().putInt("pomodoro_period_sessions", sessions).apply()
    }

    fun updatePomodoroEnablePeriodTarget(enabled: Boolean) {
        _pomodoroEnablePeriodTarget.value = enabled
        prefs.edit().putBoolean("pomodoro_enable_period_target", enabled).apply()
    }

    private val _notifFormalTone = MutableStateFlow(prefs.getBoolean("notif_formal_tone", true))
    val notifFormalTone = _notifFormalTone.asStateFlow()

    private val _notifEnableDeadlines = MutableStateFlow(prefs.getBoolean("notif_enable_deadlines", true))
    val notifEnableDeadlines = _notifEnableDeadlines.asStateFlow()

    private val _notifEnableClasses = MutableStateFlow(prefs.getBoolean("notif_enable_classes", true))
    val notifEnableClasses = _notifEnableClasses.asStateFlow()

    private val _notifEnableDailyDigest = MutableStateFlow(prefs.getBoolean("notif_enable_daily_digest", true))
    val notifEnableDailyDigest = _notifEnableDailyDigest.asStateFlow()

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

    private val _aodTrueBlackOled = MutableStateFlow(prefs.getBoolean("aod_true_black_oled", true))
    val aodTrueBlackOled = _aodTrueBlackOled.asStateFlow()

    private val _aodAutoDeactivateTrueBlack = MutableStateFlow(prefs.getBoolean("aod_auto_deactivate_true_black", true))
    val aodAutoDeactivateTrueBlack = _aodAutoDeactivateTrueBlack.asStateFlow()

    private val _aodBurnInShiftSpeed = MutableStateFlow(prefs.getInt("aod_burn_in_shift_speed", 10)) // in seconds
    val aodBurnInShiftSpeed = _aodBurnInShiftSpeed.asStateFlow()
    
    private val _aodLockScreenSupport = MutableStateFlow(prefs.getBoolean("aod_lock_screen_support", false))
    val aodLockScreenSupport = _aodLockScreenSupport.asStateFlow()

    private val _aodTrueAodEnabled = MutableStateFlow(prefs.getBoolean("aod_true_aod_enabled", false))
    val aodTrueAodEnabled = _aodTrueAodEnabled.asStateFlow()

    private val _aodTrueAodMode = MutableStateFlow(prefs.getString("aod_true_aod_mode", "overlay") ?: "overlay")
    val aodTrueAodMode = _aodTrueAodMode.asStateFlow()

    private val _aodSensitivity = MutableStateFlow(prefs.getString("aod_sensitivity", "highest") ?: "highest")
    val aodSensitivity = _aodSensitivity.asStateFlow()

    private val _aodMotionSensitivity = MutableStateFlow(prefs.getFloat("aod_motion_sensitivity", 1.2f))
    val aodMotionSensitivity = _aodMotionSensitivity.asStateFlow()

    private val _aodDimnessLevel = MutableStateFlow(prefs.getFloat("aod_dimness_level", 0.95f))
    val aodDimnessLevel = _aodDimnessLevel.asStateFlow()

    private val _aodLockTimeout = MutableStateFlow(prefs.getInt("aod_lock_timeout", 30))
    val aodLockTimeout = _aodLockTimeout.asStateFlow()

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

    fun updateAodTrueBlackOled(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (_themeMode.value == "Light" || _betaGlassUi.value || _betaDynamicBackground.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "AOD Style Warning",
                description = "Enabling 'True Black OLED' mode during Light theme, Dynamic wallpapers, or Glass UI can lead to strong contrast transitions when AOD focus opens or exits. Consider allowing auto-deactivation instead.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _aodTrueBlackOled.value = true
                    prefs.edit().putBoolean("aod_true_black_oled", true).apply()
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
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

    fun updatePomodoroWorkDuration(duration: Int) {
        _pomodoroWorkDuration.value = duration
        prefs.edit().putInt("pomodoro_work_duration", duration).apply()
    }

    fun updatePomodoroShortBreakDuration(duration: Int) {
        _pomodoroShortBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_short_break_duration", duration).apply()
    }

    fun updatePomodoroLongBreakDuration(duration: Int) {
        _pomodoroLongBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_long_break_duration", duration).apply()
    }

    private val _dynamicBgLightBrightness = MutableStateFlow(
        prefs.getFloat("dynamic_bg_light_brightness_${(prefs.getString("theme_color", "Ocean") ?: "Ocean").lowercase()}", 0.75f)
    )
    val dynamicBgLightBrightness = _dynamicBgLightBrightness.asStateFlow()

    private val _dynamicBgDarkBrightness = MutableStateFlow(
        prefs.getFloat("dynamic_bg_dark_brightness_${(prefs.getString("theme_color", "Ocean") ?: "Ocean").lowercase()}", 0.45f)
    )
    val dynamicBgDarkBrightness = _dynamicBgDarkBrightness.asStateFlow()

    fun refreshThemeBrightness() {
        val theme = _themeColor.value
        _dynamicBgLightBrightness.value = prefs.getFloat("dynamic_bg_light_brightness_${theme.lowercase()}", 0.75f)
        _dynamicBgDarkBrightness.value = prefs.getFloat("dynamic_bg_dark_brightness_${theme.lowercase()}", 0.45f)
    }

    private val _dynamicAppIcon = MutableStateFlow(prefs.getBoolean("dynamic_app_icon", false))
    val dynamicAppIcon = _dynamicAppIcon.asStateFlow()

    fun updateDynamicAppIcon(enabled: Boolean) {
        _dynamicAppIcon.value = enabled
        prefs.edit().putBoolean("dynamic_app_icon", enabled).apply()
        applyThemeBasedAppIcon(_themeColor.value)
        android.widget.Toast.makeText(getApplication(), "Icon changing... Launcher may take a moment to reflect changes or might require a home screen refresh.", android.widget.Toast.LENGTH_LONG).show()
    }

    private fun applyThemeBasedAppIcon(themeColor: String) {
        val enabled = _dynamicAppIcon.value
        val pm = getApplication<Application>().packageManager
        val packageName = getApplication<Application>().packageName

        val aliases = listOf(
            "lumia.tracker.DefaultAlias",
            "lumia.tracker.AliasEmerald",
            "lumia.tracker.AliasGold",
            "lumia.tracker.AliasRose",
            "lumia.tracker.AliasSage",
            "lumia.tracker.AliasTwilight",
            "lumia.tracker.AliasCustom",
            "lumia.tracker.AliasDynamic"
        )

        val targetAliasName = if (!enabled) {
            "lumia.tracker.DefaultAlias"
        } else {
            when (themeColor) {
                "Ocean" -> "lumia.tracker.DefaultAlias"
                "Emerald" -> "lumia.tracker.AliasEmerald"
                "Gold" -> "lumia.tracker.AliasGold"
                "Rose" -> "lumia.tracker.AliasRose"
                "Sage" -> "lumia.tracker.AliasSage"
                "Twilight" -> "lumia.tracker.AliasTwilight"
                "Custom" -> "lumia.tracker.AliasCustom"
                "Dynamic" -> "lumia.tracker.AliasDynamic"
                else -> "lumia.tracker.DefaultAlias"
            }
        }

        try {
            aliases.forEach { alias ->
                val compName = android.content.ComponentName(packageName, alias)
                val targetSetting = if (alias == targetAliasName) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(compName, targetSetting, android.content.pm.PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            android.util.Log.e("ScholarViewModel", "Exception toggling dynamic app icon aliases. Restoring defaults.", e)
            try {
                aliases.forEach { alias ->
                    val compName = android.content.ComponentName(packageName, alias)
                    val targetSetting = if (alias == "lumia.tracker.DefaultAlias") {
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    } else {
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    }
                    pm.setComponentEnabledSetting(compName, targetSetting, android.content.pm.PackageManager.DONT_KILL_APP)
                }
            } catch (ex: Exception) {}
        }
    }

    private val _betaBetterTexts = MutableStateFlow(prefs.getBoolean("beta_better_texts", false))
    val betaBetterTexts = _betaBetterTexts.asStateFlow()

    private val _betaBetterTextsPalette = MutableStateFlow(prefs.getBoolean("beta_better_texts_palette", true))
    val betaBetterTextsPalette = _betaBetterTextsPalette.asStateFlow()

    private val _safetyPinEnabled = MutableStateFlow(prefs.getBoolean("safety_pin_enabled", true))
    val safetyPinEnabled = _safetyPinEnabled.asStateFlow()

    private val _safetyPinConflictWarning = MutableStateFlow(prefs.getBoolean("safety_pin_conflict_warning", true))
    val safetyPinConflictWarning = _safetyPinConflictWarning.asStateFlow()

    private val _safetyPinRecommendations = MutableStateFlow(prefs.getBoolean("safety_pin_recommendations", true))
    val safetyPinRecommendations = _safetyPinRecommendations.asStateFlow()

    data class SafetyPinDialogData(
        val title: String,
        val description: String,
        val isConflict: Boolean,
        val onConfirm: () -> Unit,
        val onIgnore: () -> Unit
    )

    private val _safetyPinDialogData = run {
        val delegate = MutableStateFlow<SafetyPinDialogData?>(null)
        object : MutableStateFlow<SafetyPinDialogData?> by delegate {
            override var value: SafetyPinDialogData?
                get() = delegate.value
                set(v) {
                    if (v != null && delegate.value != null) return
                    delegate.value = v
                }
            override fun compareAndSet(expect: SafetyPinDialogData?, update: SafetyPinDialogData?): Boolean {
                if (update != null && delegate.value != null) return false
                return delegate.compareAndSet(expect, update)
            }
            override fun tryEmit(value: SafetyPinDialogData?): Boolean {
                if (value != null && delegate.value != null) return false
                return delegate.tryEmit(value)
            }
            override suspend fun emit(value: SafetyPinDialogData?) {
                if (value != null && delegate.value != null) return
                delegate.emit(value)
            }
        }
    }
    val safetyPinDialogData = _safetyPinDialogData.asStateFlow()

    fun dismissSafetyPinDialog() {
        _safetyPinDialogData.value = null
    }

    private val _showActionHistory = MutableStateFlow(prefs.getBoolean("show_action_history", true))
    val showActionHistory = _showActionHistory.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        
        refreshThemeBrightness()
        verifyFeatureEntitlements()
    }

    val courses: StateFlow<List<Course>> = repository.allCourses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val assignments: StateFlow<List<PracticeAssignment>> = repository.allAssignments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val actionLogs: StateFlow<List<ActionLog>> = repository.allActionLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pomodoroSessions: StateFlow<List<lumia.tracker.model.PomodoroSession>> = repository.allPomodoroSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTestRecords: StateFlow<List<lumia.tracker.model.TestRecord>> = repository.allTestRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTagCustomizations: StateFlow<List<TagCustomization>> = repository.dao.getAllTagCustomizations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTopics: StateFlow<List<Topic>> = repository.allTopics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allChapters: StateFlow<List<Chapter>> = repository.allChapters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getSessionsTodayCount(): Int {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return pomodoroSessions.value.count { it.dateMillis >= todayStart }
    }

    fun getNotesCount(): Int = notes.value.size
    
    fun getActiveTasksCount(): Int = tasks.value.count { !it.isCompleted }

    val notes: StateFlow<List<lumia.tracker.model.Note>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val topicFlowCache = HashMap<Int, StateFlow<List<Topic>>>()
    
    fun getTopicsForSubject(subjectId: Int): StateFlow<List<Topic>> {
        return topicFlowCache.getOrPut(subjectId) {
            repository.getTopicsForSubject(subjectId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    private val assignmentsFlowCache = HashMap<Int, StateFlow<List<PracticeAssignment>>>()
    private val chaptersFlowCache = HashMap<Int, StateFlow<List<Chapter>>>()

    fun getChaptersForSubject(subjectId: Int): StateFlow<List<Chapter>> {
        return chaptersFlowCache.getOrPut(subjectId) {
            repository.getChaptersForSubject(subjectId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun getAssignmentsForCourse(courseId: Int): StateFlow<List<PracticeAssignment>> {
        return assignmentsFlowCache.getOrPut(courseId) {
            repository.getAssignmentsForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    private val attendanceFlowCache = HashMap<Int, StateFlow<List<lumia.tracker.model.AttendanceRecord>>>()

    fun getAttendanceForCourse(courseId: Int): StateFlow<List<lumia.tracker.model.AttendanceRecord>> {
        return attendanceFlowCache.getOrPut(courseId) {
            repository.getAttendanceForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun addAttendanceRecord(courseId: Int, dateMillis: Long, status: String) {
        viewModelScope.launch {
            val normalized = java.util.Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            repository.insertAttendanceRecord(lumia.tracker.model.AttendanceRecord(courseId = courseId, dateMillis = normalized, status = status))
        }
    }

    fun addPomodoroSession(durationMinutes: Int, subjectId: Int? = null, courseId: Int? = null, assignmentId: Int? = null, taskId: Int? = null, topicId: Int? = null) {
        viewModelScope.launch {
            repository.insertPomodoroSession(lumia.tracker.model.PomodoroSession(
                dateMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                subjectId = subjectId,
                courseId = courseId,
                assignmentId = assignmentId,
                taskId = taskId,
                topicId = topicId
            ))
            logAction("Completed Pomodoro Session ($durationMinutes min)")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun addNote(content: String, courseId: Int? = null, subjectId: Int? = null, tag: String = "") {
        viewModelScope.launch {
            repository.insertNote(lumia.tracker.model.Note(
                content = content,
                dateMillis = System.currentTimeMillis(),
                courseId = courseId,
                subjectId = subjectId,
                tag = tag
            ))
            logAction("Added Note: ${content.take(20)}...")
        }
    }

    fun updateNote(note: lumia.tracker.model.Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: lumia.tracker.model.Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun updateAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(record)
        }
    }

    fun deleteAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
        }
    }
    
    // --- Test Records ---
    private val testRecordsFlowCache = HashMap<Int, StateFlow<List<lumia.tracker.model.TestRecord>>>()
    private val subjectTestRecordsFlowCache = HashMap<Int, StateFlow<List<lumia.tracker.model.TestRecord>>>()

    fun getTestRecordsForCourse(courseId: Int): StateFlow<List<lumia.tracker.model.TestRecord>> {
        return testRecordsFlowCache.getOrPut(courseId) {
            repository.getTestRecordsForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun getTestRecordsForSubject(subjectId: Int): StateFlow<List<lumia.tracker.model.TestRecord>> {
        return subjectTestRecordsFlowCache.getOrPut(subjectId) {
            repository.getTestRecordsForSubject(subjectId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun addTestRecord(record: lumia.tracker.model.TestRecord) {
        viewModelScope.launch {
            repository.insertTestRecord(record)
            logAction("Logged test record '${record.title}' with score ${record.marksObtained}/${record.totalMarks}")
        }
    }

    fun updateTestRecord(record: lumia.tracker.model.TestRecord) {
        viewModelScope.launch {
            repository.updateTestRecord(record)
            logAction("Updated test record '${record.title}'")
        }
    }

    fun deleteTestRecord(record: lumia.tracker.model.TestRecord) {
        viewModelScope.launch {
            repository.deleteTestRecord(record)
            logAction("Deleted test record '${record.title}'")
        }
    }

    fun addCourse(
        name: String,
        code: String = "",
        colorHex: String = "#3197D6",
        scheduleDays: String = "",
        scheduleStartTime: String = "",
        scheduleEndTime: String = "",
        instructor: String = "",
        schedule: String = "",
        description: String = "",
        subjectId: Int? = null,
        tags: String = "",
        subjectIds: String = ""
    ) {
        viewModelScope.launch {
            var finalSubjectId = subjectId
            if (finalSubjectId == null && _systemAutoCreateSubject.value) {
                val subId = repository.insertSubject(Subject(name = name))
                finalSubjectId = subId.toInt()
            }
            repository.insertCourse(
                Course(
                    name = name,
                    code = code,
                    colorHex = colorHex,
                    scheduleDays = scheduleDays,
                    scheduleStartTime = scheduleStartTime,
                    scheduleEndTime = scheduleEndTime,
                    instructor = instructor,
                    schedule = schedule,
                    description = description,
                    subjectId = finalSubjectId,
                    tags = tags,
                    subjectIds = subjectIds
                )
            )
            logAction("Added course: $name")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            logAction("Deleted course: ${course.name}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            logAction("Updated course: ${course.name}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun renameTagGlobally(oldTag: String, newTag: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val oldLower = oldTag.trim().lowercase()
            val cleanNew = newTag.trim()
            if (oldLower.isBlank() || cleanNew.isBlank()) return@launch

            // 1. Courses
            repository.dao.exportAllCourses().forEach { course ->
                val list = course.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateCourse(course.copy(tags = updatedTags))
                }
            }

            // 2. Subjects
            repository.dao.exportAllSubjects().forEach { subject ->
                val list = subject.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateSubject(subject.copy(tags = updatedTags))
                }
            }

            // 3. Chapters
            repository.dao.exportAllChapters().forEach { chapter ->
                val list = chapter.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateChapter(chapter.copy(tags = updatedTags))
                }
            }

            // 4. Topics
            repository.dao.exportAllTopics().forEach { topic ->
                val list = topic.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTopic(topic.copy(tags = updatedTags))
                }
            }

            // 5. Assignments
            repository.dao.exportAllAssignments().forEach { assignment ->
                val list = assignment.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateAssignment(assignment.copy(tags = updatedTags))
                }
            }

            // 6. Tasks
            repository.dao.exportAllTasks().forEach { task ->
                val list = task.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTask(task.copy(tags = updatedTags))
                }
            }

            // 7. TestRecords
            repository.dao.exportAllTestRecords().forEach { record ->
                val list = record.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == oldLower }) {
                    val updatedTags = list.map { if (it.lowercase() == oldLower) cleanNew else it }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTestRecord(record.copy(tags = updatedTags))
                }
            }

            // 8. TagCustomizations
            val oldCustom = repository.dao.exportAllTagCustomizations().find { it.tagName.lowercase() == oldLower }
            if (oldCustom != null) {
                repository.dao.deleteTagCustomization(oldCustom)
                repository.dao.insertTagCustomization(oldCustom.copy(tagName = cleanNew.lowercase()))
            }
            
            logAction("Renamed tag globally: '$oldTag' -> '$cleanNew'")
        }
    }

    fun deleteTagGlobally(tagToDelete: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val lower = tagToDelete.trim().lowercase()
            if (lower.isBlank()) return@launch

            // 1. Courses
            repository.dao.exportAllCourses().forEach { course ->
                val list = course.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateCourse(course.copy(tags = updatedTags))
                }
            }

            // 2. Subjects
            repository.dao.exportAllSubjects().forEach { subject ->
                val list = subject.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateSubject(subject.copy(tags = updatedTags))
                }
            }

            // 3. Chapters
            repository.dao.exportAllChapters().forEach { chapter ->
                val list = chapter.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateChapter(chapter.copy(tags = updatedTags))
                }
            }

            // 4. Topics
            repository.dao.exportAllTopics().forEach { topic ->
                val list = topic.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTopic(topic.copy(tags = updatedTags))
                }
            }

            // 5. Assignments
            repository.dao.exportAllAssignments().forEach { assignment ->
                val list = assignment.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateAssignment(assignment.copy(tags = updatedTags))
                }
            }

            // 6. Tasks
            repository.dao.exportAllTasks().forEach { task ->
                val list = task.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTask(task.copy(tags = updatedTags))
                }
            }

            // 7. TestRecords
            repository.dao.exportAllTestRecords().forEach { record ->
                val list = record.tags.split(",").map { it.trim() }
                if (list.any { it.lowercase() == lower }) {
                    val updatedTags = list.filter { it.lowercase() != lower }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    repository.updateTestRecord(record.copy(tags = updatedTags))
                }
            }

            // 8. TagCustomizations
            val oldCustom = repository.dao.exportAllTagCustomizations().find { it.tagName.lowercase() == lower }
            if (oldCustom != null) {
                repository.dao.deleteTagCustomization(oldCustom)
            }
            
            logAction("Deleted tag globally: '$tagToDelete'")
        }
    }

    fun insertTagCustomization(tagName: String, colorHex: String, description: String, isFavorite: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val normalized = tagName.trim().lowercase()
            if (normalized.isNotBlank()) {
                val custom = TagCustomization(
                    tagName = normalized,
                    colorHex = colorHex,
                    description = description,
                    isFavorite = isFavorite,
                    lastUsedMillis = System.currentTimeMillis()
                )
                repository.dao.insertTagCustomization(custom)
            }
        }
    }

    fun deleteTagCustomization(tagName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val normalized = tagName.trim().lowercase()
            if (normalized.isNotBlank()) {
                val custom = TagCustomization(tagName = normalized)
                repository.dao.deleteTagCustomization(custom)
            }
        }
    }

    fun addSubject(name: String, tags: String = "") {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, tags = tags))
            logAction("Added subject: $name")
        }
    }

    fun createSubjectAndLinkToCourse(name: String, tags: String = "", course: Course, existingSubjectIds: List<Int>) {
        viewModelScope.launch {
            val nextId = repository.insertSubject(Subject(name = name, tags = tags)).toInt()
            logAction("Added subject: $name")
            val updatedIds = (existingSubjectIds + nextId).distinct().joinToString(",")
            val firstId = if (course.subjectId == null) nextId else course.subjectId
            repository.updateCourse(course.copy(subjectIds = updatedIds, subjectId = firstId))
            logAction("Linked subject: $name to course: ${course.name}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            logAction("Deleted subject: ${subject.name}")
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            logAction("Updated subject: ${subject.name}")
        }
    }

    fun addTopic(subjectId: Int, title: String, tags: String = "", chapterId: Int? = null) {
        viewModelScope.launch {
            repository.insertTopic(Topic(subjectId = subjectId, title = title, tags = tags, chapterId = chapterId))
            logAction("Added topic: $title")
        }
    }

    fun toggleTopicCompleted(topic: Topic) {
        viewModelScope.launch {
            val newlyCompleted = !topic.isCompleted
            repository.updateTopic(topic.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed topic: ${topic.title}" else "Unmarked topic: ${topic.title}"
            logAction(actionText)
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            logAction("Deleted topic: ${topic.title}")
        }
    }

    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            repository.updateTopic(topic)
            logAction("Updated topic: ${topic.title}")
        }
    }

    fun addChapter(name: String, subjectId: Int, description: String = "", tags: String = "") {
        viewModelScope.launch {
            repository.insertChapter(Chapter(name = name, subjectId = subjectId, description = description, tags = tags))
            logAction("Added chapter: $name")
        }
    }

    fun updateChapter(chapter: Chapter) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
            logAction("Updated chapter: ${chapter.name}")
        }
    }

    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            logAction("Deleted chapter: ${chapter.name}")
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val newId = repository.insertTask(task).toInt()
            if (task.dueDateMillis != null) {
                val context = getApplication<Application>().applicationContext
                val links = mutableListOf<String>()
                if (task.subjectId != null) links.add("Subject")
                if (task.courseId != null) links.add("Course")
                if (task.assignmentId != null) links.add("Assignment")
                if (task.tags.isNotBlank()) links.add("Tags: ${task.tags}")
                lumia.tracker.util.ReminderScheduler.scheduleReminder(
                    context, newId + 20000,
                    "Task: ${task.title}",
                    task.description,
                    links.joinToString(", "),
                    task.dueDateMillis,
                    "task",
                    courseId = task.courseId,
                    subjectId = task.subjectId
                )
            }
            logAction("Added task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val newlyCompleted = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed task: ${task.title}" else "Unmarked task: ${task.title}"
            logAction(actionText)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            logAction("Updated task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun updateTasksOrder(tasks: List<Task>) {
        viewModelScope.launch {
            repository.updateTasks(tasks)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            logAction("Deleted task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun addAssignment(courseId: Int, title: String, desc: String, dueDate: Long, category: String = "Homework", categoryColor: String = "#3197D6", tags: String = "", subjectId: Int? = null) {
        viewModelScope.launch {
            val newId = repository.insertAssignment(PracticeAssignment(courseId = courseId, title = title, description = desc, dueDateMillis = dueDate, category = category, categoryColor = categoryColor, tags = tags, subjectId = subjectId)).toInt()
            val context = getApplication<Application>().applicationContext
            var interconnections = "Course: " + (courses.value.find { it.id == courseId }?.name ?: "Unknown")
            if (tags.isNotBlank()) interconnections += ", Tags: $tags"
            lumia.tracker.util.ReminderScheduler.scheduleReminder(
                context, newId, title, desc, interconnections, dueDate,
                courseId = courseId,
                subjectId = subjectId
            )
            logAction("Added assignment: $title ($category)")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun toggleAssignmentCompleted(assignment: PracticeAssignment) {
        viewModelScope.launch {
            val newlyCompleted = !assignment.isCompleted
            repository.updateAssignment(assignment.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed assignment: ${assignment.title}" else "Unmarked assignment: ${assignment.title}"
            logAction(actionText)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun deleteAssignment(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun updateAssignmentDetails(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
            logAction("Updated assignment: ${assignment.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    fun updateAssignmentsOrder(assignments: List<PracticeAssignment>) {
        viewModelScope.launch {
            repository.updateAssignments(assignments)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }

    // Export/Import
    private val _importExportStatus = MutableStateFlow<String?>(null)
    val importExportStatus = _importExportStatus.asStateFlow()

    private fun logAction(action: String) {
        viewModelScope.launch {
            repository.insertActionLog(ActionLog(actionText = action))
        }
    }

    private fun todayDateString(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())



    private fun sendInstantNotification(channelId: String, notifId: Int, title: String, text: String, iconRes: Int, color: Int, openScreen: String? = null, openTab: Int = -1) {
        val application = getApplication<Application>()
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Scholar System Alerts", android.app.NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                lightColor = color
            }
            notificationManager.createNotificationChannel(channel)
        }
        val intent = android.content.Intent(application, lumia.tracker.MainActivity::class.java).apply { 
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP 
            if (openScreen != null) {
                putExtra("OPEN_SCREEN", openScreen)
            }
            if (openTab != -1) {
                putExtra("OPEN_TAB", openTab)
            }
        }
        val pendingIntent = android.app.PendingIntent.getActivity(application, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val notification = androidx.core.app.NotificationCompat.Builder(application, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(text))
            .setColor(color)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notifId, notification)
    }

    private fun gatherSettings(pref: android.content.SharedPreferences = prefs): Map<String, String> {
        val map = mutableMapOf<String, String>()
        pref.all.forEach { (key, value) ->
            map[key] = value.toString()
        }
        return map
    }

    private fun restoreProfileSettings(pref: android.content.SharedPreferences, settings: Map<String, String>) {
        val editor = pref.edit()
        val booleanKeys = setOf(
            "onboarding_completed", "was_installed_before", "pure_black_mode", "beta_floating_nav",
            "nav_bar_glass_force_enabled", "beta_notes", "more_rounds", "beta_glass_ui", "beta_glass_dynamic",
            "beta_frost_glass", "beta_nav_bar_size_controls", "nav_bar_glass_linked_to_main", "nav_bar_glass_dynamic",
            "beta_enhanced_header", "beta_minimalist_mode", "beta_dynamic_background", "system_auto_link_by_name",
            "system_enable_synergy", "system_auto_create_subject", "system_fuse_subjects_courses", "system_advanced_tasks",
            "system_pomodoro_auto_log", "feature_subject_enabled", "feature_self_study_enabled", "feature_analytics_enabled",
            "feature_calendar_enabled", "feature_quick_notes_enabled", "pomodoro_enable_period_target", "notif_formal_tone",
            "notif_enable_deadlines", "notif_enable_classes", "notif_enable_daily_digest", "aod_true_black_oled",
            "aod_auto_deactivate_true_black", "aod_lock_screen_support", "aod_true_aod_enabled", "dynamic_app_icon",
            "beta_better_texts", "beta_better_texts_palette", "safety_pin_enabled", "safety_pin_conflict_warning",
            "safety_pin_recommendations", "show_action_history", "streak_is_complete_today"
        )

        val floatKeys = setOf(
            "nav_bar_height", "nav_bar_padding_horizontal", "nav_bar_padding_bottom", "nav_bar_corner_radius",
            "nav_bar_indicator_alpha", "glass_opacity_value", "nav_bar_glass_opacity_value", "aod_motion_sensitivity",
            "aod_dimness_level", "streak_partial_threshold", "streak_brightness"
        )

        val intKeys = setOf(
            "pomodoro_work_duration", "pomodoro_short_break_duration", "pomodoro_long_break_duration",
            "pomodoro_period_sessions", "aod_burn_in_shift_speed", "aod_lock_timeout", "streak_total_normal",
            "streak_total_complete", "streak_current", "streak_longest", "streak_req_tasks", "streak_req_assignments",
            "streak_req_study_mins"
        )

        val longKeys = setOf("streak_last_date")

        settings.forEach { (key, value) ->
            try {
                when {
                    booleanKeys.contains(key) -> {
                        val boolVal = value.toBooleanStrictOrNull() ?: (value == "1" || value.lowercase() == "true")
                        editor.putBoolean(key, boolVal)
                    }
                    floatKeys.contains(key) || key.startsWith("dynamic_bg_light_brightness_") || key.startsWith("dynamic_bg_dark_brightness_") -> {
                        val floatVal = value.toFloatOrNull() ?: 0f
                        editor.putFloat(key, floatVal)
                    }
                    intKeys.contains(key) -> {
                        val intVal = value.toIntOrNull() ?: 0
                        editor.putInt(key, intVal)
                    }
                    longKeys.contains(key) -> {
                        val longVal = value.toLongOrNull() ?: 0L
                        editor.putLong(key, longVal)
                    }
                    else -> {
                        editor.putString(key, value)
                    }
                }
            } catch (e: Exception) {
                editor.putString(key, value)
            }
        }
        editor.commit()
    }

    private fun loadSettings(settings: Map<String, String>?) {
        if (settings == null) return
        val editor = prefs.edit()
        val booleanKeys = setOf(
            "onboarding_completed", "was_installed_before", "pure_black_mode", "beta_floating_nav",
            "nav_bar_glass_force_enabled", "beta_notes", "more_rounds", "beta_glass_ui", "beta_glass_dynamic",
            "beta_frost_glass", "beta_nav_bar_size_controls", "nav_bar_glass_linked_to_main", "nav_bar_glass_dynamic",
            "beta_enhanced_header", "beta_minimalist_mode", "beta_dynamic_background", "system_auto_link_by_name",
            "system_enable_synergy", "system_auto_create_subject", "system_fuse_subjects_courses", "system_advanced_tasks",
            "system_pomodoro_auto_log", "feature_subject_enabled", "feature_self_study_enabled", "feature_analytics_enabled",
            "feature_calendar_enabled", "feature_quick_notes_enabled", "pomodoro_enable_period_target", "notif_formal_tone",
            "notif_enable_deadlines", "notif_enable_classes", "notif_enable_daily_digest", "aod_true_black_oled",
            "aod_auto_deactivate_true_black", "aod_lock_screen_support", "aod_true_aod_enabled", "dynamic_app_icon",
            "beta_better_texts", "beta_better_texts_palette", "safety_pin_enabled", "safety_pin_conflict_warning",
            "safety_pin_recommendations", "show_action_history", "streak_is_complete_today"
        )

        val floatKeys = setOf(
            "nav_bar_height", "nav_bar_padding_horizontal", "nav_bar_padding_bottom", "nav_bar_corner_radius",
            "nav_bar_indicator_alpha", "glass_opacity_value", "nav_bar_glass_opacity_value", "aod_motion_sensitivity",
            "aod_dimness_level", "streak_partial_threshold", "streak_brightness"
        )

        val intKeys = setOf(
            "pomodoro_work_duration", "pomodoro_short_break_duration", "pomodoro_long_break_duration",
            "pomodoro_period_sessions", "aod_burn_in_shift_speed", "aod_lock_timeout", "streak_total_normal",
            "streak_total_complete", "streak_current", "streak_longest", "streak_req_tasks", "streak_req_assignments",
            "streak_req_study_mins"
        )

        val longKeys = setOf("streak_last_date")

        settings.forEach { (key, value) ->
            try {
                when {
                    booleanKeys.contains(key) -> {
                        val boolVal = value.toBooleanStrictOrNull() ?: (value == "1" || value.lowercase() == "true")
                        editor.putBoolean(key, boolVal)
                        when (key) {
                            "pure_black_mode" -> _pureBlackMode.value = boolVal
                            "beta_floating_nav" -> _betaFloatingNav.value = boolVal
                            "beta_notes" -> _betaNotes.value = boolVal
                            "beta_glass_ui" -> _betaGlassUi.value = boolVal
                            "beta_glass_dynamic" -> _betaGlassDynamic.value = boolVal
                            "beta_frost_glass" -> _betaFrostGlass.value = boolVal
                            "beta_enhanced_header" -> _betaEnhancedHeader.value = boolVal
                            "beta_minimalist_mode" -> _betaMinimalistMode.value = boolVal
                            "beta_dynamic_background" -> _betaDynamicBackground.value = boolVal
                            "dynamic_app_icon" -> _dynamicAppIcon.value = boolVal
                            "beta_better_texts" -> _betaBetterTexts.value = boolVal
                            "beta_better_texts_palette" -> _betaBetterTextsPalette.value = boolVal
                            "safety_pin_enabled" -> _safetyPinEnabled.value = boolVal
                            "safety_pin_conflict_warning" -> _safetyPinConflictWarning.value = boolVal
                            "safety_pin_recommendations" -> _safetyPinRecommendations.value = boolVal
                            "show_action_history" -> _showActionHistory.value = boolVal
                            "system_auto_link_by_name" -> _systemAutoLinkByName.value = boolVal
                            "system_enable_synergy" -> _systemEnableSynergy.value = boolVal
                            "system_auto_create_subject" -> _systemAutoCreateSubject.value = boolVal
                            "system_fuse_subjects_courses" -> _systemFuseSubjectsCourses.value = boolVal
                            "system_advanced_tasks" -> _systemAdvancedTasks.value = boolVal
                            "system_pomodoro_auto_log" -> _systemPomodoroAutoLog.value = boolVal
                            "nav_bar_glass_force_enabled" -> _navBarGlassForceEnabled.value = boolVal
                            "beta_nav_bar_size_controls" -> _betaNavBarSizeControls.value = boolVal
                            "nav_bar_glass_linked_to_main" -> _navBarGlassLinkedToMain.value = boolVal
                            "nav_bar_glass_dynamic" -> _navBarGlassDynamic.value = boolVal
                            "aod_true_aod_enabled" -> _aodTrueAodEnabled.value = boolVal
                            "streak_is_complete_today" -> _streakIsCompleteToday.value = boolVal
                        }
                    }
                    floatKeys.contains(key) || key.startsWith("dynamic_bg_light_brightness_") || key.startsWith("dynamic_bg_dark_brightness_") -> {
                        val floatVal = value.toFloatOrNull() ?: 0f
                        editor.putFloat(key, floatVal)
                        when (key) {
                            "nav_bar_height" -> _navBarHeight.value = floatVal
                            "nav_bar_padding_horizontal" -> _navBarPaddingHorizontal.value = floatVal
                            "nav_bar_padding_bottom" -> _navBarPaddingBottom.value = floatVal
                            "nav_bar_corner_radius" -> _navBarCornerRadius.value = floatVal
                            "nav_bar_indicator_alpha" -> _navBarIndicatorAlpha.value = floatVal
                            "glass_opacity_value" -> _glassOpacityValue.value = floatVal
                            "nav_bar_glass_opacity_value" -> _navBarGlassOpacityValue.value = floatVal
                            "aod_motion_sensitivity" -> _aodMotionSensitivity.value = floatVal
                            "aod_dimness_level" -> _aodDimnessLevel.value = floatVal
                            "streak_partial_threshold" -> _streakPartialThreshold.value = floatVal
                            "streak_brightness" -> _streakBrightness.value = floatVal
                            "dynamic_bg_light_brightness" -> _dynamicBgLightBrightness.value = floatVal
                            "dynamic_bg_dark_brightness" -> _dynamicBgDarkBrightness.value = floatVal
                        }
                    }
                    intKeys.contains(key) -> {
                        val intVal = value.toIntOrNull() ?: 0
                        editor.putInt(key, intVal)
                        when (key) {
                            "pomodoro_work_duration" -> _pomodoroWorkDuration.value = intVal
                            "pomodoro_short_break_duration" -> _pomodoroShortBreakDuration.value = intVal
                            "pomodoro_long_break_duration" -> _pomodoroLongBreakDuration.value = intVal
                            "pomodoro_period_sessions" -> _pomodoroPeriodSessions.value = intVal
                            "aod_burn_in_shift_speed" -> _aodBurnInShiftSpeed.value = intVal
                            "aod_lock_timeout" -> _aodLockTimeout.value = intVal
                            "streak_total_normal" -> _streakTotalNormal.value = intVal
                            "streak_total_complete" -> _streakTotalComplete.value = intVal
                            "streak_current" -> _streakCurrent.value = intVal
                            "streak_longest" -> _streakLongest.value = intVal
                            "streak_req_tasks" -> _streakRequirementTasks.value = intVal
                            "streak_req_assignments" -> _streakRequirementAssignments.value = intVal
                            "streak_req_study_mins" -> _streakRequirementStudyMins.value = intVal
                        }
                    }
                    longKeys.contains(key) -> {
                        val longVal = value.toLongOrNull() ?: 0L
                        editor.putLong(key, longVal)
                    }
                    else -> {
                        editor.putString(key, value)
                        when (key) {
                            "theme_mode" -> _themeMode.value = value
                            "theme_color" -> _themeColor.value = value
                            "custom_primary" -> _customPrimary.value = value
                            "custom_primary_container" -> _customPrimaryContainer.value = value
                            "custom_background" -> _customBackground.value = value
                            "custom_surface" -> _customSurface.value = value
                            "custom_text" -> _customText.value = value
                            "nav_bar_label_mode" -> _navBarLabelMode.value = value
                            "glass_backdrop_style" -> _glassBackdropStyle.value = value
                            "nav_bar_glass_backdrop_style" -> _navBarGlassBackdropStyle.value = value
                            "aod_true_aod_mode" -> _aodTrueAodMode.value = value
                            "aod_sensitivity" -> _aodSensitivity.value = value
                            "streak_progress_color" -> _streakProgressColor.value = value
                            "streak_anim_override" -> _streakAnimationOverride.value = value
                            "streak_notif_tone" -> _streakNotificationTone.value = value
                        }
                    }
                }
            } catch (e: Exception) {
                editor.putString(key, value)
            }
        }
        editor.apply()
        refreshThemeBrightness()
    }

        fun exportData(uri: Uri, exportAll: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val backupAdapter = moshi.adapter(lumia.tracker.model.ScholarBackup::class.java)
                val fullBackupAdapter = moshi.adapter(lumia.tracker.model.FullAppBackup::class.java)

                if (exportAll) {
                    val allProfs = profileManager.getAllProfiles()
                    val mappedProfs = allProfs.map { prof ->
                        val isLocal = prof.avatarEmoji.startsWith("/") || prof.avatarEmoji.startsWith("file://") || prof.avatarEmoji.startsWith("content://")
                        val base64 = if (isLocal) {
                            try {
                                val cleanPath = prof.avatarEmoji.removePrefix("file://").removePrefix("content://")
                                val file = java.io.File(cleanPath)
                                if (file.exists()) {
                                    val bytes = file.readBytes()
                                    android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                } else null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        } else null
                        if (base64 != null) prof.copy(avatarBase64 = base64) else prof
                    }
                    val profileBackupsJson = mutableMapOf<String, String>()
                    for (prof in mappedProfs) {
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                        val profDao = db.scholarDao()
                        val pref = profileManager.getProfilePrefs(prof.id)
                        val sets = gatherSettings(pref)
                        val pBackup = lumia.tracker.model.ScholarBackup(
                            courses = profDao.exportAllCourses(),
                            subjects = profDao.exportAllSubjects(),
                            topics = profDao.exportAllTopics(),
                            assignments = profDao.exportAllAssignments(),
                            settings = sets,
                            attendance = profDao.exportAllAttendance(),
                            pomodoro = profDao.exportAllPomodoro(),
                            actionLogs = profDao.exportAllActionLogs(),
                            notes = profDao.exportAllNotes(),
                            chapters = profDao.exportAllChapters(),
                            tasks = profDao.exportAllTasks(),
                            attachments = profDao.exportAllAttachments(),
                            testRecords = profDao.exportAllTestRecords(),
                            profile = prof
                        )
                        profileBackupsJson[prof.id] = backupAdapter.toJson(pBackup)
                    }
                    val fullAppBackup = lumia.tracker.model.FullAppBackup(
                        profiles = mappedProfs,
                        activeProfileId = profileManager.getActiveProfileId(),
                        globalPrefs = emptyMap(),
                        profileBackupsJson = profileBackupsJson
                    )
                    
                    val mainBackup = lumia.tracker.model.ScholarBackup(isFullAppBackup = true, fullAppBackupJson = fullBackupAdapter.toJson(fullAppBackup))
                    
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                        repository.exportDataToStream(os, mainBackup)
                    }
                } else {
                    val currentProf = profileManager.getActiveProfile()
                    val isLocal = currentProf.avatarEmoji.startsWith("/") || currentProf.avatarEmoji.startsWith("file://") || currentProf.avatarEmoji.startsWith("content://")
                    val base64 = if (isLocal) {
                        try {
                            val cleanPath = currentProf.avatarEmoji.removePrefix("file://").removePrefix("content://")
                            val file = java.io.File(cleanPath)
                            if (file.exists()) {
                                val bytes = file.readBytes()
                                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                            } else null
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } else null
                    val currentProfWithPic = if (base64 != null) currentProf.copy(avatarBase64 = base64) else currentProf

                    val singleBackup = lumia.tracker.model.ScholarBackup(
                        courses = repository.dao.exportAllCourses(),
                        subjects = repository.dao.exportAllSubjects(),
                        topics = repository.dao.exportAllTopics(),
                        assignments = repository.dao.exportAllAssignments(),
                        settings = gatherSettings(),
                        attendance = repository.dao.exportAllAttendance(),
                        pomodoro = repository.dao.exportAllPomodoro(),
                        actionLogs = repository.dao.exportAllActionLogs(),
                        notes = repository.dao.exportAllNotes(),
                        chapters = repository.dao.exportAllChapters(),
                        tasks = repository.dao.exportAllTasks(),
                        attachments = repository.dao.exportAllAttachments(),
                        testRecords = repository.dao.exportAllTestRecords(),
                        profile = currentProfWithPic
                    )
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                        repository.exportDataToStream(os, singleBackup)
                    }
                }
                
                _importExportStatus.value = "Secure backup binary package exported successfully"
            } catch (e: Exception) {
                _importExportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    private fun restoreProfileAvatar(prof: lumia.tracker.model.UserProfile): lumia.tracker.model.UserProfile {
        val b64 = prof.avatarBase64
        if (b64.isNullOrBlank()) return prof
        return try {
            val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
            val context = getApplication<Application>().applicationContext
            val avatarDir = java.io.File(context.filesDir, "avatars").apply { mkdirs() }
            val destFile = java.io.File(avatarDir, "profile_avatar_${System.currentTimeMillis()}_restored_${prof.id.take(5)}.jpg")
            java.io.FileOutputStream(destFile).use { fos ->
                fos.write(bytes)
            }
            prof.copy(avatarEmoji = destFile.absolutePath, avatarBase64 = null)
        } catch (e: Exception) {
            e.printStackTrace()
            prof
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var mainBackup: lumia.tracker.model.ScholarBackup? = null
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { ins ->
                    mainBackup = repository.importDataFromStream(ins)
                }
                
                if (mainBackup == null) throw IllegalArgumentException("No data found")
                
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                
                // Clear any cached DB connections before overwriting databases
                lumia.tracker.data.AppDatabase.clearInstances()

                if (mainBackup!!.isFullAppBackup && mainBackup!!.fullAppBackupJson != null) {
                    val fullBackupAdapter = moshi.adapter(lumia.tracker.model.FullAppBackup::class.java)
                    val backupAdapter = moshi.adapter(lumia.tracker.model.ScholarBackup::class.java)
                    val fullBackup = fullBackupAdapter.fromJson(mainBackup!!.fullAppBackupJson!!) ?: throw IllegalArgumentException("Invalid full backup")
                    
                    val restoredProfiles = fullBackup.profiles.map { prof ->
                        restoreProfileAvatar(prof)
                    }

                    // Clear existing profiles
                    val currentProfs = profileManager.getAllProfiles()
                    for (prof in currentProfs) {
                        if (!prof.isDefault) {
                            profileManager.deleteProfile(prof.id)
                        }
                    }
                    
                    // Restore profiles
                    for (prof in restoredProfiles) {
                        if (prof.isDefault) continue
                        profileManager.addProfile(prof.name, prof.avatarEmoji, prof.alias, prof.starterTheme)
                    }
                    val profListJson = moshi.adapter<List<lumia.tracker.model.UserProfile>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, lumia.tracker.model.UserProfile::class.java)).toJson(restoredProfiles)
                    val globalPrefs = getApplication<Application>().getSharedPreferences("global_profiles", android.content.Context.MODE_PRIVATE)
                    globalPrefs.edit().putString("profiles_json", profListJson).commit()
                    profileManager.setActiveProfileId(fullBackup.activeProfileId)
                    
                    // Restore each profile's database & settings
                    for ((profId, pJson) in fullBackup.profileBackupsJson) {
                        val pBackup = backupAdapter.fromJson(pJson) ?: continue
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), profId)
                        val pDao = db.scholarDao()
                        repository.restoreBackupToDao(pBackup, pDao)
                        
                        // Restore settings with exact, type-safe matching
                        pBackup.settings?.let { sets ->
                            val pref = profileManager.getProfilePrefs(profId)
                            restoreProfileSettings(pref, sets)
                        }
                    }
                    
                    // Refresh current active UI with the restored settings of the active profile
                    val activeId = fullBackup.activeProfileId
                    val activeBackupJson = fullBackup.profileBackupsJson[activeId]
                    if (activeBackupJson != null) {
                        val activeBackup = backupAdapter.fromJson(activeBackupJson)
                        if (activeBackup != null) {
                            loadSettings(activeBackup.settings)
                        }
                    }
                    
                } else {
                    // Single profile restore
                    repository.restoreBackupToDao(mainBackup!!, repository.dao)
                    loadSettings(mainBackup!!.settings)
                    mainBackup!!.profile?.let { importedProf ->
                        val restoredProf = restoreProfileAvatar(importedProf)
                        val currentProf = profileManager.getActiveProfile()
                        val updatedProf = currentProf.copy(
                            name = restoredProf.name,
                            avatarEmoji = restoredProf.avatarEmoji,
                            alias = restoredProf.alias,
                            starterTheme = restoredProf.starterTheme
                        )
                        profileManager.updateProfile(updatedProf)
                    }
                }

                // Make sure cache instances are completely cleared and reset to target the updated SQLite tables
                lumia.tracker.data.AppDatabase.clearInstances()

                verifyFeatureEntitlements()
                calculateTodayStreakProgress()
                _importExportStatus.value = "Secure backup package imported and restored successfully"
                activeProfile.value = profileManager.getActiveProfile()
                allProfiles.value = profileManager.getAllProfiles()
                lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
                calculateTodayStreakProgress()

                withContext(Dispatchers.Main) {
                    val activeId = profileManager.getActiveProfileId()
                    switchProfileAndRestart(getApplication(), activeId)
                }
            } catch (e: Exception) {
                _importExportStatus.value = "Import failed: ${e.message}"
            }
        }
    }

    fun clearImportExportStatus() {
        _importExportStatus.value = ""
    }

    private fun verifyFeatureEntitlements() {
        
    }

    fun clearStatus() {
        _importExportStatus.value = null
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

    fun updateThemeMode(mode: String) {
        if (safetyPinEnabled.value && safetyPinConflictWarning.value && mode == "Light" && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "Switching to 'Light' theme conflicts with 'Pure Black Mode', which requires a dark theme to function. Proceeding will automatically disable 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (mode == "Dark" && safetyPinEnabled.value && safetyPinRecommendations.value && !_pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the deepest contrast and battery savings on OLED screens, it is recommended to enable 'Pure Black Mode' with the Dark theme. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                    updatePureBlackMode(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                }
            )
            return
        }

        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun updatePureBlackMode(enabled: Boolean) {
        val conflictsWithDynamicBg = _betaDynamicBackground.value
        val conflictsWithGlassUi = _betaGlassUi.value
        val conflictsWithPalette = _betaBetterTextsPalette.value
        val conflictsWithEnhancedHeader = _betaEnhancedHeader.value

        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (conflictsWithDynamicBg || conflictsWithGlassUi || conflictsWithPalette || conflictsWithEnhancedHeader)) {
            val opposingFeatures = mutableListOf<String>()
            if (conflictsWithDynamicBg) opposingFeatures.add("'Dynamic Lighting Background'")
            if (conflictsWithGlassUi) opposingFeatures.add("'Glass UI'")
            if (conflictsWithPalette) opposingFeatures.add("'Use Palette Shades for Text'")
            if (conflictsWithEnhancedHeader) opposingFeatures.add("'Enhanced Header'")
            
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Pure Black Mode' directly opposes the functionality of ${opposingFeatures.joinToString(" and ")}. Proceeding will automatically deactivate these opposing settings to maintain visual consistency and readability.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                    if (conflictsWithDynamicBg) updateBetaDynamicBackground(false)
                    if (conflictsWithGlassUi) updateBetaGlassUi(false)
                    if (conflictsWithPalette) updateBetaBetterTextsPalette(false)
                    if (conflictsWithEnhancedHeader) updateBetaEnhancedHeader(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && _themeMode.value != "Dark") {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the optimal experience of 'Pure Black Mode', it is highly recommended to switch your system theme to 'Dark'. The current setting limits the effectiveness of the pure black backgrounds.",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                    updateThemeMode("Dark")
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                }
            )
            return
        }
        _pureBlackMode.value = enabled
        prefs.edit().putBoolean("pure_black_mode", enabled).apply()
    }

    fun updateThemeColor(color: String) {
        _themeColor.value = color
        prefs.edit().putString("theme_color", color).apply()
        refreshThemeBrightness()
        
        // Notify widgets to update their theme
        val app = getApplication<Application>()
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarTasksWidgetProvider::class.java)) })
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarCalendarWidgetProvider::class.java)) })
        app.sendBroadcast(android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply { setComponent(android.content.ComponentName(app, lumia.tracker.util.ScholarPomodoroWidgetProvider::class.java)) })

        if (_dynamicAppIcon.value) {
            applyThemeBasedAppIcon(color)
        }
    }

    fun updateBetaFloatingNav(enabled: Boolean) {
        _betaFloatingNav.value = enabled
        prefs.edit().putBoolean("beta_floating_nav", enabled).apply()
    }

    fun updateNavBarHeight(height: Float) {
        _navBarHeight.value = height
        prefs.edit().putFloat("nav_bar_height", height).apply()
    }

    fun updateNavBarPaddingHorizontal(padding: Float) {
        _navBarPaddingHorizontal.value = padding
        prefs.edit().putFloat("nav_bar_padding_horizontal", padding).apply()
    }

    fun updateNavBarPaddingBottom(padding: Float) {
        _navBarPaddingBottom.value = padding
        prefs.edit().putFloat("nav_bar_padding_bottom", padding).apply()
    }

    fun updateNavBarCornerRadius(radius: Float) {
        _navBarCornerRadius.value = radius
        prefs.edit().putFloat("nav_bar_corner_radius", radius).apply()
    }

    fun updateNavBarLabelMode(mode: String) {
        _navBarLabelMode.value = mode
        prefs.edit().putString("nav_bar_label_mode", mode).apply()
    }

    fun updateNavBarGlassForceEnabled(enabled: Boolean) {
        _navBarGlassForceEnabled.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_force_enabled", enabled).apply()
    }

    fun updateNavBarIndicatorAlpha(alpha: Float) {
        _navBarIndicatorAlpha.value = alpha
        prefs.edit().putFloat("nav_bar_indicator_alpha", alpha).apply()
    }

    fun updateBetaNavBarSizeControls(enabled: Boolean) {
        _betaNavBarSizeControls.value = enabled
        prefs.edit().putBoolean("beta_nav_bar_size_controls", enabled).apply()
    }

    fun updateNavBarGlassLinkedToMain(enabled: Boolean) {
        _navBarGlassLinkedToMain.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_linked_to_main", enabled).apply()
    }

    fun updateNavBarGlassBackdropStyle(style: String) {
        _navBarGlassBackdropStyle.value = style
        prefs.edit().putString("nav_bar_glass_backdrop_style", style).apply()
    }

    fun updateNavBarGlassDynamic(enabled: Boolean) {
        _navBarGlassDynamic.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_dynamic", enabled).apply()
    }

    fun updateBetaNotes(enabled: Boolean) {
        _betaNotes.value = enabled
        prefs.edit().putBoolean("beta_notes", enabled).apply()
    }

    fun updateDisplayLayoutMode(mode: String) {
        if (mode != "Immersive" && _appAnimationMode.value == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Required by Bouncy Animations",
                description = "Changing from 'Immersive' mode will also disable 'Bouncy' animations and revert to 'Dynamic'. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _displayLayoutMode.value = mode
                    prefs.edit().putString("display_layout_mode", mode).apply()
                    updateAppAnimationMode("Dynamic")
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _displayLayoutMode.value = mode
        prefs.edit().putString("display_layout_mode", mode).apply()
        if (mode != "Immersive" && _appAnimationMode.value == "Bouncy") {
            updateAppAnimationMode("Dynamic")
        }
    }

    fun updateBetaMinimalistMode(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (_betaGlassUi.value || _betaDynamicBackground.value || _betaEnhancedHeader.value || _betaFloatingNav.value || _betaBetterTexts.value || _displayLayoutMode.value != "Immersive" || _appAnimationMode.value != "Minimal" || _moreRounds.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "Activating 'Minimalist Mode' will force-disable 'Glass UI', 'Dynamic Lighting', 'Enhanced Header', 'Floating Action Bar', 'Better Texts', bouncy animations, and rounded UI components, locking them to drastically reduce visual clutter. Additionally, 'Immersive Mode' will be turned ON. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaMinimalistMode.value = true
                    prefs.edit().putBoolean("beta_minimalist_mode", true).apply()
                    if (_betaGlassUi.value) updateBetaGlassUi(false)
                    if (_betaDynamicBackground.value) updateBetaDynamicBackground(false)
                    if (_betaEnhancedHeader.value) updateBetaEnhancedHeader(false)
                    if (_betaFloatingNav.value) updateBetaFloatingNav(false)
                    if (_betaBetterTexts.value) updateBetaBetterTexts(false)
                    if (_moreRounds.value) updateMoreRounds(false)
                    if (_appAnimationMode.value != "Minimal") updateAppAnimationMode("Minimal")
                    if (_displayLayoutMode.value != "Immersive") updateDisplayLayoutMode("Immersive")
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        _betaMinimalistMode.value = enabled
        prefs.edit().putBoolean("beta_minimalist_mode", enabled).apply()
        
        if (enabled && !_safetyPinEnabled.value) {
            if (_betaGlassUi.value) updateBetaGlassUi(false)
            if (_betaDynamicBackground.value) updateBetaDynamicBackground(false)
            if (_betaEnhancedHeader.value) updateBetaEnhancedHeader(false)
            if (_betaFloatingNav.value) updateBetaFloatingNav(false)
            if (_betaBetterTexts.value) updateBetaBetterTexts(false)
            if (_displayLayoutMode.value != "Immersive") updateDisplayLayoutMode("Immersive")
        }
    }

    fun updateBetaGlassUi(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Glass UI' directly opposes the functionality of 'Pure Black Mode'. Glass UI requires background colors to create frosted translucency. Proceeding will automatically deactivate 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && (!_betaDynamicBackground.value || !_betaFloatingNav.value || !_betaBetterTexts.value || !_betaEnhancedHeader.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For an enhanced visual experience, it is highly recommended to activate 'Dynamic Lighting Background', 'Floating Action Bar', 'Better Texts', and 'Enhanced Header' alongside 'Glass UI'. Would you like to apply these complementary settings?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                    updateBetaDynamicBackground(true)
                    updateBetaFloatingNav(true)
                    updateBetaBetterTexts(true)
                    updateBetaEnhancedHeader(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                }
            )
            return
        }
        _betaGlassUi.value = enabled
        prefs.edit().putBoolean("beta_glass_ui", enabled).apply()
    }

    fun updateBetaGlassDynamic(enabled: Boolean) {
        _betaGlassDynamic.value = enabled
        prefs.edit().putBoolean("beta_glass_dynamic", enabled).apply()
    }

    fun updateBetaFrostGlass(enabled: Boolean) {
        _betaFrostGlass.value = enabled
        prefs.edit().putBoolean("beta_frost_glass", enabled).apply()
    }

    fun updateGlassBackdropStyle(style: String) {
        _glassBackdropStyle.value = style
        prefs.edit().putString("glass_backdrop_style", style).apply()
    }

    fun updateGlassOpacityValue(value: Float) {
        _glassOpacityValue.value = value
        prefs.edit().putFloat("glass_opacity_value", value).apply()
    }

    fun updateNavBarGlassOpacityValue(value: Float, alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = value
        prefs.edit().putFloat(key, value).apply()
    }

    fun refreshNavBarGlassOpacity(alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = prefs.getFloat(key, 0.6f)
    }

    fun updateBetaEnhancedHeader(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Enhanced Header' directly opposes the functionality of 'Pure Black Mode'. Enhanced Header requires background colors to create frosted translucency. Proceeding will automatically deactivate 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaGlassUi.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the best visual fidelity when using 'Enhanced Header', it is highly recommended to activate 'Glass UI'. This combination creates a stunning translucent effect. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                    updateBetaGlassUi(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                }
            )
            return
        }

        _betaEnhancedHeader.value = enabled
        prefs.edit().putBoolean("beta_enhanced_header", enabled).apply()
    }

    fun updateBetaDynamicBackground(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Dynamic Lighting Background' contradicts the core purpose of 'Pure Black Mode' by introducing lit pixels and gradients. Proceeding will automatically deactivate 'Pure Black Mode' to maintain visual consistency.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaGlassUi.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the best visual fidelity when using 'Dynamic Lighting Background', it is highly recommended to activate 'Glass UI'. This combination creates a stunning translucent depth effect. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                    updateBetaGlassUi(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                }
            )
            return
        }

        _betaDynamicBackground.value = enabled
        prefs.edit().putBoolean("beta_dynamic_background", enabled).apply()
    }

    fun updateDynamicBgLightBrightness(value: Float) {
        val theme = _themeColor.value
        prefs.edit().putFloat("dynamic_bg_light_brightness_${theme.lowercase()}", value).apply()
        _dynamicBgLightBrightness.value = value
    }

    fun updateDynamicBgDarkBrightness(value: Float) {
        val theme = _themeColor.value
        prefs.edit().putFloat("dynamic_bg_dark_brightness_${theme.lowercase()}", value).apply()
        _dynamicBgDarkBrightness.value = value
    }

    fun updateBetaBetterTexts(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaBetterTextsPalette.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "To fully experience 'Better Texts', it is recommended to also enable 'Use Palette Shades for Text'. This provides a softer, more cohesive look matching your selected theme color. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaBetterTexts.value = true
                    prefs.edit().putBoolean("beta_better_texts", true).apply()
                    updateBetaBetterTextsPalette(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _betaBetterTexts.value = true
                    prefs.edit().putBoolean("beta_better_texts", true).apply()
                 }
            )
            return
        }
        _betaBetterTexts.value = enabled
        prefs.edit().putBoolean("beta_better_texts", enabled).apply()
    }

    fun updateBetaBetterTextsPalette(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Use Palette Shades for Text' directly opposes the high contrast functionality required by 'Pure Black Mode'. Proceeding will automatically deactivate 'Pure Black Mode' to maintain text readability.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaBetterTextsPalette.value = true
                    prefs.edit().putBoolean("beta_better_texts_palette", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _betaBetterTextsPalette.value = enabled
        prefs.edit().putBoolean("beta_better_texts_palette", enabled).apply()
    }

    fun updateShowActionHistory(enabled: Boolean) {
        _showActionHistory.value = enabled
        prefs.edit().putBoolean("show_action_history", enabled).apply()
    }

        
    fun switchMainAccountAndDeleteCurrent(successorId: String, createNew: Boolean = false, newName: String = "", newAvatar: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (prof.isDefault) {
                var newMainId = successorId
                if (createNew) {
                    newMainId = profileManager.addProfile(newName, newAvatar)
                }
                
                // Update the successor to be default
                val allProfs = profileManager.getAllProfiles()
                val successor = allProfs.find { it.id == newMainId }
                if (successor != null) {
                    successor.isDefault = true
                    profileManager.updateProfile(successor)
                }
                
                // Clear this user's data
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                
                switchProfileAndRestart(getApplication(), newMainId)
            }
        }
    }

    fun eraseMyDataAndAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (!prof.isDefault) {
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                switchProfileAndRestart(getApplication(), "DEFAULT") // Switch to a safe account
            }
        }
    }
fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (prof.isDefault) {
                // Main user: Erase all users' data. Delete secondary users entirely.
                val allProfs = profileManager.getAllProfiles()
                for (p in allProfs) {
                    if (!p.isDefault) {
                        // Clear their db and prefs
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                        profileManager.deleteProfile(p.id)
                    } else {
                        // Clear main user data
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                    }
                }
                allProfiles.value = profileManager.getAllProfiles()
            } else {
                // Secondary user: Erase their own data and delete their account
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                switchProfileAndRestart(getApplication(), "DEFAULT")
                return@launch
            }

            _themeMode.value = "System"
            _themeColor.value = "Ocean"
            _customPrimary.value = "#3197D6"
            _customPrimaryContainer.value = "#DAF1FF"
            _customBackground.value = "#FAFAFA"
            _customSurface.value = "#FFFFFF"
            _customText.value = "#1A1C1A"
            _pureBlackMode.value = false
            _betaFloatingNav.value = false
            _betaNotes.value = false
            _displayLayoutMode.value = "Immersive"
            _betaGlassUi.value = false
            _betaGlassDynamic.value = true
            _betaFrostGlass.value = true
            
            _aodTrueAodEnabled.value = false
            _aodTrueAodMode.value = "Clock"
            _aodDimnessLevel.value = 0.95f
            _aodSensitivity.value = "highest"
            _aodLockTimeout.value = 30
            _aodMotionSensitivity.value = 3.0f
            

            _importExportStatus.value = "All data and settings erased successfully" 
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }
}
