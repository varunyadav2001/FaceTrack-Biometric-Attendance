package com.example.attendance_system.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ActivityAdminDashboardBinding;
import com.example.attendance_system.fragments.admin.AdminHomeFragment;
import com.example.attendance_system.fragments.admin.AdminSettingsFragment;
import com.example.attendance_system.fragments.admin.AttendanceReportFragment;
import com.example.attendance_system.fragments.admin.ManageStudentsFragment;
import com.example.attendance_system.fragments.admin.ManageTeachersFragment;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, com.example.attendance_system.utils.Constants.ROLE_ADMIN)) {
            return;
        }

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        loadFragment(new AdminHomeFragment());

        binding.bottomNavAdmin.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_home) {
                loadFragment(new AdminHomeFragment());
                return true;
            } else if (id == R.id.nav_admin_students) {
                loadFragment(new ManageStudentsFragment());
                return true;
            } else if (id == R.id.nav_admin_teachers) {
                loadFragment(new ManageTeachersFragment());
                return true;
            } else if (id == R.id.nav_admin_reports) {
                loadFragment(new AttendanceReportFragment());
                return true;
            } else if (id == R.id.nav_admin_settings) {
                loadFragment(new AdminSettingsFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, com.example.attendance_system.utils.Constants.ROLE_ADMIN);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminContainer, fragment)
                .commit();
    }
}
