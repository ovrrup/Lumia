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
            settings = settings
        )
        outputStream.writer().use { it.write(backupAdapter.toJson(backup)) }
    }

    // Import
    suspend fun importDataFromStream(inputStream: InputStream): Map<String, String>? {
        val json = inputStream.reader().readText()
        val backup = backupAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid backup file")
        dao.clearCourses()
        dao.clearSubjects()
        dao.clearTopics()
        dao.clearAssignments()
        backup.courses.forEach { dao.insertCourse(it.copy(id = 0)) }
        backup.subjects.forEach { dao.insertSubject(it.copy(id = 0)) }
        backup.topics.forEach { dao.insertTopic(it.copy(id = 0)) }
        backup.assignments.forEach { dao.insertAssignment(it.copy(id = 0)) }
        return backup.settings
    }
}
