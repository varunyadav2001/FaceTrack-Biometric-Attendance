package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemTimetableBinding;
import com.example.attendance_system.models.TimetableItem;

import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableItem> list = new ArrayList<>();

    public void setList(List<TimetableItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimetableBinding binding = ItemTimetableBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableItem item = list.get(position);
        holder.binding.tvTime.setText(item.getStartTime());
        holder.binding.tvDuration.setText(item.getTimeSlot());
        
        if (item.isBreak()) {
            holder.binding.tvSubject.setText("LUNCH BREAK");
            holder.binding.tvSubject.setTextColor(0xFFF59E0B);
            holder.binding.tvTeacher.setText("No lectures during break");
            holder.binding.tvRoom.setText("Lounge / Cafeteria");
            holder.binding.getRoot().setAlpha(0.9f);
            holder.binding.getRoot().setBackgroundColor(0xFFFFFBEB);
        } else {
            String sub = item.getSubjectName();
            if (item.getLectureNumber() > 0) {
                sub = "Lecture " + item.getLectureNumber() + ": " + sub;
            }
            String tName = item.getTeacherName();
            if (tName == null || tName.isEmpty() || "TBD".equalsIgnoreCase(tName)) {
                tName = "Not Assigned";
            }
            holder.binding.tvSubject.setText(sub);
            holder.binding.tvTeacher.setText("Teacher: " + tName);
            holder.binding.tvRoom.setText("Room: " + (item.getRoomNumber() != null ? item.getRoomNumber() : "TBD") + " • Division " + item.getDivision());
            holder.binding.getRoot().setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTimetableBinding binding;
        public ViewHolder(ItemTimetableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
