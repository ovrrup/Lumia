package lumia.tracker.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import lumia.tracker.data.AppDatabase
import lumia.tracker.data.ProfileManager
import lumia.tracker.model.UserProfile
import lumia.tracker.util.ScholarCalendarWidgetProvider
import lumia.tracker.util.ScholarPomodoroWidgetProvider
import lumia.tracker.util.ScholarTasksWidgetProvider

class ScholarProfileAccountManager(
    private val application: Application,
    val profileManager: ProfileManager
) {
    val activeProfile = MutableStateFlow(profileManager.getActiveProfile())
    val allProfiles = MutableStateFlow(profileManager.getAllProfiles())

    fun refreshProfiles() {
        activeProfile.value = profileManager.getActiveProfile()
        allProfiles.value = profileManager.getAllProfiles()
    }

    fun switchProfileAndRestart(context: Context, id: String) {
        profileManager.setActiveProfileId(id)
        AppDatabase.clearInstances()

        val app = application
        app.sendBroadcast(Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            setComponent(android.content.ComponentName(app, ScholarTasksWidgetProvider::class.java))
        })
        app.sendBroadcast(Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            setComponent(android.content.ComponentName(app, ScholarCalendarWidgetProvider::class.java))
        })
        app.sendBroadcast(Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            setComponent(android.content.ComponentName(app, ScholarPomodoroWidgetProvider::class.java))
        })

        val intent = Intent(app, lumia.tracker.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        if (context is android.app.Activity) {
            context.finish()
        }
        app.startActivity(intent)
    }

    fun createProfile(name: String, avatar: String, alias: String = "", starterTheme: String = ""): String {
        val newId = profileManager.addProfile(name, avatar, alias, starterTheme)
        allProfiles.value = profileManager.getAllProfiles()
        return newId
    }

    fun setupFirstProfile(name: String, avatar: String, alias: String, starterTheme: String, onThemeUpdate: (String) -> Unit) {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(
            name = name,
            avatarEmoji = avatar,
            alias = alias,
            starterTheme = starterTheme
        )
        profileManager.updateProfile(updated)
        refreshProfiles()
        onThemeUpdate(starterTheme)
    }

    fun updateProfile(name: String, avatar: String, alias: String = "") {
        val current = profileManager.getActiveProfile()
        val updated = current.copy(
            name = name,
            avatarEmoji = avatar,
            alias = alias
        )
        profileManager.updateProfile(updated)
        refreshProfiles()
    }

    fun deleteProfile(context: Context, id: String) {
        val wasActive = profileManager.getActiveProfileId() == id
        profileManager.deleteProfile(id)
        allProfiles.value = profileManager.getAllProfiles()
        if (wasActive) {
            switchProfileAndRestart(context, profileManager.getActiveProfileId())
        }
    }
}
