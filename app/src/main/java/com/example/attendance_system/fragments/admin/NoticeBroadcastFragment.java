package com.example.attendance_system.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendance_system.adapters.NoticeAdapter;
import com.example.attendance_system.databinding.FragmentNoticeBroadcastBinding;
import com.example.attendance_system.models.Notice;
import com.example.attendance_system.viewmodel.NoticeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class NoticeBroadcastFragment extends Fragment {

    private FragmentNoticeBroadcastBinding binding;
    private NoticeViewModel viewModel;
    private NoticeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNoticeBroadcastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NoticeViewModel.class);
        binding.rvNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoticeAdapter();
        binding.rvNotices.setAdapter(adapter);

        binding.fabAddNotice.setOnClickListener(v -> showAddNoticeDialog());

        viewModel.getNoticeListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) adapter.setNoticeList(list);
        });

        viewModel.fetchNotices();
    }

    private void showAddNoticeDialog() {
        EditText etTitle = new EditText(getContext());
        etTitle.setHint("Notice Title (e.g. Exam Schedule)");
        EditText etMsg = new EditText(getContext());
        etMsg.setHint("Notice Message Body...");

        android.widget.LinearLayout container = new android.widget.LinearLayout(getContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(etTitle);
        container.addView(etMsg);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Broadcast New Notice")
                .setView(container)
                .setPositiveButton("Post Notice", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String msg = etMsg.getText().toString().trim();
                    if (!title.isEmpty() && !msg.isEmpty()) {
                        Notice n = new Notice("", title, msg, "ALL", "GENERAL", "Admin Office");
                        viewModel.addNotice(n);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
