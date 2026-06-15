package ovrrup.lumia.util

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
import ovrrup.lumia.MainActivity
import ovrrup.lumia.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val assignmentId = intent.getIntExtra("assignment_id", -1)
        
        val prefs = context.getSharedPreferences("scholar_settings", Context.MODE_PRIVATE)
        val formalTone = prefs.getBoolean("notif_formal_tone", true)
        val enableDeadlines = prefs.getBoolean("notif_enable_deadlines", true)
        
        if (action == "ACTION_MARK_DONE" && assignmentId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(assignmentId)
            
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val assignments = db.scholarDao().exportAllAssignments()
                val assignment = assignments.find { it.id == assignmentId }
                if (assignment != null) {
                    db.scholarDao().updateAssignment(assignment.copy(isCompleted = true))
                }
            }
            return
        }

        if (action == "ACTION_SNOOZE" && assignmentId != -1) {
            val title = intent.getStringExtra("title") ?: "Assignment Due"
            val desc = intent.getStringExtra("desc") ?: "You have an assignment to complete."
            val interconnections = intent.getStringExtra("interconnections") ?: ""
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(assignmentId)
            
            // Snooze for 15 minutes
            ReminderScheduler.scheduleReminderExact(context, assignmentId, title, desc, interconnections, System.currentTimeMillis() + 15 * 60 * 1000)
            return
        }

        if (!enableDeadlines) return

        val titleExtra = intent.getStringExtra("title") ?: "Assignment Due"
        val descExtra = intent.getStringExtra("desc") ?: "You have an assignment to complete."
        val interconnections = intent.getStringExtra("interconnections") ?: ""

        val finalTitle = if (formalTone) "Deadline Reminder: $titleExtra" else "URGENT: $titleExtra is DUE!"
        val finalDesc = if (formalTone) {
            descExtra + if (interconnections.isNotBlank()) "\nLinked with: $interconnections" else ""
        } else {
            "Are you procrastinating? $descExtra" + if (interconnections.isNotBlank()) "\nIt's linked to: $interconnections. You can't escape it!" else ""
        }
        
        Log.d("ReminderReceiver", "Showing notification for: $finalTitle")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "scholar_sync_channel", 
                "ScholarSync Reminders", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for assignments, tasks, and classes"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, assignmentId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_MARK_DONE"
            putExtra("assignment_id", assignmentId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, assignmentId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_SNOOZE"
            putExtra("assignment_id", assignmentId)
            putExtra("title", titleExtra)
            putExtra("desc", descExtra)
            putExtra("interconnections", interconnections)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, assignmentId + 10000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(finalDesc)
            .setBigContentTitle(finalTitle)
            .setSummaryText("Deadline Alert")

        val notification = NotificationCompat.Builder(context, "scholar_sync_channel")
            .setSmallIcon(ovrrup.lumia.R.drawable.ic_notification_deadline)
            .setContentTitle(finalTitle)
            .setContentText(finalDesc)
            .setStyle(bigTextStyle)
            .setColor(android.graphics.Color.parseColor("#E91E63"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, if (formalTone) "Mark Done" else "I Did It!", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 15m", snoozePendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")
            .build()
            
        notificationManager.notify(if (assignmentId != -1) assignmentId else System.currentTimeMillis().toInt(), notification)
    }
}

object ReminderScheduler {
    fun scheduleReminder(context: Context, assignmentId: Int, title: String, desc: String, interconnections: String, timestamp: Long) {
        // Remind 1 hour before due date
        val triggerTime = timestamp - (1000 * 60 * 60)
        
        // Ensure we don't schedule in the past
        if (triggerTime > System.currentTimeMillis()) {
            scheduleReminderExact(context, assignmentId, title, desc, interconnections, triggerTime)
        }
    }

    fun scheduleClassReminder(context: Context, classId: Int, title: String, desc: String, timestamp: Long) {
        val prefs = context.getSharedPreferences("scholar_settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notif_enable_classes", true)) return
        val triggerTime = timestamp - (1000 * 60 * 15) // 15 mins before class
        if (triggerTime > System.currentTimeMillis()) {
            scheduleReminderExact(context, classId + 50000, "Class: $title", "Starting in 15 mins: $desc", "", triggerTime)
        }
    }

    fun scheduleReminderExact(context: Context, assignmentId: Int, title: String, desc: String, interconnections: String, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("assignment_id", assignmentId)
            putExtra("title", title)
            putExtra("desc", desc)
            putExtra("interconnections", interconnections)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "Scheduled exact reminder for $title at $triggerTime")
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "Scheduled inexact reminder fallback for $title (exact alarm not allowed)")
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Exact alarm SecurityException, falling back to inexact.", e)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (ex: java.lang.Exception) {
                Log.e("ReminderScheduler", "Failed to schedule fallback inexact alarm", ex)
            }
        }
    }
}
