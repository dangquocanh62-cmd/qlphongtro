package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.data.model.RoomMeterGroup;
import com.nhatro.app.databinding.ItemMeterReadingBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeterReadingAdapter extends RecyclerView.Adapter<MeterReadingAdapter.MeterReadingViewHolder> {
    private List<RoomMeterGroup> groups = new ArrayList<>();
    private OnReadingClickListener readingClickListener;
    private OnGroupClickListener groupClickListener;
    private OnMissingReadingClickListener missingReadingClickListener;

    public interface OnReadingClickListener {
        void onReadingClick(MeterReading reading);
    }

    public interface OnGroupClickListener {
        void onGroupClick(RoomMeterGroup group);
    }

    public interface OnMissingReadingClickListener {
        void onMissingReadingClick(RoomMeterGroup group, boolean isElectric);
    }

    public void setOnReadingClickListener(OnReadingClickListener listener) {
        this.readingClickListener = listener;
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.groupClickListener = listener;
    }

    public void setOnMissingReadingClickListener(OnMissingReadingClickListener listener) {
        this.missingReadingClickListener = listener;
    }

    public void setReadings(List<MeterReading> rawReadings) {
        this.groups = groupReadings(rawReadings);
        notifyDataSetChanged();
    }

    public void setGroups(List<RoomMeterGroup> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    private List<RoomMeterGroup> groupReadings(List<MeterReading> readings) {
        if (readings == null || readings.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, RoomMeterGroup> groupMap = new LinkedHashMap<>();

        for (MeterReading reading : readings) {
            if (reading == null) continue;
            Long roomId = reading.getRoomId();
            int month = reading.getMonth();
            int year = reading.getYear();
            String key = (roomId != null ? roomId : 0L) + "_" + month + "_" + year;

            RoomMeterGroup group = groupMap.get(key);
            if (group == null) {
                group = new RoomMeterGroup(roomId, reading.getRoomCode(), month, year);
                groupMap.put(key, group);
            } else if ((group.getRoomCode() == null || group.getRoomCode().isEmpty()) && reading.getRoomCode() != null) {
                group.setRoomCode(reading.getRoomCode());
            }

            if (RoomMeterGroup.isElectric(reading)) {
                group.setElectricReading(reading);
            } else if (RoomMeterGroup.isWater(reading)) {
                group.setWaterReading(reading);
            } else {
                // Nếu dịch vụ khác, gán vào ô còn trống
                if (group.getElectricReading() == null) {
                    group.setElectricReading(reading);
                } else {
                    group.setWaterReading(reading);
                }
            }
        }

        List<RoomMeterGroup> result = new ArrayList<>(groupMap.values());
        // Sắp xếp: Kỳ mới nhất trước, sau đó theo số phòng
        Collections.sort(result, (a, b) -> {
            if (a.getYear() != b.getYear()) {
                return Integer.compare(b.getYear(), a.getYear());
            }
            if (a.getMonth() != b.getMonth()) {
                return Integer.compare(b.getMonth(), a.getMonth());
            }
            String codeA = a.getRoomCode() != null ? a.getRoomCode() : "";
            String codeB = b.getRoomCode() != null ? b.getRoomCode() : "";
            return codeA.compareToIgnoreCase(codeB);
        });

        return result;
    }

    @NonNull
    @Override
    public MeterReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMeterReadingBinding binding = ItemMeterReadingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MeterReadingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MeterReadingViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class MeterReadingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMeterReadingBinding binding;

        public MeterReadingViewHolder(ItemMeterReadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < groups.size()) {
                    RoomMeterGroup group = groups.get(pos);
                    if (groupClickListener != null) {
                        groupClickListener.onGroupClick(group);
                    } else if (readingClickListener != null) {
                        if (group.getElectricReading() != null) {
                            readingClickListener.onReadingClick(group.getElectricReading());
                        } else if (group.getWaterReading() != null) {
                            readingClickListener.onReadingClick(group.getWaterReading());
                        }
                    }
                }
            });

            binding.llElectricBox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < groups.size()) {
                    RoomMeterGroup group = groups.get(pos);
                    if (group.getElectricReading() != null && readingClickListener != null) {
                        readingClickListener.onReadingClick(group.getElectricReading());
                    } else if (group.getElectricReading() == null && missingReadingClickListener != null) {
                        missingReadingClickListener.onMissingReadingClick(group, true);
                    }
                }
            });

            binding.llWaterBox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < groups.size()) {
                    RoomMeterGroup group = groups.get(pos);
                    if (group.getWaterReading() != null && readingClickListener != null) {
                        readingClickListener.onReadingClick(group.getWaterReading());
                    } else if (group.getWaterReading() == null && missingReadingClickListener != null) {
                        missingReadingClickListener.onMissingReadingClick(group, false);
                    }
                }
            });
        }

        public void bind(RoomMeterGroup group) {
            String roomText = group.getRoomCode();
            if (roomText != null && !roomText.toLowerCase().startsWith("phòng")) {
                roomText = "Phòng " + roomText;
            }
            binding.tvMeterRoomAndService.setText(roomText);
            binding.tvPeriod.setText(String.format("Tháng %d/%d", group.getMonth(), group.getYear()));

            // Bind Cột Điện
            MeterReading electric = group.getElectricReading();
            if (electric != null) {
                binding.llElectricContent.setVisibility(View.VISIBLE);
                binding.llElectricEmpty.setVisibility(View.GONE);
                binding.tvElectricOldNew.setText(String.format("Cũ: %s → Mới: %s", formatMeterValue(electric.getOldValue()), formatMeterValue(electric.getNewValue())));
                binding.tvElectricUsage.setText(String.format("Dùng: %s %s", formatMeterValue(electric.getUsageAmount()), electric.getServiceUnit()));
                binding.tvElectricCost.setText(CurrencyUtils.formatVND(electric.getTotalCost()));
            } else {
                binding.llElectricContent.setVisibility(View.GONE);
                binding.llElectricEmpty.setVisibility(View.VISIBLE);
            }

            // Bind Cột Nước
            MeterReading water = group.getWaterReading();
            if (water != null) {
                binding.llWaterContent.setVisibility(View.VISIBLE);
                binding.llWaterEmpty.setVisibility(View.GONE);
                binding.tvWaterOldNew.setText(String.format("Cũ: %s → Mới: %s", formatMeterValue(water.getOldValue()), formatMeterValue(water.getNewValue())));
                binding.tvWaterUsage.setText(String.format("Dùng: %s %s", formatMeterValue(water.getUsageAmount()), water.getServiceUnit()));
                binding.tvWaterCost.setText(CurrencyUtils.formatVND(water.getTotalCost()));
            } else {
                binding.llWaterContent.setVisibility(View.GONE);
                binding.llWaterEmpty.setVisibility(View.VISIBLE);
            }

            // Tổng tiền
            binding.tvTotalCost.setText(CurrencyUtils.formatVND(group.getTotalCost()));
        }

        private String formatMeterValue(Double value) {
            if (value == null) return "0";
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return String.valueOf(value.longValue());
            } else {
                return String.format(java.util.Locale.US, "%.1f", value);
            }
        }
    }
}
