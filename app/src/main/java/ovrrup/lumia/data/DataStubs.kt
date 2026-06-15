package ovrrup.lumia.data

import android.content.Context
import ovrrup.lumia.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AppDatabase {
    companion object {
        fun getDatabase(c: Context): AppDatabase = AppDatabase()
    }
    lateinit var openHelper: androidx.sqlite.db.SupportSQLiteOpenHelper
    fun scholarDao(): Any? = null
}

class ScholarRepository(vararg args: Any?) {
    val allCourses: Flow<List<Course>> = emptyFlow()
    val allAssignments: Flow<List<PracticeAssignment>> = emptyFlow()
    val allCustomFonts: Flow<List<CustomFont>> = emptyFlow()
    val allActionLogs: Flow<List<ActionLog>> = emptyFlow()
    val allTasks: Flow<List<Task>> = emptyFlow()
    val allPomodoroSessions: Flow<List<PomodoroSession>> = emptyFlow()
    val allNotes: Flow<List<Note>> = emptyFlow()
    val allSubjects: Flow<List<Subject>> = emptyFlow()

    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>> = emptyFlow()
    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>> = emptyFlow()
    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>> = emptyFlow()
    fun getAttendanceForCourse(courseId: Int): Flow<List<AttendanceRecord>> = emptyFlow()

    suspend fun insertCustomFont(font: CustomFont): Long = 0L
    suspend fun updateCustomFont(font: CustomFont) {}
    suspend fun deleteCustomFont(font: CustomFont) {}

    suspend fun insertAttendanceRecord(record: AttendanceRecord): Long = 0L
    suspend fun updateAttendanceRecord(record: AttendanceRecord) {}
    suspend fun deleteAttendanceRecord(record: AttendanceRecord) {}

    suspend fun insertPomodoroSession(session: PomodoroSession): Long = 0L
    suspend fun insertNote(note: Note): Long = 0L
    suspend fun updateNote(note: Note) {}
    suspend fun deleteNote(note: Note) {}

    suspend fun insertSubject(subject: Subject): Long = 0L
    suspend fun updateSubject(subject: Subject) {}
    suspend fun deleteSubject(subject: Subject) {}

    suspend fun insertCourse(course: Course): Long = 0L
    suspend fun updateCourse(course: Course) {}
    suspend fun deleteCourse(course: Course) {}

    suspend fun insertTopic(topic: Topic): Long = 0L
    suspend fun updateTopic(topic: Topic) {}
    suspend fun deleteTopic(topic: Topic) {}

    suspend fun insertChapter(chapter: Chapter): Long = 0L
    suspend fun updateChapter(chapter: Chapter) {}
    suspend fun deleteChapter(chapter: Chapter) {}

    suspend fun insertTask(task: Task): Long = 0L
    suspend fun updateTask(task: Task) {}
    suspend fun updateTasks(tasks: List<Task>) {}
    suspend fun deleteTask(task: Task) {}

    suspend fun insertAssignment(assignment: PracticeAssignment): Long = 0L
    suspend fun updateAssignment(assignment: PracticeAssignment) {}
    suspend fun updateAssignments(assignments: List<PracticeAssignment>) {}
    suspend fun deleteAssignment(assignment: PracticeAssignment) {}

    suspend fun insertActionLog(log: ActionLog): Long = 0L
    suspend fun clearAllData() {}
    suspend fun clearActionLogs() {}
    
    suspend fun exportDataToStream(vararg args: Any?) {}
    suspend fun importDataFromStream(vararg args: Any?): Map<String, String>? = null
}
