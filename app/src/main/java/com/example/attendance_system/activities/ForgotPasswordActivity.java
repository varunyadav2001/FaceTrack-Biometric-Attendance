package com.example.attendance_system.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendance_system.databinding.ActivityForgotPasswordBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.utils.ValidationUtils;
import com.example.attendance_system.viewmodel.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel authViewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSendResetLink.setOnClickListener(v -> {
            String email = binding.etResetEmail.getText().toString().trim();
            if (!ValidationUtils.isValidEmail(email)) {
                binding.etResetEmail.setError("Please enter a valid email address");
                return;
            }
            authViewModel.resetPassword(email);
        });

        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                progressDialog = DialogHelper.showLoadingDialog(this, "Sending reset link...");
            } else if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });

        authViewModel.getPasswordResetLiveData().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                DialogHelper.showSuccessDialog(this, "Reset Link Sent",
                        "Check your email inbox for instructions to reset your password.");
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                DialogHelper.showErrorDialog(this, "Reset Error", error);
            }
        });
    }
}
