package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreateInvoiceRequest {
    @SerializedName("contract_id")
    private Long contractId;

    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("items")
    private List<InvoiceItem> items;

    public CreateInvoiceRequest() {}

    public CreateInvoiceRequest(Long contractId, Integer month, Integer year, String dueDate, List<InvoiceItem> items) {
        this.contractId = contractId;
        this.month = month;
        this.year = year;
        this.dueDate = dueDate;
        this.items = items;
    }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
