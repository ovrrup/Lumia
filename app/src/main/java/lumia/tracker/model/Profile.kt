package lumia.tracker.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val name: String = "Main User",
    val avatarEmoji: String = "A",
    var isDefault: Boolean = false,
    var starterTheme: String = "",
    val alias: String = "",
    var createdAt: Long? = null,
    val avatarBase64: String? = null
)

