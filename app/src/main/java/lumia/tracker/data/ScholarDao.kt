package lumia.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Chapter
import lumia.tracker.model.Task
import lumia.tracker.model.Attachment
import lumia.tracker.model.TagCustomization
import kotlinx.coroutines.flow.Flow

@Dao
interface ScholarDao {

    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)


    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)


    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @Query("SELECT * FROM topics")
    fun getAllTopicsReactive(): Flow<List<Topic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)


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

    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getAttendanceForCourse(courseId: Int): Flow<List<lumia.tracker.model.AttendanceRecord>>
    
    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC")
    fun getAllAttendanceRecords(): Flow<List<lumia.tracker.model.AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: lumia.tracker.model.AttendanceRecord)

    @Update
    suspend fun updateAttendanceRecord(record: lumia.tracker.model.AttendanceRecord)

    @Delete
    suspend fun deleteAttendanceRecord(record: lumia.tracker.model.AttendanceRecord)
    
    @Query("SELECT * FROM action_logs ORDER BY timestampMillis DESC")
    fun getAllActionLogs(): Flow<List<ActionLog>>

    @Insert
    suspend fun insertActionLog(log: ActionLog)

    @Query("DELETE FROM action_logs")
    suspend fun clearActionLogs()
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY dateMillis DESC")
    fun getAllPomodoroSessions(): Flow<List<lumia.tracker.model.PomodoroSession>>

    @Insert
    suspend fun insertPomodoroSession(session: lumia.tracker.model.PomodoroSession)

    // Test Records
    @Query("SELECT * FROM test_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getTestRecordsForCourse(courseId: Int): Flow<List<lumia.tracker.model.TestRecord>>

    @Query("SELECT * FROM test_records WHERE subjectId = :subjectId ORDER BY dateMillis DESC")
    fun getTestRecordsForSubject(subjectId: Int): Flow<List<lumia.tracker.model.TestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestRecord(record: lumia.tracker.model.TestRecord)

    @Update
    suspend fun updateTestRecord(record: lumia.tracker.model.TestRecord)

    @Delete
    suspend fun deleteTestRecord(record: lumia.tracker.model.TestRecord)

    @Query("SELECT * FROM test_records ORDER BY dateMillis DESC")
    fun getAllTestRecordsReactive(): Flow<List<lumia.tracker.model.TestRecord>>

    @Query("SELECT * FROM test_records")
    suspend fun exportAllTestRecords(): List<lumia.tracker.model.TestRecord>

    @Query("DELETE FROM test_records") suspend fun clearTestRecords()

    // For Backup / Restore
    @Query("SELECT * FROM courses")
    suspend fun exportAllCourses(): List<Course>
    
    @Query("SELECT * FROM subjects")
    suspend fun exportAllSubjects(): List<Subject>
    
    @Query("SELECT * FROM topics")
    suspend fun exportAllTopics(): List<Topic>
    
    @Query("SELECT * FROM assignments")
    suspend fun exportAllAssignments(): List<PracticeAssignment>

    @Query("SELECT * FROM attendance_records")
    suspend fun exportAllAttendance(): List<lumia.tracker.model.AttendanceRecord>

    @Query("SELECT * FROM pomodoro_sessions")
    suspend fun exportAllPomodoro(): List<lumia.tracker.model.PomodoroSession>

    @Query("SELECT * FROM action_logs")
    suspend fun exportAllActionLogs(): List<ActionLog>

    @Query("SELECT * FROM notes")
    suspend fun exportAllNotes(): List<lumia.tracker.model.Note>
    
    @Query("DELETE FROM courses") suspend fun clearCourses()
    @Query("DELETE FROM subjects") suspend fun clearSubjects()
    @Query("DELETE FROM topics") suspend fun clearTopics()
    @Query("DELETE FROM assignments") suspend fun clearAssignments()
    @Query("DELETE FROM attendance_records") suspend fun clearAttendance()
    @Query("DELETE FROM pomodoro_sessions") suspend fun clearPomodoro()
    @Query("DELETE FROM notes") suspend fun clearNotes()

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<lumia.tracker.model.Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: lumia.tracker.model.Note): Long

    @Update
    suspend fun updateNote(note: lumia.tracker.model.Note)

    @Delete
    suspend fun deleteNote(note: lumia.tracker.model.Note)

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("SELECT * FROM chapters")
    suspend fun exportAllChapters(): List<Chapter>

    @Query("SELECT * FROM chapters")
    fun getAllChaptersFlow(): Flow<List<Chapter>>

    @Query("DELETE FROM chapters")
    suspend fun clearChapters()

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

    // Tag Customizations
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

    @androidx.room.Transaction
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
