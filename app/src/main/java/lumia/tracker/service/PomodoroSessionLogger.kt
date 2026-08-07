package lumia.tracker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import lumia.tracker.MainActivity
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.ActionLog
import lumia.tracker.model.PomodoroSession
import lumia.tracker.util.NotificationHelper

object PomodoroSessionLogger {
    suspend fun logAndAwardSession(
        context: Context,
        durationMinutes: Int,
        isFullCompletion: Boolean,
        isWorkSession: Boolean,
        subjectId: Int?,
        courseId: Int?,
        assignmentId: Int?,
        taskId: Int?,
        topicId: Int?
    ) {
        if (!isWorkSession) return
        try {
            val profMgr = ProfileManager(context)
            if (!profMgr.getProfilePrefs().getBoolean("system_pomodoro_auto_log", true) && isFullCompletion) return

            val db = AppDatabase.getDatabase(context)
            db.scholarDao().insertPomodoroSession(
                PomodoroSession(
                    dateMillis = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    subjectId = subjectId,
                    courseId = courseId,
                    assignmentId = assignmentId,
                    taskId = taskId,
                    topicId = topicId
                )
            )
            val actionLabel = if (isFullCompletion) "Completed" else "Focused partially on"
            db.scholarDao().insertActionLog(ActionLog(actionText = "$actionLabel Pomodoro Session ($durationMinutes min)"))

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("OPEN_POMODORO", true)
            }
            val mainPending = PendingIntent.getActivity(context, 101, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val completionNotification = NotificationCompat.Builder(context, "pomodoro_service")
                .setSmallIcon(NotificationHelper.getSmallIcon())
                .setContentTitle(if (isFullCompletion) "Focus Completed!" else "Focus Saved!")
                .setContentText("Locked in $durationMinutes min study.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(mainPending)
                .setColor(NotificationHelper.getColor(context))
                .build()
            notificationManager.notify(2003, completionNotification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
