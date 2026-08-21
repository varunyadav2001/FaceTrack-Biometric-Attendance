package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Student;

import java.util.List;

public class StudentRepository {

    public interface StudentListCallback {
        void onSuccess(List<Student> students);
        void onError(String message);
    }

    public interface StudentActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void addStudent(Context context, Student student, boolean createAccount, StudentActionCallback callback) {
        addStudent(context, student, "student123", createAccount, callback);
    }

    public void addStudent(Context context, Student student, String password, boolean createAccount, StudentActionCallback callback) {
        try {
            // Save to local SQLite instantly with custom password
            DatabaseHelper.getInstance(context).insertOrUpdateStudent(student, password);

            // Background sync to Firebase Firestore & Auth
            FirebaseService.getInstance().saveStudentToFirestore(student, password, createAccount, null);

            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAllStudents(Context context, StudentListCallback callback) {
        try {
            // Return local DB records instantly so UI never waits for network latency
            List<Student> localList = DatabaseHelper.getInstance(context).getAllStudents();
            callback.onSuccess(localList);

            // Asynchronously fetch latest data from Firebase Firestore
            FirebaseService.getInstance().fetchStudentsFromFirestore(new FirebaseService.FirestoreCallback<List<Student>>() {
                @Override
                public void onSuccess(List<Student> firestoreList) {
                    if (firestoreList != null && !firestoreList.isEmpty()) {
                        for (Student s : firestoreList) {
                            DatabaseHelper.getInstance(context).insertOrUpdateStudent(s);
                        }
                        callback.onSuccess(DatabaseHelper.getInstance(context).getAllStudents());
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

    public void getStudentByUid(Context context, String uid, FirebaseService.FirestoreCallback<Student> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("Invalid UID");
            return;
        }

        // Try local first
        Student local = DatabaseHelper.getInstance(context).getStudentByUid(uid);
        if (local != null) {
            callback.onSuccess(local);
        }

        // Always fetch from cloud to ensure latest data and correct UID mapping
        FirebaseService.getInstance().getStudentByUid(uid, new FirebaseService.FirestoreCallback<Student>() {
            @Override
            public void onSuccess(Student student) {
                if (student != null) {
                    // Update local DB with fresh data from Cloud
                    DatabaseHelper.getInstance(context).insertOrUpdateStudent(student);
                    callback.onSuccess(student);
                } else if (local == null) {
                    callback.onError("Student not found in cloud");
                }
            }

            @Override
            public void onError(String error) {
                if (local == null) callback.onError(error);
            }
        });
    }

    public void deleteStudent(Context context, String studentId, StudentActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).deleteStudent(studentId);
            FirebaseService.getInstance().deleteStudentFromFirestore(studentId, null);
            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getLocalizedMessage());
        }
    }
}
