package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemAttendanceSlideBinding;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.utils.DateTimeUtils;

public class AttendanceSlideAdapter extends RecyclerView.Adapter<AttendanceSlideAdapter.SlideViewHolder> {

    public interface OnSlideActionListener {
        void onPunchInClick();
        void onPunchOutClick();
    }

    private Attendance attendance;
    private String dateDisplayStr;
    private final OnSlideActionListener listener;

    public AttendanceSlideAdapter(OnSlideActionListener listener) {
        this.listener = listener;
        this.dateDisplayStr = DateTimeUtils.getCurrentDateDisplay();
    }

    public void setAttendanceData(Attendance attendance, String dateDisplayStr) {
        this.attendance = attendance;
        if (dateDisplayStr != null && !dateDisplayStr.isEmpty()) {
            this.dateDisplayStr = dateDisplayStr;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 2; // Slide 0: Punch In, Slide 1: Punch Out
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceSlideBinding binding = ItemAttendanceSlideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SlideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.bind(position);
    }

    class SlideViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceSlideBinding binding;

        public SlideViewHolder(ItemAttendanceSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.tvSlideDate.setText("Date: " + dateDisplayStr);

            boolean hasPunchIn = (attendance != null && ((attendance.getPunchInTimestamp() > 0) || (attendance.getPunchInTime() != null && !attendance.getPunchInTime().trim().isEmpty())));
            boolean hasPunchOut = (attendance != null && ((attendance.getPunchOutTimestamp() > 0) || (attendance.getPunchOutTime() != null && !attendance.getPunchOutTime().trim().isEmpty())));

            if (position == 0) {
                // SLIDE 1: PUNCH IN SLIDE
                binding.tvSlideHeaderTitle.setText("PUNCH IN SLIDE");
                
                if (!hasPunchIn) {
                    // BEFORE PUNCH IN
                    binding.tvSlideStatusBadge.setText("NOT PUNCHED IN");
                    binding.tvSlideStatusBadge.setTextColor(0xFFF59E0B);
                    binding.tvSlideStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
                    
                    binding.tvSlideValue1.setText("Not completed");
                    binding.tvSlideValue2.setText("Not available");
                    
                    binding.layoutSlideRow2.setVisibility(View.GONE);
                    
                    binding.btnSlideAction.setText("PUNCH IN");
                    binding.btnSlideAction.setEnabled(true);
                    binding.btnSlideAction.setOnClickListener(v -> {
                        if (listener != null) listener.onPunchInClick();
                    });
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("🔒 Punch In first to unlock Punch Out");
                    binding.tvSlideHint.setTextColor(0xFF94A3B8);
                } else if (!hasPunchOut) {
                    // PUNCH IN COMPLETED (WORKING)
                    binding.tvSlideStatusBadge.setText("🟢 WORKING");
                    binding.tvSlideStatusBadge.setTextColor(0xFF10B981);
                    binding.tvSlideStatusBadge.setBackgroundResource(R.drawable.bg_badge_green_light);
                    
                    binding.tvSlideValue1.setText(attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "");
                    binding.tvSlideValue2.setText("Not completed");
                    
                    binding.layoutSlideRow2.setVisibility(View.VISIBLE);
                    binding.tvSlideLabel3.setText("Status");
                    binding.tvSlideValue3.setText("Currently Working");
                    binding.layoutAttendanceType.setVisibility(View.GONE);
                    
                    binding.btnSlideAction.setText("PUNCHED IN ✓");
                    binding.btnSlideAction.setEnabled(false);
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("Swipe left for Punch Out →");
                    binding.tvSlideHint.setTextColor(0xFF4F46E5);
                } else {
                    // PUNCH IN & PUNCH OUT COMPLETED
                    binding.tvSlideStatusBadge.setText("✓ PRESENT");
                    binding.tvSlideStatusBadge.setTextColor(0xFF10B981);
                    binding.tvSlideStatusBadge.setBackgroundResource(R.drawable.bg_badge_green_light);
                    
                    binding.tvSlideValue1.setText(attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "");
                    binding.tvSlideValue2.setText(attendance.getPunchOutTime() != null ? attendance.getPunchOutTime() : "");
                    
                    binding.layoutSlideRow2.setVisibility(View.VISIBLE);
                    binding.tvSlideLabel3.setText("Working Time");
                    binding.tvSlideValue3.setText(attendance.getWorkingDuration() != null ? attendance.getWorkingDuration() : "");
                    
                    binding.layoutAttendanceType.setVisibility(View.VISIBLE);
                    String attType = attendance.getStatus() != null ? attendance.getStatus() : "FULL DAY";
                    binding.tvSlideValue4.setText(attType);
                    
                    binding.btnSlideAction.setText("COMPLETED ✓");
                    binding.btnSlideAction.setEnabled(false);
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("Swipe left to view Punch Out summary →");
                    binding.tvSlideHint.setTextColor(0xFF4F46E5);
                }
            } else {
                // SLIDE 2: PUNCH OUT SLIDE
                binding.tvSlideHeaderTitle.setText("PUNCH OUT SLIDE");

                if (!hasPunchIn) {
                    // LOCKED SLIDE BEFORE PUNCH IN
                    binding.tvSlideStatusBadge.setText("LOCKED 🔒");
                    binding.tvSlideStatusBadge.setTextColor(0xFF94A3B8);
                    binding.tvSlideStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange_light);
                    
                    binding.tvSlideValue1.setText("Not completed");
                    binding.tvSlideValue2.setText("Not available");
                    
                    binding.layoutSlideRow2.setVisibility(View.GONE);
                    
                    binding.btnSlideAction.setText("PUNCH OUT (LOCKED)");
                    binding.btnSlideAction.setEnabled(false);
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("← Swipe right to Punch In");
                    binding.tvSlideHint.setTextColor(0xFF94A3B8);
                } else if (!hasPunchOut) {
                    // PUNCH OUT AVAILABLE (WORKING)
                    binding.tvSlideStatusBadge.setText("🟢 PUNCH OUT READY");
                    binding.tvSlideStatusBadge.setTextColor(0xFF10B981);
                    binding.tvSlideStatusBadge.setBackgroundResource(R.drawable.bg_badge_green_light);
                    
                    binding.tvSlideValue1.setText(attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "");
                    binding.tvSlideValue2.setText("Not completed");
                    
                    binding.layoutSlideRow2.setVisibility(View.VISIBLE);
                    binding.tvSlideLabel3.setText("Working Time");
                    binding.tvSlideValue3.setText("Currently Working");
                    binding.layoutAttendanceType.setVisibility(View.GONE);
                    
                    binding.btnSlideAction.setText("PUNCH OUT");
                    binding.btnSlideAction.setEnabled(true);
                    binding.btnSlideAction.setOnClickListener(v -> {
                        if (listener != null) listener.onPunchOutClick();
                    });
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("← Swipe right to return to Punch In");
                    binding.tvSlideHint.setTextColor(0xFF4F46E5);
                } else {
                    // PUNCH OUT COMPLETED
                    String attType = attendance.getStatus() != null ? attendance.getStatus() : "FULL DAY";
                    boolean isHalfDay = attType.contains("HALF") || attType.contains("HALF_DAY");

                    binding.tvSlideStatusBadge.setText(isHalfDay ? "🟡 HALF DAY" : "🟢 FULL DAY");
                    binding.tvSlideStatusBadge.setTextColor(isHalfDay ? 0xFFF59E0B : 0xFF10B981);
                    binding.tvSlideStatusBadge.setBackgroundResource(isHalfDay ? R.drawable.bg_badge_orange_light : R.drawable.bg_badge_green_light);
                    
                    binding.tvSlideValue1.setText(attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "");
                    binding.tvSlideValue2.setText(attendance.getPunchOutTime() != null ? attendance.getPunchOutTime() : "");
                    
                    binding.layoutSlideRow2.setVisibility(View.VISIBLE);
                    binding.tvSlideLabel3.setText("Working Time");
                    binding.tvSlideValue3.setText(attendance.getWorkingDuration() != null ? attendance.getWorkingDuration() : "");
                    
                    binding.layoutAttendanceType.setVisibility(View.VISIBLE);
                    binding.tvSlideValue4.setText(attType);
                    binding.tvSlideValue4.setTextColor(isHalfDay ? 0xFFF59E0B : 0xFF10B981);

                    binding.btnSlideAction.setText("PUNCH OUT COMPLETED ✓");
                    binding.btnSlideAction.setEnabled(false);
                    
                    binding.tvSlideHint.setVisibility(View.VISIBLE);
                    binding.tvSlideHint.setText("← Swipe right to view Punch In");
                    binding.tvSlideHint.setTextColor(0xFF4F46E5);
                }
            }
        }
    }
}
