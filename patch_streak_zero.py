import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val percentage = if (totalRequired == 0f) 1f else (totalDone / totalRequired).coerceIn(0f, 1f)", "val percentage = if (totalRequired == 0f) 0f else (totalDone / totalRequired).coerceIn(0f, 1f)")

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
print("Fixed zero requirement loophole!")

