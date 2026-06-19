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
        // B-Grade (Utility) - 1 in 15 rarity (6.67%)
        PlusFeature("feat_notification_tone", "Custom Notification Tones", "Select custom sound profiles and alert frequencies for Pomodoro sessions.", 100, 5000, 150, 5, "Customization", "B"),
        PlusFeature("feat_theme_overlay", "Thematic Layout Overlays", "Inject frosted glass color overlays into the main screen layout.", 150, 7500, 200, 10, "Appearance", "B"),
        
        // A-Grade (Standard Plus) - 1 in 30 rarity (3.33%)
        PlusFeature("feat_theme_pack", "Theme Pack Expansion", "Unlocks Pastel, Matrix, and Cyberpunk premium aesthetic dynamic colorways.", 300, 15000, 350, 15, "Appearance", "A"),
        PlusFeature("feat_screen_layout", "Advanced Screen Layouts", "Freely adjust layout paddings, grids, and dock position in the primary view.", 400, 20000, 450, 20, "Customization", "A"),
        
        // A+-Grade (Advanced Scholar) - 1 in 50 rarity (2.0%)
        PlusFeature("feat_custom_theme", "Custom Theme Engine", "Design your own custom Material 3 themed color codes from hex inputs.", 600, 30000, 600, 30, "Appearance", "A+"),
        PlusFeature("feat_leaderboard", "Study Leaderboard", "Compare study performance, daily sessions, and focus times with friends or local peer tiers.", 700, 35000, 700, 40, "Analytics", "A+"),
        
        // S-Grade (High Premium) - 1 in 100 rarity (1.0%)
        PlusFeature("feat_minimal_ui", "Minimalist Mode", "A complete distraction-free minimalist alternative launcher layout.", 1000, 50000, 1000, 50, "Appearance", "S"),
        PlusFeature("feat_true_aod", "True Always-On Display", "An overlay-based screen-saver clock for physical screen and hardware protection during deep focus.", 1500, 75000, 1500, 75, "Experimental", "S"),
        
        // SS-Grade (Ultra Premium / Scholar God) - 1 in 250 rarity (0.4%)
        PlusFeature("feat_advanced_data", "DB Inspector & Diagnostics", "Run local sqlite schema inspections, defragment indexes, clean orphans, and export filtered CSV packages.", 2000, 100000, 2000, 100, "Analytics", "SS"),
        PlusFeature("feat_experimental", "Mad Scientist Lab", "Unlock highly experimental fluid UI components, customizable live wave particle visualizers, and premium soundscapes.", 3000, 150000, 3000, 150, "Experimental", "SS")
    )
}
