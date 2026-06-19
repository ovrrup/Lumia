package lumia.tracker.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    var points: Int = 0,
    var level: Int = 0,
    var unlockedAchievements: List<String> = emptyList(),
    var isDefault: Boolean = false,
    var unlockedFeatures: List<String> = emptyList(),
    var starterTheme: String = "",
    val alias: String = "",
    var selectedBadge: String = "",
    var credits: Int = 250, // default starter credits
    var rentedFeatures: Map<String, Long> = emptyMap(), // map of feature ID to expiration epoch millis
    var experience: Int = 0,
    var pendingSurpriseBoxes: Int = 0
) {
    fun isFeatureUnlocked(featureId: String): Boolean {
        if (unlockedFeatures.contains(featureId)) return true
        val expiry = rentedFeatures[featureId] ?: 0L
        return expiry > System.currentTimeMillis()
    }
}

@JsonClass(generateAdapter = true)
data class Achievement(
    val id: String,
    val name: String,
    val requiredType: String, // "POINTS", "TASKS", "SESSIONS"
    val requiredValue: Int,
    val iconId: String
)
