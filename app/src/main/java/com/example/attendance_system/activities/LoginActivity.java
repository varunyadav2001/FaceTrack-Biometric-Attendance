package com.example.attendance_system.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ActivityLoginBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.utils.ValidationUtils;
import com.example.attendance_system.viewmodel.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private String selectedRole = Constants.ROLE_ADMIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        applyEntranceAnimations();
        startSubtleBackgroundMotion();

        // Ensure input fields are completely empty by default
        binding.etEmail.setText("");
        binding.etPassword.setText("");

        binding.roleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRoleAdmin) {
                    selectedRole = Constants.ROLE_ADMIN;
                } else if (checkedId == R.id.btnRoleTeacher) {
                    selectedRole = Constants.ROLE_TEACHER;
                } else if (checkedId == R.id.btnRoleStudent) {
                    selectedRole = Constants.ROLE_STUDENT;
                }
            }
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.btnGoogleSignIn.setOnClickListener(v ->
                Toast.makeText(this, "Google Workspace Single Sign-On initialized", Toast.LENGTH_SHORT).show());

        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        binding.tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.tvContactAdmin.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Contact Support & Admin")
                        .setMessage("For account credentials, password resets, or system access, please contact your university IT administrator.")
                        .setPositiveButton("Close", null)
                        .show());

        observeViewModel();
    }

    private void applyEntranceAnimations() {
        try {
            Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
            binding.cardLogin.startAnimation(fadeInUp);
        } catch (Exception ignored) {}
    }

    private void attemptLogin() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Enter a valid email address");
            return;
        }

        authViewModel.login(email, password, selectedRole);
    }

    private void observeViewModel() {
        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                binding.btnLogin.setText("");
                binding.btnLogin.setEnabled(false);
                binding.progressLogin.setVisibility(View.VISIBLE);
            } else {
                binding.btnLogin.setText("Sign In");
                binding.btnLogin.setEnabled(true);
                binding.progressLogin.setVisibility(View.GONE);
            }
        });

        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                // Role Validation: Ensure selected role matches user profile role
                if (user.getRole() != null && !selectedRole.equalsIgnoreCase(user.getRole())) {
                    DialogHelper.showErrorDialog(this, "Role Mismatch", "Invalid role selected for this account.\n\nExpected: " + user.getRole());
                    return;
                }

                SharedPrefManager pref = SharedPrefManager.getInstance(this);
                pref.saveUser(user);
                pref.setRememberMe(binding.cbRememberMe.isChecked());

                Toast.makeText(this, "Welcome " + user.getName() + " (" + user.getRole() + ")", Toast.LENGTH_SHORT).show();

                com.example.attendance_system.helpers.AuthGuardHelper.redirectToCorrectDashboard(LoginActivity.this, user.getRole());
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                DialogHelper.showErrorDialog(this, "Authentication Failed", error);
            }
        });
    }

    private void startSubtleBackgroundMotion() {
        if (binding.vTopRightCircle != null) {
            android.animation.ObjectAnimator animTopRight = android.animation.ObjectAnimator.ofFloat(binding.vTopRightCircle, "translationY", 0f, 15f, 0f);
            animTopRight.setDuration(12000);
            animTopRight.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animTopRight.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animTopRight.start();
        }
        if (binding.vBottomLeftCircle != null) {
            android.animation.ObjectAnimator animBottomLeft = android.animation.ObjectAnimator.ofFloat(binding.vBottomLeftCircle, "translationY", 0f, -15f, 0f);
            animBottomLeft.setDuration(15000);
            animBottomLeft.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animBottomLeft.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animBottomLeft.start();
        }
    }
}
