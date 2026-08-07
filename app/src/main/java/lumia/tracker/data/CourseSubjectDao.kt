package lumia.tracker.data

import androidx.room.*
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.Chapter
import kotlinx.coroutines.flow.Flow

interface CourseSubjectDao {
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

    @Query("SELECT * FROM courses")
    suspend fun exportAllCourses(): List<Course>

    @Query("SELECT * FROM subjects")
    suspend fun exportAllSubjects(): List<Subject>

    @Query("SELECT * FROM topics")
    suspend fun exportAllTopics(): List<Topic>

    @Query("DELETE FROM courses") suspend fun clearCourses()
    @Query("DELETE FROM subjects") suspend fun clearSubjects()
    @Query("DELETE FROM topics") suspend fun clearTopics()
    @Query("DELETE FROM chapters") suspend fun clearChapters()
}
