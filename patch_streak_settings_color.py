import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('val colors = listOf("#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")',
'''val colors = listOf("Theme", "#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")''')

content = content.replace('''Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { viewModel.updateStreakProgressColor(hex) },''',
'''Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (hex == "Theme") MaterialTheme.colorScheme.primary else Color(android.graphics.Color.parseColor(hex)))
                                .clickable { viewModel.updateStreakProgressColor(hex) },''')

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "w") as f:
    f.write(content)
