package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Teacher;

import java.util.List;

public class TeacherRepository {

    public interface TeacherListCallback {
        void onSuccess(List<Teacher> teachers);
        void onError(String message);
    }

    public interface TeacherActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void addTeacher(Context context, Teacher teacher, boolean createAccount, TeacherActionCallback callback) {
        addTeacher(context, teacher, "teacher123", createAccount, callback);
    }

    public void addTeacher(Context context, Teacher teacher, String password, boolean createAccount, TeacherActionCallback callback) {
        try {
            // Save to local SQLite instantly with custom password
            DatabaseHelper.getInstance(context).insertOrUpdateTeacher(teacher, password);

            // Background sync to Firebase Firestore & Auth
            FirebaseService.getInstance().saveTeacherToFirestore(teacher, password, createAccount, null);

            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAllTeachers(Context context, TeacherListCallback callback) {
        try {
            // Return local DB records instantly so UI never waits for network latency
            List<Teacher> localList = DatabaseHelper.getInstance(context).getAllTeachers();
            callback.onSuccess(localList);

            // Asynchronously fetch latest data from Firebase Firestore
            FirebaseService.getInstance().fetchTeachersFromFirestore(new FirebaseService.FirestoreCallback<List<Teacher>>() {
                @Override
                public void onSuccess(List<Teacher> firestoreList) {
                    if (firestoreList != null && !firestoreList.isEmpty()) {
                        for (Teacher t : firestoreList) {
                            DatabaseHelper.getInstance(context).insertOrUpdateTeacher(t);
                        }
                        callback.onSuccess(DatabaseHelper.getInstance(context).getAllTeachers());
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

    public void deleteTeacher(Context context, String teacherId, TeacherActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).deleteTeacher(teacherId);
            FirebaseService.getInstance().deleteTeacherFromFirestore(teacherId, null);
            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getLocalizedMessage());
        }
    }

    public void getTeacherByUid(Context context, String uid, FirebaseService.FirestoreCallback<Teacher> callback) {
        // Try local first
        Teacher local = null;
        List<Teacher> all = DatabaseHelper.getInstance(context).getAllTeachers();
        for (Teacher t : all) {
            if (t.getUid() != null && t.getUid().equals(uid)) {
                local = t;
                break;
            }
        }
        if (local != null) callback.onSuccess(local);

        FirebaseService.getInstance().fetchTeachersFromFirestore(new FirebaseService.FirestoreCallback<List<Teacher>>() {
            @Override
            public void onSuccess(List<Teacher> list) {
                if (list != null) {
                    for (Teacher t : list) {
                        DatabaseHelper.getInstance(context).insertOrUpdateTeacher(t);
                        if (t.getUid() != null && t.getUid().equals(uid)) {
                            callback.onSuccess(t);
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Ignore error if local was found
            }
        });
    }
}
