package com.nhatro.app.ui.invoices;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ActivityInvoiceDetailBinding;
import com.nhatro.app.ui.adapters.InvoiceItemAdapter;
import com.nhatro.app.ui.adapters.PaymentAdapter;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.utils.InvoiceExportHelper;
import com.nhatro.app.utils.SharedPrefsHelper;
import com.nhatro.app.viewmodel.InvoiceViewModel;
import com.nhatro.app.viewmodel.PaymentViewModel;
import com.nhatro.app.viewmodel.ServiceViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class InvoiceDetailActivity extends BaseActivity {
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
        binding.btnAddInvoiceItem.setOnClickListener(v -> showAddInvoiceItemDialog());
    }

    private void setupRecyclerViews() {
        itemAdapter = new InvoiceItemAdapter();
        itemAdapter.setOnItemDeleteListener(item -> {
            if (currentInvoice != null && currentInvoice.isPaid()) {
                Toast.makeText(this, "Hóa đơn đã thanh toán, không thể xóa mục chi phí", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage(String.format("Bạn có chắc chắn muốn xóa \"%s\" (%s) khỏi hóa đơn không?",
                            item.getDescription(), CurrencyUtils.formatVND(item.getAmount())))
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        invoiceViewModel.removeInvoiceItem(invoiceId, item.getId()).observe(this, res -> {
                            if (res != null && res.isSuccess()) {
                                Toast.makeText(this, "Đã xóa mục chi phí!", Toast.LENGTH_SHORT).show();
                                loadInvoice();
                            } else if (res != null && res.isError()) {
                                Toast.makeText(this, res.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
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
            binding.btnAddPayment.setVisibility(android.view.View.GONE);
            binding.btnAddInvoiceItem.setVisibility(android.view.View.GONE);
            itemAdapter.setCanDelete(false);
        } else if (currentInvoice.isPartial()) {
            binding.tvInvoiceStatusBadge.setText("Đã trả một phần");
            binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_partial);
            binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_partial_text));
            binding.btnAddPayment.setVisibility(android.view.View.VISIBLE);
            binding.btnAddInvoiceItem.setVisibility(android.view.View.VISIBLE);
            itemAdapter.setCanDelete(true);
        } else {
            binding.tvInvoiceStatusBadge.setText("Chưa thanh toán");
            binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_unpaid);
            binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_unpaid_text));
            binding.btnAddPayment.setVisibility(android.view.View.VISIBLE);
            binding.btnAddInvoiceItem.setVisibility(android.view.View.VISIBLE);
            itemAdapter.setCanDelete(true);
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

        displayVietQrCard();
    }

    private void displayVietQrCard() {
        if (currentInvoice == null) return;

        SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(this);
        String bankName = prefs.getBankName();
        String bankAccount = prefs.getBankAccount();
        String bankOwner = prefs.getBankOwner();

        if (bankAccount.isEmpty()) {
            binding.layoutNoBankConfig.setVisibility(View.VISIBLE);
            binding.layoutQrDisplay.setVisibility(View.GONE);
            binding.btnSetupBankInfo.setOnClickListener(v -> showExportDialog());
            return;
        }

        binding.layoutNoBankConfig.setVisibility(View.GONE);
        binding.layoutQrDisplay.setVisibility(View.VISIBLE);

        binding.tvQrBankInfo.setText(bankName.isEmpty() ? ("STK: " + bankAccount) : (bankName + " - STK: " + bankAccount));
        binding.tvQrAccountOwner.setText("Chủ tài khoản: " + (bankOwner.isEmpty() ? "---" : bankOwner));

        String transferContent = currentInvoice.getRoomCode() + " tien nha T" + currentInvoice.getMonth();
        binding.tvQrTransferContent.setText("Cú pháp CK: " + transferContent);

        long payAmount = currentInvoice.getRemainingAmount() > 0 ? currentInvoice.getRemainingAmount() : currentInvoice.getTotalAmount();
        String qrUrl = InvoiceExportHelper.generateVietQrUrl(bankName, bankAccount, payAmount, transferContent, bankOwner);

        binding.pbQrLoading.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream in = new URL(qrUrl).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                runOnUiThread(() -> {
                    if (!isFinishing() && binding != null) {
                        binding.pbQrLoading.setVisibility(View.GONE);
                        if (bmp != null) {
                            binding.ivVietQrCode.setImageBitmap(bmp);
                            binding.ivVietQrCode.setOnClickListener(v -> showQrZoomDialog(qrUrl));
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing() && binding != null) {
                        binding.pbQrLoading.setVisibility(View.GONE);
                    }
                });
            }
        });

        binding.btnCopyAccountNo.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("STK", bankAccount);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép STK: " + bankAccount, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCopyContent.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Nội dung chuyển khoản", transferContent);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép nội dung: " + transferContent, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQrZoomDialog(String qrUrl) {
        if (isFinishing() || qrUrl == null || qrUrl.isEmpty()) return;

        ImageView iv = new ImageView(this);
        iv.setPadding(32, 32, 32, 32);
        iv.setAdjustViewBounds(true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Mã VietQR - Phòng " + (currentInvoice != null ? currentInvoice.getRoomCode() : ""))
                .setView(iv)
                .setPositiveButton("Đóng", null)
                .create();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream in = new URL(qrUrl).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                runOnUiThread(() -> {
                    if (dialog.isShowing() && bmp != null) {
                        iv.setImageBitmap(bmp);
                    }
                });
            } catch (Exception ignored) {}
        });

        dialog.show();
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

            dialog.setOnDismissListener(d -> displayVietQrCard());
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi mở hộp thoại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddInvoiceItemDialog() {
        if (currentInvoice == null) return;

        ServiceViewModel serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        android.view.View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_invoice_item, null);
        com.google.android.material.textfield.MaterialAutoCompleteTextView actvServices = dialogView.findViewById(R.id.actvAvailableServices);
        TextInputEditText etItemName = dialogView.findViewById(R.id.etItemName);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.etItemQuantity);
        TextInputEditText etUnitPrice = dialogView.findViewById(R.id.etItemUnitPrice);

        List<ServiceItem> servicesList = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        serviceViewModel.getServices().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                servicesList.clear();
                servicesList.addAll(resource.getData());
                labels.clear();
                for (ServiceItem s : servicesList) {
                    labels.add(s.getName() + " (" + CurrencyUtils.formatVND(s.getUnitPrice()) + "/" + s.getUnit() + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels);
                actvServices.setAdapter(adapter);
            }
        });

        actvServices.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            int idx = labels.indexOf(selectedText);
            if (idx >= 0 && idx < servicesList.size()) {
                ServiceItem chosen = servicesList.get(idx);
                etItemName.setText(chosen.getName());
                etUnitPrice.setText(String.valueOf(chosen.getUnitPrice()));
                if (etQuantity.getText() == null || etQuantity.getText().toString().trim().isEmpty()) {
                    etQuantity.setText("1");
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Thêm khoản phí / Dịch vụ")
                .setView(dialogView)
                .setPositiveButton("Thêm vào hóa đơn", (dialog, which) -> {
                    String desc = etItemName.getText() != null ? etItemName.getText().toString().trim() : "";
                    String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "1";
                    String priceStr = etUnitPrice.getText() != null ? etUnitPrice.getText().toString().trim() : "0";

                    if (desc.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên khoản phí/dịch vụ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double qty = 1.0;
                    try {
                        qty = Double.parseDouble(qtyStr);
                    } catch (Exception ignored) {}

                    long price = 0;
                    try {
                        price = Long.parseLong(priceStr);
                    } catch (Exception ignored) {}

                    invoiceViewModel.addInvoiceItem(invoiceId, desc, qty, price).observe(this, resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(this, "Đã thêm mục vào hóa đơn!", Toast.LENGTH_SHORT).show();
                            loadInvoice();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
