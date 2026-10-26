package com.nhatro.app.ui.contracts;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ActivityContractDetailBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.viewmodel.ContractViewModel;
import com.nhatro.app.viewmodel.TenantViewModel;
import java.util.ArrayList;
import java.util.List;

public class ContractDetailActivity extends AppCompatActivity {
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
        } else {
            binding.tvDetailStatus.setText("Đã kết thúc");
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_badge_maintenance);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_maintenance_text));
            binding.btnEndContract.setEnabled(false);
            binding.btnEndContract.setText("Hợp đồng đã kết thúc");
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

    private void showAddRoommateDialog() {
        tenantViewModel.getTenants().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                List<Tenant> tenants = resource.getData();
                List<String> names = new ArrayList<>();
                for (Tenant t : tenants) {
                    names.add(t.getFullName() + " (" + t.getPhone() + ")");
                }

                Spinner spinner = new Spinner(this);
                spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));

                new AlertDialog.Builder(this)
                        .setTitle("Thêm người ở cùng phòng")
                        .setView(spinner)
                        .setPositiveButton("Thêm", (dialog, which) -> {
                            int pos = spinner.getSelectedItemPosition();
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
