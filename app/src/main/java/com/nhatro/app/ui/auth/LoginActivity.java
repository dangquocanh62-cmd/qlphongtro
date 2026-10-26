package com.nhatro.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.nhatro.app.R;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.databinding.ActivityLoginBinding;
import com.nhatro.app.ui.main.MainActivity;
import com.nhatro.app.utils.SharedPrefsHelper;
import com.nhatro.app.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        if (SharedPrefsHelper.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.btnConfigServer.setOnClickListener(v -> showConfigServerDialog());
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            binding.tilEmail.setError("Vui lòng nhập email");
            return;
        }
        binding.tilEmail.setError(null);

        if (password.isEmpty()) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        binding.tilPassword.setError(null);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        viewModel.login(email, password).observe(this, resource -> {
            if (resource == null) return;

            if (resource.isLoading()) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (resource.isSuccess()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (resource.isError()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
}
