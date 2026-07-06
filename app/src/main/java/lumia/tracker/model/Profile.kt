package lumia.tracker.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    var isDefault: Boolean = false,
    var starterTheme: String = "",
    val alias: String = "",
    var createdAt: Long? = null,
    val avatarBase64: String? = null
)

