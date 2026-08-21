package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.TimetableItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimetableRepository {

    public interface TimetableCallback {
        void onSuccess(List<TimetableItem> timetableItems);
        void onError(String error);
    }

    public void getTimetableForDay(Context context, String departmentId, String divisionId, String day, TimetableCallback callback) {
        // Return local DB cached data first
        List<TimetableItem> localList = DatabaseHelper.getInstance(context).getTimetableForStudent(departmentId, divisionId, day);

        if (!localList.isEmpty()) {
            localList.sort(Comparator.comparingInt(TimetableItem::getLectureNumber));
            callback.onSuccess(localList);
        }

        // Fetch asynchronously from Firestore
        FirebaseService.getInstance().fetchTimetableFromFirestore(departmentId, divisionId, day, new FirebaseService.FirestoreCallback<List<TimetableItem>>() {
            @Override
            public void onSuccess(List<TimetableItem> firestoreList) {
                if (firestoreList != null && !firestoreList.isEmpty()) {
                    for (TimetableItem item : firestoreList) {
                        DatabaseHelper.getInstance(context).insertTimetableItem(item);
                    }
                    List<TimetableItem> updatedList = DatabaseHelper.getInstance(context).getTimetableForStudent(departmentId, divisionId, day);
                    updatedList.sort(Comparator.comparingInt(TimetableItem::getLectureNumber));
                    callback.onSuccess(updatedList);
                } else if (localList.isEmpty()) {
                    // If both local and firestore are empty, generate empty slots for the UI
                    callback.onSuccess(generateEmptySlots(day, departmentId, divisionId, ""));
                }
            }

            @Override
            public void onError(String error) {
                if (localList.isEmpty()) {
                    callback.onError(error);
                }
            }
        });
    }

    public void getWeeklyTimetable(Context context, String departmentId, String divisionId, TimetableCallback callback) {
        FirebaseService.getInstance().fetchWeeklyTimetableFromFirestore(departmentId, divisionId, new FirebaseService.FirestoreCallback<List<TimetableItem>>() {
            @Override
            public void onSuccess(List<TimetableItem> firestoreList) {
                if (firestoreList != null && !firestoreList.isEmpty()) {
                    for (TimetableItem item : firestoreList) {
                        DatabaseHelper.getInstance(context).insertTimetableItem(item);
                    }
                    callback.onSuccess(firestoreList);
                } else {
                    // Generate a full week of empty slots if nothing found
                    List<TimetableItem> fullWeek = new ArrayList<>();
                    String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    for (String d : days) {
                        fullWeek.addAll(generateEmptySlots(d, departmentId, divisionId, ""));
                    }
                    callback.onSuccess(fullWeek);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getTeacherTimetable(Context context, String teacherUid, String day, TimetableCallback callback) {
        List<TimetableItem> localList = DatabaseHelper.getInstance(context).getTimetableForTeacher(teacherUid, "", day);
        if (!localList.isEmpty()) {
            callback.onSuccess(localList);
        }

        FirebaseService.getInstance().fetchTeacherTimetableFromFirestore(teacherUid, day, new FirebaseService.FirestoreCallback<List<TimetableItem>>() {
            @Override
            public void onSuccess(List<TimetableItem> list) {
                if (list != null && !list.isEmpty()) {
                    callback.onSuccess(list);
                } else if (!localList.isEmpty()) {
                    callback.onSuccess(localList);
                } else {
                    List<TimetableItem> fallback = DatabaseHelper.getInstance(context).getTimetableForTeacher(teacherUid, "", day);
                    callback.onSuccess(fallback);
                }
            }

            @Override
            public void onError(String error) {
                if (!localList.isEmpty()) {
                    callback.onSuccess(localList);
                } else {
                    callback.onError(error);
                }
            }
        });
    }

    public static List<TimetableItem> generateEmptySlots(String day, String deptId, String div, String sem) {
        List<TimetableItem> list = new ArrayList<>();
        
        // Slot 1: 11:00 AM - 12:00 PM
        list.add(new TimetableItem("L1_"+day+"_"+div, day, "11:00 AM", "12:00 PM", "sub_m1", "Java Programming", "", "Not Assigned", "Room 101", deptId, "MCA", div, sem, 1, false));
        
        // Slot 2: 12:00 PM - 01:00 PM
        list.add(new TimetableItem("L2_"+day+"_"+div, day, "12:00 PM", "01:00 PM", "sub_m2", "Cloud Computing & DevOps", "", "Not Assigned", "Room 102", deptId, "MCA", div, sem, 2, false));
        
        // LUNCH BREAK: 01:00 PM - 02:00 PM
        list.add(new TimetableItem("BREAK_"+day+"_"+div, day, "01:00 PM", "02:00 PM", "", "LUNCH BREAK", "", "", "Cafeteria", deptId, "MCA", div, sem, 0, true));
        
        // Slot 3: 02:00 PM - 03:00 PM
        list.add(new TimetableItem("L3_"+day+"_"+div, day, "02:00 PM", "03:00 PM", "sub_m3", "Data Science & AI", "", "Not Assigned", "Room 103", deptId, "MCA", div, sem, 3, false));
        
        // Slot 4: 03:00 PM - 04:00 PM
        list.add(new TimetableItem("L4_"+day+"_"+div, day, "03:00 PM", "04:00 PM", "sub_m4", "Web Technologies", "", "Not Assigned", "Room 104", deptId, "MCA", div, sem, 4, false));
        
        // Slot 5: 04:00 PM - 05:00 PM
        list.add(new TimetableItem("L5_"+day+"_"+div, day, "04:00 PM", "05:00 PM", "sub_m5", "Database Systems", "", "Not Assigned", "Room 105", deptId, "MCA", div, sem, 5, false));
        
        return list;
    }
}
