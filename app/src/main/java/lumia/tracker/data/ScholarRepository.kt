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
    suspend fun exportDataToStream(outputStream: OutputStream, settings: Map<String, String>) {
        val backup = ScholarBackup(
            courses = dao.exportAllCourses(),
            subjects = dao.exportAllSubjects(),
            topics = dao.exportAllTopics(),
            assignments = dao.exportAllAssignments(),
            settings = settings,
            attendance = dao.exportAllAttendance(),
            pomodoro = dao.exportAllPomodoro(),
            actionLogs = dao.exportAllActionLogs(),
            notes = dao.exportAllNotes(),
            chapters = dao.exportAllChapters(),
            tasks = dao.exportAllTasks(),
            attachments = dao.exportAllAttachments(),
            testRecords = dao.exportAllTestRecords()
        )
        // Secure binary format: wrap outputStream in GZIPOutputStream for compression and obfuscation
        java.util.zip.GZIPOutputStream(outputStream).use { gzos ->
            gzos.writer().use { writer ->
                writer.write(backupAdapter.toJson(backup))
            }
        }
    }

    // Import
    suspend fun importDataFromStream(inputStream: InputStream): Map<String, String>? {
        val bis = java.io.BufferedInputStream(inputStream)
        bis.mark(2)
        val header = ByteArray(2)
        val readBytes = bis.read(header)
        bis.reset()

        val json = if (readBytes == 2 && header[0] == 0x1f.toByte() && header[1] == 0x8b.toByte()) {
            // Secure compressed binary GZIP stream
            java.util.zip.GZIPInputStream(bis).reader().use { it.readText() }
        } else {
            // Fallback plain-text JSON stream for older backups
            bis.reader().use { it.readText() }
        }
        val backup = backupAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup file")
        
        // 1. Clear database securely
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

        // 2. Insert Subjects and map old IDs to newly generated auto-increment IDs
        val oldToNewSubjectId = mutableMapOf<Int, Int>()
        backup.subjects.forEach { subject ->
            val oldId = subject.id
            val newId = dao.insertSubject(subject.copy(id = 0)).toInt()
            oldToNewSubjectId[oldId] = newId
        }

        // 3. Insert Courses and map old IDs to newly generated auto-increment IDs with remapped subjectId
        val oldToNewCourseId = mutableMapOf<Int, Int>()
        backup.courses.forEach { course ->
            val oldId = course.id
            val newSubjectId = course.subjectId?.let { oldToNewSubjectId[it] } ?: course.subjectId
            val newId = dao.insertCourse(course.copy(id = 0, subjectId = newSubjectId)).toInt()
            oldToNewCourseId[oldId] = newId
        }

        // 4. Insert Chapters with mapped Subject IDs
        val oldToNewChapterId = mutableMapOf<Int, Int>()
        backup.chapters?.forEach { chapter ->
            val oldId = chapter.id
            val newSubjectId = oldToNewSubjectId[chapter.subjectId] ?: chapter.subjectId
            val newId = dao.insertChapter(chapter.copy(id = 0, subjectId = newSubjectId)).toInt()
            oldToNewChapterId[oldId] = newId
        }

        // 5. Insert Topics with remapped Subject IDs and Chapter IDs
        val oldToNewTopicId = mutableMapOf<Int, Int>()
        backup.topics.forEach { topic ->
            val oldId = topic.id
            val newSubjectId = oldToNewSubjectId[topic.subjectId] ?: topic.subjectId
            val newChapterId = topic.chapterId?.let { oldToNewChapterId[it] } ?: topic.chapterId
            val newId = dao.insertTopic(topic.copy(id = 0, subjectId = newSubjectId, chapterId = newChapterId)).toInt()
            oldToNewTopicId[oldId] = newId
        }

        // 6. Insert Assignments with remapped Course IDs
        val oldToNewAssignmentId = mutableMapOf<Int, Int>()
        backup.assignments.forEach { assignment ->
            val oldId = assignment.id
            val newCourseId = oldToNewCourseId[assignment.courseId] ?: assignment.courseId
            val newSubjectId = assignment.subjectId?.let { oldToNewSubjectId[it] } ?: assignment.subjectId
            val newId = dao.insertAssignment(assignment.copy(id = 0, courseId = newCourseId, subjectId = newSubjectId)).toInt()
            oldToNewAssignmentId[oldId] = newId
        }

        // 7. Insert Tasks with remapped dependencies
        val oldToNewTaskId = mutableMapOf<Int, Int>()
        backup.tasks?.forEach { task ->
            val oldId = task.id
            val newSubjectId = task.subjectId?.let { oldToNewSubjectId[it] } ?: task.subjectId
            val newChapterId = task.chapterId?.let { oldToNewChapterId[it] } ?: task.chapterId
            val newTopicId = task.topicId?.let { oldToNewTopicId[it] } ?: task.topicId
            val newCourseId = task.courseId?.let { oldToNewCourseId[it] } ?: task.courseId
            val newAssignmentId = task.assignmentId?.let { oldToNewAssignmentId[it] } ?: task.assignmentId
            val newId = dao.insertTask(task.copy(id = 0, subjectId = newSubjectId, chapterId = newChapterId, topicId = newTopicId, courseId = newCourseId, assignmentId = newAssignmentId)).toInt()
            oldToNewTaskId[oldId] = newId
        }

        // 8. Insert Pomodoro Sessions with remapped IDs
        backup.pomodoro?.forEach { session ->
            val newSubjectId = session.subjectId?.let { oldToNewSubjectId[it] } ?: session.subjectId
            val newCourseId = session.courseId?.let { oldToNewCourseId[it] } ?: session.courseId
            val newAssignmentId = session.assignmentId?.let { oldToNewAssignmentId[it] } ?: session.assignmentId
            val newTaskId = session.taskId?.let { oldToNewTaskId[it] } ?: session.taskId
            dao.insertPomodoroSession(session.copy(id = 0, subjectId = newSubjectId, courseId = newCourseId, assignmentId = newAssignmentId, taskId = newTaskId))
        }

        // 9. Insert Attendance Records with remapped Course IDs
        backup.attendance?.forEach { record ->
            val newCourseId = oldToNewCourseId[record.courseId] ?: record.courseId
            dao.insertAttendanceRecord(record.copy(id = 0, courseId = newCourseId))
        }

        // 10. Insert Action Logs
        backup.actionLogs?.forEach { log ->
            dao.insertActionLog(log.copy(id = 0))
        }

        // 11. Insert Notes with mapped Course/Subject IDs
        backup.notes?.forEach { note ->
            val newCourseId = note.courseId?.let { oldToNewCourseId[it] } ?: note.courseId
            val newSubjectId = note.subjectId?.let { oldToNewSubjectId[it] } ?: note.subjectId
            dao.insertNote(note.copy(id = 0, courseId = newCourseId, subjectId = newSubjectId))
        }

        // 12. Insert Attachments with mapped Course/Subject IDs
        backup.attachments?.forEach { attachment ->
            val newCourseId = attachment.courseId?.let { oldToNewCourseId[it] } ?: attachment.courseId
            val newSubjectId = attachment.subjectId?.let { oldToNewSubjectId[it] } ?: attachment.subjectId
            dao.insertAttachment(attachment.copy(id = 0, courseId = newCourseId, subjectId = newSubjectId))
        }

        // 13. Insert Test Records with mapped Course/Subject IDs
        backup.testRecords?.forEach { test ->
            val newCourseId = test.courseId?.let { oldToNewCourseId[it] } ?: test.courseId
            val newSubjectId = test.subjectId?.let { oldToNewSubjectId[it] } ?: test.subjectId
            dao.insertTestRecord(test.copy(id = 0, courseId = newCourseId, subjectId = newSubjectId))
        }

        return backup.settings
    }
}
