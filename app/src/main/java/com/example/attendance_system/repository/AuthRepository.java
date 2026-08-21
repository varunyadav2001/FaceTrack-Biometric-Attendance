package com.example.attendance_system.repository;

import android.content.Context;
import android.util.Log;

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface ResetPasswordCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(Context context, String email, String password, String selectedRole, AuthCallback callback) {
        if (email == null || password == null) {
            callback.onError("Email and password are required.");
            return;
        }

        String cleanEmail = email.trim();
        String cleanPassword = password.trim();

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            callback.onError("Email and password cannot be empty.");
            return;
        }

        FirebaseAuth auth = FirebaseService.getInstance().getAuth();
        if (auth == null) {
            callback.onError("Firebase Authentication is not available.");
            return;
        }

        // Clear any stale cached auth state before authenticating
        try {
            auth.signOut();
        } catch (Exception ignored) {}

        // 1. Authenticate with Firebase Auth
        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser != null) {
                        String uid = fbUser.getUid();
                        Log.d(TAG, "Firebase Auth succeeded for " + cleanEmail + " (UID: " + uid + ")");

                        // 2. Retrieve user data from Firestore using UID
                        FirebaseService.getInstance().getUserFromFirestore(uid, new FirebaseService.FirestoreCallback<User>() {
                            @Override
                            public void onSuccess(User firestoreUser) {
                                if (firestoreUser == null || firestoreUser.getRole() == null || firestoreUser.getRole().trim().isEmpty()) {
                                    auth.signOut();
                                    callback.onError("User role not configured.");
                                    return;
                                }

                                if (!firestoreUser.getRole().equalsIgnoreCase(selectedRole)) {
                                    auth.signOut();
                                    callback.onError("Access Denied: Invalid role selected for this account.");
                                    return;
                                }

                                SharedPrefManager.getInstance(context).saveUser(firestoreUser);
                                DatabaseHelper.getInstance(context).addUser(firestoreUser, "");
                                callback.onSuccess(firestoreUser);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "Firestore profile fetch by UID failed: " + error);
                                // Check if user document exists by email
                                FirebaseService.getInstance().loginUserFromFirestore(cleanEmail, cleanPassword, selectedRole, new FirebaseService.FirestoreCallback<User>() {
                                    @Override
                                    public void onSuccess(User userByEmail) {
                                        if (userByEmail == null || userByEmail.getRole() == null || userByEmail.getRole().trim().isEmpty()) {
                                            auth.signOut();
                                            callback.onError("User role not configured.");
                                            return;
                                        }
                                        if (!userByEmail.getRole().equalsIgnoreCase(selectedRole)) {
                                            auth.signOut();
                                            callback.onError("Access Denied: Invalid role selected for this account.");
                                            return;
                                        }
                                        SharedPrefManager.getInstance(context).saveUser(userByEmail);
                                        DatabaseHelper.getInstance(context).addUser(userByEmail, "");
                                        callback.onSuccess(userByEmail);
                                    }

                                    @Override
                                    public void onError(String secondError) {
                                        auth.signOut();
                                        callback.onError("User role not configured.");
                                    }
                                });
                            }
                        });
                    } else {
                        callback.onError("Authentication failed: User object is null.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase signInWithEmailAndPassword failed for " + cleanEmail, e);
                    callback.onError("Invalid email or password.");
                });
    }

    public void resetPassword(String email, ResetPasswordCallback callback) {
        FirebaseAuth auth = FirebaseService.getInstance().getAuth();
        if (auth != null && email != null && !email.trim().isEmpty()) {
            auth.sendPasswordResetEmail(email.trim())
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Password reset failed for " + email, e);
                        callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Password reset failed.");
                    });
        } else {
            callback.onError("Please enter a valid email address.");
        }
    }
}
