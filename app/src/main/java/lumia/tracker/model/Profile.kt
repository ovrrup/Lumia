package lumia.tracker.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    var points: Int = 0,
    var level: Int = 1,
    var unlockedAchievements: List<String> = emptyList(),
    var isDefault: Boolean = false,
    var unlockedFeatures: List<String> = emptyList(),
    var starterTheme: String = ""
)

@JsonClass(generateAdapter = true)
data class Achievement(
    val id: String,
    val name: String,
    val requiredType: String, // "POINTS", "TASKS", "SESSIONS"
    val requiredValue: Int,
    val iconId: String
)
