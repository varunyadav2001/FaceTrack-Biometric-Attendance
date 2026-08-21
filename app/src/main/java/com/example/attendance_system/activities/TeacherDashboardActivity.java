package com.example.attendance_system.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ActivityTeacherDashboardBinding;
import com.example.attendance_system.fragments.teacher.MarkAttendanceFragment;
import com.example.attendance_system.fragments.teacher.QRAttendanceGenFragment;
import com.example.attendance_system.fragments.teacher.TeacherHomeFragment;
import com.example.attendance_system.fragments.teacher.TeacherLeaveApprovalFragment;
import com.example.attendance_system.fragments.teacher.TeacherProfileFragment;

public class TeacherDashboardActivity extends AppCompatActivity {

    private ActivityTeacherDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, com.example.attendance_system.utils.Constants.ROLE_TEACHER)) {
            return;
        }

        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        loadFragment(new TeacherHomeFragment());

        binding.bottomNavTeacher.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_teacher_home) {
                loadFragment(new TeacherHomeFragment());
                return true;
            } else if (id == R.id.nav_teacher_attendance) {
                loadFragment(new MarkAttendanceFragment());
                return true;
            } else if (id == R.id.nav_teacher_qr) {
                loadFragment(new QRAttendanceGenFragment());
                return true;
            } else if (id == R.id.nav_teacher_leaves) {
                loadFragment(new TeacherLeaveApprovalFragment());
                return true;
            } else if (id == R.id.nav_teacher_profile) {
                loadFragment(new TeacherProfileFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, com.example.attendance_system.utils.Constants.ROLE_TEACHER);
    }

    public void onPunchInCompleted() {
        if (binding != null && binding.bottomNavTeacher != null) {
            binding.bottomNavTeacher.setVisibility(android.view.View.VISIBLE);
        }
        loadFragment(new TeacherHomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.teacherContainer, fragment)
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
