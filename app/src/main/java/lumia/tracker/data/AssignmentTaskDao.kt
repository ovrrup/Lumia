package lumia.tracker.data

import androidx.room.*
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Task
import kotlinx.coroutines.flow.Flow

interface AssignmentTaskDao {
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

    @Query("SELECT * FROM tasks ORDER BY priority DESC, orderIndex ASC, dueDateMillis ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM assignments")
    suspend fun exportAllAssignments(): List<PracticeAssignment>

    @Query("SELECT * FROM tasks")
    suspend fun exportAllTasks(): List<Task>

    @Query("DELETE FROM assignments") suspend fun clearAssignments()
    @Query("DELETE FROM tasks") suspend fun clearTasks()
}
