package lumia.tracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lumia.tracker.data.AppDatabase

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast action: $action")
        
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val assignments = db.scholarDao().exportAllAssignments()
                    
                    val now = System.currentTimeMillis()
                    var scheduledCount = 0
                    
                    for (assignment in assignments) {
                        if (!assignment.isCompleted) {
                            val alarmTime = assignment.dueDateMillis - (1000 * 60 * 60)
                            if (alarmTime > now) {
                                ReminderScheduler.scheduleReminderExact(
                                    context = context,
                                    assignmentId = assignment.id,
                                    title = assignment.title,
                                    desc = assignment.description,
                                    interconnections = assignment.tags,
                                    triggerTime = alarmTime
                                )
                                scheduledCount++
                            }
                        }
                    }

                    val tasks = db.scholarDao().exportAllTasks()
                    for (task in tasks) {
                        if (!task.isCompleted && task.dueDateMillis != null) {
                            val alarmTime = task.dueDateMillis - (1000 * 60 * 60)
                            if (alarmTime > now) {
                                ReminderScheduler.scheduleReminderExact(
                                    context = context,
                                    assignmentId = task.id + 20000,
                                    title = task.title,
                                    desc = task.description,
                                    interconnections = task.tags,
                                    triggerTime = alarmTime
                                )
                                scheduledCount++
                            }
                        }
                    }
                    
                    // Trigger the monitor worker to ensure streaks, digest, and class attendance timings are properly configured for the current day
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<lumia.tracker.worker.AssignmentMonitorWorker>().build()
                    androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("boot_assignment_monitor", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
                    
                    Log.d("BootReceiver", "Restored $scheduledCount reminders successfully and triggered AssignmentMonitorWorker.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring notifications on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
