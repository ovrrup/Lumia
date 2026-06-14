# Lumia Release Notes

## 🌟 What's New in v1.0.2
- **Unified Alert Aesthetics**: Synchronized all system notification icons with the monochrome launcher design and active theme color palette for a seamless Material 3 experience.
- **Unified Storage Engine**: Consolidated internal settings into a centralized preference store (`lumia_prefs`), improving data consistency across background workers and the UI.
- **Scholarship Engine Stability**: Resolved critical lifecycle issues in state management, ensuring reliable streak tracking and real-time dashboard updates.
- **Refined Material 3 Layouts**: Optimized glass-morphism headers and list item rendering for improved depth and legibility across all adaptive display modes.
- **OTA Engine Resilience**: Hardened the version checking logic against GitHub API rate limits and improved tag parsing for FOSS distributions.

## 🌟 What's New in v1.0.1
- **Advanced Navigation Layout Panel**: Expanded bottom navigation configurator supporting interactive floating/padded layouts, sizing options, custom heights, and satin glass independent filters.
- **Enhanced Adaptive Navigation Bar Aesthetics**: Tailor margin parameters, rounded corner styles, custom alpha levels, and item state colors on-the-fly.
- **Robust OTA Engine**: The in-app updater is now more adaptive, parsing release metadata correctly and gracefully extracting APK download endpoints directly from GitHub Releases.
- **Pomodoro De-Googled Device Compatibility**: Introduced `PomodoroActionReceiver` to ensure the foreground Pomodoro timer notification is rendered correctly on independent Android forks (e.g., GrapheneOS, LineageOS) using standard `androidx-media` styles, addressing issues where actions were unresponsive.

## 🛠️ Performance & Under-the-Hood Fixes
- Consolidated local Room sqlite transactions for stable focus logs.
- Added dynamic color-state updates based on active system styling and appearance modes.
- Fixed layout calculations to prevent bottom clipping when the floating bottom deck height is increased.
