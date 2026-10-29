package com.nhatro.app.ui.meter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhatro.app.databinding.ActivityMeterReadingListBinding;
import com.nhatro.app.ui.adapters.MeterReadingAdapter;
import com.nhatro.app.viewmodel.MeterReadingViewModel;

public class MeterReadingListActivity extends AppCompatActivity {
    private ActivityMeterReadingListBinding binding;
    private MeterReadingViewModel viewModel;
    private MeterReadingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeterReadingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MeterReadingViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        observeReadings();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabAddReading.setOnClickListener(v ->
                startActivity(new Intent(this, AddMeterReadingActivity.class))
        );
    }

    private void setupRecyclerView() {
        adapter = new MeterReadingAdapter();
        adapter.setOnMissingReadingClickListener((group, isElectric) -> {
            Intent intent = new Intent(this, AddMeterReadingActivity.class);
            if (group.getRoomId() != null) {
                intent.putExtra("extra_room_id", group.getRoomId());
            }
            intent.putExtra("extra_is_electric", isElectric);
            intent.putExtra("extra_month", group.getMonth());
            intent.putExtra("extra_year", group.getYear());
            startActivity(intent);
        });
        binding.rvMeterReadings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMeterReadings.setAdapter(adapter);
    }

    private void observeReadings() {
        viewModel.getMeterReadings().observe(this, resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(resource.isLoading());

            if (resource.isSuccess()) {
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    adapter.setReadings(resource.getData());
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    adapter.setReadings(null);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            } else if (resource.isError()) {
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
