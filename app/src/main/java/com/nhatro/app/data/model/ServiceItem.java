package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ServiceItem implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("unit")
    private String unit;

    @SerializedName("unit_price")
    private Long unitPrice;

    @SerializedName("is_metered")
    private Boolean isMetered;

    public ServiceItem() {}

    public ServiceItem(String name, String unit, Long unitPrice, Boolean isMetered) {
        this.name = name;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.isMetered = isMetered;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit != null ? unit : ""; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getUnitPrice() { return unitPrice != null ? unitPrice : 0L; }
    public void setUnitPrice(Long unitPrice) { this.unitPrice = unitPrice; }

    public Boolean getIsMetered() { return isMetered != null ? isMetered : false; }
    public void setIsMetered(Boolean isMetered) { this.isMetered = isMetered; }

    @Override
    public String toString() {
        return getName() + " (" + getUnit() + ")";
    }
}
