package com.nhatro.app.ui.contracts;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ActivityAddEditContractBinding;
import com.nhatro.app.utils.DatePickerHelper;
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
    private long targetRoomId = -1;
    private int selectedRoomIndex = -1;
    private int selectedTenantIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditContractBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        contractViewModel = new ViewModelProvider(this).get(ContractViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Đọc thông tin phòng được chọn từ màn hình Chi tiết phòng (nếu có)
        targetRoomId = getIntent().getLongExtra("selected_room_id", -1);
        if (targetRoomId == -1) {
            Room passedRoom = (Room) getIntent().getSerializableExtra("room");
            if (passedRoom != null && passedRoom.getId() != null) {
                targetRoomId = passedRoom.getId();
            }
        }
        String selectedRoomCode = getIntent().getStringExtra("selected_room_code");
        if (selectedRoomCode != null && !selectedRoomCode.isEmpty()) {
            binding.toolbar.setTitle("Lập HĐ - Phòng " + selectedRoomCode);
        }

        setupDatePickers();

        loadRooms();
        loadTenants();

        binding.btnSaveContract.setOnClickListener(v -> saveContract());
    }

    private void setupDatePickers() {
        binding.etStartDate.setText(DateUtils.getTodayIso());
        binding.etEndDate.setText(DateUtils.addMonths(DateUtils.getTodayIso(), 12));

        DatePickerHelper.attachDatePicker(getSupportFragmentManager(), binding.etStartDate, binding.tilStartDate, "Chọn ngày bắt đầu");
        DatePickerHelper.attachDatePicker(getSupportFragmentManager(), binding.etEndDate, binding.tilEndDate, "Chọn ngày kết thúc");
    }

    private void loadRooms() {
        roomViewModel.getRooms().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                roomsList = resource.getData();
                List<String> roomLabels = new ArrayList<>();
                int targetIndex = -1;
                for (int i = 0; i < roomsList.size(); i++) {
                    Room r = roomsList.get(i);
                    String statusDesc;
                    if (r.isAvailable()) {
                        statusDesc = "Trống";
                    } else if (r.isOccupied()) {
                        String tenantInfo = (r.getTenantName() != null && !r.getTenantName().isEmpty()) ? " - " + r.getTenantName() : "";
                        statusDesc = "Đang thuê" + tenantInfo;
                    } else {
                        statusDesc = "Bảo trì";
                    }
                    roomLabels.add(r.getRoomCode() + " (" + statusDesc + ") - " + r.getPrice() + " đ");
                    if (targetRoomId != -1 && r.getId() != null && r.getId() == targetRoomId) {
                        targetIndex = i;
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomLabels);
                binding.actvRoom.setAdapter(adapter);

                binding.actvRoom.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedText = (String) parent.getItemAtPosition(position);
                    selectedRoomIndex = roomLabels.indexOf(selectedText);
                    if (selectedRoomIndex >= 0 && selectedRoomIndex < roomsList.size()) {
                        Room selected = roomsList.get(selectedRoomIndex);
                        binding.etRentPrice.setText(String.valueOf(selected.getPrice()));
                        binding.etDeposit.setText(String.valueOf(selected.getPrice()));
                    }
                });

                // Tự động trỏ tới phòng đang xem chi tiết
                if (targetIndex >= 0) {
                    selectedRoomIndex = targetIndex;
                    binding.actvRoom.setText(roomLabels.get(targetIndex), false);
                    Room selected = roomsList.get(targetIndex);
                    binding.etRentPrice.setText(String.valueOf(selected.getPrice()));
                    binding.etDeposit.setText(String.valueOf(selected.getPrice()));
                }
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tenantLabels);
                binding.actvTenant.setAdapter(adapter);

                binding.actvTenant.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedText = (String) parent.getItemAtPosition(position);
                    selectedTenantIndex = tenantLabels.indexOf(selectedText);
                });
            }
        });
    }

    private void saveContract() {
        if (roomsList.isEmpty() || tenantsList.isEmpty()) {
            Toast.makeText(this, "Cần có ít nhất một phòng và một khách thuê để lập hợp đồng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoomIndex < 0 || selectedTenantIndex < 0) {
            Toast.makeText(this, "Vui lòng chọn phòng và khách thuê đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        Room selectedRoom = roomsList.get(selectedRoomIndex);
        Tenant selectedTenant = tenantsList.get(selectedTenantIndex);

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

        // Kiểm tra nếu phòng đang có khách thuê, cảnh báo xác nhận kết thúc HĐ cũ
        if (selectedRoom.isOccupied()) {
            String currentTenant = (selectedRoom.getTenantName() != null && !selectedRoom.getTenantName().isEmpty())
                    ? selectedRoom.getTenantName()
                    : "khách hiện tại";
            String currentEnd = (selectedRoom.getContractEnd() != null && !selectedRoom.getContractEnd().isEmpty())
                    ? " (Hạn HĐ cũ: " + DateUtils.formatDateVN(selectedRoom.getContractEnd()) + ")"
                    : "";

            new AlertDialog.Builder(this)
                    .setTitle("Phòng đang có hợp đồng!")
                    .setMessage("Phòng " + selectedRoom.getRoomCode() + " hiện đang được thuê bởi " + currentTenant + currentEnd + ".\n\nBạn có muốn KẾT THÚC hợp đồng cũ để kích hoạt hợp đồng mới cho " + selectedTenant.getFullName() + " không?")
                    .setPositiveButton("Kết thúc HĐ cũ & Lưu", (dialog, which) -> {
                        executeSaveContract(selectedRoom, selectedTenant, startDate, endDate, deposit, rentPrice, true);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        executeSaveContract(selectedRoom, selectedTenant, startDate, endDate, deposit, rentPrice, false);
    }

    private void executeSaveContract(Room selectedRoom, Tenant selectedTenant, String startDate, String endDate, long deposit, long rentPrice, boolean forceTerminate) {
        binding.btnSaveContract.setEnabled(false);

        contractViewModel.createContract(selectedRoom.getId(), selectedTenant.getId(), startDate, endDate.isEmpty() ? null : endDate, deposit, rentPrice, null, forceTerminate)
                .observe(this, resource -> {
                    if (resource == null) return;
                    if (resource.isSuccess()) {
                        Toast.makeText(this, "Lập hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (resource.isError()) {
                        binding.btnSaveContract.setEnabled(true);
                        String errMsg = resource.getMessage() != null ? resource.getMessage() : "Lỗi lập hợp đồng";
                        if (errMsg.contains("đang có hợp đồng hiệu lực") && !forceTerminate) {
                            new AlertDialog.Builder(AddEditContractActivity.this)
                                    .setTitle("Phòng đang có hợp đồng!")
                                    .setMessage(errMsg + "\n\nBạn có muốn KẾT THÚC hợp đồng cũ để kích hoạt hợp đồng mới cho " + selectedTenant.getFullName() + " không?")
                                    .setPositiveButton("Kết thúc HĐ cũ & Lưu", (dialog, which) -> {
                                        executeSaveContract(selectedRoom, selectedTenant, startDate, endDate, deposit, rentPrice, true);
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        } else {
                            Toast.makeText(AddEditContractActivity.this, errMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
