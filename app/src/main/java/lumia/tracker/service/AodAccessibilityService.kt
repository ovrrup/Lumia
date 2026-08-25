package lumia.tracker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * AodAccessibilityService - Provides privileged screen locking and accessibility overlay capabilities
 * for True Always-On Display (AOD) on Android 9+ through Android 15+.
 */
@ValueScore(
    score = 92,
    importance = Importance.CRITICAL,
    description = "Accessibility service enabling true AOD lock screen actions and accessibility overlay window placement",
    category = "Accessibility"
)
class AodAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AodAccessibilityService"

        @Volatile
        var instance: AodAccessibilityService? = null
            private set

        /**
         * Checks if this accessibility service is currently enabled in Android system settings.
         */
        fun isServiceEnabled(context: Context): Boolean {
            return try {
                val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                if (am != null) {
                    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                    for (service in enabledServices) {
                        val serviceInfo = service.resolveInfo?.serviceInfo
                        if (serviceInfo != null &&
                            serviceInfo.packageName == context.packageName &&
                            serviceInfo.name == AodAccessibilityService::class.java.name
                        ) {
                            return true
                        }
                    }
                }

                // Fallback check via Settings.Secure
                val expectedComponentName = "${context.packageName}/${AodAccessibilityService::class.java.name}"
                val expectedCanonicalName = "${context.packageName}/${AodAccessibilityService::class.java.canonicalName}"
                val settingsStr = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false

                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(settingsStr)
                while (colonSplitter.hasNext()) {
                    val componentName = colonSplitter.next()
                    if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                        componentName.equals(expectedCanonicalName, ignoreCase = true) ||
                        componentName.contains(AodAccessibilityService::class.java.simpleName, ignoreCase = true)
                    ) {
                        return true
                    }
                }
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking accessibility service status", e)
                false
            }
        }

        /**
         * Performs global screen lock using accessibility API on Android 9+ (API 28+),
         * with fallback to DevicePolicyManager or home screen for older devices.
         */
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
                Log.e(TAG, "Failed to perform lockScreen global action", e)
                false
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "AodAccessibilityService connected and ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive accessibility service - only global actions and overlay tokens are utilized
    }

    override fun onInterrupt() {
        Log.w(TAG, "AodAccessibilityService interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (instance == this) {
            instance = null
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }
}
