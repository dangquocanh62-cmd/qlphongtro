package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.R;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.databinding.ItemServiceBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<ServiceItem> services = new ArrayList<>();
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(ServiceItem service);
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setServices(List<ServiceItem> services) {
        this.services = services != null ? services : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ServiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        holder.bind(services.get(position));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceBinding binding;

        public ServiceViewHolder(ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onServiceClick(services.get(pos));
                }
            });
        }

        public void bind(ServiceItem service) {
            binding.tvServiceName.setText(service.getName());
            binding.tvServicePrice.setText(CurrencyUtils.formatVND(service.getUnitPrice()) + " / " + service.getUnit());

            if (service.getIsMetered()) {
                binding.tvMeteredBadge.setText("Theo công tơ");
                binding.tvMeteredBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
            } else {
                binding.tvMeteredBadge.setText("Cố định");
                binding.tvMeteredBadge.setBackgroundResource(R.drawable.bg_badge_available);
            }
        }
    }
}
