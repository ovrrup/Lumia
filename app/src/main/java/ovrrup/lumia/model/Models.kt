package ovrrup.lumia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val instructor: String,
    val schedule: String,
    val description: String,
    val subjectId: Int? = null,
    val tags: String = ""
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val tags: String = ""
)

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val tags: String = "",
    val chapterId: Int? = null,
    val isCompleted: Boolean = false
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subjectId: Int,
    val description: String = "",
    val tags: String = ""
)

@Entity(tableName = "assignments")
data class PracticeAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String,
    val dueDateMillis: Long,
    val category: String = "Homework",
    val categoryColor: String = "#3197D6",
    val tags: String = "",
    val subjectId: Int? = null,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long? = null,
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val tags: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 0
)

@Entity(tableName = "action_logs")
data class ActionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionText: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_fonts")
data class CustomFont(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fontName: String,
    val conditionTheme: String = "Any",
    val conditionMode: String = "Any",
    val isActive: Boolean = true
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long,
    val durationMinutes: Int,
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val taskId: Int? = null
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val dateMillis: Long,
    val courseId: Int? = null,
    val subjectId: Int? = null,
    val tag: String = ""
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val dateMillis: Long,
    val status: String
)
