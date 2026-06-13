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
        val lines = crash.lines()
        val errorIndex = lines.indexOfFirst { it.startsWith("Error:") }
        
        if (errorIndex == -1) return "Unable to parse crash report."
        
        val exceptionLine = lines[errorIndex].substringAfter("Error: ").trim().ifEmpty {
            lines.getOrNull(errorIndex + 1)?.trim() ?: "Unknown Error"
        }
        
        val appTrace = lines.firstOrNull { it.trim().startsWith("at ") && it.contains("com.example.") }?.trim()
        val defaultTrace = lines.firstOrNull { it.trim().startsWith("at ") }?.trim()
        val location = appTrace ?: defaultTrace ?: "Unknown location"
        
        return "Cause: $exceptionLine\nLocation: $location"
    }
}
