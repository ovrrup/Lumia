package ovrrup.lumia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ovrrup.lumia.model.*

@Database(
    entities = [
        Course::class,
        Subject::class,
        Topic::class,
        Chapter::class,
        PracticeAssignment::class,
        Task::class,
        ActionLog::class,
        CustomFont::class,
        PomodoroSession::class,
        Note::class,
        AttendanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scholarDao(): ScholarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scholar_sync_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
