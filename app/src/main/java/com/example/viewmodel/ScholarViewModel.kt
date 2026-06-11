package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScholarRepository
import com.example.model.Course
import com.example.model.PracticeAssignment
import com.example.model.Subject
import com.example.model.Topic
import com.example.model.ActionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ScholarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScholarRepository
    
    private val prefs = application.getSharedPreferences("tard_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    val themeMode = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "Blue") ?: "Blue")
    val themeColor = _themeColor.asStateFlow()

    private val _pureBlackMode = MutableStateFlow(prefs.getBoolean("pure_black_mode", false))
    val pureBlackMode = _pureBlackMode.asStateFlow()

    private val _betaFloatingNav = MutableStateFlow(prefs.getBoolean("beta_floating_nav", false))
    val betaFloatingNav = _betaFloatingNav.asStateFlow()

    private val _betaPomodoro = MutableStateFlow(prefs.getBoolean("beta_pomodoro", false))
    val betaPomodoro = _betaPomodoro.asStateFlow()

    private val _betaCgpa = MutableStateFlow(prefs.getBoolean("beta_cgpa", false))
    val betaCgpa = _betaCgpa.asStateFlow()

    private val _betaNotes = MutableStateFlow(prefs.getBoolean("beta_notes", false))
    val betaNotes = _betaNotes.asStateFlow()

    private val _betaMotivation = MutableStateFlow(prefs.getBoolean("beta_motivation", false))
    val betaMotivation = _betaMotivation.asStateFlow()

    private val _betaImmersiveMode = MutableStateFlow(prefs.getBoolean("beta_immersive_mode", false))
    val betaImmersiveMode = _betaImmersiveMode.asStateFlow()

    private val _betaNotchOptimization = MutableStateFlow(prefs.getBoolean("beta_notch_optimization", false))
    val betaNotchOptimization = _betaNotchOptimization.asStateFlow()

    private val _betaGlassUi = MutableStateFlow(prefs.getBoolean("beta_glass_ui", false))
    val betaGlassUi = _betaGlassUi.asStateFlow()

    private val _betaDynamicBackground = MutableStateFlow(prefs.getBoolean("beta_dynamic_background", false))
    val betaDynamicBackground = _betaDynamicBackground.asStateFlow()

    private val _showActionHistory = MutableStateFlow(prefs.getBoolean("show_action_history", true))
    val showActionHistory = _showActionHistory.asStateFlow()

    private val _currentStreak = MutableStateFlow(prefs.getInt("current_streak", 0))
    val currentStreak = _currentStreak.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScholarRepository(database.scholarDao())
        
        val lastActionDate = prefs.getString("last_action_date_str", "") ?: ""
        if (lastActionDate.isNotEmpty()) {
            val today = todayDateString()
            if (lastActionDate != today) {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                try {
                    val lastDate = sdf.parse(lastActionDate)
                    if (lastDate != null) {
                        val diff = (sdf.parse(today)!!.time - lastDate.time) / 86400000L
                        if (diff > 1L) {
                            updateStreak(0)
                        }
                    }
                } catch(e: Exception) {}
            }
        }
    }

    val courses: StateFlow<List<Course>> = repository.allCourses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val assignments: StateFlow<List<PracticeAssignment>> = repository.allAssignments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val actionLogs: StateFlow<List<ActionLog>> = repository.allActionLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pomodoroSessions: StateFlow<List<com.example.model.PomodoroSession>> = repository.allPomodoroSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val topicFlowCache = HashMap<Int, StateFlow<List<Topic>>>()
    
    fun getTopicsForSubject(subjectId: Int): StateFlow<List<Topic>> {
        return topicFlowCache.getOrPut(subjectId) {
            repository.getTopicsForSubject(subjectId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun getAssignmentsForCourse(courseId: Int): StateFlow<List<PracticeAssignment>> {
        return repository.getAssignmentsForCourse(courseId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val attendanceFlowCache = HashMap<Int, StateFlow<List<com.example.model.AttendanceRecord>>>()

    fun getAttendanceForCourse(courseId: Int): StateFlow<List<com.example.model.AttendanceRecord>> {
        return attendanceFlowCache.getOrPut(courseId) {
            repository.getAttendanceForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun addAttendanceRecord(courseId: Int, dateMillis: Long, status: String) {
        viewModelScope.launch {
            val normalized = java.util.Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            repository.insertAttendanceRecord(com.example.model.AttendanceRecord(courseId = courseId, dateMillis = normalized, status = status))
        }
    }

    fun addPomodoroSession(durationMinutes: Int) {
        viewModelScope.launch {
            repository.insertPomodoroSession(com.example.model.PomodoroSession(dateMillis = System.currentTimeMillis(), durationMinutes = durationMinutes))
            logAction("Completed Pomodoro Session ($durationMinutes min)")
        }
    }

    fun updateAttendanceRecord(record: com.example.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(record)
        }
    }

    fun deleteAttendanceRecord(record: com.example.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
        }
    }

    fun addCourse(name: String, instructor: String, schedule: String, description: String) {
        viewModelScope.launch {
            repository.insertCourse(Course(name = name, instructor = instructor, schedule = schedule, description = description))
            logAction("Added course: $name")
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            logAction("Deleted course: ${course.name}")
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            logAction("Updated course: ${course.name}")
        }
    }

    fun addSubject(name: String, targetHours: Int) {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, targetHours = targetHours))
            logAction("Added subject: $name")
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            logAction("Deleted subject: ${subject.name}")
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            logAction("Updated subject: ${subject.name}")
        }
    }

    fun addTopic(subjectId: Int, title: String) {
        viewModelScope.launch {
            repository.insertTopic(Topic(subjectId = subjectId, title = title))
            logAction("Added topic: $title")
        }
    }

    fun toggleTopicCompleted(topic: Topic) {
        viewModelScope.launch {
            val newlyCompleted = !topic.isCompleted
            repository.updateTopic(topic.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed topic: ${topic.title}" else "Unmarked topic: ${topic.title}"
            logAction(actionText)
            if (newlyCompleted) checkAndUpdateStreak()
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            logAction("Deleted topic: ${topic.title}")
        }
    }

    fun addAssignment(courseId: Int, title: String, desc: String, dueDate: Long) {
        viewModelScope.launch {
            val newId = repository.insertAssignment(PracticeAssignment(courseId = courseId, title = title, description = desc, dueDateMillis = dueDate)).toInt()
            val context = getApplication<Application>().applicationContext
            com.example.util.ReminderScheduler.scheduleReminder(context, newId, title, desc, dueDate)
            logAction("Added assignment: $title")
        }
    }

    fun toggleAssignmentCompleted(assignment: PracticeAssignment) {
        viewModelScope.launch {
            val newlyCompleted = !assignment.isCompleted
            repository.updateAssignment(assignment.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed assignment: ${assignment.title}" else "Unmarked assignment: ${assignment.title}"
            logAction(actionText)
            if (newlyCompleted) checkAndUpdateStreak()
        }
    }

    fun deleteAssignment(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun updateAssignmentDetails(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
            logAction("Updated assignment: ${assignment.title}")
        }
    }

    // Export/Import
    private val _importExportStatus = MutableStateFlow<String?>(null)
    val importExportStatus = _importExportStatus.asStateFlow()

    private fun logAction(action: String) {
        viewModelScope.launch {
            repository.insertActionLog(ActionLog(actionText = action))
        }
    }

    private fun todayDateString(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

    private fun checkAndUpdateStreak() {
        val lastActionDate = prefs.getString("last_action_date_str", "") ?: ""
        val today = todayDateString()

        if (lastActionDate.isEmpty()) {
            updateStreak(1)
            prefs.edit().putString("last_action_date_str", today).apply()
            return
        }

        if (lastActionDate == today) return  // already counted today

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val lastDate = sdf.parse(lastActionDate) ?: return
        val diff = (sdf.parse(today)!!.time - lastDate.time) / 86400000L

        when {
            diff == 1L -> updateStreak(_currentStreak.value + 1)
            diff > 1L  -> updateStreak(1)  // streak broken
        }
        prefs.edit().putString("last_action_date_str", today).apply()
    }

    private fun updateStreak(streak: Int) {
        _currentStreak.value = streak
        prefs.edit().putInt("current_streak", streak).apply()
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                    repository.exportDataToStream(os)
                }
                _importExportStatus.value = "Data exported successfully (Binary format)"
            } catch (e: Exception) {
                _importExportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { ins ->
                    repository.importDataFromStream(ins)
                }
                _importExportStatus.value = "Data imported successfully"
            } catch (e: Exception) {
                _importExportStatus.value = "Import failed: Invalid file or wrong format"
            }
        }
    }

    fun clearStatus() {
        _importExportStatus.value = null
    }

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun updatePureBlackMode(enabled: Boolean) {
        _pureBlackMode.value = enabled
        prefs.edit().putBoolean("pure_black_mode", enabled).apply()
    }

    fun updateThemeColor(color: String) {
        _themeColor.value = color
        prefs.edit().putString("theme_color", color).apply()
    }

    fun updateBetaFloatingNav(enabled: Boolean) {
        _betaFloatingNav.value = enabled
        prefs.edit().putBoolean("beta_floating_nav", enabled).apply()
    }

    fun updateBetaPomodoro(enabled: Boolean) {
        _betaPomodoro.value = enabled
        prefs.edit().putBoolean("beta_pomodoro", enabled).apply()
    }

    fun updateBetaCgpa(enabled: Boolean) {
        _betaCgpa.value = enabled
        prefs.edit().putBoolean("beta_cgpa", enabled).apply()
    }

    fun updateBetaNotes(enabled: Boolean) {
        _betaNotes.value = enabled
        prefs.edit().putBoolean("beta_notes", enabled).apply()
    }

    fun updateBetaMotivation(enabled: Boolean) {
        _betaMotivation.value = enabled
        prefs.edit().putBoolean("beta_motivation", enabled).apply()
    }

    fun updateBetaImmersiveMode(enabled: Boolean) {
        _betaImmersiveMode.value = enabled
        prefs.edit().putBoolean("beta_immersive_mode", enabled).apply()
    }

    fun updateBetaNotchOptimization(enabled: Boolean) {
        _betaNotchOptimization.value = enabled
        prefs.edit().putBoolean("beta_notch_optimization", enabled).apply()
    }

    fun updateBetaGlassUi(enabled: Boolean) {
        _betaGlassUi.value = enabled
        prefs.edit().putBoolean("beta_glass_ui", enabled).apply()
    }

    fun updateBetaDynamicBackground(enabled: Boolean) {
        _betaDynamicBackground.value = enabled
        prefs.edit().putBoolean("beta_dynamic_background", enabled).apply()
    }

    fun updateShowActionHistory(enabled: Boolean) {
        _showActionHistory.value = enabled
        prefs.edit().putBoolean("show_action_history", enabled).apply()
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            repository.clearActionLogs()
            repository.insertActionLog(ActionLog(actionText = "Cleared all application data"))
        }
    }
}
