package lumia.tracker.service

import android.content.Context
import android.content.Intent

enum class PomodoroMode { WORK, SHORT_BREAK, LONG_BREAK }

data class PomodoroState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val timeLeft: Int = 25 * 60,
    val originalTime: Int = 25 * 60,
    val modeString: String = "WORK",
    val sessionsCompleted: Int = 0,
    val subjectId: Int? = null,
    val courseId: Int? = null,
    val assignmentId: Int? = null,
    val taskId: Int? = null,
    val topicId: Int? = null,
    val isAlarmActive: Boolean = false,
    val endedModeStr: String = ""
)

class PomodoroActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val serviceIntent = Intent(context, PomodoroService::class.java).apply { 
            this.action = action 
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
