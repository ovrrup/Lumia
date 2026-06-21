# Lumia Release Notes

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

## 🛠️ Performance & Under-the-Hood Fixes
- Consolidated local Room sqlite transactions for stable focus logs.
- Added dynamic color-state updates based on active system styling and appearance modes.
- Fixed layout calculations to prevent bottom clipping when the floating bottom deck height is increased.
