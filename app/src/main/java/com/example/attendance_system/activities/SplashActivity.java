package com.example.attendance_system.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance_system.databinding.ActivitySplashBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.helpers.AuthGuardHelper;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private ActivitySplashBinding binding;
    private FirebaseAuth.AuthStateListener authStateListener;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (binding.tvSplashStatus != null) {
            binding.tvSplashStatus.setText("Checking authentication...");
        }

        // Initialize Firebase Authentication listener
        setupAuthListener();
    }

    private void setupAuthListener() {
        FirebaseAuth auth = FirebaseService.getInstance().getAuth();
        if (auth == null) {
            navigateToLogin();
            return;
        }

        // Small delay to prevent visual jump and allow Splash branding to render smoothly
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (isNavigating || isFinishing() || isDestroyed()) return;

                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser == null) {
                        Log.d(TAG, "onAuthStateChanged: No active session. Directing to Login Page.");
                        isNavigating = true;
                        SharedPrefManager.getInstance(SplashActivity.this).clearSession();
                        navigateToLogin();
                    } else {
                        Log.d(TAG, "onAuthStateChanged: Active Firebase user found (UID: " + currentUser.getUid() + "). Verifying role...");
                        isNavigating = true;
                        verifyRoleAndRoute(currentUser.getUid());
                    }
                }
            };

            auth.addAuthStateListener(authStateListener);
        }, 1000);
    }

    private void verifyRoleAndRoute(String uid) {
        FirebaseFirestore db = FirebaseService.getInstance().getFirestore();
        if (db == null) {
            Log.e(TAG, "Firestore is unavailable. Directing to Login.");
            AuthGuardHelper.logoutAndRedirectToLogin(this);
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null && user.getRole() != null && !user.getRole().trim().isEmpty()) {
                            if (user.getUid() == null || user.getUid().isEmpty()) user.setUid(uid);
                            SharedPrefManager.getInstance(this).saveUser(user);
                            navigateToDashboard(user.getRole());
                            return;
                        }
                    }

                    Log.w(TAG, "User document missing or invalid role for UID " + uid + ". Logging out.");
                    AuthGuardHelper.logoutAndRedirectToLogin(SplashActivity.this);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to verify role in Firestore: " + e.getMessage() + ". Logging out.");
                    AuthGuardHelper.logoutAndRedirectToLogin(SplashActivity.this);
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard(String role) {
        AuthGuardHelper.redirectToCorrectDashboard(this, role);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            FirebaseAuth auth = FirebaseService.getInstance().getAuth();
            if (auth != null) {
                auth.removeAuthStateListener(authStateListener);
            }
        }
    }
}

