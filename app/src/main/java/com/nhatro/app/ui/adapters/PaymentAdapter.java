package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.Payment;
import com.nhatro.app.databinding.ItemPaymentBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private List<Payment> payments = new ArrayList<>();

    public void setPayments(List<Payment> payments) {
        this.payments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentBinding binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PaymentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(payments.get(position));
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPaymentBinding binding;

        public PaymentViewHolder(ItemPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Payment payment) {
            binding.tvPaymentAmount.setText("+ " + CurrencyUtils.formatVND(payment.getAmount()));
            binding.tvPaymentMethod.setText(payment.isCash() ? "Tiền mặt" : "Chuyển khoản");
            binding.tvPaymentTime.setText(DateUtils.formatDateVN(payment.getPaidAt()));
        }
    }
}
