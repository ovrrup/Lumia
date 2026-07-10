# Lumia Release Notes

## 🌟 What's New in v1.0.7 (Redesigned Dark Obsidian Glass Theme)
- **Dynamic Obsidian Infusion**: Completely redesigned the glass UI color blending algorithms for dark mode. Cards, hero banners, progress pills, and navigation elements now dynamically infuse a portion of the active theme's primary color (Twilight, Gold, Emerald, Rose, etc.) into the backing canvas, generating a luxurious, customized semi-translucent obsidian sheen.
- **Iridescent Glass Border Refinements**: Introduced a new, high-contrast iridescent glowing border gradient. Rather than using static outline shades, dark mode card edges now render light-refracting gradient highlights that transition beautifully from primary-toned glow highlights to satin transparency.
- **Enhanced Contrast & Transparency**: Recalibrated opacity and alpha constants in dark mode to maximize translucent readability, making underlying mesh shapes and dynamic background blobs stand out with unparalleled depth and clarity.

## 🌟 What's New in v1.0.6 (Onboarding Polish & Tag Consolidation)
- **High-Fidelity Onboarding Overhaul**: Completely redesigned the onboarding illustration layer with interactive, high-fidelity glassmorphic mockup panels representing Lumia's actual student cockpit interface—complete with custom status bars, streak indicator badges, the welcome mesh banner, courses progress statistics, and floating navigation bars.
- **Refined Navigation Architecture**: Re-located the central **Tag Management (Network Explorer)** feature out of the active student dashboard action-bar and directly into the main Settings dashboard. This unifies visual settings and provides a clutter-free daily study dashboard.
- **Under-the-Hood Hardening**: Re-audited codebases for potential performance issues, resolved edge-case exceptions, and cleaned up temporary build-scripts to secure an ultra-smooth, fast, and crash-free v1.0.6 release.

## WARNING: CRITICAL APP REINSTALLATION REQUIRED
Due to updates to our application's Package ID and code-signing credentials, version 1.0.5 acts as a clean break.
Installing this update directly over older installations may cause duplicate apps to show up on your home screen or raise package configuration conflicts.
Please back up your data if needed, completely UNINSTALL any previous pre-release or preview version of Lumia from your device, and perform a fresh install of Lumia v1.0.5.

## 🌟 What's New in v1.0.5 (Test Analytics & Academic Progress)
- **Comprehensive Test Tracking & Analytics**: Log test records per subject and course. Includes real-time tracking metrics, average scores, best marks, and performance trends over time.
- **Visual Progress Charts**: Newly integrated linear progress graphs visually map out your academic standing and history across tests.
- **Local Storage Avatars (FOSS Dev-Photo integration)**: Completely retired emoji-based profile identifiers. Profiles now support importing real, high-resolution custom pictures selected directly from your device's files and local storage.
- **Advanced Typography Standardized**: Custom global typography configurations (font adjustments, custom sizing scaling, and font weights) are now recognized as a standard core utility available to all users with zero locks.
- **Stability and Fixes**: Corrected layout sizing parameters, resolved memory references during picture loading, and optimized the offline workspace.

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
