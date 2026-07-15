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
    val tags: String = ""
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

// For Export/Import
@Entity(
    tableName = "chapters",
    foreignKeys = [ForeignKey(entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("subjectId")]
)
@JsonClass(generateAdapter = true)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val tags: String = ""
) : Serializable

@Entity(tableName = "tasks")
@JsonClass(generateAdapter = true)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long? = null,
    val isCompleted: Boolean = false,
    
    val subjectId: Int? = null,
    val chapterId: Int? = null,
    val topicId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val classDateMillis: Long? = null,
    
    val priority: Int = 0,
    val orderIndex: Int = 0,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "attachments"
)
@JsonClass(generateAdapter = true)
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String,
    val fileType: String,
    val sizeBytes: Long = 0,
    val courseId: Int? = null,
    val subjectId: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
) : Serializable

@JsonClass(generateAdapter = true)
data class ScholarBackup(
    val courses: List<Course>? = emptyList(),
    val subjects: List<Subject>? = emptyList(),
    val topics: List<Topic>? = emptyList(),
    val assignments: List<PracticeAssignment>? = emptyList(),
    val settings: Map<String, String>? = null,
    val attendance: List<AttendanceRecord>? = emptyList(),
    val pomodoro: List<PomodoroSession>? = emptyList(),
    val actionLogs: List<ActionLog>? = emptyList(),
    val notes: List<Note>? = emptyList(),
    val chapters: List<Chapter>? = emptyList(),
    val tasks: List<Task>? = emptyList(),
    val attachments: List<Attachment>? = emptyList(),
    val testRecords: List<TestRecord>? = emptyList(),
    val tagCustomizations: List<TagCustomization>? = emptyList(),
    val profile: UserProfile? = null
    , val isFullAppBackup: Boolean? = false
    , val fullAppBackupJson: String? = null
) : Serializable

@JsonClass(generateAdapter = true)
data class FullAppBackup(
    val version: Int? = 1,
    val profiles: List<UserProfile>? = emptyList(),
    val activeProfileId: String? = "",
    val globalPrefs: Map<String, String>? = emptyMap(),
    val profileBackupsJson: Map<String, String>? = emptyMap() // Map of ProfileID -> ScholarBackup JSON
)
