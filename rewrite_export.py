import re
import sys

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

export_code = """    fun exportData(uri: Uri, exportAll: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val backupAdapter = moshi.adapter(lumia.tracker.model.ScholarBackup::class.java)
                val fullBackupAdapter = moshi.adapter(lumia.tracker.model.FullAppBackup::class.java)

                if (exportAll && activeProfile.value.isDefault) {
                    val allProfs = profileManager.getAllProfiles()
                    val profileBackupsJson = mutableMapOf<String, String>()
                    for (prof in allProfs) {
                        val db = lumia.tracker.data.AppDatabase.getDatabase(getApplication(), prof.id)
                        val profDao = db.scholarDao()
                        val pref = profileManager.getProfilePrefs(prof.id)
                        val sets = gatherSettings(pref)
                        val pBackup = lumia.tracker.model.ScholarBackup(
                            courses = profDao.exportAllCourses(),
                            subjects = profDao.exportAllSubjects(),
                            topics = profDao.exportAllTopics(),
                            assignments = profDao.exportAllAssignments(),
                            settings = sets,
                            attendance = profDao.exportAllAttendance(),
                            pomodoro = profDao.exportAllPomodoro(),
                            actionLogs = profDao.exportAllActionLogs(),
                            notes = profDao.exportAllNotes(),
                            chapters = profDao.exportAllChapters(),
                            tasks = profDao.exportAllTasks(),
                            attachments = profDao.exportAllAttachments(),
                            testRecords = profDao.exportAllTestRecords(),
                            profile = prof
                        )
                        profileBackupsJson[prof.id] = backupAdapter.toJson(pBackup)
                    }
                    val fullAppBackup = lumia.tracker.model.FullAppBackup(
                        profiles = allProfs,
                        activeProfileId = profileManager.getActiveProfileId(),
                        globalPrefs = emptyMap(),
                        profileBackupsJson = profileBackupsJson
                    )
                    
                    val mainBackup = lumia.tracker.model.ScholarBackup(isFullAppBackup = true, fullAppBackupJson = fullBackupAdapter.toJson(fullAppBackup))
                    
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                        repository.exportDataToStream(os, mainBackup)
                    }
                } else {
                    val currentProf = profileManager.getActiveProfile()
                    val singleBackup = lumia.tracker.model.ScholarBackup(
                        courses = repository.dao.exportAllCourses(),
                        subjects = repository.dao.exportAllSubjects(),
                        topics = repository.dao.exportAllTopics(),
                        assignments = repository.dao.exportAllAssignments(),
                        settings = gatherSettings(),
                        attendance = repository.dao.exportAllAttendance(),
                        pomodoro = repository.dao.exportAllPomodoro(),
                        actionLogs = repository.dao.exportAllActionLogs(),
                        notes = repository.dao.exportAllNotes(),
                        chapters = repository.dao.exportAllChapters(),
                        tasks = repository.dao.exportAllTasks(),
                        attachments = repository.dao.exportAllAttachments(),
                        testRecords = repository.dao.exportAllTestRecords(),
                        profile = currentProf
                    )
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                        repository.exportDataToStream(os, singleBackup)
                    }
                }
                
                _importExportStatus.value = "Secure backup binary package exported successfully"
            } catch (e: Exception) {
                _importExportStatus.value = "Export failed: ${e.message}"
            }
        }
    }"""

# find start and end of exportData
start_idx = content.find("fun exportData(uri: Uri, exportAll: Boolean = false)")
end_idx = content.find("fun importData(uri: Uri)")

content = content[:start_idx] + export_code + "\n\n    " + content[end_idx:]

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
