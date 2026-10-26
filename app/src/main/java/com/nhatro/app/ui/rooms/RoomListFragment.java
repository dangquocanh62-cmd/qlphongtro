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
import com.nhatro.app.databinding.FragmentRoomListBinding;
import com.nhatro.app.ui.adapters.RoomAdapter;
import com.nhatro.app.viewmodel.RoomViewModel;

public class RoomListFragment extends Fragment {
    private FragmentRoomListBinding binding;
    private RoomViewModel viewModel;
    private RoomAdapter adapter;
    private String currentStatusFilter = null;

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

        setupRecyclerView();
        setupFilters();
        observeRooms();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddRoom.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditRoomActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter();
        adapter.setOnRoomClickListener(room -> {
            Intent intent = new Intent(requireContext(), RoomDetailActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
        });

        binding.rvRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRooms.setAdapter(adapter);
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
    }

    private void observeRooms() {
        viewModel.getRooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setRooms(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setRooms(null);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
