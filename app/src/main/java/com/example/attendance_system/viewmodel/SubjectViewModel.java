package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Subject;
import com.example.attendance_system.repository.SubjectRepository;

import java.util.List;

public class SubjectViewModel extends AndroidViewModel {
    private final SubjectRepository repository;
    private final MutableLiveData<List<Subject>> subjectListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public SubjectViewModel(@NonNull Application application) {
        super(application);
        repository = new SubjectRepository();
    }

    public LiveData<List<Subject>> getSubjectListLiveData() { return subjectListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void fetchSubjects() {
        loadingLiveData.setValue(true);
        repository.getAllSubjects(getApplication().getApplicationContext(), new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> list) {
                loadingLiveData.setValue(false);
                subjectListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchSubjectsByDeptAndSem(String departmentId, String semester) {
        subjectListLiveData.setValue(new java.util.ArrayList<>());
        loadingLiveData.setValue(true);
        repository.getSubjectsByDeptAndSem(getApplication().getApplicationContext(), departmentId, semester, new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> list) {
                loadingLiveData.setValue(false);
                subjectListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void addSubject(Subject subject) {
        loadingLiveData.setValue(true);
        repository.addSubject(getApplication().getApplicationContext(), subject, new SubjectRepository.SubjectActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchSubjects();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
