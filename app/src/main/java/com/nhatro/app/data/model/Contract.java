package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Contract implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("room_id")
    private Long roomId;

    @SerializedName("tenant_id")
    private Long tenantId;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("deposit")
    private Long deposit;

    @SerializedName("rent_price")
    private Long rentPrice;

    @SerializedName("status")
    private String status; // ACTIVE, ENDED, CANCELLED

    // Enriched fields
    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("floor")
    private Integer floor;

    @SerializedName("area")
    private Double area;

    @SerializedName("tenant_name")
    private String tenantName;

    @SerializedName("tenant_phone")
    private String tenantPhone;

    @SerializedName("tenant_id_card")
    private String tenantIdCard;

    @SerializedName("tenant_hometown")
    private String tenantHometown;

    @SerializedName("roommate_count")
    private Integer roommateCount;

    @SerializedName("roommates")
    private List<Tenant> roommates;

    public Contract() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Long getDeposit() { return deposit != null ? deposit : 0L; }
    public void setDeposit(Long deposit) { this.deposit = deposit; }

    public Long getRentPrice() { return rentPrice != null ? rentPrice : 0L; }
    public void setRentPrice(Long rentPrice) { this.rentPrice = rentPrice; }

    public String getStatus() { return status != null ? status : "ACTIVE"; }
    public void setStatus(String status) { this.status = status; }

    public String getRoomCode() { return roomCode != null ? roomCode : ""; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public Integer getFloor() { return floor != null ? floor : 1; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getArea() { return area != null ? area : 0.0; }
    public void setArea(Double area) { this.area = area; }

    public String getTenantName() { return tenantName != null ? tenantName : ""; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone != null ? tenantPhone : ""; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public String getTenantIdCard() { return tenantIdCard != null ? tenantIdCard : ""; }
    public void setTenantIdCard(String tenantIdCard) { this.tenantIdCard = tenantIdCard; }

    public String getTenantHometown() { return tenantHometown != null ? tenantHometown : ""; }
    public void setTenantHometown(String tenantHometown) { this.tenantHometown = tenantHometown; }

    public Integer getRoommateCount() { return roommateCount != null ? roommateCount : 0; }
    public void setRoommateCount(Integer roommateCount) { this.roommateCount = roommateCount; }

    public List<Tenant> getRoommates() { return roommates; }
    public void setRoommates(List<Tenant> roommates) { this.roommates = roommates; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
