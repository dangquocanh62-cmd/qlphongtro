package com.nhatro.app.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhatro.app.databinding.FragmentTenantListBinding;
import com.nhatro.app.ui.adapters.TenantAdapter;
import com.nhatro.app.viewmodel.TenantViewModel;

public class TenantListFragment extends Fragment {
    private FragmentTenantListBinding binding;
    private TenantViewModel viewModel;
    private TenantAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTenantListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeTenants();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddTenant.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTenantActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new TenantAdapter();
        adapter.setOnTenantClickListener(tenant -> {
            String[] options = {"Sửa thông tin", "Xóa khách thuê"};
            new AlertDialog.Builder(requireContext())
                    .setTitle(tenant.getFullName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(requireContext(), AddEditTenantActivity.class);
                            intent.putExtra("tenant", tenant);
                            startActivity(intent);
                        } else if (which == 1) {
                            deleteTenant(tenant.getId(), tenant.getFullName());
                        }
                    })
                    .show();
        });

        binding.rvTenants.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTenants.setAdapter(adapter);
    }

    private void deleteTenant(Long id, String name) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa khách " + name + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteTenant(id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource == null) return;
                        if (resource.isSuccess()) {
                            Toast.makeText(requireContext(), "Đã xóa khách!", Toast.LENGTH_SHORT).show();
                            viewModel.refresh();
                        } else if (resource.isError()) {
                            Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupSearch() {
        binding.etSearchTenant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeTenants() {
        viewModel.getTenants().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setTenants(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setTenants(null);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
