package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemTodayClassBinding;
import com.example.attendance_system.models.TimetableItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayClassAdapter extends RecyclerView.Adapter<TodayClassAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TimetableItem item);
    }

    private List<TimetableItem> list = new ArrayList<>();
    private OnItemClickListener listener;

    public TodayClassAdapter() {}

    public TodayClassAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<TimetableItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayClassBinding binding = ItemTodayClassBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableItem item = list.get(position);
        holder.binding.tvStartTime.setText(item.getStartTime());
        holder.binding.tvEndTime.setText(item.getEndTime());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        if (item.isBreak()) {
            holder.binding.tvClassName.setText("LUNCH BREAK");
            holder.binding.tvClassName.setTextColor(0xFFF59E0B); // Amber
            holder.binding.tvTeacherName.setText("Rest & Refuel");
            holder.binding.tvRoomInfo.setText("Cafeteria / Student Lounge");
            holder.binding.tvStatusBadge.setText("LUNCH");
            holder.binding.tvStatusBadge.setBackgroundResource(com.example.attendance_system.R.drawable.bg_badge_orange_light);
            holder.binding.tvStatusBadge.setTextColor(0xFFF59E0B);
            
            // Distinct styling for break to make it a separate field
            holder.binding.getRoot().setCardBackgroundColor(0xFFFFFBEB);
            holder.binding.getRoot().setStrokeWidth(2);
            holder.binding.getRoot().setStrokeColor(0xFFFEF3C7);
        } else {
            String title;
            if (item.getLectureNumber() > 0) {
                title = "L" + item.getLectureNumber() + ": " + item.getSubjectName();
            } else {
                title = item.getSubjectName();
            }
            holder.binding.tvClassName.setText(title);
            holder.binding.tvClassName.setTextColor(0xFF1E293B); // text_primary
            String tName = item.getTeacherName();
            if (tName == null || tName.isEmpty() || "TBD".equalsIgnoreCase(tName)) {
                tName = "Not Assigned";
            }
            holder.binding.tvTeacherName.setText(tName);
            String room = item.getRoomNumber() != null ? item.getRoomNumber() : "TBD";
            holder.binding.tvRoomInfo.setText("Room " + room + " • " + item.getDepartmentName() + "-" + item.getDivision());
            updateStatusBadge(holder, item);
            
            // Default styling for lectures
            holder.binding.getRoot().setCardBackgroundColor(0xFFFFFFFF);
            holder.binding.getRoot().setStrokeWidth(0);
        }
    }

    private void updateStatusBadge(ViewHolder holder, TimetableItem item) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTimeStr = sdf.format(new Date());
            Date now = sdf.parse(currentTimeStr);
            Date start = sdf.parse(item.getStartTime());
            Date end = sdf.parse(item.getEndTime());

            if (now != null && start != null && end != null) {
                if (now.after(start) && now.before(end)) {
                    holder.binding.tvStatusBadge.setText("LIVE");
                    holder.binding.tvStatusBadge.setTextColor(0xFFEF4444); // Red
                    holder.binding.tvStatusBadge.setBackgroundResource(com.example.attendance_system.R.drawable.bg_badge_red_light);
                } else if (now.after(end)) {
                    holder.binding.tvStatusBadge.setText("COMPLETED");
                    holder.binding.tvStatusBadge.setTextColor(0xFF10B981); // Green
                    holder.binding.tvStatusBadge.setBackgroundResource(com.example.attendance_system.R.drawable.bg_badge_green_light);
                } else {
                    holder.binding.tvStatusBadge.setText("UPCOMING");
                    holder.binding.tvStatusBadge.setTextColor(0xFF3B82F6); // Blue
                    holder.binding.tvStatusBadge.setBackgroundResource(com.example.attendance_system.R.drawable.bg_badge_light_blue);
                }
            }
        } catch (Exception e) {
            holder.binding.tvStatusBadge.setText("TODAY");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTodayClassBinding binding;
        public ViewHolder(ItemTodayClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
