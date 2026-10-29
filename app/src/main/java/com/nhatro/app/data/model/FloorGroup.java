package com.nhatro.app.data.model;

import java.util.ArrayList;
import java.util.List;

public class FloorGroup {
    private final int floorNumber;
    private final List<Room> rooms;

    public FloorGroup(int floorNumber, List<Room> rooms) {
        this.floorNumber = floorNumber;
        this.rooms = rooms != null ? rooms : new ArrayList<>();
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public String getFloorDisplayName() {
        if (floorNumber == 0) return "Tầng Trệt (G)";
        return "Tầng " + floorNumber;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getTotalRooms() {
        return rooms.size();
    }

    public int getOccupiedCount() {
        int count = 0;
        for (Room r : rooms) {
            if (r.isOccupied()) count++;
        }
        return count;
    }

    public int getAvailableCount() {
        int count = 0;
        for (Room r : rooms) {
            if (r.isAvailable()) count++;
        }
        return count;
    }

    public int getMaintenanceCount() {
        int count = 0;
        for (Room r : rooms) {
            if (r.isMaintenance()) count++;
        }
        return count;
    }
}
