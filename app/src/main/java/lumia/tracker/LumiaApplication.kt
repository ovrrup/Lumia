package lumia.tracker

import android.app.Application
import lumia.tracker.util.AutoCrashBackupManager
import lumia.tracker.util.LogDog

class LumiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AutoCrashBackupManager.setup(this)
        LogDog.setup(this)
    }
}
