package com.example.attendance_system;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class AttendanceApplication extends Application {
    private static final String TAG = "AttendanceApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply theme preference globally before any activities are created
        try {
            boolean isDark = com.example.attendance_system.utils.SharedPrefManager.getInstance(this).isDarkMode();
            if (isDark) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        } catch (Exception ignored) {}

        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "FirebaseApp initialized successfully");

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                auth.signInAnonymously().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth anonymous session established");
                    } else {
                        Log.w(TAG, "FirebaseAuth anonymous login failed: " + (task.getException() != null ? task.getException().getMessage() : "unknown"));
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception initializing FirebaseApp: " + e.getMessage());
        }
    }
}
