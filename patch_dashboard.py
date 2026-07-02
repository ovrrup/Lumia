import re

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("val isMrGlass = moreRounds && moreRoundsMode == \"Glass\"",
    "lumia.tracker.ui.components.StreakWidget(viewModel, navController)\n                            val isMrGlass = moreRounds && moreRoundsMode == \"Glass\"")

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "w") as f:
    f.write(content)
