package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendance_system.R;
import com.example.attendance_system.databinding.ItemStudentBinding;
import com.example.attendance_system.models.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onQRClick(Student student);
        void onDeleteClick(Student student);
        void onItemClick(Student student);
    }

    private List<Student> studentList = new ArrayList<>();
    private List<Student> fullList = new ArrayList<>();
    private final OnStudentClickListener listener;

    public StudentAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void setStudentList(List<Student> list) {
        this.fullList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.studentList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            this.studentList = new ArrayList<>(fullList);
        } else {
            String lower = query.toLowerCase().trim();
            List<Student> filtered = new ArrayList<>();
            for (Student s : fullList) {
                String name = s.getName() != null ? s.getName().toLowerCase() : "";
                String roll = s.getRollNo() != null ? s.getRollNo().toLowerCase() : "";
                String dept = s.getDepartmentName() != null ? s.getDepartmentName().toLowerCase() : "";
                if (name.contains(lower) || roll.contains(lower) || dept.contains(lower)) {
                    filtered.add(s);
                }
            }
            this.studentList = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(studentList.get(position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        public StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Student student) {
            binding.tvStudentName.setText(student.getName());
            String rollDept = "Roll No: " + student.getRollNo() + " • " + student.getDepartmentName() + " Sem " + student.getSemester();
            binding.tvRollAndDept.setText(rollDept);
            binding.tvStudentEmail.setText(student.getEmail());

            if (student.getProfileImageUrl() != null && !student.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(student.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person)
                        .into(binding.imgStudent);
            } else {
                binding.imgStudent.setImageResource(R.drawable.ic_person);
            }

            binding.btnQR.setOnClickListener(v -> {
                if (listener != null) listener.onQRClick(student);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(student);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(student);
            });
        }
    }
}
