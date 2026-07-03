import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_logic = """            val requiredTasks = if (plannedTasks > 0) plannedTasks else _streakRequirementTasks.value
            val requiredAssignments = if (plannedAssignments > 0) plannedAssignments else _streakRequirementAssignments.value"""

new_logic = """            val requiredTasks = maxOf(plannedTasks, _streakRequirementTasks.value)
            val requiredAssignments = maxOf(plannedAssignments, _streakRequirementAssignments.value)"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
print("Fixed planned items loophole!")
