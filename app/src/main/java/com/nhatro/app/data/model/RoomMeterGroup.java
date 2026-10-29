package com.nhatro.app.data.model;

import java.io.Serializable;

public class RoomMeterGroup implements Serializable {
    private Long roomId;
    private String roomCode;
    private int month;
    private int year;
    private MeterReading electricReading;
    private MeterReading waterReading;

    public RoomMeterGroup() {}

    public RoomMeterGroup(Long roomId, String roomCode, int month, int year) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.month = month;
        this.year = year;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomCode() {
        return roomCode != null ? roomCode : "";
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
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

    public MeterReading getElectricReading() {
        return electricReading;
    }

    public void setElectricReading(MeterReading electricReading) {
        this.electricReading = electricReading;
    }

    public MeterReading getWaterReading() {
        return waterReading;
    }

    public void setWaterReading(MeterReading waterReading) {
        this.waterReading = waterReading;
    }

    public boolean hasElectric() {
        return electricReading != null;
    }

    public boolean hasWater() {
        return waterReading != null;
    }

    public long getTotalCost() {
        long total = 0L;
        if (electricReading != null) {
            total += electricReading.getTotalCost();
        }
        if (waterReading != null) {
            total += waterReading.getTotalCost();
        }
        return total;
    }

    public static boolean isElectric(MeterReading reading) {
        if (reading == null) return false;
        String name = reading.getServiceName() != null ? reading.getServiceName().toLowerCase() : "";
        String unit = reading.getServiceUnit() != null ? reading.getServiceUnit().toLowerCase() : "";
        return name.contains("điện") || name.contains("dien") || unit.contains("kwh");
    }

    public static boolean isWater(MeterReading reading) {
        if (reading == null) return false;
        String name = reading.getServiceName() != null ? reading.getServiceName().toLowerCase() : "";
        String unit = reading.getServiceUnit() != null ? reading.getServiceUnit().toLowerCase() : "";
        return name.contains("nước") || name.contains("nuoc") || unit.contains("m3") || unit.contains("m³");
    }
}
