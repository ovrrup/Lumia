import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_tasks_logic = """            val completedTasksTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed task:") }
            val unmarkedTasksTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked task:") }
            val netDoneTasksToday = maxOf(0, completedTasksTodayCount - unmarkedTasksTodayCount)"""

new_tasks_logic = """            val completedTasksToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed task:") }
            val unmarkedTasksToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked task:") }
            
            val completedTitles = completedTasksToday.map { it.actionText.removePrefix("Completed task: ").trim() }
            val unmarkedTitles = unmarkedTasksToday.map { it.actionText.removePrefix("Unmarked task: ").trim() }
            
            val cCounts = completedTitles.groupingBy { it }.eachCount()
            val uCounts = unmarkedTitles.groupingBy { it }.eachCount()
            
            var netDoneTasksToday = 0
            for ((title, count) in cCounts) {
                val uCount = uCounts[title] ?: 0
                netDoneTasksToday += maxOf(0, count - uCount)
            }"""

content = content.replace(old_tasks_logic, new_tasks_logic)

old_assignments_logic = """            val completedAssignmentsTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed assignment:") }
            val unmarkedAssignmentsTodayCount = actionLogs.count { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked assignment:") }
            val netDoneAssignmentsToday = maxOf(0, completedAssignmentsTodayCount - unmarkedAssignmentsTodayCount)"""

new_assignments_logic = """            val completedAssignmentsToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Completed assignment:") }
            val unmarkedAssignmentsToday = actionLogs.filter { it.timestampMillis >= todayStart && it.timestampMillis < todayEnd && it.actionText.startsWith("Unmarked assignment:") }
            
            val cAssignTitles = completedAssignmentsToday.map { it.actionText.removePrefix("Completed assignment: ").trim() }
            val uAssignTitles = unmarkedAssignmentsToday.map { it.actionText.removePrefix("Unmarked assignment: ").trim() }
            
            val cAssignCounts = cAssignTitles.groupingBy { it }.eachCount()
            val uAssignCounts = uAssignTitles.groupingBy { it }.eachCount()
            
            var netDoneAssignmentsToday = 0
            for ((title, count) in cAssignCounts) {
                val uCount = uAssignCounts[title] ?: 0
                netDoneAssignmentsToday += maxOf(0, count - uCount)
            }"""

content = content.replace(old_assignments_logic, new_assignments_logic)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
print("Fixed net completed items logic!")
