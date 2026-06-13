# Privacy Policy

**Effective Date:** June 13, 2026  
**Status:** 100% Client-Side Privacy Compliance Factsheet

Lumia (hereinafter referred to as the "Application") is designed from the ground up to be an **offline-first academic tracker and focus assistant**. This document details our absolute commitment to user privacy, local-only processing, and how system permissions are utilized.

---

## 1. Zero-Data Harvesting Core

The fundamental architecture of Lumia is completely encapsulated on your local Android device:
- **No Remote Servers:** Lumia has no central backend infrastructure, telemetry servers, or user-tracking analytical pipelines.
- **Offline Storage:** All course outlines, study durations, notes, assignments, streaks, and attendance records are stored locally on your device in a private SQLite database managed via the Android Room Persistence library.
- **No Account Requirements:** No email, social sign-ins, or profile registrations are needed. Go completely off-grid.

---

## 2. Explanation of Android System Permissions

Lumia utilizes specific Android framework configurations to enable focus-related alerts and optimized study panels. All permissions are evaluated **entirely locally**:

### A. Draw System Overlays (`SYSTEM_ALERT_WINDOW`)
- **Factual Purpose:** Required to display the OLED-safe True Always-On Display (AOD) panel directly over standard active interfaces and lock panels.
- **Privacy Constraint:** Lumia draws a black overlay with clock details. It does not monitor, capture, or inspect pixels rendered by other applications.

### B. Accessibility Service Engagement (`AodAccessibilityService`)
- **Factual Purpose:** Used to bypass lock interfaces on specific Android runtime levels (Android 12+) and keep the dark screen saver safely active without burning battery hardware cycles.
- **Privacy Constraint:** We treat this permission with strict institutional integrity. The service **never** logs user touch inputs, inspects on-screen keyboard structures, or records credentials. It is registered locally and functions solely to manage standard window attachments and layout transitions.

### C. Exact Alarm Scheduling (`SCHEDULE_EXACT_ALARM`)
- **Factual Purpose:** Guarantees that study timers, Pomodoro state switch notifications, and coursework deadlines trigger with microsecond accuracy, even when the OS forces deep device sleep states (Doze Mode).
- **Privacy Constraint:** Used locally within the Android `AlarmManager`. No usage logs or warning schedules are transmitted.

---

## 3. Third-Party Integrations & Ads

- **No Third-Party SDKs:** Lumia does not bundle proprietary advertising frameworks (e.g., AdMob), analytical libraries, or crash reporting structures that call remote servers.
- **Built-in Diagnostic Handler:** Our built-in repair utility (**LogDog**) intercepts application crash logs *locally*. These logs can only be viewed and manually exported by you inside the Settings Screen. No remote transmission occurs.

---

## 4. Updates & Changes

If future updates introduce cloud backups or remote synchronizations, they will be strictly opt-in, explicitly requested, and updated in this document beforehand.

---

## 5. Contact & Support

For questions, code verification, or to inspect the open-source structure of Lumia, please refer to the Git repository or submit a formal pull request directly.
