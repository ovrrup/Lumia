package lumia.tracker.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lumia.tracker.BuildConfig
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.FullAppBackup
import lumia.tracker.model.ScholarBackup
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.GZIPOutputStream

data class AutoBackupItem(
    val fileName: String,
    val filePath: String,
    val timestamp: Long,
    val reason: String,
    val hasJson: Boolean,
    val fileSizeFormatted: String
)

object AutoCrashBackupManager {

    private const val TAG = "AutoCrashBackupManager"
    private const val MAX_BACKUPS_TO_KEEP = 15
    private val isBackingUp = AtomicBoolean(false)
    private val lastBackupTime = AtomicLong(0L)

    /**
     * Installs global uncaught exception handler as a crash safety net.
     */
    fun setup(context: Context) {
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                performEmergencyBackup(
                    context,
                    "Crash in thread [${thread.name}]: ${throwable.javaClass.simpleName} - ${throwable.message ?: "No message"}"
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Error performing auto crash backup", e)
            } finally {
                originalHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    /**
     * Synchronously creates a full backup package (.scholar) and raw database copies during crash.
     * Guaranteed to execute before process termination.
     */
    fun performEmergencyBackup(context: Context, crashReason: String): String? {
        val now = System.currentTimeMillis()
        if (now - lastBackupTime.get() < 1500) {
            Log.w(TAG, "Auto crash backup skipped (triggered too quickly)")
            return null
        }
        if (!isBackingUp.compareAndSet(false, true)) {
            return null
        }
        lastBackupTime.set(now)

        return try {
            Log.i(TAG, "Starting emergency auto backup due to crash: $crashReason")
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))

            val internalDir = File(context.filesDir, "auto_crash_backups").apply { mkdirs() }
            val externalDir = try {
                context.getExternalFilesDir("auto_crash_backups")?.apply { mkdirs() }
            } catch (_: Exception) {
                null
            }

            val backupFileName = "auto_crash_backup_${timestampStr}.scholar"
            val backupFileInternal = File(internalDir, backupFileName)

            // Step 1: Export JSON/GZIP full application backup
            val jsonSuccess = try {
                exportFullAppBackupSync(context, backupFileInternal)
            } catch (e: Throwable) {
                Log.e(TAG, "Full JSON export failed during crash backup", e)
                false
            }

            // Step 2: Copy raw SQLite database files as secondary safety net
            val rawDbDir = File(internalDir, "raw_db_$timestampStr").apply { mkdirs() }
            copyRawDatabaseFiles(context, rawDbDir)

            // Copy to external storage if accessible
            if (jsonSuccess && externalDir != null && externalDir.exists()) {
                try {
                    backupFileInternal.copyTo(File(externalDir, backupFileName), overwrite = true)
                } catch (_: Exception) {}
            }

            // Save history metadata
            val prefs = context.getSharedPreferences("auto_crash_backups_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("backup_history", "[]") ?: "[]"
            val jsonArray = try { JSONArray(historyJson) } catch (_: Exception) { JSONArray() }
            val meta = JSONObject().apply {
                put("fileName", backupFileName)
                put("filePath", backupFileInternal.absolutePath)
                put("timestamp", now)
                put("reason", crashReason)
                put("hasJson", jsonSuccess)
                put("fileSize", backupFileInternal.length())
            }
            jsonArray.put(meta)

            // Trim old records
            while (jsonArray.length() > MAX_BACKUPS_TO_KEEP) {
                val oldest = jsonArray.optJSONObject(0)
                if (oldest != null) {
                    val path = oldest.optString("filePath")
                    if (!path.isNullOrEmpty()) {
                        try { File(path).delete() } catch (_: Exception) {}
                    }
                }
                jsonArray.remove(0)
            }
            prefs.edit().putString("backup_history", jsonArray.toString()).apply()

            pruneOldBackups(internalDir)
            if (externalDir != null) pruneOldBackups(externalDir)

            Log.i(TAG, "Emergency auto backup completed: ${backupFileInternal.absolutePath}")
            backupFileInternal.absolutePath
        } catch (e: Throwable) {
            Log.e(TAG, "Fatal failure during emergency auto backup", e)
            null
        } finally {
            isBackingUp.set(false)
        }
    }

    private fun exportFullAppBackupSync(context: Context, outputFile: File): Boolean {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val backupAdapter = moshi.adapter(ScholarBackup::class.java)
            val fullAdapter = moshi.adapter(FullAppBackup::class.java)

            val profileManager = ProfileManager(context)
            val allProfs = profileManager.getAllProfiles()
            val profileBackupsJson = mutableMapOf<String, String>()

            runBlocking(Dispatchers.IO) {
                for (prof in allProfs) {
                    try {
                        val db = AppDatabase.getDatabase(context, prof.id)
                        val profDao = db.scholarDao()
                        val pref = profileManager.getProfilePrefs(prof.id)
                        val sets = pref.all.mapValues { it.value?.toString() ?: "" }
                        val profAttachments = profDao.exportAllAttachments()

                        val pBackup = ScholarBackup(
                            courses = profDao.exportAllCourses(),
                            subjects = profDao.exportAllSubjects(),
                            topics = profDao.exportAllTopics(),
                            assignments = profDao.exportAllAssignments(),
                            settings = sets,
                            attendance = profDao.exportAllAttendance(),
                            pomodoro = profDao.exportAllPomodoro(),
                            actionLogs = profDao.exportAllActionLogs(),
                            notes = profDao.exportAllNotes(),
                            chapters = profDao.exportAllChapters(),
                            tasks = profDao.exportAllTasks(),
                            attachments = profAttachments,
                            testRecords = profDao.exportAllTestRecords(),
                            profile = prof
                        )
                        profileBackupsJson[prof.id] = backupAdapter.toJson(pBackup)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error exporting profile ${prof.id} during emergency backup", e)
                    }
                }
            }

            val fullAppBackup = FullAppBackup(
                profiles = allProfs,
                activeProfileId = profileManager.getActiveProfileId(),
                globalPrefs = emptyMap(),
                profileBackupsJson = profileBackupsJson
            )

            val mainBackup = ScholarBackup(isFullAppBackup = true, fullAppBackupJson = fullAdapter.toJson(fullAppBackup))
            val jsonStr = backupAdapter.toJson(mainBackup)

            FileOutputStream(outputFile).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    gzos.writer().use { writer ->
                        writer.write(jsonStr)
                    }
                }
            }
            true
        } catch (e: Throwable) {
            Log.e(TAG, "exportFullAppBackupSync error", e)
            false
        }
    }

    private fun copyRawDatabaseFiles(context: Context, destDir: File) {
        try {
            val dbFolder = context.getDatabasePath("scholar_sync_database").parentFile ?: return
            dbFolder.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.contains("scholar") || file.name.endsWith(".db") || file.name.endsWith(".db-wal") || file.name.endsWith(".db-shm"))) {
                    file.copyTo(File(destDir, file.name), overwrite = true)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed copying raw DB files", e)
        }
    }

    private fun pruneOldBackups(dir: File) {
        try {
            val files = dir.listFiles { _, name -> name.startsWith("auto_crash_backup_") && name.endsWith(".scholar") }
            if (files != null && files.size > MAX_BACKUPS_TO_KEEP) {
                files.sortBy { it.lastModified() }
                val deleteCount = files.size - MAX_BACKUPS_TO_KEEP
                for (i in 0 until deleteCount) {
                    files[i].delete()
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error pruning old backups", e)
        }
    }

    fun getAutoBackups(context: Context): List<AutoBackupItem> {
        val list = mutableListOf<AutoBackupItem>()
        try {
            val prefs = context.getSharedPreferences("auto_crash_backups_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("backup_history", "[]") ?: "[]"
            val jsonArray = JSONArray(historyJson)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm:ss", Locale.getDefault())

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val filePath = obj.optString("filePath")
                val file = File(filePath)
                if (file.exists() && file.length() > 0) {
                    val timestamp = obj.optLong("timestamp", file.lastModified())
                    val bytes = file.length()
                    val sizeFormatted = when {
                        bytes > 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
                        bytes > 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                        else -> "$bytes B"
                    }
                    list.add(
                        AutoBackupItem(
                            fileName = obj.optString("fileName", file.name),
                            filePath = filePath,
                            timestamp = timestamp,
                            reason = obj.optString("reason", "Emergency Auto Backup"),
                            hasJson = obj.optBoolean("hasJson", true),
                            fileSizeFormatted = sizeFormatted
                        )
                    )
                }
            }
            // Also scan filesDir directory in case unrecorded files exist
            val internalDir = File(context.filesDir, "auto_crash_backups")
            internalDir.listFiles { _, name -> name.endsWith(".scholar") }?.forEach { f ->
                if (list.none { it.filePath == f.absolutePath }) {
                    val bytes = f.length()
                    val sizeFormatted = when {
                        bytes > 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
                        bytes > 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                        else -> "$bytes B"
                    }
                    list.add(
                        AutoBackupItem(
                            fileName = f.name,
                            filePath = f.absolutePath,
                            timestamp = f.lastModified(),
                            reason = "Auto Crash Safeguard",
                            hasJson = true,
                            fileSizeFormatted = sizeFormatted
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading auto backups", e)
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun deleteAutoBackup(context: Context, item: AutoBackupItem): Boolean {
        return try {
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
            val prefs = context.getSharedPreferences("auto_crash_backups_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("backup_history", "[]") ?: "[]"
            val jsonArray = JSONArray(historyJson)
            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                if (obj.optString("filePath") != item.filePath) {
                    newArray.put(obj)
                }
            }
            prefs.edit().putString("backup_history", newArray.toString()).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreAutoBackup(context: Context, item: AutoBackupItem, viewModel: ScholarViewModel) {
        viewModel.importData(Uri.fromFile(File(item.filePath)))
    }
}
