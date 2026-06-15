package ovrrup.lumia.worker

import android.content.Context
import ovrrup.lumia.model.Task

object ReminderScheduler {
    fun scheduleReminder(c: Context, task: Task) {}
    fun cancelReminder(c: Context, taskId: Int) {}
}

object NotificationHelper {
    fun sendTestNotification(c: Context) {}
    fun getActiveNotificationsCount(c: Context): Int = 0
    fun checkNotificationPermission(c: Context): Boolean = true
}
