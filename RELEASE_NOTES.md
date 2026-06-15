# Lumia Release Notes

## 🌟 What's New in v1.0.5 (Deep Interlinking & Haptics)
- **Deeper Scholar Entity Connections**: Fully integrated Tasks with Subjects, Chapters, Topics, and Courses at both the database and visual layers, creating a seamless, interconnected learning workspace.
- **Hierarchical Live Task Linking**: The Task planner now dynamically prompts custom suggestions to link Tasks to corresponding Chapters and Concepts (Topics) of the selected Subject in real-time.
- **Direct Study Action Controls**: Subject Detail screens now layout associated Chapter-level & Topic-level tasks, with direct interactive checkboxes to complete tasks on-the-fly.
- **Quick Learning Tasks**: Easily create pre-linked study tasks directly within any specific Chapter or Topic container, complete with real-time UI synchronization.
- **Tactile Acoustics & Feedback**: Introduced standard haptic configurations, custom sounds, and vibrating options for focus notifications to keep your study sessions highly responsive.

## 🌟 What's New in v1.0.4 (Elastic Buttons & Style Engine)
- **Enhanced "More Rounds" Core**: Re-engineered the shape engine to support hierarchical corner radii, delivering a more organic and polished look across all UI tiers.
- **Unified Bouncy Button System**: All buttons (Material, Text, Outlined, and Floating Action Buttons) now utilize the `Bouncy` physics engine for consistent tactile feedback.
- **Liquid Glass Integration**: Buttons now intelligently adapt to the Liquid Glass engine, gaining soft translucency and dynamic depth when transparency modes are active.
- **Advanced Rounded Modes**: Introduced **Pastel Mode** (original soft-focus high-contrast look) and **Glass Mode** (translucent liquid adaptation) as selective options within the enhanced rounds settings.

## 🌟 What's New in v1.0.3 (Stability & Conflict Resolution)
- **APK Conflict Awareness**: Added detailed in-app guidance for users installing FOSS versions over environment-specific previews to resolve "Package Conflict" errors.
- **Dynamic Versioning Engine**: Decoupled version display from hardcoded values, now synchronizing directly with `BuildConfig` for accurate build telemetry.
- **OTA Verification Hardening**: Improved the integrity check for downloaded update metadata to ensure seamless handoff to the browser for final APK acquisition.

## 🌟 What's New in v1.0.2 (Unified Aesthetics & Persistence)
- **Centralized Logic (The lumia_prefs migration)**: Consolidated all disparate internal settings stores into a singular, high-performance preference engine, eliminating state desync between background tasks and the UI.
- **Unified Alert Aesthetics**: Synchronized every system notification channel with the monochrome launcher identity and dynamic theme palette for a cohesive Material 3 experience.
- **Scholarship Stability**: Resolved critical lifecycle leaks in the streak tracking engine, ensuring your daily progress is captured accurately even across system restarts.
- **Refined Adaptive Layouts**: Optimized glass-morphism headers and card rendering for improved depth-of-field and legibility in both expanded tablet and compact mobile modes.
- **Rate-Limit Resilience**: Hardened the GitHub API fetching logic to prevent throttling during peak global traffic cycles.

## 🌟 What's New in v1.0.1
- **Advanced Navigation Layout Panel**: Expanded bottom navigation configurator supporting interactive floating/padded layouts, sizing options, custom heights, and satin glass independent filters.
- **Enhanced Adaptive Navigation Bar Aesthetics**: Tailor margin parameters, rounded corner styles, custom alpha levels, and item state colors on-the-fly.
- **Robust OTA Engine**: The in-app updater is now more adaptive, parsing release metadata correctly and gracefully extracting APK download endpoints directly from GitHub Releases.
- **Pomodoro De-Googled Device Compatibility**: Introduced `PomodoroActionReceiver` to ensure the foreground Pomodoro timer notification is rendered correctly on independent Android forks (e.g., GrapheneOS, LineageOS) using standard `androidx-media` styles, addressing issues where actions were unresponsive.

## 🛠️ Performance & Under-the-Hood Fixes
- Consolidated local Room sqlite transactions for stable focus logs.
- Added dynamic color-state updates based on active system styling and appearance modes.
- Fixed layout calculations to prevent bottom clipping when the floating bottom deck height is increased.
