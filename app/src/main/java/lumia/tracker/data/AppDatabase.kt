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
import lumia.tracker.model.TestRecord
import lumia.tracker.model.TagCustomization
import lumia.tracker.kost.KostBehaviorEvent
import lumia.tracker.kost.KostPatternReport
import lumia.tracker.kost.KostDao

@Database(entities = [Course::class, Subject::class, Topic::class, PracticeAssignment::class, ActionLog::class, AttendanceRecord::class, PomodoroSession::class, Note::class, Chapter::class, Task::class, Attachment::class, TestRecord::class, TagCustomization::class, KostBehaviorEvent::class, KostPatternReport::class], version = 21, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scholarDao(): ScholarDao
    abstract fun kostDao(): KostDao

    companion object {
        private val instances = java.util.concurrent.ConcurrentHashMap<String, AppDatabase>()

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

        val MIGRATION_16_17 = object : androidx.room.migration.Migration(16, 17) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `test_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `marksObtained` REAL NOT NULL, `totalMarks` REAL NOT NULL, `notes` TEXT NOT NULL, `subjectId` INTEGER, `courseId` INTEGER)")
            }
        }

        val MIGRATION_17_18 = object : androidx.room.migration.Migration(17, 18) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE test_records ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE test_records ADD COLUMN topicId INTEGER")
            }
        }

        val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN topicId INTEGER")
            }
        }

        val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag_customizations` (`tagName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `description` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `lastUsedMillis` INTEGER NOT NULL, PRIMARY KEY(`tagName`))")
            }
        }

        val MIGRATION_20_21 = object : androidx.room.migration.Migration(20, 21) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `kost_behavior_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `action` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `durationMillis` INTEGER, `rating` REAL, `performanceMetric` REAL, `tagString` TEXT NOT NULL, `description` TEXT NOT NULL, `metadataJson` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `kost_pattern_reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `generatedAt` INTEGER NOT NULL, `summary` TEXT NOT NULL, `insightsJson` TEXT NOT NULL, `modelAccuracyMetric` REAL NOT NULL, `actionPlanSuggestions` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context, forceProfileId: String? = null): AppDatabase {
            val profMgr = lumia.tracker.data.ProfileManager(context)
            val profileId = forceProfileId ?: profMgr.getActiveProfileId()
            
            return instances[profileId] ?: synchronized(this) {
                instances[profileId] ?: run {
                    val dbName = if (profileId == "DEFAULT") "scholar_sync_database" else "scholar_sync_$profileId"
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    )
                    .addMigrations(MIGRATION_5_6, MIGRATION_12_13, MIGRATION_14_15, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    instances[profileId] = instance
                    instance
                }
            }
        }
        
        fun clearInstances() {
            instances.clear()
        }
    }
}
