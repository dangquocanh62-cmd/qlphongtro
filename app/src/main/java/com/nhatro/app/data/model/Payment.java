package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Payment implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("invoice_id")
    private Long invoiceId;

    @SerializedName("amount")
    private Long amount;

    @SerializedName("method")
    private String method; // CASH or TRANSFER

    @SerializedName("paid_at")
    private String paidAt;

    // Enriched fields
    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("total_amount")
    private Long totalAmount;

    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("tenant_name")
    private String tenantName;

    public Payment() {}

    public Payment(Long invoiceId, Long amount, String method, String paidAt) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getAmount() { return amount != null ? amount : 0L; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getMethod() { return method != null ? method : "CASH"; }
    public void setMethod(String method) { this.method = method; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public String getRoomCode() { return roomCode != null ? roomCode : ""; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getTenantName() { return tenantName != null ? tenantName : ""; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public boolean isCash() {
        return "CASH".equalsIgnoreCase(method);
    }
}
