package com.example.attendance_system.firebase;

import android.util.Log;

import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static FirebaseService instance;

    private FirebaseService() {
        // Initialization if needed
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    // --- Firebase Authentication Account Creation ---

    public void createFirebaseUserAccount(String email, String password, String name, String role, FirestoreCallback<String> callback) {
        FirebaseAuth auth = getAuth();
        if (auth == null || email == null || email.trim().isEmpty()) {
            if (callback != null) callback.onError("Firebase Authentication is not initialized.");
            return;
        }

        String cleanEmail = email.trim();
        String cleanPassword = password != null ? password.trim() : "";

        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        String uid = authResult.getUser().getUid();
                        User u = new User(uid, cleanEmail, name, role);
                        saveUserToFirestore(u);
                        Log.d(TAG, "Firebase Auth user created: " + cleanEmail + " (UID: " + uid + ")");
                        if (callback != null) callback.onSuccess(uid);
                    } else {
                        if (callback != null) callback.onError("Failed to get user after creation.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "FirebaseAuth createUser failed for " + cleanEmail + ": " + e.getMessage(), e);
                    if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        // User already exists in Firebase Auth, attempt signing in to verify password and update profile
                        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                                .addOnSuccessListener(authResult -> {
                                    if (authResult.getUser() != null) {
                                        String uid = authResult.getUser().getUid();
                                        User u = new User(uid, cleanEmail, name, role);
                                        saveUserToFirestore(u);
                                        Log.d(TAG, "Existing Firebase Auth user re-authenticated: " + cleanEmail + " (UID: " + uid + ")");
                                        if (callback != null) callback.onSuccess(uid);
                                    } else {
                                        if (callback != null) callback.onError("An account with this email already exists.");
                                    }
                                })
                                .addOnFailureListener(signInError -> {
                                    if (callback != null) callback.onError("An account with this email already exists, but the password provided is incorrect.");
                                });
                    } else {
                        if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Account creation failed.");
                    }
                });
    }

    // --- Firebase Authentication & Users ---

    public void getUserFromFirestore(String uid, FirestoreCallback<User> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firestore is not initialized.");
            return;
        }

        db.collection(Constants.KEY_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Failed to parse user data.");
                        }
                    } else {
                        callback.onError("User document does not exist in Firestore.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    public void loginUserFromFirestore(String email, String password, String selectedRole, FirestoreCallback<User> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }

        db.collection(Constants.KEY_USERS)
                .whereEqualTo("email", email.trim())
                .whereEqualTo("role", selectedRole)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            callback.onSuccess(u);
                            return;
                        }
                    }
                    callback.onError("No authenticated " + selectedRole + " found in cloud for " + email);
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    public void saveUserToFirestore(User user) {
        FirebaseFirestore db = getFirestore();
        if (db == null || user == null || user.getUid() == null) return;
        try {
            db.collection(Constants.KEY_USERS).document(user.getUid()).set(user, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved to Firestore: " + user.getEmail()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving user to Firestore: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Exception saving user to Firestore: " + e.getMessage());
        }
    }

    // --- Attendance Operations ---

    public void fetchSubjectsFromFirestore(String departmentId, String semester, FirestoreCallback<List<com.example.attendance_system.models.Subject>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        
        db.collection("subjects")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    List<com.example.attendance_system.models.Subject> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            com.example.attendance_system.models.Subject s = doc.toObject(com.example.attendance_system.models.Subject.class);
                            if (s != null) {
                                boolean deptMatch = departmentId == null || departmentId.isEmpty() || 
                                                    (s.getDepartmentId() != null && s.getDepartmentId().equalsIgnoreCase(departmentId)) || 
                                                    (s.getDepartmentName() != null && s.getDepartmentName().equalsIgnoreCase(departmentId));
                                boolean semMatch = semester == null || semester.isEmpty() || (s.getSemester() != null && s.getSemester().equalsIgnoreCase(semester));
                                
                                if (deptMatch && semMatch) {
                                    list.add(s);
                                }
                            }
                        }
                    }
                    callback.onSuccess(list);
                });
    }

    public void saveSubjectToFirestore(com.example.attendance_system.models.Subject subject, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || subject == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        String id = subject.getSubjectId();
        if (id == null || id.isEmpty()) {
            id = "sub_" + System.currentTimeMillis();
            subject.setSubjectId(id);
        }
        db.collection("subjects").document(id).set(subject, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void fetchTimetableFromFirestore(String departmentId, String divisionId, String day, FirestoreCallback<List<com.example.attendance_system.models.TimetableItem>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        com.google.firebase.firestore.Query query = db.collection("timetable")
                .whereEqualTo("day", day);
        
        if (departmentId != null && !departmentId.isEmpty()) {
            query = query.whereEqualTo("departmentId", departmentId);
        }
        if (divisionId != null && !divisionId.isEmpty()) {
            query = query.whereEqualTo("division", divisionId);
        }

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onError(e.getMessage());
                return;
            }
            List<com.example.attendance_system.models.TimetableItem> list = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    com.example.attendance_system.models.TimetableItem item = doc.toObject(com.example.attendance_system.models.TimetableItem.class);
                    if (item != null) list.add(item);
                }
            }
            list.sort((o1, o2) -> Integer.compare(o1.getLectureNumber(), o2.getLectureNumber()));
            callback.onSuccess(list);
        });
    }

    public void fetchWeeklyTimetableFromFirestore(String departmentId, String divisionId, FirestoreCallback<List<com.example.attendance_system.models.TimetableItem>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        com.google.firebase.firestore.Query query = db.collection("timetable");
        
        if (departmentId != null && !departmentId.isEmpty()) {
            query = query.whereEqualTo("departmentId", departmentId);
        }
        if (divisionId != null && !divisionId.isEmpty()) {
            query = query.whereEqualTo("division", divisionId);
        }

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onError(e.getMessage());
                return;
            }
            List<com.example.attendance_system.models.TimetableItem> list = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    com.example.attendance_system.models.TimetableItem item = doc.toObject(com.example.attendance_system.models.TimetableItem.class);
                    if (item != null) list.add(item);
                }
            }
            callback.onSuccess(list);
        });
    }

    public void fetchTeacherTimetableFromFirestore(String teacherUid, String day, FirestoreCallback<List<com.example.attendance_system.models.TimetableItem>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }

        // Step 1: Find subjects assigned to teacherUid
        db.collection("subjects").whereEqualTo("teacherId", teacherUid).get().addOnSuccessListener(subjectSnaps -> {
            java.util.Set<String> assignedSubjectIds = new java.util.HashSet<>();
            java.util.Set<String> assignedSubjectNames = new java.util.HashSet<>();
            if (subjectSnaps != null) {
                for (DocumentSnapshot doc : subjectSnaps.getDocuments()) {
                    String sId = doc.getString("subjectId");
                    String sName = doc.getString("name");
                    if (sId != null) assignedSubjectIds.add(sId);
                    if (sName != null) assignedSubjectNames.add(sName.trim().toLowerCase());
                }
            }

            com.google.firebase.firestore.Query query = db.collection("timetable");
            if (day != null && !day.isEmpty()) {
                query = query.whereEqualTo("day", day);
            }

            query.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    callback.onError(e.getMessage());
                    return;
                }
                List<com.example.attendance_system.models.TimetableItem> list = new ArrayList<>();
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        com.example.attendance_system.models.TimetableItem item = doc.toObject(com.example.attendance_system.models.TimetableItem.class);
                        if (item != null && !item.isBreak()) {
                            boolean matches = false;
                            if (teacherUid != null && teacherUid.equals(item.getTeacherId())) matches = true;
                            else if (item.getSubjectId() != null && assignedSubjectIds.contains(item.getSubjectId())) matches = true;
                            else if (item.getSubjectName() != null && assignedSubjectNames.contains(item.getSubjectName().trim().toLowerCase())) matches = true;

                            if (matches) {
                                if (teacherUid != null) item.setTeacherId(teacherUid);
                                list.add(item);
                            }
                        }
                    }
                }
                list.sort((o1, o2) -> Integer.compare(o1.getLectureNumber(), o2.getLectureNumber()));
                callback.onSuccess(list);
            });
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchNotificationsFromFirestore(String studentUid, FirestoreCallback<List<com.example.attendance_system.models.NotificationItem>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        db.collection("notifications")
                .whereEqualTo("studentUid", studentUid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.attendance_system.models.NotificationItem> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            com.example.attendance_system.models.NotificationItem item = doc.toObject(com.example.attendance_system.models.NotificationItem.class);
                            if (item != null) list.add(item);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void markNotificationAsRead(String notificationId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || notificationId == null) return;
        db.collection("notifications").document(notificationId).update("isRead", true)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void markAllNotificationsAsRead(String studentId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || studentId == null) return;
        db.collection("notifications")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            batch.update(doc.getReference(), "isRead", true);
                        }
                        batch.commit().addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(null);
                        }).addOnFailureListener(e -> {
                            if (callback != null) callback.onError(e.getMessage());
                        });
                    }
                });
    }

    public void syncAttendanceBatchToFirestore(List<Attendance> list, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || list == null || list.isEmpty()) {
            if (callback != null) callback.onSuccess(null);
            return;
        }
        try {
            WriteBatch batch = db.batch();
            for (Attendance a : list) {
                String id = a.getAttendanceId();
                if (id == null || id.isEmpty()) {
                    id = a.getDate() + "_" + a.getStudentId() + "_" + a.getSubjectId();
                    a.setAttendanceId(id);
                }
                String docId = id.replace("/", "_");
                batch.set(db.collection("attendance").document(docId), a, SetOptions.merge());
            }
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Attendance batch saved successfully to Firestore");
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving attendance batch to Firestore: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception saving attendance batch: " + e.getMessage());
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void fetchAttendanceFromFirestore(android.content.Context context, String date, String subjectId, String deptId, String section, String semester, FirestoreCallback<List<Attendance>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        
        com.google.firebase.firestore.Query query = db.collection("attendance")
                .whereEqualTo("date", date)
                .whereEqualTo("subjectId", subjectId);
        
        if (deptId != null && !deptId.isEmpty()) {
            query = query.whereEqualTo("departmentId", deptId);
        }
        if (section != null && !section.isEmpty()) {
            query = query.whereEqualTo("section", section);
        }
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Attendance> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Attendance a = doc.toObject(Attendance.class);
                            if (a != null) {
                                list.add(a);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchAttendanceForTeacherFromFirestore(String date, String teacherUid, String deptId, FirestoreCallback<List<Attendance>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        
        com.google.firebase.firestore.Query query = db.collection("attendance")
                .whereEqualTo("date", date);
        
        if (teacherUid != null && !teacherUid.isEmpty()) {
            // Priority: items marked by this teacher
            // However, to show all relevant students for a teacher's department:
            if (deptId != null && !deptId.isEmpty()) {
                 query = query.whereEqualTo("departmentId", deptId);
            } else {
                 query = query.whereEqualTo("markedByUid", teacherUid);
            }
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                callback.onError(error.getMessage());
                return;
            }
            
            List<Attendance> list = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Attendance a = doc.toObject(Attendance.class);
                    if (a != null) list.add(a);
                }
            }
            callback.onSuccess(list);
        });
    }

    public void saveTimetableItemToFirestore(com.example.attendance_system.models.TimetableItem item, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || item == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        String id = item.getId();
        if (id == null || id.isEmpty()) {
            id = "tt_" + System.currentTimeMillis();
            item.setId(id);
        }
        db.collection("timetable").document(id).set(item, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void fetchStudentAttendanceFromFirestore(String studentUid, FirestoreCallback<List<Attendance>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || studentUid == null || studentUid.isEmpty()) {
            if (callback != null) callback.onError("Invalid student identifier");
            return;
        }

        // Real-time listener for student attendance to support automatic student dashboard updates
        db.collection("attendance")
                .whereEqualTo("studentUid", studentUid)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    List<Attendance> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Attendance a = doc.toObject(Attendance.class);
                            if (a != null) {
                                list.add(a);
                            }
                        }
                    }
                    callback.onSuccess(list);
                });
    }

    // --- Students Operations ---

    public void saveStudentToFirestore(Student student, boolean createAccount, FirestoreCallback<Void> callback) {
        saveStudentToFirestore(student, "student123", createAccount, callback);
    }

    public void saveStudentToFirestore(Student student, String password, boolean createAccount, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || student == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            student.setStudentId("st_" + System.currentTimeMillis());
        }
        db.collection("students").document(student.getStudentId()).set(student, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Student saved to Firestore: " + student.getName());
                    if (createAccount) {
                        createFirebaseUserAccount(student.getEmail(), password != null && !password.isEmpty() ? password : "student123", student.getName(), Constants.ROLE_STUDENT, new FirestoreCallback<String>() {
                            @Override
                            public void onSuccess(String realUid) {
                                // Update student document with real UID
                                db.collection("students").document(student.getStudentId()).update("uid", realUid);
                                if (callback != null) callback.onSuccess(null);
                            }

                            @Override
                            public void onError(String error) {
                                if (callback != null) callback.onError(error);
                            }
                        });
                    } else {
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving student to Firestore: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void fetchStudentsFromFirestore(FirestoreCallback<List<Student>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        db.collection("students").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Student> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Student s = doc.toObject(Student.class);
                            if (s != null) list.add(s);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getStudentByUid(String uid, FirestoreCallback<Student> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        db.collection("students").whereEqualTo("uid", uid).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Student s = queryDocumentSnapshots.getDocuments().get(0).toObject(Student.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onError("Student not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteStudentFromFirestore(String studentId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) return;
        db.collection("students").document(studentId).delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    // --- Teachers Operations ---

    public void saveTeacherToFirestore(Teacher teacher, boolean createAccount, FirestoreCallback<Void> callback) {
        saveTeacherToFirestore(teacher, "teacher123", createAccount, callback);
    }

    public void saveTeacherToFirestore(Teacher teacher, String password, boolean createAccount, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || teacher == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        if (teacher.getTeacherId() == null || teacher.getTeacherId().trim().isEmpty()) {
            teacher.setTeacherId("t_" + System.currentTimeMillis());
        }
        db.collection("teachers").document(teacher.getTeacherId()).set(teacher, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Teacher saved to Firestore: " + teacher.getName());
                    if (createAccount) {
                        createFirebaseUserAccount(teacher.getEmail(), password != null && !password.isEmpty() ? password : "teacher123", teacher.getName(), Constants.ROLE_TEACHER, null);
                    }
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving teacher to Firestore: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void fetchTeachersFromFirestore(FirestoreCallback<List<Teacher>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        db.collection("teachers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Teacher> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Teacher t = doc.toObject(Teacher.class);
                            if (t != null) list.add(t);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteTeacherFromFirestore(String teacherId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) return;
        db.collection("teachers").document(teacherId).delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    // --- Departments Operations ---

    public void saveDepartmentToFirestore(com.example.attendance_system.models.Department dept, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || dept == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        db.collection("departments").document(dept.getDepartmentId()).set(dept, SetOptions.merge())
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
    }

    public void fetchDepartmentsFromFirestore(FirestoreCallback<List<com.example.attendance_system.models.Department>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            callback.onError("Firebase unavailable");
            return;
        }
        db.collection("departments").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.attendance_system.models.Department> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            com.example.attendance_system.models.Department d = doc.toObject(com.example.attendance_system.models.Department.class);
                            if (d != null) list.add(d);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // --- Leaves Operations ---

    public void saveLeaveToFirestore(LeaveRequest leave, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || leave == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return;
        }
        if (leave.getLeaveId() == null || leave.getLeaveId().trim().isEmpty()) {
            leave.setLeaveId("leave_" + System.currentTimeMillis());
        }
        db.collection("leaves").document(leave.getLeaveId()).set(leave, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Leave saved to Firestore: " + leave.getLeaveId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving leave to Firestore: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void updateLeaveStatusInFirestore(String leaveId, String status, String remarks, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null || leaveId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("remarks", remarks);
        db.collection("leaves").document(leaveId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Leave status updated in Firestore: " + leaveId);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating leave status in Firestore: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public com.google.firebase.firestore.ListenerRegistration listenLeavesFromFirestore(FirestoreCallback<List<LeaveRequest>> callback) {
        FirebaseFirestore db = getFirestore();
        if (db == null) {
            if (callback != null) callback.onError("Firebase unavailable");
            return null;
        }
        return db.collection("leaves")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        if (callback != null) callback.onError(e.getMessage());
                        return;
                    }
                    List<LeaveRequest> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            LeaveRequest l = doc.toObject(LeaveRequest.class);
                            if (l != null) list.add(l);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                });
    }
}
