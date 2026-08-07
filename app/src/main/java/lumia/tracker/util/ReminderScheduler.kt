package lumia.tracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object ReminderScheduler {
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
        val triggerTime = timestamp - (1000 * 60 * 60)
        if (triggerTime > System.currentTimeMillis()) {
            scheduleReminderExact(context, assignmentId, title, desc, interconnections, triggerTime, type, courseId, subjectId)
        }
    }

    fun scheduleClassReminder(
        context: Context,
        classId: Int,
        title: String,
        desc: String,
        timestamp: Long,
        type: String = "class_start",
        courseId: Int? = null
    ) {
        val profMgr = lumia.tracker.data.ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        if (!prefs.getBoolean("notif_enable_classes", true)) return
        if (timestamp > System.currentTimeMillis()) {
            scheduleReminderExact(context, classId + (if (type == "class_start") 50000 else 60000), title, desc, "", timestamp, type, courseId = courseId)
        }
    }

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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
                Log.d("ReminderScheduler", "Scheduled inexact reminder fallback for $title")
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
