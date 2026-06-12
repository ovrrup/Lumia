package com.example.data

import com.example.model.Course
import com.example.model.PracticeAssignment
import com.example.model.ScholarBackup
import com.example.model.Subject
import com.example.model.Topic
import com.example.model.ActionLog
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
    val allPomodoroSessions: Flow<List<com.example.model.PomodoroSession>> = dao.getAllPomodoroSessions()

    fun getTopicsForSubject(subjectId: Int) = dao.getTopicsForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int) = dao.getAssignmentsForCourse(courseId)

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
    suspend fun deleteAssignment(assignment: PracticeAssignment) = dao.deleteAssignment(assignment)
    
    fun getAttendanceForCourse(courseId: Int) = dao.getAttendanceForCourse(courseId)
    val allAttendanceRecords = dao.getAllAttendanceRecords()
    suspend fun insertAttendanceRecord(record: com.example.model.AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: com.example.model.AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: com.example.model.AttendanceRecord) = dao.deleteAttendanceRecord(record)
    
    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()
    suspend fun insertPomodoroSession(session: com.example.model.PomodoroSession) = dao.insertPomodoroSession(session)
    suspend fun clearAllData() {
        dao.clearCourses()
        dao.clearSubjects()
        dao.clearTopics()
        dao.clearAssignments()
        dao.clearAttendance()
        dao.clearPomodoro()
        dao.clearActionLogs()
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
            actionLogs = dao.exportAllActionLogs()
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
        dao.clearTopics()
        dao.clearAssignments()
        dao.clearAttendance()
        dao.clearPomodoro()
        dao.clearActionLogs()

        // 2. Insert Courses and map old IDs to newly generated auto-increment IDs
        val oldToNewCourseId = mutableMapOf<Int, Int>()
        backup.courses.forEach { course ->
            val oldId = course.id
            val newId = dao.insertCourse(course.copy(id = 0)).toInt()
            oldToNewCourseId[oldId] = newId
        }

        // 3. Insert Subjects and map old IDs to newly generated auto-increment IDs
        val oldToNewSubjectId = mutableMapOf<Int, Int>()
        backup.subjects.forEach { subject ->
            val oldId = subject.id
            val newId = dao.insertSubject(subject.copy(id = 0)).toInt()
            oldToNewSubjectId[oldId] = newId
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

        // 7. Insert Pomodoro Sessions
        backup.pomodoro?.forEach { session ->
            dao.insertPomodoroSession(session.copy(id = 0))
        }

        // 8. Insert Action Logs
        backup.actionLogs?.forEach { log ->
            dao.insertActionLog(log.copy(id = 0))
        }

        return backup.settings
    }
}
