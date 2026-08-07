package lumia.tracker.util

import android.content.Context
import android.os.Build
import android.util.Log
import lumia.tracker.BuildConfig
import org.json.JSONArray

object LogDog {
    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val now = System.currentTimeMillis()
            val prefs = context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            val lastCrash = prefs.getLong("last_crash_timestamp", 0L)
            
            if (now - lastCrash < 4500) {
                Log.e("LogDog", "Subsequent crash detected too quickly! Aborting to prevent restart loop.")
                defaultHandler?.uncaughtException(thread, throwable)
                return@setDefaultUncaughtExceptionHandler
            }
            
            val stackTrace = throwable.stackTraceToString()
            val crashInfo = """
                Device: ${Build.MODEL}
                Version: ${BuildConfig.VERSION_NAME}
                Error: $stackTrace
            """.trimIndent()
            
            Log.e("LogDog", "Crash captured: $crashInfo")
            
            try {
                AutoCrashBackupManager.performEmergencyBackup(
                    context,
                    "LogDog: ${throwable.javaClass.simpleName} - ${throwable.message ?: "Fatal Exception"}"
                )
            } catch (backupErr: Throwable) {
                Log.e("LogDog", "Auto crash backup failed", backupErr)
            }

            val crashes = try {
                JSONArray(prefs.getString("crashes", "[]") ?: "[]")
            } catch (e: Exception) {
                JSONArray()
            }
            if (crashes.length() >= 5) {
                crashes.remove(0)
            }
            crashes.put(crashInfo)
            
            prefs.edit()
                .putString("crashes", crashes.toString())
                .putLong("last_crash_timestamp", now)
                .commit()
            
            val intent = android.content.Intent(context, lumia.tracker.MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("FATAL_CRASH_DATA", crashInfo)
            }
            context.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
    
    fun getCrashes(context: Context): List<String> {
        return try {
            val json = context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
                .getString("crashes", "[]") ?: "[]"
            val crashes = JSONArray(json)
            (0 until crashes.length()).map { crashes.getString(it) }.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun analyze(crash: String): String {
        val parsed = CrashAnalyzer.analyzeCrash(crash)
        return "Cause: ${parsed.exceptionType} - ${parsed.errorMessage}\nLocation: ${parsed.crashLocation}"
    }

    fun analyzeCrash(crash: String): AnalyzedCrash {
        return CrashAnalyzer.analyzeCrash(crash)
    }

    fun clearCrashes(context: Context) {
        context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("crashes")
            .commit()
    }
}
