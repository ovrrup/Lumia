package lumia.tracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import lumia.tracker.data.ProfileManager

object ReminderScheduler {

    /**
     * Schedules a deadline reminder (defaulting to 1 hour before timestamp).
     * If due in less than 1 hour, schedules for immediate delivery (+5 seconds).
     */
    fun scheduleReminder(
        context: Context,
        assignmentId: Int,
        title: String,
        desc: String,
        interconnections: String,
        timestamp: Long,
        type: String = "assignment",
        courseId: Int? = null,
        subjectId: Int? = null
    ) {
        val now = System.currentTimeMillis()
        if (timestamp <= now) return

        // Remind 1 hour before due date
        val triggerTime = timestamp - (1000 * 60 * 60)
        val finalTrigger = if (triggerTime > now) triggerTime else (now + 5000L)

        scheduleReminderExact(
            context = context,
            assignmentId = assignmentId,
            title = title,
            desc = desc,
            interconnections = interconnections,
            triggerTime = finalTrigger,
            type = type,
            courseId = courseId,
            subjectId = subjectId
        )
    }

    /**
     * Schedules class start / end reminders if class notifications are enabled.
     */
    fun scheduleClassReminder(
        context: Context,
        classId: Int,
        title: String,
        desc: String,
        timestamp: Long,
        type: String = "class_start",
        courseId: Int? = null
    ) {
        val profMgr = ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        if (!prefs.getBoolean("notif_enable_classes", true)) return

        if (timestamp > System.currentTimeMillis()) {
            val reminderId = classId + (if (type == "class_start") 50000 else 60000)
            scheduleReminderExact(
                context = context,
                assignmentId = reminderId,
                title = title,
                desc = desc,
                interconnections = "",
                triggerTime = timestamp,
                type = type,
                courseId = courseId
            )
        }
    }

    /**
     * Schedules an exact alarm with fallback to inexact alarms when exact alarm permissions are absent.
     * Prevents SecurityException crashes on Android 12+ (API 31+).
     */
    fun scheduleReminderExact(
        context: Context,
        assignmentId: Int,
        title: String,
        desc: String,
        interconnections: String,
        triggerTime: Long,
        type: String = "assignment",
        courseId: Int? = null,
        subjectId: Int? = null
    ) {
        val now = System.currentTimeMillis()
        if (triggerTime <= now) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("assignment_id", assignmentId)
            putExtra("title", title)
            putExtra("desc", desc)
            putExtra("interconnections", interconnections)
            putExtra("type", type)
            if (courseId != null) putExtra("courseId", courseId)
            if (subjectId != null) putExtra("subjectId", subjectId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                alarmManager.canScheduleExactAlarms()
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                Log.d("ReminderScheduler", "Scheduled exact reminder for $title (id: $assignmentId) at $triggerTime")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                Log.d("ReminderScheduler", "Scheduled inexact reminder for $title (id: $assignmentId) at $triggerTime (exact alarms not permitted)")
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Exact alarm SecurityException for $title, falling back to inexact.", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (ex: Exception) {
                Log.e("ReminderScheduler", "Failed to schedule fallback inexact alarm", ex)
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Exception scheduling alarm for $title", e)
        }
    }

    /**
     * Cancels an active scheduled reminder.
     */
    fun cancelReminder(context: Context, assignmentId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                assignmentId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("ReminderScheduler", "Cancelled reminder for id: $assignmentId")
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error cancelling reminder for id $assignmentId", e)
        }
    }
}
