package lumia.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lumia.tracker.model.AttendanceRecord
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.ActionLog
import lumia.tracker.model.PomodoroSession
import lumia.tracker.model.Note
import lumia.tracker.model.Chapter
import lumia.tracker.model.Task
import lumia.tracker.model.Attachment

@Database(entities = [Course::class, Subject::class, Topic::class, PracticeAssignment::class, ActionLog::class, AttendanceRecord::class, PomodoroSession::class, Note::class, Chapter::class, Task::class, Attachment::class], version = 16, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scholarDao(): ScholarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("UPDATE attendance_records SET dateMillis = (dateMillis / 86400000) * 86400000")
            }
        }

        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN courseId INTEGER")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN assignmentId INTEGER")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN taskId INTEGER")
            }
        }
        
        val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assignments ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE assignments ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scholar_sync_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_12_13, MIGRATION_14_15)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
