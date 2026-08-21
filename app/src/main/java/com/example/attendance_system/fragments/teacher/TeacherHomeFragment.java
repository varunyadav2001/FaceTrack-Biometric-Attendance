package com.example.attendance_system.fragments.teacher;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.R;
import com.example.attendance_system.adapters.AttendanceAdapter;
import com.example.attendance_system.adapters.NoticeAdapter;
import com.example.attendance_system.adapters.TodayClassAdapter;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentTeacherHomeBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Teacher;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.viewmodel.AttendanceViewModel;
import com.example.attendance_system.viewmodel.NoticeViewModel;
import com.example.attendance_system.viewmodel.TeacherViewModel;
import com.example.attendance_system.viewmodel.TimetableViewModel;

import java.util.ArrayList;
import java.util.List;

public class TeacherHomeFragment extends Fragment {

    private static final String TAG = "TeacherHomeFragment";
    private FragmentTeacherHomeBinding binding;
    private NoticeViewModel noticeViewModel;
    private AttendanceViewModel attendanceViewModel;
    private TimetableViewModel timetableViewModel;
    private TeacherViewModel teacherViewModel;
    private NoticeAdapter noticeAdapter;
    private AttendanceAdapter presentStudentAdapter;
    private TodayClassAdapter timetableAdapter;
    private Teacher currentTeacher;
    private List<Attendance> allAttendance = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        if (user != null) {
            binding.tvWelcomeTeacher.setText("Welcome, " + user.getName());
            binding.tvAssignedStudentCount.setText(db.getAssignedStudentCount(user.getUid()) + " Students");
        }

        setupHorizontalSwipeListener();
        setupClickListeners();
        startSubtleBackgroundMotion();

        noticeViewModel = new ViewModelProvider(this).get(NoticeViewModel.class);
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        timetableViewModel = new ViewModelProvider(this).get(TimetableViewModel.class);
        teacherViewModel = new ViewModelProvider(this).get(TeacherViewModel.class);

        binding.rvTeacherNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        noticeAdapter = new NoticeAdapter();
        binding.rvTeacherNotices.setAdapter(noticeAdapter);

        binding.rvPresentStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        presentStudentAdapter = new AttendanceAdapter((attendance, newStatus) -> {
            // Optional: Handle status change if needed
        });
        binding.rvPresentStudents.setAdapter(presentStudentAdapter);

        binding.rvTeacherTimetable.setLayoutManager(new LinearLayoutManager(getContext()));
        timetableAdapter = new TodayClassAdapter(item -> {
            if (!item.isBreak()) {
                MarkAttendanceFragment fragment = new MarkAttendanceFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("selected_timetable_item", item);
                fragment.setArguments(bundle);
                navigateTo(fragment);
            }
        });
        binding.rvTeacherTimetable.setAdapter(timetableAdapter);

