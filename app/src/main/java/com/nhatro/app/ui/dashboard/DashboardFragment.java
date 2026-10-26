package com.nhatro.app.ui.dashboard;

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
import com.nhatro.app.data.model.DashboardStats;
import com.nhatro.app.databinding.FragmentDashboardBinding;
import com.nhatro.app.ui.invoices.CreateInvoiceActivity;
import com.nhatro.app.ui.meter.MeterReadingListActivity;
import com.nhatro.app.ui.rooms.AddEditRoomActivity;
import com.nhatro.app.ui.services.ServiceListActivity;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.SharedPrefsHelper;
import com.nhatro.app.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupUserInfo();
        setupListeners();
        loadStats();
    }

    private void setupUserInfo() {
        SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(requireContext());
        binding.tvWelcomeUser.setText("Xin chào, " + prefs.getUserName());
        binding.tvUserRole.setText("Vai trò: " + (prefs.getUserRole().equals("OWNER") ? "Chủ nhà trọ (OWNER)" : "Quản lý (MANAGER)"));
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadStats);

        binding.btnQuickMeter.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MeterReadingListActivity.class))
        );

        binding.btnQuickInvoice.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateInvoiceActivity.class))
        );

        binding.btnQuickService.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ServiceListActivity.class))
        );

        binding.btnQuickAddRoom.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditRoomActivity.class))
        );
    }

    private void loadStats() {
        viewModel.getDashboardStats().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess() && resource.getData() != null) {
                DashboardStats stats = resource.getData();
                binding.tvAvailableRooms.setText(String.valueOf(stats.getAvailableRooms()));
                binding.tvOccupiedRooms.setText(String.valueOf(stats.getOccupiedRooms()));
                binding.tvTotalRevenue.setText(CurrencyUtils.formatVND(stats.getTotalRevenue()));
                binding.tvUnpaidDebt.setText(CurrencyUtils.formatVND(stats.getUnpaidDebt()));

                binding.tvTotalRooms.setText(String.format("• Tổng số phòng: %d phòng (%d bảo trì)", stats.getTotalRooms(), stats.getMaintenanceRooms()));
                binding.tvActiveContracts.setText(String.format("• Hợp đồng đang hiệu lực: %d hợp đồng", stats.getActiveContracts()));
                binding.tvTotalTenants.setText(String.format("• Tổng số khách thuê đang quản lý: %d người", stats.getTotalTenants()));
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
