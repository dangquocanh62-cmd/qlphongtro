package com.nhatro.app.ui.dashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.nhatro.app.R;
import com.nhatro.app.data.model.DashboardStats;
import com.nhatro.app.data.model.MonthlyRevenue;
import com.nhatro.app.databinding.FragmentDashboardBinding;
import com.nhatro.app.ui.contracts.AddEditContractActivity;
import com.nhatro.app.ui.invoices.CreateInvoiceActivity;
import com.nhatro.app.ui.main.MainActivity;
import com.nhatro.app.ui.meter.MeterReadingListActivity;
import com.nhatro.app.ui.services.ServiceListActivity;
import com.nhatro.app.ui.tenants.AddEditTenantActivity;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.utils.SharedPrefsHelper;
import com.nhatro.app.viewmodel.DashboardViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private final Calendar selectedCalendar = Calendar.getInstance();
    private int currentMonthsRange = 6;

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
        observeViewModel();

        updatePeriodDisplay();
        int month = selectedCalendar.get(Calendar.MONTH) + 1;
        int year = selectedCalendar.get(Calendar.YEAR);
        viewModel.loadStats(month, year);
        viewModel.loadRevenue(currentMonthsRange);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && binding != null) {
            setupUserInfo();
            viewModel.refresh();
        }
    }

    private void setupUserInfo() {
        if (!isAdded() || binding == null) return;
        SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(requireContext());
        binding.tvWelcomeUser.setText("Xin chào, " + prefs.getUserName());
        // Hiển thị ngày hôm nay tiếng Việt (ví dụ: "Thứ Tư, 28/10/2026") thay cho dòng (OWNER)
        binding.tvUserRole.setText(DateUtils.getTodayFullVN());
    }

    private void updatePeriodDisplay() {
        if (binding == null) return;
        int month = selectedCalendar.get(Calendar.MONTH) + 1;
        int year = selectedCalendar.get(Calendar.YEAR);
        String periodText = String.format(Locale.getDefault(), "Tháng %d/%d", month, year);
        binding.tvSelectedPeriod.setText(periodText);
        binding.tvRevenuePeriod.setText("Kỳ " + periodText);
        binding.tvDebtPeriod.setText("Kỳ " + periodText);
    }

    private void observeViewModel() {
        // Observe dashboard stats exactly once
        viewModel.getStats().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess() && resource.getData() != null) {
                displayStats(resource.getData());
            } else if (resource.isError()) {
                String message = resource.getMessage() != null ? resource.getMessage() : "Lỗi kết nối máy chủ";
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                        .setAction("Thử lại", v -> viewModel.refresh())
                        .setActionTextColor(Color.parseColor("#F59E0B"))
                        .show();
            }
        });

        // Observe monthly revenue chart data exactly once
        viewModel.getMonthlyRevenue().observe(getViewLifecycleOwner(), resource -> {
            if (!isAdded() || binding == null || resource == null) return;
            if (resource.isLoading()) {
                binding.pbChartLoading.setVisibility(View.VISIBLE);
            } else if (resource.isSuccess() && resource.getData() != null) {
                binding.pbChartLoading.setVisibility(View.GONE);
                displayRevenueChart(resource.getData());
            } else if (resource.isError()) {
                binding.pbChartLoading.setVisibility(View.GONE);
                binding.tvChartEmpty.setVisibility(View.VISIBLE);
                binding.barChartRevenue.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setupListeners() {
        // Swipe to Refresh
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        // Month Picker Navigation (‹ ›)
        binding.btnPrevMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, -1);
            updatePeriodDisplay();
            viewModel.loadStats(selectedCalendar.get(Calendar.MONTH) + 1, selectedCalendar.get(Calendar.YEAR));
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, 1);
            updatePeriodDisplay();
            viewModel.loadStats(selectedCalendar.get(Calendar.MONTH) + 1, selectedCalendar.get(Calendar.YEAR));
        });

        binding.layoutPeriodCenter.setOnClickListener(v -> showDatePickerDialog());

        // Clickable Financial Cards
        binding.cardCollectedRevenue.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToInvoices("PAID");
            }
        });

        binding.cardUnpaidDebt.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToInvoices("UNPAID");
            }
        });

        // Clickable Occupancy Card -> Filter AVAILABLE
        binding.cardRoomOccupancy.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRooms("AVAILABLE");
            }
        });

        // 4 Mini Metric Cards
        binding.cardStatRooms.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRooms(null);
            }
        });

        binding.cardStatMaintenance.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRooms("MAINTENANCE");
            }
        });

        binding.cardStatContracts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToContracts(false);
            }
        });

        binding.cardStatTenants.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTenants();
            }
        });

        // Attention Items Clicks
        binding.layoutAttentionOverdue.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToInvoices("UNPAID");
            }
        });

        binding.layoutAttentionContracts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToContracts(true);
            }
        });

        binding.layoutAttentionMeters.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MeterReadingListActivity.class))
        );

        // Monthly Workflow Quick Actions
        binding.btnQuickMeter.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MeterReadingListActivity.class))
        );

        binding.btnQuickInvoice.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateInvoiceActivity.class))
        );

        binding.btnQuickCollectMoney.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToInvoices("UNPAID");
            }
        });

        binding.btnQuickContract.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditContractActivity.class))
        );

        binding.btnQuickTenant.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTenantActivity.class))
        );

        binding.btnQuickService.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ServiceListActivity.class))
        );

        // Revenue Chart Range Toggle
        binding.toggleMonthRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentMonthsRange = (checkedId == R.id.btnRange6Months) ? 6 : 12;
                viewModel.loadRevenue(currentMonthsRange);
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    updatePeriodDisplay();
                    viewModel.loadStats(month + 1, year);
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                1
        );
        dialog.setTitle("Chọn kỳ tháng / năm");
        dialog.show();
    }

    private void displayStats(DashboardStats stats) {
        if (binding == null || stats == null) return;

        // 1. Period Labels
        int month = (stats.getMonth() > 0 && stats.getMonth() <= 12) ? stats.getMonth() : (selectedCalendar.get(Calendar.MONTH) + 1);
        int year = (stats.getYear() > 1900) ? stats.getYear() : selectedCalendar.get(Calendar.YEAR);
        selectedCalendar.set(Calendar.MONTH, month - 1);
        selectedCalendar.set(Calendar.YEAR, year);

        String periodText = String.format(Locale.getDefault(), "Tháng %d/%d", month, year);
        binding.tvSelectedPeriod.setText(periodText);
        binding.tvRevenuePeriod.setText("Kỳ " + periodText);
        binding.tvDebtPeriod.setText("Kỳ " + periodText);

        // 2. Financial Figures
        binding.tvTotalRevenue.setText(CurrencyUtils.formatVND(stats.getTotalRevenue()));
        binding.tvUnpaidDebt.setText(CurrencyUtils.formatVND(stats.getUnpaidDebt()));

        // 3. Occupancy Ratio
        int total = stats.getTotalRooms();
        int occupied = stats.getOccupiedRooms();
        int available = stats.getAvailableRooms();
        int maintenance = stats.getMaintenanceRooms();
        int percent = total > 0 ? (int) Math.round((occupied * 100.0) / total) : 0;

        binding.tvOccupancyRatio.setText(String.format(Locale.getDefault(), "%d/%d phòng đang thuê (%d%%)", occupied, total, percent));
        binding.pbOccupancy.setProgress(percent);
        binding.tvOccupancyBreakdown.setText(String.format(Locale.getDefault(), "%d phòng trống · %d phòng bảo trì", available, maintenance));

        // 4. 4 Mini Overview Cards
        binding.tvStatRooms.setText(String.valueOf(total));
        binding.tvStatMaintenance.setText(String.valueOf(maintenance));
        binding.tvStatContracts.setText(String.valueOf(stats.getActiveContracts()));
        binding.tvStatTenants.setText(String.valueOf(stats.getTotalTenants()));

        // 5. Attention Banner (Cần chú ý)
        int overdue = stats.getOverdueInvoicesCount();
        int expiring = stats.getExpiringContractsCount();
        int missingMeters = stats.getMissingMeterRoomsCount();
        int totalAttention = (overdue > 0 ? 1 : 0) + (expiring > 0 ? 1 : 0) + (missingMeters > 0 ? 1 : 0);

        if (totalAttention > 0) {
            binding.cardAttention.setVisibility(View.VISIBLE);
            binding.tvAttentionHeaderTitle.setText(String.format(Locale.getDefault(), "Cần chú ý (%d việc phát sinh)", overdue + expiring + missingMeters));

            // Overdue Invoices
            if (overdue > 0) {
                binding.layoutAttentionOverdue.setVisibility(View.VISIBLE);
                binding.tvAttentionOverdue.setText(String.format(Locale.getDefault(), "%d hóa đơn quá hạn chưa thu", overdue));
            } else {
                binding.layoutAttentionOverdue.setVisibility(View.GONE);
            }

            // Expiring Contracts
            if (expiring > 0) {
                binding.layoutAttentionContracts.setVisibility(View.VISIBLE);
                binding.tvAttentionContracts.setText(String.format(Locale.getDefault(), "%d hợp đồng hết hạn trong 30 ngày", expiring));
            } else {
                binding.layoutAttentionContracts.setVisibility(View.GONE);
            }

            // Missing Meter Readings
            if (missingMeters > 0) {
                binding.layoutAttentionMeters.setVisibility(View.VISIBLE);
                binding.tvAttentionMeters.setText(String.format(Locale.getDefault(), "%d phòng chưa chốt điện nước T%d", missingMeters, month));
            } else {
                binding.layoutAttentionMeters.setVisibility(View.GONE);
            }
        } else {
            binding.cardAttention.setVisibility(View.GONE);
        }
    }

    private void displayRevenueChart(List<MonthlyRevenue> dataList) {
        if (binding == null || dataList == null || dataList.isEmpty()) {
            if (binding != null) {
                binding.tvChartEmpty.setVisibility(View.VISIBLE);
                binding.barChartRevenue.setVisibility(View.INVISIBLE);
            }
            return;
        }

        binding.tvChartEmpty.setVisibility(View.GONE);
        binding.barChartRevenue.setVisibility(View.VISIBLE);

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            MonthlyRevenue item = dataList.get(i);
            float valueInMillions = (float) (item.getRevenue() / 1_000_000.0);
            entries.add(new BarEntry(i, valueInMillions));
            labels.add(item.getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.parseColor("#2563EB"));
        dataSet.setValueTextColor(Color.parseColor("#1E3A8A"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value <= 0) return "0";
                return String.format(Locale.US, "%.1ftr", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.55f);

        BarChart chart = binding.barChartRevenue;
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setExtraBottomOffset(6f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        leftAxis.setTextColor(Color.parseColor("#64748B"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f tr", value);
            }
        });

        chart.getAxisRight().setEnabled(false);

        chart.animateY(800);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
