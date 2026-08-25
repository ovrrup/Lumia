package lumia.tracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Attachment
import lumia.tracker.model.Chapter
import lumia.tracker.model.Course
import lumia.tracker.model.Note
import lumia.tracker.model.PomodoroSession
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.ScholarBackup
import lumia.tracker.model.Subject
import lumia.tracker.model.Task
import lumia.tracker.model.TestRecord
import lumia.tracker.model.Topic
import java.io.InputStream
import java.io.OutputStream

class ScholarRepository(val dao: ScholarDao) {

    val allCourses: Flow<List<Course>> = dao.getAllCourses()
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allAssignments: Flow<List<PracticeAssignment>> = dao.getAllAssignments()
    val allActionLogs: Flow<List<ActionLog>> = dao.getAllActionLogs()
    val allPomodoroSessions: Flow<List<PomodoroSession>> = dao.getAllPomodoroSessions()
    val allNotes: Flow<List<Note>> = dao.getAllNotes()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val allChapters: Flow<List<Chapter>> = dao.getAllChaptersFlow()
    val allAttachments: Flow<List<Attachment>> = dao.getAllAttachments()
    val allTopics: Flow<List<Topic>> = dao.getAllTopicsReactive()
    val allAttendanceRecords: Flow<List<lumia.tracker.model.AttendanceRecord>> = dao.getAllAttendanceRecords()
    val allTestRecords: Flow<List<TestRecord>> = dao.getAllTestRecordsReactive()

    fun getTopicsForSubject(subjectId: Int) = dao.getTopicsForSubject(subjectId)
    fun getChaptersForSubject(subjectId: Int) = dao.getChaptersForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int) = dao.getAssignmentsForCourse(courseId)
    fun getAttachmentsForCourse(courseId: Int) = dao.getAttachmentsForCourse(courseId)
    fun getAttachmentsForSubject(subjectId: Int) = dao.getAttachmentsForSubject(subjectId)
    fun getAttendanceForCourse(courseId: Int) = dao.getAttendanceForCourse(courseId)
    fun getTestRecordsForCourse(courseId: Int) = dao.getTestRecordsForCourse(courseId)
    fun getTestRecordsForSubject(subjectId: Int) = dao.getTestRecordsForSubject(subjectId)

    suspend fun insertAttachment(attachment: Attachment) = dao.insertAttachment(attachment)
    suspend fun deleteAttachment(attachment: Attachment) = dao.deleteAttachment(attachment)

    suspend fun insertChapter(chapter: Chapter) = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: Chapter) = dao.deleteChapter(chapter)

    suspend fun insertTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun updateTasks(tasks: List<Task>) { tasks.forEach { dao.updateTask(it) } }
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun insertCourse(course: Course) = dao.insertCourse(course)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    suspend fun insertSubject(subject: Subject) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    suspend fun insertTopic(topic: Topic) = dao.insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = dao.deleteTopic(topic)

    suspend fun insertAssignment(assignment: PracticeAssignment): Long = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: PracticeAssignment) = dao.updateAssignment(assignment)
    suspend fun updateAssignments(assignments: List<PracticeAssignment>) { assignments.forEach { dao.updateAssignment(it) } }
    suspend fun deleteAssignment(assignment: PracticeAssignment) = dao.deleteAssignment(assignment)
    
    suspend fun insertAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.deleteAttendanceRecord(record)
    
    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()
    suspend fun insertPomodoroSession(session: PomodoroSession) = dao.insertPomodoroSession(session)
    
    suspend fun insertNote(note: Note) = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    suspend fun insertTestRecord(record: TestRecord) = dao.insertTestRecord(record)
    suspend fun updateTestRecord(record: TestRecord) = dao.updateTestRecord(record)
    suspend fun deleteTestRecord(record: TestRecord) = dao.deleteTestRecord(record)

    suspend fun clearAllData() {
        dao.clearCourses()
        dao.clearSubjects()
        dao.clearChapters()
        dao.clearTopics()
        dao.clearAssignments()
        dao.clearAttendance()
        dao.clearPomodoro()
        dao.clearActionLogs()
        dao.clearNotes()
        dao.clearTasks()
        dao.clearAttachments()
        dao.clearTestRecords()
        dao.clearTagCustomizations()
    }

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(ScholarBackup::class.java)

    suspend fun exportDataToStream(
        outputStream: OutputStream,
        backup: ScholarBackup,
        compress: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val json = backupAdapter.toJson(backup)
        if (compress) {
            java.util.zip.GZIPOutputStream(java.io.BufferedOutputStream(outputStream)).use { gzos ->
                gzos.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            }
        } else {
            java.io.BufferedOutputStream(outputStream).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
    }

    suspend fun importDataFromStream(inputStream: InputStream): ScholarBackup = withContext(Dispatchers.IO) {
        val bis = java.io.BufferedInputStream(inputStream)
        bis.mark(4)
        val header = ByteArray(2)
        val readBytes = bis.read(header)
        bis.reset()

        val isGzip = readBytes >= 2 && header[0] == 0x1f.toByte() && header[1] == 0x8b.toByte()
        val json = if (isGzip) {
            java.util.zip.GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
            bis.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }

        if (json.isBlank()) {
            throw IllegalArgumentException("Import failed: backup content is empty")
        }

        backupAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup JSON format")
    }

    suspend fun restoreBackupToDao(backup: ScholarBackup, targetDao: ScholarDao) = withContext(Dispatchers.IO) {
        targetDao.restoreBackup(backup)
    }
}
