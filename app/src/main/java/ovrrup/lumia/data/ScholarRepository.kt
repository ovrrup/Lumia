package ovrrup.lumia.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import ovrrup.lumia.model.*

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val courses: List<Course> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val assignments: List<PracticeAssignment> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val customFonts: List<CustomFont> = emptyList(),
    val pomodoroSessions: List<PomodoroSession> = emptyList(),
    val notes: List<Note> = emptyList(),
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val actionLogs: List<ActionLog> = emptyList(),
    val settings: Map<String, String> = emptyMap()
)

class ScholarRepository(private val scholarDao: ScholarDao) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backupPayloadAdapter = moshi.adapter(BackupPayload::class.java)

    val allCourses: Flow<List<Course>> = scholarDao.getAllCourses()
    val allAssignments: Flow<List<PracticeAssignment>> = scholarDao.getAllAssignments()
    val allCustomFonts: Flow<List<CustomFont>> = scholarDao.getAllCustomFonts()
    val allActionLogs: Flow<List<ActionLog>> = scholarDao.getAllActionLogs()
    val allTasks: Flow<List<Task>> = scholarDao.getAllTasks()
    val allPomodoroSessions: Flow<List<PomodoroSession>> = scholarDao.getAllPomodoroSessions()
    val allNotes: Flow<List<Note>> = scholarDao.getAllNotes()
    val allSubjects: Flow<List<Subject>> = scholarDao.getAllSubjects()

    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>> = scholarDao.getTopicsForSubject(subjectId)
    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>> = scholarDao.getChaptersForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>> = scholarDao.getAssignmentsForCourse(courseId)
    fun getAttendanceForCourse(courseId: Int): Flow<List<AttendanceRecord>> = scholarDao.getAttendanceForCourse(courseId)

    // Inserts, updates, deletes
    suspend fun insertCustomFont(font: CustomFont) = scholarDao.insertCustomFont(font)
    suspend fun updateCustomFont(font: CustomFont) = scholarDao.updateCustomFont(font)
    suspend fun deleteCustomFont(font: CustomFont) = scholarDao.deleteCustomFont(font)

    suspend fun insertAttendanceRecord(record: AttendanceRecord) = scholarDao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = scholarDao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: AttendanceRecord) = scholarDao.deleteAttendanceRecord(record)

    suspend fun insertPomodoroSession(session: PomodoroSession) = scholarDao.insertPomodoroSession(session)

    suspend fun insertNote(note: Note) = scholarDao.insertNote(note)
    suspend fun updateNote(note: Note) = scholarDao.updateNote(note)
    suspend fun deleteNote(note: Note) = scholarDao.deleteNote(note)

    suspend fun insertCourse(course: Course) = scholarDao.insertCourse(course)
    suspend fun deleteCourse(course: Course) = scholarDao.deleteCourse(course)
    suspend fun updateCourse(course: Course) = scholarDao.updateCourse(course)

    suspend fun insertSubject(subject: Subject): Long = scholarDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = scholarDao.deleteSubject(subject)
    suspend fun updateSubject(subject: Subject) = scholarDao.updateSubject(subject)

    suspend fun insertTopic(topic: Topic) = scholarDao.insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = scholarDao.updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = scholarDao.deleteTopic(topic)

    suspend fun insertChapter(chapter: Chapter) = scholarDao.insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = scholarDao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: Chapter) = scholarDao.deleteChapter(chapter)

    suspend fun insertTask(task: Task): Long = scholarDao.insertTask(task)
    suspend fun updateTask(task: Task) = scholarDao.updateTask(task)
    suspend fun updateTasks(tasks: List<Task>) = scholarDao.updateTasks(tasks)
    suspend fun deleteTask(task: Task) = scholarDao.deleteTask(task)

    suspend fun insertAssignment(assignment: PracticeAssignment): Long = scholarDao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: PracticeAssignment) = scholarDao.updateAssignment(assignment)
    suspend fun updateAssignments(assignments: List<PracticeAssignment>) = scholarDao.updateAssignments(assignments)
    suspend fun deleteAssignment(assignment: PracticeAssignment) = scholarDao.deleteAssignment(assignment)

    suspend fun insertActionLog(log: ActionLog) = scholarDao.insertActionLog(log)

    suspend fun clearActionLogs() = scholarDao.clearActionLogs()

    suspend fun clearAllData() {
        scholarDao.clearCourses()
        scholarDao.clearAssignments()
        scholarDao.clearCustomFonts()
        scholarDao.clearActionLogs()
        scholarDao.clearTasks()
        scholarDao.clearPomodoroSessions()
        scholarDao.clearNotes()
        scholarDao.clearSubjects()
        scholarDao.clearTopics()
        scholarDao.clearChapters()
        scholarDao.clearAttendanceRecords()
    }

    suspend fun exportDataToStream(os: OutputStream, settings: Map<String, String>) {
        val payload = BackupPayload(
            courses = scholarDao.getAllCourses().first(),
            subjects = scholarDao.getAllSubjects().first(),
            topics = scholarDao.getAllTopics().first(),
            chapters = scholarDao.getAllChapters().first(),
            customFonts = scholarDao.getAllCustomFonts().first(),
            pomodoroSessions = scholarDao.getAllPomodoroSessions().first(),
            notes = scholarDao.getAllNotes().first(),
            attendanceRecords = scholarDao.getAllAttendanceRecords().first(),
            actionLogs = scholarDao.getAllActionLogs().first(),
            tasks = scholarDao.getAllTasks().first(),
            assignments = scholarDao.getAllAssignments().first(),
            settings = settings
        )
        val json = backupPayloadAdapter.toJson(payload)
        os.write(json.toByteArray(Charsets.UTF_8))
    }

    suspend fun importDataFromStream(ins: InputStream): Map<String, String> {
        val bytes = ins.readBytes()
        val json = String(bytes, Charsets.UTF_8)
        val payload = backupPayloadAdapter.fromJson(json) ?: throw Exception("Failed to parse backup format")
        
        clearAllData()
        
        // Restore DB records
        payload.courses.forEach { scholarDao.insertCourse(it) }
        payload.subjects.forEach { scholarDao.insertSubject(it) }
        payload.topics.forEach { scholarDao.insertTopic(it) }
        payload.chapters.forEach { scholarDao.insertChapter(it) }
        payload.assignments.forEach { scholarDao.insertAssignment(it) }
        payload.tasks.forEach { scholarDao.insertTask(it) }
        payload.customFonts.forEach { scholarDao.insertCustomFont(it) }
        payload.pomodoroSessions.forEach { scholarDao.insertPomodoroSession(it) }
        payload.notes.forEach { scholarDao.insertNote(it) }
        payload.attendanceRecords.forEach { scholarDao.insertAttendanceRecord(it) }
        payload.actionLogs.forEach { scholarDao.insertActionLog(it) }

        return payload.settings
    }
}
