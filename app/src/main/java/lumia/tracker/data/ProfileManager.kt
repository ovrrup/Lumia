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
        val json = prefs.getString("profiles_json", null) ?: return initDefaultProfile()
        return try {
            profileAdapter.fromJson(json) ?: initDefaultProfile()
        } catch (e: Exception) {
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
        prefs.edit().putString("profiles_json", profileAdapter.toJson(profiles)).apply()
    }

    fun addProfile(name: String, avatar: String, alias: String = "", starterTheme: String = "") {
        val list = getAllProfiles().toMutableList()
        val newProfile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme
        )
        list.add(newProfile)
        saveProfiles(list)
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
        prefs.edit().putString("active_profile_id", id).apply()
    }

    fun getActiveProfile(): UserProfile {
        val id = getActiveProfileId()
        return getAllProfiles().find { it.id == id } ?: getAllProfiles().first()
    }
}
