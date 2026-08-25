package lumia.tracker.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import lumia.tracker.model.UserProfile
import java.util.UUID

class ProfileManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
    
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val profileListType = Types.newParameterizedType(List::class.java, UserProfile::class.java)
    private val profileAdapter = moshi.adapter<List<UserProfile>>(profileListType)

    private val lock = Any()

    fun getAllProfiles(): List<UserProfile> = synchronized(lock) {
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
            isDefault = true
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize profiles", e)
        }
    }

    fun addProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        val newId = UUID.randomUUID().toString()
        val newProfile = UserProfile(
            id = newId,
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme,
            isDefault = false
        )
        list.add(newProfile)
        saveProfilesInternal(list)
        return newId
    }
    
    fun updateProfile(updated: UserProfile) = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            list[index] = updated
            saveProfilesInternal(list)
        }
    }

    fun deleteProfile(id: String) = synchronized(lock) {
        val list = getAllProfiles().toMutableList()
        if (list.size > 1) {
            list.removeAll { it.id == id }
            if (list.none { it.isDefault }) {
                list[0] = list[0].copy(isDefault = true)
            }
            saveProfilesInternal(list)
            if (getActiveProfileId() == id) {
                setActiveProfileId(list.first().id)
            }
        }
    }

    fun getActiveProfileId(): String {
        return prefs.getString(KEY_ACTIVE_PROFILE_ID, DEFAULT_PROFILE_ID) ?: DEFAULT_PROFILE_ID
    }

    fun setActiveProfileId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, id).commit()
    }

    fun getActiveProfile(): UserProfile {
        val activeId = getActiveProfileId()
        val profiles = getAllProfiles()
        return profiles.find { it.id == activeId } ?: profiles.firstOrNull() ?: UserProfile(
            id = DEFAULT_PROFILE_ID,
            name = "Main User",
            avatarEmoji = "A",
            isDefault = true
        )
    }

    fun getProfilePrefs(id: String = getActiveProfileId()): SharedPreferences {
        val prefName = if (id == DEFAULT_PROFILE_ID) "lumia_prefs" else "lumia_prefs_$id"
        return appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "ProfileManager"
        const val DEFAULT_PROFILE_ID = "DEFAULT"
        private const val KEY_PROFILES_JSON = "profiles_json"
        private const val KEY_PROFILES_BACKUP = "profiles_json_backup"
        private const val KEY_PROFILES_CORRUPTED = "profiles_json_corrupted"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }
}
