package com.nhatro.app.ui.services;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ActivityAddEditServiceBinding;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.viewmodel.ServiceViewModel;

public class AddEditServiceActivity extends BaseActivity {
    private ActivityAddEditServiceBinding binding;
    private ServiceViewModel viewModel;
    private ServiceItem existingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        existingService = (ServiceItem) getIntent().getSerializableExtra("service");
        if (existingService != null) {
            binding.toolbar.setTitle("Sửa Dịch Vụ");
            binding.etServiceName.setText(existingService.getName());
            binding.etServiceUnit.setText(existingService.getUnit());
            binding.etUnitPrice.setText(String.valueOf(existingService.getUnitPrice()));
            binding.cbIsMetered.setChecked(existingService.getIsMetered());
        }

        binding.btnSaveService.setOnClickListener(v -> saveService());
    }

    private void saveService() {
        String name = binding.etServiceName.getText() != null ? binding.etServiceName.getText().toString().trim() : "";
        String unit = binding.etServiceUnit.getText() != null ? binding.etServiceUnit.getText().toString().trim() : "";
        String priceStr = binding.etUnitPrice.getText() != null ? binding.etUnitPrice.getText().toString().trim() : "0";
        boolean isMetered = binding.cbIsMetered.isChecked();

        if (name.isEmpty() || unit.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và đơn vị tính của dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        long price = priceStr.isEmpty() ? 0 : Long.parseLong(priceStr);
        ServiceItem item = new ServiceItem(name, unit, price, isMetered);

        binding.btnSaveService.setEnabled(false);

        if (existingService != null) {
            viewModel.updateService(existingService.getId(), item).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Cập nhật dịch vụ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSaveService.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            viewModel.createService(item).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Thêm dịch vụ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSaveService.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
