package lumia.tracker.service

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PomodoroSessionHandler {
    fun handleWorkSessionCompletion(
        context: Context,
        scope: CoroutineScope,
        originalTime: Int,
        subjectId: Int?,
        courseId: Int?,
        assignmentId: Int?,
        taskId: Int?,
        topicId: Int?
    ) {
        val finishedIntent = Intent("PomodoroLogSession").apply { setPackage(context.packageName) }
        finishedIntent.putExtra("isWork", true)
        finishedIntent.putExtra("originalTime", originalTime)
        subjectId?.let { finishedIntent.putExtra("subjectId", it) }
        courseId?.let { finishedIntent.putExtra("courseId", it) }
        assignmentId?.let { finishedIntent.putExtra("assignmentId", it) }
        taskId?.let { finishedIntent.putExtra("taskId", it) }
        topicId?.let { finishedIntent.putExtra("topicId", it) }
        context.sendBroadcast(finishedIntent)

        val mins = Math.max(1, originalTime / 60)
        scope.launch(Dispatchers.IO) {
            PomodoroSessionLogger.logAndAwardSession(context, mins, isFullCompletion = true, isWorkSession = true, subjectId, courseId, assignmentId, taskId, topicId)
        }
    }
}
