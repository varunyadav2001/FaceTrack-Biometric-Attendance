package com.example.attendance_system.fragments.teacher;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentQrAttendanceGenBinding;
import com.example.attendance_system.databinding.ItemAttendanceBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.QrSession;
import com.example.attendance_system.models.Subject;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.TimetableItem;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.QRCodeUtils;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QRAttendanceGenFragment extends Fragment {

    private static final String TAG = "QRAttendanceGen";
    private static final long SESSION_DURATION = 120000; // 120 seconds

    private FragmentQrAttendanceGenBinding binding;
    private final List<TimetableItem> assignedSubjectsList = new ArrayList<>();
    private TimetableItem selectedSubjectItem;

    private CountDownTimer countDownTimer;
    private QrSession currentSession;

    private LiveStudentAttendanceAdapter liveAdapter;
    private final List<Attendance> livePresentStudents = new ArrayList<>();
    private ListenerRegistration liveScanListener;

    private String teacherUid = "";
    private String teacherName = "Faculty";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQrAttendanceGenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        teacherUid = SharedPrefManager.getInstance(requireContext()).getUserUid();
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) {
            teacherName = currentUser.getName();
        }

        binding.tvQRDate.setText(DateTimeUtils.getCurrentDateDisplay());
        binding.tvQRTime.setText(DateTimeUtils.getCurrentTime());

        // Setup live attendance RecyclerView
        binding.rvLivePresentStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        liveAdapter = new LiveStudentAttendanceAdapter(livePresentStudents);
        binding.rvLivePresentStudents.setAdapter(liveAdapter);

        // Subject Spinner Listener
        binding.spinnerQRSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < assignedSubjectsList.size()) {
                    selectedSubjectItem = assignedSubjectsList.get(position);
                    updateSessionDetailsUI(selectedSubjectItem);
                    binding.btnGenerateQR.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnGenerateQR.setOnClickListener(v -> generateDynamicSession());
        binding.btnRefreshQR.setOnClickListener(v -> generateDynamicSession());
        binding.btnRefreshSubjects.setOnClickListener(v -> loadTeacherSubjects());

        // Start by disabling button until subjects load
        binding.btnGenerateQR.setEnabled(false);

        loadTeacherSubjects();
    }

    /**
     * Comprehensive multi-source subject loader for the logged-in teacher.
     */
    private void loadTeacherSubjects() {
        if (getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

        // Map to deduplicate subjects while preserving richest timetable metadata
        Map<String, TimetableItem> consolidatedMap = new HashMap<>();

        // 1. Check local SQLite assigned subjects
        List<Subject> localSubjects = db.getAssignedSubjectsForTeacher(teacherUid, teacherName);
        if (localSubjects != null) {
            for (Subject s : localSubjects) {
                String key = (s.getSubjectId() != null && !s.getSubjectId().isEmpty()) ? s.getSubjectId() : s.getName();
                if (key != null && !key.isEmpty()) {
                    TimetableItem item = new TimetableItem();
                    item.setSubjectId(s.getSubjectId());
                    item.setSubjectName(s.getName());
                    item.setDepartmentId(s.getDepartmentId() != null ? s.getDepartmentId() : "d1");
                    item.setDepartmentName(s.getDepartmentName() != null ? s.getDepartmentName() : "MCA");
                    item.setDivision("A");
                    item.setSemester(s.getSemester() != null ? s.getSemester() : "1");
                    item.setLectureNumber(1);
                    item.setRoomNumber("101");
                    item.setTeacherId(teacherUid);
                    item.setTeacherName(teacherName);
                    consolidatedMap.put(key, item);
                }
            }
        }

        // 2. Check local SQLite timetable for this teacher
        List<TimetableItem> localTimetable = db.getTimetableForTeacher(teacherUid, teacherName, null);
        if (localTimetable != null) {
            for (TimetableItem t : localTimetable) {
                if (t != null && !t.isBreak()) {
                    String key = (t.getSubjectId() != null && !t.getSubjectId().isEmpty()) ? t.getSubjectId() : t.getSubjectName();
                    if (key != null && !key.isEmpty()) {
                        if (t.getTeacherId() == null || t.getTeacherId().isEmpty()) t.setTeacherId(teacherUid);
                        if (t.getTeacherName() == null || t.getTeacherName().isEmpty()) t.setTeacherName(teacherName);
                        consolidatedMap.put(key, t);
                    }
                }
            }
        }

        // 3. Check teacher profile in SQLite for assignedSubjects array
        Teacher localTeacher = db.getTeacherByUid(teacherUid);
        if (localTeacher != null) {
            if (localTeacher.getName() != null && !localTeacher.getName().isEmpty()) {
                teacherName = localTeacher.getName();
            }
            if (localTeacher.getAssignedSubjects() != null) {
                for (String subRef : localTeacher.getAssignedSubjects()) {
                    if (subRef != null && !subRef.trim().isEmpty() && !consolidatedMap.containsKey(subRef)) {
                        TimetableItem item = new TimetableItem();
                        item.setSubjectId(subRef);
                        item.setSubjectName(subRef);
                        item.setDepartmentId(localTeacher.getDepartmentId() != null ? localTeacher.getDepartmentId() : "d1");
                        item.setDepartmentName(localTeacher.getDepartmentName() != null ? localTeacher.getDepartmentName() : "MCA");
                        item.setDivision("A");
                        item.setSemester("1");
                        item.setLectureNumber(1);
                        item.setRoomNumber("101");
                        item.setTeacherId(teacherUid);
                        item.setTeacherName(teacherName);
                        consolidatedMap.put(subRef, item);
                    }
                }
            }
        }

        // Populate local results first
        if (!consolidatedMap.isEmpty()) {
            updateSpinnerData(new ArrayList<>(consolidatedMap.values()));
        }

        // 4. Remote Firestore Fetch & Merge
        FirebaseFirestore fdb = FirebaseService.getInstance().getFirestore();
        if (fdb != null && !teacherUid.isEmpty()) {
            // Fetch subjects assigned to teacher UID
            fdb.collection("subjects").whereEqualTo("teacherId", teacherUid).get().addOnSuccessListener(snaps -> {
                if (snaps != null && !snaps.isEmpty()) {
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Subject s = doc.toObject(Subject.class);
                        if (s != null) {
                            String key = s.getSubjectId() != null ? s.getSubjectId() : s.getName();
                            if (key != null && !key.isEmpty()) {
                                TimetableItem item = new TimetableItem();
                                item.setSubjectId(s.getSubjectId());
                                item.setSubjectName(s.getName());
                                item.setDepartmentId(s.getDepartmentId() != null ? s.getDepartmentId() : "d1");
                                item.setDepartmentName(s.getDepartmentName() != null ? s.getDepartmentName() : "MCA");
                                item.setDivision("A");
                                item.setSemester(s.getSemester() != null ? s.getSemester() : "1");
                                item.setLectureNumber(1);
                                item.setRoomNumber("101");
                                item.setTeacherId(teacherUid);
                                item.setTeacherName(teacherName);
                                consolidatedMap.put(key, item);
                            }
                        }
                    }
                    if (isAdded()) {
                        updateSpinnerData(new ArrayList<>(consolidatedMap.values()));
                    }
                }
            });

            // Fetch timetable assigned to teacher UID
            fdb.collection("timetable").whereEqualTo("teacherId", teacherUid).get().addOnSuccessListener(tSnaps -> {
                if (tSnaps != null && !tSnaps.isEmpty()) {
                    for (DocumentSnapshot doc : tSnaps.getDocuments()) {
                        TimetableItem t = doc.toObject(TimetableItem.class);
                        if (t != null && !t.isBreak()) {
                            String key = t.getSubjectId() != null ? t.getSubjectId() : t.getSubjectName();
                            if (key != null && !key.isEmpty()) {
                                if (t.getTeacherId() == null || t.getTeacherId().isEmpty()) t.setTeacherId(teacherUid);
                                if (t.getTeacherName() == null || t.getTeacherName().isEmpty()) t.setTeacherName(teacherName);
                                consolidatedMap.put(key, t);
                            }
                        }
                    }
                    if (isAdded()) {
                        updateSpinnerData(new ArrayList<>(consolidatedMap.values()));
                    }
                }
            });

            // Fetch teacher doc for assignedSubjects list
            fdb.collection("teachers").document(teacherUid).get().addOnSuccessListener(tDoc -> {
                if (tDoc != null && tDoc.exists()) {
                    Teacher t = tDoc.toObject(Teacher.class);
                    if (t != null && t.getAssignedSubjects() != null) {
                        for (String subRef : t.getAssignedSubjects()) {
                            if (subRef != null && !subRef.trim().isEmpty() && !consolidatedMap.containsKey(subRef)) {
                                TimetableItem item = new TimetableItem();
                                item.setSubjectId(subRef);
                                item.setSubjectName(subRef);
                                item.setDepartmentId(t.getDepartmentId() != null ? t.getDepartmentId() : "d1");
                                item.setDepartmentName(t.getDepartmentName() != null ? t.getDepartmentName() : "MCA");
                                item.setDivision("A");
                                item.setSemester("1");
                                item.setLectureNumber(1);
                                item.setRoomNumber("101");
                                item.setTeacherId(teacherUid);
                                item.setTeacherName(teacherName);
                                consolidatedMap.put(subRef, item);
                            }
                        }
                        if (isAdded()) {
                            updateSpinnerData(new ArrayList<>(consolidatedMap.values()));
                        }
                    }
                }
            });
        }

        // If after checking everything it is empty, show proper fallback
        if (consolidatedMap.isEmpty()) {
            binding.layoutNoSubjects.setVisibility(View.VISIBLE);
            binding.btnGenerateQR.setEnabled(false);
        }
    }

    private void updateSpinnerData(List<TimetableItem> list) {
        if (!isAdded() || getContext() == null) return;

        assignedSubjectsList.clear();
        assignedSubjectsList.addAll(list);

        if (assignedSubjectsList.isEmpty()) {
            binding.layoutNoSubjects.setVisibility(View.VISIBLE);
            binding.btnGenerateQR.setEnabled(false);
            return;
        }

        binding.layoutNoSubjects.setVisibility(View.GONE);

        List<String> spinnerLabels = new ArrayList<>();
        for (TimetableItem item : assignedSubjectsList) {
            String label = "• " + item.getSubjectName();
            if (item.getDepartmentName() != null && !item.getDepartmentName().isEmpty()) {
                label += " (" + item.getDepartmentName() + (item.getDivision() != null ? "-" + item.getDivision() : "") + ")";
            }
            spinnerLabels.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, spinnerLabels);
        binding.spinnerQRSubject.setAdapter(adapter);

        // Pre-select first item
        if (!assignedSubjectsList.isEmpty()) {
            selectedSubjectItem = assignedSubjectsList.get(0);
            updateSessionDetailsUI(selectedSubjectItem);
            binding.btnGenerateQR.setEnabled(true);
        }
    }

    private void updateSessionDetailsUI(TimetableItem item) {
        if (item == null) return;

        String dept = (item.getDepartmentName() != null && !item.getDepartmentName().isEmpty()) ?
                item.getDepartmentName() : "MCA";
        String div = (item.getDivision() != null && !item.getDivision().isEmpty()) ?
                item.getDivision() : "A";

        binding.tvQRClassVal.setText(dept + "-" + div);
        binding.tvQRDivVal.setText(div);

        int lecNum = item.getLectureNumber() > 0 ? item.getLectureNumber() : 3;
        binding.tvQRLectureVal.setText("Lecture " + lecNum);

        String room = (item.getRoomNumber() != null && !item.getRoomNumber().isEmpty()) ?
                item.getRoomNumber() : "102";
        binding.tvQRRoomVal.setText(room);

        binding.tvQRDate.setText(DateTimeUtils.getCurrentDateDisplay());
        binding.tvQRTime.setText(DateTimeUtils.getCurrentTime());
    }

    private void generateDynamicSession() {
        if (selectedSubjectItem == null) {
            Toast.makeText(getContext(), "Please select a subject to generate attendance QR.", Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long expiresAt = now + SESSION_DURATION;

        String dept = selectedSubjectItem.getDepartmentName() != null ? selectedSubjectItem.getDepartmentName() : "MCA";
        String div = selectedSubjectItem.getDivision() != null ? selectedSubjectItem.getDivision() : "A";
        int lecNum = selectedSubjectItem.getLectureNumber() > 0 ? selectedSubjectItem.getLectureNumber() : 3;
        String room = selectedSubjectItem.getRoomNumber() != null ? selectedSubjectItem.getRoomNumber() : "102";

        currentSession = new QrSession(
                sessionId,
                teacherUid,
                teacherName,
                selectedSubjectItem.getSubjectId(),
                selectedSubjectItem.getSubjectName(),
                selectedSubjectItem.getDepartmentId(),
                dept,
                div,
                selectedSubjectItem.getSemester(),
                DateTimeUtils.getCurrentDateDb(),
                now,
                expiresAt
        );
        currentSession.setLectureNumber(lecNum);
        currentSession.setRoom(room);
        currentSession.setStatus("active");

        binding.btnGenerateQR.setEnabled(false);
        binding.btnRefreshQR.setEnabled(false);

        // Save session to Firestore
        FirebaseService.getInstance().getFirestore()
                .collection("qrSessions")
                .document(sessionId)
                .set(currentSession)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        binding.btnGenerateQR.setEnabled(true);
                        binding.btnRefreshQR.setEnabled(true);
                        showQR(sessionId);
                        startTimer(expiresAt);
                        startLiveAttendanceListener(sessionId, selectedSubjectItem.getSubjectId());
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        binding.btnGenerateQR.setEnabled(true);
                        binding.btnRefreshQR.setEnabled(true);
                        Toast.makeText(getContext(), "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showQR(String sessionId) {
        try {
            Bitmap qrBitmap = QRCodeUtils.generateQRCode(sessionId, 800, 800);
            if (qrBitmap != null) {
                binding.imgGeneratedQR.setImageBitmap(qrBitmap);
                binding.imgGeneratedQR.setAlpha(1.0f);

                binding.tvQRSubject.setText(selectedSubjectItem.getSubjectName());
                String dept = selectedSubjectItem.getDepartmentName() != null ? selectedSubjectItem.getDepartmentName() : "MCA";
                String div = selectedSubjectItem.getDivision() != null ? selectedSubjectItem.getDivision() : "A";
                binding.tvQRClass.setText(dept + "-" + div + " • Division " + div);
                binding.tvQRTeacher.setText("Teacher: " + teacherName);

                binding.cardQRDisplay.setVisibility(View.VISIBLE);
                binding.cardLiveAttendance.setVisibility(View.VISIBLE);
                binding.btnGenerateQR.setText("GENERATE QR");
            } else {
                Toast.makeText(getContext(), "Error: Could not generate QR image.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "QR Render Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer(long expiresAt) {
        if (countDownTimer != null) countDownTimer.cancel();
        binding.tvQRTimer.setTextColor(Color.parseColor("#F59E0B"));

        long remaining = expiresAt - System.currentTimeMillis();
        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return;
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                binding.tvQRTimer.setText(String.format(Locale.US, "Expires in: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (!isAdded()) return;
                binding.tvQRTimer.setText("QR EXPIRED");
                binding.tvQRTimer.setTextColor(Color.RED);
                binding.imgGeneratedQR.setAlpha(0.2f);
                if (currentSession != null) {
                    FirebaseService.getInstance().getFirestore()
                            .collection("qrSessions").document(currentSession.getSessionId()).delete();
                }
            }
        }.start();
    }

    private void startLiveAttendanceListener(String sessionId, String subjectId) {
        if (liveScanListener != null) {
            liveScanListener.remove();
        }

        String today = DateTimeUtils.getCurrentDateDb();
        FirebaseFirestore fdb = FirebaseService.getInstance().getFirestore();
        if (fdb == null) return;

        liveScanListener = fdb.collection("attendance")
                .whereEqualTo("date", today)
                .whereEqualTo("status", Constants.STATUS_PRESENT)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || !isAdded()) return;
                    if (snapshots != null) {
                        livePresentStudents.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Attendance att = doc.toObject(Attendance.class);
                            if (att != null) {
                                // Match by subjectId, teacherId, or timetableId
                                boolean matches = (subjectId != null && subjectId.equals(att.getSubjectId())) ||
                                                  (teacherUid != null && teacherUid.equals(att.getTeacherId())) ||
                                                  (teacherUid != null && teacherUid.equals(att.getMarkedByUid()));
                                if (matches) {
                                    livePresentStudents.add(att);
                                }
                            }
                        }

                        if (livePresentStudents.isEmpty()) {
                            binding.tvNoLiveStudents.setVisibility(View.VISIBLE);
                            binding.rvLivePresentStudents.setVisibility(View.GONE);
                            binding.tvLiveAttendanceCount.setText("0 Scanned");
                        } else {
                            binding.tvNoLiveStudents.setVisibility(View.GONE);
                            binding.rvLivePresentStudents.setVisibility(View.VISIBLE);
                            binding.tvLiveAttendanceCount.setText(livePresentStudents.size() + " Scanned");
                            liveAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        if (liveScanListener != null) {
            liveScanListener.remove();
            liveScanListener = null;
        }
        binding = null;
    }

    // --- Live Attendance Inner Adapter ---
    private static class LiveStudentAttendanceAdapter extends RecyclerView.Adapter<LiveStudentAttendanceAdapter.ViewHolder> {
        private final List<Attendance> list;

        public LiveStudentAttendanceAdapter(List<Attendance> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAttendanceBinding binding = ItemAttendanceBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Attendance a = list.get(position);
            String name = a.getStudentName() != null ? a.getStudentName() : "Student";
            String roll = a.getRollNo() != null ? a.getRollNo() : "";
            holder.binding.tvStudentInfo.setText(name + (!roll.isEmpty() ? " (" + roll + ")" : ""));

            String time = a.getPunchInTime() != null ? a.getPunchInTime() : "Just now";
            String sub = a.getSubjectName() != null ? a.getSubjectName() : "Subject";
            holder.binding.tvSubjectAndDate.setText(sub + " • " + time);

            String dept = a.getDepartmentName() != null ? a.getDepartmentName() : "MCA";
            String div = a.getSection() != null ? a.getSection() : "A";
            holder.binding.tvClassInfo.setText(dept + " - Div " + div);

            holder.binding.btnStatusToggle.setText("PRESENT ✓");
            holder.binding.btnStatusToggle.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_present));
            holder.binding.btnStatusToggle.setEnabled(false);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemAttendanceBinding binding;
            ViewHolder(ItemAttendanceBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
