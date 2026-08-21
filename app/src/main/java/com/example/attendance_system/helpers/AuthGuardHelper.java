package com.example.attendance_system.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.attendance_system.activities.AdminDashboardActivity;
import com.example.attendance_system.activities.LoginActivity;
import com.example.attendance_system.activities.StudentDashboardActivity;
import com.example.attendance_system.activities.TeacherDashboardActivity;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthGuardHelper {

    private static final String TAG = "AuthGuardHelper";

    /**
     * Checks if user is authenticated and has the required role.
     * If unauthenticated, clears session and redirects to LoginActivity.
     * If user has a different valid role, redirects to their corresponding dashboard.
     * @return true if allowed to proceed, false if redirected.
     */
    public static boolean checkAndEnforce(Activity activity, String requiredRole) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return false;

        SharedPrefManager pref = SharedPrefManager.getInstance(activity);
        FirebaseAuth auth = FirebaseService.getInstance().getAuth();
        FirebaseUser fbUser = auth != null ? auth.getCurrentUser() : null;

        // 1. Unauthenticated check - Firebase Auth session is the strict source of truth
        if (fbUser == null) {
            Log.w(TAG, "Unauthenticated access attempt on " + activity.getClass().getSimpleName() + ". Redirecting to Login.");
            logoutAndRedirectToLogin(activity);
            return false;
        }

        // 2. Check Role Presence
        String userRole = pref.getUserRole();
        if (userRole == null || userRole.trim().isEmpty()) {
            Log.w(TAG, "Missing role for user on " + activity.getClass().getSimpleName() + ". Redirecting to Login.");
            logoutAndRedirectToLogin(activity);
            return false;
        }

        // 3. Role enforcement check
        if (requiredRole != null && !requiredRole.trim().isEmpty()) {
            if (!requiredRole.equalsIgnoreCase(userRole)) {
                Log.w(TAG, "Role mismatch on " + activity.getClass().getSimpleName() + ". Required: " + requiredRole + ", Actual: " + userRole + ". Redirecting.");
                redirectToCorrectDashboard(activity, userRole);
                return false;
            }
        }

        return true;
    }

    /**
     * Fully logs out the user from Firebase and clears local preferences.
     */
    public static void logout(Context context) {
        try {
            FirebaseAuth auth = FirebaseService.getInstance().getAuth();
            if (auth != null) {
                auth.signOut();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error signing out of Firebase Auth", e);
        }

        try {
            SharedPrefManager.getInstance(context).clearSession();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing local session", e);
        }
    }

    /**
     * Logs out user and brings them to LoginActivity with a clean task stack.
     */
    public static void logoutAndRedirectToLogin(Activity activity) {
        logout(activity);
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Redirects to the dashboard corresponding to user's assigned role.
     */
    public static void redirectToCorrectDashboard(Activity activity, String role) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        Intent intent;
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(activity, AdminDashboardActivity.class);
        } else if (Constants.ROLE_TEACHER.equalsIgnoreCase(role)) {
            intent = new Intent(activity, TeacherDashboardActivity.class);
        } else if (Constants.ROLE_STUDENT.equalsIgnoreCase(role)) {
            intent = new Intent(activity, StudentDashboardActivity.class);
        } else {
            Log.w(TAG, "Unrecognized or missing role: " + role + ". Directing to Login.");
            logoutAndRedirectToLogin(activity);
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
