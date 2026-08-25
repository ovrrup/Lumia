package lumia.tracker.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lumia.tracker.model.UserProfile
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.io.File
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ProfileManager - Multi-profile lifecycle engine for Lumia Academic Tracker.
 * Manages isolated tenant workspaces, preferences routing, metadata persistence,
 * reactive state observation, and storage lifecycle cleanup.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Multi-profile lifecycle, reactive workspace switching, and isolated tenant preferences",
    category = "Data"
)
class ProfileManager(context: Context) {
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_GLOBAL_PROFILES, Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val profileListType = Types.newParameterizedType(List::class.java, UserProfile::class.java)
    private val profileAdapter = moshi.adapter<List<UserProfile>>(profileListType)

    private val lock = Any()

    @Volatile
    private var cachedProfiles: List<UserProfile>? = null

    private val _profilesFlow = MutableStateFlow<List<UserProfile>>(emptyList())
    val profilesFlow: StateFlow<List<UserProfile>> = _profilesFlow.asStateFlow()

    private val _activeProfileFlow = MutableStateFlow(
        UserProfile(id = DEFAULT_PROFILE_ID, name = "Main User", avatarEmoji = "A", isDefault = true)
    )
    val activeProfileFlow: StateFlow<UserProfile> = _activeProfileFlow.asStateFlow()

    private val profileChangeListeners = CopyOnWriteArrayList<(UserProfile) -> Unit>()

    init {
        synchronized(lock) {
            val initialList = loadProfilesInternal()
            cachedProfiles = initialList
            _profilesFlow.value = initialList
            _activeProfileFlow.value = resolveActiveProfileInternal(initialList)
        }
    }

    /**
     * Retrieves all available user profiles. Uses cached in-memory list when valid.
     */
    fun getAllProfiles(): List<UserProfile> = synchronized(lock) {
        cachedProfiles ?: loadProfilesInternal().also {
            cachedProfiles = it
            _profilesFlow.value = it
        }
    }

    private fun loadProfilesInternal(): List<UserProfile> {
        val json = prefs.getString(KEY_PROFILES_JSON, null)
        if (json.isNullOrBlank()) {
            return initDefaultProfile()
        }
        return try {
            val list = profileAdapter.fromJson(json)
            if (!list.isNullOrEmpty()) {
                prefs.edit().putString(KEY_PROFILES_BACKUP, json).apply()
                list
            } else {
                initDefaultProfile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse profiles_json, attempting backup recovery", e)
            prefs.edit().putString(KEY_PROFILES_CORRUPTED, json).apply()

            val backupJson = prefs.getString(KEY_PROFILES_BACKUP, null)
            if (!backupJson.isNullOrBlank()) {
                try {
                    val backupList = profileAdapter.fromJson(backupJson)
                    if (!backupList.isNullOrEmpty()) {
                        prefs.edit().putString(KEY_PROFILES_JSON, backupJson).apply()
                        return backupList
                    }
                } catch (be: Exception) {
                    Log.e(TAG, "Failed to parse profiles_json_backup", be)
                }
            }
            initDefaultProfile()
        }
    }

    private fun initDefaultProfile(): List<UserProfile> {
        val defaultProfile = UserProfile(
            id = DEFAULT_PROFILE_ID,
            name = "Main User",
            avatarEmoji = "A",
            isDefault = true,
            createdAt = System.currentTimeMillis()
        )
        val list = listOf(defaultProfile)
        saveProfilesInternal(list)
        setActiveProfileId(defaultProfile.id)
        return list
    }

    private fun saveProfilesInternal(profiles: List<UserProfile>) {
        try {
            val json = profileAdapter.toJson(profiles)
            prefs.edit()
                .putString(KEY_PROFILES_JSON, json)
                .putString(KEY_PROFILES_BACKUP, json)
                .commit()
            cachedProfiles = profiles
            _profilesFlow.value = profiles
            val active = resolveActiveProfileInternal(profiles)
            _activeProfileFlow.value = active
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize profiles", e)
        }
    }

    /**
     * Adds a new isolated academic workspace profile.
     */
    fun addProfile(
        name: String,
        avatar: String,
        alias: String = "",
        starterTheme: String = ""
    ): String = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        val newId = UUID.randomUUID().toString()
        val newProfile = UserProfile(
            id = newId,
            name = name.trim().ifBlank { "Workspace ${list.size + 1}" },
            avatarEmoji = avatar,
            alias = alias.trim(),
            starterTheme = starterTheme,
            isDefault = false,
            createdAt = System.currentTimeMillis()
        )
        list.add(newProfile)
        saveProfilesInternal(list)
        return newId
    }

