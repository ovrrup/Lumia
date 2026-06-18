# Lumia Release Notes

## WARNING: CRITICAL APP REINSTALLATION REQUIRED
Due to updates to our application's Package ID and code-signing credentials, version 1.0.5 acts as a clean break.
Installing this update directly over older installations may cause duplicate apps to show up on your home screen or raise package configuration conflicts.
Please back up your data if needed, completely UNINSTALL any previous pre-release or preview version of Lumia from your device, and perform a fresh install of Lumia v1.0.5.

## What's New in v1.0.5 (Local Photo Avatars & Functional Unlocks)
- **Local Storage Avatars (FOSS Dev-Photo integration)**: Completely retired emoji-based profile identifiers. Profiles now support importing real, high-resolution custom pictures selected directly from your device's files and local storage.
- **Advanced Typography Standardized**: Custom global typography configurations (font adjustments, custom sizing scaling, and font weights) have been fully uncaged from the Plus Shop. They are now recognized as a standard core utility available to all users with zero locks.
- **Achievement Visual Overhaul**: Fully refactored achievement cards to utilize sharp, professional, high-fidelity Material symbols and vector graphics instead of loose illustrative emojis.
- **Stability and Fixes**: Corrected layout sizing parameters, resolved memory references during picture loading, and removed leftover emoji-based elements for a streamlined, high-performance offline workspace.

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
