package com.example.attendance_system.fragments.student;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.LeaveAdapter;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentApplyLeaveBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.viewmodel.LeaveViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ApplyLeaveFragment extends Fragment {

    private FragmentApplyLeaveBinding binding;
    private LeaveViewModel viewModel;
    private LeaveAdapter adapter;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    private final SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentApplyLeaveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LeaveViewModel.class);

        binding.rvStudentLeaveHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaveAdapter(null, false);
        binding.rvStudentLeaveHistory.setAdapter(adapter);

        setupPickers();

        binding.btnSubmitLeaveRequest.setOnClickListener(v -> submitLeave());

        viewModel.getLeaveListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.setLeaveList(list);
            }
        });

        viewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                DialogHelper.showSuccessDialog(getContext(), "Leave Application Submitted",
                        "Your leave application has been submitted for faculty approval.");
                binding.etStartDate.setText("");
                binding.etStartTime.setText("");
                binding.etEndDate.setText("");
                binding.etEndTime.setText("");
                binding.etLeaveReason.setText("");
            }
        });

        String uid = SharedPrefManager.getInstance(requireContext()).getUserUid();
        viewModel.fetchStudentLeaves(uid.isEmpty() ? "1" : uid);
    }

    private void setupPickers() {
        // Start Date
        View.OnClickListener startDateClick = v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.etStartDate.setText(dateFormat.format(startCalendar.getTime()));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        };
        binding.etStartDate.setOnClickListener(startDateClick);
        binding.tilStartDate.setEndIconOnClickListener(startDateClick);

        // Start Time
        View.OnClickListener startTimeClick = v -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalendar.set(Calendar.MINUTE, minute);
                startCalendar.set(Calendar.SECOND, 0);
                binding.etStartTime.setText(timeFormat.format(startCalendar.getTime()));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
            tpd.show();
        };
        binding.etStartTime.setOnClickListener(startTimeClick);
        binding.tilStartTime.setEndIconOnClickListener(startTimeClick);

        // End Date
        View.OnClickListener endDateClick = v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.etEndDate.setText(dateFormat.format(endCalendar.getTime()));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        };
        binding.etEndDate.setOnClickListener(endDateClick);
        binding.tilEndDate.setEndIconOnClickListener(endDateClick);

        // End Time
        View.OnClickListener endTimeClick = v -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endCalendar.set(Calendar.MINUTE, minute);
                endCalendar.set(Calendar.SECOND, 0);
                binding.etEndTime.setText(timeFormat.format(endCalendar.getTime()));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
            tpd.show();
        };
        binding.etEndTime.setOnClickListener(endTimeClick);
        binding.tilEndTime.setEndIconOnClickListener(endTimeClick);
    }

    private void submitLeave() {
        String startDateStr = binding.etStartDate.getText() != null ? binding.etStartDate.getText().toString().trim() : "";
        String startTimeStr = binding.etStartTime.getText() != null ? binding.etStartTime.getText().toString().trim() : "";
        String endDateStr = binding.etEndDate.getText() != null ? binding.etEndDate.getText().toString().trim() : "";
        String endTimeStr = binding.etEndTime.getText() != null ? binding.etEndTime.getText().toString().trim() : "";
        String reason = binding.etLeaveReason.getText() != null ? binding.etLeaveReason.getText().toString().trim() : "";

        // 1. Check all required fields
        if (startDateStr.isEmpty() || startTimeStr.isEmpty() || endDateStr.isEmpty() || endTimeStr.isEmpty() || reason.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all date, time, and reason fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Comprehensive Date/Time comparison
        try {
            Date startDateTime = fullDateTimeFormat.parse(startDateStr + " " + startTimeStr);
            Date endDateTime = fullDateTimeFormat.parse(endDateStr + " " + endTimeStr);

            if (startDateTime != null && endDateTime != null) {
                if (!endDateTime.after(startDateTime)) {
                    Toast.makeText(getContext(), "End date/time must be after start date/time.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } catch (Exception e) {
            // In case of parse discrepancy, also verify dates directly
            try {
                Date sDate = dateFormat.parse(startDateStr);
                Date eDate = dateFormat.parse(endDateStr);
                if (sDate != null && eDate != null && eDate.before(sDate)) {
                    Toast.makeText(getContext(), "End date/time must be after start date/time.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception ignored) {}
        }

        String uid = SharedPrefManager.getInstance(requireContext()).getUserUid();
        User loggedInUser = SharedPrefManager.getInstance(requireContext()).getUser();
        String name = loggedInUser != null ? loggedInUser.getName() : "Student";
        String rollNo = "";
        String departmentId = "MCA";
        String departmentName = "Master of Computer Applications";
        String semester = "MCA";
        String section = "A";

        // Query rich student details from SQLite
        Student student = DatabaseHelper.getInstance(requireContext()).getStudentByUid(uid);
        if (student != null) {
            if (student.getName() != null && !student.getName().isEmpty()) name = student.getName();
            if (student.getRollNo() != null) rollNo = student.getRollNo();
            if (student.getDepartmentId() != null) departmentId = student.getDepartmentId();
            if (student.getDepartmentName() != null) departmentName = student.getDepartmentName();
            if (student.getSemester() != null) semester = student.getSemester();
            if (student.getSection() != null) section = student.getSection();
        }

        String leaveId = "leave_" + System.currentTimeMillis();
        LeaveRequest request = new LeaveRequest();
        request.setLeaveId(leaveId);
        request.setStudentId(uid.isEmpty() ? "1" : uid);
        request.setStudentUid(uid.isEmpty() ? "1" : uid);
        request.setStudentName(name);
        request.setRollNo(rollNo);
        request.setDepartmentId(departmentId);
        request.setDepartmentName(departmentName);
        request.setSemester(semester);
        request.setSection(section);
        request.setStartDate(startDateStr);
        request.setStartTime(startTimeStr);
        request.setEndDate(endDateStr);
        request.setEndTime(endTimeStr);
        request.setReason(reason);
        request.setStatus("Pending");
        request.setRemarks("");
        request.setAppliedOn(System.currentTimeMillis());
        request.setSubmittedAt(System.currentTimeMillis());

        viewModel.applyLeave(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
