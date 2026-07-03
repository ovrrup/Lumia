import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

new_methods = """
    fun switchMainAccountAndDeleteCurrent(successorId: String, createNew: Boolean = false, newName: String = "", newAvatar: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (prof.isDefault) {
                var newMainId = successorId
                if (createNew) {
                    newMainId = profileManager.addProfile(newName, newAvatar)
                }
                
                // Update the successor to be default
                val allProfs = profileManager.getAllProfiles()
                val successor = allProfs.find { it.id == newMainId }
                if (successor != null) {
                    successor.isDefault = true
                    profileManager.updateProfile(successor)
                }
                
                // Clear this user's data
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                
                switchProfileAndRestart(getApplication(), newMainId)
            }
        }
    }

    fun eraseMyDataAndAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (!prof.isDefault) {
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                switchProfileAndRestart(getApplication(), "DEFAULT") // Switch to a safe account
            }
        }
    }
"""

idx = content.find("fun clearAllData()")
if idx != -1:
    content = content[:idx] + new_methods + content[idx:]
    with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
        f.write(content)
else:
    print("Not found clearAllData")

