# 🎓 Adroit

<p align="center">
  <img src="https://img.shields.io/badge/Flutter-3.x-02569B?style=for-the-badge&logo=flutter&logoColor=white" alt="Flutter" />
  <img src="https://img.shields.io/badge/Language-Dart-0175C2?style=for-the-badge&logo=dart&logoColor=white" alt="Dart" />
  <img src="https://img.shields.io/badge/State-Riverpod-blue?style=for-the-badge" alt="Riverpod" />
  <img src="https://img.shields.io/badge/Database-Drift_SQLite-teal?style=for-the-badge" alt="Drift SQLite" />
  <img src="https://img.shields.io/badge/License-GNU_GPLv3-red?style=for-the-badge" alt="GPLv3 License" />
</p>

**Adroit** is a minimalist, local-first academic companion engineered strictly for tracking classes, timetables, attendance, and syllabus coverage. No telemetry, no background lockscreen overlays, and zero distraction.

---

## 🏛️ Core Features

### 1. 📅 Classes & Timetable
- **Weekly Schedule & Today's Agenda:** Chronologically sorted class schedules with rooms, instructors, and start/end times.
- **One-Tap Attendance Logging:** Quickly log attendance directly from the agenda (`Present`, `Absent`, `Cancelled`, or `Late`).
- **Smart Attendance Intelligence:**
  - Real-time attendance percentage vs your target threshold (e.g. 75%).
  - **Safe Bunks Calculator:** Know exactly how many future classes you can safely skip.
  - **Recovery Calculator:** Know exactly how many consecutive classes you must attend to restore low attendance.

### 2. 📚 Syllabus & Curriculum Tracking
- **Multi-Level Academic Trees:** Organize studies into **Subjects**, nested **Units/Chapters**, and atomic **Checklist Topics**.
- **Interactive Checklists:** Tap to check off topics as you cover them during lectures or exam revision.
- **Visual Progress:** Dynamic progress rings and percentage indicators per unit and across the entire subject curriculum.
- **Filter & Search:** Filter by pending or completed topics, or search across your entire syllabus.

### 3. 📝 Tasks & Deadlines
- **Coursework Management:** Track homework, lab reports, readings, and assignment due dates.
- **Deadline Indicators:** Real-time countdowns (`Due today`, `Due tomorrow`, `Overdue`).
- **Priority Tags:** High, Medium, and Low priority classification.

### 4. 🎨 Minimal Design & Theming
- **Material 3 Aesthetics:** Clean, distraction-free cards, typography, and fluid microtransitions.
- **Pure OLED Black Mode:** True `#000000` blacks for battery efficiency and night study sessions.
- **Custom Accent Palettes:** Indigo, Emerald, Sapphire, Rose, Amber, Violet, Teal, and Coral.

### 5. 🔒 100% Offline & Local-First
- **Drift SQLite Engine:** All data is stored in a structured local SQLite database on your device.
- **JSON Backup & Restore:** Export complete offline backups anytime. Backwards-compatible with legacy Lumia backup files.

---

## 🛠️ Building & Development

### Requirements
- **Flutter SDK** 3.19+ (or newer)
- **Dart SDK** 3.3+
- **JDK 17+**

### Steps
```bash
# Fetch dependencies
flutter pub get

# Generate Drift database code
dart run build_runner build --delete-conflicting-outputs

# Run the app
flutter run

# Build release APK
flutter build apk --release
```

---

## ⚖️ License

Licensed under the **GNU General Public License v3.0** (GPLv3).
