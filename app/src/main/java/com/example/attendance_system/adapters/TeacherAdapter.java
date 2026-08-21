package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemTeacherBinding;
import com.example.attendance_system.models.Teacher;

import java.util.ArrayList;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    public interface OnTeacherClickListener {
        void onDeleteClick(Teacher teacher);
        void onForcePunchOutClick(Teacher teacher);
        void onItemClick(Teacher teacher);
    }

    private List<Teacher> teacherList = new ArrayList<>();
    private List<Teacher> fullList = new ArrayList<>();
    private final OnTeacherClickListener listener;

    public TeacherAdapter(OnTeacherClickListener listener) {
        this.listener = listener;
    }

    public void setTeacherList(List<Teacher> list) {
        this.fullList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.teacherList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            this.teacherList = new ArrayList<>(fullList);
        } else {
            String lower = query.toLowerCase().trim();
            List<Teacher> filtered = new ArrayList<>();
            for (Teacher t : fullList) {
                String name = t.getName() != null ? t.getName().toLowerCase() : "";
                String dept = t.getDepartmentName() != null ? t.getDepartmentName().toLowerCase() : "";
                String desig = t.getDesignation() != null ? t.getDesignation().toLowerCase() : "";
                if (name.contains(lower) || dept.contains(lower) || desig.contains(lower)) {
                    filtered.add(t);
                }
            }
            this.teacherList = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeacherBinding binding = ItemTeacherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TeacherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        holder.bind(teacherList.get(position));
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    class TeacherViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeacherBinding binding;

        public TeacherViewHolder(ItemTeacherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Teacher teacher) {
            binding.tvTeacherName.setText(teacher.getName());
            String desigDept = teacher.getDesignation() + " • Dept of " + teacher.getDepartmentName();
            binding.tvDesignationAndDept.setText(desigDept);
            binding.tvTeacherEmail.setText(teacher.getEmail());

            if (teacher.getProfileImageUrl() != null && !teacher.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(teacher.getProfileImageUrl())
                        .placeholder(R.drawable.ic_teacher)
                        .into(binding.imgTeacher);
            } else {
                binding.imgTeacher.setImageResource(R.drawable.ic_teacher);
            }

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(teacher);
            });

            binding.btnForcePunchOut.setOnClickListener(v -> {
                if (listener != null) listener.onForcePunchOutClick(teacher);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(teacher);
            });
        }
    }
}
