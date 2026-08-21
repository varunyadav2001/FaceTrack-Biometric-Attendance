package com.example.attendance_system.fragments.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.AttendanceAdapter;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentAttendanceReportBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Department;
import com.example.attendance_system.models.Subject;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.TimetableItem;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.ExportUtils;
import com.example.attendance_system.viewmodel.AttendanceViewModel;
import com.example.attendance_system.viewmodel.DepartmentViewModel;
import com.example.attendance_system.viewmodel.SubjectViewModel;
import com.example.attendance_system.viewmodel.TeacherViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceReportFragment extends Fragment {

    private FragmentAttendanceReportBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private TeacherViewModel teacherViewModel;
    private DepartmentViewModel departmentViewModel;
    private SubjectViewModel subjectViewModel;
    private AttendanceAdapter adapter;

    private List<Attendance> currentList = new ArrayList<>();
    private List<Teacher> allTeachers = new ArrayList<>();
    private List<Department> allDepts = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();

    private String selectedDept = "All Departments";
    private String selectedDiv = "All Divisions";
    private Teacher selectedTeacher = null;
    private Subject selectedSubject = null;
    private String selectedDate = DateTimeUtils.getCurrentDateDb();
    private String selectedLecture = "All Lectures";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        teacherViewModel = new ViewModelProvider(this).get(TeacherViewModel.class);
        departmentViewModel = new ViewModelProvider(this).get(DepartmentViewModel.class);
        subjectViewModel = new ViewModelProvider(this).get(SubjectViewModel.class);

        binding.rvReportList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttendanceAdapter((attendance, newStatus) -> {});
        binding.rvReportList.setAdapter(adapter);

        binding.etDateFilter.setText(selectedDate);
        binding.etDateFilter.setOnClickListener(v -> showDatePicker());

        setupObservers();
        setupListeners();
        loadInitialData();

        binding.cardExportPDF.setOnClickListener(v -> generatePDF());
        binding.cardExportExcel.setOnClickListener(v -> generateExcel());
    }

    private void loadInitialData() {
        departmentViewModel.fetchDepartments();
        teacherViewModel.fetchTeachers();
        subjectViewModel.fetchSubjects();
        performQuery();
    }

    private void setupObservers() {
        departmentViewModel.getDepartmentListLiveData().observe(getViewLifecycleOwner(), depts -> {
            if (depts != null) {
                allDepts = depts;
                populateDeptSpinner();
            }
        });

        teacherViewModel.getTeacherListLiveData().observe(getViewLifecycleOwner(), teachers -> {
            if (teachers != null) {
                allTeachers = teachers;
                populateTeacherSpinner();
            }
        });

        subjectViewModel.getSubjectListLiveData().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                allSubjects = subjects;
                populateSubjectSpinner();
            }
        });

        attendanceViewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                currentList = list;
                adapter.setAttendanceList(list);
                updateSummaryCard(list);
            } else {
                currentList = new ArrayList<>();
                adapter.setAttendanceList(new ArrayList<>());
                updateSummaryCard(new ArrayList<>());
            }
        });
    }

    private void populateDeptSpinner() {
        if (!isAdded() || getContext() == null) return;
        List<String> items = new ArrayList<>();
        items.add("All Departments");
        for (Department d : allDepts) {
            items.add(d.getCode() != null ? d.getCode() : d.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinnerDeptFilter.setAdapter(adapter);
    }

    private void populateDivSpinner() {
        if (!isAdded() || getContext() == null) return;
        List<String> items = new ArrayList<>();
        items.add("All Divisions");
        items.add("Div A");
        items.add("Div B");
        items.add("Div C");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinnerDivFilter.setAdapter(adapter);
    }

    private void populateTeacherSpinner() {
        if (!isAdded() || getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<String> items = new ArrayList<>();
        items.add("All Teachers");

        for (Teacher t : allTeachers) {
            if ("All Departments".equals(selectedDept) || t.getDepartmentName().equalsIgnoreCase(selectedDept) || t.getDepartmentId().equalsIgnoreCase(selectedDept)) {
                int count = db.getAssignedSubjectCountForTeacher(t.getUid(), t.getName());
                items.add(t.getName() + " (" + count + "/5 Subjects)");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinnerTeacherFilter.setAdapter(adapter);
    }

    private void populateSubjectSpinner() {
        if (!isAdded() || getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<String> items = new ArrayList<>();
        items.add("All Subjects");

        if (selectedTeacher != null) {
            List<Subject> teacherSubjects = db.getAssignedSubjectsForTeacher(selectedTeacher.getUid(), selectedTeacher.getName());
            if (teacherSubjects != null && !teacherSubjects.isEmpty()) {
                for (Subject s : teacherSubjects) {
                    items.add(s.getName());
                }
            } else {
                items.add("No subjects assigned");
            }
        } else {
            for (Subject s : allSubjects) {
                if (!items.contains(s.getName())) {
                    items.add(s.getName());
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinnerSubjectFilter.setAdapter(adapter);
    }

    private void populateLectureSpinner() {
        if (!isAdded() || getContext() == null) return;
        List<String> items = new ArrayList<>();
        items.add("All Lectures");
        items.add("Lecture 1 (11:00 AM - 12:00 PM)");
        items.add("Lecture 2 (12:00 PM - 01:00 PM)");
        items.add("Lecture 3 (02:00 PM - 03:00 PM)");
        items.add("Lecture 4 (03:00 PM - 04:00 PM)");
        items.add("Lecture 5 (04:00 PM - 05:00 PM)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinnerLectureFilter.setAdapter(adapter);
    }

    private void setupListeners() {
        populateDivSpinner();
        populateLectureSpinner();

        // STEP 1: Change Dept ➔ Reset Div, Teacher, Subject, Lecture
        binding.spinnerDeptFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDept = parent.getItemAtPosition(position).toString();
                populateTeacherSpinner();
                populateSubjectSpinner();
                performQuery();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // STEP 2: Change Division ➔ Reset Teacher, Subject, Lecture
        binding.spinnerDivFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDiv = parent.getItemAtPosition(position).toString();
                performQuery();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // STEP 3: Change Teacher ➔ Reset Subject, Lecture (populate ONLY assigned subjects!)
        binding.spinnerTeacherFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String itemStr = parent.getItemAtPosition(position).toString();
                    String rawName = itemStr.contains(" (") ? itemStr.substring(0, itemStr.indexOf(" (")).trim() : itemStr.trim();
                    selectedTeacher = null;
                    for (Teacher t : allTeachers) {
                        if (t.getName().trim().equalsIgnoreCase(rawName)) {
                            selectedTeacher = t;
                            break;
                        }
                    }
                } else {
                    selectedTeacher = null;
                }
                selectedSubject = null;
                populateSubjectSpinner();
                performQuery();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // STEP 4: Change Subject ➔ Reset Lecture
        binding.spinnerSubjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                if (!name.startsWith("All") && !name.contains("No subjects")) {
                    selectedSubject = null;
                    for (Subject s : allSubjects) {
                        if (s.getName().trim().equalsIgnoreCase(name.trim())) {
                            selectedSubject = s;
                            break;
                        }
                    }
                } else {
                    selectedSubject = null;
                }
                performQuery();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // STEP 6: Change Lecture
        binding.spinnerLectureFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLecture = parent.getItemAtPosition(position).toString();
                performQuery();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String m = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
            String d = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
            selectedDate = year + "-" + m + "-" + d;
            binding.etDateFilter.setText(selectedDate);
            performQuery();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void performQuery() {
        if (!isAdded() || getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        
        String subId = selectedSubject != null ? selectedSubject.getSubjectId() : "";
        String deptId = !"All Departments".equals(selectedDept) ? selectedDept : "";
        String div = !"All Divisions".equals(selectedDiv) ? selectedDiv.replace("Div ", "") : "";

        List<Attendance> list = db.getAttendanceByDateAndSubject(selectedDate, subId, deptId, div, "1");
        
        // Filter by selected teacher or lecture if specified
        List<Attendance> filtered = new ArrayList<>();
        for (Attendance a : list) {
            boolean matchesTeacher = true;
            if (selectedTeacher != null) {
                matchesTeacher = (selectedTeacher.getUid() != null && selectedTeacher.getUid().equals(a.getTeacherId())) ||
                                 (selectedTeacher.getName() != null && selectedTeacher.getName().equalsIgnoreCase(a.getTeacherName()));
            }

            boolean matchesLecture = true;
            if (!"All Lectures".equals(selectedLecture)) {
                if (selectedLecture.contains("1")) matchesLecture = (a.getLectureNumber() == 1);
                else if (selectedLecture.contains("2")) matchesLecture = (a.getLectureNumber() == 2);
                else if (selectedLecture.contains("3")) matchesLecture = (a.getLectureNumber() == 3);
                else if (selectedLecture.contains("4")) matchesLecture = (a.getLectureNumber() == 4);
                else if (selectedLecture.contains("5")) matchesLecture = (a.getLectureNumber() == 5);
            }

            if (matchesTeacher && matchesLecture) {
                filtered.add(a);
            }
        }

        currentList = filtered;
        adapter.setAttendanceList(filtered);
        updateSummaryCard(filtered);
    }

    private void updateSummaryCard(List<Attendance> list) {
        int present = 0, absent = 0, leave = 0;
        if (list != null) {
            for (Attendance a : list) {
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(a.getStatus())) present++;
                else if (Constants.STATUS_ABSENT.equalsIgnoreCase(a.getStatus())) absent++;
                else if (Constants.STATUS_LEAVE.equalsIgnoreCase(a.getStatus())) leave++;
            }
        }
        int total = list != null ? list.size() : 0;
        int pct = total > 0 ? (present * 100) / total : 0;

        binding.tvPresentCount.setText(String.valueOf(present));
        binding.tvAbsentCount.setText(String.valueOf(absent));
        binding.tvTotalCount.setText(String.valueOf(total));
        binding.tvPercentage.setText(pct + "%");
    }

    private void generatePDF() {
        if (currentList == null || currentList.isEmpty()) {
            Toast.makeText(getContext(), "No attendance records found to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File pdfFile = ExportUtils.exportAttendanceToPDF(requireContext(), currentList, "Daily Attendance Report");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("PDF Report Exported")
                    .setMessage("PDF saved to Documents: " + pdfFile.getName())
                    .setPositiveButton("Share PDF", (dialog, which) -> ExportUtils.sharePDF(requireContext(), pdfFile))
                    .setNegativeButton("Close", null)
                    .show();
        } catch (Exception e) {
            DialogHelper.showErrorDialog(getContext(), "PDF Export Error", e.getLocalizedMessage());
        }
    }

    private void generateExcel() {
        if (currentList == null || currentList.isEmpty()) {
            Toast.makeText(getContext(), "No attendance records found to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File csvFile = ExportUtils.exportAttendanceToExcelCSV(requireContext(), currentList, "Daily Attendance Report");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Excel CSV Report Exported")
                    .setMessage("CSV file saved to Documents: " + csvFile.getName())
                    .setPositiveButton("Share Excel CSV", (dialog, which) -> ExportUtils.shareFile(requireContext(), csvFile, "text/csv", "Excel Attendance Report"))
                    .setNegativeButton("Close", null)
                    .show();
        } catch (Exception e) {
            DialogHelper.showErrorDialog(getContext(), "Excel Export Error", e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
