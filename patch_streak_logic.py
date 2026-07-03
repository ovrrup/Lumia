import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

# Add new StateFlows
new_states = """    private val _streakTotalNormal = MutableStateFlow(prefs.getInt("streak_total_normal", 0))
    val streakTotalNormal = _streakTotalNormal.asStateFlow()

    private val _streakTotalComplete = MutableStateFlow(prefs.getInt("streak_total_complete", 0))
    val streakTotalComplete = _streakTotalComplete.asStateFlow()

    private val _streakIsCompleteToday = MutableStateFlow(false)
    val streakIsCompleteToday = _streakIsCompleteToday.asStateFlow()

"""

idx = content.find("private val _streakPercentage")
if idx != -1:
    content = content[:idx] + new_states + content[idx:]
else:
    print("Could not find _streakPercentage")

old_calc = """                // Check if streak applies
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
                }"""

new_calc = """                // Check if streak applies
                val lastStreakDate = prefs.getLong("streak_last_date", 0L)
                val threshold = _streakPartialThreshold.value
                
                val isComplete = (percentage >= threshold) && 
                                 (plannedTasks == 0 || doneTasks >= plannedTasks) && 
                                 (plannedAssignments == 0 || doneAssignments >= plannedAssignments) && 
                                 (percentage >= 1.0f)
                _streakIsCompleteToday.value = isComplete

                val todayStr = todayDateString()
                val statusToday = prefs.getString("streak_status_$todayStr", "none")

                if (percentage >= threshold && statusToday != "complete") {
                    
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
                    }
                } else if (percentage < threshold && lastStreakDate < todayStart) {
                    // Reset if yesterday was missed
                    val yesterday = todayStart - 86400000L
                    if (lastStreakDate < yesterday) {
                        _streakCurrent.value = 0
                        prefs.edit().putInt("streak_current", 0).apply()
                    }
                }"""

content = content.replace(old_calc, new_calc)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)

