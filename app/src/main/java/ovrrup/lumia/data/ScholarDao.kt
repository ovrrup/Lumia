package ovrrup.lumia.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ovrrup.lumia.model.Course
import ovrrup.lumia.model.PracticeAssignment
import ovrrup.lumia.model.Subject
import ovrrup.lumia.model.Topic
import ovrrup.lumia.model.ActionLog
import ovrrup.lumia.model.Chapter
import ovrrup.lumia.model.Task
import ovrrup.lumia.model.AttendanceRecord
import ovrrup.lumia.model.PomodoroSession
import ovrrup.lumia.model.Note
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

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
    suspend fun exportAllAttendance(): List<AttendanceRecord>

    @Query("SELECT * FROM pomodoro_sessions")
    suspend fun exportAllPomodoro(): List<PomodoroSession>

    @Query("SELECT * FROM action_logs")
    suspend fun exportAllActionLogs(): List<ActionLog>

    @Query("SELECT * FROM notes")
    suspend fun exportAllNotes(): List<Note>
    
    @Query("DELETE FROM courses") suspend fun clearCourses()
    @Query("DELETE FROM subjects") suspend fun clearSubjects()
    @Query("DELETE FROM topics") suspend fun clearTopics()
    @Query("DELETE FROM assignments") suspend fun clearAssignments()
    @Query("DELETE FROM attendance_records") suspend fun clearAttendance()
    @Query("DELETE FROM pomodoro_sessions") suspend fun clearPomodoro()
    @Query("DELETE FROM notes") suspend fun clearNotes()

    @Query("SELECT * FROM notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

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
}
