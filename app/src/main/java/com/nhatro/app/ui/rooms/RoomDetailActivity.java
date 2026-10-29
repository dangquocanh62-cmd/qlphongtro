package com.nhatro.app.ui.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.databinding.ActivityRoomDetailBinding;
import com.nhatro.app.ui.base.BaseActivity;
import com.nhatro.app.ui.contracts.AddEditContractActivity;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import com.nhatro.app.viewmodel.RoomViewModel;

public class RoomDetailActivity extends BaseActivity {
    private ActivityRoomDetailBinding binding;
    private RoomViewModel viewModel;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        room = (Room) getIntent().getSerializableExtra("room");
        if (room == null) {
            finish();
            return;
        }

        displayRoomInfo();
        setupActions();
    }

    private void displayRoomInfo() {
        binding.tvDetailRoomCode.setText("Phòng " + room.getRoomCode());
        binding.tvDetailFloorArea.setText(String.format("Tầng %d • Diện tích %.1f m²", room.getFloor(), room.getArea()));
        binding.tvDetailPrice.setText("Giá thuê: " + CurrencyUtils.formatVND(room.getPrice()) + " / tháng");

        if (room.getNote() != null && !room.getNote().isEmpty()) {
            binding.tvDetailNote.setText(room.getNote());
        } else {
            binding.tvDetailNote.setText("Không có ghi chú");
        }

        if (room.isAvailable()) {
            binding.tvDetailStatusBadge.setText("Còn trống");
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_available);
            binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
            binding.cardOccupiedInfo.setVisibility(View.GONE);
            binding.btnCreateContract.setVisibility(View.VISIBLE);
        } else if (room.isOccupied()) {
            binding.tvDetailStatusBadge.setText("Đang thuê");
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
            binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_occupied_text));
            binding.cardOccupiedInfo.setVisibility(View.VISIBLE);
            binding.btnCreateContract.setVisibility(View.GONE);

            binding.tvOccupiedTenant.setText("Khách đại diện: " + (room.getTenantName() != null ? room.getTenantName() : "---"));
            binding.tvOccupiedPhone.setText("Số điện thoại: " + (room.getTenantPhone() != null ? room.getTenantPhone() : "---"));
            binding.tvOccupiedDates.setText("Hợp đồng: " + DateUtils.formatDateVN(room.getContractStart()) + " -> " +
                    (room.getContractEnd() != null ? DateUtils.formatDateVN(room.getContractEnd()) : "Không thời hạn"));
        } else {
            binding.tvDetailStatusBadge.setText("Bảo trì");
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_maintenance);
            binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_maintenance_text));
            binding.cardOccupiedInfo.setVisibility(View.GONE);
            binding.btnCreateContract.setVisibility(View.GONE);
        }
    }

    private void setupActions() {
        binding.btnEditRoom.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRoomActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
            finish();
        });

        binding.btnCreateContract.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditContractActivity.class);
            intent.putExtra("selected_room_id", room.getId());
            intent.putExtra("selected_room_code", room.getRoomCode());
            intent.putExtra("room", room);
            startActivity(intent);
        });

        binding.btnDeleteRoom.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa phòng")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng " + room.getRoomCode() + " không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteRoom())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void deleteRoom() {
        viewModel.deleteRoom(room.getId()).observe(this, resource -> {
            if (resource == null) return;
            if (resource.isSuccess()) {
                Toast.makeText(this, "Đã xóa phòng!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.isError()) {
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
