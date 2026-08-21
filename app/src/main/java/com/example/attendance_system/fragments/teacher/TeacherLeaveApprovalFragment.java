package com.example.attendance_system.fragments.teacher;

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
import com.example.attendance_system.databinding.FragmentTeacherLeaveApprovalBinding;
import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.utils.Constants;
import com.example.attendance_system.viewmodel.LeaveViewModel;

public class TeacherLeaveApprovalFragment extends Fragment {

    private FragmentTeacherLeaveApprovalBinding binding;
    private LeaveViewModel viewModel;
    private LeaveAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherLeaveApprovalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LeaveViewModel.class);

        binding.rvPendingLeaves.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaveAdapter(new LeaveAdapter.OnLeaveActionListener() {
            @Override
            public void onApprove(LeaveRequest request) {
                viewModel.updateLeaveStatus(request.getLeaveId(), Constants.LEAVE_APPROVED, "Approved by Faculty");
                Toast.makeText(getContext(), "Leave Approved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(LeaveRequest request) {
                viewModel.updateLeaveStatus(request.getLeaveId(), Constants.LEAVE_REJECTED, "Insufficient reason");
                Toast.makeText(getContext(), "Leave Rejected", Toast.LENGTH_SHORT).show();
            }
        }, true);
        binding.rvPendingLeaves.setAdapter(adapter);

        viewModel.getLeaveListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) adapter.setLeaveList(list);
        });

        viewModel.fetchPendingLeaves();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
