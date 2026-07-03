import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/CoursesTab.kt", "r") as f:
    content = f.read()

# Replace the items(courses) block
# Since there are varying spaces, we'll use a regex that matches from `items(courses, key = { it.id }) { course ->`
# all the way to the end of the GlassCard block.
start_str = r"            items\(courses, key = \{ it\.id \}\) \{ course ->"
end_str = r"                \}\n            \}"

match = re.search(f"({start_str}.*?{end_str})", content, re.DOTALL)
if match:
    replacement = """            items(courses, key = { it.id }) { course ->
                lumia.tracker.ui.screens.study.CourseItemCard(
                    course = course,
                    onClick = { navController.navigate("courseDetail/${course.id}") },
                    onEdit = { courseToEdit = course },
                    viewModel = viewModel
                )
            }"""
    content = content.replace(match.group(1), replacement)
    with open("app/src/main/java/lumia/tracker/ui/screens/study/CoursesTab.kt", "w") as f:
        f.write(content)
    print("Replaced Course item block in CoursesTab.kt")
else:
    print("Could not find the items(courses...) block.")
