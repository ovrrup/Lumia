# Lumia Release Notes

## 🌟 v1.0.5 (Diagnostic & Analytics Harmonization) — June 2026
This update focuses on structural refinement and UI performance, harmonizing the diagnostic tools with the core Lumia aesthetic.

- **Redesigned LogDog Core**: Reverted the diagnostic handler to the **v1 layout architecture**. It now features the signature **GlassCard** styling, clearer monochrome iconography, and a more intuitive "sniffing" animation for stack trace analysis.
- **Unified Action History**: Re-engineered the **Analytics Tab** to combat UI bloat. All telemetry and activity logs are now housed within a single, high-performance scrollable panel. This prevents layout fragmentation and significantly reduces system pressure when viewing extensive histories.
- **Improved Data Management**: Optimized core telemetry parsing to handle thousands of historical records without impacting app responsiveness.
- **Updater Streamlining**: Refined the in-app update experience by removing redundant package conflict warnings, providing a cleaner handoff for official signed releases.
- **General Stability**: Resolved several under-the-hood compilation warnings and missing UI component references.

---

## 🌟 v1.0.4 (Elastic Buttons & Style Engine)
- **Enhanced "More Rounds" Core**: Re-engineered the shape engine to support hierarchical corner radii for a more organic look.
- **Unified Bouncy Button System**: All buttons now utilize the `Bouncy` physics engine for consistent tactile feedback.
- **Liquid Glass Integration**: Buttons adapt to the Liquid Glass engine with soft translucency and dynamic depth.
- **Advanced Rounded Modes**: Introduced **Pastel Mode** and **Glass Mode** as selective options.

## 🌟 v1.0.3 (Stability & Conflict Resolution)
- **APK Conflict Awareness**: Added detailed guidance for users resolving "Package Conflict" errors.
- **Dynamic Versioning Engine**: Decoupled version display from hardcoded values, syncing directly with `BuildConfig`.
- **OTA Verification Hardening**: Improved integrity checks for update metadata.

## 🌟 v1.0.2 (Unified Aesthetics & Persistence)
- **Centralized Logic**: Consolidated settings into a single preference engine (lumia_prefs).
- **Unified Alert Aesthetics**: Synchronized system notification channels with the monochrome launcher identity.
- **Scholarship Stability**: Resolved lifecycle leaks in the streak tracking engine.
- **Refined Adaptive Layouts**: Optimized glass-morphism headers and card rendering for tablets and mobile.

## 🌟 v1.0.1
- **Advanced Navigation Layout Panel**: Expanded bottom navigation configurator supporting interactive floating/padded layouts.
- **Enhanced Adaptive Navigation Bar**: Tailor margin parameters, rounded corners, and custom alpha levels.
- **Robust OTA Engine**: Adaptive release metadata parsing from GitHub Releases.
- **De-Googled Compatibility**: Improved notification action responsiveness for independent Android forks.

---

## 🛠️ Global Performance & Legacy Fixes
- Consolidated local Room SQLite transactions for stable focus logs.
- Added dynamic color-state updates based on active system styling.
- Optimized layout calculations to prevent clipping in custom navigation heights.
- Improved memory management for long-running Pomodoro sessions.
