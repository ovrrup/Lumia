package lumia.tracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

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

@Entity(tableName = "attachments")
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
    val flashcards: List<Flashcard>? = emptyList(),
    val tagCustomizations: List<TagCustomization>? = emptyList(),
    val profile: UserProfile? = null,
    val isFullAppBackup: Boolean? = false,
    val fullAppBackupJson: String? = null,
    val attachmentFiles: Map<String, String>? = emptyMap()
) : Serializable

@JsonClass(generateAdapter = true)
data class FullAppBackup(
    val version: Int? = 1,
    val profiles: List<UserProfile>? = emptyList(),
    val activeProfileId: String? = "",
    val globalPrefs: Map<String, String>? = emptyMap(),
    val profileBackupsJson: Map<String, String>? = emptyMap()
)
