package lumia.tracker.sync.merge

import android.app.Application
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.data.ScholarDao
import lumia.tracker.model.*
import lumia.tracker.sync.model.SyncMergeReport

/**
 * Intelligent conflict-resolution and delta merging engine for Lumia Multi-Device Sync.
 * Ensures zero data loss and prevents duplicate entity creation during P2P sync.
 */
object SyncMergeEngine {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(ScholarBackup::class.java)
    private val fullBackupAdapter = moshi.adapter(FullAppBackup::class.java)

    /**
     * Merges a remote FullAppBackup or single ScholarBackup with the local database.
     */
    suspend fun mergeFullApp(
        context: Context,
        profileManager: ProfileManager,
        remoteBackup: ScholarBackup,
        peerDeviceName: String,
        syncMode: String = "SMART_MERGE"
    ): SyncMergeReport = withContext(Dispatchers.IO) {
        var coursesMerged = 0
        var subjectsMerged = 0
        var tasksMerged = 0
        var assignmentsMerged = 0
        var notesMerged = 0
        var pomodoroMerged = 0
        var profilesSynced = 0
        var tagCustomizationsMerged = 0

        if (remoteBackup.isFullAppBackup && !remoteBackup.fullAppBackupJson.isNullOrBlank()) {
            val fullRemote = try {
                fullBackupAdapter.fromJson(remoteBackup.fullAppBackupJson)
            } catch (e: Exception) {
                null
            }

            if (fullRemote != null) {
                val localProfiles = profileManager.getAllProfiles().toMutableList()
                val localProfileNames = localProfiles.associateBy { it.name.trim().lowercase() }

                for (remoteProf in fullRemote.profiles) {
                    val matchingLocal = localProfileNames[remoteProf.name.trim().lowercase()]
                    val targetProfileId: String

                    if (matchingLocal != null) {
                        targetProfileId = matchingLocal.id
                        profilesSynced++
                    } else {
                        // Create new profile locally
                        targetProfileId = profileManager.addProfile(
                            name = remoteProf.name,
                            avatarEmoji = remoteProf.avatarEmoji,
                            alias = remoteProf.alias,
                            starterTheme = remoteProf.starterTheme
                        )
                        profilesSynced++
                    }

                    val rawRemoteJson = fullRemote.profileBackupsJson[remoteProf.id]
                    if (!rawRemoteJson.isNullOrBlank()) {
                        val singleRemoteBackup = try {
                            backupAdapter.fromJson(rawRemoteJson)
                        } catch (e: Exception) {
                            null
                        }

                        if (singleRemoteBackup != null) {
                            val db = AppDatabase.getDatabase(context, targetProfileId)
                            val dao = db.scholarDao()
                            val report = mergeSingleScholarDatabase(dao, singleRemoteBackup)

                            coursesMerged += report.coursesMerged
                            subjectsMerged += report.subjectsMerged
                            tasksMerged += report.tasksMerged
                            assignmentsMerged += report.assignmentsMerged
                            notesMerged += report.notesMerged
                            pomodoroMerged += report.pomodoroSessionsMerged
                            tagCustomizationsMerged += report.tagCustomizationsMerged
                        }
                    }
                }
            }
        } else {
            // Merge into active profile
            val activeId = profileManager.getActiveProfileId()
            val db = AppDatabase.getDatabase(context, activeId)
            val dao = db.scholarDao()
            val report = mergeSingleScholarDatabase(dao, remoteBackup)

            coursesMerged = report.coursesMerged
            subjectsMerged = report.subjectsMerged
            tasksMerged = report.tasksMerged
            assignmentsMerged = report.assignmentsMerged
            notesMerged = report.notesMerged
            pomodoroMerged = report.pomodoroSessionsMerged
            tagCustomizationsMerged = report.tagCustomizationsMerged
            profilesSynced = 1
        }

        SyncMergeReport(
            coursesMerged = coursesMerged,
            subjectsMerged = subjectsMerged,
            tasksMerged = tasksMerged,
            assignmentsMerged = assignmentsMerged,
            notesMerged = notesMerged,
            pomodoroSessionsMerged = pomodoroMerged,
            profilesSynced = profilesSynced,
            tagCustomizationsMerged = tagCustomizationsMerged,
            peerDeviceName = peerDeviceName,
            syncTimestamp = System.currentTimeMillis(),
            syncMode = syncMode
        )
    }

