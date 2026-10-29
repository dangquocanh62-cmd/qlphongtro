package com.nhatro.app.ui.contracts;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ActivityContractDetailBinding;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.viewmodel.ContractViewModel;
import com.nhatro.app.viewmodel.TenantViewModel;
import java.util.ArrayList;
import java.util.List;

public class ContractDetailActivity extends BaseActivity {
    private ActivityContractDetailBinding binding;
    private ContractViewModel viewModel;
    private TenantViewModel tenantViewModel;
    private Long contractId;
    private Contract contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContractDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContractViewModel.class);
        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        contractId = getIntent().getLongExtra("contract_id", -1);
        if (contractId == -1) {
            finish();
            return;
        }

        loadContract();

        binding.btnAddRoommate.setOnClickListener(v -> showAddRoommateDialog());
        binding.btnExtendContract.setOnClickListener(v -> showExtendContractDialog());
        binding.btnEndContract.setOnClickListener(v -> confirmEndContract());
    }

    private void loadContract() {
        viewModel.getContractById(contractId).observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                contract = resource.getData();
                displayContract();
            }
        });
    }

    private void displayContract() {
        binding.tvDetailRoom.setText("Phòng " + contract.getRoomCode());
        binding.tvDetailTenant.setText("Đại diện: " + contract.getTenantName() + " (" + contract.getTenantPhone() + ")");
        binding.tvDetailIdCard.setText("CCCD: " + contract.getTenantIdCard() + " • Quê quán: " + (contract.getTenantHometown() != null ? contract.getTenantHometown() : "---"));

        binding.tvDetailDates.setText("Thời hạn: " + DateUtils.formatDateVN(contract.getStartDate()) + " -> " +
                (contract.getEndDate() != null ? DateUtils.formatDateVN(contract.getEndDate()) : "Không thời hạn"));
        binding.tvDetailRentPrice.setText("Giá thuê theo HĐ: " + CurrencyUtils.formatVND(contract.getRentPrice()) + " / tháng");
        binding.tvDetailDeposit.setText("Tiền cọc: " + CurrencyUtils.formatVND(contract.getDeposit()));

        if (contract.isActive()) {
            binding.tvDetailStatus.setText("Hiệu lực");
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_badge_available);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
            binding.btnEndContract.setEnabled(true);
            binding.btnExtendContract.setEnabled(true);

            if (contract.isExpired()) {
                int daysPast = Math.abs(contract.calculateDaysRemaining());
                binding.cardDetailExpiryAlert.setVisibility(View.VISIBLE);
                binding.tvDetailExpiryText.setText("Hợp đồng này đã quá hạn " + daysPast + " ngày! Hãy liên hệ khách thuê để gia hạn hoặc thanh lý phòng.");
            } else if (contract.isExpiringSoon()) {
                int daysLeft = contract.calculateDaysRemaining();
                binding.cardDetailExpiryAlert.setVisibility(View.VISIBLE);
                binding.tvDetailExpiryText.setText("Hợp đồng này sắp hết hạn (còn " + daysLeft + " ngày)! Hãy trao đổi với khách thuê để tiến hành gia hạn.");
            } else {
                binding.cardDetailExpiryAlert.setVisibility(View.GONE);
            }
        } else {
            binding.tvDetailStatus.setText("Đã kết thúc");
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_badge_maintenance);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_maintenance_text));
            binding.cardDetailExpiryAlert.setVisibility(View.GONE);
            binding.btnEndContract.setEnabled(false);
            binding.btnEndContract.setText("Hợp đồng đã kết thúc");
            binding.btnExtendContract.setText("Tái ký / Gia hạn lại hợp đồng");
        }

        if (contract.getRoommates() != null && !contract.getRoommates().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Tenant t : contract.getRoommates()) {
                sb.append("• ").append(t.getFullName()).append(" (SĐT: ").append(t.getPhone()).append(", CCCD: ").append(t.getIdCard()).append(")\n");
            }
            binding.tvRoommateList.setText(sb.toString().trim());
        } else {
            binding.tvRoommateList.setText("Chưa có người ở cùng");
        }
    }

    private void showExtendContractDialog() {
        if (contract == null) return;

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        TextView tvPrompt = new TextView(this);
        tvPrompt.setText("Nhập ngày kết thúc mới và giá thuê (nếu có thay đổi):");
        tvPrompt.setTextSize(13);
        tvPrompt.setPadding(0, 0, 0, 16);
        tvPrompt.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        layout.addView(tvPrompt);

        com.google.android.material.textfield.TextInputLayout tilDate = new com.google.android.material.textfield.TextInputLayout(this);
        tilDate.setHint("Ngày kết thúc mới (YYYY-MM-DD)");
        com.google.android.material.textfield.TextInputEditText etDate = new com.google.android.material.textfield.TextInputEditText(this);
        String defaultNextDate = DateUtils.addMonths(contract.getEndDate(), 6);
        etDate.setText(defaultNextDate);
        tilDate.addView(etDate);
        layout.addView(tilDate);

        com.google.android.material.textfield.TextInputLayout tilPrice = new com.google.android.material.textfield.TextInputLayout(this);
        tilPrice.setHint("Giá thuê mới (VNĐ)");
        tilPrice.setPadding(0, 16, 0, 0);
        com.google.android.material.textfield.TextInputEditText etPrice = new com.google.android.material.textfield.TextInputEditText(this);
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPrice.setText(String.valueOf(contract.getRentPrice()));
        tilPrice.addView(etPrice);
        layout.addView(tilPrice);

        new AlertDialog.Builder(this)
                .setTitle("Gia hạn hợp đồng phòng " + contract.getRoomCode())
                .setView(layout)
                .setPositiveButton("Xác nhận gia hạn", (dialog, which) -> {
                    String newDate = etDate.getText() != null ? etDate.getText().toString().trim() : "";
                    String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
                    Long newPrice = priceStr.isEmpty() ? null : Long.parseLong(priceStr);

                    if (newDate.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập ngày kết thúc mới", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.extendContract(contractId, newDate, newPrice).observe(this, resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(this, "Đã gia hạn hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                            loadContract();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddRoommateDialog() {
        tenantViewModel.getTenants().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                List<Tenant> tenants = resource.getData();
                List<String> names = new ArrayList<>();
                for (Tenant t : tenants) {
                    names.add(t.getFullName() + " (" + t.getPhone() + ")");
                }

                final int[] selectedIndex = {0};
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Thêm người ở cùng phòng")
                        .setSingleChoiceItems(names.toArray(new String[0]), 0, (d, which) -> selectedIndex[0] = which)
                        .setPositiveButton("Thêm", (dialog, which) -> {
                            int pos = selectedIndex[0];
                            if (pos >= 0 && pos < tenants.size()) {
                                viewModel.addRoommate(contractId, tenants.get(pos).getId()).observe(this, res -> {
                                    if (res != null && res.isSuccess()) {
                                        Toast.makeText(this, "Đã thêm người ở cùng!", Toast.LENGTH_SHORT).show();
                                        loadContract();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    private void confirmEndContract() {
        new AlertDialog.Builder(this)
                .setTitle("Kết thúc hợp đồng")
                .setMessage("Bạn có chắc chắn muốn thanh lý và kết thúc hợp đồng này không? Phòng sẽ được chuyển về trạng thái 'Còn trống'.")
                .setPositiveButton("Kết thúc", (dialog, which) -> {
                    viewModel.endContract(contractId).observe(this, resource -> {
                        if (resource != null && resource.isSuccess()) {
                            Toast.makeText(this, "Hợp đồng đã kết thúc thành công!", Toast.LENGTH_SHORT).show();
                            loadContract();
                        } else if (resource != null && resource.isError()) {
                            Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
