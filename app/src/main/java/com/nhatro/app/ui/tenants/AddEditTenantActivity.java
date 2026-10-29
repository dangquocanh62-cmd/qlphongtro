package com.nhatro.app.ui.tenants;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ActivityAddEditTenantBinding;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.utils.DatePickerHelper;
import com.nhatro.app.viewmodel.TenantViewModel;

public class AddEditTenantActivity extends BaseActivity {
    private ActivityAddEditTenantBinding binding;
    private TenantViewModel viewModel;
    private Tenant existingTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditTenantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        DatePickerHelper.attachDatePicker(getSupportFragmentManager(), binding.etBirthDate, binding.tilBirthDate, "Chọn ngày sinh");

        existingTenant = (Tenant) getIntent().getSerializableExtra("tenant");
        if (existingTenant != null) {
            binding.toolbar.setTitle("Sửa Thông Tin Khách");
            binding.etFullName.setText(existingTenant.getFullName());
            binding.etPhone.setText(existingTenant.getPhone());
            binding.etIdCard.setText(existingTenant.getIdCard());
            binding.etBirthDate.setText(existingTenant.getBirthDate());
            binding.etHometown.setText(existingTenant.getHometown());
        }

        binding.btnSaveTenant.setOnClickListener(v -> saveTenant());
    }

    private void saveTenant() {
        String name = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String idCard = binding.etIdCard.getText() != null ? binding.etIdCard.getText().toString().trim() : "";
        String birthDate = binding.etBirthDate.getText() != null ? binding.etBirthDate.getText().toString().trim() : "";
        String hometown = binding.etHometown.getText() != null ? binding.etHometown.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ và tên khách thuê", Toast.LENGTH_SHORT).show();
            return;
        }

        Tenant tenant = new Tenant(name, phone, idCard, birthDate, hometown);
        binding.btnSaveTenant.setEnabled(false);

        if (existingTenant != null) {
            viewModel.updateTenant(existingTenant.getId(), tenant).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Cập nhật khách thuê thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSaveTenant.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            viewModel.createTenant(tenant).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Thêm khách thuê thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSaveTenant.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
