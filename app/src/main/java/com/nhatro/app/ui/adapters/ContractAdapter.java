package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.databinding.ItemContractBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {
    private List<Contract> contracts = new ArrayList<>();
    private OnContractClickListener listener;

    public interface OnContractClickListener {
        void onContractClick(Contract contract);
    }

    public interface OnContractCallListener {
        void onContractCall(Contract contract);
    }

    private OnContractCallListener callListener;

    public void setOnContractClickListener(OnContractClickListener listener) {
        this.listener = listener;
    }

    public void setOnContractCallListener(OnContractCallListener callListener) {
        this.callListener = callListener;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts != null ? contracts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContractBinding binding = ItemContractBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContractViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        holder.bind(contracts.get(position));
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    class ContractViewHolder extends RecyclerView.ViewHolder {
        private final ItemContractBinding binding;

        public ContractViewHolder(ItemContractBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onContractClick(contracts.get(pos));
                }
            });

            binding.btnCallContractTenant.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Contract c = contracts.get(pos);
                    if (callListener != null) {
                        callListener.onContractCall(c);
                    } else if (c.getTenantPhone() != null && !c.getTenantPhone().trim().isEmpty()) {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        intent.setData(android.net.Uri.parse("tel:" + c.getTenantPhone().trim()));
                        itemView.getContext().startActivity(intent);
                    }
                }
            });
        }

        public void bind(Contract contract) {
            binding.tvContractRoom.setText("Phòng " + contract.getRoomCode());
            binding.tvContractTenant.setText("Đại diện: " + contract.getTenantName() + " (" + contract.getTenantPhone() + ")");
            
            String dates = "Thời hạn: " + DateUtils.formatDateVN(contract.getStartDate()) + " -> " +
                    (contract.getEndDate() != null ? DateUtils.formatDateVN(contract.getEndDate()) : "Không thời hạn");
            binding.tvContractDates.setText(dates);

            binding.tvRentPrice.setText("Giá thuê: " + CurrencyUtils.formatVND(contract.getRentPrice()));
            binding.tvDeposit.setText("Cọc: " + CurrencyUtils.formatVND(contract.getDeposit()));

            if (contract.isActive()) {
                binding.tvContractStatus.setText("Hiệu lực");
                binding.tvContractStatus.setBackgroundResource(R.drawable.bg_badge_available);
                binding.tvContractStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_available_text));

                if (contract.isExpired()) {
                    int daysPast = Math.abs(contract.calculateDaysRemaining());
                    binding.tvContractExpiryBadge.setVisibility(View.VISIBLE);
                    binding.tvContractExpiryBadge.setText("Quá hạn " + daysPast + " ngày");
                    binding.tvContractExpiryBadge.setBackgroundResource(R.drawable.bg_badge_unpaid);
                    binding.tvContractExpiryBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_unpaid_text));
                } else if (contract.isExpiringSoon()) {
                    int daysLeft = contract.calculateDaysRemaining();
                    binding.tvContractExpiryBadge.setVisibility(View.VISIBLE);
                    binding.tvContractExpiryBadge.setText(daysLeft == 0 ? "Hết hạn hôm nay" : ("Còn " + daysLeft + " ngày"));
                    binding.tvContractExpiryBadge.setBackgroundResource(R.drawable.bg_badge_partial);
                    binding.tvContractExpiryBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_partial_text));
                } else {
                    binding.tvContractExpiryBadge.setVisibility(View.GONE);
                }
            } else {
                binding.tvContractStatus.setText("Đã kết thúc");
                binding.tvContractStatus.setBackgroundResource(R.drawable.bg_badge_maintenance);
                binding.tvContractStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_maintenance_text));
                binding.tvContractExpiryBadge.setVisibility(View.GONE);
            }

            if (contract.getRoommateCount() > 0) {
                binding.tvRoommateInfo.setVisibility(View.VISIBLE);
                binding.tvRoommateInfo.setText("+ " + contract.getRoommateCount() + " người ở cùng");
            } else {
                binding.tvRoommateInfo.setVisibility(View.GONE);
            }

            if (contract.getTenantPhone() != null && !contract.getTenantPhone().trim().isEmpty()) {
                binding.btnCallContractTenant.setVisibility(View.VISIBLE);
            } else {
                binding.btnCallContractTenant.setVisibility(View.GONE);
            }
        }
    }
}
