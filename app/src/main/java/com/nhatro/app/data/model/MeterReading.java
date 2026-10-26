package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MeterReading implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("room_id")
    private Long roomId;

    @SerializedName("service_id")
    private Long serviceId;

    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("old_value")
    private Double oldValue;

    @SerializedName("new_value")
    private Double newValue;

    // Enriched fields
    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("service_unit")
    private String serviceUnit;

    @SerializedName("service_unit_price")
    private Long serviceUnitPrice;

    @SerializedName("usage_amount")
    private Double usageAmount;

    @SerializedName("total_cost")
    private Long totalCost;

    public MeterReading() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Integer getMonth() { return month != null ? month : 1; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year != null ? year : 2024; }
    public void setYear(Integer year) { this.year = year; }

    public Double getOldValue() { return oldValue != null ? oldValue : 0.0; }
    public void setOldValue(Double oldValue) { this.oldValue = oldValue; }

    public Double getNewValue() { return newValue != null ? newValue : 0.0; }
    public void setNewValue(Double newValue) { this.newValue = newValue; }

    public String getRoomCode() { return roomCode != null ? roomCode : ""; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getServiceName() { return serviceName != null ? serviceName : ""; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceUnit() { return serviceUnit != null ? serviceUnit : ""; }
    public void setServiceUnit(String serviceUnit) { this.serviceUnit = serviceUnit; }

    public Long getServiceUnitPrice() { return serviceUnitPrice != null ? serviceUnitPrice : 0L; }
    public void setServiceUnitPrice(Long serviceUnitPrice) { this.serviceUnitPrice = serviceUnitPrice; }

    public Double getUsageAmount() {
        if (usageAmount != null) return usageAmount;
        return getNewValue() - getOldValue();
    }
    public void setUsageAmount(Double usageAmount) { this.usageAmount = usageAmount; }

    public Long getTotalCost() {
        if (totalCost != null) return totalCost;
        return (long) (getUsageAmount() * getServiceUnitPrice());
    }
    public void setTotalCost(Long totalCost) { this.totalCost = totalCost; }
}
