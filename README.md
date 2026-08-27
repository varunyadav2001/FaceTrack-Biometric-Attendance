# 🩸 SmartBlood • Smart Blood Donation & Emergency Management System

A next-generation **Digital Blood Donation & Emergency Management Ecosystem** built for Android using **Java**, **XML**, and **Firebase**. It connects **Donors**, **Hospitals**, and **Blood Banks** through a unified platform for blood requests, emergency requirements, donation appointments, blood inventory, and real-time coordination.

---

## 🚀 Quick Links & Live Demo

* 🌐 **Official Landing Page:** [View Live Site](https://varunyadav2001.github.io/Blood_bank/)
* 📥 **Download APK:** [Download Latest Version](https://github.com/varunyadav2001/Blood_bank/releases/download/v1.0.0/app-debug.apk)

---

## 🌟 Unique Highlights

- 🔐 **Multi-Role Architecture:** Dedicated workflows for Donors, Hospitals, and Blood Banks.
- 🩸 **Smart Blood Group Management:** Donor profiles maintain blood group and donation information.
- 🚨 **Emergency Blood Requests:** Hospitals can create urgent blood requirements and coordinate with eligible donors.
- 🏦 **Blood Bank Discovery:** Users can search and connect with available blood banks.
- 📅 **Donation Appointment Booking:** Donors can schedule blood donation appointments with date and time.
- 📊 **Real-Time Blood Inventory:** Blood banks can manage available blood stock and inventory.
- 🔄 **Request Management:** Hospitals and blood banks can manage blood requests and approvals.
- ☁️ **Firebase Integration:** Authentication and cloud-based real-time data management.

---

## ✨ Comprehensive Features Breakdown

### 🔐 1. Authentication & Security

* **Role-Based Authentication:** Dedicated login and registration workflows for Donors, Hospitals, and Blood Banks.
* **Protected Dashboard Routing:** Users are directed to the appropriate dashboard according to their role.
* **Firebase Authentication:** Secure authentication and user account management using Firebase.
* **Session Management:** Maintains authenticated user sessions across the application.

### 👤 2. Donor Management

* **Donor Registration & Login:** Donors can create and manage their accounts.
* **Blood Group Profile:** Store and manage donor blood group information.
* **Blood Bank Search:** Find suitable blood banks for donation.
* **Donation Appointment:** Book donation appointments with selected date and time.
* **Upcoming Donations:** View scheduled donation appointments.
* **Donation History:** Maintain a record of previous donations.
* **Emergency Response:** Donors can respond to emergency blood requirements.
* **Lifesaving Impact:** Track the contribution made through blood donations.

### 🏥 3. Hospital Blood Request Management

* **Hospital Registration & Login:** Secure hospital account management.
* **Blood Requests:** Hospitals can create blood requirements based on blood group and quantity.
* **Emergency Requests:** Create urgent emergency blood requests.
* **Request Tracking:** Track submitted blood requests and their status.
* **Blood Bank Search:** Find suitable blood banks for required blood groups.
* **Donor Response Tracking:** Monitor responses from eligible donors.
* **Request Management:** Manage active, approved, fulfilled, and pending requests.

### 🏦 4. Blood Bank Management

* **Blood Bank Registration & Login:** Dedicated blood bank authentication.
* **Blood Inventory:** Manage available blood groups and stock quantities.
* **Hospital Requests:** View incoming blood requirements from hospitals.
* **Request Approval:** Approve and process suitable blood requests.
* **Stock Transfers:** Manage blood stock movement and availability.
* **Donor Appointments:** View and manage scheduled donor appointments.
* **Donation Completion:** Update inventory after successful blood donation.
* **Inventory Monitoring:** Maintain an organized view of available blood stock.

### 🚨 5. Emergency Blood Workflow

* **Emergency Request Creation:** Hospitals can create urgent blood requirements.
* **Active Emergency:** Emergency requests remain visible until fulfilled.
* **Eligible Donors:** Suitable donors can view emergency requirements.
* **Donor Response:** Donors can select **"I'm Available"** to respond.
* **Hospital Coordination:** Hospitals can view donor responses.
* **Requirement Fulfillment:** Blood requirement can be marked fulfilled after coordination.

### 📅 6. Blood Donation Booking

* **Blood Bank Selection:** Select a preferred blood bank.
* **Appointment Date:** Choose a suitable donation date.
* **Appointment Time:** Select an available time.
* **Appointment Tracking:** View upcoming appointments.
* **Donation History:** Maintain completed donation records.
* **Donation Status:** Track appointment and donation completion status.

### 📊 7. Real-Time Management

* **Firebase Firestore:** Cloud-based database for application data.
* **Real-Time Updates:** Keep blood requests, appointments, and inventory synchronized.
* **Cloud Data Management:** Centralized storage of donor, hospital, blood bank, and request information.
* **Live Coordination:** Supports communication between the three major roles.

---

## 🔄 Emergency Blood Workflow

```text
🏥 HOSPITAL
      │
      ▼
🚨 Create Emergency Blood Request
      │
      ▼
🔥 Active Emergency
      │
      ▼
👤 Eligible Donors
      │
      ▼
✅ I'm Available
      │
      ▼
🏥 Hospital Receives Response
      │
      ▼
🩸 Blood Requirement Fulfilled
