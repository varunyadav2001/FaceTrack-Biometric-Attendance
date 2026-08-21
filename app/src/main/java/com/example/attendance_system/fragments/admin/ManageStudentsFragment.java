package com.example.attendance_system.fragments.admin;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.StudentAdapter;
import com.example.attendance_system.databinding.FragmentManageStudentsBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.QRCodeUtils;
import com.example.attendance_system.viewmodel.AttendanceViewModel;
import com.example.attendance_system.viewmodel.StudentViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageStudentsFragment extends Fragment {

    private FragmentManageStudentsBinding binding;
    private StudentViewModel viewModel;
    private AttendanceViewModel attendanceViewModel;
    private StudentAdapter adapter;
    private List<Student> allStudents = new ArrayList<>();
    private List<Attendance> todayAttendance = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageStudentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        binding.rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudentAdapter(new StudentAdapter.OnStudentClickListener() {
            @Override
            public void onQRClick(Student student) {
                showStudentQRDialog(student);
            }

            @Override
            public void onDeleteClick(Student student) {
                DialogHelper.showConfirmDialog(getContext(), "Delete Student",
                        "Are you sure you want to delete " + student.getName() + "?", "Delete",
                        () -> viewModel.deleteStudent(student.getStudentId()));
            }

            @Override
            public void onItemClick(Student student) {
                Toast.makeText(getContext(), "Student: " + student.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvStudents.setAdapter(adapter);

        binding.fabAddStudent.setOnClickListener(v -> showAddStudentDialog());

        binding.etSearchStudent.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.chipGroupStudentFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyFilter();
        });

        viewModel.getStudentListLiveData().observe(getViewLifecycleOwner(), students -> {
            if (students != null) {
                allStudents = students;
                applyFilter();
            }
        });

        attendanceViewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                todayAttendance = list;
                applyFilter();
            }
        });

        viewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Student saved successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        fetchData();
    }

    private void fetchData() {
        viewModel.fetchStudents();
        // For Admin, fetch all attendance for today by passing empty teacherUid and deptName
        attendanceViewModel.fetchAttendanceForTeacher(DateTimeUtils.getCurrentDateDb(), "", ""); 
    }

    private void applyFilter() {
        int checkedId = binding.chipGroupStudentFilter.getCheckedChipId();
        List<Student> filtered = new ArrayList<>();

        if (checkedId == R.id.chipPresent) {
            Set<String> presentUids = new HashSet<>();
            for (Attendance a : todayAttendance) {
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(a.getStatus())) {
                    presentUids.add(a.getStudentUid());
                    presentUids.add(a.getStudentId());
                }
            }
            for (Student s : allStudents) {
                if (presentUids.contains(s.getUid()) || presentUids.contains(s.getStudentId())) {
                    filtered.add(s);
                }
            }
        } else if (checkedId == R.id.chipAbsent) {
            Set<String> presentUids = new HashSet<>();
            for (Attendance a : todayAttendance) {
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(a.getStatus()) || Constants.STATUS_LEAVE.equalsIgnoreCase(a.getStatus())) {
                    presentUids.add(a.getStudentUid());
                    presentUids.add(a.getStudentId());
                }
            }
            for (Student s : allStudents) {
                if (!presentUids.contains(s.getUid()) && !presentUids.contains(s.getStudentId())) {
                    filtered.add(s);
                }
            }
        } else if (checkedId == R.id.chipLeave) {
            Set<String> leaveUids = new HashSet<>();
            for (Attendance a : todayAttendance) {
                if (Constants.STATUS_LEAVE.equalsIgnoreCase(a.getStatus())) {
                    leaveUids.add(a.getStudentUid());
                    leaveUids.add(a.getStudentId());
                }
            }
            for (Student s : allStudents) {
                if (leaveUids.contains(s.getUid()) || leaveUids.contains(s.getStudentId())) {
                    filtered.add(s);
                }
            }
        } else if (checkedId == R.id.chipDept) {
            for (Student s : allStudents) {
                if ("MCA".equalsIgnoreCase(s.getDepartmentName())) filtered.add(s);
            }
        } else {
            filtered = new ArrayList<>(allStudents);
        }

        adapter.setStudentList(filtered);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
    }

    private void showStudentQRDialog(Student student) {
        Bitmap qrBitmap = QRCodeUtils.generateQRCode("STUDENT_ID|" + student.getRollNo() + "|" + student.getDepartmentName(), 400, 400);
        ImageView imgView = new ImageView(getContext());
        imgView.setPadding(30, 30, 30, 30);
        if (qrBitmap != null) imgView.setImageBitmap(qrBitmap);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Digital ID - " + student.getName())
                .setMessage("Roll No: " + student.getRollNo())
                .setView(imgView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_student, null);

        com.google.android.material.textfield.TextInputEditText etName = dialogView.findViewById(R.id.etAddStudentName);
        com.google.android.material.textfield.TextInputEditText etEmail = dialogView.findViewById(R.id.etAddStudentEmail);
        com.google.android.material.textfield.TextInputEditText etPassword = dialogView.findViewById(R.id.etAddStudentPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etAddStudentConfirmPassword);

        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAddStudent);
        com.google.android.material.button.MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmAddStudent);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword != null && etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!com.example.attendance_system.utils.ValidationUtils.isValidEmail(email)) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDept = "MCA";
            String selectedSem = "1";
            String selectedSection = "A";

            long now = System.currentTimeMillis();
            String roll = selectedDept + String.valueOf(now).substring(String.valueOf(now).length() - 4);
            String stId = "st_" + now;
            // Generate a placeholder UID that will be updated by repository/firebase
            String placeholderUid = "uid_" + stId;
            
            String deptId = "d1"; // MCA

            Student s = new Student(stId, placeholderUid, name, email, roll, deptId, selectedDept, selectedSem, selectedSection, "9876543210", "", roll);

            viewModel.addStudent(s, password);
            Toast.makeText(getContext(), "Student registered successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
