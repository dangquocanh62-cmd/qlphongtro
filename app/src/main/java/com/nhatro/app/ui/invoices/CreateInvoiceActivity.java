package com.nhatro.app.ui.invoices;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.data.model.CreateInvoiceRequest;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.data.model.InvoiceItem;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ActivityCreateInvoiceBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DatePickerHelper;
import com.nhatro.app.viewmodel.ContractViewModel;
import com.nhatro.app.viewmodel.InvoiceViewModel;
import com.nhatro.app.viewmodel.MeterReadingViewModel;
import com.nhatro.app.viewmodel.ServiceViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateInvoiceActivity extends AppCompatActivity {
    private ActivityCreateInvoiceBinding binding;
    private InvoiceViewModel invoiceViewModel;
    private ContractViewModel contractViewModel;
    private ServiceViewModel serviceViewModel;
    private MeterReadingViewModel meterReadingViewModel;

    private List<Contract> contractsList = new ArrayList<>();
    private List<ServiceItem> allServicesList = new ArrayList<>();
    private final List<ServiceRowHolder> serviceRowHolders = new ArrayList<>();
    private final List<CustomFeeItem> customFeesList = new ArrayList<>();

    private long currentRentPrice = 0;
    private long currentElectricCost = 0;
    private long currentWaterCost = 0;
    private String electricDescription = "";
    private String waterDescription = "";
    private double electricUsage = 0;
    private double waterUsage = 0;
    private long electricUnitPrice = 3500;
    private long waterUnitPrice = 25000;
    private int selectedContractIndex = -1;
    private Invoice existingInvoice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        invoiceViewModel = new ViewModelProvider(this).get(InvoiceViewModel.class);
        contractViewModel = new ViewModelProvider(this).get(ContractViewModel.class);
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        meterReadingViewModel = new ViewModelProvider(this).get(MeterReadingViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        Calendar now = Calendar.getInstance();
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curYear = now.get(Calendar.YEAR);
        binding.etMonth.setText(String.valueOf(curMonth));
        binding.etYear.setText(String.valueOf(curYear));

        int nextMonth = curMonth == 12 ? 1 : curMonth + 1;
        int nextYear = curMonth == 12 ? curYear + 1 : curYear;
        binding.etDueDate.setText(String.format("%04d-%02d-05", nextYear, nextMonth));

        DatePickerHelper.setupMonthDropdown(this, binding.etMonth);
        DatePickerHelper.setupYearDropdown(this, binding.etYear);
        DatePickerHelper.attachDatePicker(getSupportFragmentManager(), binding.etDueDate, binding.tilDueDate, "Chọn hạn nộp tiền");

        setupListeners();
        setupMeterReadingObserver();
        loadServices();
        loadActiveContracts();
    }

    private void setupListeners() {
        binding.btnCreateInvoice.setOnClickListener(v -> submitCreateInvoice());
        binding.btnAutoGenerate.setOnClickListener(v -> generateMonthlyInvoice());
        binding.btnBulkGenerate.setOnClickListener(v -> confirmBulkGenerateInvoices());
        binding.btnAddCustomFee.setOnClickListener(v -> showAddCustomFeeDialog());

        TextWatcher periodWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateInvoicePreview();
            }
        };

        binding.etMonth.addTextChangedListener(periodWatcher);
        binding.etYear.addTextChangedListener(periodWatcher);
    }

    private void loadServices() {
        serviceViewModel.getServices().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                allServicesList = resource.getData();
                buildServicesUi();
                updateInvoicePreview();
            }
        });
    }

    private void loadActiveContracts() {
        contractViewModel.filterByStatus("ACTIVE");
        contractViewModel.getContracts().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                contractsList = resource.getData();
                List<String> labels = new ArrayList<>();
                long targetContractId = getIntent().getLongExtra("contract_id", -1);
                long targetRoomId = getIntent().getLongExtra("room_id", -1);
                int selectedIndex = 0;

                for (int i = 0; i < contractsList.size(); i++) {
                    Contract c = contractsList.get(i);
                    labels.add("Phòng " + c.getRoomCode() + " - Khách: " + c.getTenantName());
                    if (targetContractId != -1 && c.getId() != null && c.getId() == targetContractId) {
                        selectedIndex = i;
                    } else if (targetRoomId != -1 && c.getRoomId() != null && c.getRoomId() == targetRoomId) {
                        selectedIndex = i;
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels);
                binding.actvContract.setAdapter(adapter);

                binding.actvContract.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedText = (String) parent.getItemAtPosition(position);
                    selectedContractIndex = labels.indexOf(selectedText);
                    updateInvoicePreview();
                });

                if (!contractsList.isEmpty()) {
                    selectedContractIndex = selectedIndex;
                    binding.actvContract.setText(labels.get(selectedIndex), false);
                    updateInvoicePreview();
                }
            }
        });
    }

    private void buildServicesUi() {
        binding.llServicesContainer.removeAllViews();
        serviceRowHolders.clear();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ServiceItem s : allServicesList) {
            if (Boolean.TRUE.equals(s.getIsMetered())) {
                // Điện / Nước handled separately
                if (s.getName().toLowerCase().contains("điện")) {
                    electricUnitPrice = s.getUnitPrice();
                } else if (s.getName().toLowerCase().contains("nước")) {
                    waterUnitPrice = s.getUnitPrice();
                }
                continue;
            }

            View row = inflater.inflate(R.layout.item_invoice_service_selectable, binding.llServicesContainer, false);
            CheckBox cb = row.findViewById(R.id.cbServiceSelected);
            TextView tvName = row.findViewById(R.id.tvServiceName);
            TextView tvPrice = row.findViewById(R.id.tvServiceUnitPrice);
            EditText etQty = row.findViewById(R.id.etServiceQty);
            TextView tvAmt = row.findViewById(R.id.tvServiceAmount);

            tvName.setText(s.getName());
            tvPrice.setText(CurrencyUtils.formatVND(s.getUnitPrice()) + " / " + s.getUnit());
            etQty.setText("1");
            tvAmt.setText(CurrencyUtils.formatVND(s.getUnitPrice()));

            ServiceRowHolder holder = new ServiceRowHolder(s, row, cb, tvName, tvPrice, etQty, tvAmt);
            serviceRowHolders.add(holder);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etQty.setEnabled(isChecked);
                recalculateGrandTotal();
            });

            etQty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    tvAmt.setText(CurrencyUtils.formatVND(holder.getAmount()));
                    recalculateGrandTotal();
                }
            });

            binding.llServicesContainer.addView(row);
        }
    }

    private void setupMeterReadingObserver() {
        meterReadingViewModel.getMeterReadings().observe(this, res -> {
            currentElectricCost = 0;
            currentWaterCost = 0;
            electricUsage = 0;
            waterUsage = 0;
            electricDescription = "";
            waterDescription = "";

            if (res != null && res.isSuccess() && res.getData() != null) {
                for (MeterReading r : res.getData()) {
                    boolean isElec = (r.getServiceUnit() != null && r.getServiceUnit().equalsIgnoreCase("kWh"))
                            || (r.getServiceName() != null && r.getServiceName().toLowerCase().contains("điện"));
                    boolean isWater = (r.getServiceUnit() != null && r.getServiceUnit().contains("m"))
                            || (r.getServiceName() != null && r.getServiceName().toLowerCase().contains("nước"));

                    double usage = Math.max(0, r.getNewValue() - r.getOldValue());
                    long unitP = r.getServiceUnitPrice() != null && r.getServiceUnitPrice() > 0 ? r.getServiceUnitPrice() : (isElec ? electricUnitPrice : waterUnitPrice);
                    long cost = (long) (usage * unitP);

                    if (isElec) {
                        electricUsage = usage;
                        electricUnitPrice = unitP;
                        currentElectricCost = cost;
                        electricDescription = String.format("Tiền điện (%.0f -> %.0f kWh)", r.getOldValue(), r.getNewValue());
                        binding.tvElectricLabel.setText(electricDescription);
                        binding.tvElectricFee.setText(CurrencyUtils.formatVND(cost));
                    } else if (isWater) {
                        waterUsage = usage;
                        waterUnitPrice = unitP;
                        currentWaterCost = cost;
                        waterDescription = String.format("Tiền nước sinh hoạt (%.0f -> %.0f m³)", r.getOldValue(), r.getNewValue());
                        binding.tvWaterLabel.setText(waterDescription);
                        binding.tvWaterFee.setText(CurrencyUtils.formatVND(cost));
                    }
                }
            }

            if (currentElectricCost == 0 && electricDescription.isEmpty()) {
                binding.tvElectricLabel.setText("Tiền điện");
                binding.tvElectricFee.setText("Chưa ghi chỉ số (0 đ)");
            }
            if (currentWaterCost == 0 && waterDescription.isEmpty()) {
                binding.tvWaterLabel.setText("Tiền nước sinh hoạt");
                binding.tvWaterFee.setText("Chưa ghi chỉ số (0 đ)");
            }

            recalculateGrandTotal();
        });
    }

    private void updateInvoicePreview() {
        if (selectedContractIndex < 0 || selectedContractIndex >= contractsList.size()) return;

        Contract contract = contractsList.get(selectedContractIndex);
        currentRentPrice = contract.getRentPrice() != null ? contract.getRentPrice() : 0L;
        binding.tvRentFee.setText(CurrencyUtils.formatVND(currentRentPrice));

        int month = getSelectedMonth();
        int year = getSelectedYear();

        // 1. Kiểm tra hóa đơn tháng này đã tồn tại hay chưa
        checkExistingInvoice(contract, month, year);

        // 2. Query meter readings for this room, month, year (cập nhật qua observer đã đăng ký)
        meterReadingViewModel.filter(contract.getRoomId(), null, month, year);
    }

    private void checkExistingInvoice(Contract contract, int month, int year) {
        invoiceViewModel.checkExistingInvoice(contract.getId(), month, year).observe(this, res -> {
            if (res != null && res.isSuccess()) {
                if (res.getData() != null && !res.getData().isEmpty()) {
                    existingInvoice = res.getData().get(0);
                    binding.cvExistingInvoiceAlert.setVisibility(View.VISIBLE);

                    String statusLabel = "Chưa thanh toán";
                    if (existingInvoice.isPaid()) {
                        statusLabel = "Đã thanh toán";
                    } else if (existingInvoice.isPartial()) {
                        statusLabel = "Thanh toán 1 phần";
                    }

                    binding.tvExistingInvoiceMessage.setText(String.format(
                            "Phòng %s đã có hóa đơn Tháng %d/%d (Mã #%d)\nSố tiền: %s • Trạng thái: %s",
                            contract.getRoomCode(),
                            month,
                            year,
                            existingInvoice.getId(),
                            CurrencyUtils.formatVND(existingInvoice.getTotalAmount()),
                            statusLabel
                    ));

                    binding.btnViewExistingInvoice.setOnClickListener(v -> {
                        Intent intent = new Intent(CreateInvoiceActivity.this, InvoiceDetailActivity.class);
                        intent.putExtra("invoice_id", existingInvoice.getId());
                        startActivity(intent);
                    });

                    binding.btnCreateInvoice.setEnabled(false);
                    binding.btnCreateInvoice.setText(String.format("Phòng %s đã có hóa đơn T%d/%d", contract.getRoomCode(), month, year));
                    binding.btnCreateInvoice.setAlpha(0.6f);

                    binding.btnAutoGenerate.setEnabled(false);
                    binding.btnAutoGenerate.setAlpha(0.6f);
                } else {
                    existingInvoice = null;
                    binding.cvExistingInvoiceAlert.setVisibility(View.GONE);

                    binding.btnCreateInvoice.setEnabled(true);
                    binding.btnCreateInvoice.setText(R.string.btn_create_invoice);
                    binding.btnCreateInvoice.setAlpha(1.0f);

                    binding.btnAutoGenerate.setEnabled(true);
                    binding.btnAutoGenerate.setAlpha(1.0f);
                }
            }
        });
    }

    private void recalculateGrandTotal() {
        long total = currentRentPrice + currentElectricCost + currentWaterCost;

        for (ServiceRowHolder h : serviceRowHolders) {
            if (h.isChecked()) {
                total += h.getAmount();
            }
        }

        for (CustomFeeItem fee : customFeesList) {
            total += fee.getAmount();
        }

        binding.tvCalculatedTotal.setText(CurrencyUtils.formatVND(total));
    }

    private void showAddCustomFeeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_invoice_item, null);
        View tilAvailableServices = dialogView.findViewById(R.id.tilAvailableServices);
        if (tilAvailableServices != null) {
            tilAvailableServices.setVisibility(View.GONE);
        }
        TextInputEditText etName = dialogView.findViewById(R.id.etItemName);
        TextInputEditText etQty = dialogView.findViewById(R.id.etItemQuantity);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etItemUnitPrice);

        new AlertDialog.Builder(this)
                .setTitle("Thêm khoản thu khác")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String qtyStr = etQty.getText() != null ? etQty.getText().toString().trim() : "1";
                    String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "0";

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên khoản phí", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double qty = 1.0;
                    try { qty = Double.parseDouble(qtyStr); } catch (Exception ignored) {}
                    long price = 0;
                    try { price = Long.parseLong(priceStr); } catch (Exception ignored) {}

                    addCustomFeeRow(name, qty, price);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addCustomFeeRow(String name, double qty, long unitPrice) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_invoice_service_selectable, binding.llServicesContainer, false);
        CheckBox cb = row.findViewById(R.id.cbServiceSelected);
        TextView tvName = row.findViewById(R.id.tvServiceName);
        TextView tvPrice = row.findViewById(R.id.tvServiceUnitPrice);
        EditText etQty = row.findViewById(R.id.etServiceQty);
        TextView tvAmt = row.findViewById(R.id.tvServiceAmount);

        tvName.setText(name + " (Khoản thu ngoài)");
        tvPrice.setText(CurrencyUtils.formatVND(unitPrice) + " / mục");
        etQty.setText(String.valueOf(qty));
        long amt = (long) (qty * unitPrice);
        tvAmt.setText(CurrencyUtils.formatVND(amt));

        CustomFeeItem item = new CustomFeeItem(name, qty, unitPrice, row);
        customFeesList.add(item);

        cb.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isChecked) {
                binding.llServicesContainer.removeView(row);
                customFeesList.remove(item);
                recalculateGrandTotal();
            }
        });

        binding.llServicesContainer.addView(row);
        recalculateGrandTotal();
    }

    private void submitCreateInvoice() {
        if (selectedContractIndex < 0 || selectedContractIndex >= contractsList.size()) {
            Toast.makeText(this, "Vui lòng chọn hợp đồng / phòng", Toast.LENGTH_SHORT).show();
            return;
        }

        Contract contract = contractsList.get(selectedContractIndex);
        int month = getSelectedMonth();
        int year = getSelectedYear();
        String dueDate = binding.etDueDate.getText() != null ? binding.etDueDate.getText().toString().trim() : "";

        if (existingInvoice != null) {
            Toast.makeText(this, String.format("Hóa đơn tháng %d/%d cho phòng %s đã tồn tại!", month, year, contract.getRoomCode()), Toast.LENGTH_LONG).show();
            return;
        }

        List<InvoiceItem> items = new ArrayList<>();

        // 1. Tiền phòng
        items.add(new InvoiceItem("Tiền thuê phòng " + contract.getRoomCode() + " tháng " + month + "/" + year, 1.0, currentRentPrice, currentRentPrice));

        // 2. Tiền điện
        if (currentElectricCost > 0) {
            items.add(new InvoiceItem(electricDescription.isEmpty() ? "Tiền điện" : electricDescription, electricUsage, electricUnitPrice, currentElectricCost));
        }

        // 3. Tiền nước
        if (currentWaterCost > 0) {
            items.add(new InvoiceItem(waterDescription.isEmpty() ? "Tiền nước sinh hoạt" : waterDescription, waterUsage, waterUnitPrice, currentWaterCost));
        }

        // 4. Các dịch vụ khác được chọn
        for (ServiceRowHolder h : serviceRowHolders) {
            if (h.isChecked()) {
                items.add(new InvoiceItem(h.getName(), h.getQuantity(), h.getUnitPrice(), h.getAmount()));
            }
        }

        // 5. Khoản thu khác
        for (CustomFeeItem f : customFeesList) {
            items.add(new InvoiceItem(f.description, f.quantity, f.unitPrice, f.getAmount()));
        }

        binding.btnCreateInvoice.setEnabled(false);
        binding.btnAutoGenerate.setEnabled(false);

        CreateInvoiceRequest req = new CreateInvoiceRequest(contract.getId(), month, year, dueDate.isEmpty() ? null : dueDate, items);
        invoiceViewModel.createInvoice(req).observe(this, res -> {
            if (res == null || res.isLoading()) {
                return;
            }
            binding.btnCreateInvoice.setEnabled(true);
            binding.btnAutoGenerate.setEnabled(true);
            if (res.isSuccess()) {
                Toast.makeText(this, "Lập hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (res.isError()) {
                Toast.makeText(this, res.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateMonthlyInvoice() {
        int pos = selectedContractIndex;
        if (pos < 0 || pos >= contractsList.size()) {
            Toast.makeText(this, "Vui lòng chọn một hợp đồng phòng để lập hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }

        Contract selectedContract = contractsList.get(pos);
        int month = getSelectedMonth();
        int year = getSelectedYear();
        String dueDate = binding.etDueDate.getText() != null ? binding.etDueDate.getText().toString().trim() : "";

        if (existingInvoice != null) {
            Toast.makeText(this, String.format("Hóa đơn tháng %d/%d cho phòng %s đã tồn tại!", month, year, selectedContract.getRoomCode()), Toast.LENGTH_LONG).show();
            return;
        }

        binding.btnAutoGenerate.setEnabled(false);
        binding.btnCreateInvoice.setEnabled(false);

        invoiceViewModel.generateMonthlyInvoice(selectedContract.getId(), month, year, dueDate.isEmpty() ? null : dueDate)
                .observe(this, resource -> {
                    if (resource == null || resource.isLoading()) return;
                    binding.btnAutoGenerate.setEnabled(true);
                    binding.btnCreateInvoice.setEnabled(true);
                    if (resource.isSuccess()) {
                        Toast.makeText(this, "Đã tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (resource.isError()) {
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmBulkGenerateInvoices() {
        int month = getSelectedMonth();
        int year = getSelectedYear();
        String dueDate = binding.etDueDate.getText() != null ? binding.etDueDate.getText().toString().trim() : "";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lập hóa đơn hàng loạt")
                .setMessage(String.format("Bạn có chắc chắn muốn tự động tạo hóa đơn Tháng %d/%d cho TẤT CẢ các phòng đang thuê không?\n\n(Hệ thống sẽ tự động tính tiền phòng + điện nước + dịch vụ và bỏ qua các phòng đã có hóa đơn)", month, year))
                .setPositiveButton("Tạo ngay", (dialog, which) -> {
                    binding.btnBulkGenerate.setEnabled(false);
                    binding.btnAutoGenerate.setEnabled(false);

                    invoiceViewModel.bulkGenerateInvoices(month, year, dueDate.isEmpty() ? null : dueDate)
                            .observe(this, resource -> {
                                binding.btnBulkGenerate.setEnabled(true);
                                binding.btnAutoGenerate.setEnabled(true);

                                if (resource != null && resource.isSuccess()) {
                                    String msg = "Đã hoàn tất lập hóa đơn hàng loạt!";
                                    if (resource.getData() != null && resource.getData().containsKey("created_count")) {
                                        Object created = resource.getData().get("created_count");
                                        Object skipped = resource.getData().get("skipped_count");
                                        msg = String.format("Thành công!\n• Đã lập: %s hóa đơn\n• Bỏ qua (đã có): %s phòng", created, skipped);
                                    }
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("Kết quả lập hóa đơn hàng loạt")
                                            .setMessage(msg)
                                            .setPositiveButton("Đồng ý", (d, w) -> finish())
                                            .show();
                                } else if (resource != null && resource.isError()) {
                                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int getSelectedMonth() {
        String s = binding.etMonth.getText() != null ? binding.etMonth.getText().toString().trim() : "1";
        try { return Integer.parseInt(s); } catch (Exception e) { return 1; }
    }

    private int getSelectedYear() {
        String s = binding.etYear.getText() != null ? binding.etYear.getText().toString().trim() : "2024";
        try { return Integer.parseInt(s); } catch (Exception e) { return 2024; }
    }

    private static class ServiceRowHolder {
        final ServiceItem service;
        final View view;
        final CheckBox cbSelected;
        final TextView tvName;
        final TextView tvUnitPrice;
        final EditText etQty;
        final TextView tvAmount;

        ServiceRowHolder(ServiceItem service, View view, CheckBox cb, TextView tvName, TextView tvPrice, EditText etQty, TextView tvAmt) {
            this.service = service;
            this.view = view;
            this.cbSelected = cb;
            this.tvName = tvName;
            this.tvUnitPrice = tvPrice;
            this.etQty = etQty;
            this.tvAmount = tvAmt;
        }

        double getQuantity() {
            String q = etQty.getText() != null ? etQty.getText().toString().trim() : "1";
            try { return Double.parseDouble(q); } catch (Exception e) { return 1.0; }
        }

        long getUnitPrice() {
            return service.getUnitPrice() != null ? service.getUnitPrice() : 0L;
        }

        long getAmount() {
            return (long) (getQuantity() * getUnitPrice());
        }

        boolean isChecked() {
            return cbSelected.isChecked();
        }

        String getName() {
            return service.getName();
        }
    }

    private static class CustomFeeItem {
        final String description;
        final double quantity;
        final long unitPrice;
        final View view;

        CustomFeeItem(String description, double quantity, long unitPrice, View view) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.view = view;
        }

        long getAmount() {
            return (long) (quantity * unitPrice);
        }
    }
}
