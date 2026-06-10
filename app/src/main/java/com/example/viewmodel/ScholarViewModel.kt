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

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "Default") ?: "Default")
    val themeColor = _themeColor.asStateFlow()

    private val _currentStreak = MutableStateFlow(prefs.getInt("current_streak", 0))
    val currentStreak = _currentStreak.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScholarRepository(database.scholarDao())
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

    val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getTopicsForSubject(subjectId: Int): StateFlow<List<Topic>> {
        return repository.getTopicsForSubject(subjectId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun getAssignmentsForCourse(courseId: Int): StateFlow<List<PracticeAssignment>> {
        return repository.getAssignmentsForCourse(courseId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addCourse(title: String, description: String) {
        viewModelScope.launch {
            repository.insertCourse(Course(title = title, description = description))
            logAction("Added course: $title")
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            logAction("Deleted course: ${course.title}")
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            logAction("Updated course: ${course.title}")
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
            repository.insertAssignment(PracticeAssignment(courseId = courseId, title = title, description = desc, dueDateMillis = dueDate))
            // Schedule the reminder using the application context
            val context = getApplication<Application>().applicationContext
            // For simplicity in this demo, hashcode is used as an ID. In production use the inserted assigned ID.
            com.example.util.ReminderScheduler.scheduleReminder(context, (courseId + title.hashCode()), title, desc, dueDate)
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

    // Export/Import
    private val _importExportStatus = MutableStateFlow<String?>(null)
    val importExportStatus = _importExportStatus.asStateFlow()

    private fun logAction(action: String) {
        viewModelScope.launch {
            repository.insertActionLog(ActionLog(actionText = action))
        }
    }

    private fun checkAndUpdateStreak() {
        val lastActionDate = prefs.getLong("last_action_date", 0L)
        val now = System.currentTimeMillis()
        
        val msPerDay = 1000 * 60 * 60 * 24L
        val offsetNow = java.util.TimeZone.getDefault().getOffset(now)
        val dayNow = (now + offsetNow) / msPerDay
        
        val dayLast = if (lastActionDate == 0L) 0 else (lastActionDate + java.util.TimeZone.getDefault().getOffset(lastActionDate)) / msPerDay

        if (dayNow == dayLast + 1L) {
            updateStreak(_currentStreak.value + 1)
        } else if (dayNow > dayLast + 1L || lastActionDate == 0L) {
            updateStreak(1)
        }
        
        prefs.edit().putLong("last_action_date", now).apply()
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
        getApplication<Application>().getSharedPreferences("tard_prefs", Context.MODE_PRIVATE)
            .edit().putString("theme_mode", mode).apply()
    }

    fun updateThemeColor(color: String) {
        _themeColor.value = color
        getApplication<Application>().getSharedPreferences("tard_prefs", Context.MODE_PRIVATE)
            .edit().putString("theme_color", color).apply()
    }
}
