package com.example.ecologic_route_ws.Models;


public class TrafficCondition {

    private int id;

    private String trafficLevel;

    private int averageDelay;


    public TrafficCondition(int id, String trafficLevel, int averageDelay) {

        this.id = id;

        this.trafficLevel = trafficLevel;

        this.averageDelay = averageDelay;

    }


    // Getters and Setters

    public int getId() {

        return id;

    }


    public void setId(int id) {

        this.id = id;

    }


    public String getTrafficLevel() {

        return trafficLevel;

    }


    public void setTrafficLevel(String trafficLevel) {

        this.trafficLevel = trafficLevel;

    }


    public int getAverageDelay() {

        return averageDelay;

    }


    public void setAverageDelay(int averageDelay) {

        this.averageDelay = averageDelay;

    }

}