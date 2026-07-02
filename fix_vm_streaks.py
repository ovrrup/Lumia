import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('''    private fun verifyFeatureEntitlements()
        calculateTodayStreakProgress() {''', "    private fun verifyFeatureEntitlements() {")

# Double check if any other syntax errors exist in verifyFeatureEntitlements replacement
content = content.replace("verifyFeatureEntitlements()\n        calculateTodayStreakProgress()\n    }", "verifyFeatureEntitlements()\n    }") # if any

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
