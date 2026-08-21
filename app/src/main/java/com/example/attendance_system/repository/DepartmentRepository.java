package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.models.Department;

import java.util.List;

public class DepartmentRepository {

    public interface DepartmentListCallback {
        void onSuccess(List<Department> list);
        void onError(String message);
    }

    public interface DepartmentActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void addDepartment(Context context, Department dept, DepartmentActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).insertDepartment(dept);
            com.example.attendance_system.firebase.FirebaseService.getInstance().saveDepartmentToFirestore(dept, new com.example.attendance_system.firebase.FirebaseService.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    callback.onSuccess();
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void getAllDepartments(Context context, DepartmentListCallback callback) {
        try {
            List<Department> local = DatabaseHelper.getInstance(context).getAllDepartments();
            if (!local.isEmpty()) callback.onSuccess(local);

            com.example.attendance_system.firebase.FirebaseService.getInstance().fetchDepartmentsFromFirestore(new com.example.attendance_system.firebase.FirebaseService.FirestoreCallback<List<Department>>() {
                @Override
                public void onSuccess(List<Department> list) {
                    if (list != null) {
                        for (Department d : list) {
                            DatabaseHelper.getInstance(context).insertDepartment(d);
                        }
                        callback.onSuccess(list);
                    }
                }

                @Override
                public void onError(String error) {
                    if (local.isEmpty()) callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getLocalizedMessage());
        }
    }
}
