package com.example.ecologic_route_ws.Models;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public class PublicTransportDTO {

    private Long id; // Optional, if you are using it
    private boolean shortDistance;
    private boolean operatesOnWeekend;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private PublicTransportType transportType; // Enum
    private String lineNumber;
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isShortDistance() {
        return shortDistance;
    }

    public void setShortDistance(boolean shortDistance) {
        this.shortDistance = shortDistance;
    }

    public boolean isOperatesOnWeekend() {
        return operatesOnWeekend;
    }

    public void setOperatesOnWeekend(boolean operatesOnWeekend) {
        this.operatesOnWeekend = operatesOnWeekend;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public PublicTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(PublicTransportType transportType) {
        this.transportType = transportType;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }
}
