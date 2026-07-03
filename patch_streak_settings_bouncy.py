import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "r") as f:
    content = f.read()

# add bouncyClick import if not present
if "import lumia.tracker.ui.theme.bouncyClick" not in content:
    content = content.replace("import lumia.tracker.ui.screens.SettingsGroupCard", "import lumia.tracker.ui.screens.SettingsGroupCard\nimport lumia.tracker.ui.theme.bouncyClick")

# replace .clickable { viewModel.updateStreakProgressColor(hex) } with .bouncyClick { viewModel.updateStreakProgressColor(hex) }
content = content.replace(".clickable { viewModel.updateStreakProgressColor(hex) }", ".bouncyClick { viewModel.updateStreakProgressColor(hex) }")

# replace .clickable { viewModel.updateStreakAnimationOverride(style) } with .bouncyClick { viewModel.updateStreakAnimationOverride(style) }
content = content.replace(".clickable { viewModel.updateStreakAnimationOverride(style) }", ".bouncyClick { viewModel.updateStreakAnimationOverride(style) }")

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "w") as f:
    f.write(content)

