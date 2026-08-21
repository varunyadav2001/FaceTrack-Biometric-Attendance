package com.example.attendance_system.fragments.student;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.activities.LoginActivity;
import com.example.attendance_system.databinding.FragmentStudentProfileBinding;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.QRCodeUtils;
import com.example.attendance_system.utils.SharedPrefManager;

public class StudentProfileFragment extends Fragment {

    private FragmentStudentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        if (user != null) {
            binding.tvStudentProfileName.setText(user.getName());
            Bitmap qrBitmap = QRCodeUtils.generateQRCode("STUDENT_ID|" + user.getUid() + "|MCA", 300, 300);
            if (qrBitmap != null) {
                binding.imgStudentQR.setImageBitmap(qrBitmap);
            }
        }

        boolean isDark = SharedPrefManager.getInstance(requireContext()).isDarkMode();
        binding.switchStudentDarkMode.setChecked(isDark);

        binding.switchStudentDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefManager.getInstance(requireContext()).setDarkMode(isChecked);
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.btnStudentLogout.setOnClickListener(v -> {
            com.example.attendance_system.helpers.AuthGuardHelper.logoutAndRedirectToLogin(requireActivity());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
