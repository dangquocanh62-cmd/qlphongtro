package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Room implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("floor")
    private Integer floor;

    @SerializedName("area")
    private Double area;

    @SerializedName("price")
    private Long price;

    @SerializedName("status")
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @SerializedName("note")
    private String note;

    // Enriched fields from API join
    @SerializedName("contract_id")
    private Long contractId;

    @SerializedName("contract_start")
    private String contractStart;

    @SerializedName("contract_end")
    private String contractEnd;

    @SerializedName("contract_rent_price")
    private Long contractRentPrice;

    @SerializedName("tenant_id")
    private Long tenantId;

    @SerializedName("tenant_name")
    private String tenantName;

    @SerializedName("tenant_phone")
    private String tenantPhone;

    public Room() {}

    public Room(String roomCode, Integer floor, Double area, Long price, String status, String note) {
        this.roomCode = roomCode;
        this.floor = floor;
        this.area = area;
        this.price = price;
        this.status = status;
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public Integer getFloor() { return floor != null ? floor : 1; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getArea() { return area != null ? area : 0.0; }
    public void setArea(Double area) { this.area = area; }

    public Long getPrice() { return price != null ? price : 0L; }
    public void setPrice(Long price) { this.price = price; }

    public String getStatus() { return status != null ? status : "AVAILABLE"; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note != null ? note : ""; }
    public void setNote(String note) { this.note = note; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getContractStart() { return contractStart; }
    public void setContractStart(String contractStart) { this.contractStart = contractStart; }

    public String getContractEnd() { return contractEnd; }
    public void setContractEnd(String contractEnd) { this.contractEnd = contractEnd; }

    public Long getContractRentPrice() { return contractRentPrice; }
    public void setContractRentPrice(Long contractRentPrice) { this.contractRentPrice = contractRentPrice; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "OCCUPIED".equalsIgnoreCase(status);
    }

    public boolean isMaintenance() {
        return "MAINTENANCE".equalsIgnoreCase(status);
    }
}
