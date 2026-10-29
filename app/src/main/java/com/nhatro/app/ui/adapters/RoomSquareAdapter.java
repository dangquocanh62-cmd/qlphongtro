package com.nhatro.app.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.databinding.ItemRoomSquareBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class RoomSquareAdapter extends RecyclerView.Adapter<RoomSquareAdapter.SquareViewHolder> {
    private List<Room> rooms = new ArrayList<>();
    private final RoomAdapter.OnRoomClickListener listener;

    public RoomSquareAdapter(RoomAdapter.OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SquareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomSquareBinding binding = ItemRoomSquareBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SquareViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SquareViewHolder holder, int position) {
        holder.bind(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class SquareViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomSquareBinding binding;

        public SquareViewHolder(ItemRoomSquareBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.cardRoomSquare.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoomClick(rooms.get(pos));
                }
            });
        }

        public void bind(Room room) {
            binding.tvSquareRoomCode.setText(room.getRoomCode());

            if (room.isOccupied()) {
                // Đang thuê: Xanh dương chủ đạo
                binding.cardRoomSquare.setCardBackgroundColor(Color.parseColor("#EFF6FF"));
                binding.cardRoomSquare.setStrokeColor(Color.parseColor("#3B82F6"));
                binding.tvSquareRoomCode.setTextColor(Color.parseColor("#1D4ED8"));

                binding.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_occupied);
                binding.viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                binding.tvSquareStatus.setText("Đang thuê");
                binding.tvSquareStatus.setTextColor(Color.parseColor("#1E40AF"));

                String tenant = room.getTenantName();
                binding.tvSquareSubtitle.setText(tenant != null && !tenant.trim().isEmpty() ? tenant.trim() : "Đang thuê");
                binding.tvSquareSubtitle.setTextColor(Color.parseColor("#2563EB"));

            } else if (room.isAvailable()) {
                // Còn trống: Xanh lá chủ đạo
                binding.cardRoomSquare.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
                binding.cardRoomSquare.setStrokeColor(Color.parseColor("#22C55E"));
                binding.tvSquareRoomCode.setTextColor(Color.parseColor("#15803D"));

                binding.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_available);
                binding.viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));
                binding.tvSquareStatus.setText("Còn trống");
                binding.tvSquareStatus.setTextColor(Color.parseColor("#15803D"));

                binding.tvSquareSubtitle.setText(CurrencyUtils.formatVND(room.getPrice()));
                binding.tvSquareSubtitle.setTextColor(Color.parseColor("#16A34A"));

            } else {
                // Bảo trì: Vàng cam chủ đạo
                binding.cardRoomSquare.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
                binding.cardRoomSquare.setStrokeColor(Color.parseColor("#F59E0B"));
                binding.tvSquareRoomCode.setTextColor(Color.parseColor("#B45309"));

                binding.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_maintenance);
                binding.viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
                binding.tvSquareStatus.setText("Bảo trì");
                binding.tvSquareStatus.setTextColor(Color.parseColor("#B45309"));

                binding.tvSquareSubtitle.setText("Đang sửa");
                binding.tvSquareSubtitle.setTextColor(Color.parseColor("#D97706"));
            }
        }
    }
}
