package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemNoticeBinding;
import com.example.attendance_system.models.Notice;
import com.example.attendance_system.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> list = new ArrayList<>();

    public void setNoticeList(List<Notice> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoticeBinding binding = ItemNoticeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NoticeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoticeBinding binding;

        public NoticeViewHolder(ItemNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Notice notice) {
            binding.tvNoticeTitle.setText(notice.getTitle());
            binding.tvNoticeBadge.setText(notice.getType() != null ? notice.getType() : "NOTICE");
            binding.tvNoticeMessage.setText(notice.getMessage());

            String senderDate = "Posted by " + (notice.getSenderName() != null ? notice.getSenderName() : "Admin") +
                    " • " + DateTimeUtils.formatTimestamp(notice.getTimestamp());
            binding.tvSenderAndDate.setText(senderDate);
        }
    }
}
