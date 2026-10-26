package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.databinding.ItemRoomBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private List<Room> rooms = new ArrayList<>();
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomBinding binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.bind(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomBinding binding;

        public RoomViewHolder(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoomClick(rooms.get(pos));
                }
            });
        }

        public void bind(Room room) {
            binding.tvRoomCode.setText(room.getRoomCode());
            binding.tvFloorArea.setText(String.format("Tầng %d • %.1f m²", room.getFloor(), room.getArea()));
            binding.tvPrice.setText(CurrencyUtils.formatVND(room.getPrice()) + " / tháng");

            if (room.isAvailable()) {
                binding.tvStatusBadge.setText("Còn trống");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_available);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_available_text));
                binding.layoutTenantInfo.setVisibility(View.GONE);
            } else if (room.isOccupied()) {
                binding.tvStatusBadge.setText("Đang thuê");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_occupied_text));
                binding.layoutTenantInfo.setVisibility(View.VISIBLE);
                binding.tvTenantName.setText("Khách: " + (room.getTenantName() != null ? room.getTenantName() : "---"));
                binding.tvTenantPhone.setText(room.getTenantPhone() != null ? room.getTenantPhone() : "");
            } else {
                binding.tvStatusBadge.setText("Bảo trì");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_maintenance);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_maintenance_text));
                binding.layoutTenantInfo.setVisibility(View.GONE);
            }

            if (room.getNote() != null && !room.getNote().trim().isEmpty()) {
                binding.tvNote.setVisibility(View.VISIBLE);
                binding.tvNote.setText(room.getNote());
            } else {
                binding.tvNote.setVisibility(View.GONE);
            }
        }
    }
}
