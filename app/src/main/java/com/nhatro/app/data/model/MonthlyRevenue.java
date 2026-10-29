package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MonthlyRevenue implements Serializable {
    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("label")
    private String label;

    @SerializedName("revenue")
    private double revenue;

    @SerializedName("invoiced")
    private double invoiced;

    public MonthlyRevenue() {}

    public MonthlyRevenue(int month, int year, String label, double revenue, double invoiced) {
        this.month = month;
        this.year = year;
        this.label = label;
        this.revenue = revenue;
        this.invoiced = invoiced;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLabel() {
        return label != null ? label : ("T" + month);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getInvoiced() {
        return invoiced;
    }

    public void setInvoiced(double invoiced) {
        this.invoiced = invoiced;
    }
}
