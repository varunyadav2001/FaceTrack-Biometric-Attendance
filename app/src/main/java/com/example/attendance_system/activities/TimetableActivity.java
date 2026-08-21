package com.example.attendance_system.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.TimetableAdapter;
import com.example.attendance_system.databinding.ActivityTimetableBinding;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.utils.SharedPrefManager;
import com.example.attendance_system.viewmodel.StudentViewModel;
import com.example.attendance_system.viewmodel.TimetableViewModel;
import com.google.android.material.tabs.TabLayout;

public class TimetableActivity extends AppCompatActivity {

    private ActivityTimetableBinding binding;
    private TimetableViewModel timetableViewModel;
    private StudentViewModel studentViewModel;
    private TimetableAdapter adapter;
    private Student currentStudent;

    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.attendance_system.helpers.AuthGuardHelper.checkAndEnforce(this, null)) {
            return;
        }

        binding = ActivityTimetableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupTabs();
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
    }

    private void setupRecyclerView() {
        binding.rvTimetable.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimetableAdapter();
        binding.rvTimetable.setAdapter(adapter);
    }

    private void setupTabs() {
        for (String day : days) {
            binding.tabDays.addTab(binding.tabDays.newTab().setText(day));
        }

        binding.tabDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fetchTimetable(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initViewModels() {
        timetableViewModel = new ViewModelProvider(this).get(TimetableViewModel.class);
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);
    }

    private void observeData() {
        studentViewModel.getStudentLiveData().observe(this, student -> {
            if (student != null) {
                currentStudent = student;
                fetchTimetable(binding.tabDays.getSelectedTabPosition());
            }
        });

        timetableViewModel.getTimetableLiveData().observe(this, list -> {
            adapter.setList(list);
        });
    }

    private void fetchTimetable(int dayIndex) {
        if (currentStudent != null) {
            timetableViewModel.fetchTimetableForDay(currentStudent.getDepartmentName(), currentStudent.getSection(), days[dayIndex]);
        }
    }
}
