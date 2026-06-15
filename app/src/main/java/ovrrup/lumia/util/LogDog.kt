package ovrrup.lumia.util

import android.content.Context
import android.os.Build
import android.util.Log
import ovrrup.lumia.BuildConfig
import org.json.JSONArray

object LogDog {
    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = throwable.stackTraceToString()
            val crashInfo = """
                Device: ${Build.MODEL}
                Version: ${BuildConfig.VERSION_NAME}
                Error: $stackTrace
            """.trimIndent()
            
            Log.e("LogDog", "Crash captured: $crashInfo")
            
            // Gracefully restart and show panel
            val intent = android.content.Intent(context, ovrrup.lumia.MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("FATAL_CRASH_DATA", crashInfo)
            }
            context.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
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
        val lines = crash.lines()
        val errorLineIndex = lines.indexOfFirst { it.contains("Error:") }
        
        var exceptionType = "Unknown Exception"
        var errorMessage = "No details available."
        
        if (errorLineIndex != -1) {
            val errorLine = lines[errorLineIndex].substringAfter("Error:").trim()
            if (errorLine.contains(":")) {
                exceptionType = errorLine.substringBefore(":").trim()
                errorMessage = errorLine.substringAfter(":").trim()
            } else {
                exceptionType = errorLine
                errorMessage = "Unknown exception detail"
            }
        }
        
        // Find ovrrup.lumia. line in stack trace
        val appTraceLine = lines.firstOrNull { it.trim().startsWith("at ") && it.contains("ovrrup.lumia.") }?.trim()
        val defaultTraceLine = lines.firstOrNull { it.trim().startsWith("at ") }?.trim()
        val rawLocation = appTraceLine ?: defaultTraceLine ?: ""
        
        // Parse rawLocation: at ovrrup.lumia.ui.screens.CourseDetailScreenKt.CourseDetailScreen(CourseDetailScreen.kt:318)
        var parsedLocation = "Unknown Location"
        var fileAndLine = "Unknown file"
        var methodName = ""
        
        if (rawLocation.isNotEmpty()) {
            if (rawLocation.contains("(") && rawLocation.contains(")")) {
                fileAndLine = rawLocation.substringAfter("(").substringBefore(")")
                val beforeParen = rawLocation.substringBefore("(")
                val lastDot = beforeParen.lastIndexOf(".")
                methodName = if (lastDot != -1) beforeParen.substring(lastDot + 1) else ""
                parsedLocation = "$fileAndLine inside method $methodName"
            } else {
                parsedLocation = rawLocation.substringAfter("at ")
            }
        }
        
        // Intelligent troubleshooting suggestions based on type
        val suggestion = when {
            exceptionType.contains("NullPointerException") -> {
                "🐾 Sniffing detail: Whoops, found a NullPointerException! You tried to play fetch with a stick that doesn't exist. Check variables at $fileAndLine to ensure they aren't null."
            }
            exceptionType.contains("IllegalArgumentException") -> {
                if (errorMessage.contains("matches route", ignoreCase = true) || errorMessage.contains("navigation", ignoreCase = true)) {
                    "🐾 Sniffing detail: Navigation route mismatch! We ran into a wall because the route ain't on the map. Check route definitions at $fileAndLine."
                } else {
                    "🐾 Sniffing detail: We got an IllegalArgument. I only eat premium kibble, but you gave me this! Verify input types at $fileAndLine."
                }
            }
            exceptionType.contains("SQLiteException") || exceptionType.contains("Room") || exceptionType.contains("database", ignoreCase = true) -> {
                "🐾 Sniffing detail: SQL Database barked back! Check if you changed the schema without a migration, or maybe a typo'd column. The data bowl is a mess."
            }
            exceptionType.contains("IndexOutOfBoundsException") || exceptionType.contains("ArrayIndexOutOfBoundsException") -> {
                "🐾 Sniffing detail: Out of bounds! We ran out of yard space. You're looking for an element past the end of the line at $fileAndLine."
            }
            exceptionType.contains("ClassCastException") -> {
                "🐾 Sniffing detail: Class Cast Exception! You're trying to put a cat uniform on a dog. Check 'instanceof' or 'as' casts safely."
            }
            exceptionType.contains("IllegalStateException") -> {
                "🐾 Sniffing detail: Illegal State! I'm dizzy. You activated something before the app was ready for it. Check lifecycle states or dirty components."
            }
            exceptionType.contains("ActivityNotFoundException") -> {
                "🐾 Sniffing detail: Activity Not Found! That activity threw its invisibility cloak on. Did you declare it in the AndroidManifest?"
            }
            exceptionType.contains("SecurityException") -> {
                "🐾 Sniffing detail: Security Exception! Halt! Who goes there? We need permissions before doing this action."
            }
            exceptionType.contains("OutOfMemoryError") -> {
                "🐾 Sniffing detail: OUT OF MEMORY (OOM)! The bowl overfloweth! The app loaded too much data or massive bitmaps. Try downscaling."
            }
            else -> {
                "🐾 Sniffing detail: Standard exception glitch. It happens even to the goodest boys. Pinpoint your investigation around $fileAndLine!"
            }
        }
        
        val isFrameworkBug = appTraceLine == null
        
        val severityLevel = when {
            exceptionType.contains("NullPointerException") || exceptionType.contains("OutOfMemory") || exceptionType.contains("SQLite") -> "Critical"
            exceptionType.contains("Exception") || exceptionType.contains("Error") -> "High"
            else -> "Warning"
        }
        
        val likelyComponent = when {
            rawLocation.contains(".ui.") -> "UI Layer"
            rawLocation.contains(".data.") || rawLocation.contains(".database.") -> "Database / Data"
            rawLocation.contains(".worker.") || rawLocation.contains(".service.") -> "Background Task"
            rawLocation.contains(".util.") -> "Utility / Core"
            else -> "System / Main"
        }
        
        return AnalyzedCrash(
            exceptionType = exceptionType,
            errorMessage = errorMessage,
            crashLocation = parsedLocation,
            appCrashLine = appTraceLine,
            suggestion = suggestion,
            isFrameworkBug = isFrameworkBug,
            severityLevel = severityLevel,
            likelyComponent = likelyComponent
        )
    }
}
