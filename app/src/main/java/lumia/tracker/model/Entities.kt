package lumia.tracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "courses")
@JsonClass(generateAdapter = true)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String = "",
    val colorHex: String = "#3197D6",
    val scheduleDays: String = "",
    val scheduleStartTime: String = "",
    val scheduleEndTime: String = "",
    val instructor: String = "",
    val schedule: String = "",
    val description: String = "",
    val attendedClasses: Int = 0,
    val totalClasses: Int = 0,
    val subjectId: Int? = null,
    val tags: String = "",
    val subjectIds: String = ""
) : Serializable

@Entity(tableName = "test_records")
@JsonClass(generateAdapter = true)
data class TestRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val marksObtained: Float = 0f,
    val totalMarks: Float = 100f,
    val notes: String = "",
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val tags: String = "",
    val topicId: Int? = null
) : Serializable

@Entity(tableName = "subjects")
@JsonClass(generateAdapter = true)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val tags: String = ""
) : Serializable

@Entity(
    tableName = "topics",
    foreignKeys = [ForeignKey(entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("subjectId")]
)
@JsonClass(generateAdapter = true)
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val chapterId: Int? = null,
    val tags: String = "",
    val weightage: Float = 1.0f,
    val difficultyRating: Int = 3,
    val linkedFilePath: String = "",
    val unitName: String = ""
) : Serializable

@Entity(tableName = "flashcards")
@JsonClass(generateAdapter = true)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int? = null,
    val topicId: Int? = null,
    val question: String,
    val answer: String,
    val box: Int = 1,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 1,
    val nextReviewMillis: Long = System.currentTimeMillis(),
    val retentionScore: Float = 0f
) : Serializable

@Entity(
    tableName = "assignments",
    foreignKeys = [ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("courseId")]
)
@JsonClass(generateAdapter = true)
data class PracticeAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long = 0,
    val isCompleted: Boolean = false,
    val category: String = "Homework",
    val categoryColor: String = "#3197D6",
    val tags: String = "",
    val subjectId: Int? = null,
    val priority: Int = 0,
    val orderIndex: Int = 0
) : Serializable

@Entity(
    tableName = "attendance_records",
    foreignKeys = [ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("courseId")]
)
@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val dateMillis: Long, // Start of the day in millis
    val status: String // e.g., "Present", "Absent", "Cancelled", "Late", "Holiday"
) : Serializable

@Entity(tableName = "action_logs")
@JsonClass(generateAdapter = true)
data class ActionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionText: String,
    val timestampMillis: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "pomodoro_sessions")
@JsonClass(generateAdapter = true)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long, // timestamp
    val durationMinutes: Int,
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val taskId: Int? = null,
    val topicId: Int? = null
) : Serializable

@Entity(tableName = "notes")
@JsonClass(generateAdapter = true)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val dateMillis: Long,
    val courseId: Int? = null,
    val subjectId: Int? = null,
    val tag: String = ""
) : Serializable
