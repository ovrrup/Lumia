package lumia.tracker.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import lumia.tracker.util.ScholarPomodoroWidgetProvider

object PomodoroServiceBroadcaster {
    fun updateNotification(context: Context, mode: PomodoroMode, timeLeft: Int, originalTime: Int, isPaused: Boolean, isAlarmActive: Boolean, sessionsCompleted: Int, periodSessions: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2002, PomodoroNotificationHelper.buildNotification(context, mode, timeLeft, originalTime, isPaused, isAlarmActive, sessionsCompleted, periodSessions))
    }

    fun sendTick(context: Context, timeLeft: Int, originalTime: Int, modeName: String, isPaused: Boolean, sessionsCompleted: Int) {
        val broadcastIntent = Intent("PomodoroTick").apply { setPackage(context.packageName) }
        broadcastIntent.putExtra("timeLeft", timeLeft)
        broadcastIntent.putExtra("originalTime", originalTime)
        broadcastIntent.putExtra("mode", modeName)
        broadcastIntent.putExtra("isPaused", isPaused)
        broadcastIntent.putExtra("sessionsCompleted", sessionsCompleted)
        context.sendBroadcast(broadcastIntent)
        updatePomodoroWidget(context)
    }

    fun updatePomodoroWidget(context: Context) {
        try {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, ScholarPomodoroWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, ScholarPomodoroWidgetProvider::class.java).apply {
                    action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
