package ovrrup.lumia.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Academic Reminder"
        val description = intent.getStringExtra("description") ?: ""
        val linksText = intent.getStringExtra("linksText") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "lumia_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lumia Academic Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(context, id, launchIntent, flags)
        } else {
            null
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$description\n$linksText".trim()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }

        notificationManager.notify(id, builder.build())
    }
}
