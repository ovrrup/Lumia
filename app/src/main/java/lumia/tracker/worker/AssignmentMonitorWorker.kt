package lumia.tracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.util.NotificationContent
import lumia.tracker.util.NotificationHelper
import lumia.tracker.util.ReminderScheduler
import java.util.concurrent.TimeUnit

class AssignmentMonitorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val database = AppDatabase.getDatabase(context)
            val profMgr = lumia.tracker.data.ProfileManager(context)
            val prefs = profMgr.getProfilePrefs()

            val enableDailyDigest = prefs.getBoolean("notif_enable_daily_digest", true)
            val formalTone = prefs.getBoolean("notif_formal_tone", true)

            if (enableDailyDigest) {
                val allAssignments = database.scholarDao().exportAllAssignments()
                val allTasks = database.scholarDao().exportAllTasks()
                val currentTime = System.currentTimeMillis()
                val approachTimeLimit = currentTime + TimeUnit.HOURS.toMillis(24)
                
                val approachingAssignments = allAssignments.filter { !it.isCompleted && it.dueDateMillis > currentTime && it.dueDateMillis <= approachTimeLimit }
                val approachingTasks = allTasks.filter { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis > currentTime && it.dueDateMillis <= approachTimeLimit }
                
                if (approachingAssignments.isNotEmpty() || approachingTasks.isNotEmpty()) {
                    showDigestNotification(approachingAssignments, approachingTasks, formalTone)
                }
            }

            if (prefs.getBoolean("notif_enable_classes", true)) {
                val calendar = java.util.Calendar.getInstance()
                val currentDayOfWeekStr = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
                val todaysCourses = database.scholarDao().exportAllCourses().filter { it.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true) }

                todaysCourses.forEach { course ->
                    scheduleCourseNotifs(course)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("AppMonitor", "Error monitoring app stats", e)
            return Result.failure()
        }
    }

    private fun scheduleCourseNotifs(course: lumia.tracker.model.Course) {
        try {
            val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            if (course.scheduleStartTime.isNotBlank()) {
                sdfTime.parse(course.scheduleStartTime.uppercase())?.let { parsed ->
                    val timeCal = java.util.Calendar.getInstance().apply { time = parsed }
                    val startCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                        set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                        set(java.util.Calendar.SECOND, 0)
                    }
                    ReminderScheduler.scheduleClassReminder(context, course.id, course.name, "Starts at ${course.scheduleStartTime}", startCal.timeInMillis - (10 * 60 * 1000), "class_start")
                }
            }
            if (course.scheduleEndTime.isNotBlank()) {
                sdfTime.parse(course.scheduleEndTime.uppercase())?.let { parsed ->
                    val timeCal = java.util.Calendar.getInstance().apply { time = parsed }
                    val endCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                        set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                        set(java.util.Calendar.SECOND, 0)
                    }
                    ReminderScheduler.scheduleClassReminder(context, course.id, course.name, "Class finished. Don't forget to mark your attendance!", endCal.timeInMillis, "class_end")
                }
            }
        } catch (e: Exception) {
            Log.e("AppMonitor", "Error parsing time for course ${course.name}", e)
        }
    }

    private fun showDigestNotification(assignments: List<PracticeAssignment>, tasks: List<lumia.tracker.model.Task>, formalTone: Boolean) {
        val totalCount = assignments.size + tasks.size
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("scholar_monitor_channel", "ScholarSync Monitor", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily digest of upcoming deadlines"
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_TAB", 3)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val (finalTitle, finalDesc) = NotificationContent.getPersonalizedContent(
            type = "daily_digest",
            title = "$totalCount items approaching",
            desc = "You have ${assignments.size} assignment(s) and ${tasks.size} task(s) due within the next 24 hours.",
            tone = if (formalTone) "Formal" else "Aggressive"
        )
        
        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(finalTitle).setSummaryText("Daily Summary")
        var shown = 0
        assignments.take(3).forEach { inboxStyle.addLine("[Assignment] ${it.title}"); shown++ }
        tasks.take(3).forEach { inboxStyle.addLine("[Task] ${it.title}"); shown++ }
        if (totalCount > shown) inboxStyle.addLine("...and ${totalCount - shown} more")

        val notification = NotificationCompat.Builder(context, "scholar_monitor_channel")
            .setSmallIcon(NotificationHelper.getSmallIcon())
            .setContentTitle(finalTitle)
            .setContentText(finalDesc)
            .setStyle(inboxStyle)
            .setColor(NotificationHelper.getColor(context))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")
            .setGroupSummary(true)
            .build()
            
        notificationManager.notify(1001, notification)
    }
}
