package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.FloorGroup;
import com.nhatro.app.databinding.ItemFloorPlanBinding;
import java.util.ArrayList;
import java.util.List;

public class FloorPlanAdapter extends RecyclerView.Adapter<FloorPlanAdapter.FloorViewHolder> {
    private List<FloorGroup> floorGroups = new ArrayList<>();
    private final RoomAdapter.OnRoomClickListener roomClickListener;

    public FloorPlanAdapter(RoomAdapter.OnRoomClickListener roomClickListener) {
        this.roomClickListener = roomClickListener;
    }

    public void setFloorGroups(List<FloorGroup> floorGroups) {
        this.floorGroups = floorGroups != null ? floorGroups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFloorPlanBinding binding = ItemFloorPlanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FloorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        holder.bind(floorGroups.get(position));
    }

    @Override
    public int getItemCount() {
        return floorGroups.size();
    }

    class FloorViewHolder extends RecyclerView.ViewHolder {
        private final ItemFloorPlanBinding binding;
        private final RoomSquareAdapter squareAdapter;

        public FloorViewHolder(ItemFloorPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            squareAdapter = new RoomSquareAdapter(roomClickListener);
            binding.rvFloorRooms.setLayoutManager(new GridLayoutManager(itemView.getContext(), 3));
            binding.rvFloorRooms.setAdapter(squareAdapter);
            binding.rvFloorRooms.setNestedScrollingEnabled(false);
        }

        public void bind(FloorGroup group) {
            binding.tvFloorTitle.setText(group.getFloorDisplayName());
            binding.tvFloorTotalBadge.setText(group.getTotalRooms() + " phòng");

            StringBuilder stats = new StringBuilder();
            if (group.getOccupiedCount() > 0) {
                stats.append(group.getOccupiedCount()).append(" đang thuê");
            }
            if (group.getAvailableCount() > 0) {
                if (stats.length() > 0) stats.append(" • ");
                stats.append(group.getAvailableCount()).append(" trống");
            }
            if (group.getMaintenanceCount() > 0) {
                if (stats.length() > 0) stats.append(" • ");
                stats.append(group.getMaintenanceCount()).append(" bảo trì");
            }
            binding.tvFloorStats.setText(stats.toString());

            squareAdapter.setRooms(group.getRooms());
        }
    }
}
