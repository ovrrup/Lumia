import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())",
"lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())\n            calculateTodayStreakProgress()")

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
