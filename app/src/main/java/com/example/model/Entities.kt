package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "courses")
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
data class PracticeAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long = 0,
    val isCompleted: Boolean = false
) : Serializable

@Entity(
    tableName = "attendance_records",
    foreignKeys = [ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("courseId")]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val dateMillis: Long, // Start of the day in millis
    val status: String // e.g., "Present", "Absent", "Cancelled", "Late", "Holiday"
) : Serializable

@Entity(tableName = "action_logs")
data class ActionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionText: String,
    val timestampMillis: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long, // timestamp
    val durationMinutes: Int
) : Serializable

// For Export/Import
data class ScholarBackup(
    val courses: List<Course>,
    val subjects: List<Subject>,
    val topics: List<Topic>,
    val assignments: List<PracticeAssignment>,
    val settings: Map<String, String>? = null
) : Serializable
