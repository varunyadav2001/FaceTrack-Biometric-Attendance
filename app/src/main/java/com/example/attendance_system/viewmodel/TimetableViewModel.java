package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.TimetableItem;
import com.example.attendance_system.repository.TimetableRepository;

import java.util.List;

public class TimetableViewModel extends AndroidViewModel {
    private final TimetableRepository repository;
    private final MutableLiveData<List<TimetableItem>> timetableLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<TimetableItem>> weeklyTimetableLiveData = new MutableLiveData<>();

    public TimetableViewModel(@NonNull Application application) {
        super(application);
        repository = new TimetableRepository();
    }

    public LiveData<List<TimetableItem>> getTimetableLiveData() { return timetableLiveData; }
    public LiveData<List<TimetableItem>> getWeeklyTimetableLiveData() { return weeklyTimetableLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    public void fetchTimetableForDay(String departmentId, String divisionId, String day) {
        repository.getTimetableForDay(getApplication().getApplicationContext(), departmentId, divisionId, day, new TimetableRepository.TimetableCallback() {
            @Override
            public void onSuccess(List<TimetableItem> timetableItems) {
                timetableLiveData.setValue(timetableItems);
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void fetchWeeklyTimetable(String departmentId, String divisionId) {
        repository.getWeeklyTimetable(getApplication().getApplicationContext(), departmentId, divisionId, new TimetableRepository.TimetableCallback() {
            @Override
            public void onSuccess(List<TimetableItem> timetableItems) {
                weeklyTimetableLiveData.setValue(timetableItems);
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void fetchTeacherTimetable(String teacherUid, String day) {
        repository.getTeacherTimetable(getApplication().getApplicationContext(), teacherUid, day, new TimetableRepository.TimetableCallback() {
            @Override
            public void onSuccess(List<TimetableItem> timetableItems) {
                timetableLiveData.setValue(timetableItems);
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
}
