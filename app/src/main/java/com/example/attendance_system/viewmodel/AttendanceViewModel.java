package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.repository.AttendanceRepository;

import java.util.List;

public class AttendanceViewModel extends AndroidViewModel {
    private final AttendanceRepository repository;
    private final MutableLiveData<List<Attendance>> attendanceListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        repository = new AttendanceRepository();
    }

    public LiveData<List<Attendance>> getAttendanceListLiveData() { return attendanceListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    public void saveAttendanceList(List<Attendance> list) {
        loadingLiveData.setValue(true);
        repository.saveAttendanceList(getApplication().getApplicationContext(), list, new AttendanceRepository.AttendanceActionCallback() {
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

    public void fetchAttendanceByDateAndSubject(String date, String subjectId, String deptId, String section, String semester) {
        fetchAttendanceByTimetable(date, "", subjectId, deptId, section, semester);
    }

    public void fetchAttendanceByTimetable(String date, String timetableId, String subjectId, String deptId, String section, String semester) {
        loadingLiveData.setValue(true);
        repository.getAttendanceByTimetable(getApplication().getApplicationContext(), date, timetableId, subjectId, deptId, section, semester, new AttendanceRepository.AttendanceListCallback() {
            @Override
            public void onSuccess(List<Attendance> list) {
                loadingLiveData.setValue(false);
                attendanceListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchAttendanceByStudent(String studentUid) {
        attendanceListLiveData.setValue(new java.util.ArrayList<>()); // Clear
        loadingLiveData.setValue(true);
        repository.getAttendanceByStudent(getApplication().getApplicationContext(), studentUid, new AttendanceRepository.AttendanceListCallback() {
            @Override
            public void onSuccess(List<Attendance> list) {
                loadingLiveData.setValue(false);
                attendanceListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void fetchAttendanceForTeacher(String date, String teacherUid, String deptId) {
        loadingLiveData.setValue(true);
        repository.getAttendanceForTeacher(getApplication().getApplicationContext(), date, teacherUid, deptId, new AttendanceRepository.AttendanceListCallback() {
            @Override
            public void onSuccess(List<Attendance> list) {
                loadingLiveData.setValue(false);
                attendanceListLiveData.setValue(list);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
