package com.example.worker

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
import com.example.MainActivity
import com.example.data.AppDatabase
import java.util.concurrent.TimeUnit

class AssignmentMonitorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AssignmentMonitor", "Running AssignmentMonitorWorker...")
        
        try {
            val database = AppDatabase.getDatabase(context)
            val allAssignments = database.scholarDao().exportAllAssignments()
            
            val currentTime = System.currentTimeMillis()
            // e.g. approaching = due within the next 24 hours
            val approachTimeLimit = currentTime + TimeUnit.HOURS.toMillis(24)
            
            val approachingAssignments = allAssignments.filter {
                !it.isCompleted && it.dueDateMillis > currentTime && it.dueDateMillis <= approachTimeLimit
            }
            
            if (approachingAssignments.isNotEmpty()) {
                showNotification(approachingAssignments.size)
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("AssignmentMonitor", "Error monitoring assignments", e)
            return Result.failure()
        }
    }
    
    private fun showNotification(count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("scholar_monitor_channel", "ScholarSync Monitor", NotificationManager.IMPORTANCE_DEFAULT)
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
        
        val title = if (count == 1) "1 Assignment Approaching Deadline" else "$count Assignments Approaching Deadlines"
        val desc = "You have $count practice assignment(s) due within the next 24 hours."
        
        val notification = NotificationCompat.Builder(context, "scholar_monitor_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        // Use a fixed ID so it just updates the existing notification if it's still there
        notificationManager.notify(1001, notification)
    }
}
