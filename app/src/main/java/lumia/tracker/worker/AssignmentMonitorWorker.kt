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
import java.util.concurrent.TimeUnit

class AssignmentMonitorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AppMonitor", "Running AppMonitorWorker...")
        
        try {
            val database = AppDatabase.getDatabase(context)
            val profMgr = lumia.tracker.data.ProfileManager(context)
            val prefs = profMgr.getProfilePrefs()

            val enableDailyDigest = prefs.getBoolean("notif_enable_daily_digest", true)
            val formalTone = prefs.getBoolean("notif_formal_tone", true)
            val enableStreaks = prefs.getBoolean("notif_enable_streaks", true)

            // 1. Streak checks
            if (enableStreaks) {
                checkStreaks(prefs, formalTone)
            }

            // 2. Daily digest (Deadlines for Assignments and Tasks)
            if (enableDailyDigest) {
                val allAssignments = database.scholarDao().exportAllAssignments()
                val allTasks = database.scholarDao().exportAllTasks()
                
                val currentTime = System.currentTimeMillis()
                val approachTimeLimit = currentTime + TimeUnit.HOURS.toMillis(24)
                
                val approachingAssignments = allAssignments.filter {
                    !it.isCompleted && it.dueDateMillis > currentTime && it.dueDateMillis <= approachTimeLimit
                }
                val approachingTasks = allTasks.filter {
                    !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis > currentTime && it.dueDateMillis <= approachTimeLimit
                }
                
                if (approachingAssignments.isNotEmpty() || approachingTasks.isNotEmpty()) {
                    showDigestNotification(approachingAssignments, approachingTasks, formalTone)
                }
            }

            // 3. Classes & Attendance Reminder
            val enableClasses = prefs.getBoolean("notif_enable_classes", true)
            if (enableClasses) {
                // Get today's day string (e.g. "Monday")
                val calendar = java.util.Calendar.getInstance()
                val currentDayOfWeekStr = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
                
                val allCourses = database.scholarDao().exportAllCourses()
                val todaysCourses = allCourses.filter {
                    it.scheduleDays.contains(currentDayOfWeekStr, ignoreCase = true)
                }

                if (todaysCourses.isEmpty()) {
                    // Generic daily reminder if no classes but we want them to open the app? Actually just skip.
                } else {
                    todaysCourses.forEach { course ->
                        try {
                            if (course.scheduleStartTime.isNotBlank()) {
                                // parse time and create calendar for today at that time
                                val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                val startCal = java.util.Calendar.getInstance()
                                val parsedStart = sdfTime.parse(course.scheduleStartTime.uppercase())
                                if (parsedStart != null) {
                                    val timeCal = java.util.Calendar.getInstance()
                                    timeCal.time = parsedStart
                                    startCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                                    startCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                                    startCal.set(java.util.Calendar.SECOND, 0)
                                    // schedule 10 minutes before
                                    lumia.tracker.util.ReminderScheduler.scheduleClassReminder(
                                        context, course.id, course.name, "Starts at ${course.scheduleStartTime}",
                                        startCal.timeInMillis - (10 * 60 * 1000), "class_start"
                                    )
                                }
                            }
                            if (course.scheduleEndTime.isNotBlank()) {
                                val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                val endCal = java.util.Calendar.getInstance()
                                val parsedEnd = sdfTime.parse(course.scheduleEndTime.uppercase())
                                if (parsedEnd != null) {
                                    val timeCal = java.util.Calendar.getInstance()
                                    timeCal.time = parsedEnd
                                    endCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                                    endCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                                    endCal.set(java.util.Calendar.SECOND, 0)
                                    // schedule at the exact end time
                                    lumia.tracker.util.ReminderScheduler.scheduleClassReminder(
                                        context, course.id, course.name, "Class finished. Don't forget to mark your attendance!",
                                        endCal.timeInMillis, "class_end"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AppMonitor", "Error parsing time for course ${course.name}", e)
                        }
                    }
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("AppMonitor", "Error monitoring app stats", e)
            return Result.failure()
        }
    }
    
    private fun checkStreaks(prefs: android.content.SharedPreferences, formalTone: Boolean) {
        val lastActionDate = prefs.getString("last_action_date_str", "") ?: ""
        val currentStreak = prefs.getInt("current_streak", 0)
        
        if (currentStreak > 0 && lastActionDate.isNotEmpty()) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val today = sdf.format(java.util.Date())
            try {
                val lastDate = sdf.parse(lastActionDate)
                if (lastDate != null) {
                    val diff = ((sdf.parse(today)?.time ?: System.currentTimeMillis()) - lastDate.time) / 86400000L
                    if (diff == 1L) {
                        // At risk of breaking the streak tomorrow if not continued
                        val title = if (formalTone) "Maintain Your Streak" else "Don't Break the Streak!"
                        val msg = if (formalTone) "Your current streak is $currentStreak days. Study today to keep it going." else "You've survived $currentStreak days! Don't be lazy today to ruin it!"
                        sendNotification("scholar_streak_channel", 1002, title, msg, lumia.tracker.util.NotificationHelper.getSmallIcon(), lumia.tracker.util.NotificationHelper.getColor(context))
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun sendNotification(channelId: String, notifId: Int, title: String, text: String, iconRes: Int, color: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Scholar System Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                lightColor = color
            }
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notifId, notification)
    }

    private fun showDigestNotification(assignments: List<PracticeAssignment>, tasks: List<lumia.tracker.model.Task>, formalTone: Boolean) {
        val assignmentCount = assignments.size
        val taskCount = tasks.size
        val totalCount = assignmentCount + taskCount
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "scholar_monitor_channel", 
                "ScholarSync Monitor", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily digest of upcoming deadlines"
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (formalTone) {
            "Daily Digest: $totalCount items approaching"
        } else {
            "Wake up! $totalCount items are due soon"
        }
        val desc = if (formalTone) {
            "You have $assignmentCount assignment(s) and $taskCount task(s) due within the next 24 hours."
        } else {
            "Tick tock! $assignmentCount assignment(s) and $taskCount task(s) are about to crush you if you don't act."
        }
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("Daily Summary")
        
        var shown = 0
        assignments.take(3).forEach { assignment ->
            inboxStyle.addLine("[Assignment] ${assignment.title}")
            shown++
        }
        tasks.take(3).forEach { task ->
            inboxStyle.addLine("[Task] ${task.title}")
            shown++
        }
        if (totalCount > shown) {
            inboxStyle.addLine("...and ${totalCount - shown} more")
        }

        val notification = NotificationCompat.Builder(context, "scholar_monitor_channel")
            .setSmallIcon(lumia.tracker.util.NotificationHelper.getSmallIcon())
            .setContentTitle(title)
            .setContentText(desc)
            .setStyle(inboxStyle)
            .setColor(lumia.tracker.util.NotificationHelper.getColor(context))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("assignments_group")
            .setGroupSummary(true)
            .build()
            
        // Use a fixed ID so it just updates the existing notification if it's still there
        notificationManager.notify(1001, notification)
    }
}
