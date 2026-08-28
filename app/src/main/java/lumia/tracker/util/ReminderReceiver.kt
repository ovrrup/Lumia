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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val assignmentId = intent.getIntExtra("assignment_id", -1)
        val typeExtra = intent.getStringExtra("type") ?: "assignment"

        val profMgr = ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        val formalTone = prefs.getBoolean("notif_formal_tone", true)
        val enableDeadlines = prefs.getBoolean("notif_enable_deadlines", true)
        val enableClasses = prefs.getBoolean("notif_enable_classes", true)

        if (action == "ACTION_MARK_DONE" && assignmentId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(assignmentId)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val isTask = typeExtra == "task" || assignmentId >= 20000
                    val realId = if (assignmentId >= 20000) assignmentId - 20000 else assignmentId

                    if (isTask) {
                        val tasks = db.scholarDao().exportAllTasks()
                        val task = tasks.find { it.id == realId }
                        if (task != null) {
                            db.scholarDao().updateTask(task.copy(isCompleted = true))
                        }
                    } else {
                        val assignments = db.scholarDao().exportAllAssignments()
                        val assignment = assignments.find { it.id == realId }
                        if (assignment != null) {
                            db.scholarDao().updateAssignment(assignment.copy(isCompleted = true))
                        }
                    }
                    WidgetUpdateHelper.updateAllWidgets(context)
                    Log.d("ReminderReceiver", "Marked item $realId (isTask=$isTask) as completed")
                } catch (e: Exception) {
                    Log.e("ReminderReceiver", "Error marking item as done", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == "ACTION_MARK_PRESENT" || action == "ACTION_MARK_ABSENT") {
            val courseId = intent.getIntExtra("courseId", -1)
            val notifId = intent.getIntExtra("notif_id", assignmentId)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notifId != -1) notificationManager?.cancel(notifId)

            if (courseId != -1) {
                val status = if (action == "ACTION_MARK_PRESENT") "PRESENT" else "ABSENT"
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val cal = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        val record = lumia.tracker.model.AttendanceRecord(
                            courseId = courseId,
                            dateMillis = cal.timeInMillis,
                            status = status
                        )
                        db.scholarDao().insertAttendanceRecord(record)
                        WidgetUpdateHelper.updateAllWidgets(context)
                        Log.d("ReminderReceiver", "Auto-marked attendance for course $courseId as $status")
                    } catch (e: Exception) {
                        Log.e("ReminderReceiver", "Error marking attendance from notification", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        if (action == "ACTION_SNOOZE" && assignmentId != -1) {
            val title = intent.getStringExtra("title") ?: "Reminder"
            val desc = intent.getStringExtra("desc") ?: ""
            val interconnections = intent.getStringExtra("interconnections") ?: ""
            val courseId = if (intent.hasExtra("courseId")) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
            val subjectId = if (intent.hasExtra("subjectId")) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(assignmentId)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Snooze for 10 minutes
                    val snoozeTriggerTime = System.currentTimeMillis() + 10 * 60 * 1000L
                    ReminderScheduler.scheduleReminderExact(
                        context = context,
                        assignmentId = assignmentId,
                        title = title,
                        desc = desc,
                        interconnections = interconnections,
                        triggerTime = snoozeTriggerTime,
                        type = typeExtra,
                        courseId = courseId,
                        subjectId = subjectId
                    )
                    Log.d("ReminderReceiver", "Snoozed reminder $assignmentId for 10m until $snoozeTriggerTime")
                } catch (e: Exception) {
                    Log.e("ReminderReceiver", "Error snoozing reminder", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        // Check if reminders for this category are enabled
        if (typeExtra.startsWith("class")) {
            if (!enableClasses) return
        } else {
            if (!enableDeadlines) return
        }

        val titleExtra = intent.getStringExtra("title") ?: "Reminder"
        val descExtra = intent.getStringExtra("desc") ?: ""
        val interconnections = intent.getStringExtra("interconnections") ?: ""
        val courseId = if (intent.hasExtra("courseId")) intent.getIntExtra("courseId", -1).takeIf { it != -1 } else null
        val subjectId = if (intent.hasExtra("subjectId")) intent.getIntExtra("subjectId", -1).takeIf { it != -1 } else null

        val (finalTitle, finalDesc) = NotificationContent.getPersonalizedContent(
            type = typeExtra,
            title = titleExtra,
            desc = descExtra,
            tone = if (formalTone) "Formal" else "Aggressive",
            interconnections = interconnections
        )

        Log.d("ReminderReceiver", "Showing notification for: $finalTitle (type: $typeExtra)")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channelId = "scholar_sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
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
                putExtra("OPEN_TAB", 2)
            } else if (courseId != null) {
                putExtra("OPEN_SCREEN", "courseDetail/$courseId")
            } else if (subjectId != null) {
                putExtra("OPEN_SCREEN", "subjectDetail/$subjectId")
            } else if (typeExtra.startsWith("class")) {
                putExtra("OPEN_TAB", 1) // Courses tab
            } else {
                putExtra("OPEN_TAB", 2) // Self Study & Tasks tab
            }
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            assignmentId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_MARK_DONE"
            putExtra("assignment_id", assignmentId)
            putExtra("type", typeExtra)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = "ACTION_SNOOZE"
            putExtra("assignment_id", assignmentId)
            putExtra("title", titleExtra)
            putExtra("desc", descExtra)
            putExtra("interconnections", interconnections)
            putExtra("type", typeExtra)
            if (courseId != null) putExtra("courseId", courseId)
            if (subjectId != null) putExtra("subjectId", subjectId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId + 10000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summaryText = if (typeExtra.startsWith("class")) "Class Alert" else "Deadline Alert"
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(finalDesc)
            .setBigContentTitle(finalTitle)
            .setSummaryText(summaryText)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(NotificationHelper.getSmallIcon())
            .setContentTitle(finalTitle)
            .setContentText(finalDesc)
            .setStyle(bigTextStyle)
            .setColor(NotificationHelper.getColor(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")

        if (typeExtra == "assignment" || typeExtra == "task") {
            builder.addAction(
                android.R.drawable.ic_menu_edit,
                if (formalTone) "Mark Done" else "I Did It!",
                donePendingIntent
            )
            builder.addAction(
                android.R.drawable.ic_popup_sync,
                "Snooze 10m",
                snoozePendingIntent
            )
        } else if (typeExtra == "class_end" && courseId != null) {
            val notifId = if (assignmentId != -1) assignmentId else (System.currentTimeMillis() % 100000).toInt()
            val presentIntent = Intent(context, ReminderReceiver::class.java).apply {
                this.action = "ACTION_MARK_PRESENT"
                putExtra("courseId", courseId)
                putExtra("notif_id", notifId)
            }
            val presentPending = PendingIntent.getBroadcast(
                context,
                notifId + 1,
                presentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val absentIntent = Intent(context, ReminderReceiver::class.java).apply {
                this.action = "ACTION_MARK_ABSENT"
                putExtra("courseId", courseId)
                putExtra("notif_id", notifId)
            }
            val absentPending = PendingIntent.getBroadcast(
                context,
                notifId + 2,
                absentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(android.R.drawable.checkbox_on_background, "Present", presentPending)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Absent", absentPending)
        }

        val notification = builder.build()
        val notifId = if (assignmentId != -1) assignmentId else (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notifId, notification)
    }
}
