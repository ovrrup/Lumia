package ovrrup.lumia.util

import android.content.Context
import ovrrup.lumia.data.AppDatabase
import ovrrup.lumia.model.Task

object TrueAodManager {
    fun isAodConfigured(c: Context): Boolean = false
    fun testAodFunctionality(c: Context) {}
    fun initiateAod(c: Context) {}
    fun enableTrueAodMode(c: Context) {}
    fun cancelAod(c: Context) {}
}

object VersionUtils {
    fun isUpdateAvailable(vararg args: Any?): Boolean = false
}

data class CrashAnalysis(
    val severityLevel: String = "Low",
    val exceptionType: String = "",
    val errorMessage: String = "",
    val crashLocation: String = "",
    val isFrameworkBug: Boolean = false,
    val likelyComponent: String = "",
    val suggestion: String = ""
)

object LogDog {
    fun getCrashes(context: Context): List<String> = emptyList()
    fun clearCrashes(context: Context) {}
    fun analyzeCrash(crash: String): CrashAnalysis = CrashAnalysis()
}

object ReminderScheduler {
    fun scheduleReminder(vararg args: Any?) {}
    fun cancelReminder(vararg args: Any?) {}
}

object NotificationHelper {
    fun sendTestNotification(vararg args: Any?) {}
    fun getActiveNotificationsCount(vararg args: Any?): Int = 0
    fun checkNotificationPermission(vararg args: Any?): Boolean = true
    fun clearTestNotification(vararg args: Any?) {}
    fun getSmallIcon(vararg args: Any?): Int = 0
    fun getColor(vararg args: Any?): Int = 0
}
