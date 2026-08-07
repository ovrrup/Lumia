package lumia.tracker.util

import android.content.Context
import android.util.Log
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

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

    fun performEmergencyBackup(context: Context, crashReason: String): String? {
        val now = System.currentTimeMillis()
        if (now - lastBackupTime.get() < 1500) return null
        if (!isBackingUp.compareAndSet(false, true)) return null
        lastBackupTime.set(now)

        return try {
            Log.i(TAG, "Starting emergency auto backup due to crash: $crashReason")
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))

            val internalDir = File(context.filesDir, "auto_crash_backups").apply { mkdirs() }
            val externalDir = try {
                context.getExternalFilesDir("auto_crash_backups")?.apply { mkdirs() }
            } catch (_: Exception) { null }

            val backupFileName = "auto_crash_backup_${timestampStr}.scholar"
            val backupFileInternal = File(internalDir, backupFileName)

            val jsonSuccess = try {
                AutoCrashBackupExporter.exportFullAppBackupSync(context, backupFileInternal)
            } catch (e: Throwable) {
                Log.e(TAG, "Full JSON export failed during crash backup", e)
                false
            }

            val rawDbDir = File(internalDir, "raw_db_$timestampStr").apply { mkdirs() }
            AutoCrashBackupExporter.copyRawDatabaseFiles(context, rawDbDir)

            if (jsonSuccess && externalDir != null && externalDir.exists()) {
                try {
                    backupFileInternal.copyTo(File(externalDir, backupFileName), overwrite = true)
                } catch (_: Exception) {}
            }

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

    fun getAutoBackups(context: Context): List<AutoBackupItem> = AutoCrashBackupHistory.getAutoBackups(context)

    fun deleteAutoBackup(context: Context, item: AutoBackupItem): Boolean = AutoCrashBackupHistory.deleteAutoBackup(context, item)

    fun restoreAutoBackup(context: Context, item: AutoBackupItem, viewModel: ScholarViewModel) = AutoCrashBackupHistory.restoreAutoBackup(item, viewModel)
}
