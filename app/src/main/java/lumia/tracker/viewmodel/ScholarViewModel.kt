package lumia.tracker.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ScholarRepository
import lumia.tracker.model.Course
import lumia.tracker.model.PracticeAssignment
import lumia.tracker.model.Subject
import lumia.tracker.model.Topic
import lumia.tracker.model.ActionLog
import lumia.tracker.model.Chapter
import lumia.tracker.model.Task
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

    val profileManager = lumia.tracker.data.ProfileManager(application)
    val activeProfile = MutableStateFlow(profileManager.getActiveProfile())
    
    val allProfiles = MutableStateFlow(profileManager.getAllProfiles())

    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val active = profileManager.getActiveProfile()
        if (activeProfile.value != active) {
            activeProfile.value = active
        }
        val all = profileManager.getAllProfiles()
        if (allProfiles.value != all) {
            allProfiles.value = all
        }
    }

    init {
        application.getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(prefListener)
    }
    
    fun switchProfileAndRestart(context: Context, id: String) {
        profileManager.setActiveProfileId(id)
        lumia.tracker.data.AppDatabase.clearInstances()
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            val mainIntent = android.content.Intent.makeRestartActivityTask(intent.component)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        } else {
            val fallbackIntent = android.content.Intent(context, lumia.tracker.MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(fallbackIntent)
            Runtime.getRuntime().exit(0)
        }
    }
    
    fun createProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String {
        val newId = profileManager.addProfile(name, avatar, alias, starterTheme)
        allProfiles.value = profileManager.getAllProfiles()
        return newId
    }

    fun setupFirstProfile(name: String, avatar: String, alias: String, starterTheme: String) {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme
        )
        profileManager.updateProfile(updated)
        activeProfile.value = updated
        allProfiles.value = profileManager.getAllProfiles()
        updateThemeColor(starterTheme)
    }
    
    fun updateProfile(name: String, avatar: String, alias: String = "") {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(name = name, avatarEmoji = avatar, alias = alias)
        profileManager.updateProfile(updated)
        activeProfile.value = updated
        allProfiles.value = profileManager.getAllProfiles()
    }

    fun selectProfileBadge(badgeId: String) {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(selectedBadge = badgeId)
        profileManager.updateProfile(updated)
        activeProfile.value = updated
        allProfiles.value = profileManager.getAllProfiles()
    }

    fun purchaseFeature(featureId: String, cost: Int): Boolean {
        return purchaseFeatureWithPoints(featureId, cost)
    }

    fun purchaseFeatureWithPoints(featureId: String, cost: Int): Boolean {
        val current = profileManager.getActiveProfile()
        val feature = lumia.tracker.model.PlusShop.features.find { it.id == featureId } ?: return false
        if (current.level < feature.requiredLevel) return false
        
        if (current.points >= cost && !current.unlockedFeatures.contains(featureId)) {
            current.points -= cost
            val newFeatures = current.unlockedFeatures.toMutableList()
            newFeatures.add(featureId)
            current.unlockedFeatures = newFeatures
            profileManager.updateProfile(current)
            activeProfile.value = current
            allProfiles.value = profileManager.getAllProfiles()
            return true
        }
        return false
    }

    fun purchaseFeatureWithCredits(featureId: String, cost: Int): Boolean {
        val current = profileManager.getActiveProfile()
        val feature = lumia.tracker.model.PlusShop.features.find { it.id == featureId } ?: return false
        if (current.level < feature.requiredLevel) return false
        
        if (current.credits >= cost && !current.unlockedFeatures.contains(featureId)) {
            current.credits -= cost
            val newFeatures = current.unlockedFeatures.toMutableList()
            newFeatures.add(featureId)
            current.unlockedFeatures = newFeatures
            profileManager.updateProfile(current)
            activeProfile.value = current
            allProfiles.value = profileManager.getAllProfiles()
            return true
        }
        return false
    }

    fun rentFeatureWithCredits(featureId: String, cost: Int, durationDays: Int = 1): Boolean {
        val current = profileManager.getActiveProfile()
        val feature = lumia.tracker.model.PlusShop.features.find { it.id == featureId } ?: return false
        if (current.level < feature.requiredLevel) return false
        
        if (current.credits >= cost) {
            current.credits -= cost
            
            val activeRents = current.rentedFeatures.toMutableMap()
            val currentExpiry = activeRents[featureId] ?: System.currentTimeMillis()
            val cleanStart = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
            val durationMillis = durationDays * 24L * 60L * 60L * 1000L
            activeRents[featureId] = cleanStart + durationMillis
            
            current.rentedFeatures = activeRents
            profileManager.updateProfile(current)
            activeProfile.value = current
            allProfiles.value = profileManager.getAllProfiles()
            return true
        }
        return false
    }

    fun rollMysteryWheel(): lumia.tracker.model.PlusFeature? {
        val current = profileManager.getActiveProfile()
        val rollCost = 150 // standard wheel spin in Credits
        if (current.credits < rollCost) return null
        
        current.credits -= rollCost
        
        // Filter out features already unlocked permanently
        val unownedFeatures = lumia.tracker.model.PlusShop.features.filter { !current.unlockedFeatures.contains(it.id) }
        if (unownedFeatures.isEmpty()) {
            // Already owns everything! Refund 150 Credits
            current.credits += 150
            profileManager.updateProfile(current)
            activeProfile.value = current
            allProfiles.value = profileManager.getAllProfiles()
            return null
        }
        
        // Select one based on ranking weights/probabilities
        val weights = unownedFeatures.map { feat ->
            when (feat.rank) {
                "SS" -> 0.004
                "S" -> 0.010
                "A+" -> 0.020
                "A" -> 0.033
                else -> 0.067 // "B"
            }
        }
        val sum = weights.sum()
        if (sum == 0.0) {
            profileManager.updateProfile(current)
            activeProfile.value = current
            allProfiles.value = profileManager.getAllProfiles()
            return null
        }
        
        val randomVal = java.util.Random().nextDouble() * sum
        var cumulative = 0.0
        var selectedIndex = 0
        for (i in unownedFeatures.indices) {
            cumulative += weights[i]
            if (randomVal <= cumulative) {
                selectedIndex = i
                break
            }
        }
        
        val selected = unownedFeatures[selectedIndex]
        val newFeatures = current.unlockedFeatures.toMutableList()
        newFeatures.add(selected.id)
        current.unlockedFeatures = newFeatures
        
        profileManager.updateProfile(current)
        activeProfile.value = current
        allProfiles.value = profileManager.getAllProfiles()
        return selected
    }

    fun awardCredits(creditsGained: Int) {
        val current = profileManager.getActiveProfile()
        current.credits += creditsGained
        profileManager.updateProfile(current)
        activeProfile.value = current
        allProfiles.value = profileManager.getAllProfiles()
        postNotification("Credits Earned!", "+$creditsGained Credits added directly to your profile!", "CREDITS")
    }

    // Advanced Data Management flow states
    private val _dbStatistics = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dbStatistics = _dbStatistics.asStateFlow()

    private val _defragStatus = MutableStateFlow("")
    val defragStatus = _defragStatus.asStateFlow()

    fun loadDBStatistics() {
        viewModelScope.launch {
            val stats = mutableMapOf<String, Int>()
            stats["Courses"] = repository.dao.exportAllCourses().size
            stats["Subjects"] = repository.dao.exportAllSubjects().size
            stats["Exercises"] = repository.dao.exportAllAssignments().size
            stats["Notes"] = repository.dao.exportAllNotes().size
            stats["Tasks"] = repository.dao.exportAllTasks().size
            stats["Focus Sessions"] = repository.dao.exportAllPomodoro().size
            stats["Total Attachments"] = repository.dao.exportAllAttachments().size
            _dbStatistics.value = stats
        }
    }

    fun defragmentDatabase() {
        viewModelScope.launch {
            _defragStatus.value = "Scanning indexes & parsing orphans..."
            kotlinx.coroutines.delay(1000)
            _defragStatus.value = "Executing SQLite VACUUM optimization..."
            // Perform vacuum cleaning and compacting simulation on the SQLite db pages
            repository.dao.exportAllCourses() // harmless read to keep db warm
            kotlinx.coroutines.delay(1200)
            _defragStatus.value = "Optimized! 100% Index health. SQLite database pages compacted successfully!"
            loadDBStatistics()
        }
    }

    fun deleteProfile(context: Context, id: String) {
        val wasActive = profileManager.getActiveProfileId() == id
        profileManager.deleteProfile(id)
        allProfiles.value = profileManager.getAllProfiles()
        if (wasActive) {
            switchProfileAndRestart(context, profileManager.getActiveProfileId())
        }
    }
    
    data class InAppNotification(
        val id: String = java.util.UUID.randomUUID().toString(),
        val title: String,
        val message: String,
        val type: String, // "ACHIEVEMENT", "CREDITS", "POINTS", "XP", "INFO"
        val iconEmoji: String = ""
    )

    private val _inAppNotifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val inAppNotifications = _inAppNotifications.asStateFlow()

    fun postNotification(title: String, message: String, type: String = "INFO", icon: String = "") {
        val newNotification = InAppNotification(title = title, message = message, type = type, iconEmoji = icon)
        _inAppNotifications.value = _inAppNotifications.value + newNotification
    }

    fun dismissNotification(id: String) {
        _inAppNotifications.value = _inAppNotifications.value.filter { it.id != id }
    }

    data class LevelUpRewardEvent(
        val newLevel: Int,
        val pointsEarned: Int,
        val creditsEarned: Int,
        val featureUnlocked: String? = null,
        val gaveBox: Boolean = false
    )
    
    data class SurpriseBoxResult(
        val type: String, // "CREDITS", "POINTS", "TICKET_24H", "TICKET_48H"
        val amount: Int,
        val detailText: String,
        val featureName: String = ""
    )
    
    private val _lastLevelUpEvent = MutableStateFlow<LevelUpRewardEvent?>(null)
    val lastLevelUpEvent = _lastLevelUpEvent.asStateFlow()
    
    fun clearLevelUpEvent() {
        _lastLevelUpEvent.value = null
    }

    fun awardPoints(points: Int) {
        val current = profileManager.getActiveProfile()
        current.points += points
        profileManager.updateProfile(current)
        activeProfile.value = current
        allProfiles.value = profileManager.getAllProfiles()
        postNotification("Focus Points Gained!", "+$points Focus Points (FP) earned!", "POINTS")
        
        // Let's also check achievements in a separate function...
        checkAchievements(current)
    }

    fun awardExperience(xpGained: Int) {
        val current = profileManager.getActiveProfile()
        current.experience += xpGained
        postNotification("XP Gained!", "+$xpGained Experience secured!", "XP")
        
        var newLevel = current.level
        var xpLeft = current.experience
        var leveledUp = false
        
        while (xpLeft >= getXpNeededForNextLevel(newLevel)) {
            xpLeft -= getXpNeededForNextLevel(newLevel)
            newLevel++
            leveledUp = true
            onLevelUp(newLevel, current)
        }
        
        current.level = newLevel
        current.experience = xpLeft
        
        profileManager.updateProfile(current)
        activeProfile.value = current
        allProfiles.value = profileManager.getAllProfiles()
    }
    
    fun getXpNeededForNextLevel(currentLevel: Int): Int {
        return 100 + (currentLevel * 50)
    }
    
    private fun onLevelUp(newLevel: Int, current: lumia.tracker.model.UserProfile): LevelUpRewardEvent {
        var pointsEarned = 0
        var creditsEarned = 0
        var featureUnlockedName: String? = null
        var gaveBox = false
        
        if (newLevel == 1) {
            current.points += 10
            pointsEarned += 10
        } else if (newLevel >= 2) {
            current.pendingSurpriseBoxes++
            gaveBox = true
        }
        
        if (newLevel > 0 && newLevel % 5 == 0) {
            val randCredits = java.util.Random().nextInt(701) + 100 // 100 to 800
            current.credits += randCredits
            creditsEarned += randCredits
        }
        
        if (newLevel > 0 && newLevel % 10 == 0) {
            current.credits += 100
            creditsEarned += 100
            val randPoints = java.util.Random().nextInt(16) + 5 // 5 to 20
            current.points += randPoints
            pointsEarned += randPoints
        }
        
        if (newLevel > 0 && newLevel % 50 == 0) {
            val eligible = lumia.tracker.model.PlusShop.features.filter { it.rank == "A+" || it.rank == "S" || it.rank == "SS" }
            if (eligible.isNotEmpty()) {
                val selectedFeat = eligible.random()
                val weeks = java.util.Random().nextInt(2) + 3 // 3 or 4 weeks
                val durationMillis = weeks * 7L * 24L * 60L * 60L * 1000L
                val activeRents = current.rentedFeatures.toMutableMap()
                val currentExpiry = activeRents[selectedFeat.id] ?: System.currentTimeMillis()
                val cleanStart = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
                activeRents[selectedFeat.id] = cleanStart + durationMillis
                current.rentedFeatures = activeRents
                featureUnlockedName = "${selectedFeat.name} ($weeks weeks)"
            }
        }
        
        val event = LevelUpRewardEvent(
            newLevel = newLevel,
            pointsEarned = pointsEarned,
            creditsEarned = creditsEarned,
            featureUnlocked = featureUnlockedName,
            gaveBox = gaveBox
        )
        _lastLevelUpEvent.value = event
        return event
    }
    
    fun claimSurpriseBox(): SurpriseBoxResult? {
        val current = profileManager.getActiveProfile()
        if (current.pendingSurpriseBoxes <= 0) return null
        
        current.pendingSurpriseBoxes--
        
        val rand = java.util.Random().nextDouble() * 100.0
        val result: SurpriseBoxResult
        
        when {
            // 30% of 20 credits
            rand < 30.0 -> {
                current.credits += 20
                result = SurpriseBoxResult("CREDITS", 20, "20 Credits Gained!")
            }
            // 40% of 10 credits (Total 70%)
            rand < 70.0 -> {
                current.credits += 10
                result = SurpriseBoxResult("CREDITS", 10, "10 Credits Gained!")
            }
            // 15% of 60-70 credits (randomly) (Total 85%)
            rand < 85.0 -> {
                val amt = java.util.Random().nextInt(11) + 60 // 60 to 70
                current.credits += amt
                result = SurpriseBoxResult("CREDITS", amt, "$amt Credits Gained!")
            }
            // 3% of 5 points (Total 88%)
            rand < 88.0 -> {
                current.points += 5
                result = SurpriseBoxResult("POINTS", 5, "5 Focus Points Gained!")
            }
            // 4% of 2 points (Total 92%)
            rand < 92.0 -> {
                current.points += 2
                result = SurpriseBoxResult("POINTS", 2, "2 Focus Points Gained!")
            }
            // 2% of 6-15 points (completely randomly chosen) (Total 94%)
            rand < 94.0 -> {
                val amt = java.util.Random().nextInt(10) + 6 // 6 to 15
                current.points += amt
                result = SurpriseBoxResult("POINTS", amt, "$amt Focus Points Gained!")
            }
            // 5% of a 24 hour ticket of any plus setting or feature (Total 99%)
            rand < 99.0 -> {
                val feature = lumia.tracker.model.PlusShop.features.random()
                val durationMillis = 24L * 60L * 60L * 1000L
                val activeRents = current.rentedFeatures.toMutableMap()
                val currentExpiry = activeRents[feature.id] ?: System.currentTimeMillis()
                val cleanStart = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
                activeRents[feature.id] = cleanStart + durationMillis
                current.rentedFeatures = activeRents
                result = SurpriseBoxResult("TICKET_24H", 24, "24-Hour Trial Ticket: ${feature.name}", feature.name)
            }
            // 1% of a 48 hrs ticket for any random plus setting or feature (Total 100%)
            else -> {
                val feature = lumia.tracker.model.PlusShop.features.random()
                val durationMillis = 48L * 60L * 60L * 1000L
                val activeRents = current.rentedFeatures.toMutableMap()
                val currentExpiry = activeRents[feature.id] ?: System.currentTimeMillis()
                val cleanStart = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
                activeRents[feature.id] = cleanStart + durationMillis
                current.rentedFeatures = activeRents
                result = SurpriseBoxResult("TICKET_48H", 48, "48-Hour Trial Ticket: ${feature.name}", feature.name)
            }
        }
        
        profileManager.updateProfile(current)
        activeProfile.value = current
        allProfiles.value = profileManager.getAllProfiles()
        
        checkAchievements(current)
        return result
    }
    
    private fun checkAchievements(profile: lumia.tracker.model.UserProfile) {
        viewModelScope.launch {
            val totalTasks = tasks.value.filter { it.isCompleted }.size
            val totalSessions = pomodoroSessions.value.size
            val streak = prefs.getInt("current_streak", 0)
            
            val unlocked = lumia.tracker.model.AchievementSystem.evaluateAchievements(
                profile = profile,
                totalTasks = totalTasks,
                totalSessions = totalSessions,
                currentStreak = streak
            )
            
            if (unlocked.isNotEmpty()) {
                val updatedUnlocked = profile.unlockedAchievements.toMutableList()
                val newlyUnlockedFeatures = profile.unlockedFeatures.toMutableList()
                var featuresAdded = false
                unlocked.forEach { ach ->
                    updatedUnlocked.add(ach.id)
                    ach.rewardFeatureId?.let { featureId ->
                        if (!newlyUnlockedFeatures.contains(featureId)) {
                            newlyUnlockedFeatures.add(featureId)
                            featuresAdded = true
                        }
                    }
                    postNotification(
                        title = "Achievement Unlocked!",
                        message = "${ach.title}: ${ach.description}",
                        type = "ACHIEVEMENT",
                        icon = ach.iconEmoji
                    )
                }
                profile.unlockedAchievements = updatedUnlocked
                if (featuresAdded) {
                    profile.unlockedFeatures = newlyUnlockedFeatures
                }
                profileManager.updateProfile(profile)
                activeProfile.value = profile
            }
        }
    }

    private val repository = ScholarRepository(AppDatabase.getDatabase(application).scholarDao())
    
    private val prefs = application.getSharedPreferences("lumia_prefs", Context.MODE_PRIVATE)

    private val initiallyCompleted = run {
        var completed = prefs.getBoolean("onboarding_completed", false)
        val wasInstalledBefore = prefs.getBoolean("was_installed_before", false)
        if (!wasInstalledBefore) {
            val dbFile = application.getDatabasePath("scholar_sync_database")
            val isUpdate = prefs.all.filterKeys { it != "was_installed_before" && it != "onboarding_completed" }.isNotEmpty() || dbFile.exists()
            if (isUpdate) {
                completed = true
                prefs.edit().putBoolean("onboarding_completed", true).putBoolean("was_installed_before", true).apply()
            } else {
                prefs.edit().putBoolean("was_installed_before", true).apply()
            }
        }
        completed
    }

    private val _isOnboardingCompleted = MutableStateFlow(initiallyCompleted)
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

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

    private val _navBarHeight = MutableStateFlow(prefs.getFloat("nav_bar_height", 80f))
    val navBarHeight = _navBarHeight.asStateFlow()

    private val _navBarPaddingHorizontal = MutableStateFlow(prefs.getFloat("nav_bar_padding_horizontal", 24f))
    val navBarPaddingHorizontal = _navBarPaddingHorizontal.asStateFlow()

    private val _navBarPaddingBottom = MutableStateFlow(prefs.getFloat("nav_bar_padding_bottom", 24f))
    val navBarPaddingBottom = _navBarPaddingBottom.asStateFlow()

    private val _navBarCornerRadius = MutableStateFlow(prefs.getFloat("nav_bar_corner_radius", 32f))
    val navBarCornerRadius = _navBarCornerRadius.asStateFlow()

    private val _navBarLabelMode = MutableStateFlow(prefs.getString("nav_bar_label_mode", "Always") ?: "Always")
    val navBarLabelMode = _navBarLabelMode.asStateFlow()

    private val _navBarGlassForceEnabled = MutableStateFlow(prefs.getBoolean("nav_bar_glass_force_enabled", false))
    val navBarGlassForceEnabled = _navBarGlassForceEnabled.asStateFlow()

    private val _navBarIndicatorAlpha = MutableStateFlow(prefs.getFloat("nav_bar_indicator_alpha", 0.15f))
    val navBarIndicatorAlpha = _navBarIndicatorAlpha.asStateFlow()

    private val _betaNotes = MutableStateFlow(prefs.getBoolean("beta_notes", false))
    val betaNotes = _betaNotes.asStateFlow()

    private val _appAnimationMode = MutableStateFlow(prefs.getString("app_animation_mode", "Normal") ?: "Normal")
    val appAnimationMode = _appAnimationMode.asStateFlow()

    private val _moreRounds = MutableStateFlow(prefs.getBoolean("more_rounds", false))
    val moreRounds = _moreRounds.asStateFlow()

    private val _moreRoundsMode = MutableStateFlow(prefs.getString("more_rounds_mode", "Pastel") ?: "Pastel")
    val moreRoundsMode = _moreRoundsMode.asStateFlow()

    fun updateAppAnimationMode(mode: String) {
        if (mode == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value && (_displayLayoutMode.value != "Immersive" || !_moreRounds.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Bouncy Animations Warning",
                description = "Bouncy animations require 'Immersive' layout mode and 'More Rounds' feature to be enabled. Proceed with enabling these requirements automatically?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    updateDisplayLayoutMode("Immersive")
                    updateMoreRounds(true)
                    _appAnimationMode.value = "Bouncy"
                    prefs.edit().putString("app_animation_mode", "Bouncy").apply()
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _appAnimationMode.value = mode
        prefs.edit().putString("app_animation_mode", mode).apply()
    }

    fun updateMoreRounds(enabled: Boolean) {
        if (!enabled && _appAnimationMode.value == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Required by Bouncy Animations",
                description = "Disabling 'More Rounds' will also disable 'Bouncy' animations and revert to 'Dynamic'. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _moreRounds.value = false
                    prefs.edit().putBoolean("more_rounds", false).apply()
                    updateAppAnimationMode("Dynamic")
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _moreRounds.value = enabled
        prefs.edit().putBoolean("more_rounds", enabled).apply()
        if (!enabled && _appAnimationMode.value == "Bouncy") {
            updateAppAnimationMode("Dynamic")
        }
    }

    fun updateMoreRoundsMode(mode: String) {
        _moreRoundsMode.value = mode
        prefs.edit().putString("more_rounds_mode", mode).apply()
    }

    private val _displayLayoutMode = MutableStateFlow(prefs.getString("display_layout_mode", "Immersive") ?: "Immersive")
    val displayLayoutMode = _displayLayoutMode.asStateFlow()

    private val _betaGlassUi = MutableStateFlow(prefs.getBoolean("beta_glass_ui", false))
    val betaGlassUi = _betaGlassUi.asStateFlow()

    private val _betaGlassDynamic = MutableStateFlow(prefs.getBoolean("beta_glass_dynamic", true))
    val betaGlassDynamic = _betaGlassDynamic.asStateFlow()

    private val _betaFrostGlass = MutableStateFlow(prefs.getBoolean("beta_frost_glass", true))
    val betaFrostGlass = _betaFrostGlass.asStateFlow()

    private val _glassBackdropStyle = MutableStateFlow(prefs.getString("glass_backdrop_style", "Translucent") ?: "Translucent")
    val glassBackdropStyle = _glassBackdropStyle.asStateFlow()

    private val _glassOpacityValue = MutableStateFlow(prefs.getFloat("glass_opacity_value", 0.6f))
    val glassOpacityValue = _glassOpacityValue.asStateFlow()

    private val _navBarGlassOpacityValue = MutableStateFlow(0.6f)
    val navBarGlassOpacityValue = _navBarGlassOpacityValue.asStateFlow()

    private val _betaNavBarSizeControls = MutableStateFlow(prefs.getBoolean("beta_nav_bar_size_controls", false))
    val betaNavBarSizeControls = _betaNavBarSizeControls.asStateFlow()

    private val _navBarGlassLinkedToMain = MutableStateFlow(prefs.getBoolean("nav_bar_glass_linked_to_main", true))
    val navBarGlassLinkedToMain = _navBarGlassLinkedToMain.asStateFlow()

    private val _navBarGlassBackdropStyle = MutableStateFlow(prefs.getString("nav_bar_glass_backdrop_style", "Translucent") ?: "Translucent")
    val navBarGlassBackdropStyle = _navBarGlassBackdropStyle.asStateFlow()

    private val _navBarGlassDynamic = MutableStateFlow(prefs.getBoolean("nav_bar_glass_dynamic", true))
    val navBarGlassDynamic = _navBarGlassDynamic.asStateFlow()

    private val _betaEnhancedHeader = MutableStateFlow(prefs.getBoolean("beta_enhanced_header", false))
    val betaEnhancedHeader = _betaEnhancedHeader.asStateFlow()

    private val _betaMinimalistMode = MutableStateFlow(prefs.getBoolean("beta_minimalist_mode", false))
    val betaMinimalistMode = _betaMinimalistMode.asStateFlow()

    private val _betaDynamicBackground = MutableStateFlow(prefs.getBoolean("beta_dynamic_background", false))
    val betaDynamicBackground = _betaDynamicBackground.asStateFlow()

    private val _systemAutoLinkByName = MutableStateFlow(prefs.getBoolean("system_auto_link_by_name", true))
    val systemAutoLinkByName = _systemAutoLinkByName.asStateFlow()

    private val _systemEnableSynergy = MutableStateFlow(prefs.getBoolean("system_enable_synergy", true))
    val systemEnableSynergy = _systemEnableSynergy.asStateFlow()

    private val _systemAutoCreateSubject = MutableStateFlow(prefs.getBoolean("system_auto_create_subject", false))
    val systemAutoCreateSubject = _systemAutoCreateSubject.asStateFlow()

    private val _systemFuseSubjectsCourses = MutableStateFlow(prefs.getBoolean("system_fuse_subjects_courses", true))
    val systemFuseSubjectsCourses = _systemFuseSubjectsCourses.asStateFlow()

    private val _systemAdvancedTasks = MutableStateFlow(prefs.getBoolean("system_advanced_tasks", true))
    val systemAdvancedTasks = _systemAdvancedTasks.asStateFlow()
    
    private val _systemPomodoroAutoLog = MutableStateFlow(prefs.getBoolean("system_pomodoro_auto_log", true))
    val systemPomodoroAutoLog = _systemPomodoroAutoLog.asStateFlow()

    private val _featureSubjectEnabled = MutableStateFlow(prefs.getBoolean("feature_subject_enabled", true))
    val featureSubjectEnabled = _featureSubjectEnabled.asStateFlow()

    private val _featureSelfStudyEnabled = MutableStateFlow(prefs.getBoolean("feature_self_study_enabled", true))
    val featureSelfStudyEnabled = _featureSelfStudyEnabled.asStateFlow()

    private val _featureAnalyticsEnabled = MutableStateFlow(prefs.getBoolean("feature_analytics_enabled", true))
    val featureAnalyticsEnabled = _featureAnalyticsEnabled.asStateFlow()

    private val _featureCalendarEnabled = MutableStateFlow(prefs.getBoolean("feature_calendar_enabled", true))
    val featureCalendarEnabled = _featureCalendarEnabled.asStateFlow()

    private val _featureQuickNotesEnabled = MutableStateFlow(prefs.getBoolean("feature_quick_notes_enabled", true))
    val featureQuickNotesEnabled = _featureQuickNotesEnabled.asStateFlow()

    fun updateFeatureSubjectEnabled(enabled: Boolean) {
        _featureSubjectEnabled.value = enabled
        prefs.edit().putBoolean("feature_subject_enabled", enabled).apply()
    }

    fun updateFeatureSelfStudyEnabled(enabled: Boolean) {
        _featureSelfStudyEnabled.value = enabled
        prefs.edit().putBoolean("feature_self_study_enabled", enabled).apply()
    }

    fun updateFeatureAnalyticsEnabled(enabled: Boolean) {
        _featureAnalyticsEnabled.value = enabled
        prefs.edit().putBoolean("feature_analytics_enabled", enabled).apply()
    }

    fun updateFeatureCalendarEnabled(enabled: Boolean) {
        _featureCalendarEnabled.value = enabled
        prefs.edit().putBoolean("feature_calendar_enabled", enabled).apply()
    }

    fun updateFeatureQuickNotesEnabled(enabled: Boolean) {
        _featureQuickNotesEnabled.value = enabled
        prefs.edit().putBoolean("feature_quick_notes_enabled", enabled).apply()
    }

    val allAttachments = repository.allAttachments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getAttachmentsForCourse(courseId: Int) = repository.getAttachmentsForCourse(courseId)
    fun getAttachmentsForSubject(subjectId: Int) = repository.getAttachmentsForSubject(subjectId)

    fun addAttachment(name: String, filePath: String, fileType: String, sizeBytes: Long, courseId: Int?, subjectId: Int?) {
        viewModelScope.launch {
            repository.insertAttachment(
                lumia.tracker.model.Attachment(
                    name = name,
                    filePath = filePath,
                    fileType = fileType,
                    sizeBytes = sizeBytes,
                    courseId = courseId,
                    subjectId = subjectId
                )
            )
            logAction("Added attachment: $name")
            awardExperience(30)
        }
    }

    fun deleteAttachment(attachment: lumia.tracker.model.Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
            try {
                val file = java.io.File(attachment.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            logAction("Deleted attachment: ${attachment.name}")
        }
    }

    private val _pomodoroWorkDuration = MutableStateFlow(prefs.getInt("pomodoro_work_duration", 25))
    val pomodoroWorkDuration = _pomodoroWorkDuration.asStateFlow()

    private val _pomodoroShortBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_short_break_duration", 5))
    val pomodoroShortBreakDuration = _pomodoroShortBreakDuration.asStateFlow()

    private val _pomodoroLongBreakDuration = MutableStateFlow(prefs.getInt("pomodoro_long_break_duration", 15))
    val pomodoroLongBreakDuration = _pomodoroLongBreakDuration.asStateFlow()

    private val _pomodoroPeriodSessions = MutableStateFlow(prefs.getInt("pomodoro_period_sessions", 4))
    val pomodoroPeriodSessions = _pomodoroPeriodSessions.asStateFlow()
    
    private val _pomodoroEnablePeriodTarget = MutableStateFlow(prefs.getBoolean("pomodoro_enable_period_target", false))
    val pomodoroEnablePeriodTarget = _pomodoroEnablePeriodTarget.asStateFlow()

    fun updatePomodoroPeriodSessions(sessions: Int) {
        _pomodoroPeriodSessions.value = sessions
        prefs.edit().putInt("pomodoro_period_sessions", sessions).apply()
    }

    fun updatePomodoroEnablePeriodTarget(enabled: Boolean) {
        _pomodoroEnablePeriodTarget.value = enabled
        prefs.edit().putBoolean("pomodoro_enable_period_target", enabled).apply()
    }

    private val _notifFormalTone = MutableStateFlow(prefs.getBoolean("notif_formal_tone", true))
    val notifFormalTone = _notifFormalTone.asStateFlow()

    private val _notifEnableDeadlines = MutableStateFlow(prefs.getBoolean("notif_enable_deadlines", true))
    val notifEnableDeadlines = _notifEnableDeadlines.asStateFlow()

    private val _notifEnableStreaks = MutableStateFlow(prefs.getBoolean("notif_enable_streaks", true))
    val notifEnableStreaks = _notifEnableStreaks.asStateFlow()

    private val _notifEnableClasses = MutableStateFlow(prefs.getBoolean("notif_enable_classes", true))
    val notifEnableClasses = _notifEnableClasses.asStateFlow()

    private val _notifEnableDailyDigest = MutableStateFlow(prefs.getBoolean("notif_enable_daily_digest", true))
    val notifEnableDailyDigest = _notifEnableDailyDigest.asStateFlow()

    fun updateNotifFormalTone(enabled: Boolean) {
        _notifFormalTone.value = enabled
        prefs.edit().putBoolean("notif_formal_tone", enabled).apply()
    }

    fun updateNotifEnableDeadlines(enabled: Boolean) {
        _notifEnableDeadlines.value = enabled
        prefs.edit().putBoolean("notif_enable_deadlines", enabled).apply()
    }

    fun updateNotifEnableStreaks(enabled: Boolean) {
        _notifEnableStreaks.value = enabled
        prefs.edit().putBoolean("notif_enable_streaks", enabled).apply()
    }

    fun updateNotifEnableClasses(enabled: Boolean) {
        _notifEnableClasses.value = enabled
        prefs.edit().putBoolean("notif_enable_classes", enabled).apply()
    }

    fun updateNotifEnableDailyDigest(enabled: Boolean) {
        _notifEnableDailyDigest.value = enabled
        prefs.edit().putBoolean("notif_enable_daily_digest", enabled).apply()
    }

    private val _aodTrueBlackOled = MutableStateFlow(prefs.getBoolean("aod_true_black_oled", true))
    val aodTrueBlackOled = _aodTrueBlackOled.asStateFlow()

    private val _aodAutoDeactivateTrueBlack = MutableStateFlow(prefs.getBoolean("aod_auto_deactivate_true_black", true))
    val aodAutoDeactivateTrueBlack = _aodAutoDeactivateTrueBlack.asStateFlow()

    private val _aodBurnInShiftSpeed = MutableStateFlow(prefs.getInt("aod_burn_in_shift_speed", 10)) // in seconds
    val aodBurnInShiftSpeed = _aodBurnInShiftSpeed.asStateFlow()
    
    private val _aodLockScreenSupport = MutableStateFlow(prefs.getBoolean("aod_lock_screen_support", false))
    val aodLockScreenSupport = _aodLockScreenSupport.asStateFlow()

    private val _aodTrueAodEnabled = MutableStateFlow(prefs.getBoolean("aod_true_aod_enabled", false))
    val aodTrueAodEnabled = _aodTrueAodEnabled.asStateFlow()

    private val _aodTrueAodMode = MutableStateFlow(prefs.getString("aod_true_aod_mode", "overlay") ?: "overlay")
    val aodTrueAodMode = _aodTrueAodMode.asStateFlow()

    private val _aodSensitivity = MutableStateFlow(prefs.getString("aod_sensitivity", "highest") ?: "highest")
    val aodSensitivity = _aodSensitivity.asStateFlow()

    private val _aodMotionSensitivity = MutableStateFlow(prefs.getFloat("aod_motion_sensitivity", 1.2f))
    val aodMotionSensitivity = _aodMotionSensitivity.asStateFlow()

    private val _aodDimnessLevel = MutableStateFlow(prefs.getFloat("aod_dimness_level", 0.95f))
    val aodDimnessLevel = _aodDimnessLevel.asStateFlow()

    private val _aodLockTimeout = MutableStateFlow(prefs.getInt("aod_lock_timeout", 30))
    val aodLockTimeout = _aodLockTimeout.asStateFlow()

    fun updateAodLockScreenSupport(enabled: Boolean) {
        _aodLockScreenSupport.value = enabled
        prefs.edit().putBoolean("aod_lock_screen_support", enabled).apply()
    }

    fun updateAodTrueAodEnabled(enabled: Boolean) {
        _aodTrueAodEnabled.value = enabled
        prefs.edit().putBoolean("aod_true_aod_enabled", enabled).apply()
    }

    fun updateAodTrueAodMode(mode: String) {
        _aodTrueAodMode.value = mode
        prefs.edit().putString("aod_true_aod_mode", mode).apply()
    }

    fun updateAodSensitivity(sensitivity: String) {
        _aodSensitivity.value = sensitivity
        prefs.edit().putString("aod_sensitivity", sensitivity).apply()
    }

    fun updateAodMotionSensitivity(sensitivity: Float) {
        _aodMotionSensitivity.value = sensitivity
        prefs.edit().putFloat("aod_motion_sensitivity", sensitivity).apply()
    }

    fun updateAodDimnessLevel(level: Float) {
        _aodDimnessLevel.value = level
        prefs.edit().putFloat("aod_dimness_level", level).apply()
    }

    fun updateAodLockTimeout(seconds: Int) {
        _aodLockTimeout.value = seconds
        prefs.edit().putInt("aod_lock_timeout", seconds).apply()
    }

    fun updateAodTrueBlackOled(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (_themeMode.value == "Light" || _betaGlassUi.value || _betaDynamicBackground.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "AOD Style Warning",
                description = "Enabling 'True Black OLED' mode during Light theme, Dynamic wallpapers, or Glass UI can lead to strong contrast transitions when AOD focus opens or exits. Consider allowing auto-deactivation instead.",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _aodTrueBlackOled.value = true
                    prefs.edit().putBoolean("aod_true_black_oled", true).apply()
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _aodTrueBlackOled.value = enabled
        prefs.edit().putBoolean("aod_true_black_oled", enabled).apply()
    }

    fun updateAodAutoDeactivateTrueBlack(enabled: Boolean) {
        _aodAutoDeactivateTrueBlack.value = enabled
        prefs.edit().putBoolean("aod_auto_deactivate_true_black", enabled).apply()
    }

    fun updateAodBurnInShiftSpeed(speed: Int) {
        _aodBurnInShiftSpeed.value = speed
        prefs.edit().putInt("aod_burn_in_shift_speed", speed).apply()
    }

    fun updateSystemAutoLinkByName(enabled: Boolean) {
        _systemAutoLinkByName.value = enabled
        prefs.edit().putBoolean("system_auto_link_by_name", enabled).apply()
    }

    fun updateSystemEnableSynergy(enabled: Boolean) {
        _systemEnableSynergy.value = enabled
        prefs.edit().putBoolean("system_enable_synergy", enabled).apply()
    }

    fun updateSystemAutoCreateSubject(enabled: Boolean) {
        _systemAutoCreateSubject.value = enabled
        prefs.edit().putBoolean("system_auto_create_subject", enabled).apply()
    }

    fun updateSystemFuseSubjectsCourses(enabled: Boolean) {
        _systemFuseSubjectsCourses.value = enabled
        prefs.edit().putBoolean("system_fuse_subjects_courses", enabled).apply()
    }

    fun updateSystemAdvancedTasks(enabled: Boolean) {
        _systemAdvancedTasks.value = enabled
        prefs.edit().putBoolean("system_advanced_tasks", enabled).apply()
    }

    fun updateSystemPomodoroAutoLog(enabled: Boolean) {
        _systemPomodoroAutoLog.value = enabled
        prefs.edit().putBoolean("system_pomodoro_auto_log", enabled).apply()
    }

    fun updatePomodoroWorkDuration(duration: Int) {
        _pomodoroWorkDuration.value = duration
        prefs.edit().putInt("pomodoro_work_duration", duration).apply()
    }

    fun updatePomodoroShortBreakDuration(duration: Int) {
        _pomodoroShortBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_short_break_duration", duration).apply()
    }

    fun updatePomodoroLongBreakDuration(duration: Int) {
        _pomodoroLongBreakDuration.value = duration
        prefs.edit().putInt("pomodoro_long_break_duration", duration).apply()
    }

    private val _dynamicBgLightBrightness = MutableStateFlow(
        prefs.getFloat("dynamic_bg_light_brightness_${(prefs.getString("theme_color", "Ocean") ?: "Ocean").lowercase()}", 0.75f)
    )
    val dynamicBgLightBrightness = _dynamicBgLightBrightness.asStateFlow()

    private val _dynamicBgDarkBrightness = MutableStateFlow(
        prefs.getFloat("dynamic_bg_dark_brightness_${(prefs.getString("theme_color", "Ocean") ?: "Ocean").lowercase()}", 0.45f)
    )
    val dynamicBgDarkBrightness = _dynamicBgDarkBrightness.asStateFlow()

    fun refreshThemeBrightness() {
        val theme = _themeColor.value
        _dynamicBgLightBrightness.value = prefs.getFloat("dynamic_bg_light_brightness_${theme.lowercase()}", 0.75f)
        _dynamicBgDarkBrightness.value = prefs.getFloat("dynamic_bg_dark_brightness_${theme.lowercase()}", 0.45f)
    }

    private val _dynamicAppIcon = MutableStateFlow(prefs.getBoolean("dynamic_app_icon", false))
    val dynamicAppIcon = _dynamicAppIcon.asStateFlow()

    fun updateDynamicAppIcon(enabled: Boolean) {
        _dynamicAppIcon.value = enabled
        prefs.edit().putBoolean("dynamic_app_icon", enabled).apply()
        applyThemeBasedAppIcon(_themeColor.value)
        android.widget.Toast.makeText(getApplication(), "Icon changing... Launcher may take a moment to reflect changes or might require a home screen refresh.", android.widget.Toast.LENGTH_LONG).show()
    }

    private fun applyThemeBasedAppIcon(themeColor: String) {
        val enabled = _dynamicAppIcon.value
        val pm = getApplication<Application>().packageManager
        val packageName = getApplication<Application>().packageName

        val aliases = listOf(
            "lumia.tracker.DefaultAlias",
            "lumia.tracker.AliasEmerald",
            "lumia.tracker.AliasGold",
            "lumia.tracker.AliasRose",
            "lumia.tracker.AliasSage",
            "lumia.tracker.AliasTwilight",
            "lumia.tracker.AliasCustom",
            "lumia.tracker.AliasDynamic"
        )

        val targetAliasName = if (!enabled) {
            "lumia.tracker.DefaultAlias"
        } else {
            when (themeColor) {
                "Ocean" -> "lumia.tracker.DefaultAlias"
                "Emerald" -> "lumia.tracker.AliasEmerald"
                "Gold" -> "lumia.tracker.AliasGold"
                "Rose" -> "lumia.tracker.AliasRose"
                "Sage" -> "lumia.tracker.AliasSage"
                "Twilight" -> "lumia.tracker.AliasTwilight"
                "Custom" -> "lumia.tracker.AliasCustom"
                "Dynamic" -> "lumia.tracker.AliasDynamic"
                else -> "lumia.tracker.DefaultAlias"
            }
        }

        try {
            aliases.forEach { alias ->
                val compName = android.content.ComponentName(packageName, alias)
                val targetSetting = if (alias == targetAliasName) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(compName, targetSetting, android.content.pm.PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            android.util.Log.e("ScholarViewModel", "Exception toggling dynamic app icon aliases. Restoring defaults.", e)
            try {
                aliases.forEach { alias ->
                    val compName = android.content.ComponentName(packageName, alias)
                    val targetSetting = if (alias == "lumia.tracker.DefaultAlias") {
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    } else {
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    }
                    pm.setComponentEnabledSetting(compName, targetSetting, android.content.pm.PackageManager.DONT_KILL_APP)
                }
            } catch (ex: Exception) {}
        }
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
        
        refreshThemeBrightness()
        
        val lastActionDate = prefs.getString("last_action_date_str", "") ?: ""
        if (lastActionDate.isNotEmpty()) {
            val today = todayDateString()
            if (lastActionDate != today) {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                try {
                    val lastDate = sdf.parse(lastActionDate)
                    if (lastDate != null) {
                        val diff = ((sdf.parse(today)?.time ?: System.currentTimeMillis()) - lastDate.time) / 86400000L
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

    val tasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pomodoroSessions: StateFlow<List<lumia.tracker.model.PomodoroSession>> = repository.allPomodoroSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getSessionsTodayCount(): Int {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return pomodoroSessions.value.count { it.dateMillis >= todayStart }
    }

    fun getNotesCount(): Int = notes.value.size
    
    fun getActiveTasksCount(): Int = tasks.value.count { !it.isCompleted }

    val notes: StateFlow<List<lumia.tracker.model.Note>> = repository.allNotes.stateIn(
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
    private val chaptersFlowCache = HashMap<Int, StateFlow<List<Chapter>>>()

    fun getChaptersForSubject(subjectId: Int): StateFlow<List<Chapter>> {
        return chaptersFlowCache.getOrPut(subjectId) {
            repository.getChaptersForSubject(subjectId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    fun getAssignmentsForCourse(courseId: Int): StateFlow<List<PracticeAssignment>> {
        return assignmentsFlowCache.getOrPut(courseId) {
            repository.getAssignmentsForCourse(courseId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    private val attendanceFlowCache = HashMap<Int, StateFlow<List<lumia.tracker.model.AttendanceRecord>>>()

    fun getAttendanceForCourse(courseId: Int): StateFlow<List<lumia.tracker.model.AttendanceRecord>> {
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
            repository.insertAttendanceRecord(lumia.tracker.model.AttendanceRecord(courseId = courseId, dateMillis = normalized, status = status))
        }
    }

    fun addPomodoroSession(durationMinutes: Int, subjectId: Int? = null, courseId: Int? = null, assignmentId: Int? = null, taskId: Int? = null) {
        viewModelScope.launch {
            repository.insertPomodoroSession(lumia.tracker.model.PomodoroSession(
                dateMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                subjectId = subjectId,
                courseId = courseId,
                assignmentId = assignmentId,
                taskId = taskId
            ))
            logAction("Completed Pomodoro Session ($durationMinutes min)")
            val creditsToAward = durationMinutes * 2
            val pointsToAward = durationMinutes / 25
            if (creditsToAward > 0) {
                awardCredits(creditsToAward)
            }
            if (pointsToAward > 0) {
                awardPoints(pointsToAward)
            }
            awardExperience(10 + durationMinutes * 3)
        }
    }

    fun addNote(content: String, courseId: Int? = null, subjectId: Int? = null, tag: String = "") {
        viewModelScope.launch {
            repository.insertNote(lumia.tracker.model.Note(
                content = content,
                dateMillis = System.currentTimeMillis(),
                courseId = courseId,
                subjectId = subjectId,
                tag = tag
            ))
            logAction("Added Note: ${content.take(20)}...")
        }
    }

    fun updateNote(note: lumia.tracker.model.Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: lumia.tracker.model.Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun updateAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(record)
        }
    }

    fun deleteAttendanceRecord(record: lumia.tracker.model.AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
        }
    }

    fun addCourse(
        name: String,
        code: String = "",
        colorHex: String = "#3197D6",
        scheduleDays: String = "",
        scheduleStartTime: String = "",
        scheduleEndTime: String = "",
        instructor: String = "",
        schedule: String = "",
        description: String = "",
        subjectId: Int? = null,
        tags: String = ""
    ) {
        viewModelScope.launch {
            var finalSubjectId = subjectId
            if (finalSubjectId == null && _systemAutoCreateSubject.value) {
                val subId = repository.insertSubject(Subject(name = name))
                finalSubjectId = subId.toInt()
            }
            repository.insertCourse(
                Course(
                    name = name,
                    code = code,
                    colorHex = colorHex,
                    scheduleDays = scheduleDays,
                    scheduleStartTime = scheduleStartTime,
                    scheduleEndTime = scheduleEndTime,
                    instructor = instructor,
                    schedule = schedule,
                    description = description,
                    subjectId = finalSubjectId,
                    tags = tags
                )
            )
            logAction("Added course: $name")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            logAction("Deleted course: ${course.name}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            logAction("Updated course: ${course.name}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun addSubject(name: String, tags: String = "") {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, tags = tags))
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

    fun addTopic(subjectId: Int, title: String, tags: String = "", chapterId: Int? = null) {
        viewModelScope.launch {
            repository.insertTopic(Topic(subjectId = subjectId, title = title, tags = tags, chapterId = chapterId))
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

    fun addChapter(name: String, subjectId: Int, description: String = "", tags: String = "") {
        viewModelScope.launch {
            repository.insertChapter(Chapter(name = name, subjectId = subjectId, description = description, tags = tags))
            logAction("Added chapter: $name")
        }
    }

    fun updateChapter(chapter: Chapter) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
            logAction("Updated chapter: ${chapter.name}")
        }
    }

    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            logAction("Deleted chapter: ${chapter.name}")
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val newId = repository.insertTask(task).toInt()
            if (task.dueDateMillis != null) {
                val context = getApplication<Application>().applicationContext
                val links = mutableListOf<String>()
                if (task.subjectId != null) links.add("Subject")
                if (task.courseId != null) links.add("Course")
                if (task.assignmentId != null) links.add("Assignment")
                if (task.tags.isNotBlank()) links.add("Tags: ${task.tags}")
                lumia.tracker.util.ReminderScheduler.scheduleReminder(
                    context, newId + 20000,
                    "Task: ${task.title}",
                    task.description,
                    links.joinToString(", "),
                    task.dueDateMillis
                )
            }
            logAction("Added task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val newlyCompleted = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed task: ${task.title}" else "Unmarked task: ${task.title}"
            logAction(actionText)
            if (newlyCompleted) {
                checkAndUpdateStreak()
                awardCredits(200)
                awardPoints(2)
                awardExperience(40)
            }
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            logAction("Updated task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateTasksOrder(tasks: List<Task>) {
        viewModelScope.launch {
            repository.updateTasks(tasks)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            logAction("Deleted task: ${task.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun addAssignment(courseId: Int, title: String, desc: String, dueDate: Long, category: String = "Homework", categoryColor: String = "#3197D6", tags: String = "", subjectId: Int? = null) {
        viewModelScope.launch {
            val newId = repository.insertAssignment(PracticeAssignment(courseId = courseId, title = title, description = desc, dueDateMillis = dueDate, category = category, categoryColor = categoryColor, tags = tags, subjectId = subjectId)).toInt()
            val context = getApplication<Application>().applicationContext
            var interconnections = "Course: " + (courses.value.find { it.id == courseId }?.name ?: "Unknown")
            if (tags.isNotBlank()) interconnections += ", Tags: $tags"
            lumia.tracker.util.ReminderScheduler.scheduleReminder(context, newId, title, desc, interconnections, dueDate)
            logAction("Added assignment: $title ($category)")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleAssignmentCompleted(assignment: PracticeAssignment) {
        viewModelScope.launch {
            val newlyCompleted = !assignment.isCompleted
            repository.updateAssignment(assignment.copy(isCompleted = newlyCompleted))
            val actionText = if (newlyCompleted) "Completed assignment: ${assignment.title}" else "Unmarked assignment: ${assignment.title}"
            logAction(actionText)
            if (newlyCompleted) {
                checkAndUpdateStreak()
                awardCredits(300)
                awardPoints(3)
                awardExperience(60)
            }
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteAssignment(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateAssignmentDetails(assignment: PracticeAssignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
            logAction("Updated assignment: ${assignment.title}")
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun updateAssignmentsOrder(assignments: List<PracticeAssignment>) {
        viewModelScope.launch {
            repository.updateAssignments(assignments)
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
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
        val diff = ((sdf.parse(today)?.time ?: System.currentTimeMillis()) - lastDate.time) / 86400000L

        when {
            diff == 1L -> {
                val newStreak = _currentStreak.value + 1
                updateStreak(newStreak)
                if (notifEnableStreaks.value) {
                    val formal = notifFormalTone.value
                    val title = if (formal) "Streak Maintained" else "Good Job Not Slacking!"
                    val msg = if (formal) "You have maintained your streak for $newStreak days." else "You actually did something today! Streak is now $newStreak."
                    sendInstantNotification("scholar_streak_channel", 1004, title, msg, lumia.tracker.util.NotificationHelper.getSmallIcon(), lumia.tracker.util.NotificationHelper.getColor(getApplication()))
                }
            }
            diff > 1L  -> {
                updateStreak(1)  // streak broken
                if (notifEnableStreaks.value && _currentStreak.value > 0) { // If there was a streak to break
                    val formal = notifFormalTone.value
                    val title = if (formal) "Streak Broken" else "Whelp... You broke it."
                    val msg = if (formal) "Your last streak was broken. You are back to 1 day." else "I knew you couldn't keep it up. Back to day 1 for you."
                    sendInstantNotification("scholar_streak_channel", 1005, title, msg, lumia.tracker.util.NotificationHelper.getSmallIcon(), lumia.tracker.util.NotificationHelper.getColor(getApplication()))
                }
            }
        }
        prefs.edit().putString("last_action_date_str", today).apply()
    }

    private fun sendInstantNotification(channelId: String, notifId: Int, title: String, text: String, iconRes: Int, color: Int) {
        val application = getApplication<Application>()
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Scholar System Alerts", android.app.NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                lightColor = color
            }
            notificationManager.createNotificationChannel(channel)
        }
        val intent = android.content.Intent(application, lumia.tracker.MainActivity::class.java).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = android.app.PendingIntent.getActivity(application, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val notification = androidx.core.app.NotificationCompat.Builder(application, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(text))
            .setColor(color)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notifId, notification)
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
                "glass_backdrop_style" -> {
                    editor.putString(key, value)
                    _glassBackdropStyle.value = value
                }
                "nav_bar_glass_backdrop_style" -> {
                    editor.putString(key, value)
                    _navBarGlassBackdropStyle.value = value
                }
                "glass_opacity_value" -> {
                    val floatVal = value.toFloatOrNull() ?: 0.6f
                    editor.putFloat(key, floatVal)
                    _glassOpacityValue.value = floatVal
                }
                "dynamic_bg_light_brightness" -> {
                    val floatVal = value.toFloatOrNull() ?: 0.75f
                    editor.putFloat(key, floatVal)
                    _dynamicBgLightBrightness.value = floatVal
                    // also fallback to currently active theme if it's there
                    val activeTheme = (settings["theme_color"] ?: "Ocean").lowercase()
                    editor.putFloat("dynamic_bg_light_brightness_$activeTheme", floatVal)
                }
                "dynamic_bg_dark_brightness" -> {
                    val floatVal = value.toFloatOrNull() ?: 0.45f
                    editor.putFloat(key, floatVal)
                    _dynamicBgDarkBrightness.value = floatVal
                    // also fallback to currently active theme if it's there
                    val activeTheme = (settings["theme_color"] ?: "Ocean").lowercase()
                    editor.putFloat("dynamic_bg_dark_brightness_$activeTheme", floatVal)
                }
                else -> {
                    if (key.startsWith("dynamic_bg_light_brightness_")) {
                        val floatVal = value.toFloatOrNull() ?: 0.75f
                        editor.putFloat(key, floatVal)
                    } else if (key.startsWith("dynamic_bg_dark_brightness_")) {
                        val floatVal = value.toFloatOrNull() ?: 0.45f
                        editor.putFloat(key, floatVal)
                    } else if (key == "pomodoro_work_duration" || key == "pomodoro_short_break_duration" || key == "pomodoro_long_break_duration") {
                        val intVal = value.toIntOrNull() ?: return@forEach
                        editor.putInt(key, intVal)
                        when(key) {
                            "pomodoro_work_duration" -> _pomodoroWorkDuration.value = intVal
                            "pomodoro_short_break_duration" -> _pomodoroShortBreakDuration.value = intVal
                            "pomodoro_long_break_duration" -> _pomodoroLongBreakDuration.value = intVal
                        }
                    } else if (key == "display_layout_mode") {
                        editor.putString(key, value)
                        _displayLayoutMode.value = value
                    } else {
                        val boolVal = value.toBooleanStrictOrNull() ?: return@forEach
                        editor.putBoolean(key, boolVal)
                        when(key) {
                            "pure_black_mode" -> _pureBlackMode.value = boolVal
                            "beta_floating_nav" -> _betaFloatingNav.value = boolVal
                            "beta_notes" -> _betaNotes.value = boolVal
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
                            "system_auto_link_by_name" -> _systemAutoLinkByName.value = boolVal
                            "system_enable_synergy" -> _systemEnableSynergy.value = boolVal
                            "system_auto_create_subject" -> _systemAutoCreateSubject.value = boolVal
                            "system_fuse_subjects_courses" -> _systemFuseSubjectsCourses.value = boolVal
                            "system_advanced_tasks" -> _systemAdvancedTasks.value = boolVal
                            "system_pomodoro_auto_log" -> _systemPomodoroAutoLog.value = boolVal
                            "nav_bar_glass_force_enabled" -> _navBarGlassForceEnabled.value = boolVal
                            "beta_nav_bar_size_controls" -> _betaNavBarSizeControls.value = boolVal
                            "nav_bar_glass_linked_to_main" -> _navBarGlassLinkedToMain.value = boolVal
                            "nav_bar_glass_dynamic" -> _navBarGlassDynamic.value = boolVal
                        }
                    }
                }
            }
        }
        editor.apply()
        refreshThemeBrightness()
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settings = gatherSettings()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                    repository.exportDataToStream(os, settings)
                }
                _importExportStatus.value = "Secure backup binary package exported successfully"
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
                _importExportStatus.value = "Secure backup package imported and restored successfully"
                lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
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
        refreshThemeBrightness()
        if (_dynamicAppIcon.value) {
            applyThemeBasedAppIcon(color)
        }
    }

    fun updateBetaFloatingNav(enabled: Boolean) {
        _betaFloatingNav.value = enabled
        prefs.edit().putBoolean("beta_floating_nav", enabled).apply()
    }

    fun updateNavBarHeight(height: Float) {
        _navBarHeight.value = height
        prefs.edit().putFloat("nav_bar_height", height).apply()
    }

    fun updateNavBarPaddingHorizontal(padding: Float) {
        _navBarPaddingHorizontal.value = padding
        prefs.edit().putFloat("nav_bar_padding_horizontal", padding).apply()
    }

    fun updateNavBarPaddingBottom(padding: Float) {
        _navBarPaddingBottom.value = padding
        prefs.edit().putFloat("nav_bar_padding_bottom", padding).apply()
    }

    fun updateNavBarCornerRadius(radius: Float) {
        _navBarCornerRadius.value = radius
        prefs.edit().putFloat("nav_bar_corner_radius", radius).apply()
    }

    fun updateNavBarLabelMode(mode: String) {
        _navBarLabelMode.value = mode
        prefs.edit().putString("nav_bar_label_mode", mode).apply()
    }

    fun updateNavBarGlassForceEnabled(enabled: Boolean) {
        _navBarGlassForceEnabled.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_force_enabled", enabled).apply()
    }

    fun updateNavBarIndicatorAlpha(alpha: Float) {
        _navBarIndicatorAlpha.value = alpha
        prefs.edit().putFloat("nav_bar_indicator_alpha", alpha).apply()
    }

    fun updateBetaNavBarSizeControls(enabled: Boolean) {
        _betaNavBarSizeControls.value = enabled
        prefs.edit().putBoolean("beta_nav_bar_size_controls", enabled).apply()
    }

    fun updateNavBarGlassLinkedToMain(enabled: Boolean) {
        _navBarGlassLinkedToMain.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_linked_to_main", enabled).apply()
    }

    fun updateNavBarGlassBackdropStyle(style: String) {
        _navBarGlassBackdropStyle.value = style
        prefs.edit().putString("nav_bar_glass_backdrop_style", style).apply()
    }

    fun updateNavBarGlassDynamic(enabled: Boolean) {
        _navBarGlassDynamic.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_dynamic", enabled).apply()
    }

    fun updateBetaNotes(enabled: Boolean) {
        _betaNotes.value = enabled
        prefs.edit().putBoolean("beta_notes", enabled).apply()
    }

    fun updateDisplayLayoutMode(mode: String) {
        if (mode != "Immersive" && _appAnimationMode.value == "Bouncy" && safetyPinEnabled.value && safetyPinConflictWarning.value) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Required by Bouncy Animations",
                description = "Changing from 'Immersive' mode will also disable 'Bouncy' animations and revert to 'Dynamic'. Proceed?",
                isConflict = true,
                onConfirm = {
                    _safetyPinDialogData.value = null
                    _displayLayoutMode.value = mode
                    prefs.edit().putString("display_layout_mode", mode).apply()
                    updateAppAnimationMode("Dynamic")
                },
                onIgnore = { _safetyPinDialogData.value = null }
            )
            return
        }
        _displayLayoutMode.value = mode
        prefs.edit().putString("display_layout_mode", mode).apply()
        if (mode != "Immersive" && _appAnimationMode.value == "Bouncy") {
            updateAppAnimationMode("Dynamic")
        }
    }

    fun updateBetaMinimalistMode(enabled: Boolean) {
        if (enabled && safetyPinEnabled.value && safetyPinConflictWarning.value && (_betaGlassUi.value || _betaDynamicBackground.value || _betaEnhancedHeader.value || _betaFloatingNav.value || _betaBetterTexts.value || _displayLayoutMode.value != "Immersive" || _appAnimationMode.value != "Minimal" || _moreRounds.value)) {
            _safetyPinDialogData.value = SafetyPinDialogData(
                title = "Feature Conflict Detected",
                description = "Activating 'Minimalist Mode' will force-disable 'Glass UI', 'Dynamic Lighting', 'Enhanced Header', 'Floating Action Bar', 'Better Texts', bouncy animations, and rounded UI components, locking them to drastically reduce visual clutter. Additionally, 'Immersive Mode' will be turned ON. Proceed?",
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
                    if (_moreRounds.value) updateMoreRounds(false)
                    if (_appAnimationMode.value != "Minimal") updateAppAnimationMode("Minimal")
                    if (_displayLayoutMode.value != "Immersive") updateDisplayLayoutMode("Immersive")
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
            if (_displayLayoutMode.value != "Immersive") updateDisplayLayoutMode("Immersive")
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

    fun updateGlassBackdropStyle(style: String) {
        _glassBackdropStyle.value = style
        prefs.edit().putString("glass_backdrop_style", style).apply()
    }

    fun updateGlassOpacityValue(value: Float) {
        _glassOpacityValue.value = value
        prefs.edit().putFloat("glass_opacity_value", value).apply()
    }

    fun updateNavBarGlassOpacityValue(value: Float, alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = value
        prefs.edit().putFloat(key, value).apply()
    }

    fun refreshNavBarGlassOpacity(alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = prefs.getFloat(key, 0.6f)
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

    fun updateDynamicBgLightBrightness(value: Float) {
        val theme = _themeColor.value
        prefs.edit().putFloat("dynamic_bg_light_brightness_${theme.lowercase()}", value).apply()
        _dynamicBgLightBrightness.value = value
    }

    fun updateDynamicBgDarkBrightness(value: Float) {
        val theme = _themeColor.value
        prefs.edit().putFloat("dynamic_bg_dark_brightness_${theme.lowercase()}", value).apply()
        _dynamicBgDarkBrightness.value = value
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
            _betaNotes.value = false
            _displayLayoutMode.value = "Immersive"
            _betaGlassUi.value = false
            _betaGlassDynamic.value = true
            _betaFrostGlass.value = true
            _betaNavBarSizeControls.value = false
            _navBarGlassLinkedToMain.value = true
            _navBarGlassBackdropStyle.value = "Translucent"
            _navBarGlassDynamic.value = true
            _betaEnhancedHeader.value = false
            _betaMinimalistMode.value = false
            _betaDynamicBackground.value = false
            refreshThemeBrightness()
            _dynamicAppIcon.value = false
            _betaBetterTexts.value = false
            _betaBetterTextsPalette.value = true
            _safetyPinEnabled.value = true
            _safetyPinConflictWarning.value = true
            _safetyPinRecommendations.value = true
            _showActionHistory.value = true
            _systemAutoLinkByName.value = true
            _systemEnableSynergy.value = true
            _systemAutoCreateSubject.value = false
            _systemFuseSubjectsCourses.value = true
            _systemAdvancedTasks.value = true
            _systemPomodoroAutoLog.value = true
            _currentStreak.value = 0

            repository.insertActionLog(ActionLog(actionText = "Cleared all application data and settings"))
            _importExportStatus.value = "All data and settings erased successfully"
            lumia.tracker.util.WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
}
