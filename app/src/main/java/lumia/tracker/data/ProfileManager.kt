package lumia.tracker.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import lumia.tracker.model.UserProfile
import java.util.UUID

class ProfileManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val profileListType = Types.newParameterizedType(List::class.java, UserProfile::class.java)
    private val profileAdapter = moshi.adapter<List<UserProfile>>(profileListType)

    fun getAllProfiles(): List<UserProfile> {
        val json = prefs.getString("profiles_json", null)
        if (json.isNullOrEmpty()) {
            return initDefaultProfile()
        }
        return try {
            val list = profileAdapter.fromJson(json)
            if (!list.isNullOrEmpty()) {
                val sanitized = list.map { prof ->
                    UserProfile(
                        id = (prof.id as? String) ?: java.util.UUID.randomUUID().toString(),
                        name = (prof.name as? String) ?: "Main User",
                        avatarEmoji = (prof.avatarEmoji as? String) ?: "A",
                        isDefault = (prof.isDefault as? Boolean) == true || prof.id == "DEFAULT",
                        starterTheme = (prof.starterTheme as? String) ?: "Default",
                        alias = (prof.alias as? String) ?: "",
                        createdAt = (prof.createdAt as? Long) ?: System.currentTimeMillis(),
                        avatarBase64 = prof.avatarBase64 as? String
                    )
                }
                // Successfully parsed! Back it up.
                prefs.edit().putString("profiles_json_backup", json).apply()
                sanitized
            } else {
                initDefaultProfile()
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileManager", "Failed to parse profiles_json. Attempting recovery...", e)
            // Save corrupted for safety
            prefs.edit().putString("profiles_json_corrupted", json).apply()
            
            // Try to load backup
            val backupJson = prefs.getString("profiles_json_backup", null)
            if (!backupJson.isNullOrEmpty()) {
                try {
                    val backupList = profileAdapter.fromJson(backupJson)
                    if (!backupList.isNullOrEmpty()) {
                        // Restore backup to active profiles
                        prefs.edit().putString("profiles_json", backupJson).apply()
                        return backupList
                    }
                } catch (be: Exception) {
                    android.util.Log.e("ProfileManager", "Failed to parse profiles_json_backup", be)
                }
            }
            
            initDefaultProfile()
        }
    }

    private fun initDefaultProfile(): List<UserProfile> {
        val defaultProfile = UserProfile(
            id = "DEFAULT",
            name = "Main User",
            avatarEmoji = "A", // Changed from emoji
            isDefault = true
        )
        val list = listOf(defaultProfile)
        saveProfiles(list)
        setActiveProfileId(defaultProfile.id)
        return list
    }

    private fun saveProfiles(profiles: List<UserProfile>) {
        prefs.edit().putString("profiles_json", profileAdapter.toJson(profiles)).commit()
    }

    fun addProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String {
        val list = getAllProfiles().toMutableList()
        val newId = UUID.randomUUID().toString()
        val newProfile = UserProfile(
            id = newId,
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme
        )
        list.add(newProfile)
        saveProfiles(list)
        return newId
    }
    
    fun updateProfile(updated: UserProfile) {
        val list = getAllProfiles().toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            list[index] = updated
            saveProfiles(list)
        }
    }

    fun deleteProfile(id: String) {
        val list = getAllProfiles().toMutableList()
        if (list.size > 1) {
            list.removeAll { it.id == id }
            saveProfiles(list)
            if (getActiveProfileId() == id) {
                setActiveProfileId(list.first().id)
            }
        }
    }

    fun getActiveProfileId(): String {
        return prefs.getString("active_profile_id", "DEFAULT") ?: "DEFAULT"
    }

    fun setActiveProfileId(id: String) {
        prefs.edit().putString("active_profile_id", id).commit()
    }

    fun getActiveProfile(): UserProfile {
        
        val activeId = getActiveProfileId()
        return getAllProfiles().find { it.id == activeId } ?: getAllProfiles().first()
    }

    fun getProfilePrefs(id: String = getActiveProfileId()): SharedPreferences {
        
        val prefName = if (id == "DEFAULT") "lumia_prefs" else "lumia_prefs_$id"
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }
}
