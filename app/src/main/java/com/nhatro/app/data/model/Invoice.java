package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Invoice implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("contract_id")
    private Long contractId;

    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("total_amount")
    private Long totalAmount;

    @SerializedName("status")
    private String status; // UNPAID, PARTIAL, PAID

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("created_at")
    private String createdAt;

    // Enriched fields
    @SerializedName("room_id")
    private Long roomId;

    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("tenant_name")
    private String tenantName;

    @SerializedName("tenant_phone")
    private String tenantPhone;

    @SerializedName("tenant_id_card")
    private String tenantIdCard;

    @SerializedName("paid_amount")
    private Long paidAmount;

    @SerializedName("remaining_amount")
    private Long remainingAmount;

    @SerializedName("items")
    private List<InvoiceItem> items;

    @SerializedName("payments")
    private List<Payment> payments;

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public Integer getMonth() { return month != null ? month : 1; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year != null ? year : 2024; }
    public void setYear(Integer year) { this.year = year; }

    public Long getTotalAmount() { return totalAmount != null ? totalAmount : 0L; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status != null ? status : "UNPAID"; }
    public void setStatus(String status) { this.status = status; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomCode() { return roomCode != null ? roomCode : ""; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getTenantName() { return tenantName != null ? tenantName : ""; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone != null ? tenantPhone : ""; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public String getTenantIdCard() { return tenantIdCard != null ? tenantIdCard : ""; }
    public void setTenantIdCard(String tenantIdCard) { this.tenantIdCard = tenantIdCard; }

    public Long getPaidAmount() { return paidAmount != null ? paidAmount : 0L; }
    public void setPaidAmount(Long paidAmount) { this.paidAmount = paidAmount; }

    public Long getRemainingAmount() {
        if (remainingAmount != null) return remainingAmount;
        return Math.max(0L, getTotalAmount() - getPaidAmount());
    }
    public void setRemainingAmount(Long remainingAmount) { this.remainingAmount = remainingAmount; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(status);
    }

    public boolean isPartial() {
        return "PARTIAL".equalsIgnoreCase(status);
    }

    public boolean isUnpaid() {
        return "UNPAID".equalsIgnoreCase(status);
    }
}
