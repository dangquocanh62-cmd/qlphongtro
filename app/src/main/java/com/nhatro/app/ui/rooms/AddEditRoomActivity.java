package com.nhatro.app.ui.rooms;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.databinding.ActivityAddEditRoomBinding;
import com.nhatro.app.viewmodel.RoomViewModel;

public class AddEditRoomActivity extends AppCompatActivity {
    private ActivityAddEditRoomBinding binding;
    private RoomViewModel viewModel;
    private Room existingRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        existingRoom = (Room) getIntent().getSerializableExtra("room");
        if (existingRoom != null) {
            binding.toolbar.setTitle("Sửa Thông Tin Phòng");
            binding.etRoomCode.setText(existingRoom.getRoomCode());
            binding.etFloor.setText(String.valueOf(existingRoom.getFloor()));
            binding.etArea.setText(String.valueOf(existingRoom.getArea()));
            binding.etPrice.setText(String.valueOf(existingRoom.getPrice()));
            binding.etNote.setText(existingRoom.getNote());

            if (existingRoom.isAvailable()) {
                binding.rbAvailable.setChecked(true);
            } else if (existingRoom.isOccupied()) {
                binding.rbOccupied.setChecked(true);
            } else {
                binding.rbMaintenance.setChecked(true);
            }
        }

        binding.btnSave.setOnClickListener(v -> saveRoom());
    }

    private void saveRoom() {
        String code = binding.etRoomCode.getText() != null ? binding.etRoomCode.getText().toString().trim() : "";
        String floorStr = binding.etFloor.getText() != null ? binding.etFloor.getText().toString().trim() : "1";
        String areaStr = binding.etArea.getText() != null ? binding.etArea.getText().toString().trim() : "0";
        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "0";
        String note = binding.etNote.getText() != null ? binding.etNote.getText().toString().trim() : "";

        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã phòng (vd: P101)", Toast.LENGTH_SHORT).show();
            return;
        }

        int floor = floorStr.isEmpty() ? 1 : Integer.parseInt(floorStr);
        double area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr);
        long price = priceStr.isEmpty() ? 0 : Long.parseLong(priceStr);

        String status = "AVAILABLE";
        if (binding.rbOccupied.isChecked()) {
            status = "OCCUPIED";
        } else if (binding.rbMaintenance.isChecked()) {
            status = "MAINTENANCE";
        }

        Room room = new Room(code, floor, area, price, status, note);

        binding.btnSave.setEnabled(false);

        if (existingRoom != null) {
            viewModel.updateRoom(existingRoom.getId(), room).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Cập nhật phòng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            viewModel.createRoom(room).observe(this, resource -> {
                if (resource == null) return;
                if (resource.isSuccess()) {
                    Toast.makeText(this, "Thêm phòng mới thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resource.isError()) {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
