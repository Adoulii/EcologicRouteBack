package com.example.ecologic_route_ws.Models;

// Base class for Energy
public  class Energy {
    private int id; // Unique ID for the energy resource
    private String type;
    private double consumptionRate;
    private boolean renewable;

    // Constructor
    public Energy(int id, double consumptionRate, boolean renewable, String type) {
        this.id = id;
        this.consumptionRate = consumptionRate;
        this.renewable = renewable;
        this.type = type;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(double consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    // Electric class
public class Electric extends Energy {
    public Electric(int id, double consumptionRate, boolean renewable) {
        super(id, consumptionRate, renewable, "electric");
    }
}

// Fuel class
public class Fuel extends Energy {
    public Fuel(int id, double consumptionRate, boolean renewable) {
        super(id, consumptionRate, renewable, "fuel");
    }
}

// Hybrid class
public class Hybrid extends Energy {
    public Hybrid(int id, double consumptionRate, boolean renewable) {
        super(id, consumptionRate, renewable, "hybrid");
    }
}


}
