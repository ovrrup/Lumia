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
            
            // Loop Protection: If app crashes within 4.5 seconds of last crash/reboot,
            // let the system default crash-handler abort the process to avoid endless loop cascades.
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
            val crashes = try {
                JSONArray(prefs.getString("crashes", "[]") ?: "[]")
            } catch (e: Exception) {
                JSONArray()
            }
            if (crashes.length() >= 5) {
                crashes.remove(0)
            }
            crashes.put(crashInfo)
            
            // Use commit() rather than apply() here because the JVM process is terminated immediately.
            // commit() ensures synchronous disk write, guaranteeing the log is preserved before process kill.
            prefs.edit()
                .putString("crashes", crashes.toString())
                .putLong("last_crash_timestamp", now)
                .commit()
            
            // Gracefully restart and show panel
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
        val parsed = analyzeCrash(crash)
        return "Cause: ${parsed.exceptionType} - ${parsed.errorMessage}\nLocation: ${parsed.crashLocation}"
    }

    data class AnalyzedCrash(
        val exceptionType: String,
        val errorMessage: String,
        val crashLocation: String,
        val appCrashLine: String?,
        val suggestion: String,
        val severityLevel: String,
        val likelyComponent: String,
        val isFrameworkBug: Boolean
    )

    fun analyzeCrash(crash: String): AnalyzedCrash {
        var exceptionType = "Unknown Exception"
        var errorMessage = "No details available."
        var appTraceLine: String? = null
        var defaultTraceLine: String? = null
        
        // Fast, memory-efficient single-pass segment traversal
        val lines = crash.split('\n')
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("Error:") && exceptionType == "Unknown Exception") {
                val errorMsg = trimmed.substringAfter("Error:").trim()
                if (errorMsg.contains(":")) {
                    exceptionType = errorMsg.substringBefore(":").trim()
                    errorMessage = errorMsg.substringAfter(":").trim()
                } else {
                    exceptionType = errorMsg
                    errorMessage = "Check details below"
                }
            } else if (trimmed.startsWith("at ")) {
                if (trimmed.contains("lumia.tracker.") && appTraceLine == null) {
                    appTraceLine = trimmed
                } else if (defaultTraceLine == null) {
                    defaultTraceLine = trimmed
                }
            }
        }
        
        val rawLocation = appTraceLine ?: defaultTraceLine ?: ""
        var parsedLocation = "Unknown Location"
        var fileAndLine = "Unknown file"
        
        if (rawLocation.isNotEmpty()) {
            if (rawLocation.contains("(") && rawLocation.contains(")")) {
                fileAndLine = rawLocation.substringAfter("(").substringBefore(")")
                val beforeParen = rawLocation.substringBefore("(")
                val lastDot = beforeParen.lastIndexOf(".")
                val methodName = if (lastDot != -1) beforeParen.substring(lastDot + 1) else ""
                parsedLocation = "$fileAndLine inside method $methodName"
            } else {
                parsedLocation = rawLocation.substringAfter("at ")
            }
        }
        
        var severityLevel = "Moderate"
        var likelyComponent = "Unknown Component"
        
        if (crash.contains("androidx.compose") || crash.contains("android.view")) {
            likelyComponent = "UI Framework"
        } else if (crash.contains("androidx.room") || crash.contains("android.database")) {
            likelyComponent = "Local Database"
        } else if (crash.contains("androidx.navigation")) {
            likelyComponent = "Navigation"
        } else if (crash.contains("java.lang.OutOfMemoryError")) {
            likelyComponent = "Memory Management"
        } else if (crash.contains("retrofit2") || crash.contains("okhttp3")) {
            likelyComponent = "Network Layer"
        } else if (crash.contains("coroutines") || crash.contains("java.lang.Thread")) {
            likelyComponent = "Concurrency"
        }

        val isFrameworkBug = appTraceLine == null && rawLocation.isNotEmpty()

        val suggestion = when {
            exceptionType.contains("NullPointerException") -> {
                severityLevel = "High"
                "🐾 Sniffing detail: Found a NullPointerException! Checked variables around $fileAndLine. Ensure you are using Kotlin's null safety (?.) and not forcing non-null (!!)."
            }
            exceptionType.contains("IllegalArgumentException") -> {
                severityLevel = "Moderate"
                if (errorMessage.contains("matches route", ignoreCase = true) || errorMessage.contains("navigation", ignoreCase = true)) {
                    "🐾 Sniffing detail: Navigation route mismatch! We ran into a wall. Check route definitions and arguments passed at $fileAndLine."
                } else {
                    "🐾 Sniffing detail: Got an IllegalArgument. Verify input types and argument constraints at $fileAndLine."
                }
            }
            exceptionType.contains("SQLiteException") || exceptionType.contains("Room") || exceptionType.contains("database", ignoreCase = true) -> {
                severityLevel = "Critical"
                "🐾 Sniffing detail: SQL Database conflict! Check for missing migrations, typo'd column names, or incorrect Dao syntax."
            }
            exceptionType.contains("IndexOutOfBoundsException") || exceptionType.contains("ArrayIndexOutOfBoundsException") -> {
                severityLevel = "High"
                "🐾 Sniffing detail: Out of bounds array indexing. You requested an element past the end of the collection at $fileAndLine."
            }
            exceptionType.contains("ClassCastException") -> {
                severityLevel = "High"
                "🐾 Sniffing detail: Invalid Cast operation. Verify 'as' casts safely using 'as?' or check generic boundaries."
            }
            exceptionType.contains("IllegalStateException") -> {
                severityLevel = "High"
                "🐾 Sniffing detail: Illegal State! A component or lifecycle method was called at an inappropriate time."
            }
            exceptionType.contains("ActivityNotFoundException") -> {
                severityLevel = "Moderate"
                "🐾 Sniffing detail: Intent target missing. Did you forget to declare an Activity in the AndroidManifest?"
            }
            exceptionType.contains("SecurityException") -> {
                severityLevel = "Critical"
                "🐾 Sniffing detail: Permission denied! Declare necessary permissions in AndroidManifest and request them explicitly."
            }
            exceptionType.contains("OutOfMemoryError") -> {
                severityLevel = "Critical"
                "🐾 Sniffing detail: OUT OF MEMORY (OOM)! The heap overflowed. Check for memory leaks, massive bitmaps, or endless loops."
            }
            else -> {
                "🐾 Sniffing detail: An undetermined exception occurred. Pinpoint your investigation around $fileAndLine!"
            }
        }
        
        return AnalyzedCrash(
            exceptionType = exceptionType,
            errorMessage = errorMessage,
            crashLocation = parsedLocation,
            appCrashLine = appTraceLine,
            suggestion = suggestion,
            severityLevel = severityLevel,
            likelyComponent = likelyComponent,
            isFrameworkBug = isFrameworkBug
        )
    }

    fun clearCrashes(context: Context) {
        context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("crashes")
            .commit()
    }
}
