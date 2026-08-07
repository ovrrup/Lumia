package lumia.tracker.util

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

object CrashAnalyzer {
    fun analyzeCrash(crash: String): AnalyzedCrash {
        var exceptionType = "Unknown Exception"
        var errorMessage = "No details available."
        var appTraceLine: String? = null
        var defaultTraceLine: String? = null
        
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
                "Sniffing detail: Found a NullPointerException! Checked variables around $fileAndLine. Ensure you are using Kotlin's null safety (?.) and not forcing non-null (!!)."
            }
            exceptionType.contains("IllegalArgumentException") -> {
                severityLevel = "Moderate"
                if (errorMessage.contains("matches route", ignoreCase = true) || errorMessage.contains("navigation", ignoreCase = true)) {
                    "Sniffing detail: Navigation route mismatch! We ran into a wall. Check route definitions and arguments passed at $fileAndLine."
                } else {
                    "Sniffing detail: Got an IllegalArgument. Verify input types and argument constraints at $fileAndLine."
                }
            }
            exceptionType.contains("SQLiteException") || exceptionType.contains("Room") || exceptionType.contains("database", ignoreCase = true) -> {
                severityLevel = "Critical"
                "Sniffing detail: SQL Database conflict! Check for missing migrations, typo'd column names, or incorrect Dao syntax."
            }
            exceptionType.contains("IndexOutOfBoundsException") || exceptionType.contains("ArrayIndexOutOfBoundsException") -> {
                severityLevel = "High"
                "Sniffing detail: Out of bounds array indexing. You requested an element past the end of the collection at $fileAndLine."
            }
            exceptionType.contains("ClassCastException") -> {
                severityLevel = "High"
                "Sniffing detail: Invalid Cast operation. Verify 'as' casts safely using 'as?' or check generic boundaries."
            }
            exceptionType.contains("IllegalStateException") -> {
                severityLevel = "High"
                "Sniffing detail: Illegal State! A component or lifecycle method was called at an inappropriate time."
            }
            exceptionType.contains("ActivityNotFoundException") -> {
                severityLevel = "Moderate"
                "Sniffing detail: Intent target missing. Did you forget to declare an Activity in the AndroidManifest?"
            }
            exceptionType.contains("SecurityException") -> {
                severityLevel = "Critical"
                "Sniffing detail: Permission denied! Declare necessary permissions in AndroidManifest and request them explicitly."
            }
            exceptionType.contains("OutOfMemoryError") -> {
                severityLevel = "Critical"
                "Sniffing detail: OUT OF MEMORY (OOM)! The heap overflowed. Check for memory leaks, massive bitmaps, or endless loops."
            }
            else -> {
                "Sniffing detail: An undetermined exception occurred. Pinpoint your investigation around $fileAndLine!"
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
}
