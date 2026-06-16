package lumia.tracker.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class AodAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AodAccessibilityService? = null
            private set

        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponentName = "${context.packageName}/${AodAccessibilityService::class.java.canonicalName}"
            val settingsStr = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return settingsStr?.contains(expectedComponentName) == true || settingsStr?.contains(AodAccessibilityService::class.java.simpleName) == true
        }

        fun lockScreen(): Boolean {
            val currentService = instance
            if (currentService != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return currentService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op - we only utilize global actions
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }
}
