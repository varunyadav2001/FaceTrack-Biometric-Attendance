package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.repository.TeacherRepository;

import java.util.List;

public class TeacherViewModel extends AndroidViewModel {
    private final TeacherRepository repository;
    private final MutableLiveData<List<Teacher>> teacherListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Teacher> teacherLiveData = new MutableLiveData<>();

    public TeacherViewModel(@NonNull Application application) {
        super(application);
        repository = new TeacherRepository();
    }

    public LiveData<Teacher> getTeacherLiveData() { return teacherLiveData; }
    public LiveData<List<Teacher>> getTeacherListLiveData() { return teacherListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void fetchTeachers() {
        loadingLiveData.setValue(true);
        repository.getAllTeachers(getApplication().getApplicationContext(), new TeacherRepository.TeacherListCallback() {
            @Override
            public void onSuccess(List<Teacher> teachers) {
                loadingLiveData.setValue(false);
                teacherListLiveData.setValue(teachers);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void addTeacher(Teacher teacher) {
        addTeacher(teacher, "teacher123");
    }

    public void addTeacher(Teacher teacher, String password) {
        loadingLiveData.setValue(true);
        repository.addTeacher(getApplication().getApplicationContext(), teacher, password, true, new TeacherRepository.TeacherActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchTeachers();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void deleteTeacher(String id) {
        loadingLiveData.setValue(true);
        repository.deleteTeacher(getApplication().getApplicationContext(), id, new TeacherRepository.TeacherActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchTeachers();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchTeacherByUid(String uid) {
        loadingLiveData.setValue(true);
        repository.getTeacherByUid(getApplication().getApplicationContext(), uid, new com.example.attendance_system.firebase.FirebaseService.FirestoreCallback<Teacher>() {
            @Override
            public void onSuccess(Teacher teacher) {
                loadingLiveData.setValue(false);
                teacherLiveData.setValue(teacher);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }
}
