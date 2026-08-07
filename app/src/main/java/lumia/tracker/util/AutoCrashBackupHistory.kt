package lumia.tracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONArray
import java.io.File
import java.util.Locale

object AutoCrashBackupHistory {
    private const val TAG = "AutoCrashBackupHistory"

    fun getAutoBackups(context: Context): List<AutoBackupItem> {
        val list = mutableListOf<AutoBackupItem>()
        try {
            val prefs = context.getSharedPreferences("auto_crash_backups_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("backup_history", "[]") ?: "[]"
            val jsonArray = JSONArray(historyJson)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val filePath = obj.optString("filePath")
                val file = File(filePath)
                if (file.exists() && file.length() > 0) {
                    val timestamp = obj.optLong("timestamp", file.lastModified())
                    val bytes = file.length()
                    val sizeFormatted = formatFileSize(bytes)
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
            val internalDir = File(context.filesDir, "auto_crash_backups")
            internalDir.listFiles { _, name -> name.endsWith(".scholar") }?.forEach { f ->
                if (list.none { it.filePath == f.absolutePath }) {
                    list.add(
                        AutoBackupItem(
                            fileName = f.name,
                            filePath = f.absolutePath,
                            timestamp = f.lastModified(),
                            reason = "Auto Crash Safeguard",
                            hasJson = true,
                            fileSizeFormatted = formatFileSize(f.length())
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading auto backups", e)
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes > 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            bytes > 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    fun deleteAutoBackup(context: Context, item: AutoBackupItem): Boolean {
        return try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
            val prefs = context.getSharedPreferences("auto_crash_backups_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("backup_history", "[]") ?: "[]"
            val jsonArray = JSONArray(historyJson)
            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                if (obj.optString("filePath") != item.filePath) newArray.put(obj)
            }
            prefs.edit().putString("backup_history", newArray.toString()).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreAutoBackup(item: AutoBackupItem, viewModel: ScholarViewModel) {
        viewModel.importData(Uri.fromFile(File(item.filePath)))
    }
}
