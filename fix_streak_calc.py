import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

calc_old = """            if (requiredTasks > 0) { totalRequired += 1f; totalDone += doneTasks.toFloat() / requiredTasks.toFloat() }
            if (requiredAssignments > 0) { totalRequired += 1f; totalDone += doneAssignments.toFloat() / requiredAssignments.toFloat() }
            if (requiredPomos > 0) { totalRequired += 1f; totalDone += donePomos.toFloat() / requiredPomos.toFloat() }
            
            val percentage = if (totalRequired == 0f) 0f else (totalDone / totalRequired).coerceIn(0f, 1f)"""

calc_new = """            if (requiredTasks > 0) { totalRequired += 1f; totalDone += (doneTasks.toFloat() / requiredTasks.toFloat()).coerceAtMost(1f) }
            if (requiredAssignments > 0) { totalRequired += 1f; totalDone += (doneAssignments.toFloat() / requiredAssignments.toFloat()).coerceAtMost(1f) }
            if (requiredPomos > 0) { totalRequired += 1f; totalDone += (donePomos.toFloat() / requiredPomos.toFloat()).coerceAtMost(1f) }
            
            val percentage = if (totalRequired == 0f) 1f else (totalDone / totalRequired).coerceIn(0f, 1f)"""

content = content.replace(calc_old, calc_new)

add_pomo_old = """    fun addPomodoroSession(durationMinutes: Int, subjectId: Int? = null, courseId: Int? = null, assignmentId: Int? = null, taskId: Int? = null) {
        viewModelScope.launch {
            repository.insertPomodoroSession(lumia.tracker.model.PomodoroSession(
                dateMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                subjectId = subjectId,
                courseId = courseId,
                assignmentId = assignmentId,
                taskId = taskId
            ))
            logAction("Completed Pomodoro Session ($durationMinutes min)")
        }
    }"""

add_pomo_new = """    fun addPomodoroSession(durationMinutes: Int, subjectId: Int? = null, courseId: Int? = null, assignmentId: Int? = null, taskId: Int? = null) {
        viewModelScope.launch {
            repository.insertPomodoroSession(lumia.tracker.model.PomodoroSession(
                dateMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                subjectId = subjectId,
                courseId = courseId,
                assignmentId = assignmentId,
                taskId = taskId
            ))
            logAction("Completed Pomodoro Session ($durationMinutes min)")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            calculateTodayStreakProgress()
        }
    }"""

content = content.replace(add_pomo_old, add_pomo_new)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
