package com.example.attendance_system.fragments.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.AdminAttendanceAdapter;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentAdminHomeBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.SharedPrefManager;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private static final String TAG = "AdminHomeFragment";
    private FragmentAdminHomeBinding binding;
    private AdminAttendanceAdapter studentAttendanceAdapter;
    private AdminAttendanceAdapter teacherAttendanceAdapter;
    private ListenerRegistration attendanceListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            binding.tvAdminGreeting.setText("Good Morning, " + user.getName() + " 👋");
        }

        binding.imgAdminProfile.setOnClickListener(v -> navigateToFragment(new AdminSettingsFragment()));
        binding.tvAdminGreeting.setOnClickListener(v -> navigateToFragment(new AdminSettingsFragment()));

        binding.btnHeaderSearch.setOnClickListener(v -> navigateToFragment(new ManageStudentsFragment()));
        binding.btnHeaderNotification.setOnClickListener(v ->
                Toast.makeText(getContext(), "You have 3 unread academic notifications", Toast.LENGTH_SHORT).show());

        binding.cardQuickAddStudent.setOnClickListener(v -> navigateToFragment(new ManageStudentsFragment()));
        binding.cardQuickAddTeacher.setOnClickListener(v -> navigateToFragment(new ManageTeachersFragment()));
        binding.cardQuickDepartments.setOnClickListener(v -> navigateToFragment(new ManageDepartmentsFragment()));
        binding.cardQuickSubjects.setOnClickListener(v -> navigateToFragment(new ManageSubjectsFragment()));
        binding.cardQuickNotices.setOnClickListener(v -> navigateToFragment(new NoticeBroadcastFragment()));
        binding.cardQuickAttendance.setOnClickListener(v -> navigateToFragment(new AttendanceReportFragment()));

        setupRecyclerViews();
        setupSwipeListener();
        listenToLiveAttendance();
        startSubtleBackgroundMotion();
    }

    private void setupRecyclerViews() {
        studentAttendanceAdapter = new AdminAttendanceAdapter(this::showAttendanceDetailsBottomSheet);
        binding.rvLiveAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLiveAttendance.setAdapter(studentAttendanceAdapter);

        teacherAttendanceAdapter = new AdminAttendanceAdapter(this::showAttendanceDetailsBottomSheet);
        binding.rvLiveTeacherAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLiveTeacherAttendance.setAdapter(teacherAttendanceAdapter);
    }

    private void setupSwipeListener() {
        if (binding == null || binding.hsvAdminAttendance == null) return;
        binding.hsvAdminAttendance.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (binding != null && binding.tvAdminSlideIndicator != null) {
                int threshold = binding.layoutStudentAttendancePage.getWidth() / 2;
                if (threshold <= 0) threshold = 175;
                boolean isTeacherPage = scrollX >= threshold;
                binding.tvAdminSlideIndicator.setText(isTeacherPage ? "○ ●" : "● ○");
                binding.tvLiveAttendanceTitle.setText(isTeacherPage ? "LIVE TEACHER ATTENDANCE" : "LIVE STUDENT ATTENDANCE");
            }
        });
    }

    private void listenToLiveAttendance() {
        if (!isAdded()) return;

        String todayDate = DateTimeUtils.getCurrentDateDb();
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        int totalStudents = db.getStudentCount();
        int totalTeachers = db.getTeacherCount();

        binding.tvStatTotalStudents.setText(String.valueOf(totalStudents));
        binding.tvStatTotalTeachers.setText(String.valueOf(totalTeachers));

        attendanceListener = FirebaseService.getInstance().getFirestore()
                .collection("attendance")
                .whereEqualTo("date", todayDate)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore Attendance Listener error: " + e.getMessage());
                        loadLocalFallback(todayDate, totalStudents);
                        return;
                    }

                    if (!isAdded() || binding == null) return;

                    List<Attendance> studentRecords = new ArrayList<>();
                    List<Attendance> teacherRecords = new ArrayList<>();
                    int present = 0, absent = 0, leave = 0, pendingPunchOut = 0;

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Attendance record = doc.toObject(Attendance.class);
                            if (record != null) {
                                boolean isTeacher = (record.getTeacherId() != null && !record.getTeacherId().isEmpty()) || "Faculty Attendance".equalsIgnoreCase(record.getSubjectName());
                                
                                if (isTeacher) {
                                    teacherRecords.add(record);
                                } else {
                                    studentRecords.add(record);
                                }

                                String status = record.getStatus() != null ? record.getStatus().toUpperCase() : "PRESENT";
                                if (status.contains("LEAVE")) {
                                    leave++;
                                } else if (status.contains("ABSENT")) {
                                    absent++;
                                } else {
                                    present++;
                                }

                                if (record.getPunchOutTime() == null || record.getPunchOutTime().isEmpty()) {
                                    pendingPunchOut++;
                                }
                            }
                        }
                    }

                    int finalAbsent = Math.max(0, totalStudents - present - leave);
                    binding.tvStatPresentToday.setText(String.valueOf(present));
                    binding.tvStatAbsentToday.setText(String.valueOf(finalAbsent));
                    binding.tvStatLeaveToday.setText(String.valueOf(leave));
                    binding.tvStatPendingPunchOut.setText(String.valueOf(pendingPunchOut));

                    // Sorting: Pending first, then latest Punch In first
                    java.util.Comparator<Attendance> sortingComparator = (o1, o2) -> {
                        boolean pending1 = o1.getPunchOutTime() == null || o1.getPunchOutTime().trim().isEmpty();
                        boolean pending2 = o2.getPunchOutTime() == null || o2.getPunchOutTime().trim().isEmpty();
                        if (pending1 && !pending2) return -1;
                        if (!pending1 && pending2) return 1;
                        // Both pending or both completed, sort by Punch In Timestamp (latest first)
                        return Long.compare(o2.getPunchInTimestamp(), o1.getPunchInTimestamp());
                    };

                    studentRecords.sort(sortingComparator);
                    teacherRecords.sort(sortingComparator);

                    studentAttendanceAdapter.setAttendanceList(studentRecords);
                    teacherAttendanceAdapter.setAttendanceList(teacherRecords);

                    binding.tvNoStudentAttendance.setVisibility(studentRecords.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvLiveAttendance.setVisibility(studentRecords.isEmpty() ? View.GONE : View.VISIBLE);
                    
                    binding.tvNoTeacherAttendance.setVisibility(teacherRecords.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvLiveTeacherAttendance.setVisibility(teacherRecords.isEmpty() ? View.GONE : View.VISIBLE);

                    setupPieChart(present, finalAbsent, leave);
                });
    }

    private void loadLocalFallback(String todayDate, int totalStudents) {
        if (!isAdded() || binding == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        int[] stats = db.getTodayAttendanceStats(todayDate);
        binding.tvStatPresentToday.setText(String.valueOf(stats[0]));
        binding.tvStatAbsentToday.setText(String.valueOf(stats[1]));
        binding.tvStatLeaveToday.setText(String.valueOf(stats[2]));
        setupPieChart(stats[0], stats[1], stats[2]);
    }

    private void showAttendanceDetailsBottomSheet(Attendance attendance) {
        if (!isAdded() || attendance == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_attendance_details, null, false);
        dialog.setContentView(v);

        TextView tvName = v.findViewById(R.id.tvDetailStudentName);
        TextView tvStatusBadge = v.findViewById(R.id.tvDetailStatusBadge);
        TextView tvStudentId = v.findViewById(R.id.tvDetailStudentId);
        TextView tvSubject = v.findViewById(R.id.tvDetailSubject);
        TextView tvPunchIn = v.findViewById(R.id.tvDetailPunchIn);
        TextView tvPunchOut = v.findViewById(R.id.tvDetailPunchOut);
        TextView tvDuration = v.findViewById(R.id.tvDetailDuration);
        TextView tvLocationCoords = v.findViewById(R.id.tvDetailLocationCoords);
        TextView tvGpsDistance = v.findViewById(R.id.tvDetailGpsDistance);
        TextView tvGeofenceStatus = v.findViewById(R.id.tvDetailGeofenceStatus);
        TextView tvFaceStatus = v.findViewById(R.id.tvDetailFaceStatus);
        TextView tvLivenessStatus = v.findViewById(R.id.tvDetailLivenessStatus);
        TextView tvMatchScore = v.findViewById(R.id.tvDetailMatchScore);
        View btnDismiss = v.findViewById(R.id.btnDismissDetail);

        String name = attendance.getStudentName() != null && !attendance.getStudentName().isEmpty() ? attendance.getStudentName() : (attendance.getTeacherName() != null ? attendance.getTeacherName() : "User");
        boolean isTeacher = (attendance.getTeacherId() != null && !attendance.getTeacherId().isEmpty()) || "Faculty Attendance".equalsIgnoreCase(attendance.getSubjectName());
        String studentId = attendance.getStudentId() != null && !attendance.getStudentId().isEmpty() ? attendance.getStudentId() : (attendance.getTeacherId() != null ? attendance.getTeacherId() : "N/A");

        if (tvName != null) tvName.setText(name);
        if (tvStudentId != null) tvStudentId.setText((isTeacher ? "Teacher ID: " : "Student ID: ") + studentId + (isTeacher ? " • Role: Faculty" : " • Section: " + (attendance.getSection() != null ? attendance.getSection() : "A")));
        if (tvSubject != null) tvSubject.setText(isTeacher ? "Faculty Daily Attendance" : "Subject: " + (attendance.getSubjectName() != null ? attendance.getSubjectName() : "Data Science & AI"));

        if (tvPunchIn != null) tvPunchIn.setText(attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "--:--");
        if (tvPunchOut != null) tvPunchOut.setText(attendance.getPunchOutTime() != null ? attendance.getPunchOutTime() : "Pending");
        if (tvDuration != null) tvDuration.setText(attendance.getWorkingDuration());

        if (tvStatusBadge != null) {
            String status = attendance.getStatus() != null ? attendance.getStatus() : "PRESENT";
            tvStatusBadge.setText(status);
        }

        if (tvLocationCoords != null) {
            tvLocationCoords.setText("Punch In Location: " + String.format("%.4f", attendance.getPunchInLatitude()) + "° N, " + String.format("%.4f", attendance.getPunchInLongitude()) + "° E");
        }
        if (tvGpsDistance != null) {
            tvGpsDistance.setText("Distance from Office: " + (int) attendance.getPunchInDistanceFromOffice() + "m (Accuracy: " + (int) attendance.getPunchInAccuracy() + "m)");
        }
        if (tvGeofenceStatus != null) {
            tvGeofenceStatus.setText(attendance.isPunchInGeofenceVerified() ? "Geofence Status: ✓ INSIDE OFFICE RADIUS" : "Geofence Status: ✕ OUTSIDE GEOFENCE");
            tvGeofenceStatus.setTextColor(attendance.isPunchInGeofenceVerified() ? 0xFF10B981 : 0xFFEF4444);
        }

        if (tvFaceStatus != null) {
            tvFaceStatus.setText("Face Verified: " + (attendance.isFaceVerified() ? "✓ VERIFIED" : "✕ NOT VERIFIED"));
        }
        if (tvLivenessStatus != null) {
            tvLivenessStatus.setText("Liveness Scanner: " + (attendance.isLivenessVerified() ? "✓ LIVE HUMAN DETECTED" : "✕ NOT CHECKED"));
        }
        if (tvMatchScore != null) {
            tvMatchScore.setText("Identity Match Result: " + (attendance.isIdentityVerified() ? "PASSED (Identity Matched)" : "FAILED"));
        }

        if (btnDismiss != null) btnDismiss.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (attendanceListener != null) {
            attendanceListener.remove();
            attendanceListener = null;
        }
        binding = null;
    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setupPieChart(int present, int absent, int leave) {
        if (binding == null) return;
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int total = present + absent + leave;

        if (present > 0) {
            entries.add(new PieEntry(present, "Present"));
            colors.add(Color.parseColor("#10B981"));
        }
        if (absent > 0) {
            entries.add(new PieEntry(absent, "Absent"));
            colors.add(Color.parseColor("#EF4444"));
        }
        if (leave > 0) {
            entries.add(new PieEntry(leave, "Leave"));
            colors.add(Color.parseColor("#F59E0B"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
            colors.add(Color.parseColor("#CBD5E1"));
            binding.pieChartAttendance.setCenterText("No Attendance\nRecorded Today");
        } else {
            binding.pieChartAttendance.setCenterText("Total Sessions\n" + total);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(4f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(13f);

        PieData pieData = new PieData(dataSet);
        if (total > 0) {
            pieData.setValueFormatter(new PercentFormatter(binding.pieChartAttendance));
            binding.pieChartAttendance.setUsePercentValues(true);
        } else {
            pieData.setValueTextSize(0f);
        }

        binding.pieChartAttendance.setDrawEntryLabels(false);
        binding.pieChartAttendance.getDescription().setEnabled(false);
        binding.pieChartAttendance.setHoleRadius(50f);
        binding.pieChartAttendance.setTransparentCircleRadius(55f);
        binding.pieChartAttendance.setCenterTextSize(14f);
        binding.pieChartAttendance.setCenterTextColor(Color.parseColor("#1E293B"));
        binding.pieChartAttendance.setData(pieData);

        Legend l = binding.pieChartAttendance.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTextSize(12f);
        l.setXEntrySpace(16f);

        binding.pieChartAttendance.animateY(600);
        binding.pieChartAttendance.invalidate();
    }

    private void startSubtleBackgroundMotion() {
        if (binding.vAdminTopRightCircle != null) {
            android.animation.ObjectAnimator animTopRight = android.animation.ObjectAnimator.ofFloat(binding.vAdminTopRightCircle, "translationY", 0f, 15f, 0f);
            animTopRight.setDuration(12000);
            animTopRight.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animTopRight.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animTopRight.start();
        }
        if (binding.vAdminBottomLeftCircle != null) {
            android.animation.ObjectAnimator animBottomLeft = android.animation.ObjectAnimator.ofFloat(binding.vAdminBottomLeftCircle, "translationY", 0f, -15f, 0f);
            animBottomLeft.setDuration(15000);
            animBottomLeft.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animBottomLeft.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animBottomLeft.start();
        }
    }
}
