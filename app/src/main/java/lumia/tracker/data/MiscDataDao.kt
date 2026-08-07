package lumia.tracker.data

import androidx.room.*
import lumia.tracker.model.*
import kotlinx.coroutines.flow.Flow

interface MiscDataDao {
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
    
    @Query("SELECT * FROM action_logs ORDER BY timestampMillis DESC")
    fun getAllActionLogs(): Flow<List<ActionLog>>

    @Insert
    suspend fun insertActionLog(log: ActionLog)

    @Query("DELETE FROM action_logs")
    suspend fun clearActionLogs()
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY dateMillis DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>

    @Insert
    suspend fun insertPomodoroSession(session: PomodoroSession)

    @Update
    suspend fun updatePomodoroSession(session: PomodoroSession)

    @Delete
    suspend fun deletePomodoroSession(session: PomodoroSession)

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

    @Query("DELETE FROM test_records") suspend fun clearTestRecords()

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

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

    @Query("SELECT * FROM tag_customizations ORDER BY tagName ASC")
    fun getAllTagCustomizations(): Flow<List<TagCustomization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCustomization(customization: TagCustomization)

    @Update
    suspend fun updateTagCustomization(customization: TagCustomization)

    @Delete
    suspend fun deleteTagCustomization(customization: TagCustomization)

    @Query("SELECT * FROM flashcards ORDER BY nextReviewMillis ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId ORDER BY nextReviewMillis ASC")
    fun getFlashcardsForSubject(subjectId: Int): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)

    @Query("SELECT * FROM attendance_records") suspend fun exportAllAttendance(): List<AttendanceRecord>
    @Query("SELECT * FROM pomodoro_sessions") suspend fun exportAllPomodoro(): List<PomodoroSession>
    @Query("SELECT * FROM action_logs") suspend fun exportAllActionLogs(): List<ActionLog>
    @Query("SELECT * FROM notes") suspend fun exportAllNotes(): List<Note>
    @Query("SELECT * FROM attachments") suspend fun exportAllAttachments(): List<Attachment>
    @Query("SELECT * FROM tag_customizations") suspend fun exportAllTagCustomizations(): List<TagCustomization>
    @Query("SELECT * FROM flashcards") suspend fun exportAllFlashcards(): List<Flashcard>

    @Query("DELETE FROM attendance_records") suspend fun clearAttendance()
    @Query("DELETE FROM pomodoro_sessions") suspend fun clearPomodoro()
    @Query("DELETE FROM notes") suspend fun clearNotes()
    @Query("DELETE FROM attachments") suspend fun clearAttachments()
    @Query("DELETE FROM tag_customizations") suspend fun clearTagCustomizations()
    @Query("DELETE FROM flashcards") suspend fun clearFlashcards()
}
