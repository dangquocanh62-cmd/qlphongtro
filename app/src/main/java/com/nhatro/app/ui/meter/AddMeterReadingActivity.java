package com.nhatro.app.ui.meter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ActivityAddMeterReadingBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.viewmodel.MeterReadingViewModel;
import com.nhatro.app.viewmodel.RoomViewModel;
import com.nhatro.app.viewmodel.ServiceViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMeterReadingActivity extends AppCompatActivity {
    private ActivityAddMeterReadingBinding binding;
    private MeterReadingViewModel meterViewModel;
    private RoomViewModel roomViewModel;
    private ServiceViewModel serviceViewModel;

    private List<Room> roomsList = new ArrayList<>();
    private List<ServiceItem> meteredServices = new ArrayList<>();

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
        binding.etMonth.setText(String.valueOf(now.get(Calendar.MONTH) + 1));
        binding.etYear.setText(String.valueOf(now.get(Calendar.YEAR)));

        loadRooms();
        loadMeteredServices();
        setupCalculations();

        binding.btnSaveReading.setOnClickListener(v -> saveReading());
    }

    private void loadRooms() {
        roomViewModel.getRooms().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                roomsList = resource.getData();
                List<String> roomLabels = new ArrayList<>();
                for (Room r : roomsList) {
                    roomLabels.add("Phòng " + r.getRoomCode());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roomLabels);
                binding.spRoom.setAdapter(adapter);

                binding.spRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        checkAndAutoFillOldValue();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
    }

    private void loadMeteredServices() {
        serviceViewModel.getServices().observe(this, resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                meteredServices.clear();
                List<String> serviceLabels = new ArrayList<>();
                for (ServiceItem s : resource.getData()) {
                    if (s.getIsMetered()) {
                        meteredServices.add(s);
                        serviceLabels.add(s.getName() + " (" + CurrencyUtils.formatVND(s.getUnitPrice()) + " / " + s.getUnit() + ")");
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, serviceLabels);
                binding.spService.setAdapter(adapter);

                binding.spService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        checkAndAutoFillOldValue();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
    }

    private void checkAndAutoFillOldValue() {
        int roomPos = binding.spRoom.getSelectedItemPosition();
        int servicePos = binding.spService.getSelectedItemPosition();

        if (roomPos < 0 || roomPos >= roomsList.size() || servicePos < 0 || servicePos >= meteredServices.size()) {
            return;
        }

        Room r = roomsList.get(roomPos);
        ServiceItem s = meteredServices.get(servicePos);

        meterViewModel.getLatestReading(r.getId(), s.getId()).observe(this, resource -> {
            if (resource != null && resource.isSuccess()) {
                MeterReading latest = resource.getData();
                if (latest != null) {
                    // Pre-fill old value with previous new_value!
                    binding.etOldValue.setText(String.valueOf(latest.getNewValue()));
                } else {
                    binding.etOldValue.setText("0");
                }
                updateCalculationPreview();
            }
        });
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

        binding.etOldValue.addTextChangedListener(watcher);
        binding.etNewValue.addTextChangedListener(watcher);
    }

    private void updateCalculationPreview() {
        try {
            String oldStr = binding.etOldValue.getText() != null ? binding.etOldValue.getText().toString().trim() : "0";
            String newStr = binding.etNewValue.getText() != null ? binding.etNewValue.getText().toString().trim() : "0";

            double oldVal = oldStr.isEmpty() ? 0 : Double.parseDouble(oldStr);
            double newVal = newStr.isEmpty() ? 0 : Double.parseDouble(newStr);

            double usage = Math.max(0, newVal - oldVal);

            int servicePos = binding.spService.getSelectedItemPosition();
            if (servicePos >= 0 && servicePos < meteredServices.size()) {
                ServiceItem s = meteredServices.get(servicePos);
                long cost = (long) (usage * s.getUnitPrice());
                binding.tvPreviewUsage.setText(String.format("Tiêu thụ: %.1f %s • Dự tính tiền: %s", usage, s.getUnit(), CurrencyUtils.formatVND(cost)));
            }
        } catch (Exception ignored) {}
    }

    private void saveReading() {
        int roomPos = binding.spRoom.getSelectedItemPosition();
        int servicePos = binding.spService.getSelectedItemPosition();

        if (roomPos < 0 || servicePos < 0) {
            Toast.makeText(this, "Vui lòng chọn phòng và dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        Room room = roomsList.get(roomPos);
        ServiceItem service = meteredServices.get(servicePos);

        String monthStr = binding.etMonth.getText() != null ? binding.etMonth.getText().toString().trim() : "1";
        String yearStr = binding.etYear.getText() != null ? binding.etYear.getText().toString().trim() : "2024";
        String oldStr = binding.etOldValue.getText() != null ? binding.etOldValue.getText().toString().trim() : "";
        String newStr = binding.etNewValue.getText() != null ? binding.etNewValue.getText().toString().trim() : "";

        if (oldStr.isEmpty() || newStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập cả chỉ số cũ và chỉ số mới", Toast.LENGTH_SHORT).show();
            return;
        }

        double oldVal = Double.parseDouble(oldStr);
        double newVal = Double.parseDouble(newStr);

        if (newVal < oldVal) {
            Toast.makeText(this, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ", Toast.LENGTH_SHORT).show();
            return;
        }

        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        MeterReading reading = new MeterReading();
        reading.setRoomId(room.getId());
        reading.setServiceId(service.getId());
        reading.setMonth(month);
        reading.setYear(year);
        reading.setOldValue(oldVal);
        reading.setNewValue(newVal);

        binding.btnSaveReading.setEnabled(false);

        meterViewModel.createMeterReading(reading).observe(this, resource -> {
            if (resource == null) return;
            if (resource.isSuccess()) {
                Toast.makeText(this, "Ghi chỉ số thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.isError()) {
                binding.btnSaveReading.setEnabled(true);
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
