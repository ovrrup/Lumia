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
                    // Instantly and accurately restore all active assignment, task, and class alarms
                    ReminderScheduler.rescheduleAllAlarms(context)
                    
                    // Trigger the monitor worker to ensure digest is properly configured for the current day
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<lumia.tracker.worker.AssignmentMonitorWorker>().build()
                    androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("boot_assignment_monitor", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
                    
                    Log.d("BootReceiver", "Restored all reminders successfully on boot and triggered AssignmentMonitorWorker.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring notifications on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
