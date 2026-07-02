import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

streak_props = """
    private val _streakPercentage = MutableStateFlow(0f)
    val streakPercentage = _streakPercentage.asStateFlow()

    private val _streakCurrent = MutableStateFlow(prefs.getInt("streak_current", 0))
    val streakCurrent = _streakCurrent.asStateFlow()

    private val _streakLongest = MutableStateFlow(prefs.getInt("streak_longest", 0))
    val streakLongest = _streakLongest.asStateFlow()

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
            
            val doneTasks = tasksToday.count { it.isCompleted }
            val doneAssignments = assignmentsToday.count { it.isCompleted }
            val donePomos = pomosToday.sumOf { it.durationMinutes }
            
            var totalRequired = 0f
            var totalDone = 0f
            
            if (requiredTasks > 0) { totalRequired += 1f; totalDone += doneTasks.toFloat() / requiredTasks.toFloat() }
            if (requiredAssignments > 0) { totalRequired += 1f; totalDone += doneAssignments.toFloat() / requiredAssignments.toFloat() }
            if (requiredPomos > 0) { totalRequired += 1f; totalDone += donePomos.toFloat() / requiredPomos.toFloat() }
            
            val percentage = if (totalRequired == 0f) 0f else (totalDone / totalRequired).coerceIn(0f, 1f)
            
            withContext(Dispatchers.Main) {
                _streakPercentage.value = percentage
                
                // Check if streak applies
                val lastStreakDate = prefs.getLong("streak_last_date", 0L)
                val threshold = _streakPartialThreshold.value
                
                if (percentage >= threshold && lastStreakDate < todayStart) {
                    // Update streak
                    val isConsecutive = (todayStart - lastStreakDate) <= 86400000L * 2
                    val newCurrent = if (isConsecutive) _streakCurrent.value + 1 else 1
                    
                    _streakCurrent.value = newCurrent
                    if (newCurrent > _streakLongest.value) {
                        _streakLongest.value = newCurrent
                        prefs.edit().putInt("streak_longest", newCurrent).apply()
                    }
                    
                    prefs.edit()
                        .putInt("streak_current", newCurrent)
                        .putLong("streak_last_date", todayStart)
                        .apply()
                } else if (percentage < threshold && lastStreakDate < todayStart) {
                    // Reset if yesterday was missed
                    val yesterday = todayStart - 86400000L
                    if (lastStreakDate < yesterday) {
                        _streakCurrent.value = 0
                        prefs.edit().putInt("streak_current", 0).apply()
                    }
                }
            }
        }
    }
"""

content = content.replace("private val _themeMode =", streak_props + "\n    private val _themeMode =")

# Call calculateTodayStreakProgress in init block or when tasks/assignments change. 
# For simplicity, we can call it inside verifyFeatureEntitlements or just periodically.
# Let's add it right after verifyFeatureEntitlements() in init block
content = content.replace("verifyFeatureEntitlements()", "verifyFeatureEntitlements()\n        calculateTodayStreakProgress()")

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
