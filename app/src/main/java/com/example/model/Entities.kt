package com.example.model

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
    val instructor: String = "",
    val schedule: String = "",
    val description: String = "",
    val attendedClasses: Int = 0,
    val totalClasses: Int = 0
) : Serializable

@Entity(tableName = "subjects")
@JsonClass(generateAdapter = true)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetHours: Int = 0
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
    val isCompleted: Boolean = false
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
    val categoryColor: String = "#3197D6"
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
    val durationMinutes: Int
) : Serializable

// For Export/Import
@JsonClass(generateAdapter = true)
data class ScholarBackup(
    val courses: List<Course>,
    val subjects: List<Subject>,
    val topics: List<Topic>,
    val assignments: List<PracticeAssignment>,
    val settings: Map<String, String>? = null,
    val attendance: List<AttendanceRecord>? = emptyList(),
    val pomodoro: List<PomodoroSession>? = emptyList(),
    val actionLogs: List<ActionLog>? = emptyList()
) : Serializable
