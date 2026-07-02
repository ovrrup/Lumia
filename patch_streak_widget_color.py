import re

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "r") as f:
    content = f.read()

content = content.replace('''val baseColor = try {
        Color(android.graphics.Color.parseColor(streakColorHex))
    } catch (e: Exception) {
        Color(0xFFFF9800)
    }''',
'''val primary = MaterialTheme.colorScheme.primary
    val baseColor = if (streakColorHex == "Theme") primary else try {
        Color(android.graphics.Color.parseColor(streakColorHex))
    } catch (e: Exception) {
        Color(0xFFFF9800)
    }''')

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "w") as f:
    f.write(content)
