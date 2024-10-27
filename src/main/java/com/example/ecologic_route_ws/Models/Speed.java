package com.example.ecologic_route_ws.Models;

public class Speed {

    private Long id;
    private float speedValue;
    private boolean fastSpeed;
    private boolean mediumSpeed;
    private boolean slowSpeed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(float speedValue) {
        this.speedValue = speedValue;
    }

    public boolean isFastSpeed() {
        return fastSpeed;
    }

    public void setFastSpeed(boolean fastSpeed) {
        this.fastSpeed = fastSpeed;
    }

    public boolean isMediumSpeed() {
        return mediumSpeed;
    }

    public void setMediumSpeed(boolean mediumSpeed) {
        this.mediumSpeed = mediumSpeed;
    }

    public boolean isSlowSpeed() {
        return slowSpeed;
    }

    public void setSlowSpeed(boolean slowSpeed) {
        this.slowSpeed = slowSpeed;
    }
}
