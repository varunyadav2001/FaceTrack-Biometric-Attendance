package com.example.attendance_system.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.DepartmentAdapter;
import com.example.attendance_system.databinding.FragmentManageDepartmentsBinding;
import com.example.attendance_system.viewmodel.DepartmentViewModel;

public class ManageDepartmentsFragment extends Fragment {

    private FragmentManageDepartmentsBinding binding;
    private DepartmentViewModel viewModel;
    private DepartmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageDepartmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DepartmentViewModel.class);
        binding.rvDepartments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DepartmentAdapter();
        binding.rvDepartments.setAdapter(adapter);

        viewModel.getDepartmentListLiveData().observe(getViewLifecycleOwner(), depts -> {
            if (depts != null) adapter.setDepartmentList(depts);
        });

        viewModel.fetchDepartments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
