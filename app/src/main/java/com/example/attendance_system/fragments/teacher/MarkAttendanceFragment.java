package com.example.attendance_system.fragments.teacher;

import android.os.Bundle;
import android.util.Log;
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

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.AttendanceAdapter;
import com.example.attendance_system.databinding.FragmentMarkAttendanceBinding;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.TimetableItem;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.ExportUtils;
import com.example.attendance_system.viewmodel.AttendanceViewModel;
import com.example.attendance_system.viewmodel.TeacherViewModel;
import com.example.attendance_system.viewmodel.TimetableViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MarkAttendanceFragment extends Fragment {

    private static final String TAG = "MarkAttendanceFragment";
    private FragmentMarkAttendanceBinding binding;
    private AttendanceViewModel viewModel;
    private TeacherViewModel teacherViewModel;
    private TimetableViewModel timetableViewModel;
    private AttendanceAdapter adapter;
    private List<Attendance> attendanceList = new ArrayList<>();
    
    private Teacher currentTeacher;
    private List<TimetableItem> teacherTodayTimetable = new ArrayList<>();
    private TimetableItem selectedTimetableItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMarkAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedTimetableItem = (TimetableItem) getArguments().getSerializable("selected_timetable_item");
        }

        initViewModels();
        setupRecyclerView();
        setupClickListeners();
        
        // Lock department/division spinner to disable manual editing by teacher
        binding.spinnerDivision.setEnabled(false);

        observeData();
        fetchInitialData();
    }

    private void initViewModels() {
        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AttendanceViewModel.class);
        teacherViewModel = new ViewModelProvider(this, factory).get(TeacherViewModel.class);
        timetableViewModel = new ViewModelProvider(this, factory).get(TimetableViewModel.class);
    }

    private void setupRecyclerView() {
        binding.rvMarkAttendance.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttendanceAdapter((attendance, newStatus) -> {
            for (Attendance item : attendanceList) {
                if (item.getStudentUid() != null && item.getStudentUid().equals(attendance.getStudentUid())) {
                    item.setStatus(newStatus);
                    break;
                }
            }
        });
        binding.rvMarkAttendance.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnMarkAllPresent.setOnClickListener(v -> markAll(Constants.STATUS_PRESENT));
        binding.btnMarkAllAbsent.setOnClickListener(v -> markAll(Constants.STATUS_ABSENT));
        binding.btnSubmitAttendance.setOnClickListener(v -> submitAttendance());
        binding.btnAddStudentTeacherPortal.setOnClickListener(v -> showAddStudentDialog());

        binding.etSearchStudentAttendance.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void fetchInitialData() {
        String uid = com.example.attendance_system.utils.SharedPrefManager.getInstance(requireContext()).getUserUid();
        if (uid != null && !uid.isEmpty()) {
            teacherViewModel.fetchTeacherByUid(uid);
        }
    }

    private void observeData() {
        teacherViewModel.getTeacherLiveData().observe(getViewLifecycleOwner(), teacher -> {
            if (teacher != null) {
                currentTeacher = teacher;
                timetableViewModel.fetchTeacherTimetable(teacher.getUid(), DateTimeUtils.getCurrentDay());
            }
        });

        timetableViewModel.getTimetableLiveData().observe(getViewLifecycleOwner(), list -> {
            List<TimetableItem> filtered = new ArrayList<>();
            if (list != null) {
                for (TimetableItem item : list) {
                    if (!item.isBreak()) {
                        filtered.add(item);
                    }
                }
            }
            teacherTodayTimetable = filtered;
            populateTodayLectureSpinner(filtered);
        });

        viewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            binding.progressAttendance.setVisibility(View.GONE);
            String deptDiv = (selectedTimetableItem != null && selectedTimetableItem.getDepartmentName() != null ? selectedTimetableItem.getDepartmentName() : "MCA") + 
                             "-" + (selectedTimetableItem != null && selectedTimetableItem.getDivision() != null ? selectedTimetableItem.getDivision() : "A");

            if (list != null && !list.isEmpty()) {
                if (selectedTimetableItem != null) {
                    for (Attendance a : list) {
                        a.setTimetableId(selectedTimetableItem.getId());
                        a.setLectureNumber(selectedTimetableItem.getLectureNumber());
                        a.setStartTime(selectedTimetableItem.getStartTime());
                        a.setEndTime(selectedTimetableItem.getEndTime());
                        a.setLectureTime(selectedTimetableItem.getTimeSlot());
                        a.setSubjectName(selectedTimetableItem.getSubjectName());
                        a.setTeacherName(selectedTimetableItem.getTeacherName());
                    }
                }
                attendanceList = list;
                adapter.setAttendanceList(new ArrayList<>(list));
                binding.rvMarkAttendance.setVisibility(View.VISIBLE);
                binding.tvNoStudentsAttendance.setVisibility(View.GONE);

                binding.tvAttendanceStudentCount.setText(deptDiv + " • " + list.size() + " Students");

                Log.d(TAG, "================ ATTENDANCE FETCH DEBUG ==================");
                Log.d(TAG, "Logged-In Teacher UID: " + (currentTeacher != null ? currentTeacher.getUid() : "unknown"));
                Log.d(TAG, "Selected Lecture: " + (selectedTimetableItem != null ? selectedTimetableItem.getSubjectName() : "N/A"));
                Log.d(TAG, "Class & Division: " + deptDiv);
                Log.d(TAG, "Loaded Registered Students Count: " + list.size());
                for (int i = 0; i < Math.min(list.size(), 5); i++) {
                    Attendance a = list.get(i);
                    Log.d(TAG, "  Student #" + (i + 1) + ": Name=" + a.getStudentName() + ", RollNo=" + a.getRollNo() + ", Status=" + a.getStatus());
                }
                Log.d(TAG, "==========================================================");
            } else {
                attendanceList = new ArrayList<>();
                if (adapter != null) adapter.setAttendanceList(new ArrayList<>());
                binding.rvMarkAttendance.setVisibility(View.GONE);
                binding.tvNoStudentsAttendance.setVisibility(View.VISIBLE);
                binding.tvAttendanceStudentCount.setText(deptDiv + " • 0 Students");

                if (teacherTodayTimetable == null || teacherTodayTimetable.isEmpty()) {
                    binding.tvNoStudentsAttendance.setText("No lectures scheduled for today.");
                } else {
                    binding.tvNoStudentsAttendance.setText("No registered students found for " + deptDiv + ".");
                }

                Log.d(TAG, "================ ATTENDANCE FETCH DEBUG ==================");
                Log.d(TAG, "Logged-In Teacher UID: " + (currentTeacher != null ? currentTeacher.getUid() : "unknown"));
                Log.d(TAG, "Selected Lecture: " + (selectedTimetableItem != null ? selectedTimetableItem.getSubjectName() : "N/A"));
                Log.d(TAG, "Class & Division: " + deptDiv);
                Log.d(TAG, "Loaded Registered Students Count: 0");
                Log.d(TAG, "==========================================================");
            }
        });

        viewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                showPostSubmitShareDialog();
            }
        });
    }

    private void populateTodayLectureSpinner(List<TimetableItem> lectures) {
        if (!isAdded() || getContext() == null) return;

        if (lectures == null || lectures.isEmpty()) {
            binding.tvSelectedClassTitle.setText("No lectures scheduled for today");
            binding.tvSelectedClassDetails.setText("Check with Admin for subject assignments");
            binding.rvMarkAttendance.setVisibility(View.GONE);
            binding.progressAttendance.setVisibility(View.GONE);
            binding.tvNoStudentsAttendance.setVisibility(View.VISIBLE);
            binding.tvNoStudentsAttendance.setText("No lectures scheduled for today.");
            
            List<String> emptyList = new ArrayList<>();
            emptyList.add("No lectures scheduled for today");
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, emptyList);
            binding.spinnerTodayLectures.setAdapter(emptyAdapter);
            binding.spinnerTodayLectures.setEnabled(false);
            return;
        }

        binding.spinnerTodayLectures.setEnabled(true);
        ArrayAdapter<TimetableItem> lectureAdapter = new ArrayAdapter<TimetableItem>(requireContext(),
                R.layout.item_lecture_spinner_dropdown, lectures) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return createItemView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return createItemView(position, convertView, parent);
            }

            private View createItemView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = LayoutInflater.from(getContext()).inflate(R.layout.item_lecture_spinner_dropdown, parent, false);
                }
                TimetableItem item = getItem(position);
                if (item != null) {
                    android.widget.TextView tvTitle = v.findViewById(R.id.tvSpinnerLectureTitle);
                    android.widget.TextView tvDetails = v.findViewById(R.id.tvSpinnerLectureDetails);
                    
                    String dept = item.getDepartmentName() != null ? item.getDepartmentName() : "MCA";
                    String div = item.getDivision() != null ? item.getDivision() : "A";
                    String room = item.getRoomNumber() != null ? item.getRoomNumber() : "102";
                    
                    tvTitle.setText(item.getSubjectName() + " • " + dept + "-" + div);
                    tvDetails.setText((item.getStartTime() != null ? item.getStartTime() : "12:00 PM") +
                            " – " + (item.getEndTime() != null ? item.getEndTime() : "01:00 PM") + " • Room " + room);
                }
                return v;
            }
        };
        binding.spinnerTodayLectures.setAdapter(lectureAdapter);

        // Pre-select lecture if passed from Timetable click
        int preSelectIndex = 0;
        if (selectedTimetableItem != null && !lectures.isEmpty()) {
            for (int i = 0; i < lectures.size(); i++) {
                if (lectures.get(i).getId() != null && lectures.get(i).getId().equals(selectedTimetableItem.getId())) {
                    preSelectIndex = i;
                    break;
                }
            }
        }

        selectedTimetableItem = lectures.get(preSelectIndex);
        updateClassHeaderUI(selectedTimetableItem);
        binding.spinnerTodayLectures.setSelection(preSelectIndex);

        binding.spinnerTodayLectures.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (lectures.isEmpty() || position >= lectures.size()) return;

                selectedTimetableItem = lectures.get(position);
                updateClassHeaderUI(selectedTimetableItem);

                binding.progressAttendance.setVisibility(View.VISIBLE);
                binding.rvMarkAttendance.setVisibility(View.GONE);
                binding.tvNoStudentsAttendance.setVisibility(View.GONE);

                // Asynchronously sync cloud students first
                new com.example.attendance_system.repository.StudentRepository().getAllStudents(requireContext(), new com.example.attendance_system.repository.StudentRepository.StudentListCallback() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        if (isAdded()) {
                            viewModel.fetchAttendanceByTimetable(
                                    DateTimeUtils.getCurrentDateDb(),
                                    selectedTimetableItem.getId(),
                                    selectedTimetableItem.getSubjectId(),
                                    selectedTimetableItem.getDepartmentId(),
                                    selectedTimetableItem.getDivision(),
                                    selectedTimetableItem.getSemester()
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isAdded()) {
                            viewModel.fetchAttendanceByTimetable(
                                    DateTimeUtils.getCurrentDateDb(),
                                    selectedTimetableItem.getId(),
                                    selectedTimetableItem.getSubjectId(),
                                    selectedTimetableItem.getDepartmentId(),
                                    selectedTimetableItem.getDivision(),
                                    selectedTimetableItem.getSemester()
                            );
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateClassHeaderUI(TimetableItem item) {
        if (item == null || !isAdded()) return;
        String dept = item.getDepartmentName() != null ? item.getDepartmentName() : "MCA";
        String div = item.getDivision() != null ? item.getDivision() : "A";
        String room = item.getRoomNumber() != null ? item.getRoomNumber() : "102";
        binding.tvSelectedClassTitle.setText(item.getSubjectName() + " • " + dept + "-" + div);
        binding.tvSelectedClassDetails.setText("Time: " + item.getStartTime() + " – " + item.getEndTime() + " • Room " + room);
    }

    private void updateDivisionDisplay(String label) {
        if (!isAdded() || getContext() == null) return;
        List<String> list = new ArrayList<>();
        list.add(label);
        ArrayAdapter<String> divAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, list);
        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDivision.setAdapter(divAdapter);
        binding.spinnerDivision.setEnabled(false);
    }

    private void markAll(String status) {
        if (attendanceList != null && !attendanceList.isEmpty()) {
            for (Attendance item : attendanceList) {
                item.setStatus(status);
            }
            adapter.setAttendanceList(new ArrayList<>(attendanceList));
        }
    }

    private void submitAttendance() {
        if (adapter.getAttendanceList() != null && !adapter.getAttendanceList().isEmpty() && selectedTimetableItem != null) {
            String tUid = currentTeacher != null ? currentTeacher.getUid() : "unknown";
            String teacherName = currentTeacher != null ? currentTeacher.getName() : "Teacher";
            String date = DateTimeUtils.getCurrentDateDb();
            String day = DateTimeUtils.getCurrentDay();

            for (Attendance a : adapter.getAttendanceList()) {
                a.setDate(date);
                a.setDay(day);
                a.setMarkedByUid(tUid);
                a.setTeacherId(tUid);
                a.setTeacherName(teacherName);
                a.setTimetableId(selectedTimetableItem.getId());
                a.setSubjectId(selectedTimetableItem.getSubjectId());
                a.setSubjectName(selectedTimetableItem.getSubjectName());
                a.setDepartmentId(selectedTimetableItem.getDepartmentId());
                a.setDepartmentName(selectedTimetableItem.getDepartmentName());
                a.setSection(selectedTimetableItem.getDivision());
                a.setSemester(selectedTimetableItem.getSemester());
                a.setLectureNumber(selectedTimetableItem.getLectureNumber());
                a.setStartTime(selectedTimetableItem.getStartTime());
                a.setEndTime(selectedTimetableItem.getEndTime());
                a.setLectureTime(selectedTimetableItem.getTimeSlot());

                // Key format: Date + TimetableId + StudentId
                String attId = date + "_" + a.getStudentId() + "_" + selectedTimetableItem.getId();
                a.setAttendanceId(attId);
            }
            viewModel.saveAttendanceList(adapter.getAttendanceList());
        } else {
            Toast.makeText(getContext(), "Select a valid assigned lecture slot", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPostSubmitShareDialog() {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Success")
                .setMessage("Attendance saved successfully. Share report?")
                .setPositiveButton("Share", (dialog, which) -> {
                    try {
                        String title = selectedTimetableItem != null ? selectedTimetableItem.getSubjectName() : "Attendance";
                        File f = ExportUtils.exportAttendanceToPDF(requireContext(), adapter.getAttendanceList(), title);
                        ExportUtils.sharePDF(requireContext(), f);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showAddStudentDialog() {
        if (getContext() == null) return;

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_student, null);
        com.google.android.material.textfield.TextInputEditText etN = v.findViewById(R.id.etAddStudentName);
        com.google.android.material.textfield.TextInputEditText etE = v.findViewById(R.id.etAddStudentEmail);
        com.google.android.material.textfield.TextInputEditText etP = v.findViewById(R.id.etAddStudentPassword);
        com.google.android.material.textfield.TextInputEditText etC = v.findViewById(R.id.etAddStudentConfirmPassword);

        androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setCancelable(true)
                .create();

        v.findViewById(R.id.btnCancelAddStudent).setOnClickListener(v1 -> d.dismiss());
        v.findViewById(R.id.btnConfirmAddStudent).setOnClickListener(v1 -> {
            String n = etN.getText() != null ? etN.getText().toString().trim() : "";
            String e = etE.getText() != null ? etE.getText().toString().trim() : "";
            String p = etP.getText() != null ? etP.getText().toString().trim() : "";
            String c = etC.getText() != null ? etC.getText().toString().trim() : "";

            if (n.isEmpty() || e.isEmpty() || p.isEmpty() || !p.equals(c)) {
                Toast.makeText(getContext(), "Validation Error", Toast.LENGTH_SHORT).show();
                return;
            }

            long now = System.currentTimeMillis();
            String deptId = selectedTimetableItem != null ? selectedTimetableItem.getDepartmentId() : "d1";
            String deptName = selectedTimetableItem != null ? selectedTimetableItem.getDepartmentName() : "MCA";
            String section = selectedTimetableItem != null ? selectedTimetableItem.getDivision() : "A";
            String semester = selectedTimetableItem != null ? selectedTimetableItem.getSemester() : "1";

            Student s = new Student("st_" + now, "uid_st_" + now, n, e, "STD" + now % 1000, deptId, deptName, semester, section, "9876543210", "", "STD" + now % 1000);
            new com.example.attendance_system.repository.StudentRepository().addStudent(requireContext(), s, p, true, new com.example.attendance_system.repository.StudentRepository.StudentActionCallback() {
                @Override
                public void onSuccess() { 
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Student Added", Toast.LENGTH_SHORT).show();
                        if (selectedTimetableItem != null) {
                            viewModel.fetchAttendanceByTimetable(
                                    DateTimeUtils.getCurrentDateDb(),
                                    selectedTimetableItem.getId(),
                                    selectedTimetableItem.getSubjectId(),
                                    selectedTimetableItem.getDepartmentId(),
                                    selectedTimetableItem.getDivision(),
                                    selectedTimetableItem.getSemester()
                            );
                        }
                    });
                }
                @Override
                public void onError(String msg) { 
                    if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show());
                }
            });
            d.dismiss();
        });
        d.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
