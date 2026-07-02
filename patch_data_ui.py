import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/DataManagementScreen.kt", "r") as f:
    content = f.read()

content = content.replace('title = "Full Environment Factory Erase",\n                    subtitle = "Permanently delete all customisations, courses, subjects, assignments, and local history reports"', 
'''title = if (activeProfile.isDefault) "Full Environment Factory Erase (All Accounts)" else "Erase Data & Delete Account",
                    subtitle = if (activeProfile.isDefault) "Permanently delete all data for all accounts on this device" else "Permanently delete your profile and all your data"''')

content = content.replace('text = { Text("Are you sure you want to export a binary backup of all your data and settings?") }',
'text = { Text(if (activeProfile.isDefault) "Export a backup of ALL user accounts?" else "Export a backup of YOUR data?") }')

content = content.replace('title = { Text("Erase All Data & Settings?") },\n            text = { Text("This action cannot be undone. All your progress, custom themes, subjects, and settings will be permanently removed.", color = MaterialTheme.colorScheme.error) }',
'''title = { Text(if (activeProfile.isDefault) "Erase All App Data?" else "Erase Data & Delete Account?") },
            text = { Text(if (activeProfile.isDefault) "This action cannot be undone. ALL user accounts (except Main) and their data will be permanently removed." else "This action cannot be undone. Your profile and all your data will be permanently removed.", color = MaterialTheme.colorScheme.error) }''')

with open("app/src/main/java/lumia/tracker/ui/screens/settings/DataManagementScreen.kt", "w") as f:
    f.write(content)
