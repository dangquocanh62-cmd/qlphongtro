package com.nhatro.app.ui.contracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhatro.app.R;
import com.nhatro.app.databinding.FragmentContractListBinding;
import com.nhatro.app.ui.adapters.ContractAdapter;
import com.nhatro.app.viewmodel.ContractViewModel;

public class ContractListFragment extends Fragment {
    private FragmentContractListBinding binding;
    private ContractViewModel viewModel;
    private ContractAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContractListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ContractViewModel.class);

        setupRecyclerView();
        setupFilters();
        observeContracts();

        if (getArguments() != null && getArguments().getBoolean("filter_expiring", false)) {
            binding.chipExpiringContracts.setChecked(true);
            viewModel.filterExpiring();
        }

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddContract.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditContractActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new ContractAdapter();
        adapter.setOnContractClickListener(contract -> {
            Intent intent = new Intent(requireContext(), ContractDetailActivity.class);
            intent.putExtra("contract_id", contract.getId());
            startActivity(intent);
        });
        adapter.setOnContractCallListener(contract -> {
            if (contract.getTenantPhone() != null && !contract.getTenantPhone().trim().isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:" + contract.getTenantPhone().trim()));
                startActivity(callIntent);
            } else {
                Toast.makeText(requireContext(), "Khách thuê chưa có số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvContracts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvContracts.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupContractStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipActiveContracts) {
                viewModel.filterByStatus("ACTIVE");
            } else if (checkedId == R.id.chipExpiringContracts) {
                viewModel.filterExpiring();
            } else if (checkedId == R.id.chipEndedContracts) {
                viewModel.filterByStatus("ENDED");
            } else {
                viewModel.filterByStatus(null);
            }
        });
    }

    private void observeContracts() {
        viewModel.getContracts().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setContracts(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setContracts(null);
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
