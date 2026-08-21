package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Student;
import com.example.attendance_system.repository.StudentRepository;

import java.util.List;

public class StudentViewModel extends AndroidViewModel {
    private final StudentRepository repository;
    private final MutableLiveData<List<Student>> studentListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Student> studentLiveData = new MutableLiveData<>();

    public StudentViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository();
    }

    public LiveData<Student> getStudentLiveData() { return studentLiveData; }
    public LiveData<List<Student>> getStudentListLiveData() { return studentListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void fetchStudentByUid(String uid) {
        studentLiveData.setValue(null);
        loadingLiveData.setValue(true);
        repository.getStudentByUid(getApplication().getApplicationContext(), uid, new com.example.attendance_system.firebase.FirebaseService.FirestoreCallback<Student>() {
            @Override
            public void onSuccess(Student student) {
                loadingLiveData.setValue(false);
                studentLiveData.setValue(student);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void fetchStudents() {
        loadingLiveData.setValue(true);
        repository.getAllStudents(getApplication().getApplicationContext(), new StudentRepository.StudentListCallback() {
            @Override
            public void onSuccess(List<Student> students) {
                loadingLiveData.setValue(false);
                studentListLiveData.setValue(students);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void addStudent(Student student) {
        addStudent(student, "student123");
    }

    public void addStudent(Student student, String password) {
        loadingLiveData.setValue(true);
        repository.addStudent(getApplication().getApplicationContext(), student, password, true, new StudentRepository.StudentActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchStudents();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void deleteStudent(String id) {
        loadingLiveData.setValue(true);
        repository.deleteStudent(getApplication().getApplicationContext(), id, new StudentRepository.StudentActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
                fetchStudents();
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
