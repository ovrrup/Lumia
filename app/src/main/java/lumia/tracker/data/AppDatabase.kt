package lumia.tracker.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Attachment
import lumia.tracker.model.AttendanceRecord
import lumia.tracker.model.Chapter
import lumia.tracker.model.Course
import lumia.tracker.model.Note
import lumia.tracker.model.PomodoroSession
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.TagCustomization
import lumia.tracker.model.Task
import lumia.tracker.model.TestRecord
import lumia.tracker.model.Topic
import java.util.concurrent.ConcurrentHashMap

@Database(
    entities = [
        Course::class,
        Subject::class,
        Topic::class,
        PracticeAssignment::class,
        ActionLog::class,
        AttendanceRecord::class,
        PomodoroSession::class,
        Note::class,
        Chapter::class,
        Task::class,
        Attachment::class,
        TestRecord::class,
        TagCustomization::class
    ],
    version = 20,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scholarDao(): ScholarDao

    companion object {
        private const val TAG = "AppDatabase"
        private val instances = ConcurrentHashMap<String, AppDatabase>()

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE attendance_records SET dateMillis = (dateMillis / 86400000) * 86400000")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN courseId INTEGER")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN assignmentId INTEGER")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN taskId INTEGER")
            }
        }
        
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assignments ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE assignments ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `test_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `marksObtained` REAL NOT NULL, `totalMarks` REAL NOT NULL, `notes` TEXT NOT NULL, `subjectId` INTEGER, `courseId` INTEGER)")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE test_records ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE test_records ADD COLUMN topicId INTEGER")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN topicId INTEGER")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag_customizations` (`tagName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `description` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `lastUsedMillis` INTEGER NOT NULL, PRIMARY KEY(`tagName`))")
            }
        }

        fun getDatabase(context: Context, forceProfileId: String? = null): AppDatabase {
            val profMgr = ProfileManager(context)
            val profileId = forceProfileId ?: profMgr.getActiveProfileId()

            val existing = instances[profileId]
            if (existing != null && existing.isOpen) {
                return existing
            }

            return synchronized(this) {
                val current = instances[profileId]
                if (current != null && current.isOpen) {
                    current
                } else {
                    current?.let {
                        try {
                            if (it.isOpen) it.close()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error closing stale database for $profileId", e)
                        }
                    }

                    val dbName = if (profileId == ProfileManager.DEFAULT_PROFILE_ID) {
                        "scholar_sync_database"
                    } else {
                        "scholar_sync_$profileId"
                    }

                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    )
                    .addMigrations(
                        MIGRATION_5_6,
                        MIGRATION_12_13,
                        MIGRATION_14_15,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20
                    )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()

                    instances[profileId] = instance
                    instance
                }
            }
        }

        fun closeDatabase(profileId: String) {
            synchronized(this) {
                instances.remove(profileId)?.let { db ->
                    try {
                        if (db.isOpen) {
                            db.close()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error closing database for profile $profileId", e)
                    }
                }
            }
        }

        fun deleteDatabaseFiles(context: Context, profileId: String) {
            val dbName = if (profileId == ProfileManager.DEFAULT_PROFILE_ID) {
                "scholar_sync_database"
            } else {
                "scholar_sync_$profileId"
            }
            context.deleteDatabase(dbName)
        }

        fun clearInstances() {
            synchronized(this) {
                instances.values.forEach { db ->
                    try {
                        if (db.isOpen) {
                            db.close()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error closing database instance during clear", e)
                    }
                }
                instances.clear()
            }
        }

        suspend fun defragmentDatabase(context: Context, profileId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val db = getDatabase(context, profileId)
                val writableDb = db.openHelper.writableDatabase

                try {
                    writableDb.execSQL("PRAGMA wal_checkpoint(FULL)")
                } catch (e: Exception) {
                    Log.w(TAG, "WAL checkpoint before VACUUM non-critical warning: ${e.message}")
                }

                writableDb.execSQL("VACUUM")

                try {
                    writableDb.execSQL("PRAGMA optimize")
                } catch (e: Exception) {
                    Log.w(TAG, "PRAGMA optimize warning: ${e.message}")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to defragment SQLite database", e)
                Result.failure(e)
            }
        }
    }
}
