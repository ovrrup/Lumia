package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Course
import com.example.model.PracticeAssignment
import com.example.model.Subject
import com.example.model.Topic
import com.example.model.ActionLog
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


    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDateMillis ASC")
    fun getAssignmentsForCourse(courseId: Int): Flow<List<PracticeAssignment>>

    @Query("SELECT * FROM assignments ORDER BY dueDateMillis ASC")
    fun getAllAssignments(): Flow<List<PracticeAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: PracticeAssignment)

    @Update
    suspend fun updateAssignment(assignment: PracticeAssignment)

    @Delete
    suspend fun deleteAssignment(assignment: PracticeAssignment)

    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getAttendanceForCourse(courseId: Int): Flow<List<com.example.model.AttendanceRecord>>
    
    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC")
    fun getAllAttendanceRecords(): Flow<List<com.example.model.AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: com.example.model.AttendanceRecord)

    @Update
    suspend fun updateAttendanceRecord(record: com.example.model.AttendanceRecord)

    @Delete
    suspend fun deleteAttendanceRecord(record: com.example.model.AttendanceRecord)
    
    @Query("SELECT * FROM action_logs ORDER BY timestampMillis DESC")
    fun getAllActionLogs(): Flow<List<ActionLog>>

    @Insert
    suspend fun insertActionLog(log: ActionLog)

    @Query("DELETE FROM action_logs")
    suspend fun clearActionLogs()
    
    // For Backup / Restore
    @Query("SELECT * FROM courses")
    suspend fun exportAllCourses(): List<Course>
    
    @Query("SELECT * FROM subjects")
    suspend fun exportAllSubjects(): List<Subject>
    
    @Query("SELECT * FROM topics")
    suspend fun exportAllTopics(): List<Topic>
    
    @Query("SELECT * FROM assignments")
    suspend fun exportAllAssignments(): List<PracticeAssignment>
    
    @Query("DELETE FROM courses")
    suspend fun clearAll()
}
