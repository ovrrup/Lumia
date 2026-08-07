package lumia.tracker.util

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.FullAppBackup
import lumia.tracker.model.ScholarBackup
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

object AutoCrashBackupExporter {
    private const val TAG = "AutoCrashBackupExporter"

    fun exportFullAppBackupSync(context: Context, outputFile: File): Boolean {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val backupAdapter = moshi.adapter(ScholarBackup::class.java)
            val fullAdapter = moshi.adapter(FullAppBackup::class.java)

            val profileManager = ProfileManager(context)
            val allProfs = profileManager.getAllProfiles()
            val profileBackupsJson = mutableMapOf<String, String>()

            runBlocking(Dispatchers.IO) {
                for (prof in allProfs) {
                    try {
                        val db = AppDatabase.getDatabase(context, prof.id)
                        val profDao = db.scholarDao()
                        val pref = profileManager.getProfilePrefs(prof.id)
                        val sets = pref.all.mapValues { it.value?.toString() ?: "" }
                        val profAttachments = profDao.exportAllAttachments()

                        val pBackup = ScholarBackup(
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
                            attachments = profAttachments,
                            testRecords = profDao.exportAllTestRecords(),
                            profile = prof
                        )
                        profileBackupsJson[prof.id] = backupAdapter.toJson(pBackup)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error exporting profile ${prof.id} during emergency backup", e)
                    }
                }
            }

            val fullAppBackup = FullAppBackup(
                profiles = allProfs,
                activeProfileId = profileManager.getActiveProfileId(),
                globalPrefs = emptyMap(),
                profileBackupsJson = profileBackupsJson
            )

            val mainBackup = ScholarBackup(isFullAppBackup = true, fullAppBackupJson = fullAdapter.toJson(fullAppBackup))
            val jsonStr = backupAdapter.toJson(mainBackup)

            FileOutputStream(outputFile).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    gzos.writer().use { writer ->
                        writer.write(jsonStr)
                    }
                }
            }
            true
        } catch (e: Throwable) {
            Log.e(TAG, "exportFullAppBackupSync error", e)
            false
        }
    }

    fun copyRawDatabaseFiles(context: Context, destDir: File) {
        try {
            val dbFolder = context.getDatabasePath("scholar_sync_database").parentFile ?: return
            dbFolder.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.contains("scholar") || file.name.endsWith(".db") || file.name.endsWith(".db-wal") || file.name.endsWith(".db-shm"))) {
                    file.copyTo(File(destDir, file.name), overwrite = true)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed copying raw DB files", e)
        }
    }
}
