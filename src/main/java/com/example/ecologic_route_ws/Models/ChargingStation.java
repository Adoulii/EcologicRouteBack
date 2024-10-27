package com.example.ecologic_route_ws.Models;

public class ChargingStation {

    private Long id;

    private float chargingSpeed;

    private boolean fastCharging;

    private String stationType;


//    private Vehicle chargesVehicle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getChargingSpeed() {
        return chargingSpeed;
    }

    public void setChargingSpeed(float chargingSpeed) {
        this.chargingSpeed = chargingSpeed;
    }

    public boolean isFastCharging() {
        return fastCharging;
    }

    public void setFastCharging(boolean fastCharging) {
        this.fastCharging = fastCharging;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

//    public Vehicle getChargesVehicle() {
//        return chargesVehicle;
//    }
//
//    public void setChargesVehicle(Vehicle chargesVehicle) {
//        this.chargesVehicle = chargesVehicle;
//    }
}
