package ovrrup.lumia.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ovrrup.lumia.model.*

@Dao
interface ScholarDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM assignments")
    fun getAllAssignments(): Flow<List<PracticeAssignment>>

    @Query("SELECT * FROM custom_fonts")
    fun getAllCustomFonts(): Flow<List<CustomFont>>

    @Query("SELECT * FROM action_logs ORDER BY timestampMillis DESC")
    fun getAllActionLogs(): Flow<List<ActionLog>>

    @Query("SELECT * FROM tasks ORDER BY priority ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY dateMillis DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId")
    fun getChaptersForSubject(subjectId: Int): Flow<List<Chapter>>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId")
    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>>

    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId")
    fun getAttendanceForCourse(courseId: Int): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomFont(font: CustomFont)

    @Update
    suspend fun updateCustomFont(font: CustomFont)

    @Delete
    suspend fun deleteCustomFont(font: CustomFont)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord)

    @Update
    suspend fun updateAttendanceRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendanceRecord(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Update
    suspend fun updateTasks(tasks: List<Task>)

    @Delete
    suspend fun deleteTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: PracticeAssignment): Long

    @Update
    suspend fun updateAssignment(assignment: PracticeAssignment)

    @Update
    suspend fun updateAssignments(assignments: List<PracticeAssignment>)

    @Delete
    suspend fun deleteAssignment(assignment: PracticeAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionLog(log: ActionLog)

    @Query("DELETE FROM courses")
    suspend fun clearCourses()

    @Query("DELETE FROM assignments")
    suspend fun clearAssignments()

    @Query("DELETE FROM custom_fonts")
    suspend fun clearCustomFonts()

    @Query("DELETE FROM action_logs")
    suspend fun clearActionLogs()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearPomodoroSessions()

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM topics")
    suspend fun clearTopics()

    @Query("DELETE FROM chapters")
    suspend fun clearChapters()

    @Query("DELETE FROM attendance_records")
    suspend fun clearAttendanceRecords()
}