        noticeViewModel.getNoticeListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) noticeAdapter.setNoticeList(list);
        });

        attendanceViewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                allAttendance = list;
                updateAttendanceStats(list);
                loadTodayAttendance();
            }
        });

        teacherViewModel.getTeacherLiveData().observe(getViewLifecycleOwner(), teacher -> {
            if (teacher != null) {
                currentTeacher = teacher;
                Log.d(TAG, "Current Logged-In Teacher UID: " + teacher.getUid() + " | Name: " + teacher.getName());
                binding.tvWelcomeTeacher.setText("Welcome, " + teacher.getName());
                binding.tvAssignedStudentCount.setText(db.getAssignedStudentCount(teacher.getUid()) + " Students");
                timetableViewModel.fetchTeacherTimetable(teacher.getUid(), DateTimeUtils.getCurrentDay());
                attendanceViewModel.fetchAttendanceForTeacher(DateTimeUtils.getCurrentDateDb(), "", "");
                loadTodayAttendance();
            } else {
                Log.d(TAG, "Teacher data is null for logged in user");
            }
        });

        attendanceViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                binding.progressTeacherHome.setVisibility(View.VISIBLE);
            } else {
                binding.progressTeacherHome.setVisibility(View.GONE);
            }
        });

        timetableViewModel.getTimetableLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                Log.d(TAG, "Fetched Timetable Assignments Count: " + list.size());
                binding.tvTodayClassesCount.setText(list.size() + " Classes");
                timetableAdapter.setList(list);
                binding.rvTeacherTimetable.setVisibility(View.VISIBLE);
                binding.tvNoClassesTeacher.setVisibility(View.GONE);

                User userProfile = SharedPrefManager.getInstance(requireContext()).getUser();
                if (userProfile != null) {
                    binding.tvAssignedStudentCount.setText(db.getAssignedStudentCount(userProfile.getUid()) + " Students");
                }
            } else {
                Log.d(TAG, "Fetched Timetable Assignments Count: 0");
                binding.tvTodayClassesCount.setText("0 Classes");
                timetableAdapter.setList(new ArrayList<>());
                binding.rvTeacherTimetable.setVisibility(View.GONE);
                binding.tvNoClassesTeacher.setVisibility(View.VISIBLE);
                binding.tvNoClassesTeacher.setText("No classes assigned for today.");
            }
        });

        noticeViewModel.fetchNotices();
        fetchTodayAttendance();
        loadTodayAttendance();
    }

    private void setupHorizontalSwipeListener() {
        if (binding == null || binding.hsvTeacherAttendanceSlider == null) return;
        binding.hsvTeacherAttendanceSlider.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (binding != null && binding.tvTeacherSlideIndicator != null) {
                int threshold = binding.cardTeacherPunchInPage.getWidth() / 2;
                if (threshold <= 0) threshold = 160;
                boolean isPage2 = scrollX >= threshold;
                binding.tvTeacherSlideIndicator.setText(isPage2 ? "○ ●" : "● ○");
                binding.tvTeacherSlideIndicator.setTextColor(isPage2 ? 0xFFEF4444 : 0xFF4F46E5);
            }
        });
    }

    private void setupClickListeners() {
        // QR GENERATE button
        binding.btnGenerateQRQuick.setOnClickListener(v -> navigateTo(new QRAttendanceGenFragment()));

        binding.btnRegisterFaceQuickHome.setOnClickListener(v -> handleFaceRegistrationClick());
        binding.btnTeacherRegisterFace.setOnClickListener(v -> handleFaceRegistrationClick());

        binding.btnTeacherPunchIn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.teacherContainer, new PunchInVerificationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.btnTeacherPunchOut.setOnClickListener(v -> {
            if (getActivity() != null) {
                PunchInVerificationFragment fragment = new PunchInVerificationFragment();
                Bundle args = new Bundle();
                args.putBoolean("isPunchOut", true);
                fragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.teacherContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void handleFaceRegistrationClick() {
        User currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser != null && currentUser.isFaceRegistered()) {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("FACE ALREADY REGISTERED")
                    .setMessage("Your face is already registered for this account.\n\nOnly one face registration is allowed.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            navigateTo(new RegisterFaceFragment());
        }
    }

    private void loadTodayAttendance() {
        if (binding == null || getContext() == null) return;

        String dateDisplay = DateTimeUtils.getCurrentDateDisplay();
        binding.tvTeacherTodayDate.setText("Date: " + dateDisplay);
        binding.tvTeacherPage2Date.setText("Date: " + dateDisplay);

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        String uid = user != null ? user.getUid() : SharedPrefManager.getInstance(requireContext()).getUserUid();
        String dateStr = DateTimeUtils.getCurrentDateDb();

        Attendance record = DatabaseHelper.getInstance(requireContext()).getTodayAttendanceForUser(dateStr, uid);
        if (record == null && currentTeacher != null && currentTeacher.getTeacherId() != null) {
            record = DatabaseHelper.getInstance(requireContext()).getTodayAttendanceForUser(dateStr, currentTeacher.getTeacherId());
        }

        if (record == null && allAttendance != null) {
            for (Attendance a : allAttendance) {
                if (a != null && a.getPunchInTime() != null && !a.getPunchInTime().trim().isEmpty()) {
                    if (uid.equals(a.getStudentUid()) || uid.equals(a.getStudentId()) || uid.equals(a.getTeacherId()) || uid.equals(a.getMarkedByUid())) {
                        record = a;
                        break;
                    }
                }
            }
        }

        Log.d(TAG, "CURRENT TEACHER UID: " + uid);
        Log.d(TAG, "TODAY DATE: " + dateStr);
        Log.d(TAG, "TODAY ATTENDANCE RECORD: " + (record != null ? record.getAttendanceId() : "NULL"));

        if (record == null && uid != null && !uid.isEmpty()) {
            String attId = dateStr + "_" + uid + "_main";
            FirebaseService.getInstance().getFirestore().collection("attendance").document(attId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null && snapshot.exists() && isAdded()) {
                            Attendance remoteRecord = snapshot.toObject(Attendance.class);
                            if (remoteRecord != null) {
                                DatabaseHelper.getInstance(requireContext()).insertOrUpdateAttendanceRecord(remoteRecord);
                                loadTodayAttendance();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "Remote attendance check error: " + e.getMessage()));
        }

        boolean hasPunchIn = (record != null && ((record.getPunchInTimestamp() > 0) || (record.getPunchInTime() != null && !record.getPunchInTime().trim().isEmpty())));
        boolean hasPunchOut = (record != null && ((record.getPunchOutTimestamp() > 0) || (record.getPunchOutTime() != null && !record.getPunchOutTime().trim().isEmpty())));

        if (!hasPunchIn) {
            // STATE A: BEFORE PUNCH IN
            // Page 1 (Punch In)
            binding.tvTeacherPunchInTime.setText("Not completed");
            binding.tvTeacherPunchOutTime.setText("Not available");
            binding.tvTeacherAttendanceStatusBadge.setText("NOT PUNCHED IN");
            binding.tvTeacherAttendanceStatusBadge.setTextColor(0xFFF59E0B);
            binding.tvTeacherAttendanceStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
            binding.layoutTeacherPage1WorkingRow.setVisibility(View.GONE);
            binding.btnTeacherPunchIn.setText("PUNCH IN");
            binding.btnTeacherPunchIn.setEnabled(true);
            binding.tvTeacherPage1SwipeHint.setVisibility(View.GONE);

            // Page 2 (Punch Out - Locked)
            binding.tvTeacherPage2PunchInTime.setText("Not completed");
            binding.tvTeacherPage2PunchOutTime.setText("Not available");
            binding.tvTeacherPage2StatusBadge.setText("LOCKED 🔒");
            binding.tvTeacherPage2StatusBadge.setTextColor(0xFF94A3B8);
            binding.tvTeacherPage2StatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
            binding.layoutTeacherPage2WorkingRow.setVisibility(View.GONE);
            binding.btnTeacherPunchOut.setText("PUNCH OUT (LOCKED)");
            binding.btnTeacherPunchOut.setEnabled(false);
            binding.tvTeacherPage2LockHint.setText("🔒 Complete Punch In first");
            binding.tvTeacherPage2LockHint.setTextColor(0xFF94A3B8);
        } else if (!hasPunchOut) {
            // STATE B: PUNCH IN COMPLETED (WORKING / PENDING)
            String pInTime = record.getPunchInTime() != null ? record.getPunchInTime() : "";

            // Page 1 (Punch In Done)
            binding.tvTeacherPunchInTime.setText(pInTime);
            binding.tvTeacherPunchOutTime.setText("Pending");
            binding.tvTeacherAttendanceStatusBadge.setText("PUNCH OUT PENDING");
            binding.tvTeacherAttendanceStatusBadge.setTextColor(0xFFF59E0B);
            binding.tvTeacherAttendanceStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
            binding.layoutTeacherPage1WorkingRow.setVisibility(View.VISIBLE);
            binding.tvTeacherPage1WorkingTime.setText("In progress");
            binding.btnTeacherPunchIn.setText("PUNCHED IN ✓");
            binding.btnTeacherPunchIn.setEnabled(false);
            binding.tvTeacherPage1SwipeHint.setVisibility(View.VISIBLE);
            binding.tvTeacherPage1SwipeHint.setText("Swipe left for Punch Out →");

            // Page 2 (Punch Out Ready)
            binding.tvTeacherPage2PunchInTime.setText(pInTime);
            binding.tvTeacherPage2PunchOutTime.setText("Pending");
            binding.tvTeacherPage2StatusBadge.setText("🔴 PUNCH OUT READY");
            binding.tvTeacherPage2StatusBadge.setTextColor(0xFFEF4444);
            binding.tvTeacherPage2StatusBadge.setBackgroundResource(R.drawable.bg_badge_red_light);
            binding.layoutTeacherPage2WorkingRow.setVisibility(View.VISIBLE);
            binding.tvTeacherPage2WorkingTime.setText("In progress");
            binding.layoutTeacherPage2AttendanceType.setVisibility(View.GONE);
            binding.btnTeacherPunchOut.setText("PUNCH OUT");
            binding.btnTeacherPunchOut.setEnabled(true);
            binding.tvTeacherPage2LockHint.setText("Swipe right for Punch In ←");
            binding.tvTeacherPage2LockHint.setTextColor(0xFFEF4444);
        } else {
            // STATE C: PUNCH OUT COMPLETED
            String pInTime = record.getPunchInTime() != null ? record.getPunchInTime() : "";
            String pOutTime = record.getPunchOutTime() != null ? record.getPunchOutTime() : "";
            String duration = record.getWorkingDuration() != null ? record.getWorkingDuration() : "";
            String attType = record.getStatus() != null && !record.getStatus().isEmpty() ? record.getStatus() : "FULL DAY";
            boolean isHalfDay = attType.contains("HALF") || attType.contains("HALF_DAY");

            // Page 1
            binding.tvTeacherPunchInTime.setText(pInTime);
            binding.tvTeacherPunchOutTime.setText(pOutTime);
            binding.tvTeacherAttendanceStatusBadge.setText("✓ " + attType);
            binding.tvTeacherAttendanceStatusBadge.setTextColor(isHalfDay ? 0xFFF59E0B : 0xFF10B981);
            binding.tvTeacherAttendanceStatusBadge.setBackgroundResource(isHalfDay ? R.drawable.bg_badge_orange_light : R.drawable.bg_badge_green_light);
            binding.layoutTeacherPage1WorkingRow.setVisibility(View.VISIBLE);
            binding.tvTeacherPage1WorkingTime.setText(duration);
            binding.btnTeacherPunchIn.setText("COMPLETED ✓");
            binding.btnTeacherPunchIn.setEnabled(false);
            binding.tvTeacherPage1SwipeHint.setVisibility(View.VISIBLE);
            binding.tvTeacherPage1SwipeHint.setText("Swipe left for Punch Out summary →");

            // Page 2 (Punch Out Summary)
            binding.tvTeacherPage2PunchInTime.setText(pInTime);
            binding.tvTeacherPage2PunchOutTime.setText(pOutTime);
            binding.tvTeacherPage2StatusBadge.setText(isHalfDay ? "🟡 HALF DAY" : "🟢 FULL DAY");
            binding.tvTeacherPage2StatusBadge.setTextColor(isHalfDay ? 0xFFF59E0B : 0xFF10B981);
            binding.tvTeacherPage2StatusBadge.setBackgroundResource(isHalfDay ? R.drawable.bg_badge_orange_light : R.drawable.bg_badge_green_light);
            binding.layoutTeacherPage2WorkingRow.setVisibility(View.VISIBLE);
            binding.tvTeacherPage2WorkingTime.setText(duration);
            binding.layoutTeacherPage2AttendanceType.setVisibility(View.VISIBLE);
            binding.tvTeacherPage2AttendanceType.setText(attType);
            binding.tvTeacherPage2AttendanceType.setTextColor(isHalfDay ? 0xFFF59E0B : 0xFF10B981);
            binding.btnTeacherPunchOut.setText("PUNCH OUT COMPLETED ✓");
            binding.btnTeacherPunchOut.setEnabled(false);
            binding.tvTeacherPage2LockHint.setText("Swipe right for Punch In ←");
            binding.tvTeacherPage2LockHint.setTextColor(0xFFEF4444);

            // Auto smooth scroll to Page 2 on completed Punch Out
            binding.hsvTeacherAttendanceSlider.post(() -> {
                if (binding != null && binding.hsvTeacherAttendanceSlider != null) {
                    binding.hsvTeacherAttendanceSlider.fullScroll(View.FOCUS_RIGHT);
                }
            });
        }

        updateVerificationDetailsUI(record);

        if (user != null && user.isFaceRegistered()) {
            binding.btnTeacherRegisterFace.setText("✓ FACE REGISTERED");
        } else {
            binding.btnTeacherRegisterFace.setText("REGISTER FACE");
        }
    }

    private void updateVerificationDetailsUI(Attendance record) {
        if (binding == null || record == null) {
            if (binding != null) binding.cardTeacherVerificationDetails.setVisibility(View.GONE);
            return;
        }

        boolean hasPunchIn = (record.getPunchInTime() != null && !record.getPunchInTime().trim().isEmpty());
        if (!hasPunchIn) {
            binding.cardTeacherVerificationDetails.setVisibility(View.GONE);
            return;
        }

        binding.cardTeacherVerificationDetails.setVisibility(View.VISIBLE);

        // Set Photo
        if (record.getVerificationImage() != null && !record.getVerificationImage().isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(record.getVerificationImage(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.ivTeacherVerificationPhoto.setImageBitmap(decodedByte);
            } catch (Exception e) {
                binding.ivTeacherVerificationPhoto.setImageResource(R.drawable.ic_person);
            }
        } else {
            binding.ivTeacherVerificationPhoto.setImageResource(R.drawable.ic_person);
        }

        // Set Statuses
        if (record.isFaceVerified()) {
            binding.tvTeacherFaceVerifiedStatus.setText("✓ Face Verified");
            binding.tvTeacherFaceVerifiedStatus.setTextColor(0xFF10B981);
        } else {
            binding.tvTeacherFaceVerifiedStatus.setText("✕ Face Not Verified");
            binding.tvTeacherFaceVerifiedStatus.setTextColor(0xFFEF4444);
        }

        if (record.isLivenessVerified()) {
            binding.tvTeacherLivenessVerifiedStatus.setText("✓ Liveness Verified");
            binding.tvTeacherLivenessVerifiedStatus.setTextColor(0xFF10B981);
        } else {
            binding.tvTeacherLivenessVerifiedStatus.setText("✕ Liveness Not Verified");
            binding.tvTeacherLivenessVerifiedStatus.setTextColor(0xFFEF4444);
        }

        if (record.isIdentityVerified()) {
            binding.tvTeacherIdentityMatchedStatus.setText("✓ Identity Matched");
            binding.tvTeacherIdentityMatchedStatus.setTextColor(0xFF10B981);
        } else {
            binding.tvTeacherIdentityMatchedStatus.setText("✕ Identity Not Matched");
            binding.tvTeacherIdentityMatchedStatus.setTextColor(0xFFEF4444);
        }

        binding.tvTeacherIdentityMatchScore.setText("Identity Match: " + (int)(record.getFaceMatchScore() * 100) + "%");
    }

    private void fetchTodayAttendance() {
        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        if (user == null) return;

        String uid = user.getUid();
        teacherViewModel.fetchTeacherByUid(uid);
        attendanceViewModel.fetchAttendanceForTeacher(DateTimeUtils.getCurrentDateDb(), uid, "");
    }

    private void updateAttendanceStats(List<Attendance> list) {
        int present = 0, absent = 0, leave = 0;
        List<Attendance> presentList = new ArrayList<>();

        if (list != null) {
            for (Attendance a : list) {
                String st = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(st) || st.contains("FULL DAY") || st.contains("HALF DAY") || st.contains("WORKING") || st.contains("PRESENT")) {
                    present++;
                    presentList.add(a);
                } else if (Constants.STATUS_ABSENT.equalsIgnoreCase(st)) {
                    absent++;
                } else if (Constants.STATUS_LEAVE.equalsIgnoreCase(st)) {
                    leave++;
                }
            }
        }

        binding.tvPresentTodayCount.setText(String.valueOf(present));
        binding.tvAbsentTodayCount.setText(String.valueOf(absent));
        binding.tvLeaveTodayCount.setText(String.valueOf(leave));
        binding.tvTotalTodayCount.setText(String.valueOf(list != null ? list.size() : 0));

        if (presentList.isEmpty()) {
            binding.rvPresentStudents.setVisibility(View.GONE);
            binding.tvNoPresentStudents.setVisibility(View.VISIBLE);
            binding.tvNoPresentStudents.setText("No attendance marked yet today.");
        } else {
            binding.rvPresentStudents.setVisibility(View.VISIBLE);
            binding.tvNoPresentStudents.setVisibility(View.GONE);
            presentStudentAdapter.setAttendanceList(presentList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTodayAttendance();
        loadTodayAttendance();
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.teacherContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void startSubtleBackgroundMotion() {
        if (binding.vTeacherTopRightCircle != null) {
            android.animation.ObjectAnimator animTopRight = android.animation.ObjectAnimator.ofFloat(binding.vTeacherTopRightCircle, "translationY", 0f, 15f, 0f);
            animTopRight.setDuration(12000);
            animTopRight.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animTopRight.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animTopRight.start();
        }
        if (binding.vTeacherBottomLeftCircle != null) {
            android.animation.ObjectAnimator animBottomLeft = android.animation.ObjectAnimator.ofFloat(binding.vTeacherBottomLeftCircle, "translationY", 0f, -15f, 0f);
            animBottomLeft.setDuration(15000);
            animBottomLeft.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animBottomLeft.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            animBottomLeft.start();
        }
    }
}

