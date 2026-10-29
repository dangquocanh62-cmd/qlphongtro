package com.nhatro.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhatro.app.data.model.InvoiceItem;
import com.nhatro.app.databinding.ItemInvoiceItemBinding;
import com.nhatro.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ItemViewHolder> {
    private List<InvoiceItem> items = new ArrayList<>();
    private boolean canDelete = true;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onItemDelete(InvoiceItem item);
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
        notifyDataSetChanged();
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceItemBinding binding = ItemInvoiceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceItemBinding binding;

        public ItemViewHolder(ItemInvoiceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InvoiceItem item) {
            binding.tvItemDescription.setText(item.getDescription());
            binding.tvItemAmount.setText(CurrencyUtils.formatVND(item.getAmount()));
            binding.tvItemCalculation.setText(String.format("%.1f x %s", item.getQuantity(), CurrencyUtils.formatVND(item.getUnitPrice())));

            if (canDelete && item.getId() != null) {
                binding.btnDeleteItem.setVisibility(View.VISIBLE);
                binding.btnDeleteItem.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onItemDelete(item);
                    }
                });
            } else {
                binding.btnDeleteItem.setVisibility(View.GONE);
            }
        }
    }
}
