package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Tenant implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("id_card")
    private String idCard;

    @SerializedName("birth_date")
    private String birthDate;

    @SerializedName("hometown")
    private String hometown;

    @SerializedName("created_at")
    private String createdAt;

    public Tenant() {}

    public Tenant(String fullName, String phone, String idCard, String birthDate, String hometown) {
        this.fullName = fullName;
        this.phone = phone;
        this.idCard = idCard;
        this.birthDate = birthDate;
        this.hometown = hometown;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName != null ? fullName : ""; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIdCard() { return idCard != null ? idCard : ""; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getHometown() { return hometown != null ? hometown : ""; }
    public void setHometown(String hometown) { this.hometown = hometown; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getFullName() + " (" + getPhone() + ")";
    }
}
