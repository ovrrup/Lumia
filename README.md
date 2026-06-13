# <img src="app/src/main/res/drawable/ic_notification_scholar.png" width="36" valign="middle"/> Lumia 

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Platform-Android_8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_/_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Database-SQLite_/_Room-00599C?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite Room" />
  <img src="https://img.shields.io/badge/License-GNU_GPLv3-red?style=for-the-badge" alt="GPLv3 License" />
</p>

<p align="center">
  [![Build APK](https://github.com/ovrrup/Lumia/actions/workflows/build-apk.yml/badge.svg)](https://github.com/ovrrup/Lumia/actions/workflows/build-apk.yml)
</p>

**Lumia** is an offline-first academic battle-station designed for students, self-learners, and those fighting the modern urge of checking notifications every 17 seconds. This application wraps course trackers, attendance calculators, task schedulers, statistical visualization charts, and a zero-compromise focus ecosystem into a single unified terminal.

Powered by a customized **True AOD (Always-On Display) study-screen overlay**, Lumia turns your phone into a dedicated OLED focus timer. It overrides standard brightness limits, monitors accelerometers for physical movement, and defends your studies against the constant storm of lockscreen distractions.

---

## 🏛️ Central Feature Architecture

### 1. The Dashboard Control Center
Lumia launches straight into your central visual hub:
- **At-a-glance status checks:** See your class schedule for the current date, pending coursework tasks, and active deadlines.
- **The Judgmental Streak Flame:** Maintains active streak logs. Keep your studying trend rising; otherwise, the flame dies, and Lumia’s statistics look disappointingly empty.
- **Fast Metrics:** Direct displays of average subjects completion indices, remaining files to read, and quick links to continue your last coursework node.

### 2. Deep Academic Trees (Courses & Subjects)
Track organized courses (e.g., *Computer Science (B.Sc.)* or *Pre-Med*) down to the atomic study level:
- **Structurable Entities:** Courses split into independent **Subjects**, which contain nested **Chapters**, containing tracked **Topics** and study milestones.
- **Progress Tracking:** Interactive completed percentages rise dynamically as you check off topics, helping you study step-by-step.
- **Attendance Registry:** Log class attendance records with fully visual percentage charts. No more manually guessing if you can afford to skip that 8:00 AM lecture.

<p align="center">
  <img src="app/src/main/res/drawable/ic_notification_streak.png" width="80" alt="Streak Asset"/>
</p>

### 3. Pomodoro focus Engine & "True AOD" (The Ultimate Weapon)
This isn't a simple aesthetic countdown timer. Lumia features a dual-mode system overlay that blocks standard Android lock-panel updates to turn your phone into a pure hardware focus clock:
* **The True AOD Interface:** Shows simple time elements, focus progress rings, dynamic study ticks, and pure black OLED-safe background configurations.
* **Dual Integration Profiles:**
  * **System Overlay Mode:** Draws the study board over active applications and system interfaces for instantaneous access.
  * **Accessibility Service Mode:** Installs a private accessibility engine that intercepts key handlers to safely lock the interface behind hardware controls under modern lockscreens (Android 12+).
* **OLED Stealth Level:** Slide down hardware outputs with dimming levels up to **99% Highest Darkness** (perfect for studying in dimly lit library desks).
* **Anti-Cheat Wake-up Sensitivities:** Pick how Lumia unlocks:
  * *Tap/Motion:* Uses linear accelerometer deltas. Simply shake, tilt, or wave your hand above the device to wake up!
  * *Double-tap:* Prevents accidental triggers inside deep backpack pockets.
  * *Hold Secure:* Requires holding down on the OLED panel for 1 second to end focus. Excellent for preventing quick exit impulses when trying to focus.

### 4. Continuous Analytics Terminal
Visualize your scholarly dedication using built-in, native vector bar graphs, charts, and distribution indicators:
- Subject distribution maps explaining where your hours went.
- Historical trend indicators showing Pomodoro focus efficiency over time.
- Attendance ratios complete with Material 3 status tags showing safe vs critical attendance values.

### 5. Quick Notes Storage
A localized scratchpad to quickly log fleeting research items, ideas, formulas, or coffee orders. Everything stays private, offline, and ready to edit or delete at a moments notice.

### 6. LogDog (The Persistent Canine Diagnostic Agent 🐕)
If something breaks under the hood, there is no need to connect to a desktop computer and run complicated development terminal configurations:
- Lumia integrates **LogDog**, an internal crash handler that catches exceptions, formats stack traces, and compiles diagnostic files.
- View and manage local repair suggestions directly inside the Lumia Settings Panel. It's a developer in your pocket!

---

## 🔒 Privately Local & Factual

Lumia believes that your study logs should never be a telemetry product:
- **Zero Remote Calls:** No remote trackers, analytical frameworks, or advertising APIs.
- **Local Databases:** SQLite Room database isolated inside highly secure local Android sandboxes.
- **Offline Reliability:** Perfect for off-grid reading sessions, cellular-restricted campus basements, or flights.

For formal clauses, view our dedicated **[Privacy Policy](PRIVACY.md)** and **[Terms and Conditions](TERMS.md)**.

---

## 🛠️ Build & Development Guidelines

Lumia has been polished to utilize modern Android patterns (Jetpack Compose, Kotlin Coroutines, and Room Architecture).

### Compilation Checklist
1. Ensure you have **Android Studio Koala or newer** installed.
2. Verify you have **JDK 17+** configured on your environmental path.
3. Keep standard dependencies untouched to ensure version-locked compiler compatibility.

### Building via Terminal
To build, test, and package Lumia manually, run standard Gradle execution commands in the root directory:

```bash
# Verify unit tests & Robolectric interfaces
gradle :app:testDebugUnitTest

# Build the complete installable development APK
gradle assembleDebug
```

After compilation, your build artifacts will reside in:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 GitHub Actions CI & Automatic Release Setup

Lumia comes pre-configured with a continuous integration file in `.github/workflows/build-apk.yml`. 

### Automated Releases:
Every time you push or trigger a Pull Request to your `main` / `master` repository branches:
1. **GitHub Runner** launches an Ubuntu container.
2. **Setup-Java** configures JDK 17 environments.
3. Automatically generates temporary secure `debug` keystore signatures.
4. Evaluates layout integrity, clean assets, and compiles Lumia.
5. Renames the binary output to `Lumia.apk`.
6. Saves the compiled application as a downloadable artifact.

### How to obtain the app:
1. Navigate to the **Releases** section on the right-hand sidebar of the GitHub repository page (or click [Releases](https://github.com/ovrrup/Lumia/releases)).
2. Under the latest release (e.g., `v1.4.0`), expand the **Assets** section if collapsed.
3. Click on the compiled **Lumia.apk** asset file to download it.
4. Transfer the downloaded APK file to your Android device and install it!

---

## ⚖️ Open Source & GPLv3 Licensing

This project is licensed under the **GNU General Public License v3.0**. 

You are free to fork, hack, and deploy Lumia according to open-source safety rules. If you release your own modified variant of Lumia to the world, you **must** keep the source code public and license your modifications under the GPLv3.

Let's maintain software freedom together! Read **[LICENSE](LICENSE)** for the full regulatory texts.
