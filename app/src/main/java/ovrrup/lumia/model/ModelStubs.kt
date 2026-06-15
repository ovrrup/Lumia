package ovrrup.lumia.model

data class Course(val id: Int = 0, val name: String = "", val color: Long = 0L, val instructor: String = "", val schedule: String = "", val description: String = "", val subjectId: Int? = null, val tags: String = "")
data class PracticeAssignment(val id: Int = 0, val courseId: Int = 0, val title: String = "", val isCompleted: Boolean = false, val dueDateMillis: Long = 0L, val description: String = "", val category: String = "", val categoryColor: String = "", val tags: String = "", val subjectId: Int? = null)
data class Subject(val id: Int = 0, val name: String = "", val tags: String = "")
data class Topic(val id: Int = 0, val subjectId: Int = 0, val title: String = "", val isCompleted: Boolean = false, val tags: String = "", val chapterId: Int? = null)
data class ActionLog(val id: Int = 0, val timestampMillis: Long = 0, val actionText: String = "")
data class Chapter(val id: Int = 0, val subjectId: Int = 0, val name: String = "", val description: String = "", val tags: String = "")
data class Task(val id: Int = 0, val subjectId: Int? = null, val courseId: Int? = null, val assignmentId: Int? = null, val title: String = "", val description: String = "", val dueDateMillis: Long = 0, val isCompleted: Boolean = false, val tags: String = "")
data class PomodoroSession(val id: Int = 0, val dateMillis: Long = 0, val durationMinutes: Int = 0, val subjectId: Int? = null, val courseId: Int? = null, val assignmentId: Int? = null, val taskId: Int? = null)
data class AttendanceRecord(val id: Int = 0, val courseId: Int = 0, val isPresent: Boolean = false, val dateMillis: Long = 0, val status: String = "")
data class Note(val id: Int = 0, val title: String = "", val content: String = "", val dateMillis: Long = 0, val courseId: Int? = null, val subjectId: Int? = null, val tag: String = "")
data class CustomFont(val id: Int = 0, val name: String = "", val fontName: String = "", val conditionTheme: String = "", val conditionMode: String = "", val isActive: Boolean = false)
