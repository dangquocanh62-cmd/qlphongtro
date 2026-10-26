package com.nhatro.app.ui.invoices;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.databinding.ActivityCreateInvoiceBinding;
import com.nhatro.app.viewmodel.ContractViewModel;
import com.nhatro.app.viewmodel.InvoiceViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateInvoiceActivity extends AppCompatActivity {
    private ActivityCreateInvoiceBinding binding;
    private InvoiceViewModel invoiceViewModel;
    private ContractViewModel contractViewModel;
    private List<Contract> contractsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        invoiceViewModel = new ViewModelProvider(this).get(InvoiceViewModel.class);
        contractViewModel = new ViewModelProvider(this).get(ContractViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        Calendar now = Calendar.getInstance();
        binding.etMonth.setText(String.valueOf(now.get(Calendar.MONTH) + 1));
        binding.etYear.setText(String.valueOf(now.get(Calendar.YEAR)));

        loadActiveContracts();

        binding.btnAutoGenerate.setOnClickListener(v -> generateMonthlyInvoice());
    }

    private void loadActiveContracts() {
        contractViewModel.filterByStatus("ACTIVE");
        contractViewModel.getContracts().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                contractsList = resource.getData();
                List<String> labels = new ArrayList<>();
                for (Contract c : contractsList) {
                    labels.add("Phòng " + c.getRoomCode() + " - Khách: " + c.getTenantName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
                binding.spContract.setAdapter(adapter);
            }
        });
    }

    private void generateMonthlyInvoice() {
        int pos = binding.spContract.getSelectedItemPosition();
        if (pos < 0 || pos >= contractsList.size()) {
            Toast.makeText(this, "Vui lòng chọn một hợp đồng phòng để lập hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }

        Contract selectedContract = contractsList.get(pos);

        String monthStr = binding.etMonth.getText() != null ? binding.etMonth.getText().toString().trim() : "1";
        String yearStr = binding.etYear.getText() != null ? binding.etYear.getText().toString().trim() : "2024";
        String dueDate = binding.etDueDate.getText() != null ? binding.etDueDate.getText().toString().trim() : "";

        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        binding.btnAutoGenerate.setEnabled(false);

        invoiceViewModel.generateMonthlyInvoice(selectedContract.getId(), month, year, dueDate.isEmpty() ? null : dueDate)
                .observe(this, resource -> {
                    if (resource == null) return;
                    if (resource.isSuccess()) {
                        Toast.makeText(this, "Đã tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (resource.isError()) {
                        binding.btnAutoGenerate.setEnabled(true);
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
