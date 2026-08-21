package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Department;
import com.example.attendance_system.repository.DepartmentRepository;

import java.util.List;

public class DepartmentViewModel extends AndroidViewModel {
    private final DepartmentRepository repository;
    private final MutableLiveData<List<Department>> departmentListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public DepartmentViewModel(@NonNull Application application) {
        super(application);
        repository = new DepartmentRepository();
    }

    public LiveData<List<Department>> getDepartmentListLiveData() { return departmentListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void fetchDepartments() {
        loadingLiveData.setValue(true);
        repository.getAllDepartments(getApplication().getApplicationContext(), new DepartmentRepository.DepartmentListCallback() {
            @Override
            public void onSuccess(List<Department> list) {
                loadingLiveData.setValue(false);
                departmentListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void addDepartment(Department dept) {
        loadingLiveData.setValue(true);
        repository.addDepartment(getApplication().getApplicationContext(), dept, new DepartmentRepository.DepartmentActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchDepartments();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