    /**
     * Merges a single ScholarBackup into the specified ScholarDao.
     */
    suspend fun mergeSingleScholarDatabase(
        dao: ScholarDao,
        remoteBackup: ScholarBackup
    ): SyncMergeReport = withContext(Dispatchers.IO) {
        var coursesMerged = 0
        var subjectsMerged = 0
        var tasksMerged = 0
        var assignmentsMerged = 0
        var notesMerged = 0
        var pomodoroMerged = 0
        var tagCustomizationsMerged = 0

        // 1. Merge Subjects
        val localSubjects = dao.exportAllSubjects()
        val subjectMap = localSubjects.associateBy { it.name.trim().lowercase() }.toMutableMap()
        val remoteToLocalSubjectId = mutableMapOf<Int, Int>()

        remoteBackup.subjects?.forEach { rSub ->
            val match = subjectMap[rSub.name.trim().lowercase()]
            if (match != null) {
                remoteToLocalSubjectId[rSub.id] = match.id
            } else {
                val newId = dao.insertSubject(rSub.copy(id = 0)).toInt()
                val created = rSub.copy(id = newId)
                subjectMap[created.name.trim().lowercase()] = created
                remoteToLocalSubjectId[rSub.id] = newId
                subjectsMerged++
            }
        }

        // 2. Merge Courses
        val localCourses = dao.exportAllCourses()
        val courseMap = localCourses.associateBy { it.name.trim().lowercase() }.toMutableMap()
        val remoteToLocalCourseId = mutableMapOf<Int, Int>()

        remoteBackup.courses?.forEach { rCourse ->
            val match = courseMap[rCourse.name.trim().lowercase()]
            val mappedSubjectId = rCourse.subjectId?.let { remoteToLocalSubjectId[it] }

            if (match != null) {
                remoteToLocalCourseId[rCourse.id] = match.id
                val updated = match.copy(
                    attendedClasses = maxOf(match.attendedClasses, rCourse.attendedClasses),
                    totalClasses = maxOf(match.totalClasses, rCourse.totalClasses),
                    description = if (match.description.isBlank()) rCourse.description else match.description,
                    instructor = if (match.instructor.isBlank()) rCourse.instructor else match.instructor,
                    schedule = if (match.schedule.isBlank()) rCourse.schedule else match.schedule,
                    subjectId = match.subjectId ?: mappedSubjectId
                )
                if (updated != match) {
                    dao.updateCourse(updated)
                    courseMap[updated.name.trim().lowercase()] = updated
                    coursesMerged++
                }
            } else {
                val newId = dao.insertCourse(rCourse.copy(id = 0, subjectId = mappedSubjectId)).toInt()
                val created = rCourse.copy(id = newId, subjectId = mappedSubjectId)
                courseMap[created.name.trim().lowercase()] = created
                remoteToLocalCourseId[rCourse.id] = newId
                coursesMerged++
            }
        }

        // 3. Merge Chapters
        val localChapters = dao.exportAllChapters()
        val remoteToLocalChapterId = mutableMapOf<Int, Int>()
        remoteBackup.chapters?.forEach { rChap ->
            val localSubId = remoteToLocalSubjectId[rChap.subjectId] ?: rChap.subjectId
            val existing = localChapters.find { it.subjectId == localSubId && it.name.equals(rChap.name.trim(), ignoreCase = true) }
            if (existing != null) {
                remoteToLocalChapterId[rChap.id] = existing.id
            } else {
                val newId = dao.insertChapter(rChap.copy(id = 0, subjectId = localSubId)).toInt()
                remoteToLocalChapterId[rChap.id] = newId
            }
        }

        // 4. Merge Topics
        val localTopics = dao.exportAllTopics()
        val remoteToLocalTopicId = mutableMapOf<Int, Int>()
        remoteBackup.topics?.forEach { rTopic ->
            val localSubId = remoteToLocalSubjectId[rTopic.subjectId] ?: rTopic.subjectId
            val localChapId = rTopic.chapterId?.let { remoteToLocalChapterId[it] }
            val existing = localTopics.find { it.subjectId == localSubId && it.title.equals(rTopic.title.trim(), ignoreCase = true) }
            if (existing != null) {
                remoteToLocalTopicId[rTopic.id] = existing.id
                if (!existing.isCompleted && rTopic.isCompleted) {
                    dao.updateTopic(existing.copy(isCompleted = true))
                }
            } else {
                val newId = dao.insertTopic(rTopic.copy(id = 0, subjectId = localSubId, chapterId = localChapId)).toInt()
                remoteToLocalTopicId[rTopic.id] = newId
            }
        }

        // 5. Merge Assignments
        val localAssignments = dao.exportAllAssignments()
        remoteBackup.assignments?.forEach { rAssign ->
            val localCourseId = remoteToLocalCourseId[rAssign.courseId] ?: rAssign.courseId
            val localSubId = rAssign.subjectId?.let { remoteToLocalSubjectId[it] }
            val existing = localAssignments.find { it.courseId == localCourseId && it.title.equals(rAssign.title.trim(), ignoreCase = true) }
            if (existing != null) {
                if (!existing.isCompleted && rAssign.isCompleted) {
                    dao.updateAssignment(existing.copy(isCompleted = true))
                    assignmentsMerged++
                }
            } else {
                dao.insertAssignment(rAssign.copy(id = 0, courseId = localCourseId, subjectId = localSubId))
                assignmentsMerged++
            }
        }

        // 6. Merge Tasks
        val localTasks = dao.exportAllTasks()
        remoteBackup.tasks?.forEach { rTask ->
            val localCourseId = rTask.courseId?.let { remoteToLocalCourseId[it] }
            val localSubId = rTask.subjectId?.let { remoteToLocalSubjectId[it] }
            val localChapId = rTask.chapterId?.let { remoteToLocalChapterId[it] }
            val localTopicId = rTask.topicId?.let { remoteToLocalTopicId[it] }

            val existing = localTasks.find { 
                it.title.equals(rTask.title.trim(), ignoreCase = true) && 
                (it.dueDateMillis == rTask.dueDateMillis || it.courseId == localCourseId)
            }

            if (existing != null) {
                val completed = existing.isCompleted || rTask.isCompleted
                val updated = existing.copy(
                    isCompleted = completed,
                    priority = maxOf(existing.priority, rTask.priority),
                    description = if (existing.description.isBlank()) rTask.description else existing.description
                )
                if (updated != existing) {
                    dao.updateTask(updated)
                    tasksMerged++
                }
            } else {
                dao.insertTask(
                    rTask.copy(
                        id = 0,
                        courseId = localCourseId,
                        subjectId = localSubId,
                        chapterId = localChapId,
                        topicId = localTopicId
                    )
                )
                tasksMerged++
            }
        }

        // 7. Merge Notes
        val localNotes = dao.exportAllNotes()
        remoteBackup.notes?.forEach { rNote ->
            val localCourseId = rNote.courseId?.let { remoteToLocalCourseId[it] }
            val localSubId = rNote.subjectId?.let { remoteToLocalSubjectId[it] }
            val existing = localNotes.find { 
                it.content.trim() == rNote.content.trim() && 
                Math.abs(it.dateMillis - rNote.dateMillis) < 60000 
            }
            if (existing == null) {
                dao.insertNote(rNote.copy(id = 0, courseId = localCourseId, subjectId = localSubId))
                notesMerged++
            }
        }

        // 8. Merge Pomodoro Focus Sessions
        val localPomodoro = dao.exportAllPomodoro()
        remoteBackup.pomodoro?.forEach { rPomo ->
            val localCourseId = rPomo.courseId?.let { remoteToLocalCourseId[it] }
            val localSubId = rPomo.subjectId?.let { remoteToLocalSubjectId[it] }
            val existing = localPomodoro.find { 
                it.dateMillis == rPomo.dateMillis && it.durationMinutes == rPomo.durationMinutes 
            }
            if (existing == null) {
                dao.insertPomodoroSession(rPomo.copy(id = 0, courseId = localCourseId, subjectId = localSubId))
                pomodoroMerged++
            }
        }

        // 9. Merge Attendance Records
        val localAttendance = dao.exportAllAttendance()
        remoteBackup.attendance?.forEach { rAtt ->
            val localCourseId = remoteToLocalCourseId[rAtt.courseId] ?: rAtt.courseId
            val existing = localAttendance.find { 
                it.courseId == localCourseId && it.dateMillis == rAtt.dateMillis 
            }
            if (existing == null) {
                dao.insertAttendanceRecord(rAtt.copy(id = 0, courseId = localCourseId))
            }
        }

        // 10. Merge Tag Customizations
        val localTags = dao.exportAllTagCustomizations()
        remoteBackup.tagCustomizations?.forEach { rTag ->
            val existing = localTags.find { it.tagName.equals(rTag.tagName.trim(), ignoreCase = true) }
            if (existing == null) {
                dao.insertTagCustomization(rTag)
                tagCustomizationsMerged++
            }
        }

        SyncMergeReport(
            coursesMerged = coursesMerged,
            subjectsMerged = subjectsMerged,
            tasksMerged = tasksMerged,
            assignmentsMerged = assignmentsMerged,
            notesMerged = notesMerged,
            pomodoroSessionsMerged = pomodoroMerged,
            tagCustomizationsMerged = tagCustomizationsMerged
        )
    }
}
