package lumia.tracker.service

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent

class AodAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AodAccessibilityService? = null
            private set

        fun isServiceEnabled(context: Context): Boolean {
            return try {
                val expectedComponentName = "${context.packageName}/${AodAccessibilityService::class.java.canonicalName}"
                val settingsStr = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false
                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(settingsStr)
                while (colonSplitter.hasNext()) {
                    val componentName = colonSplitter.next()
                    if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                        componentName.contains(AodAccessibilityService::class.java.simpleName, ignoreCase = true)
                    ) {
                        return true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }

        fun lockScreen(): Boolean {
            val service = instance ?: return false
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    val dpm = service.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                    try {
                        dpm?.lockNow()
                        true
                    } catch (e: Exception) {
                        service.performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            } catch (e: Exception) {
                false
            }
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
