package lumia.tracker.model

data class PlusFeature(
    val id: String,
    val name: String,
    val description: String,
    val pricePoints: Int,
    val requiredLevel: Int,
    val category: String, // "Appearance", "Customization", "Experimental", "Analytics"
    val rank: String // "Bronze", "Silver", "Gold", "Diamond"
)

object PlusShop {
    val features = listOf(
        PlusFeature("feat_theme_pack", "Theme Pack Expansion", "Unlocks Pastel, Matrix, and Cyberpunk themes.", 200, 2, "Appearance", "Silver"),
        PlusFeature("feat_custom_theme", "Custom Themes", "Create your own unique themes completely from scratch.", 500, 5, "Appearance", "Gold"),
        PlusFeature("feat_minimal_ui", "Minimalist Mode", "A distraction-free minimal UI with zero bloat.", 1000, 10, "Appearance", "Diamond"),
        PlusFeature("feat_screen_layout", "Advanced Screen Layouts", "Freely adjust layout paddings, grids, and dock position.", 300, 3, "Customization", "Silver"),
        PlusFeature("feat_true_aod", "True AOD", "An always-on display mode optimized for OLED screens.", 1500, 15, "Experimental", "Diamond"),
        PlusFeature("feat_notification_tone", "Custom Notification Tones", "Select custom sounds for Pomodoro and task completions.", 250, 2, "Customization", "Bronze"),
        PlusFeature("feat_leaderboard", "Study Leaderboard", "Compare study performance, daily sessions, and focus times with friends or local peer tiers.", 350, 3, "Analytics", "Gold"),
        PlusFeature("feat_experimental", "Mad Scientist Lab", "Unlock highly experimental and test features.", 2000, 20, "Experimental", "Diamond")
    )
}
