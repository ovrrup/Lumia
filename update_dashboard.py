import re

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace AddCourseDialog
add_course_regex = re.compile(r"    if \(showAddCourseDialog\) \{.*?    \}\n\n    if \(showAddSubjectDialog\) \{", re.DOTALL)
content = add_course_regex.sub("""    if (showAddCourseDialog) {
        lumia.tracker.ui.screens.study.AddCourseDialog(
            viewModel = viewModel,
            onDismiss = { showAddCourseDialog = false }
        )
    }

    if (showAddSubjectDialog) {""", content)

# Replace AddSubjectDialog
add_subj_regex = re.compile(r"    if \(showAddSubjectDialog\) \{.*?        \)\n    \}", re.DOTALL)
content = add_subj_regex.sub("""    if (showAddSubjectDialog) {
        lumia.tracker.ui.screens.study.AddSubjectDialog(
            viewModel = viewModel,
            onDismiss = { showAddSubjectDialog = false }
        )
    }""", content)

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Updated DashboardScreen.kt")
