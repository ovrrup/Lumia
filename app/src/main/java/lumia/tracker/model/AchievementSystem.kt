package lumia.tracker.model

import android.content.Context
import android.content.SharedPreferences

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val requiredType: String, // e.g. "POINTS", "TASKS", "SESSIONS", "STREAK"
    val requiredValue: Int,
    val iconEmoji: String
)

object AchievementSystem {
    val ACHIEVEMENTS = listOf(
        // Points based
        AchievementDef("P_100", "Apprentice", "Reach 100 points.", "POINTS", 100, "Novice"),
        AchievementDef("P_500", "Scholar", "Reach 500 points.", "POINTS", 500, "Scroll"),
        AchievementDef("P_1000", "Master", "Reach 1,000 points.", "POINTS", 1000, "Cap"),
        AchievementDef("P_5000", "Grandmaster", "Reach 5,000 points.", "POINTS", 5000, "Crown"),
        AchievementDef("P_10000", "Legend", "Reach 10,000 points.", "POINTS", 10000, "Star"),
        // Tasks based
        AchievementDef("T_1", "First Step", "Complete your first task.", "TASKS", 1, "Check"),
        AchievementDef("T_10", "Task Doer", "Complete 10 tasks.", "TASKS", 10, "Check"),
        AchievementDef("T_50", "Task Manager", "Complete 50 tasks.", "TASKS", 50, "Check"),
        AchievementDef("T_100", "Task Master", "Complete 100 tasks.", "TASKS", 100, "Check"),
        AchievementDef("T_500", "Task Overlord", "Complete 500 tasks.", "TASKS", 500, "Check"),
        // Pomodoro based
        AchievementDef("S_1", "Focus Novice", "Complete your first focus session.", "SESSIONS", 1, "Timer"),
        AchievementDef("S_10", "Focus Regular", "Complete 10 focus sessions.", "SESSIONS", 10, "Timer"),
        AchievementDef("S_50", "Focus Expert", "Complete 50 focus sessions.", "SESSIONS", 50, "Timer"),
        AchievementDef("S_100", "Focus Master", "Complete 100 focus sessions.", "SESSIONS", 100, "Timer"),
        AchievementDef("S_500", "Focus Legend", "Complete 500 focus sessions.", "SESSIONS", 500, "Timer"),
        // Streak based
        AchievementDef("ST_3", "Warming Up", "Reach a 3-day streak.", "STREAK", 3, "Fire"),
        AchievementDef("ST_7", "Weekly Warrior", "Reach a 7-day streak.", "STREAK", 7, "Fire"),
        AchievementDef("ST_30", "Monthly Master", "Reach a 30-day streak.", "STREAK", 30, "Fire"),
        AchievementDef("ST_100", "Unstoppable", "Reach a 100-day streak.", "STREAK", 100, "Fire"),
        AchievementDef("ST_365", "A Year of Focus", "Reach a 365-day streak.", "STREAK", 365, "Fire"),
        // Add more to reach 60...
        AchievementDef("P_200", "Learner II", "Reach 200 points.", "POINTS", 200, "Book"),
        AchievementDef("P_300", "Learner III", "Reach 300 points.", "POINTS", 300, "Book"),
        AchievementDef("P_400", "Learner IV", "Reach 400 points.", "POINTS", 400, "Book"),
        AchievementDef("P_600", "Scholar II", "Reach 600 points.", "POINTS", 600, "Scroll"),
        AchievementDef("P_700", "Scholar III", "Reach 700 points.", "POINTS", 700, "Scroll"),
        AchievementDef("P_800", "Scholar IV", "Reach 800 points.", "POINTS", 800, "Scroll"),
        AchievementDef("P_900", "Scholar V", "Reach 900 points.", "POINTS", 900, "Scroll"),
        AchievementDef("P_1500", "Master II", "Reach 1,500 points.", "POINTS", 1500, "Cap"),
        AchievementDef("P_2000", "Master III", "Reach 2,000 points.", "POINTS", 2000, "Cap"),
        AchievementDef("P_2500", "Master IV", "Reach 2,500 points.", "POINTS", 2500, "Cap"),
        AchievementDef("P_3000", "Master V", "Reach 3,000 points.", "POINTS", 3000, "Cap"),
        AchievementDef("P_4000", "Master VI", "Reach 4,000 points.", "POINTS", 4000, "Cap"),
        AchievementDef("T_5", "Task Beginner", "Complete 5 tasks.", "TASKS", 5, "Check"),
        AchievementDef("T_25", "Task Apprentice", "Complete 25 tasks.", "TASKS", 25, "Check"),
        AchievementDef("T_75", "Task Pro", "Complete 75 tasks.", "TASKS", 75, "Check"),
        AchievementDef("T_150", "Task Veteran", "Complete 150 tasks.", "TASKS", 150, "Check"),
        AchievementDef("T_200", "Task Hero", "Complete 200 tasks.", "TASKS", 200, "Check"),
        AchievementDef("T_250", "Task Legend", "Complete 250 tasks.", "TASKS", 250, "Check"),
        AchievementDef("T_300", "Task Mythic", "Complete 300 tasks.", "TASKS", 300, "Check"),
        AchievementDef("T_400", "Task God", "Complete 400 tasks.", "TASKS", 400, "Check"),
        AchievementDef("S_5", "Focus Beginner", "Complete 5 focus sessions.", "SESSIONS", 5, "Timer"),
        AchievementDef("S_25", "Focus Apprentice", "Complete 25 focus sessions.", "SESSIONS", 25, "Timer"),
        AchievementDef("S_75", "Focus Pro", "Complete 75 focus sessions.", "SESSIONS", 75, "Timer"),
        AchievementDef("S_150", "Focus Veteran", "Complete 150 focus sessions.", "SESSIONS", 150, "Timer"),
        AchievementDef("S_200", "Focus Hero", "Complete 200 focus sessions.", "SESSIONS", 200, "Timer"),
        AchievementDef("S_250", "Focus Legend", "Complete 250 focus sessions.", "SESSIONS", 250, "Timer"),
        AchievementDef("S_300", "Focus Mythic", "Complete 300 focus sessions.", "SESSIONS", 300, "Timer"),
        AchievementDef("S_400", "Focus God", "Complete 400 focus sessions.", "SESSIONS", 400, "Timer"),
        AchievementDef("ST_5", "Five Days", "Reach a 5-day streak.", "STREAK", 5, "Fire"),
        AchievementDef("ST_10", "Ten Days", "Reach a 10-day streak.", "STREAK", 10, "Fire"),
        AchievementDef("ST_14", "Fortnight", "Reach a 14-day streak.", "STREAK", 14, "Fire"),
        AchievementDef("ST_21", "Three Weeks", "Reach a 21-day streak.", "STREAK", 21, "Fire"),
        AchievementDef("ST_50", "Fifty Days", "Reach a 50-day streak.", "STREAK", 50, "Fire"),
        AchievementDef("ST_75", "Seventy-Five Days", "Reach a 75-day streak.", "STREAK", 75, "Fire"),
        AchievementDef("ST_150", "One Hundred Fifty Days", "Reach a 150-day streak.", "STREAK", 150, "Fire"),
        AchievementDef("ST_200", "Two Hundred Days", "Reach a 200-day streak.", "STREAK", 200, "Fire"),
        AchievementDef("ST_250", "Two Hundred Fifty Days", "Reach a 250-day streak.", "STREAK", 250, "Fire"),
        AchievementDef("ST_300", "Three Hundred Days", "Reach a 300-day streak.", "STREAK", 300, "Fire"),
        AchievementDef("P_15000", "Galactic", "Reach 15,000 points.", "POINTS", 15000, "Star"),
        AchievementDef("P_20000", "Universal", "Reach 20,000 points.", "POINTS", 20000, "Star")
    )
    
    fun evaluateAchievements(
        profile: UserProfile,
        totalTasks: Int,
        totalSessions: Int,
        currentStreak: Int
    ): List<AchievementDef> {
        val newlyUnlocked = mutableListOf<AchievementDef>()
        
        ACHIEVEMENTS.forEach { achievement ->
            if (!profile.unlockedAchievements.contains(achievement.id)) {
                val conditionMet = when (achievement.requiredType) {
                    "POINTS" -> profile.points >= achievement.requiredValue
                    "TASKS" -> totalTasks >= achievement.requiredValue
                    "SESSIONS" -> totalSessions >= achievement.requiredValue
                    "STREAK" -> currentStreak >= achievement.requiredValue
                    else -> false
                }
                if (conditionMet) {
                    newlyUnlocked.add(achievement)
                }
            }
        }
        
        return newlyUnlocked
    }
}
