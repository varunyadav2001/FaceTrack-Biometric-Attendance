# 📸 FaceTrack • Smart Biometric & Geofenced Attendance System

A next-generation **AI-Powered Attendance Management Ecosystem** built for Android using **Java**, **XML**, and **Firebase**. It features real-time facial recognition with liveness detection, 200m GPS geofencing, dynamic QR sessions, and synchronized role-based dashboards for **Students**, **Teachers**, and **Admins**.

---

## 🚀 Quick Links & Live Demo
* 🌐 **Official Landing Page:** [View Live Site](https://varunyadav2001.github.io/MyAndroidApp/)
* 📥 **Download APK:** [Download Latest Version](https://github.com/varunyadav2001/MyAndroidApp/releases/latest/download/app-debug.apk)

---

## 🌟 Unique Highlights

- 🔐 **Tri-Role Architecture:** Dedicated, protected workflows for Admin, Teacher, and Student.
- 👤 **Duplicate-Proof Face Registration:** One-time face enrollment mapped strictly to a single account.
- ⚡ **AI Liveness & Biometric Matching:** Anti-spoofing face verification with confidence scoring.
- 📍 **GPS + 200m Geofencing:** Restricts punch-in/out strictly within registered premises.
- ⏱️ **Automated Work-Hour Engine:** Auto-calculates hours and marks **Full Day / Half Day / Pending**.
- 🔄 **Interactive Swipe-to-Punch UI:** Smooth card-swipe workflow for logging check-ins and check-outs.
- 📱 **Dynamic QR Attendance:** Teacher-generated dynamic QR with real-time session expiration.
- 📝 **End-to-End Leave Lifecycle:** Digital request, real-time approval/rejection, and auto-attendance sync.
- 📊 **Real-Time Synchronized Monitoring:** Live updates across all dashboards via Cloud Firestore.

---

## ✨ Comprehensive Features Breakdown

### 🔐 1. Authentication & Security
* **Role-Based Authentication:** Dedicated credentials and automatic dashboard routing for Admin, Teacher, and Student.
* **Protected Dashboard Routing:** Prevents unauthorized cross-dashboard navigation and secures active sessions.
* **Multi-Factor Attendance Validation:** Attendance is logged only when authenticated user, valid registered face, liveness test, and GPS geofence criteria are simultaneously met.

### 👤 2. Biometric & Face Recognition Engine
* **One-Time Face Enrollment:** Detects and registers unique face vectors, throwing instant errors on duplicate attempts.
* **Face-Verified Punch In & Punch Out:** Mandatory biometric check on both entry and exit to eliminate proxy logging.
* **Liveness & Match Verification:** Camera-based identity matching with detailed verification metadata (Face snapshot, match score, timestamp).

### 📍 3. Location & Automated Time Tracking
* **200m Geofence Validation:** Compares live GPS coordinates with institution/company location before granting punch access.
* **Smart Working-Hour Calculation:**
  * **8+ Hours:** Categorized as **Full Day**.
  * **< 8 Hours:** Categorized as **Half Day**.
  * Auto-assigns **Punch Out Pending** status until day close.
* **Swipe-Based Interaction:** Tactile swipe cards for instant Punch In / Punch Out actions.

### 📱 4. Dynamic QR Attendance
* **Teacher Session Generator:** Generates time-sensitive, single-session QR codes.
* **Anti-Fraud QR Scanning:** Expiration timers, duplicate scan blockers, and class validation algorithms.
* **Live Register Updates:** Real-time visual feedback on teacher screens as students scan in.

### 📝 5. Digital Leave & Management Workflow
* **Leave Management:** Students submit leave requests with date ranges; Teachers approve/reject with instant status reflection in attendance logs.
* **Teacher Management Hub:** Manage assigned student rosters, daily registers, leave requests, and self-attendance.
* **Admin Centralized Command:** Complete control over departments, subjects, staff/student rosters, live logs, and aggregated statistics.
* **Attendance History & Logs:** Detailed audit trail with timestamps, durations, statuses, and verification history.

---

## 🛠️ Tech Stack & Architecture

| Component | Technology / Library |
| :--- | :--- |
| **Mobile Platform** | Native Android (Java) |
| **User Interface** | XML, Material Components, Custom Animations |
| **Backend & Auth** | Google Firebase Authentication |
| **Cloud Database** | Cloud Firestore (Real-Time Sync) |
| **Location Services** | Google Play Services Location API (GPS / Geofencing) |
| **Camera & Vision** | CameraX API, Machine Learning Face Detection |
| **Deployment** | GitHub Releases & GitHub Pages |

---

## 📲 Installation & Setup

1. Download the latest `.apk` from the [Download APK](https://github.com/varunyadav2001/MyAndroidApp/releases/latest/download/app-debug.apk) link.
2. Tap the downloaded file on your Android device.
3. Enable **"Allow from this source"** if prompted by Android security settings.
4. Open the app, log in with your assigned role, and complete initial face enrollment.

---

## 👨‍💻 Developer & Maintainer

**Varun Vikram Yadav**
* 🐙 **GitHub:** [@varunyadav2001](https://github.com/varunyadav2001)
* 💼 **LinkedIn:** [Varun Yadav](https://www.linkedin.com/in/varun-yadav006)
