package com.nhatro.app.ui.invoices;

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
import com.nhatro.app.databinding.FragmentInvoiceListBinding;
import com.nhatro.app.ui.adapters.InvoiceAdapter;
import com.nhatro.app.viewmodel.InvoiceViewModel;

public class InvoiceListFragment extends Fragment {
    private FragmentInvoiceListBinding binding;
    private InvoiceViewModel viewModel;
    private InvoiceAdapter adapter;
    private String currentStatusFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInvoiceListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InvoiceViewModel.class);

        setupRecyclerView();
        setupFilters();
        observeInvoices();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabCreateInvoice.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateInvoiceActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter();
        adapter.setOnInvoiceClickListener(invoice -> {
            Intent intent = new Intent(requireContext(), InvoiceDetailActivity.class);
            intent.putExtra("invoice_id", invoice.getId());
            startActivity(intent);
        });

        binding.rvInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInvoices.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupInvoiceStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAllInvoices) {
                currentStatusFilter = null;
            } else if (checkedId == R.id.chipUnpaid) {
                currentStatusFilter = "UNPAID";
            } else if (checkedId == R.id.chipPartial) {
                currentStatusFilter = "PARTIAL";
            } else if (checkedId == R.id.chipPaid) {
                currentStatusFilter = "PAID";
            }
            viewModel.filter(currentStatusFilter, null, null, null);
        });
    }

    private void observeInvoices() {
        viewModel.getInvoices().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setInvoices(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setInvoices(null);
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
