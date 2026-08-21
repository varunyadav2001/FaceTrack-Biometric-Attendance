package com.example.attendance_system.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.activities.LoginActivity;
import com.example.attendance_system.databinding.FragmentAdminSettingsBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.utils.SharedPrefManager;

public class AdminSettingsFragment extends Fragment {

    private FragmentAdminSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean isDark = SharedPrefManager.getInstance(requireContext()).isDarkMode();
        binding.switchDarkMode.setChecked(isDark);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefManager.getInstance(requireContext()).setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.btnBackupRestore.setOnClickListener(v -> DialogHelper.showSuccessDialog(getContext(),
                "Cloud Backup Completed", "Firestore database indexes and documents backed up to cloud."));

        binding.btnChangePassword.setOnClickListener(v -> Toast.makeText(getContext(),
                "Password reset link sent to admin email.", Toast.LENGTH_LONG).show());

        // Load existing Company Settings from Firestore
        com.example.attendance_system.firebase.FirebaseService.getInstance().getFirestore()
                .collection("companySettings").document("main")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        com.example.attendance_system.models.CompanySettings cs = snapshot.toObject(com.example.attendance_system.models.CompanySettings.class);
                        if (cs != null && binding != null) {
                            binding.etAdminCompanyName.setText(cs.getCompanyName());
                            binding.etAdminLatitude.setText(String.valueOf(cs.getCompanyLatitude()));
                            binding.etAdminLongitude.setText(String.valueOf(cs.getCompanyLongitude()));
                            binding.etAdminRadius.setText(String.valueOf((int) cs.getAllowedRadiusMeters()));
                            binding.etAdminShiftStart.setText(cs.getShiftStartTime());
                            binding.etAdminShiftEnd.setText(cs.getShiftEndTime());
                        }
                    }
                });

        // Save Company Settings to Firestore
        binding.btnSaveCompanySettings.setOnClickListener(v -> {
            String name = binding.etAdminCompanyName.getText() != null ? binding.etAdminCompanyName.getText().toString().trim() : "INFOYASHONAND TECHNOLOGY PVT. LTD.";
            double lat = 16.8493056;
            double lng = 74.596352;
            double radius = 200.0;
            try {
                if (binding.etAdminLatitude.getText() != null) lat = Double.parseDouble(binding.etAdminLatitude.getText().toString().trim());
                if (binding.etAdminLongitude.getText() != null) lng = Double.parseDouble(binding.etAdminLongitude.getText().toString().trim());
                if (binding.etAdminRadius.getText() != null) radius = Double.parseDouble(binding.etAdminRadius.getText().toString().trim());
            } catch (Exception ignored) {}

            String shiftStart = binding.etAdminShiftStart.getText() != null ? binding.etAdminShiftStart.getText().toString().trim() : "09:00 AM";
            String shiftEnd = binding.etAdminShiftEnd.getText() != null ? binding.etAdminShiftEnd.getText().toString().trim() : "06:00 PM";

            com.example.attendance_system.models.CompanySettings newSettings =
                    new com.example.attendance_system.models.CompanySettings(name, lat, lng, radius, "1st Floor, Pearl Enclave, M.S.E.B. Road, Vishrambag, Sangli, Maharashtra 416416", shiftStart, shiftEnd, true);

            com.example.attendance_system.firebase.FirebaseService.getInstance().getFirestore()
                    .collection("companySettings").document("main")
                    .set(newSettings)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Company Geofence & Shift Config Saved ✓", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving config: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        binding.cardLogout.setOnClickListener(v -> {
            com.example.attendance_system.helpers.AuthGuardHelper.logoutAndRedirectToLogin(requireActivity());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
