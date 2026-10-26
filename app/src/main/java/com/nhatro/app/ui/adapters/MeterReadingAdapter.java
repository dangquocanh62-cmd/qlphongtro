package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.databinding.ItemMeterReadingBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class MeterReadingAdapter extends RecyclerView.Adapter<MeterReadingAdapter.MeterReadingViewHolder> {
    private List<MeterReading> readings = new ArrayList<>();
    private OnReadingClickListener listener;

    public interface OnReadingClickListener {
        void onReadingClick(MeterReading reading);
    }

    public void setOnReadingClickListener(OnReadingClickListener listener) {
        this.listener = listener;
    }

    public void setReadings(List<MeterReading> readings) {
        this.readings = readings != null ? readings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeterReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMeterReadingBinding binding = ItemMeterReadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MeterReadingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MeterReadingViewHolder holder, int position) {
        holder.bind(readings.get(position));
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    class MeterReadingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMeterReadingBinding binding;

        public MeterReadingViewHolder(ItemMeterReadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReadingClick(readings.get(pos));
                }
            });
        }

        public void bind(MeterReading reading) {
            binding.tvMeterRoomAndService.setText(reading.getRoomCode() + " • " + reading.getServiceName());
            binding.tvPeriod.setText(String.format("Tháng %d/%d", reading.getMonth(), reading.getYear()));
            binding.tvOldNewValue.setText(String.format("Cũ: %.1f -> Mới: %.1f", reading.getOldValue(), reading.getNewValue()));
            binding.tvUsage.setText(String.format("Dùng: %.1f %s", reading.getUsageAmount(), reading.getServiceUnit()));
            binding.tvTotalCost.setText("Thành tiền: " + CurrencyUtils.formatVND(reading.getTotalCost()));
        }
    }
}
