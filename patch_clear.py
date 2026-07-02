import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

start_idx = content.find("fun clearAllData()")
end_idx = content.find("_importExportStatus.value = \"All data and settings erased successfully\"", start_idx) + len("_importExportStatus.value = \"All data and settings erased successfully\"")

new_code = """    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = activeProfile.value
            if (prof.isDefault) {
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
            } else {
                // Secondary user: Erase their own data and delete their account
                val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                db.clearAllTables()
                profileManager.getProfilePrefs(prof.id).edit().clear().apply()
                profileManager.deleteProfile(prof.id)
                switchProfileAndRestart(getApplication(), "DEFAULT")
                return@launch
            }

            _themeMode.value = "System"
            _themeColor.value = "Ocean"
            _customPrimary.value = "#3197D6"
            _customPrimaryContainer.value = "#DAF1FF"
            _customBackground.value = "#FAFAFA"
            _customSurface.value = "#FFFFFF"
            _customText.value = "#1A1C1A"
            _pureBlackMode.value = false
            _betaFloatingNav.value = false
            _betaNotes.value = false
            _displayLayoutMode.value = "Immersive"
            _betaGlassUi.value = false
            _betaGlassDynamic.value = true
            _betaFrostGlass.value = true
            _betaUsePaletteForText.value = false
            _aodTrueAodEnabled.value = false
            _aodTrueAodMode.value = "Clock"
            _aodDimnessLevel.value = 0.95f
            _aodSensitivity.value = 5
            _aodLockTimeout.value = 15L
            _aodMotionSensitivity.value = 3.0f
            _aodTapToWake.value = true

            _importExportStatus.value = "All data and settings erased successfully" """

content = content[:start_idx] + new_code + content[end_idx:]

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
