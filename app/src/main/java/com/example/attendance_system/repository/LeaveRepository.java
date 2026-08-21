package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.LeaveRequest;

import java.util.List;

public class LeaveRepository {

    public interface LeaveListCallback {
        void onSuccess(List<LeaveRequest> list);
        void onError(String message);
    }

    public interface LeaveActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void applyLeave(Context context, LeaveRequest request, LeaveActionCallback callback) {
        try {
            // Save to local SQLite instantly so UI updates in 0 ms
            DatabaseHelper.getInstance(context).insertLeave(request);

            // Background sync to Firebase Firestore
            FirebaseService.getInstance().saveLeaveToFirestore(request, null);

            callback.onSuccess();
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void updateLeaveStatus(Context context, String leaveId, String status, String remarks, LeaveActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).updateLeaveStatus(leaveId, status, remarks);
            FirebaseService.getInstance().updateLeaveStatusInFirestore(leaveId, status, remarks, null);
            callback.onSuccess();
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getLeavesByStudent(Context context, String studentId, LeaveListCallback callback) {
        try {
            List<LeaveRequest> localList = DatabaseHelper.getInstance(context).getLeavesByStudent(studentId);
            callback.onSuccess(localList);

            FirebaseService.getInstance().listenLeavesFromFirestore(new FirebaseService.FirestoreCallback<List<LeaveRequest>>() {
                @Override
                public void onSuccess(List<LeaveRequest> firestoreLeaves) {
                    if (firestoreLeaves != null) {
                        for (LeaveRequest r : firestoreLeaves) {
                            DatabaseHelper.getInstance(context).insertLeave(r);
                        }
                        callback.onSuccess(DatabaseHelper.getInstance(context).getLeavesByStudent(studentId));
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

    public void getAllPendingLeaves(Context context, LeaveListCallback callback) {
        try {
            List<LeaveRequest> localList = DatabaseHelper.getInstance(context).getAllPendingLeaves();
            callback.onSuccess(localList);

            FirebaseService.getInstance().listenLeavesFromFirestore(new FirebaseService.FirestoreCallback<List<LeaveRequest>>() {
                @Override
                public void onSuccess(List<LeaveRequest> firestoreLeaves) {
                    if (firestoreLeaves != null) {
                        for (LeaveRequest r : firestoreLeaves) {
                            DatabaseHelper.getInstance(context).insertLeave(r);
                        }
                        callback.onSuccess(DatabaseHelper.getInstance(context).getAllPendingLeaves());
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
}
