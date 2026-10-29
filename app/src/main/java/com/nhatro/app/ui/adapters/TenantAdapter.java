package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.databinding.ItemTenantBinding;
import com.nhatro.app.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {
    private List<Tenant> tenants = new ArrayList<>();
    private OnTenantClickListener listener;
    private OnTenantCallListener callListener;

    public interface OnTenantClickListener {
        void onTenantClick(Tenant tenant);
    }

    public interface OnTenantCallListener {
        void onTenantCall(Tenant tenant);
    }

    public void setOnTenantClickListener(OnTenantClickListener listener) {
        this.listener = listener;
    }

    public void setOnTenantCallListener(OnTenantCallListener callListener) {
        this.callListener = callListener;
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTenantBinding binding = ItemTenantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TenantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        holder.bind(tenants.get(position));
    }

    @Override
    public int getItemCount() {
        return tenants.size();
    }

    class TenantViewHolder extends RecyclerView.ViewHolder {
        private final ItemTenantBinding binding;

        public TenantViewHolder(ItemTenantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTenantClick(tenants.get(pos));
                }
            });

            binding.btnCallTenant.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Tenant t = tenants.get(pos);
                    if (callListener != null) {
                        callListener.onTenantCall(t);
                    } else if (t.getPhone() != null && !t.getPhone().trim().isEmpty()) {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        intent.setData(android.net.Uri.parse("tel:" + t.getPhone().trim()));
                        itemView.getContext().startActivity(intent);
                    }
                }
            });
        }

        public void bind(Tenant tenant) {
            binding.tvFullName.setText(tenant.getFullName());
            binding.tvPhone.setText(tenant.getPhone());
            binding.tvIdCard.setText("CCCD: " + (tenant.getIdCard() != null ? tenant.getIdCard() : "Chưa cập nhật"));

            if (tenant.getPhone() != null && !tenant.getPhone().trim().isEmpty()) {
                binding.btnCallTenant.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.btnCallTenant.setVisibility(android.view.View.GONE);
            }

            String info = "Quê quán: " + (tenant.getHometown() != null ? tenant.getHometown() : "---");
            if (tenant.getBirthDate() != null) {
                info += " • Sinh: " + DateUtils.formatDateVN(tenant.getBirthDate());
            }
            binding.tvHometown.setText(info);
        }
    }
}
