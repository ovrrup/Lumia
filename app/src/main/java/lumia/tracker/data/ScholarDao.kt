package lumia.tracker.data

import androidx.room.Dao
import androidx.room.Transaction
import lumia.tracker.model.ScholarBackup

@Dao
interface ScholarDao : CourseSubjectDao, AssignmentTaskDao, MiscDataDao {

    @Transaction
    suspend fun restoreBackup(backup: ScholarBackup) {
        clearTopics()
        clearChapters()
        clearAssignments()
        clearAttendance()
        
        clearCourses()
        clearSubjects()
        
        clearPomodoro()
        clearActionLogs()
        clearNotes()
        clearTasks()
        clearAttachments()
        clearTestRecords()
        clearTagCustomizations()
        clearFlashcards()

        backup.courses?.forEach { insertCourse(it) }
        backup.subjects?.forEach { insertSubject(it) }
        backup.chapters?.forEach { insertChapter(it) }
        backup.topics?.forEach { insertTopic(it) }
        backup.assignments?.forEach { insertAssignment(it) }
        backup.attendance?.forEach { insertAttendanceRecord(it) }
        
        backup.pomodoro?.forEach { insertPomodoroSession(it) }
        backup.actionLogs?.forEach { insertActionLog(it) }
        backup.notes?.forEach { insertNote(it) }
        backup.tasks?.forEach { insertTask(it) }
        backup.attachments?.forEach { insertAttachment(it) }
        backup.testRecords?.forEach { insertTestRecord(it) }
        backup.tagCustomizations?.forEach { insertTagCustomization(it) }
        backup.flashcards?.forEach { insertFlashcard(it) }
    }
}
