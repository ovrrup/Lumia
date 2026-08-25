package lumia.tracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lumia.tracker.data.AppDatabase
import lumia.tracker.worker.AssignmentMonitorWorker

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
                    val now = System.currentTimeMillis()
                    var scheduledCount = 0

                    // 1. Restore incomplete assignments
                    val assignments = db.scholarDao().exportAllAssignments()
                    for (assignment in assignments) {
                        if (!assignment.isCompleted && assignment.dueDateMillis > now) {
                            val triggerTime = assignment.dueDateMillis - (60 * 60 * 1000L)
                            val finalTrigger = if (triggerTime > now) triggerTime else (now + 5000L)
                            ReminderScheduler.scheduleReminderExact(
                                context = context,
                                assignmentId = assignment.id,
                                title = assignment.title,
                                desc = assignment.description,
                                interconnections = assignment.tags,
                                triggerTime = finalTrigger,
                                type = "assignment",
                                courseId = assignment.courseId,
                                subjectId = assignment.subjectId
                            )
                            scheduledCount++
                        }
                    }

                    // 2. Restore incomplete tasks
                    val tasks = db.scholarDao().exportAllTasks()
                    for (task in tasks) {
                        val due = task.dueDateMillis
                        if (!task.isCompleted && due != null && due > now) {
                            val triggerTime = due - (60 * 60 * 1000L)
                            val finalTrigger = if (triggerTime > now) triggerTime else (now + 5000L)
                            ReminderScheduler.scheduleReminderExact(
                                context = context,
                                assignmentId = task.id + 20000,
                                title = task.title,
                                desc = task.description,
                                interconnections = task.tags,
                                triggerTime = finalTrigger,
                                type = "task",
                                courseId = task.courseId,
                                subjectId = task.subjectId
                            )
                            scheduledCount++
                        }
                    }

                    // 3. Trigger AssignmentMonitorWorker for daily digest & class schedule
                    try {
                        val workRequest = OneTimeWorkRequestBuilder<AssignmentMonitorWorker>().build()
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "boot_assignment_monitor",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "WorkManager enqueue failed on boot", e)
                    }

                    Log.d("BootReceiver", "Restored $scheduledCount reminders on boot")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring notifications on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
