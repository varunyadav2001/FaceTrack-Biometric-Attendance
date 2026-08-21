package com.example.attendance_system.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.attendance_system.models.User;

public class SharedPrefManager {
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_KEY_UID, user.getUid());
        editor.putString(Constants.PREF_KEY_EMAIL, user.getEmail());
        editor.putString(Constants.PREF_KEY_NAME, user.getName());
        editor.putString(Constants.PREF_KEY_ROLE, user.getRole());
        editor.putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, true);
        editor.putBoolean("face_registered", user.isFaceRegistered());

        if (user.getFaceEmbedding() != null && !user.getFaceEmbedding().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < user.getFaceEmbedding().size(); i++) {
                sb.append(user.getFaceEmbedding().get(i));
                if (i < user.getFaceEmbedding().size() - 1) sb.append(",");
            }
            editor.putString("face_embedding", sb.toString());
        } else {
            editor.remove("face_embedding");
        }
        editor.apply();
    }

    public User getUser() {
        if (!isLoggedIn()) return null;
        User user = new User();
        user.setUid(sharedPreferences.getString(Constants.PREF_KEY_UID, ""));
        user.setEmail(sharedPreferences.getString(Constants.PREF_KEY_EMAIL, ""));
        user.setName(sharedPreferences.getString(Constants.PREF_KEY_NAME, ""));
        user.setRole(sharedPreferences.getString(Constants.PREF_KEY_ROLE, ""));
        user.setFaceRegistered(sharedPreferences.getBoolean("face_registered", false));

        String embeddingStr = sharedPreferences.getString("face_embedding", null);
        if (embeddingStr != null && !embeddingStr.isEmpty()) {
            String[] parts = embeddingStr.split(",");
            java.util.List<Float> list = new java.util.ArrayList<>();
            for (String part : parts) {
                try {
                    list.add(Float.parseFloat(part.trim()));
                } catch (Exception ignored) {}
            }
            user.setFaceEmbedding(list);
        }
        return user;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_KEY_IS_LOGGED_IN, false);
    }

    public String getUserRole() {
        return sharedPreferences.getString(Constants.PREF_KEY_ROLE, "");
    }

    public String getUserUid() {
        return sharedPreferences.getString(Constants.PREF_KEY_UID, "");
    }

    public void setDarkMode(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(Constants.PREF_KEY_DARK_MODE, isEnabled).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(Constants.PREF_KEY_DARK_MODE, false);
    }

    public void setRememberMe(boolean remember) {
        sharedPreferences.edit().putBoolean(Constants.PREF_KEY_REMEMBER_ME, remember).apply();
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(Constants.PREF_KEY_REMEMBER_ME, true);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PREF_KEY_UID);
        editor.remove(Constants.PREF_KEY_EMAIL);
        editor.remove(Constants.PREF_KEY_NAME);
        editor.remove(Constants.PREF_KEY_ROLE);
        editor.remove("face_registered");
        editor.remove("face_embedding");
        editor.putBoolean(Constants.PREF_KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}
