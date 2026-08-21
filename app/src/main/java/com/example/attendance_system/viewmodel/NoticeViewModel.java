package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Notice;
import com.example.attendance_system.repository.NoticeRepository;

import java.util.List;

public class NoticeViewModel extends AndroidViewModel {
    private final NoticeRepository repository;
    private final MutableLiveData<List<Notice>> noticeListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public NoticeViewModel(@NonNull Application application) {
        super(application);
        repository = new NoticeRepository();
    }

    public LiveData<List<Notice>> getNoticeListLiveData() { return noticeListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void fetchNotices() {
        loadingLiveData.setValue(true);
        repository.getAllNotices(getApplication().getApplicationContext(), new NoticeRepository.NoticeListCallback() {
            @Override
            public void onSuccess(List<Notice> list) {
                loadingLiveData.setValue(false);
                noticeListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void addNotice(Notice notice) {
        loadingLiveData.setValue(true);
        repository.addNotice(getApplication().getApplicationContext(), notice, new NoticeRepository.NoticeActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchNotices();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
