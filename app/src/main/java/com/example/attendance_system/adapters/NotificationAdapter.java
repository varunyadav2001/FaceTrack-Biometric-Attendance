package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemNotificationBinding;
import com.example.attendance_system.models.NotificationItem;
import com.example.attendance_system.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> list = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<NotificationItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvMessage.setText(item.getMessage());
        holder.binding.tvTime.setText(DateTimeUtils.getRelativeTime(item.getTimestamp()));
        
        if (item.isRead()) {
            holder.binding.unreadIndicator.setVisibility(View.GONE);
            holder.binding.llContainer.setAlpha(0.7f);
        } else {
            holder.binding.unreadIndicator.setVisibility(View.VISIBLE);
            holder.binding.llContainer.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        public ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
