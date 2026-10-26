package com.nhatro.app.ui.services;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhatro.app.databinding.ActivityServiceListBinding;
import com.nhatro.app.ui.adapters.ServiceAdapter;
import com.nhatro.app.viewmodel.ServiceViewModel;

public class ServiceListActivity extends AppCompatActivity {
    private ActivityServiceListBinding binding;
    private ServiceViewModel viewModel;
    private ServiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        observeServices();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddService.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditServiceActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new ServiceAdapter();
        adapter.setOnServiceClickListener(service -> {
            String[] options = {"Sửa dịch vụ", "Xóa dịch vụ"};
            new AlertDialog.Builder(this)
                    .setTitle(service.getName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(this, AddEditServiceActivity.class);
                            intent.putExtra("service", service);
                            startActivity(intent);
                        } else if (which == 1) {
                            deleteService(service.getId(), service.getName());
                        }
                    })
                    .show();
        });

        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(adapter);
    }

    private void deleteService(Long id, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa dịch vụ " + name + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteService(id).observe(this, resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(this, "Đã xóa dịch vụ!", Toast.LENGTH_SHORT).show();
                            viewModel.refresh();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeServices() {
        viewModel.getServices().observe(this, resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setServices(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setServices(null);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            } else if (resource.isError()) {
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
