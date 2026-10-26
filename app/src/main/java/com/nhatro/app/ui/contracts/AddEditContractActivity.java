package com.nhatro.app.ui.contracts;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ActivityAddEditContractBinding;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.viewmodel.ContractViewModel;
import com.nhatro.app.viewmodel.RoomViewModel;
import com.nhatro.app.viewmodel.TenantViewModel;
import java.util.ArrayList;
import java.util.List;

public class AddEditContractActivity extends AppCompatActivity {
    private ActivityAddEditContractBinding binding;
    private ContractViewModel contractViewModel;
    private RoomViewModel roomViewModel;
    private TenantViewModel tenantViewModel;

    private List<Room> roomsList = new ArrayList<>();
    private List<Tenant> tenantsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditContractBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        contractViewModel = new ViewModelProvider(this).get(ContractViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.etStartDate.setText(DateUtils.getTodayIso());

        loadRooms();
        loadTenants();

        binding.btnSaveContract.setOnClickListener(v -> saveContract());
    }

    private void loadRooms() {
        roomViewModel.getRooms().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                roomsList = resource.getData();
                List<String> roomLabels = new ArrayList<>();
                for (Room r : roomsList) {
                    roomLabels.add(r.getRoomCode() + " (" + (r.isAvailable() ? "Trống" : r.getStatus()) + ") - " + r.getPrice() + " đ");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roomLabels);
                binding.spRoom.setAdapter(adapter);

                binding.spRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position >= 0 && position < roomsList.size()) {
                            Room selected = roomsList.get(position);
                            binding.etRentPrice.setText(String.valueOf(selected.getPrice()));
                            binding.etDeposit.setText(String.valueOf(selected.getPrice()));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
    }

    private void loadTenants() {
        tenantViewModel.getTenants().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                tenantsList = resource.getData();
                List<String> tenantLabels = new ArrayList<>();
                for (Tenant t : tenantsList) {
                    tenantLabels.add(t.getFullName() + " (" + t.getPhone() + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tenantLabels);
                binding.spTenant.setAdapter(adapter);
            }
        });
    }

    private void saveContract() {
        if (roomsList.isEmpty() || tenantsList.isEmpty()) {
            Toast.makeText(this, "Cần có ít nhất một phòng và một khách thuê để lập hợp đồng", Toast.LENGTH_SHORT).show();
            return;
        }

        int roomIdx = binding.spRoom.getSelectedItemPosition();
        int tenantIdx = binding.spTenant.getSelectedItemPosition();

        if (roomIdx < 0 || tenantIdx < 0) return;

        Room selectedRoom = roomsList.get(roomIdx);
        Tenant selectedTenant = tenantsList.get(tenantIdx);

        String startDate = binding.etStartDate.getText() != null ? binding.etStartDate.getText().toString().trim() : "";
        String endDate = binding.etEndDate.getText() != null ? binding.etEndDate.getText().toString().trim() : "";
        String rentPriceStr = binding.etRentPrice.getText() != null ? binding.etRentPrice.getText().toString().trim() : "0";
        String depositStr = binding.etDeposit.getText() != null ? binding.etDeposit.getText().toString().trim() : "0";

        if (startDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        long rentPrice = rentPriceStr.isEmpty() ? 0 : Long.parseLong(rentPriceStr);
        long deposit = depositStr.isEmpty() ? 0 : Long.parseLong(depositStr);

        binding.btnSaveContract.setEnabled(false);

        contractViewModel.createContract(selectedRoom.getId(), selectedTenant.getId(), startDate, endDate.isEmpty() ? null : endDate, deposit, rentPrice, null)
                .observe(this, resource -> {
                    if (resource == null) return;
                    if (resource.isSuccess()) {
                        Toast.makeText(this, "Lập hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (resource.isError()) {
                        binding.btnSaveContract.setEnabled(true);
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
