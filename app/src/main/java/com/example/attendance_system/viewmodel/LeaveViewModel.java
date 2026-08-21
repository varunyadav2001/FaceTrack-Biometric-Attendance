package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.repository.LeaveRepository;

import java.util.List;

public class LeaveViewModel extends AndroidViewModel {
    private final LeaveRepository repository;
    private final MutableLiveData<List<LeaveRequest>> leaveListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public LeaveViewModel(@NonNull Application application) {
        super(application);
        repository = new LeaveRepository();
    }

    public LiveData<List<LeaveRequest>> getLeaveListLiveData() { return leaveListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void applyLeave(LeaveRequest request) {
        loadingLiveData.setValue(true);
        repository.applyLeave(getApplication().getApplicationContext(), request, new LeaveRepository.LeaveActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void updateLeaveStatus(String leaveId, String status, String remarks) {
        loadingLiveData.setValue(true);
        repository.updateLeaveStatus(getApplication().getApplicationContext(), leaveId, status, remarks, new LeaveRepository.LeaveActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchPendingLeaves();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchStudentLeaves(String studentId) {
        loadingLiveData.setValue(true);
        repository.getLeavesByStudent(getApplication().getApplicationContext(), studentId, new LeaveRepository.LeaveListCallback() {
            @Override
            public void onSuccess(List<LeaveRequest> list) {
                loadingLiveData.setValue(false);
                leaveListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchPendingLeaves() {
        loadingLiveData.setValue(true);
        repository.getAllPendingLeaves(getApplication().getApplicationContext(), new LeaveRepository.LeaveListCallback() {
            @Override
            public void onSuccess(List<LeaveRequest> list) {
                loadingLiveData.setValue(false);
                leaveListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
