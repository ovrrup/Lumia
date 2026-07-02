package lumia.tracker.data

import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.ScholarBackup
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Chapter
import lumia.tracker.model.Task
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

class ScholarRepository(val dao: ScholarDao) {

    val allCourses: Flow<List<Course>> = dao.getAllCourses()
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allAssignments: Flow<List<PracticeAssignment>> = dao.getAllAssignments()
    val allActionLogs: Flow<List<ActionLog>> = dao.getAllActionLogs()
    val allPomodoroSessions: Flow<List<lumia.tracker.model.PomodoroSession>> = dao.getAllPomodoroSessions()
    val allNotes: Flow<List<lumia.tracker.model.Note>> = dao.getAllNotes()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val allAttachments: Flow<List<lumia.tracker.model.Attachment>> = dao.getAllAttachments()
    val allTopics: Flow<List<Topic>> = dao.getAllTopicsReactive()

    fun getTopicsForSubject(subjectId: Int) = dao.getTopicsForSubject(subjectId)
    fun getChaptersForSubject(subjectId: Int) = dao.getChaptersForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int) = dao.getAssignmentsForCourse(courseId)
    fun getAttachmentsForCourse(courseId: Int) = dao.getAttachmentsForCourse(courseId)
    fun getAttachmentsForSubject(subjectId: Int) = dao.getAttachmentsForSubject(subjectId)

    suspend fun insertAttachment(attachment: lumia.tracker.model.Attachment) = dao.insertAttachment(attachment)
    suspend fun deleteAttachment(attachment: lumia.tracker.model.Attachment) = dao.deleteAttachment(attachment)

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
    
    fun getAttendanceForCourse(courseId: Int) = dao.getAttendanceForCourse(courseId)
    val allAttendanceRecords = dao.getAllAttendanceRecords()
    suspend fun insertAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) = dao.deleteAttendanceRecord(record)
    
    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()
    suspend fun insertPomodoroSession(session: lumia.tracker.model.PomodoroSession) = dao.insertPomodoroSession(session)
    
    suspend fun insertNote(note: lumia.tracker.model.Note) = dao.insertNote(note)
    suspend fun updateNote(note: lumia.tracker.model.Note) = dao.updateNote(note)
    suspend fun deleteNote(note: lumia.tracker.model.Note) = dao.deleteNote(note)

    fun getTestRecordsForCourse(courseId: Int) = dao.getTestRecordsForCourse(courseId)
    fun getTestRecordsForSubject(subjectId: Int) = dao.getTestRecordsForSubject(subjectId)
    val allTestRecords = dao.getAllTestRecordsReactive()
    suspend fun insertTestRecord(record: lumia.tracker.model.TestRecord) = dao.insertTestRecord(record)
    suspend fun updateTestRecord(record: lumia.tracker.model.TestRecord) = dao.updateTestRecord(record)
    suspend fun deleteTestRecord(record: lumia.tracker.model.TestRecord) = dao.deleteTestRecord(record)

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
    }

    private val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(ScholarBackup::class.java)

    // Export
    suspend fun exportDataToStream(outputStream: OutputStream, backup: ScholarBackup) {
        // Secure binary format: wrap outputStream in GZIPOutputStream for compression and obfuscation
        java.util.zip.GZIPOutputStream(outputStream).use { gzos ->
            gzos.writer().use { writer ->
                writer.write(backupAdapter.toJson(backup))
            }
        }
    }

    // Import
        suspend fun importDataFromStream(inputStream: InputStream): ScholarBackup {
        val bis = java.io.BufferedInputStream(inputStream)
        bis.mark(2)
        val header = ByteArray(2)
        val readBytes = bis.read(header)
        bis.reset()

        val json = if (readBytes == 2 && header[0] == 0x1f.toByte() && header[1] == 0x8b.toByte()) {
            java.util.zip.GZIPInputStream(bis).reader().use { it.readText() }
        } else {
            bis.reader().use { it.readText() }
        }
        val backup = backupAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup file")
        return backup
    }
    suspend fun restoreBackupToDao(backup: lumia.tracker.model.ScholarBackup, targetDao: lumia.tracker.data.ScholarDao) {
        targetDao.clearCourses()
        targetDao.clearSubjects()
        targetDao.clearChapters()
        targetDao.clearTopics()
        targetDao.clearAssignments()
        targetDao.clearAttendance()
        targetDao.clearPomodoro()
        targetDao.clearActionLogs()
        targetDao.clearNotes()
        targetDao.clearTasks()
        targetDao.clearAttachments()
        targetDao.clearTestRecords()

        backup.courses.forEach { targetDao.insertCourse(it) }
        backup.subjects.forEach { targetDao.insertSubject(it) }
        backup.chapters?.forEach { targetDao.insertChapter(it) }
        backup.topics.forEach { targetDao.insertTopic(it) }
        backup.assignments.forEach { targetDao.insertAssignment(it) }
        backup.attendance?.forEach { targetDao.insertAttendanceRecord(it) }
        backup.pomodoro?.forEach { targetDao.insertPomodoroSession(it) }
        backup.actionLogs?.forEach { targetDao.insertActionLog(it) }
        backup.notes?.forEach { targetDao.insertNote(it) }
        backup.tasks?.forEach { targetDao.insertTask(it) }
        backup.attachments?.forEach { targetDao.insertAttachment(it) }
        backup.testRecords?.forEach { targetDao.insertTestRecord(it) }
    }

}

