package ovrrup.lumia.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class AodAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponentName = "${context.packageName}/${AodAccessibilityService::class.java.name}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServicesSetting.split(':').any {
                it.equals(expectedComponentName, ignoreCase = true)
            }
        }
    }
}
