import re

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "r") as f:
    content = f.read()

restore_func = """    suspend fun restoreBackupToDao(backup: lumia.tracker.model.ScholarBackup, targetDao: lumia.tracker.data.ScholarDao) {
        targetDao.clearCourses()
        targetDao.clearSubjects()
        targetDao.clearChapters()
        targetDao.clearTopics()
        targetDao.clearAssignments()
        targetDao.clearAttendance()
        targetDao.clearPomodoroSessions()
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
        backup.attendance?.forEach { targetDao.insertAttendance(it) }
        backup.pomodoro?.forEach { targetDao.insertPomodoroSession(it) }
        backup.actionLogs?.forEach { targetDao.insertActionLog(it) }
        backup.notes?.forEach { targetDao.insertNote(it) }
        backup.tasks?.forEach { targetDao.insertTask(it) }
        backup.attachments?.forEach { targetDao.insertAttachment(it) }
        backup.testRecords?.forEach { targetDao.insertTestRecord(it) }
    }
"""

content = content[:content.rfind("}")] + restore_func + "\n}\n"

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "w") as f:
    f.write(content)
