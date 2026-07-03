import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/CourseDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onCheckedChange = { ch -> viewModel.updateTask(task.copy(isCompleted = ch)) }", "onCheckedChange = { viewModel.toggleTaskCompleted(task) }")

with open("app/src/main/java/lumia/tracker/ui/screens/study/CourseDetailScreen.kt", "w") as f:
    f.write(content)
print("Fixed CourseDetailScreen task toggle")

