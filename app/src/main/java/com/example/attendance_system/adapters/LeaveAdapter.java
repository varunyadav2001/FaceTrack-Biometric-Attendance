package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemLeaveBinding;
import com.example.attendance_system.models.LeaveRequest;
import com.example.attendance_system.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder> {

    public interface OnLeaveActionListener {
        void onApprove(LeaveRequest request);
        void onReject(LeaveRequest request);
    }

    private List<LeaveRequest> list = new ArrayList<>();
    private final OnLeaveActionListener listener;
    private boolean isTeacherOrAdmin = true;

    public LeaveAdapter(OnLeaveActionListener listener, boolean isTeacherOrAdmin) {
        this.listener = listener;
        this.isTeacherOrAdmin = isTeacherOrAdmin;
    }

    public void setLeaveList(List<LeaveRequest> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaveBinding binding = ItemLeaveBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LeaveViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LeaveViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaveBinding binding;

        public LeaveViewHolder(ItemLeaveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LeaveRequest item) {
            binding.tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "Student");

            String status = item.getStatus() != null ? item.getStatus() : "Pending";
            boolean isApproved = Constants.LEAVE_APPROVED.equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status);
            boolean isRejected = Constants.LEAVE_REJECTED.equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status);

            if (isApproved) {
                binding.tvLeaveStatus.setText("✓ LEAVE APPROVED");
                binding.tvLeaveStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_present));
                binding.tvLeaveStatus.setBackgroundResource(R.drawable.bg_badge_green_light);
            } else if (isRejected) {
                binding.tvLeaveStatus.setText("✕ LEAVE REJECTED");
                binding.tvLeaveStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_absent));
                binding.tvLeaveStatus.setBackgroundResource(R.drawable.bg_badge_red_light);
            } else {
                binding.tvLeaveStatus.setText("PENDING");
                binding.tvLeaveStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_leave));
                binding.tvLeaveStatus.setBackgroundResource(R.drawable.bg_badge_orange_light);
            }

            StringBuilder details = new StringBuilder();
            if (item.getRollNo() != null && !item.getRollNo().isEmpty()) {
                details.append("Roll No: ").append(item.getRollNo());
            }
            if (item.getSemester() != null && !item.getSemester().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Class: ").append(item.getSemester());
            }
            if (item.getSection() != null && !item.getSection().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Sec: ").append(item.getSection());
            }

            if (details.length() > 0) {
                binding.tvStudentDetails.setVisibility(View.VISIBLE);
                binding.tvStudentDetails.setText(details.toString());
            } else {
                binding.tvStudentDetails.setVisibility(View.GONE);
            }

            String startD = item.getStartDate() != null ? item.getStartDate() : "";
            String endD = item.getEndDate() != null ? item.getEndDate() : "";
            if (!startD.isEmpty() && !endD.isEmpty()) {
                binding.tvDates.setText("📅 " + startD + " → " + endD);
                binding.tvDates.setVisibility(View.VISIBLE);
            } else if (!startD.isEmpty()) {
                binding.tvDates.setText("📅 " + startD);
                binding.tvDates.setVisibility(View.VISIBLE);
            } else {
                binding.tvDates.setVisibility(View.GONE);
            }

            String startT = item.getStartTime() != null ? item.getStartTime() : "";
            String endT = item.getEndTime() != null ? item.getEndTime() : "";
            if (!startT.isEmpty() && !endT.isEmpty()) {
                binding.tvTimes.setText("🕐 " + startT + " → " + endT);
                binding.tvTimes.setVisibility(View.VISIBLE);
            } else if (!startT.isEmpty()) {
                binding.tvTimes.setText("🕐 " + startT);
                binding.tvTimes.setVisibility(View.VISIBLE);
            } else {
                binding.tvTimes.setVisibility(View.GONE);
            }

            String reason = item.getReason() != null ? item.getReason() : "";
            binding.tvReason.setText("Reason: " + reason);

            if (isTeacherOrAdmin && (!isApproved && !isRejected)) {
                binding.layoutActionButtons.setVisibility(View.VISIBLE);
            } else {
                binding.layoutActionButtons.setVisibility(View.GONE);
            }

            binding.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(item);
            });

            binding.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(item);
            });
        }
    }
}
