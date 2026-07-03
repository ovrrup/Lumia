import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                if (percentage >= threshold && statusToday != "complete") {
                    
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

new_logic = """                if (percentage >= threshold) {
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
                }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
print("Fixed streak downgrade logic!")
