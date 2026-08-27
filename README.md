# 📸 FaceTrack • Smart Biometric & Geofenced Attendance System

A next-generation **AI-Powered Attendance Management Ecosystem** built for Android using **Java**, **XML**, and **Firebase**. It features real-time facial recognition with liveness detection, 200m GPS geofencing, dynamic QR sessions, smart Punch In/Punch Out, digital leave management, and synchronized role-based dashboards for **Students**, **Teachers**, and **Admins**.

---

## 🚀 Quick Links & Live Demo

* 🌐 **Official Landing Page:** [View Live Site](https://varunyadav2001.github.io/FaceTrack-Biometric-Attendance/)
* 📥 **Download APK:** [Download Latest Version](https://github.com/varunyadav2001/FaceTrack-Biometric-Attendance/releases/latest/download/app-debug.apk)

---

## 🌟 Unique Highlights

- 🔐 **Tri-Role Architecture:** Dedicated, protected workflows for Admin, Teacher, and Student.
- 👤 **Duplicate-Proof Face Registration:** One-time face enrollment mapped strictly to a single account.
- ⚡ **AI Liveness & Biometric Matching:** Fast anti-spoofing face verification with identity confidence scoring.
- 📍 **GPS + 200m Geofencing:** Restricts attendance access strictly within the registered premises.
- ⏱️ **Automated Work-Hour Engine:** Automatically calculates working duration and marks **Full Day / Half Day / Pending**.
- 🔄 **Interactive Swipe-to-Punch UI:** Smooth swipe-based workflow for Punch In and Punch Out.
- 📱 **Dynamic QR Attendance:** Teacher-generated QR sessions with expiration and validation.
- 📝 **End-to-End Leave Lifecycle:** Student leave request → Teacher approval/rejection → Student status update.
- 📊 **Real-Time Synchronized Monitoring:** Live Student and Teacher attendance monitoring across dashboards.
- 📸 **Attendance Verification Snapshot:** Displays face verification details after successful attendance verification.
- 👨‍🏫 **Teacher Attendance Management:** Teachers can manage assigned students, attendance registers, QR sessions, and leave requests.
- 👨‍🎓 **Student Self-Service Attendance:** Students can monitor attendance, working hours, leave requests, and verification details.
- 🛡️ **Multi-Layer Attendance Validation:** Authentication + Face + Liveness + GPS + Geofence validation.

---

## ✨ Comprehensive Features Breakdown

### 🔐 1. Authentication & Security

* **Role-Based Authentication:** Dedicated login workflows for Admin, Teacher, and Student.
* **Automatic Role-Based Routing:** After successful login, users are redirected to their respective dashboard.
* **Protected Dashboard Routing:** Prevents unauthorized access to Admin, Teacher, and Student dashboards.
* **Secure Session Handling:** Logged-out users are redirected to the Login page.
* **Multi-Layer Attendance Validation:** Attendance requires authenticated user identity, registered face, liveness verification, and valid geofence conditions.
* **Credential Privacy:** Admin credentials are never displayed or hardcoded in the public interface.

---

### 👤 2. Biometric & Face Recognition Engine

* **One-Time Face Enrollment:** Each account can register its face only once.
* **Duplicate Face Detection:** Prevents the same face from being registered under multiple accounts.
* **New Face Validation:** A genuinely new face can be registered successfully without false duplicate errors.
* **Fast Face Detection:** Optimized camera-based face detection for faster attendance verification.
* **Face-Verified Punch In:** Face verification is mandatory before Punch In.
* **Face-Verified Punch Out:** Face verification is required again before Punch Out.
* **Liveness Detection:** Verifies that the person is physically present in front of the camera.
* **Identity Matching:** Compares the live face with the registered face of the logged-in user.
* **Verification Metadata:** Stores/display verification result, face snapshot, match score, liveness status, and timestamp.
* **Wrong Face Protection:** A different person's face cannot Punch In or Punch Out for another user.

---

### 📍 3. Location & Automated Time Tracking

* **200m Geofence Validation:** Compares the user's live GPS location with the registered company/institution location.
* **Live Location Detection:** Uses the user's current GPS coordinates during attendance verification.
* **Location-Based Attendance:** Attendance is allowed only when the user is within the configured attendance area.
* **Smart Working-Hour Calculation:** Automatically calculates the total working duration from Punch In to Punch Out.
* **8+ Hours:** Categorized as **Full Day**.
* **Less than 8 Hours:** Categorized as **Half Day**.
* **Punch Out Pending:** Users who have punched in but not punched out remain in a pending state.
* **Swipe-Based Attendance:** Interactive swipe cards provide a smooth Punch In → Punch Out experience.

---

### ⏱️ 4. Smart Punch In & Punch Out

* **Punch In Verification Flow:**

```text
Camera
   ↓
Face Detection
   ↓
Liveness Verification
   ↓
Identity Matching
   ↓
GPS Verification
   ↓
200m Geofence
   ↓
✓ Punch In
