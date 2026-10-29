package com.nhatro.app.ui.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhatro.app.R;
import com.nhatro.app.data.model.FloorGroup;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.databinding.FragmentRoomListBinding;
import com.nhatro.app.ui.adapters.FloorPlanAdapter;
import com.nhatro.app.ui.adapters.RoomAdapter;
import com.nhatro.app.viewmodel.RoomViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RoomListFragment extends Fragment {
    private FragmentRoomListBinding binding;
    private RoomViewModel viewModel;
    private RoomAdapter adapter;
    private FloorPlanAdapter floorPlanAdapter;
    private String currentStatusFilter = null;
    private boolean isFloorPlanMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoomListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        setupRecyclerViews();
        setupViewModeToggle();
        setupFilters();
        observeRooms();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddRoom.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditRoomActivity.class))
        );
    }

    private void setupRecyclerViews() {
        RoomAdapter.OnRoomClickListener clickListener = room -> {
            Intent intent = new Intent(requireContext(), RoomDetailActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
        };

        // 1. Adapter cho Sơ đồ tầng (Lưới ô vuông từng tầng)
        floorPlanAdapter = new FloorPlanAdapter(clickListener);
        binding.rvFloorPlan.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFloorPlan.setAdapter(floorPlanAdapter);

        // 2. Adapter cho Danh sách phòng truyền thống
        adapter = new RoomAdapter();
        adapter.setOnRoomClickListener(clickListener);
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRooms.setAdapter(adapter);
    }

    private void setupViewModeToggle() {
        // Mặc định chọn chế độ Sơ đồ tầng
        binding.btnModeFloorPlan.setChecked(true);
        updateViewModeDisplay(true);

        binding.toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isFloorPlanMode = (checkedId == R.id.btnModeFloorPlan);
                updateViewModeDisplay(isFloorPlanMode);
            }
        });
    }

    private void updateViewModeDisplay(boolean floorPlanMode) {
        if (floorPlanMode) {
            binding.rvFloorPlan.setVisibility(View.VISIBLE);
            binding.rvRooms.setVisibility(View.GONE);
            binding.layoutLegendBar.setVisibility(View.VISIBLE);
        } else {
            binding.rvFloorPlan.setVisibility(View.GONE);
            binding.rvRooms.setVisibility(View.VISIBLE);
            binding.layoutLegendBar.setVisibility(View.GONE);
        }
    }

    private void setupFilters() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentStatusFilter = null;
            } else if (checkedId == R.id.chipAvailable) {
                currentStatusFilter = "AVAILABLE";
            } else if (checkedId == R.id.chipOccupied) {
                currentStatusFilter = "OCCUPIED";
            } else if (checkedId == R.id.chipMaintenance) {
                currentStatusFilter = "MAINTENANCE";
            }
            viewModel.loadRooms(currentStatusFilter, null);
        });

        if (getArguments() != null && getArguments().containsKey("status_filter")) {
            String filter = getArguments().getString("status_filter");
            if ("AVAILABLE".equalsIgnoreCase(filter)) {
                binding.chipAvailable.setChecked(true);
            } else if ("OCCUPIED".equalsIgnoreCase(filter)) {
                binding.chipOccupied.setChecked(true);
            } else if ("MAINTENANCE".equalsIgnoreCase(filter)) {
                binding.chipMaintenance.setChecked(true);
            }
        }
    }

    private void observeRooms() {
        viewModel.getRooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                List<Room> rooms = resource.getData();
                if (rooms != null && !rooms.isEmpty()) {
                    List<FloorGroup> floorGroups = groupRoomsByFloor(rooms);
                    floorPlanAdapter.setFloorGroups(floorGroups);
                    adapter.setRooms(rooms);
                    updateLegendCounts(rooms);

                    binding.tvEmpty.setVisibility(View.GONE);
                    updateViewModeDisplay(isFloorPlanMode);
                } else {
                    floorPlanAdapter.setFloorGroups(null);
                    adapter.setRooms(null);
                    updateLegendCounts(null);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.rvFloorPlan.setVisibility(View.GONE);
                    binding.rvRooms.setVisibility(View.GONE);
                }
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<FloorGroup> groupRoomsByFloor(List<Room> roomList) {
        if (roomList == null || roomList.isEmpty()) {
            return new ArrayList<>();
        }
        // Phân nhóm theo tầng và sắp xếp tầng tăng dần (Tầng 1 -> Tầng N)
        Map<Integer, List<Room>> floorMap = new TreeMap<>();
        for (Room r : roomList) {
            int floor = r.getFloor() != null ? r.getFloor() : 1;
            if (!floorMap.containsKey(floor)) {
                floorMap.put(floor, new ArrayList<>());
            }
            floorMap.get(floor).add(r);
        }

        List<FloorGroup> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Room>> entry : floorMap.entrySet()) {
            result.add(new FloorGroup(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void updateLegendCounts(List<Room> rooms) {
        int occupied = 0;
        int available = 0;
        int maintenance = 0;

        if (rooms != null) {
            for (Room r : rooms) {
                if (r.isOccupied()) occupied++;
                else if (r.isAvailable()) available++;
                else if (r.isMaintenance()) maintenance++;
            }
        }

        binding.tvLegendOccupied.setText(String.format(Locale.getDefault(), "Đang thuê (%d)", occupied));
        binding.tvLegendAvailable.setText(String.format(Locale.getDefault(), "Còn trống (%d)", available));
        binding.tvLegendMaintenance.setText(String.format(Locale.getDefault(), "Bảo trì (%d)", maintenance));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
