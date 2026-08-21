package com.example.attendance_system.fragments.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.activities.LoginActivity;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentTeacherProfileBinding;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.SharedPrefManager;

import java.util.List;

public class TeacherProfileFragment extends Fragment {

    private FragmentTeacherProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        Teacher teacher = null;
        if (user != null) {
            binding.tvProfileName.setText(user.getName());
            binding.tvProfileEmail.setText("Email: " + user.getEmail());

            teacher = DatabaseHelper.getInstance(requireContext()).getTeacherByEmail(user.getEmail());
            if (teacher != null) {
                if (teacher.getPhone() != null && !teacher.getPhone().isEmpty()) {
                    binding.tvProfilePhone.setText("Phone: " + teacher.getPhone());
                } else {
                    binding.tvProfilePhone.setText("Phone: +91 9988776655");
                }
                if (teacher.getDepartmentName() != null && !teacher.getDepartmentName().isEmpty()) {
                    binding.tvProfileDepartment.setText("Department: " + teacher.getDepartmentName());
                    binding.tvProfileRole.setText("Faculty • " + teacher.getDepartmentName() + " Department");
                }
            } else {
                binding.tvProfilePhone.setText("Phone: +91 9988776655");
                binding.tvProfileDepartment.setText("Department: MCA");
            }
        }

        StringBuilder sbSubjects = new StringBuilder();
        
        // Fetch actual assignments from Subject collection instead of relying on hardcoded lists
        List<com.example.attendance_system.models.Subject> allSubjs = DatabaseHelper.getInstance(requireContext()).getAllSubjects();
        for (com.example.attendance_system.models.Subject s : allSubjs) {
            if (user != null && s.getTeacherId() != null && (s.getTeacherId().equals(user.getUid()) || s.getTeacherName().equalsIgnoreCase(user.getName()))) {
                if (sbSubjects.length() > 0) sbSubjects.append("\n");
                sbSubjects.append("• ").append(s.getCode()).append(": ").append(s.getName());
            }
        }

        if (sbSubjects.length() == 0 && teacher != null && teacher.getAssignedSubjects() != null && !teacher.getAssignedSubjects().isEmpty()) {
            for (String sub : teacher.getAssignedSubjects()) {
                if (sbSubjects.length() > 0) sbSubjects.append("\n");
                sbSubjects.append("• ").append(sub);
            }
        }

        if (sbSubjects.length() == 0) {
            sbSubjects.append("No subjects assigned");
        }

        binding.tvTeacherSubjects.setText(sbSubjects.toString());

        boolean isDark = SharedPrefManager.getInstance(requireContext()).isDarkMode();
        binding.switchTeacherDarkMode.setChecked(isDark);

        binding.switchTeacherDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefManager.getInstance(requireContext()).setDarkMode(isChecked);
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.btnTeacherLogout.setOnClickListener(v -> {
            com.example.attendance_system.helpers.AuthGuardHelper.logoutAndRedirectToLogin(requireActivity());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
