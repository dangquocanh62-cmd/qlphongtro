package com.nhatro.app.ui.invoices;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.data.model.Payment;
import com.nhatro.app.databinding.ActivityInvoiceDetailBinding;
import com.nhatro.app.ui.adapters.InvoiceItemAdapter;
import com.nhatro.app.ui.adapters.PaymentAdapter;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.utils.InvoiceExportHelper;
import com.nhatro.app.utils.SharedPrefsHelper;
import com.nhatro.app.viewmodel.InvoiceViewModel;
import com.nhatro.app.viewmodel.PaymentViewModel;

public class InvoiceDetailActivity extends AppCompatActivity {
    private ActivityInvoiceDetailBinding binding;
    private InvoiceViewModel invoiceViewModel;
    private PaymentViewModel paymentViewModel;

    private Long invoiceId;
    private Invoice currentInvoice;
    private InvoiceItemAdapter itemAdapter;
    private PaymentAdapter paymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        invoiceViewModel = new ViewModelProvider(this).get(InvoiceViewModel.class);
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_invoice_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                showExportDialog();
                return true;
            } else if (item.getItemId() == R.id.action_print) {
                if (currentInvoice != null) {
                    InvoiceExportHelper.printOrSavePdf(this, currentInvoice);
                }
                return true;
            }
            return false;
        });

        invoiceId = getIntent().getLongExtra("invoice_id", -1);
        if (invoiceId == -1) {
            finish();
            return;
        }

        setupRecyclerViews();
        loadInvoice();

        binding.btnExportInvoice.setOnClickListener(v -> showExportDialog());
        binding.btnPrintInvoice.setOnClickListener(v -> {
            if (currentInvoice != null) {
                InvoiceExportHelper.printOrSavePdf(this, currentInvoice);
            }
        });
        binding.btnAddPayment.setOnClickListener(v -> showAddPaymentDialog());
    }

    private void setupRecyclerViews() {
        itemAdapter = new InvoiceItemAdapter();
        binding.rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInvoiceItems.setAdapter(itemAdapter);

        paymentAdapter = new PaymentAdapter();
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPayments.setAdapter(paymentAdapter);
    }

    private void loadInvoice() {
        invoiceViewModel.getInvoiceById(invoiceId).observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                currentInvoice = resource.getData();
                displayInvoiceData();
            }
        });
    }

    private void displayInvoiceData() {
        binding.tvInvoiceRoom.setText("Phòng " + currentInvoice.getRoomCode());
        binding.tvInvoicePeriod.setText(String.format("Kỳ thanh toán: Tháng %d/%d", currentInvoice.getMonth(), currentInvoice.getYear()));
        binding.tvInvoiceTenant.setText("Khách thuê: " + currentInvoice.getTenantName() + " (" + currentInvoice.getTenantPhone() + ")");
        binding.tvInvoiceDueDate.setText("Hạn nộp tiền: " + DateUtils.formatDateVN(currentInvoice.getDueDate()));

        binding.tvTotalAmount.setText(CurrencyUtils.formatVND(currentInvoice.getTotalAmount()));
        binding.tvPaidAmount.setText(CurrencyUtils.formatVND(currentInvoice.getPaidAmount()));
        binding.tvRemainingDebt.setText(CurrencyUtils.formatVND(currentInvoice.getRemainingAmount()));

        if (currentInvoice.isPaid()) {
            binding.tvInvoiceStatusBadge.setText("Đã thanh toán");
            binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_paid);
            binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_paid_text));
            binding.btnAddPayment.setVisibility(View.GONE);
        } else if (currentInvoice.isPartial()) {
            binding.tvInvoiceStatusBadge.setText("Đã trả một phần");
            binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_partial);
            binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_partial_text));
            binding.btnAddPayment.setVisibility(View.VISIBLE);
        } else {
            binding.tvInvoiceStatusBadge.setText("Chưa thanh toán");
            binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_unpaid);
            binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_unpaid_text));
            binding.btnAddPayment.setVisibility(View.VISIBLE);
        }

        // Set items
        if (currentInvoice.getItems() != null) {
            itemAdapter.setItems(currentInvoice.getItems());
        }

        // Set payments
        if (currentInvoice.getPayments() != null && !currentInvoice.getPayments().isEmpty()) {
            paymentAdapter.setPayments(currentInvoice.getPayments());
            binding.tvNoPayments.setVisibility(View.GONE);
        } else {
            paymentAdapter.setPayments(null);
            binding.tvNoPayments.setVisibility(View.VISIBLE);
        }
    }

    private void showAddPaymentDialog() {
        if (currentInvoice == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_payment, null);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etPaymentAmount);
        RadioButton rbTransfer = dialogView.findViewById(R.id.rbTransfer);

        // Pre-fill remaining debt amount for convenience
        etAmount.setText(String.valueOf(currentInvoice.getRemainingAmount()));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Ghi nhận", (dialog, which) -> {
                    String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "0";
                    long amount = amountStr.isEmpty() ? 0 : Long.parseLong(amountStr);

                    if (amount <= 0) {
                        Toast.makeText(this, "Số tiền thanh toán phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String method = rbTransfer.isChecked() ? "TRANSFER" : "CASH";
                    Payment payment = new Payment(currentInvoice.getId(), amount, method, null);

                    paymentViewModel.createPayment(payment).observe(this, resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(this, "Ghi nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            loadInvoice();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showExportDialog() {
        if (currentInvoice == null) {
            Toast.makeText(this, "Đang tải dữ liệu hóa đơn, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_export_invoice, null);
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            dialog.setContentView(dialogView);

            TextView tvTitle = dialogView.findViewById(R.id.tvExportTitle);
            TextView tvPreview = dialogView.findViewById(R.id.tvInvoicePreviewText);
            TextInputEditText etBankName = dialogView.findViewById(R.id.etExportBankName);
            TextInputEditText etBankAccount = dialogView.findViewById(R.id.etExportBankAccount);
            TextInputEditText etBankOwner = dialogView.findViewById(R.id.etExportBankOwner);
            MaterialButton btnShare = dialogView.findViewById(R.id.btnShareText);
            MaterialButton btnCopy = dialogView.findViewById(R.id.btnCopyText);
            MaterialButton btnPrint = dialogView.findViewById(R.id.btnPrintPdf);
            MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseDialog);

            tvTitle.setText("Xuất Hóa Đơn - Phòng " + currentInvoice.getRoomCode());

            SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(this);
            etBankName.setText(prefs.getBankName());
            etBankAccount.setText(prefs.getBankAccount());
            etBankOwner.setText(prefs.getBankOwner());

            Runnable updatePreview = () -> {
                String bName = etBankName.getText() != null ? etBankName.getText().toString().trim() : "";
                String bAcc = etBankAccount.getText() != null ? etBankAccount.getText().toString().trim() : "";
                String bOwn = etBankOwner.getText() != null ? etBankOwner.getText().toString().trim() : "";
                prefs.saveBankInfo(bName, bAcc, bOwn);
                tvPreview.setText(InvoiceExportHelper.generateShareText(this, currentInvoice));
            };

            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    updatePreview.run();
                }
            };

            etBankName.addTextChangedListener(watcher);
            etBankAccount.addTextChangedListener(watcher);
            etBankOwner.addTextChangedListener(watcher);

            // Hiển thị nội dung xem trước ban đầu
            tvPreview.setText(InvoiceExportHelper.generateShareText(this, currentInvoice));

            btnShare.setOnClickListener(v -> {
                updatePreview.run();
                InvoiceExportHelper.shareInvoice(this, currentInvoice);
                dialog.dismiss();
            });

            btnCopy.setOnClickListener(v -> {
                updatePreview.run();
                InvoiceExportHelper.copyToClipboard(this, currentInvoice);
            });

            btnPrint.setOnClickListener(v -> {
                updatePreview.run();
                InvoiceExportHelper.printOrSavePdf(this, currentInvoice);
                dialog.dismiss();
            });

            btnClose.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi mở hộp thoại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
