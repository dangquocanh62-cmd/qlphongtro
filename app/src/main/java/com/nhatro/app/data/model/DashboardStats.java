package com.nhatro.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DashboardStats implements Serializable {
    @SerializedName("total_rooms")
    private int totalRooms;

    @SerializedName("available_rooms")
    private int availableRooms;

    @SerializedName("occupied_rooms")
    private int occupiedRooms;

    @SerializedName("maintenance_rooms")
    private int maintenanceRooms;

    @SerializedName("total_tenants")
    private int totalTenants;

    @SerializedName("active_contracts")
    private int activeContracts;

    @SerializedName("total_invoices")
    private int totalInvoices;

    @SerializedName("paid_invoices")
    private int paidInvoices;

    @SerializedName("unpaid_invoices")
    private int unpaidInvoices;

    @SerializedName("partial_invoices")
    private int partialInvoices;

    @SerializedName("total_revenue")
    private long totalRevenue;

    @SerializedName("unpaid_debt")
    private long unpaidDebt;

    @SerializedName("expiring_contracts_count")
    private int expiringContractsCount;

    @SerializedName("overdue_invoices_count")
    private int overdueInvoicesCount;

    @SerializedName("missing_meter_rooms_count")
    private int missingMeterRoomsCount;

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    public DashboardStats() {}

    public int getMonth() { 
        if (month <= 0 || month > 12) {
            return java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1;
        }
        return month; 
    }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { 
        if (year <= 1900) {
            return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        }
        return year; 
    }
    public void setYear(int year) { this.year = year; }

    public int getOverdueInvoicesCount() { return overdueInvoicesCount; }
    public void setOverdueInvoicesCount(int overdueInvoicesCount) { this.overdueInvoicesCount = overdueInvoicesCount; }

    public int getMissingMeterRoomsCount() { return missingMeterRoomsCount; }
    public void setMissingMeterRoomsCount(int missingMeterRoomsCount) { this.missingMeterRoomsCount = missingMeterRoomsCount; }

    public int getExpiringContractsCount() { return expiringContractsCount; }
    public void setExpiringContractsCount(int expiringContractsCount) { this.expiringContractsCount = expiringContractsCount; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

    public int getMaintenanceRooms() { return maintenanceRooms; }
    public void setMaintenanceRooms(int maintenanceRooms) { this.maintenanceRooms = maintenanceRooms; }

    public int getTotalTenants() { return totalTenants; }
    public void setTotalTenants(int totalTenants) { this.totalTenants = totalTenants; }

    public int getActiveContracts() { return activeContracts; }
    public void setActiveContracts(int activeContracts) { this.activeContracts = activeContracts; }

    public int getTotalInvoices() { return totalInvoices; }
    public void setTotalInvoices(int totalInvoices) { this.totalInvoices = totalInvoices; }

    public int getPaidInvoices() { return paidInvoices; }
    public void setPaidInvoices(int paidInvoices) { this.paidInvoices = paidInvoices; }

    public int getUnpaidInvoices() { return unpaidInvoices; }
    public void setUnpaidInvoices(int unpaidInvoices) { this.unpaidInvoices = unpaidInvoices; }

    public int getPartialInvoices() { return partialInvoices; }
    public void setPartialInvoices(int partialInvoices) { this.partialInvoices = partialInvoices; }

    public long getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(long totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getUnpaidDebt() { return unpaidDebt; }
    public void setUnpaidDebt(long unpaidDebt) { this.unpaidDebt = unpaidDebt; }
}
