package com.example.attendance_system.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.R;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.ActivityStudentDashboardBinding;
import com.example.attendance_system.fragments.student.StudentAttendanceFragment;
import com.example.attendance_system.fragments.student.StudentHomeFragment;
import com.example.attendance_system.fragments.student.StudentProfileFragment;
import com.example.attendance_system.fragments.teacher.PunchInVerificationFragment;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.SharedPrefManager;

public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, Constants.ROLE_STUDENT)) {
            return;
        }

        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        binding.bottomNavStudent.setVisibility(View.VISIBLE);
        loadFragment(new StudentHomeFragment());

        binding.bottomNavStudent.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_student_home) {
                loadFragment(new StudentHomeFragment());
                return true;
            } else if (id == R.id.nav_student_history) {
                loadFragment(new StudentAttendanceFragment());
                return true;
            } else if (id == R.id.nav_student_analytics) {
                loadFragment(new StudentAttendanceFragment());
                return true;
            } else if (id == R.id.nav_student_profile) {
                loadFragment(new StudentProfileFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, Constants.ROLE_STUDENT);
    }

    public void onPunchInCompleted() {
        binding.bottomNavStudent.setVisibility(View.VISIBLE);
        loadFragment(new StudentHomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.studentContainer, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
