import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

calc_old = """            val doneTasks = tasksToday.count { it.isCompleted }
            val doneAssignments = assignmentsToday.count { it.isCompleted }"""

calc_new = """            val actionLogs = dao.exportAllActionLogs()
            val completedTasksTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed task:") }
            val unmarkedTasksTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked task:") }
            val netDoneTasksToday = maxOf(0, completedTasksTodayCount - unmarkedTasksTodayCount)
            
            val completedAssignmentsTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed assignment:") }
            val unmarkedAssignmentsTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked assignment:") }
            val netDoneAssignmentsToday = maxOf(0, completedAssignmentsTodayCount - unmarkedAssignmentsTodayCount)

            val doneTasks = maxOf(tasksToday.count { it.isCompleted }, netDoneTasksToday)
            val doneAssignments = maxOf(assignmentsToday.count { it.isCompleted }, netDoneAssignmentsToday)"""

content = content.replace(calc_old, calc_new)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
