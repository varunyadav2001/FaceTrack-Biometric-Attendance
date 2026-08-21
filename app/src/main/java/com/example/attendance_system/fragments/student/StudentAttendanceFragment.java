package com.example.attendance_system.fragments.student;

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

import com.example.attendance_system.adapters.AttendanceAdapter;
import com.example.attendance_system.databinding.FragmentStudentAttendanceBinding;
import com.example.attendance_system.helpers.DialogHelper;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.ExportUtils;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.viewmodel.AttendanceViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceFragment extends Fragment {

    private FragmentStudentAttendanceBinding binding;
    private AttendanceViewModel viewModel;
    private AttendanceAdapter adapter;
    private List<Attendance> attendanceLogs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        binding.rvStudentHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttendanceAdapter((attendance, newStatus) -> {});
        binding.rvStudentHistory.setAdapter(adapter);

        viewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                attendanceLogs = list;
                adapter.setAttendanceList(list);
            }
        });

        binding.btnDownloadStudentPDF.setOnClickListener(v -> generateStudentPDF());

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        String identifier = "";
        if (user != null) {
            if (user.getUid() != null && !user.getUid().isEmpty()) {
                identifier = user.getUid();
            } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                identifier = user.getEmail();
            } else if (user.getName() != null && !user.getName().isEmpty()) {
                identifier = user.getName();
            }
        }

        viewModel.fetchAttendanceByStudent(identifier);
    }

    private void generateStudentPDF() {
        if (attendanceLogs == null || attendanceLogs.isEmpty()) {
            Toast.makeText(getContext(), "No attendance logs found to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File pdfFile = ExportUtils.exportAttendanceToPDF(requireContext(), attendanceLogs, "Student Attendance Transcript");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("PDF Transcript Downloaded")
                    .setMessage("PDF generated and saved to Documents: " + pdfFile.getName())
                    .setPositiveButton("Share PDF", (dialog, which) -> ExportUtils.sharePDF(requireContext(), pdfFile))
                    .setNegativeButton("Close", null)
                    .show();
        } catch (Exception e) {
            DialogHelper.showErrorDialog(getContext(), "PDF Export Failed", e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
