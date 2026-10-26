package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class InvoiceItem implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("invoice_id")
    private Long invoiceId;

    @SerializedName("description")
    private String description;

    @SerializedName("quantity")
    private Double quantity;

    @SerializedName("unit_price")
    private Long unitPrice;

    @SerializedName("amount")
    private Long amount;

    public InvoiceItem() {}

    public InvoiceItem(String description, Double quantity, Long unitPrice, Long amount) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantity() { return quantity != null ? quantity : 1.0; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Long getUnitPrice() { return unitPrice != null ? unitPrice : 0L; }
    public void setUnitPrice(Long unitPrice) { this.unitPrice = unitPrice; }

    public Long getAmount() {
        if (amount != null) return amount;
        return (long) (getQuantity() * getUnitPrice());
    }
    public void setAmount(Long amount) { this.amount = amount; }
}
