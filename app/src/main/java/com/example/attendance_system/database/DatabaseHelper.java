package com.example.attendance_system.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Department;
import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.models.Notice;
import com.example.attendance_system.models.NotificationItem;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.Subject;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.TimetableItem;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartAttendanceOffline.db";
    private static final int DATABASE_VERSION = 18;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        instance.deleteOldDummyData();
        return instance;
    }

    private void deleteOldDummyData() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM students WHERE studentId IN ('1','2','3','4','5') OR email IN ('rahul@gmail.com','priya@gmail.com','amit@gmail.com','neha@gmail.com','vikas@gmail.com') OR name LIKE '%Rahul%' OR name LIKE '%Priya%' OR name LIKE '%Amit%' OR name LIKE '%Neha%' OR name LIKE '%Vikas%'");
            db.execSQL("DELETE FROM teachers WHERE teacherId = 't1' OR email IN ('gupta@univ.edu','teacher@attendance.com') OR name LIKE '%Gupta%'");
            db.execSQL("DELETE FROM users WHERE uid IN ('uid_s1','uid_s2','uid_s3','uid_s4','uid_s5','uid_t1') OR email IN ('rahul@gmail.com','priya@gmail.com','amit@gmail.com','neha@gmail.com','vikas@gmail.com','gupta@univ.edu','teacher@attendance.com','student@attendance.com')");
        } catch (Exception ignored) {}
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureAttendanceColumnsExist(db);
        ensureLeaveColumnsExist(db);
    }

    private void ensureAttendanceColumnsExist(SQLiteDatabase db) {
        String[] columns = new String[]{
                "punchInTime TEXT",
                "punchOutTime TEXT",
                "punchInTimestamp LONG",
                "punchOutTimestamp LONG",
                "workingDuration TEXT",
                "punchInStatus TEXT",
                "punchOutStatus TEXT",
                "sessionCompleted INTEGER",
                "attendanceStatus TEXT",
                "punchInLatitude REAL",
                "punchInLongitude REAL",
                "punchInDistanceFromOffice REAL",
                "punchInAccuracy REAL",
                "punchInGeofenceVerified INTEGER",
                "punchOutLatitude REAL",
                "punchOutLongitude REAL",
                "punchOutAccuracy REAL",
                "punchOutDistanceFromOffice REAL",
                "punchOutGeofenceVerified INTEGER",
                "geofenceVerified INTEGER",
                "faceVerified INTEGER",
                "livenessVerified INTEGER",
                "identityVerified INTEGER",
                "faceMatchScore REAL",
                "punchOutFaceVerified INTEGER",
                "punchOutLivenessVerified INTEGER",
                "punchOutIdentityVerified INTEGER",
                "punchOutFaceMatchScore REAL",
                "verificationTimestamp LONG",
                "verificationResult TEXT",
                "verificationImage TEXT",
                "distanceFromOfficeMeters REAL",
                "locationAccuracyMeters REAL"
        };

        for (String colDef : columns) {
            try {
                db.execSQL("ALTER TABLE attendance ADD COLUMN " + colDef);
            } catch (Exception ignored) {}
        }
    }

    private void ensureLeaveColumnsExist(SQLiteDatabase db) {
        String[] columns = new String[]{
                "startTime TEXT",
                "endTime TEXT",
                "studentUid TEXT",
                "rollNo TEXT",
                "departmentName TEXT",
                "semester TEXT",
                "section TEXT",
                "teacherUid TEXT",
                "teacherName TEXT",
                "submittedAt LONG"
        };

        for (String colDef : columns) {
            try {
                db.execSQL("ALTER TABLE leaves ADD COLUMN " + colDef);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table Users
        db.execSQL("CREATE TABLE users (" +
                "uid TEXT PRIMARY KEY, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "name TEXT, " +
                "role TEXT, " +
                "phone TEXT, " +
                "profileImageUrl TEXT, " +
                "createdAt LONG, " +
                "active INTEGER)");

        // Table Students
        db.execSQL("CREATE TABLE students (" +
                "studentId TEXT PRIMARY KEY, " +
                "uid TEXT, " +
                "name TEXT, " +
                "email TEXT, " +
                "rollNo TEXT, " +
                "departmentId TEXT, " +
                "departmentName TEXT, " +
                "semester TEXT, " +
                "section TEXT, " +
                "phone TEXT, " +
                "profileImageUrl TEXT, " +
                "qrCodeData TEXT, " +
                "createdAt LONG)");

        // Table Teachers
        db.execSQL("CREATE TABLE teachers (" +
                "teacherId TEXT PRIMARY KEY, " +
                "uid TEXT, " +
                "name TEXT, " +
                "email TEXT, " +
                "departmentId TEXT, " +
                "departmentName TEXT, " +
                "designation TEXT, " +
                "phone TEXT, " +
                "profileImageUrl TEXT, " +
                "createdAt LONG)");

        // Table Departments
        db.execSQL("CREATE TABLE departments (" +
                "departmentId TEXT PRIMARY KEY, " +
                "code TEXT, " +
                "name TEXT, " +
                "description TEXT, " +
                "createdAt LONG)");

        // Table Subjects
        db.execSQL("CREATE TABLE subjects (" +
                "subjectId TEXT PRIMARY KEY, " +
                "code TEXT, " +
                "name TEXT, " +
                "departmentId TEXT, " +
                "departmentName TEXT, " +
                "semester TEXT, " +
                "credits INTEGER, " +
                "teacherId TEXT, " +
                "teacherName TEXT)");

        // Table Attendance
        db.execSQL("CREATE TABLE attendance (" +
                "attendanceId TEXT PRIMARY KEY, " +
                "date TEXT, " +
                "day TEXT, " +
                "studentId TEXT, " +
                "studentUid TEXT, " +
                "studentName TEXT, " +
                "rollNo TEXT, " +
                "departmentId TEXT, " +
                "departmentName TEXT, " +
                "section TEXT, " +
                "semester TEXT, " +
                "subjectId TEXT, " +
                "subjectName TEXT, " +
                "teacherId TEXT, " +
                "teacherName TEXT, " +
                "status TEXT, " +
                "markedByUid TEXT, " +
                "timetableId TEXT, " +
                "lectureNumber INTEGER, " +
                "startTime TEXT, " +
                "endTime TEXT, " +
                "lectureTime TEXT, " +
                "punchInTime TEXT, " +
                "punchOutTime TEXT, " +
                "punchInTimestamp LONG, " +
                "punchOutTimestamp LONG, " +
                "workingDuration TEXT, " +
                "punchInStatus TEXT, " +
                "punchOutStatus TEXT, " +
                "sessionCompleted INTEGER, " +
                "attendanceStatus TEXT, " +
                "punchInLatitude REAL, " +
                "punchInLongitude REAL, " +
                "punchInDistanceFromOffice REAL, " +
                "punchInAccuracy REAL, " +
                "punchInGeofenceVerified INTEGER, " +
                "punchOutLatitude REAL, " +
                "punchOutLongitude REAL, " +
                "punchOutAccuracy REAL, " +
                "punchOutDistanceFromOffice REAL, " +
                "punchOutGeofenceVerified INTEGER, " +
                "geofenceVerified INTEGER, " +
                "faceVerified INTEGER, " +
                "livenessVerified INTEGER, " +
                "identityVerified INTEGER, " +
                "faceMatchScore REAL, " +
                "punchOutFaceVerified INTEGER, " +
                "punchOutLivenessVerified INTEGER, " +
                "punchOutIdentityVerified INTEGER, " +
                "punchOutFaceMatchScore REAL, " +
                "verificationTimestamp LONG, " +
                "verificationResult TEXT, " +
                "verificationImage TEXT, " +
                "distanceFromOfficeMeters REAL, " +
                "locationAccuracyMeters REAL, " +
                "timestamp LONG)");

        // Table Leaves
        db.execSQL("CREATE TABLE leaves (" +
                "leaveId TEXT PRIMARY KEY, " +
                "studentId TEXT, " +
                "studentName TEXT, " +
                "departmentId TEXT, " +
                "reason TEXT, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "status TEXT, " +
                "remarks TEXT, " +
                "appliedOn LONG)");

        // Table Notifications
        db.execSQL("CREATE TABLE notifications (" +
                "noticeId TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "message TEXT, " +
                "targetRole TEXT, " +
                "type TEXT, " +
                "senderName TEXT, " +
                "timestamp LONG)");

        // Table Timetable
        db.execSQL("CREATE TABLE IF NOT EXISTS timetable (" +
                "id TEXT PRIMARY KEY, " +
                "day TEXT, " +
                "startTime TEXT, " +
                "endTime TEXT, " +
                "subjectId TEXT, " +
                "subjectName TEXT, " +
                "teacherId TEXT, " +
                "teacherName TEXT, " +
                "roomNumber TEXT, " +
                "departmentId TEXT, " +
                "departmentName TEXT, " +
                "division TEXT, " +
                "semester TEXT, " +
                "lectureNumber INTEGER, " +
                "isBreak INTEGER)");

        // Table Student Notifications
        db.execSQL("CREATE TABLE IF NOT EXISTS student_notifications (" +
                "id TEXT PRIMARY KEY, " +
                "studentUid TEXT, " +
                "title TEXT, " +
                "message TEXT, " +
                "type TEXT, " +
                "timestamp LONG, " +
                "isRead INTEGER)");

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ensureAttendanceColumnsExist(db);
    }

    private void seedDefaultData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // --- Departments Seeding ---
        String[][] depts = {
                {"d1", "MCA", "Master of Computer Applications", "Postgraduate IT Program"},
                {"d2", "BCA", "Bachelor of Computer Applications", "Undergraduate IT Program"},
                {"d3", "BBA", "Bachelor of Business Administration", "Management Studies"},
                {"d4", "BTech", "Bachelor of Technology", "Engineering Program"},
                {"d5", "MBA", "Master of Business Administration", "Business Program"}
        };
        for (String[] d : depts) {
            ContentValues cv = new ContentValues();
            cv.put("departmentId", d[0]);
            cv.put("code", d[1]);
            cv.put("name", d[2]);
            cv.put("description", d[3]);
            cv.put("createdAt", now);
            db.insertWithOnConflict("departments", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }

        // --- Subjects Seeding ---
        String[][] subData = {
                // MCA
                {"sub_m1", "MCA-101", "Advanced Java Programming", "d1", "MCA", "1", "4", "uid_t1", "Prof. Amit Sharma"},
                {"sub_m2", "MCA-102", "Cloud Computing & DevOps", "d1", "MCA", "1", "4", "uid_t2", "Prof. Neha Gupta"},
                {"sub_m3", "MCA-103", "Data Science & AI", "d1", "MCA", "1", "4", "uid_t3", "Prof. Vikas Patil"},
                // BCA
                {"sub_bc1", "BCA-101", "Programming in C", "d2", "BCA", "1", "4", "uid_t4", "Prof. Priya Malhotra"},
                {"sub_bc2", "BCA-102", "Web Technologies", "d2", "BCA", "1", "3", "uid_t5", "Prof. Rahul Verma"},
                {"sub_bc3", "BCA-103", "Database Systems", "d2", "BCA", "1", "4", "uid_t6", "Prof. Sneha Deshmukh"},
                // BBA
                {"sub_bb1", "BBA-101", "Business Management", "d3", "BBA", "1", "3", "uid_admin", "System Administrator"},
                {"sub_bb2", "BBA-102", "Organizational Behavior", "d3", "BBA", "1", "3", "uid_admin", "System Administrator"},
                {"sub_bb3", "BBA-103", "Marketing Management", "d3", "BBA", "1", "3", "uid_admin", "System Administrator"}
        };

        for (String[] s : subData) {
            ContentValues cv = new ContentValues();
            cv.put("subjectId", s[0]);
            cv.put("code", s[1]);
            cv.put("name", s[2]);
            cv.put("departmentId", s[3]);
            cv.put("departmentName", s[4]);
            cv.put("semester", s[5]);
            cv.put("credits", Integer.parseInt(s[6]));
            cv.put("teacherId", s[7]);
            cv.put("teacherName", s[8]);
            db.insertWithOnConflict("subjects", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }

        // --- Teachers Seeding (6 Faculties) ---
        String[][] teacherData = {
                {"t1", "uid_t1", "Prof. Amit Sharma", "amit@test.com", "d1", "MCA", "Professor", "9876543201"},
                {"t2", "uid_t2", "Prof. Neha Gupta", "neha@test.com", "d1", "MCA", "Asst. Professor", "9876543202"},
                {"t3", "uid_t3", "Prof. Vikas Patil", "vikas@test.com", "d1", "MCA", "Lecturer", "9876543203"},
                {"t4", "uid_t4", "Prof. Priya Malhotra", "priya@test.com", "d2", "BCA", "Professor", "9876543204"},
                {"t5", "uid_t5", "Prof. Rahul Verma", "rahul@test.com", "d2", "BCA", "Asst. Professor", "9876543205"},
                {"t6", "uid_t6", "Prof. Sneha Deshmukh", "sneha@test.com", "d2", "BCA", "Lecturer", "9876543206"}
        };

        for (String[] t : teacherData) {
            ContentValues cvT = new ContentValues();
            cvT.put("teacherId", t[0]);
            cvT.put("uid", t[1]);
            cvT.put("name", t[2]);
            cvT.put("email", t[3]);
            cvT.put("departmentId", t[4]);
            cvT.put("departmentName", t[5]);
            cvT.put("designation", t[6]);
            cvT.put("phone", t[7]);
            cvT.put("createdAt", now);
            db.insertWithOnConflict("teachers", null, cvT, SQLiteDatabase.CONFLICT_REPLACE);

            ContentValues cvU = new ContentValues();
            cvU.put("uid", t[1]);
            cvU.put("email", t[3]);
            cvU.put("password", "");
            cvU.put("name", t[2]);
            cvU.put("role", Constants.ROLE_TEACHER);
            cvU.put("createdAt", now);
            cvU.put("active", 1);
            db.insertWithOnConflict("users", null, cvU, SQLiteDatabase.CONFLICT_REPLACE);
        }

        // --- Students Seeding ---
        String[][] studentData = {
                {"st_m1", "uid_sm1", "MCA Student 1", "mca1@test.com", "MCA001", "d1", "MCA", "1", "A"},
                {"st_m2", "uid_sm2", "MCA Student 2", "mca2@test.com", "MCA002", "d1", "MCA", "1", "A"},
                {"st_bc1", "uid_sbc1", "BCA Student 1", "bca1@test.com", "BCA001", "d2", "BCA", "1", "A"}
        };

        for (String[] s : studentData) {
            ContentValues cvS = new ContentValues();
            cvS.put("studentId", s[0]);
            cvS.put("uid", s[1]);
            cvS.put("name", s[2]);
            cvS.put("email", s[3]);
            cvS.put("rollNo", s[4]);
            cvS.put("departmentId", s[5]);
            cvS.put("departmentName", s[6]);
            cvS.put("semester", s[7]);
            cvS.put("section", s[8]);
            cvS.put("createdAt", now);
            db.insertWithOnConflict("students", null, cvS, SQLiteDatabase.CONFLICT_REPLACE);

            ContentValues cvU = new ContentValues();
            cvU.put("uid", s[1]);
            cvU.put("email", s[3]);
            cvU.put("password", "");
            cvU.put("name", s[2]);
            cvU.put("role", Constants.ROLE_STUDENT);
            cvU.put("createdAt", now);
            cvU.put("active", 1);
            db.insertWithOnConflict("users", null, cvU, SQLiteDatabase.CONFLICT_REPLACE);
        }

        // --- Timetable Seeding for MCA Div A ---
        db.execSQL("DELETE FROM timetable"); // Clear old data to force re-seeding with new teachers
        seedTimetable(db, "d1", "MCA", "A", "1");
    }

    private void seedTimetable(SQLiteDatabase db, String deptId, String deptName, String div, String sem) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[][] slots = {
                {"11:00 AM", "12:00 PM", "1"},
                {"12:00 PM", "01:00 PM", "2"},
                {"02:00 PM", "03:00 PM", "3"},
                {"03:00 PM", "04:00 PM", "4"},
                {"04:00 PM", "05:00 PM", "5"}
        };

        // Fetch actual teachers from the database if they exist
        List<String[]> actualTeachers = new ArrayList<>();
        Cursor tCursor = db.rawQuery("SELECT uid, name FROM teachers WHERE departmentId = ? OR departmentName = ?", new String[]{deptId, deptName});
        if (tCursor != null && tCursor.moveToFirst()) {
            do {
                actualTeachers.add(new String[]{tCursor.getString(0), tCursor.getString(1)});
            } while (tCursor.moveToNext());
            tCursor.close();
        }

        // Fallback to defaults if no teachers found
        if (actualTeachers.isEmpty()) {
            actualTeachers.add(new String[]{"uid_t1", "Prof. Amit Sharma"});
            actualTeachers.add(new String[]{"uid_t2", "Prof. Neha Gupta"});
            actualTeachers.add(new String[]{"uid_t3", "Prof. Vikas Patil"});
            actualTeachers.add(new String[]{"uid_t4", "Prof. Priya Malhotra"});
            actualTeachers.add(new String[]{"uid_t5", "Prof. Rahul Verma"});
            actualTeachers.add(new String[]{"uid_t6", "Prof. Sneha Deshmukh"});
        }

        String[][] mcaSubjects = {
                {"sub_m1", "Advanced Java Programming", "Room 101"},
                {"sub_m2", "Cloud Computing & DevOps", "Room 102"},
                {"sub_m3", "Data Science & AI", "Room 103"},
                {"sub_bc2", "Web Technologies", "Lab 1"},
                {"sub_bc3", "Database Systems", "Lab 2"}
        };

        for (String day : days) {
            for (int i = 0; i < slots.length; i++) {
                ContentValues cv = new ContentValues();
                String id = "tt_" + day + "_" + div + "_" + (i + 1);
                cv.put("id", id);
                cv.put("day", day);
                cv.put("startTime", slots[i][0]);
                cv.put("endTime", slots[i][1]);
                
                int subIdx = (i + day.length()) % mcaSubjects.length;
                String[] sub = mcaSubjects[subIdx];
                
                // Assign teacher from the actual list
                String[] teacher = actualTeachers.get(i % actualTeachers.size());
                
                cv.put("subjectId", sub[0]);
                cv.put("subjectName", sub[1]);
                cv.put("teacherId", teacher[0]);
                cv.put("teacherName", teacher[1]);
                cv.put("roomNumber", sub[2]);
                cv.put("departmentId", deptId);
                cv.put("departmentName", deptName);
                cv.put("division", div);
                cv.put("semester", sem);
                cv.put("lectureNumber", Integer.parseInt(slots[i][2]));
                cv.put("isBreak", 0);
                db.insertWithOnConflict("timetable", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            
            // Seed LUNCH BREAK
            ContentValues cvB = new ContentValues();
            cvB.put("id", "BREAK_" + day + "_" + div);
            cvB.put("day", day);
            cvB.put("startTime", "01:00 PM");
            cvB.put("endTime", "02:00 PM");
            cvB.put("subjectId", "");
            cvB.put("subjectName", "LUNCH BREAK");
            cvB.put("teacherId", "");
            cvB.put("teacherName", "Rest Time");
            cvB.put("roomNumber", "Cafeteria");
            cvB.put("departmentId", deptId);
            cvB.put("departmentName", deptName);
            cvB.put("division", div);
            cvB.put("semester", sem);
            cvB.put("lectureNumber", 0);
            cvB.put("isBreak", 1);
            db.insertWithOnConflict("timetable", null, cvB, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public int getStudentCount() {
        return getAllStudents().size();
    }

    public int getAssignedStudentCount(String teacherUid) {
        if (teacherUid == null || teacherUid.isEmpty()) {
            List<Student> all = getAllStudents();
            return all != null ? all.size() : 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cTeacher = db.rawQuery("SELECT name, departmentId, departmentName FROM teachers WHERE uid = ? OR teacherId = ? LIMIT 1", new String[]{teacherUid, teacherUid});
        String tName = "", deptId = "", deptName = "";
        if (cTeacher != null && cTeacher.moveToFirst()) {
            tName = cTeacher.getString(0);
            deptId = cTeacher.getString(1);
            deptName = cTeacher.getString(2);
            cTeacher.close();
        } else if (cTeacher != null) {
            cTeacher.close();
        }

        // Students assigned to divisions/subjects where this teacher has lectures or assignments
        Cursor c = db.rawQuery("SELECT COUNT(DISTINCT s.studentId) FROM students s " +
                "INNER JOIN timetable t ON s.section = t.division AND (s.departmentId = t.departmentId OR s.departmentName = t.departmentName) " +
                "WHERE t.teacherId = ? OR LOWER(t.teacherName) = LOWER(?)", new String[]{teacherUid, tName});
        int count = 0;
        if (c != null && c.moveToFirst()) {
            count = c.getInt(0);
            c.close();
        } else if (c != null) {
            c.close();
        }

        if (count > 0) return count;

        List<Subject> assigned = getAssignedSubjectsForTeacher(teacherUid, tName);
        java.util.Set<String> sems = new java.util.HashSet<>();
        if (assigned != null) {
            for (Subject s : assigned) {
                if (s.getSemester() != null && !s.getSemester().isEmpty()) {
                    sems.add(s.getSemester());
                }
            }
        }

        if (!sems.isEmpty() && (deptId != null || deptName != null)) {
            int totalCount = 0;
            for (String sem : sems) {
                List<Student> stList = getStudentsByFilter(deptId, "", sem);
                totalCount += stList.size();
            }
            if (totalCount > 0) return totalCount;
        }

        // Fallback 1: Students in teacher's department
        if (deptId != null && !deptId.isEmpty()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM students WHERE departmentId = ? OR departmentName = ?", new String[]{deptId, deptName});
            if (cursor != null && cursor.moveToFirst()) {
                int cnt = cursor.getInt(0);
                cursor.close();
                if (cnt > 0) return cnt;
            } else if (cursor != null) {
                cursor.close();
            }
        }

        // Fallback 2: Total registered students
        List<Student> all = getAllStudents();
        return all != null ? all.size() : 0;
    }

    public int getTeacherCount() {
        return getAllTeachers().size();
    }

    public int[] getTodayAttendanceStats(String date) {
        int present = 0, absent = 0, leave = 0;
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT status, COUNT(*) FROM (" +
                        "  SELECT status FROM attendance WHERE date = ? GROUP BY studentId HAVING MAX(rowid)" +
                        ") GROUP BY status", new String[]{date});
        if (c != null && c.moveToFirst()) {
            do {
                String st = c.getString(0);
                int cnt = c.getInt(1);
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(st)) present = cnt;
                else if (Constants.STATUS_ABSENT.equalsIgnoreCase(st)) absent = cnt;
                else if (Constants.STATUS_LEAVE.equalsIgnoreCase(st)) leave = cnt;
            } while (c.moveToNext());
            c.close();
        }

        return new int[]{present, absent, leave};
    }

    public boolean markQRAttendance(String studentUid, String subjectId, String date, String markedByUid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM students WHERE uid = ? OR studentId = ?", new String[]{studentUid, studentUid});
        String stId = studentUid;
        String realUid = studentUid;
        String stName = "Student";
        String roll = "STD001";
        String deptId = "d1";
        String deptName = "MCA";
        String section = "A";
        String semester = "4";
        if (c != null && c.moveToFirst()) {
            stId = c.getString(c.getColumnIndexOrThrow("studentId"));
            realUid = c.getString(c.getColumnIndexOrThrow("uid"));
            stName = c.getString(c.getColumnIndexOrThrow("name"));
            roll = c.getString(c.getColumnIndexOrThrow("rollNo"));
            deptId = c.getString(c.getColumnIndexOrThrow("departmentId"));
            deptName = c.getString(c.getColumnIndexOrThrow("departmentName"));
            section = c.getString(c.getColumnIndexOrThrow("section"));
            semester = c.getString(c.getColumnIndexOrThrow("semester"));
            c.close();
        } else if (c != null) {
            c.close();
        }

        ContentValues cv = new ContentValues();
        String tId = ""; // Use timetableId if available in a real context
        String id = date + "_" + stId + "_" + subjectId;
        cv.put("attendanceId", id);
        cv.put("date", date);
        cv.put("studentId", stId);
        cv.put("studentUid", realUid);
        cv.put("studentName", stName);
        cv.put("rollNo", roll);
        cv.put("departmentId", deptId);
        cv.put("departmentName", deptName);
        cv.put("section", section);
        cv.put("semester", semester);
        cv.put("subjectId", subjectId);
        cv.put("subjectName", "Subject " + subjectId.toUpperCase());
        cv.put("status", Constants.STATUS_PRESENT);
        cv.put("markedByUid", markedByUid);
        cv.put("timestamp", System.currentTimeMillis());

        long result = db.insertWithOnConflict("attendance", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }


    public void addUser(User user, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("uid", user.getUid());
        cv.put("email", user.getEmail());
        cv.put("password", password != null ? password : "password123");
        cv.put("name", user.getName());
        cv.put("role", user.getRole());
        cv.put("phone", user.getPhone());
        cv.put("profileImageUrl", user.getProfileImageUrl());
        cv.put("createdAt", System.currentTimeMillis());
        cv.put("active", 1);
        db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? LIMIT 1", new String[]{email.trim()});
        if (cursor != null && cursor.moveToFirst()) {
            User u = new User();
            u.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            u.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            u.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            u.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            u.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("profileImageUrl")));
            cursor.close();
            return u;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public User getUserByUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE uid = ? LIMIT 1", new String[]{uid.trim()});
        if (cursor != null && cursor.moveToFirst()) {
            User u = new User();
            u.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            u.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            u.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            u.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            u.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("profileImageUrl")));
            cursor.close();
            return u;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Subject> getSubjectsByDepartment(String deptName) {
        List<Subject> list = new ArrayList<>();
        if (deptName == null || deptName.trim().isEmpty()) return list;
        SQLiteDatabase db = getReadableDatabase();
        String cleanDept = deptName.trim();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM subjects WHERE departmentName LIKE ? OR code LIKE ? ORDER BY code ASC",
                new String[]{"%" + cleanDept + "%", cleanDept + "%"});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Subject s = new Subject();
                s.setSubjectId(cursor.getString(cursor.getColumnIndexOrThrow("subjectId")));
                s.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                list.add(s);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM students ORDER BY rollNo ASC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Student s = new Student();
                s.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
                s.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                s.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
                s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                s.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
                s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                s.setQrCodeData(cursor.getString(cursor.getColumnIndexOrThrow("qrCodeData")));
                list.add(s);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Student getStudentByUid(String uid) {
        if (uid == null || uid.isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM students WHERE uid = ? OR studentId = ? LIMIT 1", new String[]{uid, uid});
        if (cursor != null && cursor.moveToFirst()) {
            Student s = new Student();
            s.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
            s.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            s.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
            s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
            s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
            s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
            s.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
            s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            s.setQrCodeData(cursor.getString(cursor.getColumnIndexOrThrow("qrCodeData")));
            cursor.close();
            return s;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void insertOrUpdateStudent(Student student) {
        insertOrUpdateStudent(student, "student123");
    }

    public void insertOrUpdateStudent(Student student, String password) {
        if (student == null || student.getName() == null) return;
        
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String id = student.getStudentId() != null && !student.getStudentId().isEmpty() ?
                student.getStudentId() : "st_" + System.currentTimeMillis();
        String uid = student.getUid() != null && !student.getUid().isEmpty() ?
                student.getUid() : "uid_st_" + System.currentTimeMillis();
        
        // If student has a valid UID, ensure we don't overwrite it with a mock one if already present
        if (student.getUid() == null || student.getUid().isEmpty()) {
            Student existing = getStudentByUid(id);
            if (existing != null && existing.getUid() != null && !existing.getUid().startsWith("uid_st_")) {
                uid = existing.getUid();
            }
        }
        
        student.setStudentId(id);
        student.setUid(uid);

        cv.put("studentId", id);
        cv.put("uid", uid);
        cv.put("name", student.getName());
        cv.put("email", student.getEmail());
        cv.put("rollNo", student.getRollNo());
        cv.put("departmentId", student.getDepartmentId() != null ? student.getDepartmentId() : "d1");
        cv.put("departmentName", student.getDepartmentName() != null ? student.getDepartmentName() : "MCA");
        cv.put("semester", student.getSemester() != null ? student.getSemester() : "4");
        cv.put("section", student.getSection() != null ? student.getSection() : "A");
        cv.put("phone", student.getPhone() != null ? student.getPhone() : "9876543210");
        cv.put("qrCodeData", student.getRollNo());
        cv.put("createdAt", System.currentTimeMillis());
        db.insertWithOnConflict("students", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        // Also insert/update corresponding User authentication record
        ContentValues cvUser = new ContentValues();
        cvUser.put("uid", uid);
        cvUser.put("email", student.getEmail());
        cvUser.put("password", password != null && !password.isEmpty() ? password : "student123");
        cvUser.put("name", student.getName());
        cvUser.put("role", Constants.ROLE_STUDENT);
        cvUser.put("phone", student.getPhone() != null ? student.getPhone() : "9876543210");
        cvUser.put("createdAt", System.currentTimeMillis());
        cvUser.put("active", 1);
        db.insertWithOnConflict("users", null, cvUser, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteStudent(String studentId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uid FROM students WHERE studentId = ?", new String[]{studentId});
        if (cursor != null && cursor.moveToFirst()) {
            String uid = cursor.getString(0);
            db.delete("users", "uid = ?", new String[]{uid});
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.delete("students", "studentId = ?", new String[]{studentId});
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM teachers", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Teacher t = new Teacher();
                t.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
                t.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                t.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                t.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                t.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                t.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                t.setDesignation(cursor.getString(cursor.getColumnIndexOrThrow("designation")));
                t.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                list.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Teacher getTeacherByEmail(String email) {
        if (email == null) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM teachers WHERE LOWER(email) = ?", new String[]{email.toLowerCase().trim()});
        if (cursor != null && cursor.moveToFirst()) {
            Teacher t = new Teacher();
            t.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
            t.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            t.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            t.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            t.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
            t.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
            t.setDesignation(cursor.getString(cursor.getColumnIndexOrThrow("designation")));
            t.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            cursor.close();
            return t;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public Teacher getTeacherByUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM teachers WHERE uid = ? OR teacherId = ?", new String[]{uid.trim(), uid.trim()});
        if (cursor != null && cursor.moveToFirst()) {
            Teacher t = new Teacher();
            t.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
            t.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            t.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            t.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            t.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
            t.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
            t.setDesignation(cursor.getString(cursor.getColumnIndexOrThrow("designation")));
            t.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            cursor.close();
            return t;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void insertOrUpdateTeacher(Teacher teacher) {
        insertOrUpdateTeacher(teacher, "teacher123");
    }

    public void insertOrUpdateTeacher(Teacher teacher, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String id = teacher.getTeacherId() != null && !teacher.getTeacherId().isEmpty() ?
                teacher.getTeacherId() : "t_" + System.currentTimeMillis();
        String uid = teacher.getUid() != null && !teacher.getUid().isEmpty() ?
                teacher.getUid() : "uid_t_" + System.currentTimeMillis();
        teacher.setTeacherId(id);
        teacher.setUid(uid);

        cv.put("teacherId", id);
        cv.put("uid", uid);
        cv.put("name", teacher.getName());
        cv.put("email", teacher.getEmail());
        cv.put("departmentId", teacher.getDepartmentId() != null ? teacher.getDepartmentId() : "d1");
        cv.put("departmentName", teacher.getDepartmentName() != null ? teacher.getDepartmentName() : "MCA");
        cv.put("designation", teacher.getDesignation() != null ? teacher.getDesignation() : "Assistant Professor");
        cv.put("phone", teacher.getPhone() != null ? teacher.getPhone() : "9988776655");
        cv.put("createdAt", System.currentTimeMillis());
        db.insertWithOnConflict("teachers", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        // Also insert/update corresponding User authentication record
        ContentValues cvUser = new ContentValues();
        cvUser.put("uid", uid);
        cvUser.put("email", teacher.getEmail());
        cvUser.put("password", password != null && !password.isEmpty() ? password : "teacher123");
        cvUser.put("name", teacher.getName());
        cvUser.put("role", Constants.ROLE_TEACHER);
        cvUser.put("phone", teacher.getPhone() != null ? teacher.getPhone() : "9988776655");
        cvUser.put("createdAt", System.currentTimeMillis());
        cvUser.put("active", 1);
        db.insertWithOnConflict("users", null, cvUser, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteTeacher(String teacherId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uid FROM teachers WHERE teacherId = ?", new String[]{teacherId});
        if (cursor != null && cursor.moveToFirst()) {
            String uid = cursor.getString(0);
            db.delete("users", "uid = ?", new String[]{uid});
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.delete("teachers", "teacherId = ?", new String[]{teacherId});
    }

    // --- Department Methods ---
    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM departments", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Department d = new Department();
                d.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                d.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                d.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                d.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                list.add(d);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void insertDepartment(Department d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("departmentId", d.getDepartmentId());
        cv.put("code", d.getCode());
        cv.put("name", d.getName());
        cv.put("description", d.getDescription());
        cv.put("createdAt", d.getCreatedAt());
        db.insertWithOnConflict("departments", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // --- Subject Methods ---
    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM subjects", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Subject s = new Subject();
                s.setSubjectId(cursor.getString(cursor.getColumnIndexOrThrow("subjectId")));
                s.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                s.setCredits(cursor.getInt(cursor.getColumnIndexOrThrow("credits")));
                s.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
                s.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacherName")));
                list.add(s);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void insertSubject(Subject s) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subjectId", s.getSubjectId() != null ? s.getSubjectId() : "sub_" + System.currentTimeMillis());
        cv.put("code", s.getCode());
        cv.put("name", s.getName());
        cv.put("departmentId", s.getDepartmentId());
        cv.put("departmentName", s.getDepartmentName());
        cv.put("semester", s.getSemester());
        cv.put("credits", s.getCredits());
        cv.put("teacherId", s.getTeacherId());
        cv.put("teacherName", s.getTeacherName());
        db.insertWithOnConflict("subjects", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int  getAssignedSubjectCountForTeacher(String teacherUid, String teacherName) {
        if ((teacherUid == null || teacherUid.isEmpty()) && (teacherName == null || teacherName.isEmpty())) return 0;
        SQLiteDatabase db = getReadableDatabase();
        String cleanUid = teacherUid != null ? teacherUid.trim() : "";
        String cleanName = teacherName != null ? teacherName.trim() : "";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM subjects WHERE (teacherId = ? AND teacherId != '') OR (LOWER(teacherName) = LOWER(?) AND teacherName != '')",
                new String[]{cleanUid, cleanName});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return count;
    }

    public List<Subject> getAssignedSubjectsForTeacher(String teacherUid, String teacherName) {
        List<Subject> list = new ArrayList<>();
        if ((teacherUid == null || teacherUid.isEmpty()) && (teacherName == null || teacherName.isEmpty())) return list;
        SQLiteDatabase db = getReadableDatabase();
        String cleanUid = teacherUid != null ? teacherUid.trim() : "";
        String cleanName = teacherName != null ? teacherName.trim() : "";
        Cursor cursor = db.rawQuery("SELECT * FROM subjects WHERE (teacherId = ? AND teacherId != '') OR (LOWER(teacherName) = LOWER(?) AND teacherName != '')",
                new String[]{cleanUid, cleanName});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Subject s = new Subject();
                s.setSubjectId(cursor.getString(cursor.getColumnIndexOrThrow("subjectId")));
                s.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                s.setCredits(cursor.getInt(cursor.getColumnIndexOrThrow("credits")));
                s.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
                s.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacherName")));
                list.add(s);
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    // --- Attendance Methods ---
    public void saveAttendanceBatch(List<Attendance> list) {
        if (list == null || list.isEmpty()) return;
        List<Student> registered = getAllStudents();
        java.util.Set<String> validIdentifiers = new java.util.HashSet<>();
        for (Student s : registered) {
            if (s.getStudentId() != null) validIdentifiers.add(s.getStudentId().toLowerCase().trim());
            if (s.getRollNo() != null) validIdentifiers.add(s.getRollNo().toLowerCase().trim());
            if (s.getUid() != null) validIdentifiers.add(s.getUid().toLowerCase().trim());
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Attendance a : list) {
                String sId = a.getStudentId() != null ? a.getStudentId().toLowerCase().trim() : "";
                String rNo = a.getRollNo() != null ? a.getRollNo().toLowerCase().trim() : "";
                if ((!sId.isEmpty() && validIdentifiers.contains(sId)) || (!rNo.isEmpty() && validIdentifiers.contains(rNo))) {
                    ContentValues cv = new ContentValues();
                    String tId = a.getTimetableId() != null && !a.getTimetableId().isEmpty() ? a.getTimetableId() : a.getSubjectId();
                    String id = a.getAttendanceId() != null && !a.getAttendanceId().isEmpty() ?
                            a.getAttendanceId() : a.getDate() + "_" + a.getStudentId() + "_" + tId;
                    a.setAttendanceId(id);

                    cv.put("attendanceId", id);
                    cv.put("date", a.getDate());
                    cv.put("day", a.getDay());
                    cv.put("studentId", a.getStudentId());
                    cv.put("studentUid", a.getStudentUid());
                    cv.put("studentName", a.getStudentName());
                    cv.put("rollNo", a.getRollNo());
                    cv.put("departmentId", a.getDepartmentId());
                    cv.put("departmentName", a.getDepartmentName());
                    cv.put("section", a.getSection());
                    cv.put("semester", a.getSemester());
                    cv.put("subjectId", a.getSubjectId());
                    cv.put("subjectName", a.getSubjectName());
                    cv.put("teacherId", a.getTeacherId());
                    cv.put("punchInTime", a.getPunchInTime());
                    cv.put("punchOutTime", a.getPunchOutTime());
                    cv.put("punchInTimestamp", a.getPunchInTimestamp());
                    cv.put("punchOutTimestamp", a.getPunchOutTimestamp());
                    cv.put("workingDuration", a.getWorkingDuration());
                    cv.put("punchInStatus", a.getPunchInStatus());
                    cv.put("punchOutStatus", a.getPunchOutStatus());
                    cv.put("punchInLatitude", a.getPunchInLatitude());
                    cv.put("punchInLongitude", a.getPunchInLongitude());
                    cv.put("punchInDistanceFromOffice", a.getPunchInDistanceFromOffice());
                    cv.put("punchInAccuracy", a.getPunchInAccuracy());
                    cv.put("punchInGeofenceVerified", a.isPunchInGeofenceVerified() ? 1 : 0);
                    cv.put("punchOutLatitude", a.getPunchOutLatitude());
                    cv.put("punchOutLongitude", a.getPunchOutLongitude());
                    cv.put("punchOutAccuracy", a.getPunchOutAccuracy());
                    cv.put("punchOutDistanceFromOffice", a.getPunchOutDistanceFromOffice());
                    cv.put("punchOutGeofenceVerified", a.isPunchOutGeofenceVerified() ? 1 : 0);
                    cv.put("geofenceVerified", a.isGeofenceVerified() ? 1 : 0);
                    cv.put("faceVerified", a.isFaceVerified() ? 1 : 0);
                    cv.put("livenessVerified", a.isLivenessVerified() ? 1 : 0);
                    cv.put("identityVerified", a.isIdentityVerified() ? 1 : 0);
                    cv.put("faceMatchScore", a.getFaceMatchScore());
                    cv.put("punchOutFaceVerified", a.isPunchOutFaceVerified() ? 1 : 0);
                    cv.put("punchOutLivenessVerified", a.isPunchOutLivenessVerified() ? 1 : 0);
                    cv.put("punchOutIdentityVerified", a.isPunchOutIdentityVerified() ? 1 : 0);
                    cv.put("punchOutFaceMatchScore", a.getPunchOutFaceMatchScore());
                    cv.put("verificationTimestamp", a.getVerificationTimestamp());
                    cv.put("verificationResult", a.getVerificationResult());
                    cv.put("verificationImage", a.getVerificationImage());
                    cv.put("distanceFromOfficeMeters", a.getDistanceFromOfficeMeters());
                    cv.put("locationAccuracyMeters", a.getLocationAccuracyMeters());
                    cv.put("sessionCompleted", a.isSessionCompleted() ? 1 : 0);
                    cv.put("attendanceStatus", a.getStatus());
                    cv.put("timestamp", System.currentTimeMillis());
                    db.insertWithOnConflict("attendance", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void insertOrUpdateAttendanceRecord(Attendance a) {
        if (a == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String tId = a.getTimetableId() != null && !a.getTimetableId().isEmpty() ? a.getTimetableId() : a.getSubjectId();
        String id = a.getAttendanceId() != null && !a.getAttendanceId().isEmpty() ?
                a.getAttendanceId() : a.getDate() + "_" + a.getStudentId() + "_" + tId;
        a.setAttendanceId(id);

        cv.put("attendanceId", id);
        cv.put("date", a.getDate());
        cv.put("day", a.getDay());
        cv.put("studentId", a.getStudentId());
        cv.put("studentUid", a.getStudentUid());
        cv.put("studentName", a.getStudentName());
        cv.put("rollNo", a.getRollNo());
        cv.put("departmentId", a.getDepartmentId());
        cv.put("departmentName", a.getDepartmentName());
        cv.put("section", a.getSection());
        cv.put("semester", a.getSemester());
        cv.put("subjectId", a.getSubjectId());
        cv.put("subjectName", a.getSubjectName());
        cv.put("teacherId", a.getTeacherId());
        cv.put("teacherName", a.getTeacherName());
        cv.put("status", a.getStatus());
        cv.put("markedByUid", a.getMarkedByUid());
        cv.put("timetableId", a.getTimetableId());
        cv.put("lectureNumber", a.getLectureNumber());
        cv.put("startTime", a.getStartTime());
        cv.put("endTime", a.getEndTime());
        cv.put("lectureTime", a.getLectureTime());
        cv.put("punchInTime", a.getPunchInTime());
        cv.put("punchOutTime", a.getPunchOutTime());
        cv.put("punchInTimestamp", a.getPunchInTimestamp());
        cv.put("punchOutTimestamp", a.getPunchOutTimestamp());
        cv.put("workingDuration", a.getWorkingDuration());
        cv.put("punchInStatus", a.getPunchInStatus());
        cv.put("punchOutStatus", a.getPunchOutStatus());
        cv.put("punchInLatitude", a.getPunchInLatitude());
        cv.put("punchInLongitude", a.getPunchInLongitude());
        cv.put("punchInDistanceFromOffice", a.getPunchInDistanceFromOffice());
        cv.put("punchInAccuracy", a.getPunchInAccuracy());
        cv.put("punchInGeofenceVerified", a.isPunchInGeofenceVerified() ? 1 : 0);
        cv.put("punchOutLatitude", a.getPunchOutLatitude());
        cv.put("punchOutLongitude", a.getPunchOutLongitude());
        cv.put("punchOutAccuracy", a.getPunchOutAccuracy());
        cv.put("punchOutDistanceFromOffice", a.getPunchOutDistanceFromOffice());
        cv.put("punchOutGeofenceVerified", a.isPunchOutGeofenceVerified() ? 1 : 0);
        cv.put("geofenceVerified", a.isGeofenceVerified() ? 1 : 0);
        cv.put("faceVerified", a.isFaceVerified() ? 1 : 0);
        cv.put("livenessVerified", a.isLivenessVerified() ? 1 : 0);
        cv.put("identityVerified", a.isIdentityVerified() ? 1 : 0);
        cv.put("faceMatchScore", a.getFaceMatchScore());
        cv.put("punchOutFaceVerified", a.isPunchOutFaceVerified() ? 1 : 0);
        cv.put("punchOutLivenessVerified", a.isPunchOutLivenessVerified() ? 1 : 0);
        cv.put("punchOutIdentityVerified", a.isPunchOutIdentityVerified() ? 1 : 0);
        cv.put("punchOutFaceMatchScore", a.getPunchOutFaceMatchScore());
        cv.put("verificationTimestamp", a.getVerificationTimestamp());
        cv.put("verificationResult", a.getVerificationResult());
        cv.put("verificationImage", a.getVerificationImage());
        cv.put("distanceFromOfficeMeters", a.getDistanceFromOfficeMeters());
        cv.put("locationAccuracyMeters", a.getLocationAccuracyMeters());
        cv.put("sessionCompleted", a.isSessionCompleted() ? 1 : 0);
        cv.put("attendanceStatus", a.getStatus());
        cv.put("timestamp", System.currentTimeMillis());
        db.insertWithOnConflict("attendance", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Attendance getTodayAttendanceForUser(String date, String userUid) {
        if (userUid == null || userUid.trim().isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        String todayDate = date != null ? date.trim() : DateTimeUtils.getCurrentDateDb();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        String startOfTodayStr = String.valueOf(startOfToday);

        // 1. Priority: Check for ANY record for THIS user (student or teacher) recorded TODAY (by timestamp OR date string) with non-empty punchInTime
        Cursor cursor = db.rawQuery(
                "SELECT * FROM attendance WHERE (studentUid = ? OR studentId = ? OR teacherId = ? OR markedByUid = ? OR attendanceId LIKE ?) AND (date = ? OR date LIKE ? OR punchInTimestamp >= ? OR timestamp >= ?) AND punchInTime IS NOT NULL AND punchInTime != '' ORDER BY punchInTimestamp DESC, timestamp DESC LIMIT 1",
                new String[]{userUid, userUid, userUid, userUid, "%" + userUid + "%", todayDate, "%" + todayDate + "%", startOfTodayStr, startOfTodayStr});
        if (cursor != null && cursor.moveToFirst()) {
            Attendance a = parseAttendanceFromCursor(cursor);
            cursor.close();
            return a;
        }
        if (cursor != null) cursor.close();

        // 2. Fallback: Check for ANY record for THIS user today
        Cursor fallbackCursor = db.rawQuery(
                "SELECT * FROM attendance WHERE (studentUid = ? OR studentId = ? OR teacherId = ? OR markedByUid = ? OR attendanceId LIKE ?) AND (date = ? OR date LIKE ? OR timestamp >= ?) ORDER BY timestamp DESC LIMIT 1",
                new String[]{userUid, userUid, userUid, userUid, "%" + userUid + "%", todayDate, "%" + todayDate + "%", startOfTodayStr});
        if (fallbackCursor != null && fallbackCursor.moveToFirst()) {
            Attendance a = parseAttendanceFromCursor(fallbackCursor);
            fallbackCursor.close();
            return a;
        }
        if (fallbackCursor != null) fallbackCursor.close();

        return null;
    }

    private Attendance parseAttendanceFromCursor(Cursor cursor) {
        Attendance a = new Attendance();
        int idxId = cursor.getColumnIndex("attendanceId");
        int idxDate = cursor.getColumnIndex("date");
        int idxDay = cursor.getColumnIndex("day");
        int idxStudentId = cursor.getColumnIndex("studentId");
        int idxStudentUid = cursor.getColumnIndex("studentUid");
        int idxStudentName = cursor.getColumnIndex("studentName");
        int idxRoll = cursor.getColumnIndex("rollNo");
        int idxDeptId = cursor.getColumnIndex("departmentId");
        int idxDeptName = cursor.getColumnIndex("departmentName");
        int idxSection = cursor.getColumnIndex("section");
        int idxSemester = cursor.getColumnIndex("semester");
        int idxSubjectId = cursor.getColumnIndex("subjectId");
        int idxSubjectName = cursor.getColumnIndex("subjectName");
        int idxTeacherId = cursor.getColumnIndex("teacherId");
        int idxTeacherName = cursor.getColumnIndex("teacherName");
        int idxStatus = cursor.getColumnIndex("status");
        int idxMarkedBy = cursor.getColumnIndex("markedByUid");
        int idxPunchInTime = cursor.getColumnIndex("punchInTime");
        int idxPunchOutTime = cursor.getColumnIndex("punchOutTime");
        int idxPunchInTs = cursor.getColumnIndex("punchInTimestamp");
        int idxPunchOutTs = cursor.getColumnIndex("punchOutTimestamp");
        int idxDuration = cursor.getColumnIndex("workingDuration");
        int idxPunchInStat = cursor.getColumnIndex("punchInStatus");
        int idxPunchOutStat = cursor.getColumnIndex("punchOutStatus");
        int idxSessionComp = cursor.getColumnIndex("sessionCompleted");

        int idxPunchInLat = cursor.getColumnIndex("punchInLatitude");
        int idxPunchInLng = cursor.getColumnIndex("punchInLongitude");
        int idxPunchInDist = cursor.getColumnIndex("punchInDistanceFromOffice");
        int idxPunchInAcc = cursor.getColumnIndex("punchInAccuracy");
        int idxPunchInGeo = cursor.getColumnIndex("punchInGeofenceVerified");

        int idxPunchOutLat = cursor.getColumnIndex("punchOutLatitude");
        int idxPunchOutLng = cursor.getColumnIndex("punchOutLongitude");
        int idxPunchOutAcc = cursor.getColumnIndex("punchOutAccuracy");
        int idxPunchOutDist = cursor.getColumnIndex("punchOutDistanceFromOffice");
        int idxPunchOutGeo = cursor.getColumnIndex("punchOutGeofenceVerified");

        int idxGeoVer = cursor.getColumnIndex("geofenceVerified");
        int idxFaceVer = cursor.getColumnIndex("faceVerified");
        int idxLiveVer = cursor.getColumnIndex("livenessVerified");
        int idxIdVer = cursor.getColumnIndex("identityVerified");
        int idxFaceScore = cursor.getColumnIndex("faceMatchScore");

        int idxPOFaceVer = cursor.getColumnIndex("punchOutFaceVerified");
        int idxPOLiveVer = cursor.getColumnIndex("punchOutLivenessVerified");
        int idxPOIdVer = cursor.getColumnIndex("punchOutIdentityVerified");
        int idxPOFaceScore = cursor.getColumnIndex("punchOutFaceMatchScore");

        int idxVerTs = cursor.getColumnIndex("verificationTimestamp");
        int idxVerRes = cursor.getColumnIndex("verificationResult");
        int idxVerImg = cursor.getColumnIndex("verificationImage");
        int idxDistOffice = cursor.getColumnIndex("distanceFromOfficeMeters");
        int idxLocAcc = cursor.getColumnIndex("locationAccuracyMeters");

        if (idxId != -1) a.setAttendanceId(cursor.getString(idxId));
        if (idxDate != -1) a.setDate(cursor.getString(idxDate));
        if (idxDay != -1) a.setDay(cursor.getString(idxDay));
        if (idxStudentId != -1) a.setStudentId(cursor.getString(idxStudentId));
        if (idxStudentUid != -1) a.setStudentUid(cursor.getString(idxStudentUid));
        if (idxStudentName != -1) a.setStudentName(cursor.getString(idxStudentName));
        if (idxRoll != -1) a.setRollNo(cursor.getString(idxRoll));
        if (idxDeptId != -1) a.setDepartmentId(cursor.getString(idxDeptId));
        if (idxDeptName != -1) a.setDepartmentName(cursor.getString(idxDeptName));
        if (idxSection != -1) a.setSection(cursor.getString(idxSection));
        if (idxSemester != -1) a.setSemester(cursor.getString(idxSemester));
        if (idxSubjectId != -1) a.setSubjectId(cursor.getString(idxSubjectId));
        if (idxSubjectName != -1) a.setSubjectName(cursor.getString(idxSubjectName));
        if (idxTeacherId != -1) a.setTeacherId(cursor.getString(idxTeacherId));
        if (idxTeacherName != -1) a.setTeacherName(cursor.getString(idxTeacherName));
        if (idxStatus != -1) a.setStatus(cursor.getString(idxStatus));
        if (idxMarkedBy != -1) a.setMarkedByUid(cursor.getString(idxMarkedBy));
        if (idxPunchInTime != -1) a.setPunchInTime(cursor.getString(idxPunchInTime));
        if (idxPunchOutTime != -1) a.setPunchOutTime(cursor.getString(idxPunchOutTime));
        if (idxPunchInTs != -1) a.setPunchInTimestamp(cursor.getLong(idxPunchInTs));
        if (idxPunchOutTs != -1) a.setPunchOutTimestamp(cursor.getLong(idxPunchOutTs));
        if (idxDuration != -1) a.setWorkingDuration(cursor.getString(idxDuration));
        if (idxPunchInStat != -1) a.setPunchInStatus(cursor.getString(idxPunchInStat));
        if (idxPunchOutStat != -1) a.setPunchOutStatus(cursor.getString(idxPunchOutStat));
        if (idxSessionComp != -1) a.setSessionCompleted(cursor.getInt(idxSessionComp) == 1);

        if (idxPunchInLat != -1) a.setPunchInLatitude(cursor.getDouble(idxPunchInLat));
        if (idxPunchInLng != -1) a.setPunchInLongitude(cursor.getDouble(idxPunchInLng));
        if (idxPunchInDist != -1) a.setPunchInDistanceFromOffice(cursor.getDouble(idxPunchInDist));
        if (idxPunchInAcc != -1) a.setPunchInAccuracy(cursor.getDouble(idxPunchInAcc));
        if (idxPunchInGeo != -1) a.setPunchInGeofenceVerified(cursor.getInt(idxPunchInGeo) == 1);

        if (idxPunchOutLat != -1) a.setPunchOutLatitude(cursor.getDouble(idxPunchOutLat));
        if (idxPunchOutLng != -1) a.setPunchOutLongitude(cursor.getDouble(idxPunchOutLng));
        if (idxPunchOutAcc != -1) a.setPunchOutAccuracy(cursor.getDouble(idxPunchOutAcc));
        if (idxPunchOutDist != -1) a.setPunchOutDistanceFromOffice(cursor.getDouble(idxPunchOutDist));
        if (idxPunchOutGeo != -1) a.setPunchOutGeofenceVerified(cursor.getInt(idxPunchOutGeo) == 1);

        if (idxGeoVer != -1) a.setGeofenceVerified(cursor.getInt(idxGeoVer) == 1);
        if (idxFaceVer != -1) a.setFaceVerified(cursor.getInt(idxFaceVer) == 1);
        if (idxLiveVer != -1) a.setLivenessVerified(cursor.getInt(idxLiveVer) == 1);
        if (idxIdVer != -1) a.setIdentityVerified(cursor.getInt(idxIdVer) == 1);
        if (idxFaceScore != -1) a.setFaceMatchScore(cursor.getDouble(idxFaceScore));

        if (idxPOFaceVer != -1) a.setPunchOutFaceVerified(cursor.getInt(idxPOFaceVer) == 1);
        if (idxPOLiveVer != -1) a.setPunchOutLivenessVerified(cursor.getInt(idxPOLiveVer) == 1);
        if (idxPOIdVer != -1) a.setPunchOutIdentityVerified(cursor.getInt(idxPOIdVer) == 1);
        if (idxPOFaceScore != -1) a.setPunchOutFaceMatchScore(cursor.getDouble(idxPOFaceScore));

        if (idxVerTs != -1) a.setVerificationTimestamp(cursor.getLong(idxVerTs));
        if (idxVerRes != -1) a.setVerificationResult(cursor.getString(idxVerRes));
        if (idxVerImg != -1) a.setVerificationImage(cursor.getString(idxVerImg));
        if (idxDistOffice != -1) a.setDistanceFromOfficeMeters(cursor.getDouble(idxDistOffice));
        if (idxLocAcc != -1) a.setLocationAccuracyMeters(cursor.getDouble(idxLocAcc));

        return a;
    }

    public List<Student> getStudentsByFilter(String deptId, String section, String semester) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Log.d("DB_FILTER", "getStudentsByFilter: deptId=" + deptId + ", section=" + section + ", semester=" + semester);

        StringBuilder query = new StringBuilder("SELECT * FROM students WHERE 1=1");
        List<String> args = new ArrayList<>();
        
        if (deptId != null && !deptId.isEmpty() && !"select division".equalsIgnoreCase(deptId) && !"select department".equalsIgnoreCase(deptId)) {
            query.append(" AND (departmentId = ? OR departmentName = ? OR departmentId LIKE ? OR departmentName LIKE ?)");
            args.add(deptId);
            args.add(deptId);
            args.add("%" + deptId + "%");
            args.add("%" + deptId + "%");
        }

        String cleanSec = section != null ? section.trim() : "";
        if (!cleanSec.isEmpty() && !"select division".equalsIgnoreCase(cleanSec)) {
            String secLetter = cleanSec;
            if (cleanSec.contains("-")) {
                secLetter = cleanSec.substring(cleanSec.lastIndexOf("-") + 1).trim();
            }
            query.append(" AND (section = ? OR section = ? OR section = ? OR section LIKE ?)");
            args.add(cleanSec);
            args.add(secLetter);
            args.add("Div " + secLetter);
            args.add("%" + secLetter + "%");
        }
        
        String baseQuery = query.toString();
        List<String> baseArgs = new ArrayList<>(args);

        // Try strict match on semester if provided
        if (semester != null && !semester.isEmpty()) {
            query.append(" AND (semester = ? OR semester LIKE ?)");
            args.add(semester);
            args.add("%" + semester + "%");
        }
        query.append(" ORDER BY rollNo ASC, name ASC");

        Log.d("DB_FILTER", "Running query: " + query.toString() + " with args: " + args);

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Student s = new Student();
                s.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
                s.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                s.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
                s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                s.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
                s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                list.add(s);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d("DB_FILTER", "Initial match count: " + list.size());
        
        // Fallback 1: If no students found for specific semester, show all in department/section
        if (list.isEmpty() && deptId != null && !deptId.isEmpty()) {
            Log.d("DB_FILTER", "Fallback 1: Searching department + section without semester");
            cursor = db.rawQuery(baseQuery + " ORDER BY name ASC", baseArgs.toArray(new String[0]));
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Student s = new Student();
                    s.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
                    s.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                    s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                    s.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
                    s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                    s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                    s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                    s.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
                    s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                    list.add(s);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        
        // Fallback 2: Search by department only if section also failed
        if (list.isEmpty() && deptId != null && !deptId.isEmpty()) {
            Log.d("DB_FILTER", "Fallback 2: Searching department only");
            String deptQuery = "SELECT * FROM students WHERE (departmentId = ? OR departmentName = ? OR departmentId LIKE ? OR departmentName LIKE ?) ORDER BY rollNo ASC";
            cursor = db.rawQuery(deptQuery, new String[]{deptId, deptId, "%" + deptId + "%", "%" + deptId + "%"});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Student s = new Student();
                    s.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
                    s.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                    s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                    s.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
                    s.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                    s.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                    s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                    s.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
                    s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                    list.add(s);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        // Final Fallback: If still empty, just return all students to avoid "No students found"
        if (list.isEmpty()) {
            Log.d("DB_FILTER", "Final Fallback: Returning all students");
            return getAllStudents();
        }
        
        Log.d("DB_FILTER", "Total students returned: " + list.size());
        return list;
    }

    public List<Attendance> getAttendanceByTimetable(String date, String timetableId, String subjectId, String deptId, String section, String semester) {
        List<Attendance> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Log.d("ATT_DASH", "getAttendanceByTimetable: date=" + date + ", tId=" + timetableId + ", subId=" + subjectId);
        
        Map<String, Attendance> existingMap = new HashMap<>();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance WHERE date = ? AND (timetableId = ? OR subjectId = ?)",
                new String[]{date, timetableId, subjectId});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Attendance a = parseAttendanceFromCursor(cursor);
                String stId = a.getStudentId();
                if (stId != null && !stId.isEmpty()) {
                    existingMap.put(stId, a);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.d("ATT_DASH", "Existing records found: " + existingMap.size());

        Map<String, String> approvedLeavesMap = new HashMap<>();
        Cursor cLeaves = db.rawQuery("SELECT studentId, studentName FROM leaves WHERE (status LIKE '%APPROV%' OR status = 'LEAVE') AND ? BETWEEN startDate AND endDate", new String[]{date});
        if (cLeaves != null && cLeaves.moveToFirst()) {
            do {
                String stId = cLeaves.getString(0);
                String stName = cLeaves.getString(1);
                if (stId != null) approvedLeavesMap.put(stId, stName);
            } while (cLeaves.moveToNext());
            cLeaves.close();
        }

        List<Student> filteredStudents = getStudentsByFilter(deptId, section, semester);
        Log.d("ATT_DASH", "Students for class: " + filteredStudents.size());
        
        // Get real subject name from subjects table
        String realSubjectName = "Subject " + subjectId;
        Cursor subCursor = db.rawQuery("SELECT name FROM subjects WHERE subjectId = ? OR code = ?", new String[]{subjectId, subjectId});
        if (subCursor != null && subCursor.moveToFirst()) {
            realSubjectName = subCursor.getString(0);
            subCursor.close();
        } else if (subCursor != null) {
            subCursor.close();
        }

        for (Student s : filteredStudents) {
            String stId = s.getStudentId();
            
            if (existingMap.containsKey(stId)) {
                list.add(existingMap.get(stId));
            } else {
                String tId = (timetableId != null && !timetableId.isEmpty()) ? timetableId : subjectId;
                String attId = date + "_" + stId + "_" + tId;
                String defaultStatus = Constants.STATUS_ABSENT; // Professional default is ABSENT if not marked
                if (stId != null && approvedLeavesMap.containsKey(stId)) {
                    defaultStatus = Constants.STATUS_LEAVE;
                }
                Attendance a = new Attendance(attId, date, stId, s.getUid(), s.getName(), s.getRollNo(),
                        s.getDepartmentId(), s.getDepartmentName(), s.getSection(), s.getSemester(),
                        subjectId, realSubjectName, defaultStatus, "system");
                a.setTimetableId(timetableId);
                list.add(a);
            }
        }
        Log.d("ATT_DASH", "Total list returned: " + list.size());
        return list;
    }

    public List<Attendance> getAttendanceByDateAndSubject(String date, String subjectId, String deptId, String section, String semester) {
        List<Attendance> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Map<String, Attendance> existingMap = new HashMap<>();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance WHERE date = ? AND subjectId = ?",
                new String[]{date, subjectId});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Attendance a = new Attendance();
                a.setAttendanceId(cursor.getString(cursor.getColumnIndexOrThrow("attendanceId")));
                a.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                a.setDay(cursor.getString(cursor.getColumnIndexOrThrow("day")));
                a.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentId")));
                a.setStudentUid(cursor.getString(cursor.getColumnIndexOrThrow("studentUid")));
                a.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("studentName")));
                a.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow("rollNo")));
                a.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                a.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                a.setSection(cursor.getString(cursor.getColumnIndexOrThrow("section")));
                a.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                a.setSubjectId(cursor.getString(cursor.getColumnIndexOrThrow("subjectId")));
                a.setSubjectName(cursor.getString(cursor.getColumnIndexOrThrow("subjectName")));
                a.setTeacherId(cursor.getString(cursor.getColumnIndexOrThrow("teacherId")));
                a.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacherName")));
                a.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                a.setMarkedByUid(cursor.getString(cursor.getColumnIndexOrThrow("markedByUid")));
                a.setTimetableId(cursor.getString(cursor.getColumnIndexOrThrow("timetableId")));
                a.setLectureNumber(cursor.getInt(cursor.getColumnIndexOrThrow("lectureNumber")));
                a.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
                a.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
                a.setLectureTime(cursor.getString(cursor.getColumnIndexOrThrow("lectureTime")));
                
                String stId = a.getStudentId();
                String stUid = a.getStudentUid();
                if (stId != null && !stId.isEmpty()) {
                    existingMap.put(stId, a);
                }
                if (stUid != null && !stUid.isEmpty()) {
                    existingMap.put(stUid, a);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Map<String, String> approvedLeavesMap = new HashMap<>();
        Cursor cLeaves = db.rawQuery("SELECT studentId, studentName FROM leaves WHERE (status LIKE '%APPROV%' OR status = 'LEAVE') AND ? BETWEEN startDate AND endDate", new String[]{date});
        if (cLeaves != null && cLeaves.moveToFirst()) {
            do {
                String stId = cLeaves.getString(0);
                String stName = cLeaves.getString(1);
                if (stId != null) approvedLeavesMap.put(stId, stName);
            } while (cLeaves.moveToNext());
            cLeaves.close();
        }

        List<Student> filteredStudents = getStudentsByFilter(deptId, section, semester);
        for (Student s : filteredStudents) {
            String stId = s.getStudentId();
            String stUid = s.getUid();
            
            if (existingMap.containsKey(stId)) {
                list.add(existingMap.get(stId));
            } else if (stUid != null && existingMap.containsKey(stUid)) {
                list.add(existingMap.get(stUid));
            } else {
                String attId = date + "_" + stId + "_" + subjectId;
                String defaultStatus = Constants.STATUS_PRESENT;
                if ((stId != null && approvedLeavesMap.containsKey(stId)) || (stUid != null && approvedLeavesMap.containsKey(stUid))) {
                    defaultStatus = Constants.STATUS_LEAVE;
                }
                Attendance a = new Attendance(attId, date, stId, s.getUid(), s.getName(), s.getRollNo(),
                        s.getDepartmentId(), s.getDepartmentName(), s.getSection(), s.getSemester(),
                        subjectId, "Course Subject", defaultStatus, "system");
                list.add(a);
            }
        }
        return list;
    }

    public List<Attendance> getAttendanceByStudent(String studentUid) {
        List<Attendance> list = new ArrayList<>();
        if (studentUid == null || studentUid.trim().isEmpty()) return list;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM attendance WHERE studentUid = ? OR studentId = ? OR markedByUid = ? OR attendanceId LIKE ? ORDER BY timestamp DESC, date DESC",
                new String[]{studentUid, studentUid, studentUid, "%" + studentUid + "%"});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Attendance a = parseAttendanceFromCursor(cursor);
                list.add(a);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Attendance> getAttendanceForTeacher(String date, String teacherUid, String deptId) {
        List<Attendance> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor;
        if (teacherUid != null && !teacherUid.trim().isEmpty()) {
            String query = "SELECT * FROM attendance WHERE (date = ? OR date LIKE ?) AND (markedByUid = ? OR teacherId = ? OR studentUid = ? OR studentId = ?) ORDER BY timestamp DESC";
            cursor = db.rawQuery(query, new String[]{date, "%" + date + "%", teacherUid, teacherUid, teacherUid, teacherUid});
        } else {
            String query = "SELECT * FROM attendance WHERE date = ? OR date LIKE ? ORDER BY timestamp DESC";
            cursor = db.rawQuery(query, new String[]{date, "%" + date + "%"});
        }
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(parseAttendanceFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- Leave Methods ---
    public void insertLeave(LeaveRequest request) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String id = request.getLeaveId() != null && !request.getLeaveId().isEmpty() ?
                request.getLeaveId() : "leave_" + System.currentTimeMillis();
        request.setLeaveId(id);

        long timestamp = request.getAppliedOn() > 0 ? request.getAppliedOn() : System.currentTimeMillis();

        cv.put("leaveId", id);
        cv.put("studentId", request.getStudentId());
        cv.put("studentUid", request.getStudentUid() != null ? request.getStudentUid() : request.getStudentId());
        cv.put("studentName", request.getStudentName());
        cv.put("rollNo", request.getRollNo());
        cv.put("departmentId", request.getDepartmentId());
        cv.put("departmentName", request.getDepartmentName());
        cv.put("semester", request.getSemester());
        cv.put("section", request.getSection());
        cv.put("reason", request.getReason());
        cv.put("startDate", request.getStartDate());
        cv.put("startTime", request.getStartTime());
        cv.put("endDate", request.getEndDate());
        cv.put("endTime", request.getEndTime());
        cv.put("status", request.getStatus() != null ? request.getStatus() : "Pending");
        cv.put("remarks", request.getRemarks());
        cv.put("appliedOn", timestamp);
        cv.put("submittedAt", request.getSubmittedAt() > 0 ? request.getSubmittedAt() : timestamp);
        cv.put("teacherUid", request.getTeacherUid());
        cv.put("teacherName", request.getTeacherName());
        db.insertWithOnConflict("leaves", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateLeaveStatus(String leaveId, String status, String remarks) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        cv.put("remarks", remarks);
        db.update("leaves", cv, "leaveId = ?", new String[]{leaveId});
    }

    public List<LeaveRequest> getLeavesByStudent(String studentId) {
        List<LeaveRequest> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM leaves WHERE studentId = ? OR studentUid = ? ORDER BY appliedOn DESC",
                new String[]{studentId, studentId});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(parseLeaveFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<LeaveRequest> getAllPendingLeaves() {
        List<LeaveRequest> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM leaves WHERE status = 'PENDING' OR status = 'Pending' ORDER BY appliedOn DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(parseLeaveFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private LeaveRequest parseLeaveFromCursor(Cursor cursor) {
        LeaveRequest l = new LeaveRequest();
        int idxId = cursor.getColumnIndex("leaveId");
        int idxStId = cursor.getColumnIndex("studentId");
        int idxStUid = cursor.getColumnIndex("studentUid");
        int idxName = cursor.getColumnIndex("studentName");
        int idxRoll = cursor.getColumnIndex("rollNo");
        int idxDeptId = cursor.getColumnIndex("departmentId");
        int idxDeptName = cursor.getColumnIndex("departmentName");
        int idxSem = cursor.getColumnIndex("semester");
        int idxSec = cursor.getColumnIndex("section");
        int idxReason = cursor.getColumnIndex("reason");
        int idxStartDate = cursor.getColumnIndex("startDate");
        int idxStartTime = cursor.getColumnIndex("startTime");
        int idxEndDate = cursor.getColumnIndex("endDate");
        int idxEndTime = cursor.getColumnIndex("endTime");
        int idxStatus = cursor.getColumnIndex("status");
        int idxRemarks = cursor.getColumnIndex("remarks");
        int idxAppliedOn = cursor.getColumnIndex("appliedOn");
        int idxSubmittedAt = cursor.getColumnIndex("submittedAt");
        int idxTeacherUid = cursor.getColumnIndex("teacherUid");
        int idxTeacherName = cursor.getColumnIndex("teacherName");

        if (idxId != -1) l.setLeaveId(cursor.getString(idxId));
        if (idxStId != -1) l.setStudentId(cursor.getString(idxStId));
        if (idxStUid != -1) l.setStudentUid(cursor.getString(idxStUid));
        if (idxName != -1) l.setStudentName(cursor.getString(idxName));
        if (idxRoll != -1) l.setRollNo(cursor.getString(idxRoll));
        if (idxDeptId != -1) l.setDepartmentId(cursor.getString(idxDeptId));
        if (idxDeptName != -1) l.setDepartmentName(cursor.getString(idxDeptName));
        if (idxSem != -1) l.setSemester(cursor.getString(idxSem));
        if (idxSec != -1) l.setSection(cursor.getString(idxSec));
        if (idxReason != -1) l.setReason(cursor.getString(idxReason));
        if (idxStartDate != -1) l.setStartDate(cursor.getString(idxStartDate));
        if (idxStartTime != -1) l.setStartTime(cursor.getString(idxStartTime));
        if (idxEndDate != -1) l.setEndDate(cursor.getString(idxEndDate));
        if (idxEndTime != -1) l.setEndTime(cursor.getString(idxEndTime));
        if (idxStatus != -1) l.setStatus(cursor.getString(idxStatus));
        if (idxRemarks != -1) l.setRemarks(cursor.getString(idxRemarks));
        if (idxAppliedOn != -1) l.setAppliedOn(cursor.getLong(idxAppliedOn));
        if (idxSubmittedAt != -1) l.setSubmittedAt(cursor.getLong(idxSubmittedAt));
        if (idxTeacherUid != -1) l.setTeacherUid(cursor.getString(idxTeacherUid));
        if (idxTeacherName != -1) l.setTeacherName(cursor.getString(idxTeacherName));
        return l;
    }

    // --- Notification Methods ---
    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notifications ORDER BY timestamp DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Notice n = new Notice();
                n.setNoticeId(cursor.getString(cursor.getColumnIndexOrThrow("noticeId")));
                n.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                n.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
                n.setTargetRole(cursor.getString(cursor.getColumnIndexOrThrow("targetRole")));
                n.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                n.setSenderName(cursor.getString(cursor.getColumnIndexOrThrow("senderName")));
                n.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                list.add(n);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void insertNotice(Notice notice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String id = notice.getNoticeId() != null && !notice.getNoticeId().isEmpty() ?
                notice.getNoticeId() : "n_" + System.currentTimeMillis();
        notice.setNoticeId(id);

        cv.put("noticeId", id);
        cv.put("title", notice.getTitle());
        cv.put("message", notice.getMessage());
        cv.put("targetRole", notice.getTargetRole());
        cv.put("type", notice.getType());
        cv.put("senderName", notice.getSenderName());
        cv.put("timestamp", System.currentTimeMillis());
        db.insertWithOnConflict("notifications", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // --- Timetable Methods ---
    public List<TimetableItem> getTimetableForStudent(String departmentId, String division, String day) {
        List<TimetableItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query;
        List<String> args = new ArrayList<>();

        query = "SELECT * FROM timetable WHERE 1=1";
        if (departmentId != null && !departmentId.isEmpty()) {
            query += " AND (departmentId = ? OR departmentName = ? OR departmentName IS NULL OR departmentName = '')";
            args.add(departmentId);
            args.add(departmentId);
        }
        if (division != null && !division.isEmpty()) {
            query += " AND (division = ? OR division IS NULL OR division = '')";
            args.add(division);
        }
        if (day != null && !day.isEmpty()) {
            query += " AND (day LIKE ? OR day IS NULL)";
            args.add("%" + day + "%");
        }
        query += " ORDER BY lectureNumber ASC, startTime ASC";

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                TimetableItem item = new TimetableItem();
                item.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                item.setDay(cursor.getString(cursor.getColumnIndexOrThrow("day")));
                item.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
                item.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
                String sId = cursor.getString(cursor.getColumnIndexOrThrow("subjectId"));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow("subjectName"));
                String tId = cursor.getString(cursor.getColumnIndexOrThrow("teacherId"));
                String tName = cursor.getString(cursor.getColumnIndexOrThrow("teacherName"));
                
                // CHAIN OF TRUTH LOOKUP (Single Source of Truth)
                // Resolve actual assigned teacherId and teacherName from subjects table
                if (sId != null && !sId.isEmpty()) {
                    Cursor subCursor = db.rawQuery("SELECT name, teacherId, teacherName FROM subjects WHERE subjectId = ? LIMIT 1", new String[]{sId});
                    if (subCursor != null && subCursor.moveToFirst()) {
                        if (subCursor.getString(0) != null && !subCursor.getString(0).isEmpty()) sName = subCursor.getString(0);
                        if (subCursor.getString(1) != null && !subCursor.getString(1).isEmpty()) tId = subCursor.getString(1);
                        if (subCursor.getString(2) != null && !subCursor.getString(2).isEmpty()) tName = subCursor.getString(2);
                        subCursor.close();
                    } else if (subCursor != null) {
                        subCursor.close();
                    }
                }
                if ((tId == null || tId.isEmpty()) && sName != null && !sName.isEmpty()) {
                    Cursor subCursor = db.rawQuery("SELECT subjectId, teacherId, teacherName FROM subjects WHERE LOWER(name) = LOWER(?) LIMIT 1", new String[]{sName.trim()});
                    if (subCursor != null && subCursor.moveToFirst()) {
                        if (sId == null || sId.isEmpty()) sId = subCursor.getString(0);
                        if (subCursor.getString(1) != null && !subCursor.getString(1).isEmpty()) tId = subCursor.getString(1);
                        if (subCursor.getString(2) != null && !subCursor.getString(2).isEmpty()) tName = subCursor.getString(2);
                        subCursor.close();
                    } else if (subCursor != null) {
                        subCursor.close();
                    }
                }

                // If we have a teacherId, resolve latest name from teachers table
                if (tId != null && !tId.isEmpty()) {
                    Cursor tCursor = db.rawQuery("SELECT name FROM teachers WHERE uid = ? OR teacherId = ? LIMIT 1", new String[]{tId, tId});
                    if (tCursor != null && tCursor.moveToFirst()) {
                        tName = tCursor.getString(0);
                        tCursor.close();
                    } else if (tCursor != null) {
                        tCursor.close();
                    }
                }
                
                item.setSubjectId(sId);
                item.setSubjectName(sName);
                item.setTeacherId(tId);
                item.setTeacherName(tName != null ? tName : "TBD");
                item.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("roomNumber")));
                item.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                item.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                item.setDivision(cursor.getString(cursor.getColumnIndexOrThrow("division")));
                item.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                item.setLectureNumber(cursor.getInt(cursor.getColumnIndexOrThrow("lectureNumber")));
                item.setBreak(cursor.getInt(cursor.getColumnIndexOrThrow("isBreak")) == 1);
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void insertTimetableItem(TimetableItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", item.getId() != null ? item.getId() : "tt_" + System.currentTimeMillis());
        cv.put("day", item.getDay());
        cv.put("startTime", item.getStartTime());
        cv.put("endTime", item.getEndTime());
        cv.put("subjectId", item.getSubjectId());
        cv.put("subjectName", item.getSubjectName());
        cv.put("teacherId", item.getTeacherId());
        cv.put("teacherName", item.getTeacherName());
        cv.put("roomNumber", item.getRoomNumber());
        cv.put("departmentId", item.getDepartmentId());
        cv.put("departmentName", item.getDepartmentName());
        cv.put("division", item.getDivision());
        cv.put("semester", item.getSemester());
        cv.put("lectureNumber", item.getLectureNumber());
        cv.put("isBreak", item.isBreak() ? 1 : 0);
        db.insertWithOnConflict("timetable", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<TimetableItem> getTimetableForTeacher(String teacherUid, String teacherName, String day) {
        List<TimetableItem> list = new ArrayList<>();
        if ((teacherUid == null || teacherUid.isEmpty()) && (teacherName == null || teacherName.isEmpty())) return list;
        
        List<Subject> assignedSubjects = getAssignedSubjectsForTeacher(teacherUid, teacherName);
        java.util.Set<String> assignedSubjectNames = new java.util.HashSet<>();
        java.util.Set<String> assignedSubjectIds = new java.util.HashSet<>();
        if (assignedSubjects != null) {
            for (Subject s : assignedSubjects) {
                if (s.getSubjectId() != null && !s.getSubjectId().isEmpty()) assignedSubjectIds.add(s.getSubjectId());
                if (s.getName() != null && !s.getName().isEmpty()) assignedSubjectNames.add(s.getName().trim().toLowerCase());
            }
        }

        SQLiteDatabase db = getReadableDatabase();
        String cleanUid = teacherUid != null ? teacherUid.trim() : "";
        String cleanName = teacherName != null ? teacherName.trim() : "";
        String query = "SELECT * FROM timetable WHERE (teacherId = ? OR (LOWER(teacherName) = LOWER(?) AND teacherName != ''))";
        List<String> args = new ArrayList<>();
        args.add(cleanUid);
        args.add(cleanName);

        if (day != null && !day.isEmpty()) {
            query += " AND (day LIKE ? OR day IS NULL)";
            args.add("%" + day + "%");
        }
        query += " ORDER BY lectureNumber ASC, startTime ASC";

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                TimetableItem item = new TimetableItem();
                item.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                item.setDay(cursor.getString(cursor.getColumnIndexOrThrow("day")));
                item.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
                item.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
                String sId = cursor.getString(cursor.getColumnIndexOrThrow("subjectId"));
                String sName = cursor.getString(cursor.getColumnIndexOrThrow("subjectName"));
                String tId = cursor.getString(cursor.getColumnIndexOrThrow("teacherId"));
                String tName = cursor.getString(cursor.getColumnIndexOrThrow("teacherName"));

                // Single Source of Truth Lookup from subjects table
                if (sId != null && !sId.isEmpty()) {
                    Cursor subCursor = db.rawQuery("SELECT name, teacherId, teacherName FROM subjects WHERE subjectId = ? LIMIT 1", new String[]{sId});
                    if (subCursor != null && subCursor.moveToFirst()) {
                        if (subCursor.getString(0) != null && !subCursor.getString(0).isEmpty()) sName = subCursor.getString(0);
                        if (subCursor.getString(1) != null && !subCursor.getString(1).isEmpty()) tId = subCursor.getString(1);
                        if (subCursor.getString(2) != null && !subCursor.getString(2).isEmpty()) tName = subCursor.getString(2);
                        subCursor.close();
                    } else if (subCursor != null) {
                        subCursor.close();
                    }
                }
                if ((tId == null || tId.isEmpty()) && sName != null && !sName.isEmpty()) {
                    Cursor subCursor = db.rawQuery("SELECT subjectId, teacherId, teacherName FROM subjects WHERE LOWER(name) = LOWER(?) LIMIT 1", new String[]{sName.trim()});
                    if (subCursor != null && subCursor.moveToFirst()) {
                        if (sId == null || sId.isEmpty()) sId = subCursor.getString(0);
                        if (subCursor.getString(1) != null && !subCursor.getString(1).isEmpty()) tId = subCursor.getString(1);
                        if (subCursor.getString(2) != null && !subCursor.getString(2).isEmpty()) tName = subCursor.getString(2);
                        subCursor.close();
                    } else if (subCursor != null) {
                        subCursor.close();
                    }
                }

                // Verify the resolved teacher matches cleanUid or cleanName
                boolean teacherMatch = (tId != null && tId.equals(cleanUid)) ||
                                       (tName != null && tName.equalsIgnoreCase(cleanName)) ||
                                       (cleanUid != null && cleanUid.equals(tId));

                if (teacherMatch || assignedSubjectIds.contains(sId) || (sName != null && assignedSubjectNames.contains(sName.trim().toLowerCase()))) {
                    item.setSubjectId(sId);
                    item.setSubjectName(sName);
                    item.setTeacherId(cleanUid != null && !cleanUid.isEmpty() ? cleanUid : tId);
                    item.setTeacherName(cleanName != null && !cleanName.isEmpty() ? cleanName : tName);
                    item.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("roomNumber")));
                    item.setDepartmentId(cursor.getString(cursor.getColumnIndexOrThrow("departmentId")));
                    item.setDepartmentName(cursor.getString(cursor.getColumnIndexOrThrow("departmentName")));
                    item.setDivision(cursor.getString(cursor.getColumnIndexOrThrow("division")));
                    item.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
                    item.setLectureNumber(cursor.getInt(cursor.getColumnIndexOrThrow("lectureNumber")));
                    item.setBreak(cursor.getInt(cursor.getColumnIndexOrThrow("isBreak")) == 1);
                    list.add(item);
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }

        // Fallback: match full day timetable against teacher's assigned subjects
        if (list.isEmpty()) {
            List<TimetableItem> allDayItems = getTimetableForStudent("", "", day);
            for (TimetableItem item : allDayItems) {
                if (item.isBreak()) continue;
                boolean matches = false;
                if (item.getTeacherId() != null && item.getTeacherId().equals(cleanUid)) matches = true;
                else if (item.getTeacherName() != null && item.getTeacherName().equalsIgnoreCase(cleanName)) matches = true;
                else if (item.getSubjectId() != null && assignedSubjectIds.contains(item.getSubjectId())) matches = true;
                else if (item.getSubjectName() != null && assignedSubjectNames.contains(item.getSubjectName().trim().toLowerCase())) matches = true;

                if (matches) {
                    if (cleanName != null && !cleanName.isEmpty()) item.setTeacherName(cleanName);
                    if (cleanUid != null && !cleanUid.isEmpty()) item.setTeacherId(cleanUid);
                    list.add(item);
                }
            }
        }
        return list;
    }

    // --- Student Notifications Methods ---
    public List<NotificationItem> getNotificationsForStudent(String studentUid) {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM student_notifications WHERE studentUid = ? OR studentUid IS NULL OR studentUid = '' ORDER BY timestamp DESC",
                new String[]{studentUid != null ? studentUid : ""});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                NotificationItem item = new NotificationItem();
                item.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                item.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("studentUid")));
                item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                item.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                item.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                item.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1);
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void insertNotification(NotificationItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", item.getId() != null ? item.getId() : "notif_" + System.currentTimeMillis());
        cv.put("studentUid", item.getStudentId()); // Using studentId field from model as studentUid in DB
        cv.put("title", item.getTitle());
        cv.put("message", item.getMessage());
        cv.put("type", item.getType());
        cv.put("timestamp", item.getTimestamp());
        cv.put("isRead", item.isRead() ? 1 : 0);
        db.insertWithOnConflict("student_notifications", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void markNotificationAsRead(String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isRead", 1);
        db.update("student_notifications", cv, "id = ?", new String[]{id});
    }

    public void markAllNotificationsAsRead(String studentUid) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isRead", 1);
        db.update("student_notifications", cv, "studentUid = ? OR studentUid IS NULL", new String[]{studentUid != null ? studentUid : ""});
    }
}
