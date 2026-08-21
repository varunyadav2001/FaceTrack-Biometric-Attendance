package com.example.attendance_system.utils;

public class Constants {
    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    // Firestore Collections
    public static final String KEY_USERS = "users";
    public static final String KEY_STUDENTS = "students";
    public static final String KEY_TEACHERS = "teachers";
    public static final String KEY_DEPARTMENTS = "departments";
    public static final String KEY_SUBJECTS = "subjects";
    public static final String KEY_ATTENDANCE = "attendance";
    public static final String KEY_LEAVES = "leaves";
    public static final String KEY_NOTIFICATIONS = "notifications";
    public static final String KEY_TIMETABLE = "timetable";

    // Attendance Status
    public static final String STATUS_PRESENT = "PRESENT";
    public static final String STATUS_ABSENT = "ABSENT";
    public static final String STATUS_LEAVE = "LEAVE";
    public static final String STATUS_LATE = "LATE";

    // Leave Status
    public static final String LEAVE_PENDING = "PENDING";
    public static final String LEAVE_APPROVED = "APPROVED";
    public static final String LEAVE_REJECTED = "REJECTED";

    // SharedPreferences
    public static final String PREF_NAME = "SmartAttendancePref";
    public static final String PREF_KEY_UID = "user_uid";
    public static final String PREF_KEY_EMAIL = "user_email";
    public static final String PREF_KEY_NAME = "user_name";
    public static final String PREF_KEY_ROLE = "user_role";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_DARK_MODE = "dark_mode_enabled";
    public static final String PREF_KEY_REMEMBER_ME = "remember_me";

    // Date formats
    public static final String DATE_FORMAT_DB = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
}
