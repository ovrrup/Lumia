package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
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
            val prefs = context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            val crashes = JSONArray(prefs.getString("crashes", "[]") ?: "[]")
            if (crashes.length() >= 5) crashes.remove(0)
            crashes.put(crashInfo)
            
            prefs.edit().putString("crashes", crashes.toString()).apply()
            
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    fun getCrashes(context: Context): List<String> {
        val json = context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            .getString("crashes", "[]") ?: "[]"
        val crashes = JSONArray(json)
        return (0 until crashes.length()).map { crashes.getString(it) }.reversed()
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
        val suggestion: String
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
        
        // Find com.example. line in stack trace
        val appTraceLine = lines.firstOrNull { it.trim().startsWith("at ") && it.contains("com.example.") }?.trim()
        val defaultTraceLine = lines.firstOrNull { it.trim().startsWith("at ") }?.trim()
        val rawLocation = appTraceLine ?: defaultTraceLine ?: ""
        
        // Parse rawLocation: at com.example.ui.screens.CourseDetailScreenKt.CourseDetailScreen(CourseDetailScreen.kt:318)
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
                "🐾 Sniffing detail: A NullPointerException indicates that a variable or object reference was accessed before it was initialized or assigned. Check variables at $fileAndLine to ensure they aren't null."
            }
            exceptionType.contains("IllegalArgumentException") -> {
                if (errorMessage.contains("matches route", ignoreCase = true) || errorMessage.contains("navigation", ignoreCase = true)) {
                    "🐾 Sniffing detail: This is a navigation route mismatch! A navController tried to navigate to a route that isn't defined in MainActivity.kt. Check route definitions and arguments at $fileAndLine."
                } else {
                    "🐾 Sniffing detail: An invalid argument was passed to a function at $fileAndLine. Verify input types and argument ranges."
                }
            }
            exceptionType.contains("SQLiteException") || exceptionType.contains("Room") || exceptionType.contains("database", ignoreCase = true) -> {
                "🐾 Sniffing detail: Database operation failed. This is usually caused by changing the entity schema without incrementing the Room database version, a missing migration query, or an invalid column/table name."
            }
            exceptionType.contains("IndexOutOfBoundsException") || exceptionType.contains("ArrayIndexOutOfBoundsException") -> {
                "🐾 Sniffing detail: Attempted to get an element from a list or array using an invalid index (e.g., negative index or index >= size). Check collection sizes in $fileAndLine before accessing elements."
            }
            exceptionType.contains("ClassCastException") -> {
                "🐾 Sniffing detail: An object type cast operation failed. Verify that you are cast-checking types with 'instanceof' or 'is' before downcasting."
            }
            exceptionType.contains("IllegalStateException") -> {
                "🐾 Sniffing detail: The application reached an invalid state. Check if any state assertions failed or lifecycle states were compromised."
            }
            else -> {
                "🐾 Sniffing detail: System crashed with a standard exception. Pinpoint your investigation around $fileAndLine."
            }
        }
        
        return AnalyzedCrash(
            exceptionType = exceptionType,
            errorMessage = errorMessage,
            crashLocation = parsedLocation,
            appCrashLine = appTraceLine,
            suggestion = suggestion
        )
    }

    fun clearCrashes(context: Context) {
        context.getSharedPreferences("logdog_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("crashes")
            .apply()
    }
}
