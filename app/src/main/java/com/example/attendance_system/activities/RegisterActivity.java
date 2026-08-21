package com.example.attendance_system.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance_system.R;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.ActivityRegisterBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.User;
import com.example.attendance_system.repository.StudentRepository;
import com.example.attendance_system.repository.TeacherRepository;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private String selectedRole = Constants.ROLE_STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerRoleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRegStudent) {
                    selectedRole = Constants.ROLE_STUDENT;
                } else if (checkedId == R.id.btnRegTeacher) {
                    selectedRole = Constants.ROLE_TEACHER;
                }
            }
        });

        binding.btnRegisterSubmit.setOnClickListener(v -> performRegistration());

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        binding.tilRegName.setError(null);
        binding.tilRegEmail.setError(null);
        binding.tilRegPassword.setError(null);
        binding.tilRegConfirmPassword.setError(null);

        String name = binding.etRegName.getText().toString().trim();
        String email = binding.etRegEmail.getText().toString().trim();
        String password = binding.etRegPassword.getText().toString().trim();
        String confirmPassword = binding.etRegConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilRegName.setError("Full Name is required");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilRegEmail.setError("Enter a valid email address");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilRegPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilRegConfirmPassword.setError("Passwords do not match");
            return;
        }

        binding.btnRegisterSubmit.setText("");
        binding.btnRegisterSubmit.setEnabled(false);
        binding.progressRegister.setVisibility(View.VISIBLE);

        // 1. Create Firebase Authentication Account & Firestore User document
        FirebaseService.getInstance().createFirebaseUserAccount(email, password, name, selectedRole, new FirebaseService.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String createdUid) {
                User user = new User(createdUid, email, name, selectedRole);
                finalizeRegistration(user, password);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegisterSubmit.setText("REGISTER & SIGN IN");
                    binding.btnRegisterSubmit.setEnabled(true);
                    DialogHelper.showErrorDialog(RegisterActivity.this, "Registration Failed", error);
                });
            }
        });
    }

    private void finalizeRegistration(User user, String password) {
        // Save to local database for potential offline use later
        DatabaseHelper.getInstance(this).addUser(user, password);

        // Default value for self-registration
        String defaultOption = "MCA";

        if (Constants.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            long now = System.currentTimeMillis();
            String roll = defaultOption + String.valueOf(now).substring(String.valueOf(now).length() - 4);
            String stId = "st_" + now;
            
            // Map Dept Name to ID
            String deptId = "d1"; // Default to MCA

            // Use the REAL user UID from Firebase
            Student student = new Student(stId, user.getUid(), user.getName(), user.getEmail(), roll, deptId, defaultOption, "1", "A", "9876543210", "", roll);
            new StudentRepository().addStudent(this, student, false, null);
        } else if (Constants.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            String tId = "t_" + System.currentTimeMillis();
            Teacher teacher = new Teacher(tId, user.getUid(), user.getName(), user.getEmail(), "d1", "MCA", "Assistant Professor", "9988776655", "");
            new TeacherRepository().addTeacher(this, teacher, false, null);
        }

        runOnUiThread(() -> {
            binding.progressRegister.setVisibility(View.GONE);
            binding.btnRegisterSubmit.setText("REGISTER");
            binding.btnRegisterSubmit.setEnabled(true);

            // Cleanly sign out registration session so Login screen starts with fresh auth state
            try {
                FirebaseService.getInstance().getAuth().signOut();
            } catch (Exception ignored) {}

            Toast.makeText(this, "Registration successful! Please login with your credentials.", Toast.LENGTH_LONG).show();

            // Navigate to Login screen as per requirements
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
