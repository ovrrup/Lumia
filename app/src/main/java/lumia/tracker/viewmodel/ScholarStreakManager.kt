package lumia.tracker.viewmodel

import android.content.SharedPreferences
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import lumia.tracker.data.ScholarRepository
import lumia.tracker.util.NotificationHelper
import lumia.tracker.util.StreakNotifications
import java.util.Calendar

class ScholarStreakManager(
    private val repository: ScholarRepository,
    private val prefs: SharedPreferences,
    private val sendNotification: (String, Int, String, String, Int, Int, String) -> Unit
) {
    val streakTotalNormal = MutableStateFlow(prefs.getInt("streak_total_normal", 0))
    val streakTotalComplete = MutableStateFlow(prefs.getInt("streak_total_complete", 0))
    val streakIsCompleteToday = MutableStateFlow(false)
    val streakPercentage = MutableStateFlow(0f)
    val streakCurrent = MutableStateFlow(prefs.getInt("streak_current", 0))
    val streakLongest = MutableStateFlow(prefs.getInt("streak_longest", 0))

    val streakRequirementTasks = MutableStateFlow(prefs.getInt("streak_req_tasks", 3))
    val streakRequirementAssignments = MutableStateFlow(prefs.getInt("streak_req_assignments", 1))
    val streakRequirementStudyMins = MutableStateFlow(prefs.getInt("streak_req_study_mins", 30))
    val streakPartialThreshold = MutableStateFlow(prefs.getFloat("streak_partial_threshold", 0.5f))
    val streakProgressColor = MutableStateFlow(prefs.getString("streak_progress_color", "#FF5722") ?: "#FF5722")
    val streakBrightness = MutableStateFlow(prefs.getFloat("streak_brightness", 1.0f))
    val streakAnimationOverride = MutableStateFlow(prefs.getString("streak_anim_override", "Default") ?: "Default")
    val streakNotificationTone = MutableStateFlow(prefs.getString("streak_notif_tone", "Motivational") ?: "Motivational")

    fun updateStreakNotificationTone(tone: String) { streakNotificationTone.value = tone; prefs.edit().putString("streak_notif_tone", tone).apply() }
    fun updateStreakReqTasks(count: Int, onProgressChanged: () -> Unit) { streakRequirementTasks.value = count; prefs.edit().putInt("streak_req_tasks", count).apply(); onProgressChanged() }
    fun updateStreakReqAssignments(count: Int, onProgressChanged: () -> Unit) { streakRequirementAssignments.value = count; prefs.edit().putInt("streak_req_assignments", count).apply(); onProgressChanged() }
    fun updateStreakReqStudyMins(mins: Int, onProgressChanged: () -> Unit) { streakRequirementStudyMins.value = mins; prefs.edit().putInt("streak_req_study_mins", mins).apply(); onProgressChanged() }
    fun updateStreakPartialThreshold(thresh: Float, onProgressChanged: () -> Unit) { streakPartialThreshold.value = thresh; prefs.edit().putFloat("streak_partial_threshold", thresh).apply(); onProgressChanged() }
    fun updateStreakProgressColor(colorHex: String) { streakProgressColor.value = colorHex; prefs.edit().putString("streak_progress_color", colorHex).apply() }
    fun updateStreakBrightness(brightness: Float) { streakBrightness.value = brightness; prefs.edit().putFloat("streak_brightness", brightness).apply() }
    fun updateStreakAnimationOverride(anim: String) { streakAnimationOverride.value = anim; prefs.edit().putString("streak_anim_override", anim).apply() }

    private fun todayDateString(): String {
        val cal = Calendar.getInstance()
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    suspend fun calculateTodayStreakProgress() {
        try {
            val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val todayEnd = todayStart + 86400000L

            val dao = repository.dao
            val tasks = dao.exportAllTasks()
            val assignments = dao.exportAllAssignments()
            val pomodoros = dao.exportAllPomodoro()

            val tasksToday = tasks.filter { it.dueDateMillis != null && it.dueDateMillis >= todayStart && it.dueDateMillis < todayEnd }
            val assignmentsToday = assignments.filter { it.dueDateMillis >= todayStart && it.dueDateMillis < todayEnd }
            val pomosToday = pomodoros.filter { it.dateMillis >= todayStart && it.dateMillis < todayEnd }

            val plannedTasks = tasksToday.size
            val plannedAssignments = assignmentsToday.size
            val requiredTasks = maxOf(plannedTasks, streakRequirementTasks.value)
            val requiredAssignments = maxOf(plannedAssignments, streakRequirementAssignments.value)
            val requiredPomos = streakRequirementStudyMins.value

            val actionLogs = dao.exportAllActionLogs()
            val eligibleTasksCompletedTodayTitles = tasks.filter { it.isCompleted && ((it.dueDateMillis != null && it.dueDateMillis >= todayStart) || (it.dueDateMillis == null && it.createdAt >= todayStart)) }.map { it.title }.toSet()
            val eligibleAssignmentsCompletedTodayTitles = assignments.filter { it.isCompleted && it.dueDateMillis >= todayStart }.map { it.title }.toSet()

            val netDoneTasksToday = ScholarStreakCalculator.calculateNetDoneCount(actionLogs, eligibleTasksCompletedTodayTitles, todayStart, todayEnd, "Completed task: ", "Unmarked task: ")
            val netDoneAssignmentsToday = ScholarStreakCalculator.calculateNetDoneCount(actionLogs, eligibleAssignmentsCompletedTodayTitles, todayStart, todayEnd, "Completed assignment: ", "Unmarked assignment: ")

            val doneTasks = maxOf(tasksToday.count { it.isCompleted }, netDoneTasksToday)
            val doneAssignments = maxOf(assignmentsToday.count { it.isCompleted }, netDoneAssignmentsToday)
            val donePomos = pomosToday.sumOf { it.durationMinutes }

            var totalRequired = 0f
            var totalDone = 0f
            val hasAnyTasks = tasks.isNotEmpty() || doneTasks > 0
            val hasAnyAssignments = assignments.isNotEmpty() || doneAssignments > 0
            val hasAnyPomos = pomodoros.isNotEmpty() || donePomos > 0

            if (requiredTasks > 0 && hasAnyTasks) { totalRequired += 1f; totalDone += (doneTasks.toFloat() / requiredTasks.toFloat()).coerceAtMost(1f) }
            if (requiredAssignments > 0 && hasAnyAssignments) { totalRequired += 1f; totalDone += (doneAssignments.toFloat() / requiredAssignments.toFloat()).coerceAtMost(1f) }
            if (requiredPomos > 0 && hasAnyPomos) { totalRequired += 1f; totalDone += (donePomos.toFloat() / requiredPomos.toFloat()).coerceAtMost(1f) }

            val percentage = if (totalRequired == 0f) 0f else (totalDone / totalRequired).coerceIn(0f, 1f)

            withContext(Dispatchers.Main) {
                streakPercentage.value = percentage
                val lastStreakDate = prefs.getLong("streak_last_date", 0L)
                val threshold = streakPartialThreshold.value
                val isComplete = (percentage >= threshold) && (plannedTasks == 0 || doneTasks >= plannedTasks) && (plannedAssignments == 0 || doneAssignments >= plannedAssignments) && (percentage >= 1.0f)
                streakIsCompleteToday.value = isComplete

                val todayStr = todayDateString()
                val statusToday = prefs.getString("streak_status_$todayStr", "none")

                if (isComplete && statusToday != "complete") {
                    val tone = streakNotificationTone.value
                    val message = if (tone == "Motivational") StreakNotifications.motivational.random() else StreakNotifications.aggressive.random()
                    val iconRes = NotificationHelper.getSmallIcon()
                    val colorHex = if (streakProgressColor.value == "Theme") "#3197D6" else streakProgressColor.value
                    val parsedColor = try { Color.parseColor(colorHex) } catch (_: Exception) { Color.parseColor("#3197D6") }
                    sendNotification("scholar_streak_channel", 3001, "Streak Completed!", message, iconRes, parsedColor, "settings/streaks")
                }

                if (percentage >= threshold) {
                    if (statusToday == "none") {
                        val isConsecutive = (todayStart - lastStreakDate) <= 86400000L * 2
                        val newCurrent = if (isConsecutive) streakCurrent.value + 1 else 1
                        streakCurrent.value = newCurrent
                        if (newCurrent > streakLongest.value) {
                            streakLongest.value = newCurrent
                            prefs.edit().putInt("streak_longest", newCurrent).apply()
                        }
                        if (isComplete) {
                            prefs.edit().putString("streak_status_$todayStr", "complete").apply()
                            streakTotalComplete.value += 1
                        } else {
                            prefs.edit().putString("streak_status_$todayStr", "normal").apply()
                            streakTotalNormal.value += 1
                        }
                        prefs.edit().putInt("streak_current", newCurrent).putLong("streak_last_date", todayStart)
                            .putInt("streak_total_normal", streakTotalNormal.value).putInt("streak_total_complete", streakTotalComplete.value).apply()
                    }
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("ScholarStreakManager", "Error calculating streak progress", e)
        }
    }
}
