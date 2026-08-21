package com.example.attendance_system.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.NotificationAdapter;
import com.example.attendance_system.databinding.ActivityNotificationsBinding;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.viewmodel.NotificationViewModel;
import com.example.attendance_system.viewmodel.StudentViewModel;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private NotificationViewModel notificationViewModel;
    private StudentViewModel studentViewModel;
    private NotificationAdapter adapter;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, null)) {
            return;
        }

        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        initViewModels();

        String uid = SharedPrefManager.getInstance(this).getUserUid();
        studentViewModel.fetchStudentByUid(uid);

        observeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, null);
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMarkAllRead.setOnClickListener(v -> {
            if (currentStudent != null) {
                notificationViewModel.markAllAsRead(currentStudent.getUid());
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(item -> {
            if (currentStudent != null && !item.isRead()) {
                notificationViewModel.markAsRead(item.getId(), currentStudent.getUid());
            }
        });
        binding.rvNotifications.setAdapter(adapter);
    }

    private void initViewModels() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);
    }

    private void observeData() {
        studentViewModel.getStudentLiveData().observe(this, student -> {
            if (student != null) {
                currentStudent = student;
                notificationViewModel.fetchNotifications(student.getUid());
            }
        });

        notificationViewModel.getNotificationsLiveData().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                binding.tvNoNotifications.setVisibility(View.VISIBLE);
                binding.rvNotifications.setVisibility(View.GONE);
            } else {
                binding.tvNoNotifications.setVisibility(View.GONE);
                binding.rvNotifications.setVisibility(View.VISIBLE);
                adapter.setList(list);
            }
        });
    }
}
