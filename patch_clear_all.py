import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_clear = """            if (prof.isDefault) {
                // Main user: Erase all users' data. Delete secondary users entirely.
                val allProfs = profileManager.getAllProfiles()
                for (p in allProfs) {
                    if (p.id != "DEFAULT") {
                        // Clear their db and prefs
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                        profileManager.deleteProfile(p.id)
                    } else {
                        // Clear main user data
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                    }
                }
                allProfiles.value = profileManager.getAllProfiles()
            }"""

new_clear = """            if (prof.isDefault) {
                // Main user: Erase all users' data. Delete secondary users entirely.
                val allProfs = profileManager.getAllProfiles()
                for (p in allProfs) {
                    if (!p.isDefault) {
                        // Clear their db and prefs
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                        profileManager.deleteProfile(p.id)
                    } else {
                        // Clear main user data
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), p.id)
                        db.clearAllTables()
                        profileManager.getProfilePrefs(p.id).edit().clear().apply()
                    }
                }
                allProfiles.value = profileManager.getAllProfiles()
            }"""

content = content.replace(old_clear, new_clear)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)

