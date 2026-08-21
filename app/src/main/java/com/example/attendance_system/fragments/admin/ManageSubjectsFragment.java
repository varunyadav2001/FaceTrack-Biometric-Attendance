package com.example.attendance_system.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.SubjectAdapter;
import com.example.attendance_system.databinding.FragmentManageSubjectsBinding;
import com.example.attendance_system.viewmodel.SubjectViewModel;

import java.util.ArrayList;
import java.util.List;

public class ManageSubjectsFragment extends Fragment {

    private FragmentManageSubjectsBinding binding;
    private SubjectViewModel viewModel;
    private com.example.attendance_system.viewmodel.DepartmentViewModel deptViewModel;
    private com.example.attendance_system.viewmodel.TeacherViewModel teacherViewModel;
    private SubjectAdapter adapter;
    private java.util.List<com.example.attendance_system.models.Department> allDepts = new java.util.ArrayList<>();
    private java.util.List<com.example.attendance_system.models.Teacher> allTeachers = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageSubjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SubjectViewModel.class);
        deptViewModel = new ViewModelProvider(this).get(com.example.attendance_system.viewmodel.DepartmentViewModel.class);
        teacherViewModel = new ViewModelProvider(this).get(com.example.attendance_system.viewmodel.TeacherViewModel.class);
        
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubjectAdapter();
        binding.rvSubjects.setAdapter(adapter);

        viewModel.getSubjectListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) adapter.setSubjectList(list);
        });

        deptViewModel.getDepartmentListLiveData().observe(getViewLifecycleOwner(), depts -> {
            if (depts != null) allDepts = depts;
        });

        teacherViewModel.getTeacherListLiveData().observe(getViewLifecycleOwner(), teachers -> {
            if (teachers != null) allTeachers = teachers;
        });

        binding.fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        viewModel.fetchSubjects();
        deptViewModel.fetchDepartments();
        teacherViewModel.fetchTeachers();
    }

    private void showAddSubjectDialog() {
        if (!isAdded() || getContext() == null) return;

        com.example.attendance_system.database.DatabaseHelper db = com.example.attendance_system.database.DatabaseHelper.getInstance(requireContext());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Teacher Selection Dropdown
        final android.widget.Spinner spinnerTeacher = new android.widget.Spinner(requireContext());
        java.util.List<String> teacherDisplayNames = new java.util.ArrayList<>();
        teacherDisplayNames.add("Select Teacher");

        final java.util.List<com.example.attendance_system.models.Teacher> selectableTeachers = new java.util.ArrayList<>();
        
        for (com.example.attendance_system.models.Teacher t : allTeachers) {
            int count = db.getAssignedSubjectCountForTeacher(t.getUid(), t.getName());
            String label = t.getName() + " (" + count + "/5 Subjects" + (count >= 5 ? " - Limit Reached" : "") + ")";
            teacherDisplayNames.add(label);
            selectableTeachers.add(t);
        }

        android.widget.ArrayAdapter<String> teacherAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, teacherDisplayNames);
        spinnerTeacher.setAdapter(teacherAdapter);
        layout.addView(spinnerTeacher);

        // 2. Subject Selection Dropdown (NOT free text)
        final android.widget.Spinner spinnerSubject = new android.widget.Spinner(requireContext());
        final java.util.List<String> currentAvailableSubjects = new java.util.ArrayList<>();
        currentAvailableSubjects.add("Select Subject (Choose Teacher First)");
        android.widget.ArrayAdapter<String> subjectAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, currentAvailableSubjects);
        spinnerSubject.setAdapter(subjectAdapter);
        layout.addView(spinnerSubject);

        // 3. Semester Field
        final com.google.android.material.textfield.TextInputEditText etSem = new com.google.android.material.textfield.TextInputEditText(requireContext());
        etSem.setHint("Semester (1-8)");
        etSem.setText("1");
        etSem.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etSem);

        // Standard 5 Timetable Subjects
        String[] standardSubjects = {
                "Cloud Computing & DevOps",
                "Data Science & AI",
                "Web Technologies",
                "Database Systems",
                "Advanced Java Programming"
        };

        // Intelligent filtering: update available subjects when a teacher is selected
        spinnerTeacher.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                currentAvailableSubjects.clear();
                if (position <= 0) {
                    currentAvailableSubjects.add("Select Subject (Choose Teacher First)");
                    spinnerSubject.setEnabled(false);
                } else {
                    com.example.attendance_system.models.Teacher selectedT = selectableTeachers.get(position - 1);
                    int count = db.getAssignedSubjectCountForTeacher(selectedT.getUid(), selectedT.getName());
                    
                    if (count >= 5) {
                        currentAvailableSubjects.add("This teacher already has 5 assigned subjects.");
                        spinnerSubject.setEnabled(false);
                        android.widget.Toast.makeText(getContext(), "This teacher already has 5 assigned subjects.", android.widget.Toast.LENGTH_LONG).show();
                    } else {
                        List<com.example.attendance_system.models.Subject> assigned = db.getAssignedSubjectsForTeacher(selectedT.getUid(), selectedT.getName());
                        java.util.Set<String> assignedNames = new java.util.HashSet<>();
                        if (assigned != null) {
                            for (com.example.attendance_system.models.Subject s : assigned) {
                                if (s.getName() != null) assignedNames.add(s.getName().trim().toLowerCase());
                            }
                        }

                        currentAvailableSubjects.add("Select Subject");
                        for (String stdSub : standardSubjects) {
                            if (!assignedNames.contains(stdSub.trim().toLowerCase())) {
                                currentAvailableSubjects.add(stdSub);
                            }
                        }

                        if (currentAvailableSubjects.size() <= 1) {
                            currentAvailableSubjects.clear();
                            currentAvailableSubjects.add("All standard subjects assigned to this teacher");
                            spinnerSubject.setEnabled(false);
                        } else {
                            spinnerSubject.setEnabled(true);
                        }
                    }
                }
                android.widget.ArrayAdapter<String> updatedAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, currentAvailableSubjects);
                spinnerSubject.setAdapter(updatedAdapter);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add / Assign Subject")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    int selectedTeacherPos = spinnerTeacher.getSelectedItemPosition();
                    int selectedSubjectPos = spinnerSubject.getSelectedItemPosition();
                    String sem = etSem.getText() != null ? etSem.getText().toString().trim() : "1";

                    if (selectedTeacherPos <= 0 || selectedSubjectPos <= 0 || !spinnerSubject.isEnabled()) {
                        android.widget.Toast.makeText(getContext(), "Please select a valid teacher and unassigned subject", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    com.example.attendance_system.models.Teacher selectedTeacher = selectableTeachers.get(selectedTeacherPos - 1);
                    int currentCount = db.getAssignedSubjectCountForTeacher(selectedTeacher.getUid(), selectedTeacher.getName());

                    if (currentCount >= 5) {
                        android.widget.Toast.makeText(getContext(), "This teacher already has 5 assigned subjects.", android.widget.Toast.LENGTH_LONG).show();
                        return;
                    }

                    String name = spinnerSubject.getSelectedItem().toString();
                    if (name.contains("Select") || name.contains("already") || name.contains("Choose")) {
                        android.widget.Toast.makeText(getContext(), "Please select a valid subject", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String teacherId = selectedTeacher.getUid() != null && !selectedTeacher.getUid().isEmpty() ? selectedTeacher.getUid() : selectedTeacher.getTeacherId();
                    String teacherName = selectedTeacher.getName();

                    String subId = "sub_" + Math.abs(name.hashCode());
                    String generatedCode = "SUB-" + (name.length() >= 3 ? name.substring(0, 3).toUpperCase() : "101");

                    com.example.attendance_system.models.Subject s = new com.example.attendance_system.models.Subject();
                    s.setSubjectId(subId);
                    s.setCode(generatedCode);
                    s.setName(name);
                    s.setDepartmentId("d1");
                    s.setDepartmentName("MCA");
                    s.setSemester(sem);
                    s.setCredits(4);
                    s.setTeacherId(teacherId);
                    s.setTeacherName(teacherName);
                    
                    db.insertSubject(s);
                    viewModel.addSubject(s);
                    android.widget.Toast.makeText(getContext(), "Subject '" + name + "' assigned to " + teacherName, android.widget.Toast.LENGTH_SHORT).show();
                    viewModel.fetchSubjects();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
