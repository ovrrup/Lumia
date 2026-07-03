import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_req = """            // Prioritize planned vs required
            val requiredTasks = maxOf(plannedTasks, _streakRequirementTasks.value)
            val requiredAssignments = maxOf(plannedAssignments, _streakRequirementAssignments.value)"""

new_req = """            // Prioritize planned vs required
            val requiredTasks = if (plannedTasks > 0) plannedTasks else _streakRequirementTasks.value
            val requiredAssignments = if (plannedAssignments > 0) plannedAssignments else _streakRequirementAssignments.value"""

if old_req in content:
    content = content.replace(old_req, new_req)
    with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found.")
