package com.example.ecologic_route_ws.Models;

public class Distance {
    private int id;
    private double exactDistance; // Represents the exact distance
    private boolean longDistance; // True or false for long distance

    public Distance(int id ,double exactDistance, boolean longDistance) {
        this.id=id;
        this.exactDistance = exactDistance;
        this.longDistance = longDistance;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public double getExactDistance() {
        return exactDistance;
    }

    public void setExactDistance(double exactDistance) {
        this.exactDistance = exactDistance;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public void setLongDistance(boolean longDistance) {
        this.longDistance = longDistance;
    }
}
