package lumia.tracker.util

import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import lumia.tracker.R
import lumia.tracker.data.ProfileManager

object WidgetThemeHelper {

    fun applyTheme(context: Context, views: RemoteViews) {
        val profMgr = ProfileManager(context)
        val profile = profMgr.getActiveProfile()
        val themeName = profile.starterTheme
        
        // Define base colors
        var bgColor = Color.parseColor("#1E293B")
        var primaryColor = Color.parseColor("#3197D6")
        var textColor = Color.parseColor("#FFFFFF")
        var subtitleColor = Color.parseColor("#94A3B8")
        
        when (themeName) {
            "Custom" -> {
                val prefs = profMgr.getProfilePrefs()
                val customPrimaryPref = prefs.getString("custom_primary_light", "#3197D6") ?: "#3197D6"
                val customPrimaryColor = try { Color.parseColor(customPrimaryPref) } catch(e: Exception) { Color.parseColor("#3197D6") }
                bgColor = Color.parseColor("#121212") // Just something neutral dark
                primaryColor = customPrimaryColor
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#94A3B8")
            }
            "Ocean" -> {
                bgColor = Color.parseColor("#0B121A")
                primaryColor = Color.parseColor("#9ECAFF")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#94A3B8")
            }
            "Emerald" -> {
                bgColor = Color.parseColor("#09120D")
                primaryColor = Color.parseColor("#79DC9C")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#A1CED9")
            }
            "Gold" -> {
                bgColor = Color.parseColor("#13110A")
                primaryColor = Color.parseColor("#FABD00")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#D8C4A0")
            }
            "Rose" -> {
                bgColor = Color.parseColor("#170D0E")
                primaryColor = Color.parseColor("#FFFFB3B4")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#E7BDBE")
            }
            "Sage" -> {
                bgColor = Color.parseColor("#0F140E")
                primaryColor = Color.parseColor("#A1D39A")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#BACCB3")
            }
            "Twilight" -> {
                bgColor = Color.parseColor("#111016")
                primaryColor = Color.parseColor("#C4C0FF")
                textColor = Color.parseColor("#E2E8F0")
                subtitleColor = Color.parseColor("#C7C4DC")
            }
        }
        
        // Use RemoteViews.setInt to set background tint/color filters if possible.
        // For backwards compatibility without SDK checks, updating text colors is safe.
        try {
            // Apply background color to root or elements if supported. 
            // We use a base drawable with setColorFilter on an ImageView since setBackgroundColor replaces rounded corners.
            views.setInt(R.id.widget_bg_image, "setColorFilter", bgColor)
            
            // Buttons or accents
            views.setInt(R.id.widget_btn_mode_image, "setColorFilter", primaryColor)
            views.setInt(R.id.widget_btn_app_image, "setColorFilter", primaryColor)
            
            // Texts
            views.setTextColor(R.id.widget_title, textColor)
            views.setTextColor(R.id.widget_subtitle, subtitleColor)
            views.setTextColor(R.id.widget_pomo_time, primaryColor)
            views.setTextColor(R.id.widget_pomo_state, subtitleColor)
            views.setTextColor(R.id.widget_no_tasks, subtitleColor)
            views.setTextColor(R.id.widget_no_classes, subtitleColor)
            views.setTextColor(R.id.widget_task_item_1, textColor)
            views.setTextColor(R.id.widget_task_item_2, textColor)
            views.setTextColor(R.id.widget_task_item_3, textColor)
            views.setTextColor(R.id.widget_item_1, textColor)
            views.setTextColor(R.id.widget_item_2, textColor)
            views.setTextColor(R.id.widget_item_3, textColor)

            // Make sure button texts contrast with the primaryColor
            views.setTextColor(R.id.widget_btn_text_1, Color.parseColor("#111111"))
            views.setTextColor(R.id.widget_btn_text_2, Color.parseColor("#111111"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
