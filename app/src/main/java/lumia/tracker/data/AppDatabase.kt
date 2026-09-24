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
import java.io.File
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

        /**
         * Safely inspects the SQLite table and adds the column if it doesn't already exist.
         * Guarantees zero data loss across legacy and modern schema transitions.
         */
        fun ensureColumnExists(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnTypeAndDefault: String
        ) {
            val existingColumns = mutableSetOf<String>()
            try {
                db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        while (cursor.moveToNext()) {
                            existingColumns.add(cursor.getString(nameIndex).lowercase())
                        }
                    }
                }
                if (!existingColumns.contains(columnName.lowercase())) {
                    db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnTypeAndDefault")
                    Log.i(TAG, "Safely reconciled missing column '$columnName' on table '$tableName'")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Non-critical error reconciling column $columnName in $tableName: ${e.message}")
            }
        }

        /**
         * Universal Schema Reconciler.
         * Ensures all 13 core academic tracking tables, their indexes, and columns exist,
         * preserving all user data regardless of whether the user upgraded from v1, v5, v16 (main), or v19.
         */
        fun safeReconcileSchema(db: SupportSQLiteDatabase) {
            try {
                // 1. Ensure all core entities exist
                db.execSQL("CREATE TABLE IF NOT EXISTS `courses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL DEFAULT '', `colorHex` TEXT NOT NULL DEFAULT '#3197D6', `scheduleDays` TEXT NOT NULL DEFAULT '', `scheduleStartTime` TEXT NOT NULL DEFAULT '', `scheduleEndTime` TEXT NOT NULL DEFAULT '', `instructor` TEXT NOT NULL DEFAULT '', `schedule` TEXT NOT NULL DEFAULT '', `description` TEXT NOT NULL DEFAULT '', `attendedClasses` INTEGER NOT NULL DEFAULT 0, `totalClasses` INTEGER NOT NULL DEFAULT 0, `subjectId` INTEGER, `tags` TEXT NOT NULL DEFAULT '', `subjectIds` TEXT NOT NULL DEFAULT '')")
                db.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `tags` TEXT NOT NULL DEFAULT '')")
                db.execSQL("CREATE TABLE IF NOT EXISTS `topics` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subjectId` INTEGER NOT NULL, `title` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL DEFAULT 0, `chapterId` INTEGER, `tags` TEXT NOT NULL DEFAULT '', FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseId` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '', `dueDateMillis` INTEGER NOT NULL DEFAULT 0, `isCompleted` INTEGER NOT NULL DEFAULT 0, `category` TEXT NOT NULL DEFAULT 'Homework', `categoryColor` TEXT NOT NULL DEFAULT '#3197D6', `tags` TEXT NOT NULL DEFAULT '', `subjectId` INTEGER, `priority` INTEGER NOT NULL DEFAULT 0, `orderIndex` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`courseId`) REFERENCES `courses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `attendance_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseId` INTEGER NOT NULL, `dateMillis` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`courseId`) REFERENCES `courses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `action_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `actionText` TEXT NOT NULL, `timestampMillis` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `pomodoro_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateMillis` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `subjectId` INTEGER, `courseId` INTEGER, `assignmentId` INTEGER, `taskId` INTEGER, `topicId` INTEGER)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `courseId` INTEGER, `subjectId` INTEGER, `tag` TEXT NOT NULL DEFAULT '')")
                db.execSQL("CREATE TABLE IF NOT EXISTS `chapters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subjectId` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '', `createdAt` INTEGER NOT NULL DEFAULT 0, `tags` TEXT NOT NULL DEFAULT '', FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '', `dueDateMillis` INTEGER, `isCompleted` INTEGER NOT NULL DEFAULT 0, `subjectId` INTEGER, `chapterId` INTEGER, `topicId` INTEGER, `courseId` INTEGER, `assignmentId` INTEGER, `classDateMillis` INTEGER, `priority` INTEGER NOT NULL DEFAULT 0, `orderIndex` INTEGER NOT NULL DEFAULT 0, `tags` TEXT NOT NULL DEFAULT '', `createdAt` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileType` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL DEFAULT 0, `courseId` INTEGER, `subjectId` INTEGER, `addedAt` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `test_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL DEFAULT 0, `marksObtained` REAL NOT NULL DEFAULT 0, `totalMarks` REAL NOT NULL DEFAULT 100, `notes` TEXT NOT NULL DEFAULT '', `subjectId` INTEGER, `courseId` INTEGER, `tags` TEXT NOT NULL DEFAULT '', `topicId` INTEGER)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag_customizations` (`tagName` TEXT NOT NULL, `colorHex` TEXT NOT NULL DEFAULT '', `description` TEXT NOT NULL DEFAULT '', `isFavorite` INTEGER NOT NULL DEFAULT 0, `lastUsedMillis` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`tagName`))")

                // 2. Ensure foreign key indexes exist
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_topics_subjectId` ON `topics` (`subjectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_assignments_courseId` ON `assignments` (`courseId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_courseId` ON `attendance_records` (`courseId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chapters_subjectId` ON `chapters` (`subjectId`)")

                // 3. Incrementally ensure all legacy columns are populated
                ensureColumnExists(db, "courses", "code", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "colorHex", "TEXT NOT NULL DEFAULT '#3197D6'")
                ensureColumnExists(db, "courses", "scheduleDays", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "scheduleStartTime", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "scheduleEndTime", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "instructor", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "schedule", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "description", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "attendedClasses", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "courses", "totalClasses", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "courses", "subjectId", "INTEGER")
                ensureColumnExists(db, "courses", "tags", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "courses", "subjectIds", "TEXT NOT NULL DEFAULT ''")

                ensureColumnExists(db, "subjects", "tags", "TEXT NOT NULL DEFAULT ''")

                ensureColumnExists(db, "topics", "isCompleted", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "topics", "chapterId", "INTEGER")
                ensureColumnExists(db, "topics", "tags", "TEXT NOT NULL DEFAULT ''")

                ensureColumnExists(db, "assignments", "description", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "assignments", "dueDateMillis", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "assignments", "isCompleted", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "assignments", "category", "TEXT NOT NULL DEFAULT 'Homework'")
                ensureColumnExists(db, "assignments", "categoryColor", "TEXT NOT NULL DEFAULT '#3197D6'")
                ensureColumnExists(db, "assignments", "tags", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "assignments", "subjectId", "INTEGER")
                ensureColumnExists(db, "assignments", "priority", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "assignments", "orderIndex", "INTEGER NOT NULL DEFAULT 0")

                ensureColumnExists(db, "attendance_records", "status", "TEXT NOT NULL DEFAULT 'Present'")

                ensureColumnExists(db, "pomodoro_sessions", "subjectId", "INTEGER")
                ensureColumnExists(db, "pomodoro_sessions", "courseId", "INTEGER")
                ensureColumnExists(db, "pomodoro_sessions", "assignmentId", "INTEGER")
                ensureColumnExists(db, "pomodoro_sessions", "taskId", "INTEGER")
                ensureColumnExists(db, "pomodoro_sessions", "topicId", "INTEGER")

                ensureColumnExists(db, "notes", "courseId", "INTEGER")
                ensureColumnExists(db, "notes", "subjectId", "INTEGER")
                ensureColumnExists(db, "notes", "tag", "TEXT NOT NULL DEFAULT ''")

                ensureColumnExists(db, "chapters", "description", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "chapters", "createdAt", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "chapters", "tags", "TEXT NOT NULL DEFAULT ''")

                ensureColumnExists(db, "tasks", "description", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "tasks", "dueDateMillis", "INTEGER")
                ensureColumnExists(db, "tasks", "isCompleted", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "tasks", "subjectId", "INTEGER")
                ensureColumnExists(db, "tasks", "chapterId", "INTEGER")
                ensureColumnExists(db, "tasks", "topicId", "INTEGER")
                ensureColumnExists(db, "tasks", "courseId", "INTEGER")
                ensureColumnExists(db, "tasks", "assignmentId", "INTEGER")
                ensureColumnExists(db, "tasks", "classDateMillis", "INTEGER")
                ensureColumnExists(db, "tasks", "priority", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "tasks", "orderIndex", "INTEGER NOT NULL DEFAULT 0")
                ensureColumnExists(db, "tasks", "tags", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "tasks", "createdAt", "INTEGER NOT NULL DEFAULT 0")

                ensureColumnExists(db, "test_records", "notes", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "test_records", "subjectId", "INTEGER")
                ensureColumnExists(db, "test_records", "courseId", "INTEGER")
                ensureColumnExists(db, "test_records", "tags", "TEXT NOT NULL DEFAULT ''")
                ensureColumnExists(db, "test_records", "topicId", "INTEGER")
            } catch (e: Exception) {
                Log.e(TAG, "Schema reconciliation non-fatal exception: ${e.message}", e)
            }
        }

        // =====================================================================
        // INDIVIDUAL MIGRATIONS (Preserves legacy paths & specific transforms)
        // =====================================================================

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
                db.execSQL("UPDATE attendance_records SET dateMillis = (dateMillis / 86400000) * 86400000")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeReconcileSchema(db)
            }
        }

        // Build continuous migrations for all intermediate steps: 1->2, 2->3 ... 19->20
        private fun buildStepMigrations(): List<Migration> {
            val list = mutableListOf<Migration>()
            for (v in 1..19) {
                // Avoid duplicating explicitly defined migrations
                when (v) {
                    5 -> list.add(MIGRATION_5_6)
                    12 -> list.add(MIGRATION_12_13)
                    14 -> list.add(MIGRATION_14_15)
                    16 -> list.add(MIGRATION_16_17)
                    17 -> list.add(MIGRATION_17_18)
                    18 -> list.add(MIGRATION_18_19)
                    19 -> list.add(MIGRATION_19_20)
                    else -> {
                        val next = v + 1
                        list.add(object : Migration(v, next) {
                            override fun migrate(db: SupportSQLiteDatabase) {
                                safeReconcileSchema(db)
                            }
                        })
                    }
                }
            }
            return list
        }

        // Build direct migrations from any version 1..19 straight to version 20
        private fun buildDirectToLatestMigrations(): List<Migration> {
            val list = mutableListOf<Migration>()
            for (v in 1 until 20) {
                if (v == 19) continue // 19->20 is already handled in step migrations
                list.add(object : Migration(v, 20) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        safeReconcileSchema(db)
                    }
                })
            }
            return list
        }

        // Safe downgrade migrations that preserve all tables if user switches between branches
        private fun buildSafeDowngradeMigrations(): List<Migration> {
            val list = mutableListOf<Migration>()
            for (v in 20 downTo 2) {
                val prev = v - 1
                list.add(object : Migration(v, prev) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Safe no-op: SQLite tables and columns remain intact for older builds
                    }
                })
            }
            // Direct downgrade shortcuts
            list.add(object : Migration(20, 16) {
                override fun migrate(db: SupportSQLiteDatabase) {}
            })
            list.add(object : Migration(20, 1) {
                override fun migrate(db: SupportSQLiteDatabase) {}
            })
            return list
        }

        private val ALL_MIGRATIONS: Array<Migration> by lazy {
            val combined = mutableListOf<Migration>()
            combined.addAll(buildStepMigrations())
            combined.addAll(buildDirectToLatestMigrations())
            combined.addAll(buildSafeDowngradeMigrations())
            combined.toTypedArray()
        }

        /**
         * Creates an automatic, isolated safety snapshot of the SQLite database files
         * before any Room upgrade or modification occurs.
         */
        fun createAutomaticSafetySnapshot(context: Context, dbName: String) {
            try {
                val dbFile = context.getDatabasePath(dbName)
                if (!dbFile.exists() || dbFile.length() == 0L) return

                val backupDir = File(context.filesDir, "database_backups").apply { mkdirs() }
                val targetFile = File(backupDir, "${dbName}_pre_upgrade.bak")
                dbFile.copyTo(targetFile, overwrite = true)

                val wal = File(dbFile.path + "-wal")
                if (wal.exists()) wal.copyTo(File(backupDir, "${dbName}_pre_upgrade.bak-wal"), overwrite = true)
                val shm = File(dbFile.path + "-shm")
                if (shm.exists()) shm.copyTo(File(backupDir, "${dbName}_pre_upgrade.bak-shm"), overwrite = true)

                Log.i(TAG, "Created automatic pre-upgrade safety snapshot for $dbName (${dbFile.length()} bytes)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not create automatic safety snapshot for $dbName: ${e.message}")
            }
        }

        /**
         * Restores the safety snapshot file for a database if corruption or failure is detected.
         */
        fun restoreSafetySnapshot(context: Context, dbName: String): Boolean {
            return try {
                val backupDir = File(context.filesDir, "database_backups")
                val snapshotFile = File(backupDir, "${dbName}_pre_upgrade.bak")
                if (!snapshotFile.exists() || snapshotFile.length() == 0L) return false

                val dbFile = context.getDatabasePath(dbName)
                snapshotFile.copyTo(dbFile, overwrite = true)

                val walSnapshot = File(backupDir, "${dbName}_pre_upgrade.bak-wal")
                val targetWal = File(dbFile.path + "-wal")
                if (walSnapshot.exists()) walSnapshot.copyTo(targetWal, overwrite = true) else targetWal.delete()

                val shmSnapshot = File(backupDir, "${dbName}_pre_upgrade.bak-shm")
                val targetShm = File(dbFile.path + "-shm")
                if (shmSnapshot.exists()) shmSnapshot.copyTo(targetShm, overwrite = true) else targetShm.delete()

                Log.i(TAG, "Successfully restored safety snapshot for $dbName")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore safety snapshot for $dbName", e)
                false
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

                    // 1. Create automatic pre-upgrade safety snapshot if DB file already exists
                    createAutomaticSafetySnapshot(context, dbName)

                    // 2. Build Room database with continuous migrations and ZERO destructive fallbacks
                    val builder = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    ).addMigrations(*ALL_MIGRATIONS)

                    val instance = try {
                        val dbInstance = builder.build()
                        // Ensure database open succeeds without throwing unhandled migration error
                        dbInstance.openHelper.writableDatabase
                        dbInstance
                    } catch (e: Exception) {
                        Log.e(TAG, "Migration encountered an issue for $dbName, attempting safe recovery", e)
                        restoreSafetySnapshot(context, dbName)
                        // Retry opening with rebuilt instance
                        val recoveredBuilder = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            dbName
                        ).addMigrations(*ALL_MIGRATIONS)
                        val recoveredInstance = recoveredBuilder.build()
                        try {
                            safeReconcileSchema(recoveredInstance.openHelper.writableDatabase)
                        } catch (re: Exception) {
                            Log.e(TAG, "Post-recovery schema reconcile non-fatal: ${re.message}")
                        }
                        recoveredInstance
                    }

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
