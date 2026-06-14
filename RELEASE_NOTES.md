# Lumia Release Notes

## 🌟 v1.0.5 (Modular Expansion & In-App Installer Upgrade) — June 2026
This massive structural upgrade re-architects how Lumia handles build pipelines and plugin distribution, paving the way for a lightweight core app with immensely powerful optional packages.

- **Dantotsu-Style Dynamic Micro-APKs**: Introduced the Sentinel and Spectra plugin modules as entirely separate installable APK packages. This dramatically reduces the main application size and improves offline speed.
- **Improved In-App Package Installer**: The Plugins UI Hub in Settings now points directly to our newly segregated GitHub Packages registries. You can fetch independent `Lumia-Sentinel` and `Lumia-Spectra` companion applications dynamically right from within the app.
- **Automated GitHub Actions Integration**: Fully automated the CI/CD pipelines! GitHub workflows have been bifurcated into dedicated modules. A new `build-main-apk.yml` handles the core compilation, while specialized `build-plugins.yml` workflows handle the companion assets.
- **OTA Verification Hardening**: Restructured the latest release API queries to cleanly separate updates targeting the primary `Lumia.apk` versus its dependent modular branches.
- **Storage & Resource Optimizer**: Integrated a dedicated local database and cache purger. Instantly compute active asset load across database logs, SQLite index sidecars, and temporary system cache buffers. 
- **Sleep-Resilient Pomodoro Engine**: Hardened background interval timing reliability. Re-engineered the timer loop to sync with system-level wall clock changes (`System.currentTimeMillis()`) instead of purely relying on process delay increments.
- **Redesigned LogDog Core**: Reverted the diagnostic handler to the **v1 layout architecture**. It now features the signature **GlassCard** styling, clearer monochrome iconography, and a more intuitive "sniffing" animation for stack trace analysis.

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
