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
    private com.nhatro.app.viewmodel.PaymentViewModel paymentViewModel;
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
        paymentViewModel = new ViewModelProvider(this).get(com.nhatro.app.viewmodel.PaymentViewModel.class);

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
        adapter.setOnInvoiceShareListener(invoice -> {
            com.nhatro.app.utils.InvoiceExportHelper.shareInvoice(requireContext(), invoice);
        });
        adapter.setOnInvoiceCallListener(invoice -> {
            if (invoice.getTenantPhone() != null && !invoice.getTenantPhone().trim().isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:" + invoice.getTenantPhone().trim()));
                startActivity(callIntent);
            } else {
                Toast.makeText(requireContext(), "Khách thuê chưa có số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInvoiceCollectListener(this::showQuickCollectDialog);

        binding.rvInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInvoices.setAdapter(adapter);
    }

    private void showQuickCollectDialog(com.nhatro.app.data.model.Invoice invoice) {
        if (invoice == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment, null);
        com.google.android.material.textfield.TextInputEditText etAmount = dialogView.findViewById(R.id.etPaymentAmount);
        android.widget.RadioButton rbTransfer = dialogView.findViewById(R.id.rbTransfer);

        // Pre-fill remaining debt amount for convenience
        etAmount.setText(String.valueOf(invoice.getRemainingAmount()));

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Thu tiền - Phòng " + invoice.getRoomCode())
                .setView(dialogView)
                .setPositiveButton("Ghi nhận", (dialog, which) -> {
                    String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "0";
                    long amount = amountStr.isEmpty() ? 0 : Long.parseLong(amountStr);

                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Số tiền thanh toán phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String method = rbTransfer.isChecked() ? "TRANSFER" : "CASH";
                    com.nhatro.app.data.model.Payment payment = new com.nhatro.app.data.model.Payment(invoice.getId(), amount, method, null);

                    paymentViewModel.createPayment(payment).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(requireContext(), "Ghi nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            viewModel.refresh();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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

        if (getArguments() != null && getArguments().containsKey("status_filter")) {
            String filter = getArguments().getString("status_filter");
            if ("UNPAID".equalsIgnoreCase(filter)) {
                binding.chipUnpaid.setChecked(true);
            } else if ("PAID".equalsIgnoreCase(filter)) {
                binding.chipPaid.setChecked(true);
            } else if ("PARTIAL".equalsIgnoreCase(filter)) {
                binding.chipPartial.setChecked(true);
            }
        }
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
