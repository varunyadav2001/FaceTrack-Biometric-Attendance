package com.example.attendance_system.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.TeacherAdapter;
import com.example.attendance_system.databinding.FragmentManageTeachersBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.viewmodel.TeacherViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ManageTeachersFragment extends Fragment {

    private FragmentManageTeachersBinding binding;
    private TeacherViewModel viewModel;
    private TeacherAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageTeachersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TeacherViewModel.class);
        binding.rvTeachers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeacherAdapter(new TeacherAdapter.OnTeacherClickListener() {
            @Override
            public void onDeleteClick(Teacher teacher) {
                DialogHelper.showConfirmDialog(getContext(), "Delete Faculty",
                        "Delete " + teacher.getName() + "?", "Delete",
                        () -> viewModel.deleteTeacher(teacher.getTeacherId()));
            }

            @Override
            public void onForcePunchOutClick(Teacher teacher) {
                forcePunchOutTeacher(teacher);
            }

            @Override
            public void onItemClick(Teacher teacher) {
            }
        });
        binding.rvTeachers.setAdapter(adapter);

        binding.fabAddTeacher.setOnClickListener(v -> showAddTeacherDialog());

        binding.etSearchTeacher.addTextChangedListener(new android.text.TextWatcher() {
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

        viewModel.getTeacherListLiveData().observe(getViewLifecycleOwner(), teachers -> {
            if (teachers != null) adapter.setTeacherList(teachers);
        });

        viewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                android.widget.Toast.makeText(getContext(), "Faculty saved successfully!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Error: " + error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.fetchTeachers();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.fetchTeachers();
        }
    }

    private void forcePunchOutTeacher(Teacher teacher) {
        if (teacher == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("FORCE PUNCH OUT")
                .setMessage("Are you sure you want to force Punch Out for " + teacher.getName() + "?\n\nThis will end their current session immediately.")
                .setPositiveButton("FORCE PUNCH OUT", (dialog, which) -> {
                    executeForcePunchOut(teacher);
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void executeForcePunchOut(Teacher teacher) {
        String date = com.example.attendance_system.utils.DateTimeUtils.getCurrentDateDb();
        String time = com.example.attendance_system.utils.DateTimeUtils.getCurrentTime();
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

        Attendance record = db.getTodayAttendanceForUser(date, teacher.getUid());
        if (record == null) {
            android.widget.Toast.makeText(getContext(), "No active session found for " + teacher.getName() + " today.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        if (record.getPunchOutTime() != null && !record.getPunchOutTime().trim().isEmpty()) {
            android.widget.Toast.makeText(getContext(), teacher.getName() + " is already punched out.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        long outTs = System.currentTimeMillis();
        record.setPunchOutTime(time);
        record.setPunchOutTimestamp(outTs);
        record.setPunchOutStatus("FORCE_COMPLETED");
        record.setSessionCompleted(true);
        record.setVerificationResult("FORCE_OUT_ADMIN");

        // Calculate duration
        long inTs = record.getPunchInTimestamp();
        long diffMinutes = 0;
        if (inTs > 0 && outTs > inTs) {
            diffMinutes = (outTs - inTs) / (60 * 1000);
        }
        long hours = diffMinutes / 60;
        long mins = diffMinutes % 60;
        record.setWorkingDuration(hours + "h " + String.format(java.util.Locale.US, "%02dm", mins));
        record.setStatus(diffMinutes >= 480 ? "FULL DAY" : "HALF DAY");

        db.insertOrUpdateAttendanceRecord(record);

        // Sync to Firestore
        com.example.attendance_system.firebase.FirebaseService.getInstance().getFirestore()
                .collection("attendance").document(record.getAttendanceId())
                .set(record)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) android.widget.Toast.makeText(getContext(), "Force Punch Out successful!", android.widget.Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) android.widget.Toast.makeText(getContext(), "DB updated, but Firestore sync failed.", android.widget.Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddTeacherDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_teacher, null);

        com.google.android.material.textfield.TextInputEditText etName = dialogView.findViewById(R.id.etAddTeacherName);
        com.google.android.material.textfield.TextInputEditText etEmail = dialogView.findViewById(R.id.etAddTeacherEmail);
        com.google.android.material.textfield.TextInputEditText etPassword = dialogView.findViewById(R.id.etAddTeacherPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etAddTeacherConfirmPassword);

        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAddTeacher);
        com.google.android.material.button.MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmAddTeacher);

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
                android.widget.Toast.makeText(getContext(), "Please fill in all required fields (*)", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (!com.example.attendance_system.utils.ValidationUtils.isValidEmail(email)) {
                android.widget.Toast.makeText(getContext(), "Please enter a valid email address", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                android.widget.Toast.makeText(getContext(), "Passwords do not match!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                android.widget.Toast.makeText(getContext(), "Password must be at least 6 characters", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            long now = System.currentTimeMillis();
            String tId = "t_" + now;
            String uid = "uid_t_" + now;
            Teacher t = new Teacher(tId, uid, name, email, "d1", "MCA", "Assistant Professor", "9988776655", "");

            viewModel.addTeacher(t, password);
            android.widget.Toast.makeText(getContext(), "Teacher registered successfully!", android.widget.Toast.LENGTH_SHORT).show();
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
