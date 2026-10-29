package com.nhatro.app.ui.meter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ActivityAddMeterReadingBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.utils.DatePickerHelper;
import com.nhatro.app.viewmodel.MeterReadingViewModel;
import com.nhatro.app.viewmodel.RoomViewModel;
import com.nhatro.app.viewmodel.ServiceViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMeterReadingActivity extends BaseActivity {
    private ActivityAddMeterReadingBinding binding;
    private MeterReadingViewModel meterViewModel;
    private RoomViewModel roomViewModel;
    private ServiceViewModel serviceViewModel;

    private List<Room> roomsList = new ArrayList<>();
    private List<String> roomLabels = new ArrayList<>();
    private int selectedRoomIndex = -1;

    private ServiceItem electricService;
    private ServiceItem waterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMeterReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        meterViewModel = new ViewModelProvider(this).get(MeterReadingViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        Calendar now = Calendar.getInstance();
        int defaultMonth = getIntent().getIntExtra("extra_month", now.get(Calendar.MONTH) + 1);
        int defaultYear = getIntent().getIntExtra("extra_year", now.get(Calendar.YEAR));
        binding.etMonth.setText(String.valueOf(defaultMonth));
        binding.etYear.setText(String.valueOf(defaultYear));

        DatePickerHelper.setupMonthDropdown(this, binding.etMonth);
        DatePickerHelper.setupYearDropdown(this, binding.etYear);

        setupCalculations();
        loadRooms();
        loadMeteredServices();

        binding.btnSaveReading.setOnClickListener(v -> saveReadings(false));
        binding.btnSaveAndNext.setOnClickListener(v -> saveReadings(true));
    }

    private void loadRooms() {
        roomViewModel.getRooms().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                roomsList = resource.getData();
                roomLabels.clear();
                for (Room r : roomsList) {
                    roomLabels.add("Phòng " + r.getRoomCode());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomLabels);
                binding.actvRoom.setAdapter(adapter);

                binding.actvRoom.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedText = (String) parent.getItemAtPosition(position);
                    selectedRoomIndex = roomLabels.indexOf(selectedText);
                    checkAndAutoFillOldValues();
                });

                if (!roomsList.isEmpty() && selectedRoomIndex == -1) {
                    long targetRoomId = getIntent().getLongExtra("extra_room_id", -1L);
                    int targetIndex = 0;
                    if (targetRoomId != -1L) {
                        for (int i = 0; i < roomsList.size(); i++) {
                            if (roomsList.get(i).getId() != null && roomsList.get(i).getId().equals(targetRoomId)) {
                                targetIndex = i;
                                break;
                            }
                        }
                    }
                    selectedRoomIndex = targetIndex;
                    binding.actvRoom.setText(roomLabels.get(targetIndex), false);
                    checkAndAutoFillOldValues();

                    if (getIntent().hasExtra("extra_is_electric")) {
                        boolean wantElectric = getIntent().getBooleanExtra("extra_is_electric", true);
                        if (wantElectric) {
                            binding.etElectricNewValue.requestFocus();
                        } else {
                            binding.etWaterNewValue.requestFocus();
                        }
                    }
                }
            }
        });
    }

    private void loadMeteredServices() {
        serviceViewModel.getServices().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                List<ServiceItem> meteredList = new ArrayList<>();
                for (ServiceItem s : resource.getData()) {
                    if (Boolean.TRUE.equals(s.getIsMetered())) {
                        meteredList.add(s);
                        String name = s.getName() != null ? s.getName().toLowerCase() : "";
                        String unit = s.getUnit() != null ? s.getUnit().toLowerCase() : "";
                        if (name.contains("điện") || name.contains("dien") || unit.contains("kwh")) {
                            electricService = s;
                        } else if (name.contains("nước") || name.contains("nuoc") || unit.contains("m3") || unit.contains("m³")) {
                            waterService = s;
                        }
                    }
                }

                if (electricService == null && !meteredList.isEmpty()) {
                    electricService = meteredList.get(0);
                }
                if (waterService == null && meteredList.size() > 1) {
                    waterService = meteredList.get(1);
                }

                if (electricService != null) {
                    binding.tvElectricPriceBadge.setText(
                            CurrencyUtils.formatVND(electricService.getUnitPrice()) + " / " + electricService.getUnit()
                    );
                }
                if (waterService != null) {
                    binding.tvWaterPriceBadge.setText(
                            CurrencyUtils.formatVND(waterService.getUnitPrice()) + " / " + waterService.getUnit()
                    );
                }

                checkAndAutoFillOldValues();
            }
        });
    }

    private void checkAndAutoFillOldValues() {
        if (selectedRoomIndex < 0 || selectedRoomIndex >= roomsList.size()) {
            return;
        }

        Room room = roomsList.get(selectedRoomIndex);

        // Xóa chỉ số mới của phòng trước đó
        binding.etElectricNewValue.setText("");
        binding.etWaterNewValue.setText("");

        // Tự động điền số cũ Điện từ kỳ trước
        if (electricService != null) {
            meterViewModel.getLatestReading(room.getId(), electricService.getId()).observe(this, resource -> {
                if (resource != null && resource.isSuccess()) {
                    MeterReading latest = resource.getData();
                    if (latest != null && latest.getNewValue() != null) {
                        binding.etElectricOldValue.setText(String.valueOf(latest.getNewValue()));
                    } else {
                        binding.etElectricOldValue.setText("0");
                    }
                    updateCalculationPreview();
                }
            });
        }

        // Tự động điền số cũ Nước từ kỳ trước
        if (waterService != null) {
            meterViewModel.getLatestReading(room.getId(), waterService.getId()).observe(this, resource -> {
                if (resource != null && resource.isSuccess()) {
                    MeterReading latest = resource.getData();
                    if (latest != null && latest.getNewValue() != null) {
                        binding.etWaterOldValue.setText(String.valueOf(latest.getNewValue()));
                    } else {
                        binding.etWaterOldValue.setText("0");
                    }
                    updateCalculationPreview();
                }
            });
        }
    }

    private void setupCalculations() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCalculationPreview();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etElectricOldValue.addTextChangedListener(watcher);
        binding.etElectricNewValue.addTextChangedListener(watcher);
        binding.etWaterOldValue.addTextChangedListener(watcher);
        binding.etWaterNewValue.addTextChangedListener(watcher);
    }

    private void updateCalculationPreview() {
        long electricCost = 0L;
        long waterCost = 0L;

        // Tính Điện
        try {
            String oldStr = binding.etElectricOldValue.getText() != null ? binding.etElectricOldValue.getText().toString().trim() : "0";
            String newStr = binding.etElectricNewValue.getText() != null ? binding.etElectricNewValue.getText().toString().trim() : "";

            double oldVal = oldStr.isEmpty() ? 0 : Double.parseDouble(oldStr);
            double newVal = newStr.isEmpty() ? oldVal : Double.parseDouble(newStr);
            double usage = Math.max(0, newVal - oldVal);

            long unitPrice = electricService != null && electricService.getUnitPrice() != null ? electricService.getUnitPrice() : 3500L;
            String unit = electricService != null && electricService.getUnit() != null ? electricService.getUnit() : "kWh";
            electricCost = (long) (usage * unitPrice);

            binding.tvElectricPreview.setText(String.format("Tiêu thụ: %.1f %s • Thành tiền: %s", usage, unit, CurrencyUtils.formatVND(electricCost)));
        } catch (Exception ignored) {}

        // Tính Nước
        try {
            String oldStr = binding.etWaterOldValue.getText() != null ? binding.etWaterOldValue.getText().toString().trim() : "0";
            String newStr = binding.etWaterNewValue.getText() != null ? binding.etWaterNewValue.getText().toString().trim() : "";

            double oldVal = oldStr.isEmpty() ? 0 : Double.parseDouble(oldStr);
            double newVal = newStr.isEmpty() ? oldVal : Double.parseDouble(newStr);
            double usage = Math.max(0, newVal - oldVal);

            long unitPrice = waterService != null && waterService.getUnitPrice() != null ? waterService.getUnitPrice() : 25000L;
            String unit = waterService != null && waterService.getUnit() != null ? waterService.getUnit() : "m³";
            waterCost = (long) (usage * unitPrice);

            binding.tvWaterPreview.setText(String.format("Tiêu thụ: %.1f %s • Thành tiền: %s", usage, unit, CurrencyUtils.formatVND(waterCost)));
        } catch (Exception ignored) {}

        // Tổng cộng
        binding.tvTotalPreview.setText(CurrencyUtils.formatVND(electricCost + waterCost));
    }

    private void setButtonsEnabled(boolean enabled) {
        binding.btnSaveReading.setEnabled(enabled);
        binding.btnSaveAndNext.setEnabled(enabled);
    }

    private void saveReadings(boolean moveToNext) {
        if (selectedRoomIndex < 0 || selectedRoomIndex >= roomsList.size()) {
            Toast.makeText(this, "Vui lòng chọn phòng", Toast.LENGTH_SHORT).show();
            return;
        }

        Room room = roomsList.get(selectedRoomIndex);
        String monthStr = binding.etMonth.getText() != null ? binding.etMonth.getText().toString().trim() : "1";
        String yearStr = binding.etYear.getText() != null ? binding.etYear.getText().toString().trim() : "2024";
        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        String elecOldStr = binding.etElectricOldValue.getText() != null ? binding.etElectricOldValue.getText().toString().trim() : "";
        String elecNewStr = binding.etElectricNewValue.getText() != null ? binding.etElectricNewValue.getText().toString().trim() : "";
        boolean hasElectricInput = !elecNewStr.isEmpty();

        String waterOldStr = binding.etWaterOldValue.getText() != null ? binding.etWaterOldValue.getText().toString().trim() : "";
        String waterNewStr = binding.etWaterNewValue.getText() != null ? binding.etWaterNewValue.getText().toString().trim() : "";
        boolean hasWaterInput = !waterNewStr.isEmpty();

        if (!hasElectricInput && !hasWaterInput) {
            Toast.makeText(this, "Vui lòng nhập chỉ số mới cho Điện hoặc Nước", Toast.LENGTH_SHORT).show();
            return;
        }

        MeterReading elecReading = null;
        if (hasElectricInput) {
            if (electricService == null) {
                Toast.makeText(this, "Không tìm thấy dịch vụ Điện trong hệ thống", Toast.LENGTH_SHORT).show();
                return;
            }
            double oldVal = elecOldStr.isEmpty() ? 0 : Double.parseDouble(elecOldStr);
            double newVal = Double.parseDouble(elecNewStr);
            if (newVal < oldVal) {
                Toast.makeText(this, "Chỉ số mới của Điện phải lớn hơn hoặc bằng chỉ số cũ", Toast.LENGTH_SHORT).show();
                return;
            }
            elecReading = new MeterReading();
            elecReading.setRoomId(room.getId());
            elecReading.setServiceId(electricService.getId());
            elecReading.setMonth(month);
            elecReading.setYear(year);
            elecReading.setOldValue(oldVal);
            elecReading.setNewValue(newVal);
        }

        MeterReading waterReading = null;
        if (hasWaterInput) {
            if (waterService == null) {
                Toast.makeText(this, "Không tìm thấy dịch vụ Nước trong hệ thống", Toast.LENGTH_SHORT).show();
                return;
            }
            double oldVal = waterOldStr.isEmpty() ? 0 : Double.parseDouble(waterOldStr);
            double newVal = Double.parseDouble(waterNewStr);
            if (newVal < oldVal) {
                Toast.makeText(this, "Chỉ số mới của Nước phải lớn hơn hoặc bằng chỉ số cũ", Toast.LENGTH_SHORT).show();
                return;
            }
            waterReading = new MeterReading();
            waterReading.setRoomId(room.getId());
            waterReading.setServiceId(waterService.getId());
            waterReading.setMonth(month);
            waterReading.setYear(year);
            waterReading.setOldValue(oldVal);
            waterReading.setNewValue(newVal);
        }

        setButtonsEnabled(false);
        executeSave(elecReading, waterReading, moveToNext);
    }

    private void executeSave(MeterReading elec, MeterReading water, boolean moveToNext) {
        if (elec != null && water != null) {
            meterViewModel.createMeterReading(elec).observe(this, res1 -> {
                if (res1 == null) return;
                if (res1.isSuccess()) {
                    meterViewModel.createMeterReading(water).observe(this, res2 -> {
                        if (res2 == null) return;
                        if (res2.isSuccess()) {
                            handleSaveSuccess(moveToNext);
                        } else if (res2.isError()) {
                            setButtonsEnabled(true);
                            Toast.makeText(this, "Đã lưu số điện, nhưng lưu số nước bị lỗi: " + res2.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (res1.isError()) {
                    setButtonsEnabled(true);
                    Toast.makeText(this, "Lỗi lưu số điện: " + res1.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else if (elec != null) {
            meterViewModel.createMeterReading(elec).observe(this, res -> {
                if (res == null) return;
                if (res.isSuccess()) {
                    handleSaveSuccess(moveToNext);
                } else if (res.isError()) {
                    setButtonsEnabled(true);
                    Toast.makeText(this, res.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else if (water != null) {
            meterViewModel.createMeterReading(water).observe(this, res -> {
                if (res == null) return;
                if (res.isSuccess()) {
                    handleSaveSuccess(moveToNext);
                } else if (res.isError()) {
                    setButtonsEnabled(true);
                    Toast.makeText(this, res.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void handleSaveSuccess(boolean moveToNext) {
        if (!moveToNext) {
            Toast.makeText(this, "Ghi chỉ số thành công!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (selectedRoomIndex + 1 < roomsList.size()) {
            selectedRoomIndex++;
            Room nextRoom = roomsList.get(selectedRoomIndex);
            binding.actvRoom.setText(roomLabels.get(selectedRoomIndex), false);
            setButtonsEnabled(true);
            Toast.makeText(this, "Đã lưu! Chuyển sang " + roomLabels.get(selectedRoomIndex), Toast.LENGTH_SHORT).show();
            checkAndAutoFillOldValues();
            binding.etElectricNewValue.requestFocus();
        } else {
            Toast.makeText(this, "Đã lưu xong toàn bộ các phòng trong danh sách!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
