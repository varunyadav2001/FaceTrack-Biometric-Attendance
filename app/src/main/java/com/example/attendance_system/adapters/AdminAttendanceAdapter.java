package com.example.attendance_system.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.models.Attendance;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminAttendanceAdapter extends RecyclerView.Adapter<AdminAttendanceAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Attendance attendance);
    }

    private List<Attendance> attendanceList = new ArrayList<>();
    private final OnItemClickListener listener;

    public AdminAttendanceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setAttendanceList(List<Attendance> list) {
        this.attendanceList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance record = attendanceList.get(position);
        holder.bind(record, listener);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvStudentName, tvSubjectAndId, tvRecordPunchIn, tvRecordPunchOut, tvRecordDuration, tvRecordStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAdminAttendance);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvSubjectAndId = itemView.findViewById(R.id.tvSubjectAndId);
            tvRecordPunchIn = itemView.findViewById(R.id.tvRecordPunchIn);
            tvRecordPunchOut = itemView.findViewById(R.id.tvRecordPunchOut);
            tvRecordDuration = itemView.findViewById(R.id.tvRecordDuration);
            tvRecordStatusBadge = itemView.findViewById(R.id.tvRecordStatusBadge);
        }

        public void bind(Attendance record, OnItemClickListener listener) {
            if (record == null) return;

            String name = record.getStudentName() != null && !record.getStudentName().isEmpty() ? record.getStudentName() : (record.getTeacherName() != null ? record.getTeacherName() : "User");
            boolean isTeacher = (record.getTeacherId() != null && !record.getTeacherId().isEmpty()) || "Faculty Attendance".equalsIgnoreCase(record.getSubjectName());
            String subject = isTeacher ? "Faculty Daily Attendance" : (record.getSubjectName() != null ? record.getSubjectName() : "General");
            String studentId = record.getStudentId() != null && !record.getStudentId().isEmpty() ? record.getStudentId() : (record.getTeacherId() != null ? record.getTeacherId() : "N/A");
            String dept = record.getDepartmentName() != null ? record.getDepartmentName() : "MCA";

            tvStudentName.setText(name);
            tvSubjectAndId.setText((isTeacher ? "Teacher ID: " : "ID: ") + studentId + " • " + (isTeacher ? dept : subject));

            tvRecordPunchIn.setText(record.getPunchInTime() != null ? record.getPunchInTime() : "--:--");

            boolean hasPunchOut = record.getPunchOutTime() != null && !record.getPunchOutTime().isEmpty();

            if (hasPunchOut) {
                tvRecordPunchOut.setText(record.getPunchOutTime());
                String status = record.getStatus() != null && !record.getStatus().isEmpty() ? record.getStatus().toUpperCase() : "COMPLETED";
                tvRecordStatusBadge.setText("✓ " + status);
                
                if (status.contains("HALF")) {
                    tvRecordStatusBadge.setTextColor(0xFFF59E0B);
                    tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
                } else {
                    tvRecordStatusBadge.setTextColor(0xFF10B981);
                    tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_green_light);
                }

                // Punch Out Card Theme (Red/Coral)
                if (cardView != null) {
                    cardView.setCardBackgroundColor(Color.parseColor("#FEF2F2"));
                    cardView.setStrokeColor(Color.parseColor("#F87171"));
                }
                tvRecordDuration.setTextColor(0xFFEF4444);
            } else {
                tvRecordPunchOut.setText("Pending");
                tvRecordStatusBadge.setText("🟡 PUNCH OUT PENDING");
                tvRecordStatusBadge.setTextColor(0xFFF59E0B);
                tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);

                // Punch In Card Theme (Blue/Indigo)
                if (cardView != null) {
                    cardView.setCardBackgroundColor(Color.parseColor("#F8F7FF"));
                    cardView.setStrokeColor(Color.parseColor("#6366F1"));
                }
                tvRecordDuration.setTextColor(0xFF4F46E5);
            }

            String duration = record.getWorkingDuration() != null ? record.getWorkingDuration() : (hasPunchOut ? "N/A" : "In progress");
            tvRecordDuration.setText(duration);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(record);
            });
        }
    }
}
