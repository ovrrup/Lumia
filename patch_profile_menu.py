import re

with open("app/src/main/java/lumia/tracker/ui/screens/home/ProfileMenuScreen.kt", "r") as f:
    content = f.read()

streak_item = """
                    SettingsItem(
                        icon = androidx.compose.material.icons.Icons.Rounded.LocalFireDepartment,
                        title = "Streak Goals & Visuals",
                        subtitle = "Configure percentage limits, fire color, and liquid animations",
                        onClick = { navController.navigate("settings/streaks") }
                    )
"""

content = content.replace('onClick = { navController.navigate("settings/system") }\n                    )', 'onClick = { navController.navigate("settings/system") }\n                    )\n' + streak_item)

# also add import for LocalFireDepartment if it's missing (though it might be imported or we can use fully qualified name)

with open("app/src/main/java/lumia/tracker/ui/screens/home/ProfileMenuScreen.kt", "w") as f:
    f.write(content)
