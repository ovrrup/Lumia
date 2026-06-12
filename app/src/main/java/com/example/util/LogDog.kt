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
        return when {
            crash.contains("NullPointerException") -> "Woof! A NullPointerException! Seems like something was missing... I'm sniffing for it!"
            crash.contains("SQLiteException") -> "Bark! Database trouble! Did a bone get stuck in the SQLite kennel?"
            crash.contains("IllegalArgumentException") -> "Arf! Someone didn't follow the rules of the code kennel!"
            else -> "Grrr... A mysterious error. I'm puzzling over this one."
        }
    }
}
