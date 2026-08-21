package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.NotificationItem;

import java.util.List;

public class NotificationRepository {

    public interface NotificationListCallback {
        void onSuccess(List<NotificationItem> list);
        void onError(String message);
    }

    public void getNotifications(Context context, String studentUid, NotificationListCallback callback) {
        // Return local if available (though notifications are better fetched fresh)
        List<NotificationItem> localList = DatabaseHelper.getInstance(context).getNotificationsForStudent(studentUid);
        if (!localList.isEmpty()) {
            callback.onSuccess(localList);
        }

        FirebaseService.getInstance().fetchNotificationsFromFirestore(studentUid, new FirebaseService.FirestoreCallback<List<NotificationItem>>() {
            @Override
            public void onSuccess(List<NotificationItem> firestoreList) {
                if (firestoreList != null) {
                    for (NotificationItem item : firestoreList) {
                        DatabaseHelper.getInstance(context).insertNotification(item);
                    }
                    callback.onSuccess(DatabaseHelper.getInstance(context).getNotificationsForStudent(studentUid));
                }
            }

            @Override
            public void onError(String error) {
                if (localList.isEmpty()) callback.onError(error);
            }
        });
    }

    public void markAsRead(Context context, String notificationId) {
        DatabaseHelper.getInstance(context).markNotificationAsRead(notificationId);
        FirebaseService.getInstance().markNotificationAsRead(notificationId, null);
    }

    public void markAllAsRead(Context context, String studentUid) {
        DatabaseHelper.getInstance(context).markAllNotificationsAsRead(studentUid);
        FirebaseService.getInstance().markAllNotificationsAsRead(studentUid, null);
    }
}
