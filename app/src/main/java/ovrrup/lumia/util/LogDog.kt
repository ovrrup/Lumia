package ovrrup.lumia.util

import android.content.Context
import android.content.Intent
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.PrintWriter
import java.io.StringWriter

@JsonClass(generateAdapter = true)
data class CrashAnalysis(
    val severityLevel: String,
    val likelyComponent: String,
    val exceptionType: String,
    val errorMessage: String,
    val crashLocation: String,
    val isFrameworkBug: Boolean,
    val suggestion: String
)

object LogDog {
    private const val PREFS_NAME = "logdog_diagnostics"
    private const val CRASH_LOGS_KEY = "crash_logs"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString()

            // Persist crash log locally
            saveCrash(context.applicationContext, trace)

            // Attempt to restart app pointing back to MainActivity with crash notice
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("FATAL_CRASH_DATA", trace)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Let original handler (or default JVM) terminate the crashed process
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrash(context: Context, trace: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getCrashes(context).toMutableList()
        current.add(0, trace) // Add as newest
        if (current.size > 20) {
            current.removeAt(current.lastIndex) // Enforce cap
        }
        try {
            val json = stringListAdapter.toJson(current)
            prefs.edit().putString(CRASH_LOGS_KEY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCrashes(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(CRASH_LOGS_KEY, null) ?: return emptyList()
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearCrashes(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(CRASH_LOGS_KEY).apply()
    }

    fun analyzeCrash(crashLog: String): CrashAnalysis {
        val lines = crashLog.split("\n")
        val mainLine = lines.firstOrNull() ?: "java.lang.UnknownException: Thread crashed inexplicably"
        
        // Extract Exception Type and descriptive message
        val colonIndex = mainLine.indexOf(":")
        val exceptionType = if (colonIndex != -1) {
            mainLine.substring(0, colonIndex).trim()
        } else {
            mainLine.trim()
        }
        
        val errorMessage = if (colonIndex != -1 && colonIndex < mainLine.length - 1) {
            mainLine.substring(colonIndex + 1).trim()
        } else {
            "No message specified."
        }

        // Trace first line originating from our app packages "ovrrup.lumia"
        var crashLocation = "Unknown Location"
        for (line in lines) {
            if (line.contains("at ovrrup.lumia")) {
                val openParen = line.indexOf("(")
                val closeParen = line.indexOf(")")
                if (openParen != -1 && closeParen != -1 && closeParen > openParen) {
                    crashLocation = line.substring(openParen + 1, closeParen).trim()
                } else {
                    crashLocation = line.trim()
                }
                break
            }
        }

        // Determine likely architectural layer
        val likelyComponent = when {
            crashLog.contains("viewmodel") || crashLog.contains("ViewModel") -> "Data state machine (ViewModel)"
            crashLog.contains("database") || crashLog.contains("scholarDao") || crashLog.contains("room") -> "Database Storage Engine (Room)"
            crashLog.contains("ui") || crashLog.contains("compose") || crashLog.contains("Screen") -> "Compose Render Layer (UI)"
            else -> "App Core Runtime Kernel"
        }

        // Framework Bug Check
        val isFrameworkBug = crashLog.contains("androidx.compose") || crashLog.contains("android.view") || crashLog.contains("android.os")

        // Intrude Severity Level
        val severityLevel = when {
            exceptionType.contains("NullPointerException") || exceptionType.contains("IllegalStateException") -> "Critical"
            exceptionType.contains("IllegalArgumentException") || exceptionType.contains("RuntimeException") -> "High"
            else -> "Normal"
        }

        // Dev-oriented Actionable Suggestion
        val suggestion = when {
            exceptionType.contains("NullPointerException") -> "Null pointer violation. Re-check state bindings, state flow initial values, and check for safe-unwrapped null links in your UI screen composables."
            exceptionType.contains("SQLiteException") || exceptionType.contains("Room") -> "Room Database structural conflict. If schemas were changed, verify a clean fallback migration is handled, or clear data to force rebuilding."
            exceptionType.contains("IllegalStateException") -> "Life-cycle state inconsistency. Ensure state updates are scoped properly within LaunchedEffects or inside Coroutine scopes to prevent main thread locks."
            else -> "Review the stack pointer location carefully. If it is a framework constraint, isolate compose layouts into safer GlassCard containers."
        }

        return CrashAnalysis(
            severityLevel = severityLevel,
            likelyComponent = likelyComponent,
            exceptionType = exceptionType,
            errorMessage = errorMessage,
            crashLocation = crashLocation,
            isFrameworkBug = isFrameworkBug,
            suggestion = suggestion
        )
    }
}
