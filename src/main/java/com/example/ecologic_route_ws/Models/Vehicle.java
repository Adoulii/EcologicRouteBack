package com.example.ecologic_route_ws.Models;

public class Vehicle {
    private String vehicleType; // Corresponds to PlanificateurTrajetsEcologiques:Type
    private boolean isElectric; // Corresponds to PlanificateurTrajetsEcologiques:Electric
    private float maxSpeed; // Corresponds to PlanificateurTrajetsEcologiques:MaxSpeed
    private float energyConsumption; // Corresponds to PlanificateurTrajetsEcologiques:EnergyConsumption
    private float co2EmissionRate; // Corresponds to PlanificateurTrajetsEcologiques:CO2EmissionRate
    private boolean publicTransport; // Corresponds to PlanificateurTrajetsEcologiques:PublicTransport
    private String subClass;

    // Getters and Setters
    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isElectric() {
        return isElectric;
    }

    public void setElectric(boolean electric) {
        isElectric = electric;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(float energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public float getCo2EmissionRate() {
        return co2EmissionRate;
    }

    public void setCo2EmissionRate(float co2EmissionRate) {
        this.co2EmissionRate = co2EmissionRate;
    }

    public boolean isPublicTransport() {
        return publicTransport;
    }

    public void setPublicTransport(boolean publicTransport) {
        this.publicTransport = publicTransport;
    }
    public String getSubClass() {
        return subClass;
    }

    public void setSubClass(String subClass) {
        this.subClass = subClass;
    }
}
