package lumia.tracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "tag_customizations")
@JsonClass(generateAdapter = true)
data class TagCustomization(
    @PrimaryKey val tagName: String, // Normalized to lowercase
    val colorHex: String = "",
    val description: String = "",
    val isFavorite: Boolean = false,
    val lastUsedMillis: Long = System.currentTimeMillis()
) : Serializable
