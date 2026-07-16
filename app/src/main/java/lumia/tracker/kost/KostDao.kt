package lumia.tracker.kost

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KostDao {
    @Query("SELECT * FROM kost_behavior_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<KostBehaviorEvent>>

    @Query("SELECT * FROM kost_behavior_events WHERE category = :category ORDER BY timestamp DESC")
    fun getEventsByCategory(category: String): Flow<List<KostBehaviorEvent>>

    @Query("SELECT * FROM kost_behavior_events ORDER BY timestamp DESC")
    suspend fun getAllEventsList(): List<KostBehaviorEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: KostBehaviorEvent): Long

    @Delete
    suspend fun deleteEvent(event: KostBehaviorEvent)

    @Query("DELETE FROM kost_behavior_events")
    suspend fun clearEvents()

    @Query("SELECT * FROM kost_pattern_reports ORDER BY generatedAt DESC LIMIT 1")
    fun getLatestReport(): Flow<KostPatternReport?>

    @Query("SELECT * FROM kost_pattern_reports ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestReportList(): List<KostPatternReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: KostPatternReport): Long

    @Query("DELETE FROM kost_pattern_reports")
    suspend fun clearReports()
}
