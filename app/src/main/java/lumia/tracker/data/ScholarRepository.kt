package lumia.tracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Attachment
import lumia.tracker.model.AttendanceRecord
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
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.io.InputStream
import java.io.OutputStream

/**
 * ScholarRepository - Centralized Data Repository for Lumia Academic Tracker.
 * Bridges reactive DAO queries, atomic syllabus updates, hierarchical aggregations,
 * and encrypted/compressed backup serialization.
 */
@ValueScore(
    score = 95,
    importance = Importance.CRITICAL,
    description = "Central repository managing academic curriculum entities, topics, chapters, and reactive state flows",
    category = "Data"
)
class ScholarRepository(val dao: ScholarDao) {

    // =========================================================================
    // REACTIVE STREAMS
    // =========================================================================

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
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords()
    val allTestRecords: Flow<List<TestRecord>> = dao.getAllTestRecordsReactive()

    // =========================================================================
    // HIERARCHICAL QUERIES (Course <-> Subject <-> Chapter <-> Topic)
    // =========================================================================

    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>> = dao.getTopicsForSubject(subjectId)
    fun getTopicsForChapter(chapterId: Int): Flow<List<Topic>> = dao.getTopicsForChapter(chapterId)
    fun getUnassignedTopicsForSubject(subjectId: Int): Flow<List<Topic>> = dao.getUnassignedTopicsForSubject(subjectId)
    fun getTopicById(topicId: Int): Flow<Topic?> = dao.getTopicById(topicId)
    suspend fun getTopicByIdDirect(topicId: Int): Topic? = dao.getTopicByIdDirect(topicId)

    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>> = dao.getChaptersForSubject(subjectId)
    fun getChapterById(chapterId: Int): Flow<Chapter?> = dao.getChapterById(chapterId)
    suspend fun getChapterByIdDirect(chapterId: Int): Chapter? = dao.getChapterByIdDirect(chapterId)

    fun getCoursesForSubject(subjectId: Int): Flow<List<Course>> = dao.getCoursesForSubject(subjectId)
    fun getCourseById(courseId: Int): Flow<Course?> = dao.getCourseById(courseId)
    suspend fun getCourseByIdDirect(courseId: Int): Course? = dao.getCourseByIdDirect(courseId)

    fun getSubjectById(subjectId: Int): Flow<Subject?> = dao.getSubjectById(subjectId)
    suspend fun getSubjectByIdDirect(subjectId: Int): Subject? = dao.getSubjectByIdDirect(subjectId)

    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>> = dao.getAssignmentsForCourse(courseId)
    fun getAttachmentsForCourse(courseId: Int): Flow<List<Attachment>> = dao.getAttachmentsForCourse(courseId)
    fun getAttachmentsForSubject(subjectId: Int): Flow<List<Attachment>> = dao.getAttachmentsForSubject(subjectId)
    fun getAttendanceForCourse(courseId: Int): Flow<List<AttendanceRecord>> = dao.getAttendanceForCourse(courseId)
    fun getTestRecordsForCourse(courseId: Int): Flow<List<TestRecord>> = dao.getTestRecordsForCourse(courseId)
    fun getTestRecordsForSubject(subjectId: Int): Flow<List<TestRecord>> = dao.getTestRecordsForSubject(subjectId)

    // Topic Coverage Aggregates
    fun getTotalTopicsCountForSubject(subjectId: Int): Flow<Int> = dao.getTotalTopicsCountForSubject(subjectId)
    fun getCompletedTopicsCountForSubject(subjectId: Int): Flow<Int> = dao.getCompletedTopicsCountForSubject(subjectId)
    fun getTotalTopicsCountForChapter(chapterId: Int): Flow<Int> = dao.getTotalTopicsCountForChapter(chapterId)
    fun getCompletedTopicsCountForChapter(chapterId: Int): Flow<Int> = dao.getCompletedTopicsCountForChapter(chapterId)

    // =========================================================================
    // MUTATIONS & ATOMIC SYLLABUS OPERATIONS
    // =========================================================================

    suspend fun insertAttachment(attachment: Attachment): Long = dao.insertAttachment(attachment)
    suspend fun deleteAttachment(attachment: Attachment) = dao.deleteAttachment(attachment)

    suspend fun insertChapter(chapter: Chapter): Long = dao.insertChapter(chapter)
    suspend fun insertChapters(chapters: List<Chapter>): List<Long> = dao.insertChapters(chapters)
    suspend fun updateChapter(chapter: Chapter) = dao.updateChapter(chapter)
    suspend fun updateChapters(chapters: List<Chapter>) = dao.updateChapters(chapters)

    suspend fun deleteChapter(chapter: Chapter, deleteAssignedTopics: Boolean = false) {
        if (deleteAssignedTopics) {
            dao.deleteTopicsForChapter(chapter.id)
        } else {
            dao.unassignTopicsFromChapter(chapter.id)
        }
        dao.deleteChapter(chapter)
    }

    suspend fun insertTask(task: Task): Long = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun updateTasks(tasks: List<Task>) { tasks.forEach { dao.updateTask(it) } }
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun insertCourse(course: Course): Long = dao.insertCourse(course)
    suspend fun insertCourses(courses: List<Course>): List<Long> = dao.insertCourses(courses)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun updateCourses(courses: List<Course>) = dao.updateCourses(courses)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    suspend fun insertSubject(subject: Subject): Long = dao.insertSubject(subject)
    suspend fun insertSubjects(subjects: List<Subject>): List<Long> = dao.insertSubjects(subjects)
    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    suspend fun updateSubjects(subjects: List<Subject>) = dao.updateSubjects(subjects)
    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    suspend fun insertTopic(topic: Topic): Long = dao.insertTopic(topic)
    suspend fun insertTopics(topics: List<Topic>): List<Long> = dao.insertTopics(topics)
    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)
    suspend fun updateTopics(topics: List<Topic>) = dao.updateTopics(topics)
    suspend fun deleteTopic(topic: Topic) = dao.deleteTopic(topic)

    suspend fun setTopicCompleted(topicId: Int, isCompleted: Boolean) = dao.setTopicCompleted(topicId, isCompleted)
    suspend fun setChapterTopicsCompleted(chapterId: Int, isCompleted: Boolean) = dao.setChapterTopicsCompleted(chapterId, isCompleted)
    suspend fun setSubjectTopicsCompleted(subjectId: Int, isCompleted: Boolean) = dao.setSubjectTopicsCompleted(subjectId, isCompleted)
    suspend fun assignTopicToChapter(topicId: Int, chapterId: Int?) = dao.assignTopicToChapter(topicId, chapterId)
    suspend fun batchAssignTopicsToChapter(topicIds: List<Int>, chapterId: Int?) {
        topicIds.forEach { dao.assignTopicToChapter(it, chapterId) }
    }

    suspend fun insertAssignment(assignment: PracticeAssignment): Long = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: PracticeAssignment) = dao.updateAssignment(assignment)
    suspend fun updateAssignments(assignments: List<PracticeAssignment>) { assignments.forEach { dao.updateAssignment(it) } }
    suspend fun deleteAssignment(assignment: PracticeAssignment) = dao.deleteAssignment(assignment)

    suspend fun insertAttendanceRecord(record: AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: AttendanceRecord) = dao.deleteAttendanceRecord(record)

    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()
    suspend fun insertPomodoroSession(session: PomodoroSession) = dao.insertPomodoroSession(session)

    suspend fun insertNote(note: Note): Long = dao.insertNote(note)
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

    // =========================================================================
    // EXPORT / IMPORT SERIALIZATION
    // =========================================================================

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

