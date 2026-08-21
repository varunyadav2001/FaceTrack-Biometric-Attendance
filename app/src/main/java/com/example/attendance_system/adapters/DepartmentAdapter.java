package com.example.attendance_system.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance_system.databinding.ItemDepartmentBinding;
import com.example.attendance_system.models.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    private List<Department> list = new ArrayList<>();

    public void setDepartmentList(List<Department> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepartmentBinding binding = ItemDepartmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DepartmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepartmentBinding binding;

        public DepartmentViewHolder(ItemDepartmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Department dept) {
            binding.tvDeptCode.setText(dept.getCode());
            binding.tvDeptName.setText(dept.getName());
            binding.tvDeptDesc.setText(dept.getDescription());
        }
    }
}
