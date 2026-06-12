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
    
    private val prefs = application.getSharedPreferences("lumia_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    val themeMode = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "Ocean") ?: "Ocean")
    val themeColor = _themeColor.asStateFlow()

    private val _customPrimary = MutableStateFlow(prefs.getString("custom_primary", "#3197D6") ?: "#3197D6")
    val customPrimary = _customPrimary.asStateFlow()

    private val _customPrimaryContainer = MutableStateFlow(prefs.getString("custom_primary_container", "#DAF1FF") ?: "#DAF1FF")
    val customPrimaryContainer = _customPrimaryContainer.asStateFlow()

    private val _customBackground = MutableStateFlow(prefs.getString("custom_background", "#FAFAFA") ?: "#FAFAFA")
    val customBackground = _customBackground.asStateFlow()

    private val _customSurface = MutableStateFlow(prefs.getString("custom_surface", "#FFFFFF") ?: "#FFFFFF")
    val customSurface = _customSurface.asStateFlow()

    private val _customText = MutableStateFlow(prefs.getString("custom_text", "#1A1C1A") ?: "#1A1C1A")
    val customText = _customText.asStateFlow()

    fun updateCustomColor(key: String, hex: String) {
        when(key) {
           "primary" -> _customPrimary.value = hex
           "primary_container" -> _customPrimaryContainer.value = hex
           "background" -> _customBackground.value = hex
           "surface" -> _customSurface.value = hex
           "text" -> _customText.value = hex
        }
        prefs.edit().putString("custom_$key", hex).apply()
    }

    fun generatePaletteFromPrimaryHex(hex: String) {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        try {
            val colorInt = android.graphics.Color.parseColor(cleanHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(colorInt, hsv) // Hue: 0-360, Sat: 0-1, Val: 0-1
            
            // 1. Primary is already set
            updateCustomColor("primary", cleanHex)
            
            // 2. Generate PrimaryContainer (high light, medium-low saturation)
            val pcHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.35f), 0.94f)
            val pcColor = android.graphics.Color.HSVToColor(pcHsv)
            val pcHex = String.format("#%06X", 0xFFFFFF and pcColor)
            updateCustomColor("primary_container", pcHex)
            
            // 3. Generate Ambient background (extremely low saturation, high brightness)
            val bgHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.05f), 0.98f)
            val bgColor = android.graphics.Color.HSVToColor(bgHsv)
            val bgHex = String.format("#%06X", 0xFFFFFF and bgColor)
            updateCustomColor("background", bgHex)
            
            // 4. Generate Surface (almost pure white, extremely low saturation)
            val sfHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.03f), 1.00f)
            val sfColor = android.graphics.Color.HSVToColor(sfHsv)
            val sfHex = String.format("#%06X", 0xFFFFFF and sfColor)
            updateCustomColor("surface", sfHex)
            
            // 5. Generate Text (deep brand color, high saturation weight, extremely dark value)
            val txtHsv = floatArrayOf(hsv[0], Math.min(1.0f, hsv[1] * 0.30f), 0.12f)
            val txtColor = android.graphics.Color.HSVToColor(txtHsv)
            val txtHex = String.format("#%06X", 0xFFFFFF and txtColor)
            updateCustomColor("text", txtHex)

            // Auto-select "Custom" theme color
            updateThemeColor("Custom")
            
        } catch(e: Exception) {
            // Safe fallback so formatting typos while editing the input field do not cause crashes
        }
    }

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

    private val _betaGlassDynamic = MutableStateFlow(prefs.getBoolean("beta_glass_dynamic", true))
    val betaGlassDynamic = _betaGlassDynamic.asStateFlow()

    private val _betaFrostGlass = MutableStateFlow(prefs.getBoolean("beta_frost_glass", true))
    val betaFrostGlass = _betaFrostGlass.asStateFlow()

    private val _betaEnhancedHeader = MutableStateFlow(prefs.getBoolean("beta_enhanced_header", false))
    val betaEnhancedHeader = _betaEnhancedHeader.asStateFlow()

    private val _betaMinimalistMode = MutableStateFlow(prefs.getBoolean("beta_minimalist_mode", false))
    val betaMinimalistMode = _betaMinimalistMode.asStateFlow()

    private val _betaDynamicBackground = MutableStateFlow(prefs.getBoolean("beta_dynamic_background", false))
    val betaDynamicBackground = _betaDynamicBackground.asStateFlow()

    private val _dynamicAppIcon = MutableStateFlow(prefs.getBoolean("dynamic_app_icon", false))
    val dynamicAppIcon = _dynamicAppIcon.asStateFlow()

    fun updateDynamicAppIcon(enabled: Boolean) {
        _dynamicAppIcon.value = enabled
        prefs.edit().putBoolean("dynamic_app_icon", enabled).apply()
        
        val pm = getApplication<Application>().packageManager
        val packageName = getApplication<Application>().packageName
        val defaultAlias = android.content.ComponentName(packageName, "com.example.DefaultAlias")
        val dynamicAlias = android.content.ComponentName(packageName, "com.example.DynamicAlias")

        try {
            if (enabled) {
                pm.setComponentEnabledSetting(dynamicAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(defaultAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
            } else {
                pm.setComponentEnabledSetting(defaultAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(dynamicAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            android.util.Log.e("ScholarViewModel", "Exception toggling dynamic app icon aliases. Restoring defaults.", e)
            try {
                pm.setComponentEnabledSetting(defaultAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(dynamicAlias, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
            } catch (ex: Exception) {}
        }
        android.widget.Toast.makeText(getApplication(), "Icon changing... Launcher may take a moment to reflect changes or might require a home screen refresh.", android.widget.Toast.LENGTH_LONG).show()
    }

    private val _betaBetterTexts = MutableStateFlow(prefs.getBoolean("beta_better_texts", false))
    val betaBetterTexts = _betaBetterTexts.asStateFlow()

    private val _betaBetterTextsPalette = MutableStateFlow(prefs.getBoolean("beta_better_texts_palette", true))
    val betaBetterTextsPalette = _betaBetterTextsPalette.asStateFlow()

    private val _safetyPinEnabled = MutableStateFlow(prefs.getBoolean("safety_pin_enabled", true))
    val safetyPinEnabled = _safetyPinEnabled.asStateFlow()

    private val _safetyPinConflictWarning = MutableStateFlow(prefs.getBoolean("safety_pin_conflict_warning", true))
    val safetyPinConflictWarning = _safetyPinConflictWarning.asStateFlow()

    private val _safetyPinRecommendations = MutableStateFlow(prefs.getBoolean("safety_pin_recommendations", true))
    val safetyPinRecommendations = _safetyPinRecommendations.asStateFlow()

    data class SafetyPinDialogData(
        val title: String,
        val description: String,
        val isConflict: Boolean,
        val onConfirm: () -> Unit,
        val onIgnore: () -> Unit
    )

    private val _safetyPinDialogData = run {
        val delegate = MutableStateFlow<SafetyPinDialogData?>(null)
        object : MutableStateFlow<SafetyPinDialogData?> by delegate {
            override var value: SafetyPinDialogData?
                get() = delegate.value
                set(v) {
                    if (v != null && delegate.value != null) return
                    delegate.value = v
                }
            override fun compareAndSet(expect: SafetyPinDialogData?, update: SafetyPinDialogData?): Boolean {
                if (update != null && delegate.value != null) return false
                return delegate.compareAndSet(expect, update)
            }
            override fun tryEmit(value: SafetyPinDialogData?): Boolean {
                if (value != null && delegate.value != null) return false
                return delegate.tryEmit(value)
            }
            override suspend fun emit(value: SafetyPinDialogData?) {
                if (value != null && delegate.value != null) return
                delegate.emit(value)
            }
        }
    }
    val safetyPinDialogData = _safetyPinDialogData.asStateFlow()

    fun dismissSafetyPinDialog() {
        _safetyPinDialogData.value = null
    }

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

    private val assignmentsFlowCache = HashMap<Int, StateFlow<List<PracticeAssignment>>>()

    fun getAssignmentsForCourse(courseId: Int): StateFlow<List<PracticeAssignment>> {
        return assignmentsFlowCache.getOrPut(courseId) {
            repository.getAssignmentsForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
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

    fun addAssignment(courseId: Int, title: String, desc: String, dueDate: Long, category: String = "Homework", categoryColor: String = "#3197D6") {
        viewModelScope.launch {
            val newId = repository.insertAssignment(PracticeAssignment(courseId = courseId, title = title, description = desc, dueDateMillis = dueDate, category = category, categoryColor = categoryColor)).toInt()
            val context = getApplication<Application>().applicationContext
            com.example.util.ReminderScheduler.scheduleReminder(context, newId, title, desc, dueDate)
            logAction("Added assignment: $title ($category)")
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

    private fun gatherSettings(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            map[key] = value.toString()
        }
        return map
    }

    private fun loadSettings(settings: Map<String, String>?) {
        if (settings == null) return
        val editor = prefs.edit()
        settings.forEach { (key, value) ->
            when (key) {
                "theme_mode" -> { editor.putString(key, value); _themeMode.value = value }
                "theme_color" -> { editor.putString(key, value); _themeColor.value = value }
                "custom_primary", "custom_primary_container", "custom_background", "custom_surface", "custom_text" -> {
                    editor.putString(key, value)
                    when (key) {
                        "custom_primary" -> _customPrimary.value = value
                        "custom_primary_container" -> _customPrimaryContainer.value = value
                        "custom_background" -> _customBackground.value = value
                        "custom_surface" -> _customSurface.value = value
                        "custom_text" -> _customText.value = value
                    }
                }
                "last_action_date_str" -> {
                    editor.putString(key, value)
                }
                "current_streak" -> {
                    val intVal = value.toIntOrNull() ?: 0
                    editor.putInt(key, intVal)
                    _currentStreak.value = intVal
                }
                else -> {
                    val boolVal = value.toBooleanStrictOrNull() ?: return@forEach
                    editor.putBoolean(key, boolVal)
                    when(key) {
                        "pure_black_mode" -> _pureBlackMode.value = boolVal
                        "beta_floating_nav" -> _betaFloatingNav.value = boolVal
                        "beta_pomodoro" -> _betaPomodoro.value = boolVal
                        "beta_cgpa" -> _betaCgpa.value = boolVal
                        "beta_notes" -> _betaNotes.value = boolVal
                        "beta_motivation" -> _betaMotivation.value = boolVal
                        "beta_immersive_mode" -> _betaImmersiveMode.value = boolVal
                        "beta_notch_optimization" -> _betaNotchOptimization.value = boolVal
                        "beta_glass_ui" -> _betaGlassUi.value = boolVal
                        "beta_glass_dynamic" -> _betaGlassDynamic.value = boolVal
                        "beta_frost_glass" -> _betaFrostGlass.value = boolVal
                        "beta_enhanced_header" -> _betaEnhancedHeader.value = boolVal
                        "beta_minimalist_mode" -> _betaMinimalistMode.value = boolVal
                        "beta_dynamic_background" -> _betaDynamicBackground.value = boolVal
                        "dynamic_app_icon" -> _dynamicAppIcon.value = boolVal
                        "beta_better_texts" -> _betaBetterTexts.value = boolVal
                        "beta_better_texts_palette" -> _betaBetterTextsPalette.value = boolVal
                        "safety_pin_enabled" -> _safetyPinEnabled.value = boolVal
                        "safety_pin_conflict_warning" -> _safetyPinConflictWarning.value = boolVal
                        "safety_pin_recommendations" -> _safetyPinRecommendations.value = boolVal
                        "show_action_history" -> _showActionHistory.value = boolVal
                    }
                }
            }
        }
        editor.apply()
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settings = gatherSettings()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                    repository.exportDataToStream(os, settings)
                }
                _importExportStatus.value = "Data exported successfully (JSON format)"
            } catch (e: Exception) {
                _importExportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var settings: Map<String, String>? = null
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { ins ->
                    settings = repository.importDataFromStream(ins)
                }
                loadSettings(settings)
                _importExportStatus.value = "Data imported successfully"
            } catch (e: Exception) {
                _importExportStatus.value = "Import failed: Invalid file or wrong format"
            }
        }
    }

    fun clearStatus() {
        _importExportStatus.value = null
    }

    fun updateSafetyPinEnabled(enabled: Boolean) {
        _safetyPinEnabled.value = enabled
        prefs.edit().putBoolean("safety_pin_enabled", enabled).apply()
    }

    fun updateSafetyPinConflictWarning(enabled: Boolean) {
        _safetyPinConflictWarning.value = enabled
        prefs.edit().putBoolean("safety_pin_conflict_warning", enabled).apply()
    }

    fun updateSafetyPinRecommendations(enabled: Boolean) {
        _safetyPinRecommendations.value = enabled
        prefs.edit().putBoolean("safety_pin_recommendations", enabled).apply()
    }

    fun updateThemeMode(mode: String) {
        if (safetyPinEnabled.value && safetyPinConflictWarning.value && mode == "Light" && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "Switching to 'Light' theme conflicts with 'Pure Black Mode', which requires a dark theme to function. Proceeding will automatically disable 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (mode == "Dark" && safetyPinEnabled.value && safetyPinRecommendations.value && !_pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the deepest contrast and battery savings on OLED screens, it is recommended to enable 'Pure Black Mode' with the Dark theme. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                    updatePureBlackMode(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _themeMode.value = mode
                    prefs.edit().putString("theme_mode", mode).apply()
                }
            )
            return
        }

        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun updatePureBlackMode(enabled: Boolean) {
        val conflictsWithDynamicBg = _betaDynamicBackground.value
        val conflictsWithGlassUi = _betaGlassUi.value
        val conflictsWithPalette = _betaBetterTextsPalette.value
        val conflictsWithEnhancedHeader = _betaEnhancedHeader.value

        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (conflictsWithDynamicBg || conflictsWithGlassUi || conflictsWithPalette || conflictsWithEnhancedHeader)) {
            val opposingFeatures = mutableListOf<String>()
            if (conflictsWithDynamicBg) opposingFeatures.add("'Dynamic Lighting Background'")
            if (conflictsWithGlassUi) opposingFeatures.add("'Glass UI'")
            if (conflictsWithPalette) opposingFeatures.add("'Use Palette Shades for Text'")
            if (conflictsWithEnhancedHeader) opposingFeatures.add("'Enhanced Header'")
            
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Pure Black Mode' directly opposes the functionality of ${opposingFeatures.joinToString(" and ")}. Proceeding will automatically deactivate these opposing settings to maintain visual consistency and readability.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                    if (conflictsWithDynamicBg) updateBetaDynamicBackground(false)
                    if (conflictsWithGlassUi) updateBetaGlassUi(false)
                    if (conflictsWithPalette) updateBetaBetterTextsPalette(false)
                    if (conflictsWithEnhancedHeader) updateBetaEnhancedHeader(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && _themeMode.value != "Dark") {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the optimal experience of 'Pure Black Mode', it is highly recommended to switch your system theme to 'Dark'. The current setting limits the effectiveness of the pure black backgrounds.",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                    updateThemeMode("Dark")
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _pureBlackMode.value = true
                    prefs.edit().putBoolean("pure_black_mode", true).apply()
                }
            )
            return
        }
        _pureBlackMode.value = enabled
        prefs.edit().putBoolean("pure_black_mode", enabled).apply()
    }

    fun updateThemeColor(color: String) {
        _themeColor.value = color
        prefs.edit().putString("theme_color", color).apply()
    }

    fun updateBetaFloatingNav(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaImmersiveMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the best visual experience of 'Floating Action Bar', it is highly recommended to activate 'Full Screen Punch Hole (Immersive)'. This allows the bar to float beautifully over the background without being enclosed by the system navigation area. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaFloatingNav.value = true
                    prefs.edit().putBoolean("beta_floating_nav", true).apply()
                    updateBetaImmersiveMode(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _betaFloatingNav.value = true
                    prefs.edit().putBoolean("beta_floating_nav", true).apply()
                 }
            )
            return
        }
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
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaFloatingNav.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For optimal ergonomics in Immersive Mode, it is highly recommended to activate 'Floating Action Bar', as standard bottom bars may interfere with the system gesture navigation area at the bottom. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaImmersiveMode.value = true
                    prefs.edit().putBoolean("beta_immersive_mode", true).apply()
        
                    _betaNotchOptimization.value = false
                    prefs.edit().putBoolean("beta_notch_optimization", false).apply()
                    updateBetaFloatingNav(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _betaImmersiveMode.value = true
                    prefs.edit().putBoolean("beta_immersive_mode", true).apply()
        
                    _betaNotchOptimization.value = false
                    prefs.edit().putBoolean("beta_notch_optimization", false).apply()
                }
            )
            return
        }

        _betaImmersiveMode.value = enabled
        prefs.edit().putBoolean("beta_immersive_mode", enabled).apply()
        
        _betaNotchOptimization.value = !enabled
        prefs.edit().putBoolean("beta_notch_optimization", !enabled).apply()
    }

    fun updateBetaMinimalistMode(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (_betaGlassUi.value || _betaDynamicBackground.value || _betaEnhancedHeader.value || _betaFloatingNav.value || _betaBetterTexts.value || !_betaImmersiveMode.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "Activating 'Minimalist Mode' will force-disable 'Glass UI', 'Dynamic Lighting', 'Enhanced Header', 'Floating Action Bar', and 'Better Texts', locking them to reduce visual clutter. Additionally, 'Immersive Mode' will be turned ON. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaMinimalistMode.value = true
                    prefs.edit().putBoolean("beta_minimalist_mode", true).apply()
                    if (_betaGlassUi.value) updateBetaGlassUi(false)
                    if (_betaDynamicBackground.value) updateBetaDynamicBackground(false)
                    if (_betaEnhancedHeader.value) updateBetaEnhancedHeader(false)
                    if (_betaFloatingNav.value) updateBetaFloatingNav(false)
                    if (_betaBetterTexts.value) updateBetaBetterTexts(false)
                    if (!_betaImmersiveMode.value) updateBetaImmersiveMode(true)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        _betaMinimalistMode.value = enabled
        prefs.edit().putBoolean("beta_minimalist_mode", enabled).apply()
        
        if (enabled && !_safetyPinEnabled.value) {
            if (_betaGlassUi.value) updateBetaGlassUi(false)
            if (_betaDynamicBackground.value) updateBetaDynamicBackground(false)
            if (_betaEnhancedHeader.value) updateBetaEnhancedHeader(false)
            if (_betaFloatingNav.value) updateBetaFloatingNav(false)
            if (_betaBetterTexts.value) updateBetaBetterTexts(false)
            if (!_betaImmersiveMode.value) updateBetaImmersiveMode(true)
        }
    }

    fun updateBetaGlassUi(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Glass UI' directly opposes the functionality of 'Pure Black Mode'. Glass UI requires background colors to create frosted translucency. Proceeding will automatically deactivate 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }

        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && (!_betaDynamicBackground.value || !_betaFloatingNav.value || !_betaBetterTexts.value || !_betaEnhancedHeader.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For an enhanced visual experience, it is highly recommended to activate 'Dynamic Lighting Background', 'Floating Action Bar', 'Better Texts', and 'Enhanced Header' alongside 'Glass UI'. Would you like to apply these complementary settings?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                    updateBetaDynamicBackground(true)
                    updateBetaFloatingNav(true)
                    updateBetaBetterTexts(true)
                    updateBetaEnhancedHeader(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaGlassUi.value = true
                    prefs.edit().putBoolean("beta_glass_ui", true).apply()
                }
            )
            return
        }
        _betaGlassUi.value = enabled
        prefs.edit().putBoolean("beta_glass_ui", enabled).apply()
    }

    fun updateBetaGlassDynamic(enabled: Boolean) {
        _betaGlassDynamic.value = enabled
        prefs.edit().putBoolean("beta_glass_dynamic", enabled).apply()
    }

    fun updateBetaFrostGlass(enabled: Boolean) {
        _betaFrostGlass.value = enabled
        prefs.edit().putBoolean("beta_frost_glass", enabled).apply()
    }

    fun updateBetaEnhancedHeader(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Enhanced Header' directly opposes the functionality of 'Pure Black Mode'. Enhanced Header requires background colors to create frosted translucency. Proceeding will automatically deactivate 'Pure Black Mode'.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaGlassUi.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the best visual fidelity when using 'Enhanced Header', it is highly recommended to activate 'Glass UI'. This combination creates a stunning translucent effect. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                    updateBetaGlassUi(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaEnhancedHeader.value = true
                    prefs.edit().putBoolean("beta_enhanced_header", true).apply()
                }
            )
            return
        }

        _betaEnhancedHeader.value = enabled
        prefs.edit().putBoolean("beta_enhanced_header", enabled).apply()
    }

    fun updateBetaDynamicBackground(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Dynamic Lighting Background' contradicts the core purpose of 'Pure Black Mode' by introducing lit pixels and gradients. Proceeding will automatically deactivate 'Pure Black Mode' to maintain visual consistency.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaGlassUi.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "For the best visual fidelity when using 'Dynamic Lighting Background', it is highly recommended to activate 'Glass UI'. This combination creates a stunning translucent depth effect. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                    updateBetaGlassUi(true)
                },
                onIgnore = { 
                    _safetyPinDialogData.value = null
                    _betaDynamicBackground.value = true
                    prefs.edit().putBoolean("beta_dynamic_background", true).apply()
                }
            )
            return
        }

        _betaDynamicBackground.value = enabled
        prefs.edit().putBoolean("beta_dynamic_background", enabled).apply()
    }

    fun updateBetaBetterTexts(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinRecommendations.value && !_betaBetterTextsPalette.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Optimization Recommendation",
                description = "To fully experience 'Better Texts', it is recommended to also enable 'Use Palette Shades for Text'. This provides a softer, more cohesive look matching your selected theme color. Would you like to enable it?",
                isConflict = false,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaBetterTexts.value = true
                    prefs.edit().putBoolean("beta_better_texts", true).apply()
                    updateBetaBetterTextsPalette(true)
                },
                onIgnore = {
                    _safetyPinDialogData.value = null
                    _betaBetterTexts.value = true
                    prefs.edit().putBoolean("beta_better_texts", true).apply()
                 }
            )
            return
        }
        _betaBetterTexts.value = enabled
        prefs.edit().putBoolean("beta_better_texts", enabled).apply()
    }

    fun updateBetaBetterTextsPalette(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && _pureBlackMode.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "The activation of 'Use Palette Shades for Text' directly opposes the high contrast functionality required by 'Pure Black Mode'. Proceeding will automatically deactivate 'Pure Black Mode' to maintain text readability.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _betaBetterTextsPalette.value = true
                    prefs.edit().putBoolean("beta_better_texts_palette", true).apply()
                    updatePureBlackMode(false)
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _betaBetterTextsPalette.value = enabled
        prefs.edit().putBoolean("beta_better_texts_palette", enabled).apply()
    }

    fun updateShowActionHistory(enabled: Boolean) {
        _showActionHistory.value = enabled
        prefs.edit().putBoolean("show_action_history", enabled).apply()
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            repository.clearActionLogs()

            prefs.edit().clear().apply()

            _themeMode.value = "System"
            _themeColor.value = "Ocean"
            _customPrimary.value = "#3197D6"
            _customPrimaryContainer.value = "#DAF1FF"
            _customBackground.value = "#FAFAFA"
            _customSurface.value = "#FFFFFF"
            _customText.value = "#1A1C1A"
            _pureBlackMode.value = false
            _betaFloatingNav.value = false
            _betaPomodoro.value = false
            _betaCgpa.value = false
            _betaNotes.value = false
            _betaMotivation.value = false
            _betaImmersiveMode.value = false
            _betaNotchOptimization.value = false
            _betaGlassUi.value = false
            _betaGlassDynamic.value = true
            _betaFrostGlass.value = true
            _betaEnhancedHeader.value = false
            _betaMinimalistMode.value = false
            _betaDynamicBackground.value = false
            _dynamicAppIcon.value = false
            _betaBetterTexts.value = false
            _betaBetterTextsPalette.value = true
            _safetyPinEnabled.value = true
            _safetyPinConflictWarning.value = true
            _safetyPinRecommendations.value = true
            _showActionHistory.value = true
            _currentStreak.value = 0

            repository.insertActionLog(ActionLog(actionText = "Cleared all application data and settings"))
            _importExportStatus.value = "All data and settings erased successfully"
        }
    }
}
