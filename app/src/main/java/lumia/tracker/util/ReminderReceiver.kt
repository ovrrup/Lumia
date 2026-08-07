package lumia.tracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val assignmentId = intent.getIntExtra("assignment_id", -1)
        
        val profMgr = lumia.tracker.data.ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
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
            
            ReminderScheduler.scheduleReminderExact(context, assignmentId, title, desc, interconnections, System.currentTimeMillis() + 15 * 60 * 1000)
            return
        }

        if (!enableDeadlines) return

        val typeExtra = intent.getStringExtra("type") ?: "assignment"
        val titleExtra = intent.getStringExtra("title") ?: "Assignment Due"
        val descExtra = intent.getStringExtra("desc") ?: "You have an assignment to complete."
        val interconnections = intent.getStringExtra("interconnections") ?: ""

        val (finalTitle, finalDesc) = NotificationContent.getPersonalizedContent(
            type = typeExtra,
            title = titleExtra,
            desc = descExtra,
            tone = if (formalTone) "Formal" else "Aggressive",
            interconnections = interconnections
        )
        
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (typeExtra == "task") {
                putExtra("OPEN_TAB", 3)
            } else if (intent.hasExtra("courseId")) {
                val cId = intent.getIntExtra("courseId", -1)
                if (cId != -1) {
                    putExtra("OPEN_SCREEN", "courseDetail/$cId")
                }
            } else if (intent.hasExtra("subjectId")) {
                val sId = intent.getIntExtra("subjectId", -1)
                if (sId != -1) {
                    putExtra("OPEN_SCREEN", "subjectDetail/$sId")
                }
            } else if (typeExtra == "class_start" || typeExtra == "class_end") {
                putExtra("OPEN_TAB", 1)
            } else {
                putExtra("OPEN_TAB", 3)
            }
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
            .setSummaryText(if (typeExtra.startsWith("class")) "Class Alert" else "Deadline Alert")

        val builder = NotificationCompat.Builder(context, "scholar_sync_channel")
            .setSmallIcon(NotificationHelper.getSmallIcon())
            .setContentTitle(finalTitle)
            .setContentText(finalDesc)
            .setStyle(bigTextStyle)
            .setColor(NotificationHelper.getColor(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")
            
        if (typeExtra == "assignment") {
            builder.addAction(android.R.drawable.ic_menu_edit, if (formalTone) "Mark Done" else "I Did It!", donePendingIntent)
            builder.addAction(android.R.drawable.ic_popup_sync, "Snooze 15m", snoozePendingIntent)
        }

        val notification = builder.build()
        notificationManager.notify(if (assignmentId != -1) assignmentId else System.currentTimeMillis().toInt(), notification)
    }
}
