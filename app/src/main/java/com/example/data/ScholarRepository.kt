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

    fun getTopicsForSubject(subjectId: Int) = dao.getTopicsForSubject(subjectId)
    fun getAssignmentsForCourse(courseId: Int) = dao.getAssignmentsForCourse(courseId)

    suspend fun insertCourse(course: Course) = dao.insertCourse(course)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    suspend fun insertSubject(subject: Subject) = dao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    suspend fun insertTopic(topic: Topic) = dao.insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = dao.deleteTopic(topic)

    suspend fun insertAssignment(assignment: PracticeAssignment) = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: PracticeAssignment) = dao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: PracticeAssignment) = dao.deleteAssignment(assignment)
    
    suspend fun insertActionLog(log: ActionLog) = dao.insertActionLog(log)
    suspend fun clearActionLogs() = dao.clearActionLogs()

    // Export
    suspend fun exportDataToStream(outputStream: OutputStream) {
        val backup = ScholarBackup(
            courses = dao.exportAllCourses(),
            subjects = dao.exportAllSubjects(),
            topics = dao.exportAllTopics(),
            assignments = dao.exportAllAssignments()
        )
        ObjectOutputStream(outputStream).use {
            it.writeObject(backup)
        }
    }

    // Import
    suspend fun importDataFromStream(inputStream: InputStream) {
        ObjectInputStream(inputStream).use { ois ->
            val backup = ois.readObject() as? ScholarBackup
            if (backup != null) {
                // Clear existing and replace
                dao.clearAll()
                backup.courses.forEach { dao.insertCourse(it) }
                backup.subjects.forEach { dao.insertSubject(it) }
                backup.topics.forEach { dao.insertTopic(it) }
                backup.assignments.forEach { dao.insertAssignment(it) }
            }
        }
    }
}
