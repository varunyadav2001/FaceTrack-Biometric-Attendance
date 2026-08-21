package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.NotificationItem;
import com.example.attendance_system.repository.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends AndroidViewModel {
    private final NotificationRepository repository;
    private final MutableLiveData<List<NotificationItem>> notificationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        repository = new NotificationRepository();
    }

    public LiveData<List<NotificationItem>> getNotificationsLiveData() { return notificationsLiveData; }
    public LiveData<Integer> getUnreadCountLiveData() { return unreadCountLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    public void fetchNotifications(String studentUid) {
        repository.getNotifications(getApplication().getApplicationContext(), studentUid, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<NotificationItem> list) {
                notificationsLiveData.setValue(list);
                int count = 0;
                if (list != null) {
                    for (NotificationItem item : list) {
                        if (!item.isRead()) count++;
                    }
                }
                unreadCountLiveData.setValue(count);
            }

            @Override
            public void onError(String message) {
                errorLiveData.setValue(message);
            }
        });
    }

    public void markAsRead(String notificationId, String studentUid) {
        repository.markAsRead(getApplication().getApplicationContext(), notificationId);
        fetchNotifications(studentUid);
    }

    public void markAllAsRead(String studentUid) {
        repository.markAllAsRead(getApplication().getApplicationContext(), studentUid);
        fetchNotifications(studentUid);
    }
}
