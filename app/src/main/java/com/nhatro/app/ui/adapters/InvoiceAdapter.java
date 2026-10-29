package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.R;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.databinding.ItemInvoiceBinding;
import com.nhatro.app.utils.CurrencyUtils;
import com.nhatro.app.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private List<Invoice> invoices = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    public interface OnInvoiceShareListener {
        void onInvoiceShare(Invoice invoice);
    }

    public interface OnInvoiceCallListener {
        void onInvoiceCall(Invoice invoice);
    }

    public interface OnInvoiceCollectListener {
        void onInvoiceCollect(Invoice invoice);
    }

    private OnInvoiceShareListener shareListener;
    private OnInvoiceCallListener callListener;
    private OnInvoiceCollectListener collectListener;

    public void setOnInvoiceClickListener(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    public void setOnInvoiceShareListener(OnInvoiceShareListener shareListener) {
        this.shareListener = shareListener;
    }

    public void setOnInvoiceCallListener(OnInvoiceCallListener callListener) {
        this.callListener = callListener;
    }

    public void setOnInvoiceCollectListener(OnInvoiceCollectListener collectListener) {
        this.collectListener = collectListener;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices != null ? invoices : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        holder.bind(invoices.get(position));
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onInvoiceClick(invoices.get(pos));
                }
            });

            binding.btnQuickShare.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Invoice inv = invoices.get(pos);
                    if (shareListener != null) {
                        shareListener.onInvoiceShare(inv);
                    } else {
                        com.nhatro.app.utils.InvoiceExportHelper.shareInvoice(itemView.getContext(), inv);
                    }
                }
            });

            binding.btnQuickCall.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Invoice inv = invoices.get(pos);
                    if (callListener != null) {
                        callListener.onInvoiceCall(inv);
                    } else if (inv.getTenantPhone() != null && !inv.getTenantPhone().isEmpty()) {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        intent.setData(android.net.Uri.parse("tel:" + inv.getTenantPhone()));
                        itemView.getContext().startActivity(intent);
                    }
                }
            });

            binding.btnQuickCollect.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && collectListener != null) {
                    collectListener.onInvoiceCollect(invoices.get(pos));
                }
            });
        }

        public void bind(Invoice invoice) {
            binding.tvInvoiceRoom.setText("Phòng " + invoice.getRoomCode());
            binding.tvInvoicePeriod.setText(String.format("• Tháng %d/%d", invoice.getMonth(), invoice.getYear()));
            binding.tvInvoiceTenant.setText("Khách: " + invoice.getTenantName() + " (" + invoice.getTenantPhone() + ")");
            binding.tvTotalAmount.setText("Tổng: " + CurrencyUtils.formatVND(invoice.getTotalAmount()));
            binding.tvRemainingAmount.setText("Còn nợ: " + CurrencyUtils.formatVND(invoice.getRemainingAmount()));

            if (invoice.getDueDate() != null) {
                binding.tvDueDate.setText("Hạn đóng: " + DateUtils.formatDateVN(invoice.getDueDate()));
            } else {
                binding.tvDueDate.setText("Hạn đóng: ---");
            }

            // Quick call visibility
            if (invoice.getTenantPhone() != null && !invoice.getTenantPhone().trim().isEmpty()) {
                binding.btnQuickCall.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.btnQuickCall.setVisibility(android.view.View.GONE);
            }

            // Quick collect button visibility
            if (invoice.isPaid()) {
                binding.btnQuickCollect.setVisibility(android.view.View.GONE);
            } else {
                binding.btnQuickCollect.setVisibility(android.view.View.VISIBLE);
            }

            if (invoice.isPaid()) {
                binding.tvInvoiceStatusBadge.setText("Đã thanh toán");
                binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_paid);
                binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_paid_text));
                binding.tvRemainingAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_paid_text));
            } else if (invoice.isPartial()) {
                binding.tvInvoiceStatusBadge.setText("Trả một phần");
                binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_partial);
                binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_partial_text));
                binding.tvRemainingAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_partial_text));
            } else {
                binding.tvInvoiceStatusBadge.setText("Chưa thanh toán");
                binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_unpaid);
                binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_unpaid_text));
                binding.tvRemainingAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_unpaid_text));
            }
        }
    }
}
