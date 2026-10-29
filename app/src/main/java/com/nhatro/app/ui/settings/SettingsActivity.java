package com.nhatro.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.nhatro.app.R;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.databinding.ActivitySettingsBinding;
import com.nhatro.app.ui.auth.LoginActivity;
import com.nhatro.app.ui.meter.MeterReadingListActivity;
import com.nhatro.app.ui.services.ServiceListActivity;
import com.nhatro.app.utils.Constants;
import com.nhatro.app.utils.SharedPrefsHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = SharedPrefsHelper.getInstance(this);

        setupToolbar();
        loadProfileData();
        loadBankData();
        loadServerConfig();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadProfileData() {
        String name = prefs.getUserName();
        String role = prefs.getUserRole();
        String email = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                .getString(Constants.KEY_USER_EMAIL, "admin@nhatro.vn");

        binding.tvProfileName.setText(name);
        binding.tvProfileEmail.setText(email);
        binding.tvProfileRole.setText("OWNER".equalsIgnoreCase(role) ? "Chủ nhà / Quản trị viên" : role);

        if (name != null && !name.trim().isEmpty()) {
            binding.tvAvatarInitial.setText(String.valueOf(name.trim().charAt(0)).toUpperCase());
        } else {
            binding.tvAvatarInitial.setText("C");
        }
    }

    private void loadBankData() {
        String bankName = prefs.getBankName();
        String bankAccount = prefs.getBankAccount();
        String bankOwner = prefs.getBankOwner();

        if (bankName != null && !bankName.isEmpty()) {
            binding.tvBankName.setText(bankName);
        } else {
            binding.tvBankName.setText("Chưa cài đặt");
        }

        if (bankAccount != null && !bankAccount.isEmpty()) {
            binding.tvBankAccount.setText(bankAccount);
        } else {
            binding.tvBankAccount.setText("Chưa cài đặt");
        }

        if (bankOwner != null && !bankOwner.isEmpty()) {
            binding.tvBankOwner.setText(bankOwner);
        } else {
            binding.tvBankOwner.setText(prefs.getUserName());
        }
    }

    private void loadServerConfig() {
        binding.tvCurrentServerUrl.setText(prefs.getServerUrl());
    }

    private void setupClickListeners() {
        // Chỉnh sửa tài khoản ngân hàng VietQR
        binding.btnEditBank.setOnClickListener(v -> showEditBankDialog());

        // Quản lý dịch vụ
        binding.layoutManageServices.setOnClickListener(v -> {
            startActivity(new Intent(this, ServiceListActivity.class));
        });

        // Ghi điện nước
        binding.layoutManageMeters.setOnClickListener(v -> {
            startActivity(new Intent(this, MeterReadingListActivity.class));
        });

        // Cấu hình máy chủ
        binding.layoutConfigServer.setOnClickListener(v -> showConfigServerDialog());

        // Đăng xuất
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditBankDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_bank_info, null);
        TextInputEditText etBankName = dialogView.findViewById(R.id.etBankName);
        TextInputEditText etBankAccount = dialogView.findViewById(R.id.etBankAccount);
        TextInputEditText etBankOwner = dialogView.findViewById(R.id.etBankOwner);

        etBankName.setText(prefs.getBankName());
        etBankAccount.setText(prefs.getBankAccount());
        etBankOwner.setText(prefs.getBankOwner().isEmpty() ? prefs.getUserName().toUpperCase() : prefs.getBankOwner());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu tài khoản", (dialog, which) -> {
                    String bName = etBankName.getText() != null ? etBankName.getText().toString().trim() : "";
                    String bAccount = etBankAccount.getText() != null ? etBankAccount.getText().toString().trim() : "";
                    String bOwner = etBankOwner.getText() != null ? etBankOwner.getText().toString().trim().toUpperCase() : "";

                    prefs.saveBankInfo(bName, bAccount, bOwner);
                    loadBankData();
                    Toast.makeText(this, "Đã lưu tài khoản nhận tiền VietQR!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showConfigServerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_config_server, null);
        TextInputEditText etServerUrl = dialogView.findViewById(R.id.etServerUrl);
        etServerUrl.setText(prefs.getServerUrl());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu lại", (dialog, which) -> {
                    String url = etServerUrl.getText() != null ? etServerUrl.getText().toString().trim() : "";
                    if (!url.isEmpty()) {
                        prefs.saveServerUrl(url);
                        ApiClient.resetClient();
                        loadServerConfig();
                        Toast.makeText(this, "Đã cập nhật máy chủ: " + url, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất tài khoản")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    prefs.clear();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
