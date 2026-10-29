package com.nhatro.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.nhatro.app.R;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.databinding.ActivityMainBinding;
import com.nhatro.app.ui.auth.LoginActivity;
import com.nhatro.app.ui.contracts.ContractListFragment;
import com.nhatro.app.ui.dashboard.DashboardFragment;
import com.nhatro.app.ui.invoices.InvoiceListFragment;
import com.nhatro.app.ui.meter.MeterReadingListActivity;
import com.nhatro.app.ui.rooms.RoomListFragment;
import com.nhatro.app.ui.services.ServiceListActivity;
import com.nhatro.app.ui.settings.SettingsActivity;
import com.nhatro.app.ui.tenants.TenantListFragment;
import com.nhatro.app.utils.SharedPrefsHelper;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), "Tổng Quan Nhà Trọ");
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment(), "Tổng Quan Nhà Trọ");
                return true;
            } else if (id == R.id.nav_rooms) {
                loadFragment(new RoomListFragment(), "Quản Lý Phòng Trọ");
                return true;
            } else if (id == R.id.nav_tenants) {
                loadFragment(new TenantListFragment(), "Quản Lý Khách Thuê");
                return true;
            } else if (id == R.id.nav_contracts) {
                loadFragment(new ContractListFragment(), "Quản Lý Hợp Đồng");
                return true;
            } else if (id == R.id.nav_invoices) {
                loadFragment(new InvoiceListFragment(), "Hóa Đơn & Thu Chi");
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment, String title) {
        if (binding.toolbar != null) {
            binding.toolbar.setTitle(title);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateToContracts(boolean filterExpiring) {
        ContractListFragment fragment = new ContractListFragment();
        if (filterExpiring) {
            Bundle args = new Bundle();
            args.putBoolean("filter_expiring", true);
            fragment.setArguments(args);
        }
        binding.bottomNavigation.setSelectedItemId(R.id.nav_contracts);
        loadFragment(fragment, "Quản Lý Hợp Đồng");
    }

    public void navigateToInvoices(String statusFilter) {
        InvoiceListFragment fragment = new InvoiceListFragment();
        if (statusFilter != null) {
            Bundle args = new Bundle();
            args.putString("status_filter", statusFilter);
            fragment.setArguments(args);
        }
        binding.bottomNavigation.setSelectedItemId(R.id.nav_invoices);
        loadFragment(fragment, "Hóa Đơn & Thu Chi");
    }

    public void navigateToRooms(String statusFilter) {
        RoomListFragment fragment = new RoomListFragment();
        if (statusFilter != null) {
            Bundle args = new Bundle();
            args.putString("status_filter", statusFilter);
            fragment.setArguments(args);
        }
        binding.bottomNavigation.setSelectedItemId(R.id.nav_rooms);
        loadFragment(fragment, "Quản Lý Phòng Trọ");
    }

    public void navigateToTenants() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_tenants);
        loadFragment(new TenantListFragment(), "Quản Lý Khách Thuê");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_meters) {
            startActivity(new Intent(this, MeterReadingListActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfigServerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_config_server, null);
        TextInputEditText etServerUrl = dialogView.findViewById(R.id.etServerUrl);
        etServerUrl.setText(SharedPrefsHelper.getInstance(this).getServerUrl());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu lại", (dialog, which) -> {
                    String url = etServerUrl.getText() != null ? etServerUrl.getText().toString().trim() : "";
                    if (!url.isEmpty()) {
                        SharedPrefsHelper.getInstance(this).saveServerUrl(url);
                        ApiClient.resetClient();
                        Toast.makeText(this, "Đã lưu URL: " + url, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    SharedPrefsHelper.getInstance(this).clear();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
