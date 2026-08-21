package com.example.attendance_system.repository;

import android.content.Context;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectRepository {

    public interface SubjectListCallback {
        void onSuccess(List<Subject> list);
        void onError(String message);
    }

    public interface SubjectActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public void addSubject(Context context, Subject subject, SubjectActionCallback callback) {
        try {
            DatabaseHelper.getInstance(context).insertSubject(subject);
            FirebaseService.getInstance().saveSubjectToFirestore(subject, new FirebaseService.FirestoreCallback<Void>() {
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

    public void getAllSubjects(Context context, SubjectListCallback callback) {
        try {
            List<Subject> local = DatabaseHelper.getInstance(context).getAllSubjects();
            if (!local.isEmpty()) callback.onSuccess(local);

            FirebaseService.getInstance().fetchSubjectsFromFirestore(null, null, new FirebaseService.FirestoreCallback<List<Subject>>() {
                @Override
                public void onSuccess(List<Subject> list) {
                    if (list != null) {
                        for (Subject s : list) {
                            DatabaseHelper.getInstance(context).insertSubject(s);
                        }
                    }
                    callback.onSuccess(list);
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

    public void getSubjectsByDeptAndSem(Context context, String departmentId, String semester, SubjectListCallback callback) {
        // Return local first
        List<Subject> local = DatabaseHelper.getInstance(context).getAllSubjects();
        List<Subject> filtered = new ArrayList<>();
        for (Subject s : local) {
            boolean deptMatch = (s.getDepartmentId() != null && s.getDepartmentId().equalsIgnoreCase(departmentId)) || 
                                (s.getDepartmentName() != null && s.getDepartmentName().equalsIgnoreCase(departmentId));
            boolean semMatch = semester == null || semester.isEmpty() || (s.getSemester() != null && s.getSemester().equalsIgnoreCase(semester));
            
            if (deptMatch && semMatch) {
                if (!filtered.contains(s)) filtered.add(s);
            }
        }
        if (!filtered.isEmpty()) callback.onSuccess(filtered);

        FirebaseService.getInstance().fetchSubjectsFromFirestore(departmentId, semester, new FirebaseService.FirestoreCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> list) {
                if (list != null && !list.isEmpty()) {
                    for (Subject s : list) {
                        DatabaseHelper.getInstance(context).insertSubject(s);
                    }
                    // Return latest filtered list from DB after merge
                    List<Subject> updatedLocal = DatabaseHelper.getInstance(context).getAllSubjects();
                    List<Subject> updatedFiltered = new ArrayList<>();
                    for (Subject s : updatedLocal) {
                        boolean deptMatch = (s.getDepartmentId() != null && s.getDepartmentId().equalsIgnoreCase(departmentId)) || 
                                            (s.getDepartmentName() != null && s.getDepartmentName().equalsIgnoreCase(departmentId));
                        boolean semMatch = semester == null || semester.isEmpty() || (s.getSemester() != null && s.getSemester().equalsIgnoreCase(semester));
                        if (deptMatch && semMatch) {
                            if (!updatedFiltered.contains(s)) updatedFiltered.add(s);
                        }
                    }
                    callback.onSuccess(updatedFiltered);
                } else {
                    // Only send result if nothing was found locally to avoid clearing existing UI
                    if (filtered.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (filtered.isEmpty()) callback.onError(error);
            }
        });
    }
}
