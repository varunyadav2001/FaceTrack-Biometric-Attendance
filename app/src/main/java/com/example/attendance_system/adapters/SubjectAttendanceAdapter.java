package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemSubjectAttendanceBinding;
import com.example.attendance_system.models.SubjectAttendance;

import java.util.ArrayList;
import java.util.List;

public class SubjectAttendanceAdapter extends RecyclerView.Adapter<SubjectAttendanceAdapter.ViewHolder> {

    private List<SubjectAttendance> list = new ArrayList<>();

    public void setList(List<SubjectAttendance> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectAttendanceBinding binding = ItemSubjectAttendanceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectAttendance item = list.get(position);
        holder.binding.tvSubjectName.setText(item.getSubjectName());
        
        String stats = item.getPresentClasses() + " / " + item.getTotalClasses() + " Classes";
        if (item.getTeacherName() != null && !item.getTeacherName().isEmpty()) {
            stats += " • " + item.getTeacherName();
        }
        holder.binding.tvClassStats.setText(stats);
        
        holder.binding.tvAttendancePercent.setText(String.format("%.1f%%", item.getPercentage()));
        holder.binding.pbAttendance.setProgress((int) item.getPercentage());
        
        holder.binding.tvPresentDetails.setText("Present: " + item.getPresentClasses());
        holder.binding.tvAbsentDetails.setText("Absent: " + item.getAbsentClasses());
        holder.binding.tvLeaveDetails.setText("Leave: " + item.getLeaveClasses());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSubjectAttendanceBinding binding;
        public ViewHolder(ItemSubjectAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
