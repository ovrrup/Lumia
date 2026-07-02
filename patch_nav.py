import re

with open("app/src/main/java/lumia/tracker/MainActivity.kt", "r") as f:
    content = f.read()

nav_str = """
                        composable("settings/streaks") {
                            lumia.tracker.ui.screens.settings.StreakSettingsScreen(navController, viewModel)
                        }
"""

content = content.replace('composable("settings/appearance") {', nav_str + '\n                        composable("settings/appearance") {')

with open("app/src/main/java/lumia/tracker/MainActivity.kt", "w") as f:
    f.write(content)
