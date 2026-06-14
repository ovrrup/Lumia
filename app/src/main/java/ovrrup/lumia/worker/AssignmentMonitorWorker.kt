package ovrrup.lumia.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import ovrrup.lumia.data.AppDatabase
import ovrrup.lumia.util.NotificationHelper

class AssignmentMonitorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val allAssignments = db.scholarDao().getAllAssignments().first()
            val now = System.currentTimeMillis()
            val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
            
            val pendingAssignments = allAssignments.filter { 
                !it.isCompleted && (it.dueDateMillis in now..(now + threeDaysInMillis)) 
            }

            if (pendingAssignments.isNotEmpty()) {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "assignment_alerts",
                        "Assignment Deadlines",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val title = "Assignment Deadlines Looming"
                val text = "${pendingAssignments.size} assignments are due soon. Keep on top of your schedule!"

                val notification = NotificationCompat.Builder(applicationContext, "assignment_alerts")
                    .setSmallIcon(NotificationHelper.getSmallIcon())
                    .setContentTitle(title)
                    .setContentText(text)
                    .setColor(NotificationHelper.getColor(applicationContext))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(3001, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("AssignmentMonitorWorker", "Failed to check impending assignments", e)
        }
        return Result.success()
    }
}
