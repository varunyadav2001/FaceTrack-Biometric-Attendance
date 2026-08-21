package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemSubjectBinding;
import com.example.attendance_system.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> list = new ArrayList<>();

    public void setSubjectList(List<Subject> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectBinding binding;

        public SubjectViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Subject subject) {
            binding.tvSubjectCode.setVisibility(android.view.View.GONE);
            binding.tvSubjectName.setText(subject.getName());
            String teacherSem = "Faculty: " + (subject.getTeacherName() != null && !subject.getTeacherName().isEmpty() ? subject.getTeacherName() : "Unassigned") + 
                                (subject.getSemester() != null ? " • Semester " + subject.getSemester() : "");
            binding.tvSubjectTeacher.setText(teacherSem);
        }
    }
}
