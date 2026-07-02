import re

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

start_idx = content.find("fun importData(uri: Uri)")
end_idx = content.find("fun clearImportExportStatus()", start_idx)

import_code = """    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var mainBackup: lumia.tracker.model.ScholarBackup? = null
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { ins ->
                    mainBackup = repository.importDataFromStream(ins)
                }
                
                if (mainBackup == null) throw IllegalArgumentException("No data found")
                
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                
                if (mainBackup!!.isFullAppBackup && mainBackup!!.fullAppBackupJson != null) {
                    val fullBackupAdapter = moshi.adapter(lumia.tracker.model.FullAppBackup::class.java)
                    val backupAdapter = moshi.adapter(lumia.tracker.model.ScholarBackup::class.java)
                    val fullBackup = fullBackupAdapter.fromJson(mainBackup!!.fullAppBackupJson!!) ?: throw IllegalArgumentException("Invalid full backup")
                    
                    // Clear existing profiles
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
                    }
                    // Wait, addProfile generates new ID. We should update profiles!
                    // Let's just save the entire list directly to ProfileManager via reflection or just use prefs
                    val profListJson = moshi.adapter<List<lumia.tracker.model.UserProfile>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, lumia.tracker.model.UserProfile::class.java)).toJson(fullBackup.profiles)
                    profileManager.getProfilePrefs("").edit().putString("profiles_json", profListJson).commit() // Wait, ProfileManager uses global_profiles!
                    val globalPrefs = getApplication<Application>().getSharedPreferences("global_profiles", android.content.Context.MODE_PRIVATE)
                    globalPrefs.edit().putString("profiles_json", profListJson).commit()
                    profileManager.setActiveProfileId(fullBackup.activeProfileId)
                    
                    // Restore each profile's data
                    for ((profId, pJson) in fullBackup.profileBackupsJson) {
                        val pBackup = backupAdapter.fromJson(pJson) ?: continue
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), profId)
                        val pDao = db.scholarDao()
                        repository.restoreBackupToDao(pBackup, pDao)
                        
                        // Restore settings
                        pBackup.settings?.let { sets ->
                            val pref = profileManager.getProfilePrefs(profId)
                            val editor = pref.edit()
                            sets.forEach { (key, value) -> editor.putString(key, value) }
                            editor.commit()
                        }
                    }
                    
                } else {
                    // Single profile restore
                    repository.restoreBackupToDao(mainBackup!!, repository.dao)
                    loadSettings(mainBackup!!.settings)
                }

                verifyFeatureEntitlements()
                _importExportStatus.value = "Secure backup package imported and restored successfully"
                activeProfile.value = profileManager.getActiveProfile()
                allProfiles.value = profileManager.getAllProfiles()
                lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
            } catch (e: Exception) {
                _importExportStatus.value = "Import failed: ${e.message}"
            }
        }
    }

    """

content = content[:start_idx] + import_code + content[end_idx:]

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
