package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Attendance;

import java.util.List;

public class AttendanceRepository {

    public interface AttendanceListCallback {
        void onSuccess(List<Attendance> list);
        void onError(String message);
    }

    public interface AttendanceActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void saveAttendanceList(Context context, List<Attendance> attendanceList, AttendanceActionCallback callback) {
        try {
            // Save to local SQLite instantly so UI updates in 0 ms
            DatabaseHelper.getInstance(context).saveAttendanceBatch(attendanceList);

            // Background sync to Firebase Firestore
            FirebaseService.getInstance().syncAttendanceBatchToFirestore(attendanceList, null);

            callback.onSuccess();
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAttendanceByDateAndSubject(Context context, String date, String subjectId, String deptId, String section, String semester, AttendanceListCallback callback) {
        getAttendanceByTimetable(context, date, "", subjectId, deptId, section, semester, callback);
    }

    public void getAttendanceByTimetable(Context context, String date, String timetableId, String subjectId, String deptId, String section, String semester, AttendanceListCallback callback) {
        try {
            // Return local DB records instantly
            List<Attendance> localList = DatabaseHelper.getInstance(context).getAttendanceByTimetable(date, timetableId, subjectId, deptId, section, semester);
            callback.onSuccess(localList);

            // Asynchronously fetch latest data from Firebase Firestore
            FirebaseService.getInstance().fetchAttendanceFromFirestore(context, date, subjectId, deptId, section, semester, new FirebaseService.FirestoreCallback<List<Attendance>>() {
                @Override
                public void onSuccess(List<Attendance> firestoreList) {
                    if (firestoreList != null && !firestoreList.isEmpty()) {
                        DatabaseHelper.getInstance(context).saveAttendanceBatch(firestoreList);
                    }
                    List<Attendance> cleanList = DatabaseHelper.getInstance(context).getAttendanceByTimetable(date, timetableId, subjectId, deptId, section, semester);
                    callback.onSuccess(cleanList);
                }

                @Override
                public void onError(String error) {
                }
            });
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAttendanceByStudent(Context context, String studentUid, AttendanceListCallback callback) {
        try {
            // Return local DB records instantly so UI never waits for network latency
            List<Attendance> localList = DatabaseHelper.getInstance(context).getAttendanceByStudent(studentUid);
            callback.onSuccess(localList);

            // Real-time sync with Firebase Firestore
            FirebaseService.getInstance().fetchStudentAttendanceFromFirestore(studentUid, new FirebaseService.FirestoreCallback<List<Attendance>>() {
                @Override
                public void onSuccess(List<Attendance> firestoreList) {
                    if (firestoreList != null) {
                        DatabaseHelper.getInstance(context).saveAttendanceBatch(firestoreList);
                        List<Attendance> updatedList = DatabaseHelper.getInstance(context).getAttendanceByStudent(studentUid);
                        callback.onSuccess(updatedList.isEmpty() ? firestoreList : updatedList);
                    }
                }

                @Override
                public void onError(String error) {
                }
            });
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAttendanceForTeacher(Context context, String date, String teacherUid, String deptId, AttendanceListCallback callback) {
        try {
            // Return local first
            List<Attendance> local = DatabaseHelper.getInstance(context).getAttendanceForTeacher(date, teacherUid, deptId);
            if (!local.isEmpty()) callback.onSuccess(local);

            // Background fetch from firestore
            FirebaseService.getInstance().fetchAttendanceForTeacherFromFirestore(date, teacherUid, deptId, new FirebaseService.FirestoreCallback<List<Attendance>>() {
                @Override
                public void onSuccess(List<Attendance> firestoreList) {
                    if (firestoreList != null) {
                        DatabaseHelper.getInstance(context).saveAttendanceBatch(firestoreList);
                        callback.onSuccess(DatabaseHelper.getInstance(context).getAttendanceForTeacher(date, teacherUid, deptId));
                    }
                }

                @Override
                public void onError(String error) {
                    if (local.isEmpty()) callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }
}
