package lumia.tracker.kost

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "kost_behavior_events")
@JsonClass(generateAdapter = true)
data class KostBehaviorEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // ROUTINE, PLAN, ASSIGNMENT, TEST, POMODORO, ATTENDANCE, GENERAL
    val action: String, // CREATE, COMPLETE, DELETE, UPDATE, SCORE, DEVIATE
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long? = null,
    val rating: Float? = null, // Custom user rating or priority index
    val performanceMetric: Float? = null, // e.g. score percentage, attendance rate
    val tagString: String = "", // Combined tags
    val description: String = "", // Detailed human text description
    val metadataJson: String = "{}" // Dynamic JSON payload containing detailed contextual data
) : Serializable

@Entity(tableName = "kost_pattern_reports")
@JsonClass(generateAdapter = true)
data class KostPatternReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val generatedAt: Long = System.currentTimeMillis(),
    val summary: String, // Brief text overview of key insights
    val insightsJson: String, // Structured JSON containing all noticed patterns and correlation reports
    val modelAccuracyMetric: Float = 1.0f,
    val actionPlanSuggestions: String = "" // Generated actionable plan recommendations
) : Serializable
