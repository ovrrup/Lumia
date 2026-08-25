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
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Task
import lumia.tracker.util.NotificationContent
import lumia.tracker.util.NotificationHelper
import lumia.tracker.util.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AssignmentMonitorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AssignmentMonitorWorker", "Running AssignmentMonitorWorker...")

        try {
            val profMgr = ProfileManager(context)
            val prefs = profMgr.getProfilePrefs()

            val enableDailyDigest = prefs.getBoolean("notif_enable_daily_digest", true)
            val enableClasses = prefs.getBoolean("notif_enable_classes", true)
            val formalTone = prefs.getBoolean("notif_formal_tone", true)

            // Fast exit if all features handled by worker are disabled
            if (!enableDailyDigest && !enableClasses) {
                return Result.success()
            }

            val database = AppDatabase.getDatabase(context)
            val currentTime = System.currentTimeMillis()
            val next24h = currentTime + TimeUnit.HOURS.toMillis(24)

            // 1. Daily digest (Deadlines for approaching Assignments and Tasks)
            if (enableDailyDigest) {
                val allAssignments = database.scholarDao().exportAllAssignments()
                val allTasks = database.scholarDao().exportAllTasks()

                val approachingAssignments = allAssignments.filter {
                    !it.isCompleted && it.dueDateMillis in (currentTime + 1)..next24h
                }
                val approachingTasks = allTasks.filter {
                    val due = it.dueDateMillis
                    !it.isCompleted && due != null && due in (currentTime + 1)..next24h
                }

                if (approachingAssignments.isNotEmpty() || approachingTasks.isNotEmpty()) {
                    showDigestNotification(approachingAssignments, approachingTasks, formalTone)
                }
            }

            // 2. Classes & Attendance Reminders for Today
            if (enableClasses) {
                val calendar = Calendar.getInstance()
                val todayFull = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
                val todayShort = SimpleDateFormat("EEE", Locale.US).format(calendar.time)

                val allCourses = database.scholarDao().exportAllCourses()
                val todaysCourses = allCourses.filter { course ->
                    course.scheduleDays.isNotBlank() && (
                        course.scheduleDays.contains(todayFull, ignoreCase = true) ||
                        course.scheduleDays.contains(todayShort, ignoreCase = true)
                    )
                }

                for (course in todaysCourses) {
                    try {
                        if (course.scheduleStartTime.isNotBlank()) {
                            val startMillis = parseTimeToTodayMillis(course.scheduleStartTime, calendar)
                            if (startMillis != null) {
                                val reminderTime = startMillis - (10 * 60 * 1000L)
                                if (reminderTime > System.currentTimeMillis()) {
                                    ReminderScheduler.scheduleClassReminder(
                                        context = context,
                                        classId = course.id,
                                        title = course.name,
                                        desc = "Starts at ${course.scheduleStartTime}",
                                        timestamp = reminderTime,
                                        type = "class_start",
                                        courseId = course.id
                                    )
                                }
                            }
                        }

                        if (course.scheduleEndTime.isNotBlank()) {
                            val endMillis = parseTimeToTodayMillis(course.scheduleEndTime, calendar)
                            if (endMillis != null && endMillis > System.currentTimeMillis()) {
                                ReminderScheduler.scheduleClassReminder(
                                    context = context,
                                    classId = course.id,
                                    title = course.name,
                                    desc = "Class finished. Don't forget to mark your attendance!",
                                    timestamp = endMillis,
                                    type = "class_end",
                                    courseId = course.id
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AssignmentMonitorWorker", "Error scheduling reminders for course: ${course.name}", e)
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("AssignmentMonitorWorker", "Error running AssignmentMonitorWorker", e)
            return Result.failure()
        }
    }

    private fun parseTimeToTodayMillis(timeStr: String, baseCal: Calendar): Long? {
        if (timeStr.isBlank()) return null
        val trimmed = timeStr.trim().uppercase(Locale.US)

        val formats = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = true
                val date = sdf.parse(trimmed)
                if (date != null) {
                    val timeCal = Calendar.getInstance().apply { time = date }
                    return (baseCal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            } catch (ignored: Exception) {}
        }

        // Fallback manual parser for non-standard time strings
        try {
            val isPm = trimmed.contains("PM")
            val isAm = trimmed.contains("AM")
            val clean = trimmed.replace("AM", "").replace("PM", "").trim()
            val parts = clean.split(":")
            if (parts.size >= 2) {
                var hour = parts[0].trim().toInt()
                val minute = parts[1].trim().take(2).toInt()
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                return (baseCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        } catch (ignored: Exception) {}

        return null
    }

    private fun showDigestNotification(
        assignments: List<PracticeAssignment>,
        tasks: List<Task>,
        formalTone: Boolean
    ) {
        val assignmentCount = assignments.size
        val taskCount = tasks.size
        val totalCount = assignmentCount + taskCount
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channelId = "scholar_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_TAB", 3)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val baseTitle = "$totalCount items approaching"
        val baseDesc = "You have $assignmentCount assignment(s) and $taskCount task(s) due within the next 24 hours."

        val (finalTitle, finalDesc) = NotificationContent.getPersonalizedContent(
            type = "daily_digest",
            title = baseTitle,
            desc = baseDesc,
            tone = if (formalTone) "Formal" else "Aggressive"
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(finalTitle)
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

        val notification = NotificationCompat.Builder(context, channelId)
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
