package ovrrup.lumia.viewmodel

import android.content.Context
import ovrrup.lumia.model.Task
import ovrrup.lumia.data.AppDatabase

object ReminderScheduler {
    fun scheduleReminder(c: Context, task: Task) {}
    fun cancelReminder(c: Context, taskId: Int) {}
}

object NotificationHelper {
    fun sendTestNotification(c: Context) {}
    fun getActiveNotificationsCount(c: Context): Int = 0
    fun checkNotificationPermission(c: Context): Boolean = true
}

fun exportDataToStream(vararg args: Any?) {}
fun importDataFromStream(vararg args: Any?) {}