    /**
     * Updates an existing profile's metadata.
     */
    fun updateProfile(updated: UserProfile) = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            list[index] = updated
            saveProfilesInternal(list)
            if (getActiveProfileId() == updated.id) {
                _activeProfileFlow.value = updated
                notifyProfileChanged(updated)
            }
        }
    }

    /**
     * Deletes a profile and cleans up all associated database files, preferences, and avatars.
     */
    fun deleteProfile(id: String, cleanupStorage: Boolean = true): Boolean = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        if (list.size <= 1) {
            Log.w(TAG, "Cannot delete the sole remaining profile.")
            return false
        }

        val toDelete = list.find { it.id == id } ?: return false
        list.removeAll { it.id == id }

        // Ensure at least one profile is marked as default
        if (list.none { it.isDefault }) {
            list[0] = list[0].copy(isDefault = true)
        }

        saveProfilesInternal(list)

        val wasActive = getActiveProfileId() == id
        if (wasActive) {
            val fallbackId = list.firstOrNull { it.isDefault }?.id ?: list.first().id
            setActiveProfileId(fallbackId)
        }

        if (cleanupStorage) {
            cleanupProfileStorage(id, toDelete.avatarEmoji)
        }

        return true
    }

    private fun cleanupProfileStorage(profileId: String, avatarPath: String) {
        try {
            // Close and remove Room database instance
            AppDatabase.closeDatabase(profileId)
            AppDatabase.deleteDatabaseFiles(appContext, profileId)

            // Clear and delete SharedPreferences file
            val prefName = getPrefsNameForProfile(profileId)
            val profilePref = appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            profilePref.edit().clear().commit()

            val prefsFile = File(appContext.filesDir.parentFile, "shared_prefs/$prefName.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
            }

            // Cleanup local avatar image if stored in internal files
            if (avatarPath.startsWith("/") || avatarPath.startsWith("file://")) {
                val cleanPath = avatarPath.removePrefix("file://")
                val avatarFile = File(cleanPath)
                if (avatarFile.exists() && cleanPath.contains("avatars")) {
                    avatarFile.delete()
                }
            }
            Log.i(TAG, "Cleaned up storage artifacts for profile $profileId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up profile storage for $profileId", e)
        }
    }

    /**
     * Returns the active profile ID.
     */
    fun getActiveProfileId(): String {
        return prefs.getString(KEY_ACTIVE_PROFILE_ID, DEFAULT_PROFILE_ID) ?: DEFAULT_PROFILE_ID
    }

    /**
     * Switches the active profile and updates reactive state flows.
     */
    fun setActiveProfileId(id: String) {
        synchronized(lock) {
            prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, id).commit()
            val active = getActiveProfile()
            _activeProfileFlow.value = active
            notifyProfileChanged(active)
        }
    }

    /**
     * Retrieves the currently active UserProfile.
     */
    fun getActiveProfile(): UserProfile = synchronized(lock) {
        val activeId = getActiveProfileId()
        val profiles = getAllProfiles()
        resolveActiveProfileInternal(profiles, activeId)
    }

    private fun resolveActiveProfileInternal(
        profiles: List<UserProfile>,
        activeId: String = getActiveProfileId()
    ): UserProfile {
        return profiles.find { it.id == activeId }
            ?: profiles.find { it.isDefault }
            ?: profiles.firstOrNull()
            ?: UserProfile(
                id = DEFAULT_PROFILE_ID,
                name = "Main User",
                avatarEmoji = "A",
                isDefault = true
            )
    }

    /**
     * Retrieves a profile by its ID.
     */
    fun getProfileById(id: String): UserProfile? = synchronized(lock) {
        return getAllProfiles().find { it.id == id }
    }

    /**
     * Checks if a profile ID exists in storage.
     */
    fun profileExists(id: String): Boolean = synchronized(lock) {
        return getAllProfiles().any { it.id == id }
    }

    /**
     * Clones a profile configuration and creates a new profile.
     */
    fun cloneProfile(sourceProfileId: String, newName: String): String? = synchronized(lock) {
        val source = getProfileById(sourceProfileId) ?: return null
        val newId = UUID.randomUUID().toString()
        val cloned = source.copy(
            id = newId,
            name = newName.trim().ifBlank { "${source.name} (Copy)" },
            isDefault = false,
            createdAt = System.currentTimeMillis()
        )
        val list = getAllProfiles().toMutableList()
        list.add(cloned)
        saveProfilesInternal(list)
        return newId
    }

    /**
     * Returns the dedicated SharedPreferences instance for a given profile ID.
     */
    fun getProfilePrefs(id: String = getActiveProfileId()): SharedPreferences {
        val prefName = getPrefsNameForProfile(id)
        return appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    /**
     * Returns the database filename for a given profile ID.
     */
    fun getDatabaseNameForProfile(id: String): String {
        return if (id == DEFAULT_PROFILE_ID) {
            DATABASE_NAME_DEFAULT
        } else {
            "scholar_sync_$id"
        }
    }

    /**
     * Returns the SharedPreferences filename for a given profile ID.
     */
    fun getPrefsNameForProfile(id: String): String {
        return if (id == DEFAULT_PROFILE_ID) {
            PREFS_NAME_DEFAULT
        } else {
            "lumia_prefs_$id"
        }
    }

    /**
     * Registers a profile change listener.
     */
    fun addOnProfileChangeListener(listener: (UserProfile) -> Unit) {
        if (!profileChangeListeners.contains(listener)) {
            profileChangeListeners.add(listener)
        }
    }

    /**
     * Unregisters a profile change listener.
     */
    fun removeOnProfileChangeListener(listener: (UserProfile) -> Unit) {
        profileChangeListeners.remove(listener)
    }

    private fun notifyProfileChanged(profile: UserProfile) {
        for (listener in profileChangeListeners) {
            try {
                listener(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying profile change listener", e)
            }
        }
    }

    companion object {
        private const val TAG = "ProfileManager"
        const val DEFAULT_PROFILE_ID = "DEFAULT"
        const val PREFS_GLOBAL_PROFILES = "global_profiles"
        const val PREFS_NAME_DEFAULT = "lumia_prefs"
        const val DATABASE_NAME_DEFAULT = "scholar_sync_database"

        private const val KEY_PROFILES_JSON = "profiles_json"
        private const val KEY_PROFILES_BACKUP = "profiles_json_backup"
        private const val KEY_PROFILES_CORRUPTED = "profiles_json_corrupted"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }
}
