package lumia.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Attachment
import lumia.tracker.model.AttendanceRecord
import lumia.tracker.model.Chapter
import lumia.tracker.model.Course
import lumia.tracker.model.Note
import lumia.tracker.model.PomodoroSession
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.TagCustomization
import lumia.tracker.model.Task
import lumia.tracker.model.TestRecord
import lumia.tracker.model.Topic
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import kotlinx.coroutines.flow.Flow

/**
 * ScholarDao - High-Performance Room Data Access Object for Lumia Academic Tracker.
 * Manages the Course <-> Subject <-> Chapter <-> Topic relational hierarchy,
 * attendance metrics, tests, tasks, notes, attachments, and backup restorations.
 */
@Dao
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Room Data Access Object for Academic Hierarchy, Curriculum, Syllabus, and Study entities",
    category = "Database"
)
interface ScholarDao {

    // =========================================================================
    // COURSES & COURSE-SUBJECT SYNERGY
    // =========================================================================

    @ValueScore(score = 90, importance = Importance.HIGH, description = "Reactive stream of all registered courses ordered alphabetically")
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<Course>>

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of a specific course by ID")
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    fun getCourseById(courseId: Int): Flow<Course?>

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of a course by ID")
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseByIdDirect(courseId: Int): Course?

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of courses associated with a specific subject")
    @Query("SELECT * FROM courses WHERE subjectId = :subjectId OR (',' || subjectIds || ',') LIKE ('%,' || :subjectId || ',%') ORDER BY name ASC")
    fun getCoursesForSubject(subjectId: Int): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>): List<Long>

    @Update
    suspend fun updateCourse(course: Course)

    @Update
    suspend fun updateCourses(courses: List<Course>)

    @Delete
    suspend fun deleteCourse(course: Course)

    // =========================================================================
    // SUBJECTS
    // =========================================================================

    @ValueScore(score = 92, importance = Importance.CRITICAL, description = "Reactive stream of all academic subjects ordered alphabetically")
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of a specific subject by ID")
    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    fun getSubjectById(subjectId: Int): Flow<Subject?>

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of a subject by ID")
    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectByIdDirect(subjectId: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>): List<Long>

    @Update
    suspend fun updateSubject(subject: Subject)

    @Update
    suspend fun updateSubjects(subjects: List<Subject>)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // =========================================================================
    // CHAPTERS
    // =========================================================================

    @ValueScore(score = 90, importance = Importance.HIGH, description = "Reactive stream of chapters for a subject")
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>>

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of a specific chapter by ID")
    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun getChapterById(chapterId: Int): Flow<Chapter?>

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of a chapter by ID")
    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterByIdDirect(chapterId: Int): Chapter?

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of all chapters for a subject")
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    suspend fun getChaptersForSubjectDirect(subjectId: Int): List<Chapter>

    @ValueScore(score = 86, importance = Importance.MEDIUM, description = "Reactive stream of all curriculum chapters")
    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChaptersFlow(): Flow<List<Chapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>): List<Long>

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Update
    suspend fun updateChapters(chapters: List<Chapter>)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersForSubject(subjectId: Int)

    // =========================================================================
    // TOPICS & SYLLABUS HIERARCHY
    // =========================================================================

    @ValueScore(score = 94, importance = Importance.CRITICAL, description = "Reactive stream of topics belonging to a subject")
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @ValueScore(score = 90, importance = Importance.HIGH, description = "Reactive stream of topics assigned to a chapter")
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY id ASC")
    fun getTopicsForChapter(chapterId: Int): Flow<List<Topic>>

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of unassigned topics (no chapter) for a subject")
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND (chapterId IS NULL OR chapterId = 0) ORDER BY id ASC")
    fun getUnassignedTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Reactive stream of a single topic by ID")
    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    fun getTopicById(topicId: Int): Flow<Topic?>

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of a topic by ID")
    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    suspend fun getTopicByIdDirect(topicId: Int): Topic?

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of all topics for a subject")
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY id ASC")
    suspend fun getTopicsForSubjectDirect(subjectId: Int): List<Topic>

    @ValueScore(score = 85, importance = Importance.MEDIUM, description = "Direct synchronous fetch of all topics for a chapter")
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY id ASC")
    suspend fun getTopicsForChapterDirect(chapterId: Int): List<Topic>

    @ValueScore(score = 95, importance = Importance.CRITICAL, description = "Global reactive stream of all topics across all subjects")
    @Query("SELECT * FROM topics ORDER BY id ASC")
    fun getAllTopicsReactive(): Flow<List<Topic>>

    @ValueScore(score = 89, importance = Importance.HIGH, description = "Atomic update for topic completion state")
    @Query("UPDATE topics SET isCompleted = :isCompleted WHERE id = :topicId")
    suspend fun setTopicCompleted(topicId: Int, isCompleted: Boolean)

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Bulk update for topic completion across an entire chapter")
    @Query("UPDATE topics SET isCompleted = :isCompleted WHERE chapterId = :chapterId")
    suspend fun setChapterTopicsCompleted(chapterId: Int, isCompleted: Boolean)

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Bulk update for topic completion across an entire subject")
    @Query("UPDATE topics SET isCompleted = :isCompleted WHERE subjectId = :subjectId")
    suspend fun setSubjectTopicsCompleted(subjectId: Int, isCompleted: Boolean)

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Assign or unassign a topic to a chapter")
    @Query("UPDATE topics SET chapterId = :chapterId WHERE id = :topicId")
    suspend fun assignTopicToChapter(topicId: Int, chapterId: Int?)

    @ValueScore(score = 88, importance = Importance.HIGH, description = "Unassign all topics from a chapter to prevent orphans")
    @Query("UPDATE topics SET chapterId = NULL WHERE chapterId = :chapterId")
    suspend fun unassignTopicsFromChapter(chapterId: Int)

    @ValueScore(score = 86, importance = Importance.MEDIUM, description = "Delete all topics belonging to a chapter")
    @Query("DELETE FROM topics WHERE chapterId = :chapterId")
    suspend fun deleteTopicsForChapter(chapterId: Int)

    @ValueScore(score = 86, importance = Importance.MEDIUM, description = "Delete all topics belonging to a subject")
    @Query("DELETE FROM topics WHERE subjectId = :subjectId")
    suspend fun deleteTopicsForSubject(subjectId: Int)

    // Topic Coverage Aggregates for Instant Reactive Dashboards
    @ValueScore(score = 87, importance = Importance.HIGH, description = "Count total topics in a subject")
    @Query("SELECT COUNT(*) FROM topics WHERE subjectId = :subjectId")
    fun getTotalTopicsCountForSubject(subjectId: Int): Flow<Int>

    @ValueScore(score = 87, importance = Importance.HIGH, description = "Count completed topics in a subject")
    @Query("SELECT COUNT(*) FROM topics WHERE subjectId = :subjectId AND isCompleted = 1")
    fun getCompletedTopicsCountForSubject(subjectId: Int): Flow<Int>

    @ValueScore(score = 86, importance = Importance.HIGH, description = "Count total topics in a chapter")
    @Query("SELECT COUNT(*) FROM topics WHERE chapterId = :chapterId")
    fun getTotalTopicsCountForChapter(chapterId: Int): Flow<Int>

    @ValueScore(score = 86, importance = Importance.HIGH, description = "Count completed topics in a chapter")
    @Query("SELECT COUNT(*) FROM topics WHERE chapterId = :chapterId AND isCompleted = 1")
    fun getCompletedTopicsCountForChapter(chapterId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<Topic>): List<Long>

    @Update
    suspend fun updateTopic(topic: Topic)

    @Update
    suspend fun updateTopics(topics: List<Topic>)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    // =========================================================================
    // ASSIGNMENTS & HOMEWORK
    // =========================================================================

    @Query("DELETE FROM assignments WHERE courseId = :courseId")
    suspend fun deleteAssignmentsForCourse(courseId: Int)

    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY priority DESC, orderIndex ASC, dueDateMillis ASC")
    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>>

    @Query("SELECT * FROM assignments ORDER BY priority DESC, orderIndex ASC, dueDateMillis ASC")
    fun getAllAssignments(): Flow<List<PracticeAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: PracticeAssignment): Long

    @Update
    suspend fun updateAssignment(assignment: PracticeAssignment)

    @Delete
    suspend fun deleteAssignment(assignment: PracticeAssignment)

    // =========================================================================
    // ATTENDANCE RECORDS
    // =========================================================================

    @Query("DELETE FROM attendance_records WHERE courseId = :courseId")
    suspend fun deleteAttendanceForCourse(courseId: Int)

    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getAttendanceForCourse(courseId: Int): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord)

    @Update
    suspend fun updateAttendanceRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendanceRecord(record: AttendanceRecord)

    // =========================================================================
    // ACTION LOGS
    // =========================================================================

    @Query("SELECT * FROM action_logs ORDER BY timestampMillis DESC")
    fun getAllActionLogs(): Flow<List<ActionLog>>

    @Insert
    suspend fun insertActionLog(log: ActionLog)

    @Query("DELETE FROM action_logs")
    suspend fun clearActionLogs()

    // =========================================================================
    // POMODORO FOCUS SESSIONS
    // =========================================================================

    @Query("SELECT * FROM pomodoro_sessions ORDER BY dateMillis DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>

    @Insert
    suspend fun insertPomodoroSession(session: PomodoroSession)

    // =========================================================================
    // TEST RECORDS
    // =========================================================================

    @Query("SELECT * FROM test_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getTestRecordsForCourse(courseId: Int): Flow<List<TestRecord>>

    @Query("SELECT * FROM test_records WHERE subjectId = :subjectId ORDER BY dateMillis DESC")
    fun getTestRecordsForSubject(subjectId: Int): Flow<List<TestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestRecord(record: TestRecord)

    @Update
    suspend fun updateTestRecord(record: TestRecord)

    @Delete
    suspend fun deleteTestRecord(record: TestRecord)

    @Query("SELECT * FROM test_records ORDER BY dateMillis DESC")
    fun getAllTestRecordsReactive(): Flow<List<TestRecord>>

    @Query("SELECT * FROM test_records")
    suspend fun exportAllTestRecords(): List<TestRecord>

    @Query("DELETE FROM test_records")
    suspend fun clearTestRecords()

    // =========================================================================
    // NOTES
    // =========================================================================

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // =========================================================================
    // TASKS
    // =========================================================================

    @Query("SELECT * FROM tasks ORDER BY priority DESC, orderIndex ASC, dueDateMillis ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks")
    suspend fun exportAllTasks(): List<Task>

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    // =========================================================================
    // ATTACHMENTS
    // =========================================================================

    @Query("SELECT * FROM attachments ORDER BY addedAt DESC")
    fun getAllAttachments(): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments WHERE courseId = :courseId ORDER BY addedAt DESC")
    fun getAttachmentsForCourse(courseId: Int): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments WHERE subjectId = :subjectId ORDER BY addedAt DESC")
    fun getAttachmentsForSubject(subjectId: Int): Flow<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: Attachment): Long

    @Delete
    suspend fun deleteAttachment(attachment: Attachment)

    @Query("DELETE FROM attachments")
    suspend fun clearAttachments()

    @Query("SELECT * FROM attachments")
    suspend fun exportAllAttachments(): List<Attachment>

    // =========================================================================
    // TAG CUSTOMIZATIONS
    // =========================================================================

    @Query("SELECT * FROM tag_customizations ORDER BY tagName ASC")
    fun getAllTagCustomizations(): Flow<List<TagCustomization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCustomization(customization: TagCustomization)

    @Update
    suspend fun updateTagCustomization(customization: TagCustomization)

    @Delete
    suspend fun deleteTagCustomization(customization: TagCustomization)

    @Query("SELECT * FROM tag_customizations")
    suspend fun exportAllTagCustomizations(): List<TagCustomization>

    @Query("DELETE FROM tag_customizations")
    suspend fun clearTagCustomizations()

    // =========================================================================
    // BACKUP & EXPORT / IMPORT
    // =========================================================================

    @Query("SELECT * FROM courses")
    suspend fun exportAllCourses(): List<Course>

    @Query("SELECT * FROM subjects")
    suspend fun exportAllSubjects(): List<Subject>

    @Query("SELECT * FROM chapters")
    suspend fun exportAllChapters(): List<Chapter>

    @Query("SELECT * FROM topics")
    suspend fun exportAllTopics(): List<Topic>

    @Query("SELECT * FROM assignments")
    suspend fun exportAllAssignments(): List<PracticeAssignment>

    @Query("SELECT * FROM attendance_records")
    suspend fun exportAllAttendance(): List<AttendanceRecord>

    @Query("SELECT * FROM pomodoro_sessions")
    suspend fun exportAllPomodoro(): List<PomodoroSession>

    @Query("SELECT * FROM action_logs")
    suspend fun exportAllActionLogs(): List<ActionLog>

    @Query("SELECT * FROM notes")
    suspend fun exportAllNotes(): List<Note>

    @Query("DELETE FROM courses") suspend fun clearCourses()
    @Query("DELETE FROM subjects") suspend fun clearSubjects()
    @Query("DELETE FROM chapters") suspend fun clearChapters()
    @Query("DELETE FROM topics") suspend fun clearTopics()
    @Query("DELETE FROM assignments") suspend fun clearAssignments()
    @Query("DELETE FROM attendance_records") suspend fun clearAttendance()
    @Query("DELETE FROM pomodoro_sessions") suspend fun clearPomodoro()
    @Query("DELETE FROM notes") suspend fun clearNotes()

    @Transaction
    suspend fun restoreBackup(backup: lumia.tracker.model.ScholarBackup) {
        // Clear children first to respect SQLite foreign key constraints
        clearTopics()
        clearChapters()
        clearAssignments()
        clearAttendance()

        // Clear parent tables next
        clearCourses()
        clearSubjects()

        // Clear other tables
        clearPomodoro()
        clearActionLogs()
        clearNotes()
        clearTasks()
        clearAttachments()
        clearTestRecords()
        clearTagCustomizations()

        // Insert parents first, then children to respect SQLite foreign key constraints
        backup.courses?.forEach { insertCourse(it) }
        backup.subjects?.forEach { insertSubject(it) }
        backup.chapters?.forEach { insertChapter(it) }
        backup.topics?.forEach { insertTopic(it) }
        backup.assignments?.forEach { insertAssignment(it) }
        backup.attendance?.forEach { insertAttendanceRecord(it) }

        // Insert remaining non-constrained tables
        backup.pomodoro?.forEach { insertPomodoroSession(it) }
        backup.actionLogs?.forEach { insertActionLog(it) }
        backup.notes?.forEach { insertNote(it) }
        backup.tasks?.forEach { insertTask(it) }
        backup.attachments?.forEach { insertAttachment(it) }
        backup.testRecords?.forEach { insertTestRecord(it) }
        backup.tagCustomizations?.forEach { insertTagCustomization(it) }
    }
}

