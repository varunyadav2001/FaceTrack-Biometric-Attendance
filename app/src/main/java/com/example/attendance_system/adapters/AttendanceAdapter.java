package com.example.attendance_system.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemAttendanceBinding;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChanged(Attendance attendance, String newStatus);
    }

    private List<Attendance> list = new ArrayList<>();
    private List<Attendance> fullList = new ArrayList<>();
    private final OnStatusChangeListener listener;

    public AttendanceAdapter(OnStatusChangeListener listener) {
        this.listener = listener;
    }

    public void setAttendanceList(List<Attendance> list) {
        this.fullList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            this.list = new ArrayList<>(fullList);
        } else {
            String lower = query.toLowerCase().trim();
            List<Attendance> filtered = new ArrayList<>();
            for (Attendance item : fullList) {
                String name = item.getStudentName() != null ? item.getStudentName().toLowerCase() : "";
                String roll = item.getRollNo() != null ? item.getRollNo().toLowerCase() : "";
                String sub = item.getSubjectName() != null ? item.getSubjectName().toLowerCase() : "";
                if (name.contains(lower) || roll.contains(lower) || sub.contains(lower)) {
                    filtered.add(item);
                }
            }
            this.list = filtered;
        }
        notifyDataSetChanged();
    }

    public List<Attendance> getAttendanceList() {
        return fullList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceBinding binding = ItemAttendanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceBinding binding;

        public AttendanceViewHolder(ItemAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Attendance item) {
            String name = item.getStudentName() != null ? item.getStudentName() : "Student";
            String studentId = item.getStudentId() != null && !item.getStudentId().isEmpty() ? item.getStudentId() : item.getRollNo();
            String section = item.getSection() != null && !item.getSection().isEmpty() ? item.getSection() : "A";
            String subject = item.getSubjectName() != null && !item.getSubjectName().isEmpty() ? item.getSubjectName() : "General";

            binding.tvStudentInfo.setText(name + " (" + (studentId != null ? studentId : "ST") + ")");

            String subInfo = "Section: " + section + " • Subject: " + subject;
            binding.tvSubjectAndDate.setText(subInfo);

            String inTime = item.getPunchInTime() != null && !item.getPunchInTime().isEmpty() ? item.getPunchInTime() : "--:--";
            String outTime = item.getPunchOutTime() != null && !item.getPunchOutTime().isEmpty() ? item.getPunchOutTime() : "Pending";
            String duration = item.getWorkingDuration() != null && !item.getWorkingDuration().isEmpty() ? item.getWorkingDuration() : "In progress";

            StringBuilder sb = new StringBuilder();
            sb.append("Punch In: ").append(inTime);
            sb.append("\nPunch Out: ").append(outTime);
            sb.append("\nWorking Duration: ").append(duration);

            binding.tvClassInfo.setText(sb.toString());

            String status = item.getStatus() != null ? item.getStatus() : "WORKING";
            updateStatusButton(status);

            binding.btnStatusToggle.setOnClickListener(v -> {
                String currentStatus = item.getStatus();
                String nextStatus;
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(currentStatus) || "FULL DAY".equalsIgnoreCase(currentStatus)) {
                    nextStatus = Constants.STATUS_ABSENT;
                } else if (Constants.STATUS_ABSENT.equalsIgnoreCase(currentStatus)) {
                    nextStatus = Constants.STATUS_LEAVE;
                } else if (Constants.STATUS_LEAVE.equalsIgnoreCase(currentStatus)) {
                    nextStatus = "HALF DAY";
                } else {
                    nextStatus = Constants.STATUS_PRESENT;
                }

                item.setStatus(nextStatus);
                for (Attendance masterItem : fullList) {
                    if (masterItem.getAttendanceId() != null && masterItem.getAttendanceId().equals(item.getAttendanceId())) {
                        masterItem.setStatus(nextStatus);
                    }
                }
                updateStatusButton(nextStatus);

                if (listener != null) {
                    listener.onStatusChanged(item, nextStatus);
                }
            });
        }

        private void updateStatusButton(String status) {
            String cleanStatus = status != null ? status.toUpperCase() : "WORKING";
            binding.btnStatusToggle.setText(cleanStatus);

            if (cleanStatus.contains("FULL DAY") || cleanStatus.contains("PRESENT")) {
                binding.btnStatusToggle.setBackgroundColor(Color.parseColor("#10B981")); // Green
            } else if (cleanStatus.contains("HALF DAY")) {
                binding.btnStatusToggle.setBackgroundColor(Color.parseColor("#F59E0B")); // Amber / Orange
            } else if (cleanStatus.contains("WORKING") || cleanStatus.contains("PENDING")) {
                binding.btnStatusToggle.setBackgroundColor(Color.parseColor("#6366F1")); // Purple / Indigo
            } else if (cleanStatus.contains("ABSENT")) {
                binding.btnStatusToggle.setBackgroundColor(Color.parseColor("#EF4444")); // Red
            } else {
                binding.btnStatusToggle.setBackgroundColor(Color.parseColor("#64748B")); // Slate Gray
            }
        }
    }
}
