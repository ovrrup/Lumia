import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

old_import = """                    // Clear existing profiles
                    val currentProfs = profileManager.getAllProfiles()
                    for (prof in currentProfs) {
                        if (prof.id != "DEFAULT") {
                            profileManager.deleteProfile(prof.id)
                        }
                    }
                    
                    // Restore profiles
                    for (prof in fullBackup.profiles) {
                        if (prof.id == "DEFAULT") continue
                        profileManager.addProfile(prof.name, prof.avatarEmoji, prof.alias, prof.starterTheme)
                    }"""

new_import = """                    // Clear existing profiles
                    val currentProfs = profileManager.getAllProfiles()
                    for (prof in currentProfs) {
                        if (!prof.isDefault) {
                            profileManager.deleteProfile(prof.id)
                        }
                    }
                    
                    // Restore profiles
                    for (prof in fullBackup.profiles) {
                        if (prof.isDefault) continue
                        profileManager.addProfile(prof.name, prof.avatarEmoji, prof.alias, prof.starterTheme)
                    }"""

content = content.replace(old_import, new_import)

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)

