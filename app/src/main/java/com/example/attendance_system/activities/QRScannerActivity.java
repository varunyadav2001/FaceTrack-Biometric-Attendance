package com.example.attendance_system.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.ActivityQrScannerBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.QrSession;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;
    private CaptureManager capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, null)) {
            return;
        }

        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarScanner.setNavigationOnClickListener(v -> finish());
        binding.btnCloseScanner.setOnClickListener(v -> finish());

        capture = new CaptureManager(this, binding.barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(true);

        binding.barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    String scannedData = result.getText();
                    if (scannedData.startsWith("STUDENT_ID|")) {
                        String[] parts = scannedData.split("\\|");
                        String idOrRoll = parts.length > 1 ? parts[1] : "N/A";
                        String dept = parts.length > 2 ? parts[2] : "MCA";
                        
                        // If a teacher is scanning, try to mark this student as present
                        String userRole = SharedPrefManager.getInstance(QRScannerActivity.this).getUserRole();
                        if ("teacher".equalsIgnoreCase(userRole)) {
                            String teacherUid = SharedPrefManager.getInstance(QRScannerActivity.this).getUserUid();
                            String date = DateTimeUtils.getCurrentDateDb();
                            boolean marked = DatabaseHelper.getInstance(QRScannerActivity.this)
                                    .markQRAttendance(idOrRoll, "sub_auto", date, teacherUid);
                            
                            if (marked) {
                                DialogHelper.showSuccessDialog(QRScannerActivity.this, "Student Attendance Marked",
                                        "Verified Student: " + idOrRoll + "\nDepartment: " + dept + "\n\nAttendance has been marked PRESENT for today.");
                            } else {
                                DialogHelper.showSuccessDialog(QRScannerActivity.this, "Student Verified",
                                        "Verified Student: " + idOrRoll + "\nDepartment: " + dept + "\n\n(Note: Attendance could not be auto-marked)");
                            }
                        } else {
                            DialogHelper.showSuccessDialog(QRScannerActivity.this, "Student Digital ID Verified",
                                    "Verified Student Record:\nRoll/ID: " + idOrRoll + "\nDepartment: " + dept + "\nStatus: Active System Member");
                        }
                    } else if (scannedData.startsWith("ATTENDANCE_QR|")) {
                        handleLegacyQr(scannedData);
                    } else {
                        validateDynamicSession(scannedData);
                    }
                }
            }
        });
    }

    private void handleLegacyQr(String scannedData) {
        String studentUid = SharedPrefManager.getInstance(this).getUserUid();
        if (studentUid.isEmpty()) studentUid = "uid_s1";

        String subjectId = "sub1";
        String teacherUid = "uid_t1";
        String date = DateTimeUtils.getCurrentDateDb();

        String[] parts = scannedData.split("\\|");
        if (parts.length >= 2) subjectId = parts[1];
        if (parts.length >= 3) teacherUid = parts[2];
        if (parts.length >= 4) date = parts[3];

        boolean marked = DatabaseHelper.getInstance(this).markQRAttendance(studentUid, subjectId, date, teacherUid);

        if (marked) {
            DialogHelper.showSuccessDialog(this, "Attendance Recorded",
                    "Your attendance has been marked PRESENT!\n\nSubject ID: " + subjectId.toUpperCase() + "\nDate: " + date);
        } else {
            DialogHelper.showErrorDialog(this, "Scanning Failed", "Could not record attendance in database.");
        }
    }

    private void validateDynamicSession(String sessionId) {
        FirebaseFirestore db = FirebaseService.getInstance().getFirestore();
        db.collection("qrSessions").document(sessionId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        QrSession session = snapshot.toObject(QrSession.class);
                        if (session != null) {
                            processAttendance(session);
                        } else {
                            DialogHelper.showErrorDialog(this, "INVALID QR", "This QR code is invalid.");
                        }
                    } else {
                        DialogHelper.showErrorDialog(this, "INVALID QR", "Please scan a valid session QR generated by your teacher.");
                    }
                })
                .addOnFailureListener(e -> {
                    DialogHelper.showErrorDialog(this, "Server Error", "Failed to validate QR session.");
                });
    }

    private void processAttendance(QrSession session) {
        long now = System.currentTimeMillis();
        if (now > session.getExpiresAt()) {
            DialogHelper.showErrorDialog(this, "QR EXPIRED", "This attendance session has expired. Please ask your teacher to generate a new QR.");
            return;
        }

        String userUid = SharedPrefManager.getInstance(this).getUserUid();
        User currentUser = SharedPrefManager.getInstance(this).getUser();
        
        if (currentUser == null || userUid.isEmpty()) {
            DialogHelper.showErrorDialog(this, "Auth Error", "Please login again to mark attendance.");
            return;
        }

        // Fetch Student Profile to validate Class/Division
        FirebaseService.getInstance().getFirestore().collection("students")
                .whereEqualTo("uid", userUid).get()
                .addOnSuccessListener(studentSnaps -> {
                    if (studentSnaps != null && !studentSnaps.isEmpty()) {
                        Student student = studentSnaps.getDocuments().get(0).toObject(Student.class);
                        if (student != null) {
                            validateClassAndMark(session, student, userUid);
                        } else {
                            validateDuplicateOnly(session, userUid, currentUser.getName());
                        }
                    } else {
                        // Fallback if student profile not found in students collection
                        validateDuplicateOnly(session, userUid, currentUser.getName());
                    }
                })
                .addOnFailureListener(e -> validateDuplicateOnly(session, userUid, currentUser.getName()));
    }

    private void validateClassAndMark(QrSession session, Student student, String userUid) {
        // Compare department and semester/division
        boolean deptMatch = session.getDepartmentId() == null || session.getDepartmentId().equals(student.getDepartmentId());
        boolean semMatch = session.getSemester() == null || session.getSemester().equals(student.getSemester());
        
        if (!deptMatch || !semMatch) {
             DialogHelper.showErrorDialog(this, "ATTENDANCE NOT ALLOWED", 
                     "This QR is for " + session.getDepartmentName() + " (Sem " + session.getSemester() + ").\n" +
                     "Your Info: " + student.getDepartmentName() + " (Sem " + student.getSemester() + ")");
             return;
        }

        validateDuplicateOnly(session, userUid, student != null ? student.getName() : "Student", student != null ? student.getRollNo() : "");
    }

    private void validateDuplicateOnly(QrSession session, String userUid, String userName) {
        Student localStudent = DatabaseHelper.getInstance(this).getStudentByUid(userUid);
        String rollNo = localStudent != null && localStudent.getRollNo() != null ? localStudent.getRollNo() : "";
        validateDuplicateOnly(session, userUid, userName, rollNo);
    }

    private void validateDuplicateOnly(QrSession session, String userUid, String userName, String rollNo) {
        String date = DateTimeUtils.getCurrentDateDb();
        String attId = date + "_" + userUid + "_" + session.getSubjectId();

        FirebaseService.getInstance().getFirestore().collection("attendance").document(attId).get()
                .addOnSuccessListener(attSnapshot -> {
                    if (attSnapshot.exists()) {
                        Attendance existing = attSnapshot.toObject(Attendance.class);
                        DialogHelper.showSuccessDialog(this, "ALREADY MARKED",
                                "Attendance already marked for this subject today.\n\nTime: " + (existing != null ? existing.getPunchInTime() : "N/A"));
                    } else {
                        markPresence(session, attId, userUid, userName, rollNo);
                    }
                });
    }

    private void markPresence(QrSession session, String attId, String userUid, String userName, String rollNo) {
        String time = DateTimeUtils.getCurrentTime();
        String cleanRoll = (rollNo != null && !rollNo.isEmpty()) ? rollNo : "STD_QR";
        Attendance attendance = new Attendance(
                attId,
                session.getDate(),
                userUid,
                userUid,
                userName,
                cleanRoll,
                session.getDepartmentId(),
                session.getDepartmentName(),
                session.getDivision(),
                session.getSemester(),
                session.getSubjectId(),
                session.getSubjectName(),
                Constants.STATUS_PRESENT,
                session.getTeacherUid()
        );
        attendance.setRollNo(cleanRoll);
        attendance.setTeacherId(session.getTeacherUid());
        attendance.setTeacherName(session.getTeacherName());
        attendance.setPunchInTime(time);
        attendance.setPunchInTimestamp(System.currentTimeMillis());
        attendance.setPunchInStatus("COMPLETED");
        attendance.setStatus(Constants.STATUS_PRESENT);

        DatabaseHelper.getInstance(this).insertOrUpdateAttendanceRecord(attendance);
        
        FirebaseService.getInstance().getFirestore().collection("attendance").document(attId)
                .set(attendance)
                .addOnSuccessListener(aVoid -> {
                    DialogHelper.showSuccessDialog(this, "✓ ATTENDANCE VERIFIED",
                            "Subject: " + session.getSubjectName() + "\nTeacher: " + session.getTeacherName() + "\nTime: " + time + "\n\nStatus: PRESENT");
                })
                .addOnFailureListener(e -> {
                    DialogHelper.showErrorDialog(this, "Sync Error", "Attendance recorded locally but failed to sync to cloud.");
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, null);
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }
}
