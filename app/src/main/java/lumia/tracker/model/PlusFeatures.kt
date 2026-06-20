package lumia.tracker.model

data class PlusFeature(
    val id: String,
    val name: String,
    val description: String,
    val pricePoints: Int,
    val priceCredits: Int,
    val rentCostCredits: Int,
    val requiredLevel: Int,
    val category: String, // "Appearance", "Customization", "Experimental", "Analytics"
    val rank: String // "B", "A", "A+", "S", "SS" (Grades)
)

object PlusShop {
    val features = listOf(
        // Tier 1 (Utility)
        PlusFeature("feat_notification_tone", "Custom Notification Tones", "Select custom sound profiles and alert frequencies for Pomodoro sessions.", 100, 5000, 150, 2, "Customization", "B"),
        PlusFeature("feat_theme_overlay", "Thematic Layout Overlays", "Add a color overlay to the main screen layout.", 150, 7500, 200, 4, "Appearance", "B"),
        PlusFeature("feat_ui_icon", "UI-based Launcher Icon", "Match home screen app icon style with the active Lumia color scheme.", 200, 10000, 250, 6, "Customization", "B"),
        
        // Tier 2 (Standard Plus)
        PlusFeature("feat_theme_pack", "Theme Pack Expansion", "Unlocks Pastel, Matrix, and Cyberpunk themes.", 300, 15000, 350, 3, "Appearance", "A"),
        PlusFeature("feat_screen_layout", "Advanced Screen Layouts", "Adjust layout paddings, grids, and dock position in the primary view.", 400, 20000, 450, 5, "Customization", "A"),
        PlusFeature("feat_enhanced_blur", "Enhanced Blur Navigation", "Apply a polished satin translucent backdrop to primary navigation header.", 350, 18000, 400, 10, "Appearance", "A"),
        
        // Tier 3 (Advanced Features)
        PlusFeature("feat_custom_theme", "Custom Theme Engine", "Design your own custom Material 3 themed color codes.", 600, 30000, 600, 12, "Appearance", "A+"),
        PlusFeature("feat_leaderboard", "Study Leaderboard", "Compare study performance, daily sessions, and focus times with friends.", 700, 35000, 700, 5, "Analytics", "A+"),
        PlusFeature("feat_dynamic_lighting", "Dynamic Lighting Background", "Soft, vibrant animated background gradient shifts.", 500, 25000, 500, 13, "Appearance", "A+"),
        PlusFeature("feat_animations", "Advanced Animations", "Change application animation quality.", 550, 28000, 550, 14, "Appearance", "A+"),
        
        // Tier 4 (Premium Features)
        PlusFeature("feat_minimal_ui", "Minimalist Mode", "A distraction-free minimalist layout.", 1000, 50000, 1000, 15, "Appearance", "S"),
        PlusFeature("feat_true_aod", "Always-On Display", "An overlay-based screen-saver clock for deep focus.", 1500, 75000, 1500, 20, "Experimental", "S"),
        
        // Tier 5 (Ultra Features)
        PlusFeature("feat_advanced_data", "DB Inspector & Diagnostics", "Run local database inspections and export data.", 2000, 100000, 2000, 28, "Analytics", "SS"),
        PlusFeature("feat_experimental", "Experimental Lab", "Unlock experimental components, visualizers, and soundscapes.", 3000, 150000, 3000, 25, "Experimental", "SS")
    )
}
