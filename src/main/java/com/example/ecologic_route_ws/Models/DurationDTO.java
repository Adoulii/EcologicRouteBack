package com.example.ecologic_route_ws.Models;

public class DurationDTO {
    private Long id;
    private int exactDuration; // Assuming this is in minutes, or you can specify units
    private boolean longDuration;
    private boolean mediumDuration;
    private boolean shortDuration;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getExactDuration() {
        return exactDuration;
    }

    public void setExactDuration(int exactDuration) {
        this.exactDuration = exactDuration;
    }

    public boolean isLongDuration() {
        return longDuration;
    }

    public void setLongDuration(boolean longDuration) {
        this.longDuration = longDuration;
    }

    public boolean isMediumDuration() {
        return mediumDuration;
    }

    public void setMediumDuration(boolean mediumDuration) {
        this.mediumDuration = mediumDuration;
    }

    public boolean isShortDuration() {
        return shortDuration;
    }

    public void setShortDuration(boolean shortDuration) {
        this.shortDuration = shortDuration;
    }
}