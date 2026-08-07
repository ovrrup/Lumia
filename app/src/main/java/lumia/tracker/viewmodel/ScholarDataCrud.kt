package lumia.tracker.viewmodel

import kotlinx.coroutines.flow.Flow
import lumia.tracker.data.ScholarRepository
import lumia.tracker.model.*

class ScholarDataCrud(private val repository: ScholarRepository) {

    val dao get() = repository.dao

    val courses: Flow<List<Course>> = dao.getAllCourses()
    val subjects: Flow<List<Subject>> = dao.getAllSubjects()
    val assignments: Flow<List<PracticeAssignment>> = dao.getAllAssignments()
    val tasks: Flow<List<Task>> = dao.getAllTasks()
    val notes: Flow<List<Note>> = dao.getAllNotes()
    val attendanceRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords()
    val pomodoroSessions: Flow<List<PomodoroSession>> = dao.getAllPomodoroSessions()
    val attachments: Flow<List<Attachment>> = dao.getAllAttachments()
    val testRecords: Flow<List<TestRecord>> = dao.getAllTestRecordsReactive()
    val chapters: Flow<List<Chapter>> = dao.getAllChaptersFlow()
    val topics: Flow<List<Topic>> = dao.getAllTopicsReactive()
    val flashcards: Flow<List<Flashcard>> = dao.getAllFlashcards()
    val tagCustomizations: Flow<List<TagCustomization>> = dao.getAllTagCustomizations()
    val actionLogs: Flow<List<ActionLog>> = dao.getAllActionLogs()

    suspend fun insertCourse(course: Course): Long = dao.insertCourse(course)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    suspend fun insertSubject(subject: Subject): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    suspend fun insertAssignment(assignment: PracticeAssignment): Long = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: PracticeAssignment) = dao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: PracticeAssignment) = dao.deleteAssignment(assignment)

    suspend fun insertTask(task: Task): Long = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun insertNote(note: Note): Long = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    suspend fun insertAttendanceRecord(record: AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = dao.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: AttendanceRecord) = dao.deleteAttendanceRecord(record)

    suspend fun insertPomodoroSession(session: PomodoroSession) = dao.insertPomodoroSession(session)
    suspend fun updatePomodoroSession(session: PomodoroSession) = dao.updatePomodoroSession(session)
    suspend fun deletePomodoroSession(session: PomodoroSession) = dao.deletePomodoroSession(session)

    suspend fun insertAttachment(attachment: Attachment): Long = dao.insertAttachment(attachment)
    suspend fun deleteAttachment(attachment: Attachment) = dao.deleteAttachment(attachment)

    suspend fun insertChapter(chapter: Chapter): Long = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: Chapter) = dao.deleteChapter(chapter)

    suspend fun insertTopic(topic: Topic): Long = dao.insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = dao.deleteTopic(topic)

    suspend fun insertFlashcard(flashcard: Flashcard): Long = dao.insertFlashcard(flashcard)
    suspend fun updateFlashcard(flashcard: Flashcard) = dao.updateFlashcard(flashcard)
    suspend fun deleteFlashcard(flashcard: Flashcard) = dao.deleteFlashcard(flashcard)

    suspend fun insertTestRecord(testRecord: TestRecord) = dao.insertTestRecord(testRecord)
    suspend fun updateTestRecord(testRecord: TestRecord) = dao.updateTestRecord(testRecord)
    suspend fun deleteTestRecord(testRecord: TestRecord) = dao.deleteTestRecord(testRecord)

    suspend fun insertTagCustomization(tag: TagCustomization) = dao.insertTagCustomization(tag)
    suspend fun updateTagCustomization(tag: TagCustomization) = dao.updateTagCustomization(tag)
    suspend fun deleteTagCustomization(tag: TagCustomization) = dao.deleteTagCustomization(tag)

    suspend fun insertActionLog(actionLog: ActionLog) = dao.insertActionLog(actionLog)
}
