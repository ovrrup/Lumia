package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Assignment Due"
        val desc = intent.getStringExtra("desc") ?: "You have an assignment to complete."
        
        Log.d("ReminderReceiver", "Showing notification for: $title")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("scholar_sync_channel", "ScholarSync Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, "scholar_sync_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder system icon
            .setContentTitle("Deadline Reminder: $title")
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

object ReminderScheduler {
    fun scheduleReminder(context: Context, assignmentId: Int, title: String, desc: String, timestamp: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("desc", desc)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Remind 1 hour before due date
        val triggerTime = timestamp - (1000 * 60 * 60)
        
        // Ensure we don't schedule in the past
        if (triggerTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "Scheduled reminder for $title at $triggerTime")
            } catch (e: SecurityException) {
                // Exact alarm permission not granted, let's try a graceful fallback or ignore
                Log.e("ReminderScheduler", "Exact alarm permission missing", e)
            }
        }
    }
}
