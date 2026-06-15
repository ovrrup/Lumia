package ovrrup.lumia.data

import ovrrup.lumia.model.Course
import ovrrup.lumia.model.PracticeAssignment
import ovrrup.lumia.model.ScholarBackup
import ovrrup.lumia.model.Subject
import ovrrup.lumia.model.Topic
import ovrrup.lumia.model.ActionLog
import ovrrup.lumia.model.Chapter
import ovrrup.lumia.model.Task
import ovrrup.lumia.model.AttendanceRecord
import ovrrup.lumia.model.Note
import ovrrup.lumia.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

class ScholarRepository(private val dao: ScholarDao) {

    val allCourses: Flow<List<Course>> = dao.getAllCourses()
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allAssignments: Flow<List<PracticeAssignment>> = dao.getAllAssignments()
    val allActionLogs: Flow<List<ActionLog>> = dao.getAllActionLogs()
    val allPomodoroSessions: Flow<List<PomodoroSession>> = dao.getAllPomodoroSessions()
    val allNotes: Flow<List<Note>> = dao.getAllNotes()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    fun getTopicsForSubject(subjectId: Int) = dao.getTopicsForSubject(subjectId)
    fun getChaptersForSubject(subjectId: Int) = dao.getChaptersForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int) = dao.getAssignmentsForCourse(courseId)

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
    suspend fun insertAttendanceRecord(record: AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: AttendanceRecord) = dao.deleteAttendanceRecord(record)
    
    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()
    suspend fun insertPomodoroSession(session: PomodoroSession) = dao.insertPomodoroSession(session)
    
    suspend fun insertNote(note: Note) = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

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
            tasks = dao.exportAllTasks()
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

        // 4. Insert Topics with remapped Subject IDs to satisfy foreign keys
        backup.topics.forEach { topic ->
            val newSubjectId = oldToNewSubjectId[topic.subjectId] ?: topic.subjectId
            dao.insertTopic(topic.copy(id = 0, subjectId = newSubjectId))
        }

        // 5. Insert Assignments with remapped Course IDs to satisfy foreign keys
        backup.assignments.forEach { assignment ->
            val newCourseId = oldToNewCourseId[assignment.courseId] ?: assignment.courseId
            dao.insertAssignment(assignment.copy(id = 0, courseId = newCourseId))
        }

        // 6. Insert Attendance Records with remapped Course IDs to satisfy foreign keys
        backup.attendance?.forEach { record ->
            val newCourseId = oldToNewCourseId[record.courseId] ?: record.courseId
            dao.insertAttendanceRecord(record.copy(id = 0, courseId = newCourseId))
        }

        // 7. Insert Pomodoro Sessions with remapped Subject IDs
        backup.pomodoro?.forEach { session ->
            val newSubjectId = session.subjectId?.let { oldToNewSubjectId[it] } ?: session.subjectId
            dao.insertPomodoroSession(session.copy(id = 0, subjectId = newSubjectId))
        }

        // 8. Insert Action Logs
        backup.actionLogs?.forEach { log ->
            dao.insertActionLog(log.copy(id = 0))
        }

        // 9. Insert Notes with mapped Course/Subject IDs
        backup.notes?.forEach { note ->
            val newCourseId = note.courseId?.let { oldToNewCourseId[it] } ?: note.courseId
            val newSubjectId = note.subjectId?.let { oldToNewSubjectId[it] } ?: note.subjectId
            dao.insertNote(note.copy(id = 0, courseId = newCourseId, subjectId = newSubjectId))
        }

        // 10. Insert Chapters
        val oldToNewChapterId = mutableMapOf<Int, Int>()
        backup.chapters?.forEach { chapter ->
            val oldId = chapter.id
            val newSubjectId = oldToNewSubjectId[chapter.subjectId] ?: chapter.subjectId
            val newId = dao.insertChapter(chapter.copy(id = 0, subjectId = newSubjectId)).toInt()
            oldToNewChapterId[oldId] = newId
        }

        // 11. Insert Tasks
        backup.tasks?.forEach { task ->
            val newSubjectId = task.subjectId?.let { oldToNewSubjectId[it] } ?: task.subjectId
            val newChapterId = task.chapterId?.let { oldToNewChapterId[it] } ?: task.chapterId
            val newCourseId = task.courseId?.let { oldToNewCourseId[it] } ?: task.courseId
            dao.insertTask(task.copy(id = 0, subjectId = newSubjectId, chapterId = newChapterId, courseId = newCourseId))
        }

        return backup.settings
    }
}
