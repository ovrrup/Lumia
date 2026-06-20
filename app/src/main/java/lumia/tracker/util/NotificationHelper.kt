package lumia.tracker.util

import android.content.Context
import android.graphics.Color
import lumia.tracker.R

object NotificationHelper {
    /**
     * Linked with App Icon: Returns the monochrome version of the launcher icon
     * to serve as the notification small icon, maintaining visual consistency.
     */
    fun getSmallIcon(): Int = R.drawable.ic_launcher_monochrome

    /**
     * Linked with Theme Color: Returns the current primary theme color
     * to tint the notification icon and elements, matching the app's aesthetic.
     */
    fun getColor(context: Context): Int {
        val profMgr = lumia.tracker.data.ProfileManager(context)
        val prefs = profMgr.getProfilePrefs()
        val themeColor = prefs.getString("theme_color", "Ocean") ?: "Ocean"
        val customPrimary = prefs.getString("custom_primary", "#3197D6") ?: "#3197D6"
        
        return when (themeColor) {
            "Ocean" -> Color.parseColor("#0061A4")
            "Emerald" -> Color.parseColor("#006D36")
            "Gold" -> Color.parseColor("#7D5700")
            "Rose" -> Color.parseColor("#BF0031") 
            "Sage" -> Color.parseColor("#3B6939")
            "Twilight" -> Color.parseColor("#5B53A8")
            "Custom" -> {
                try { Color.parseColor(customPrimary) } catch(e: Exception) { Color.parseColor("#3197D6") }
            }
            "Dynamic" -> {
                // Approximate Ocean Primary for dynamic fallback in background contexts
                Color.parseColor("#0061A4")
            }
            else -> Color.parseColor("#0061A4")
        }
    }
}
