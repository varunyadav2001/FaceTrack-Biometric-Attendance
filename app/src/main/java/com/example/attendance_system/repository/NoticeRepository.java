package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.models.Notice;

import java.util.List;

public class NoticeRepository {

    public interface NoticeListCallback {
        void onSuccess(List<Notice> list);
        void onError(String message);
    }

    public interface NoticeActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void addNotice(Context context, Notice notice, NoticeActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).insertNotice(notice);
            callback.onSuccess();
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAllNotices(Context context, NoticeListCallback callback) {
        try {
            List<Notice> list = DatabaseHelper.getInstance(context).getAllNotices();
            callback.onSuccess(list);
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }
}
