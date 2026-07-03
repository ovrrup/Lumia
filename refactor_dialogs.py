import re
import os

# DashboardScreen.kt
with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "r") as f:
    dashboard_content = f.read()

# Extract AddCourseDialog from DashboardScreen
# It starts with "    if (showAddCourseDialog) {" and ends before "    if (showAddSubjectDialog) {"
# Let's just find the exact block using regex
add_course_match = re.search(r"(\s*if \(showAddCourseDialog\) \{.*?\n\s*\}\n)", dashboard_content, re.DOTALL)
add_subject_match = re.search(r"(\s*if \(showAddSubjectDialog\) \{.*?\n\s*\}\n)", dashboard_content, re.DOTALL)

if add_course_match:
    dashboard_content = dashboard_content.replace(add_course_match.group(1), "\n    if (showAddCourseDialog) {\n        lumia.tracker.ui.screens.study.AddCourseDialog(\n            viewModel = viewModel,\n            onDismiss = { showAddCourseDialog = false }\n        )\n    }\n")

if add_subject_match:
    dashboard_content = dashboard_content.replace(add_subject_match.group(1), "\n    if (showAddSubjectDialog) {\n        lumia.tracker.ui.screens.study.AddSubjectDialog(\n            viewModel = viewModel,\n            onDismiss = { showAddSubjectDialog = false }\n        )\n    }\n")

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "w") as f:
    f.write(dashboard_content)

# CoursesTab.kt
with open("app/src/main/java/lumia/tracker/ui/screens/study/CoursesTab.kt", "r") as f:
    courses_content = f.read()

# It starts with "    if (courseToEdit != null) {" and ends at the end of the file.
# The class ends with a closing brace. So it's inside CoursesTab.kt
edit_course_match = re.search(r"(\s*if \(courseToEdit != null\) \{.*?\n\s*\}\n)\}", courses_content, re.DOTALL)

if edit_course_match:
    courses_content = courses_content.replace(edit_course_match.group(1), "\n    if (courseToEdit != null) {\n        lumia.tracker.ui.screens.study.EditCourseDialog(\n            course = courseToEdit!!,\n            viewModel = viewModel,\n            onDismiss = { courseToEdit = null }\n        )\n    }\n")
    with open("app/src/main/java/lumia/tracker/ui/screens/study/CoursesTab.kt", "w") as f:
        f.write(courses_content)

print("Modified DashboardScreen.kt and CoursesTab.kt")
