package com.example.ecologic_route_ws.Models;

import java.util.List;

public class Route {
    private String id;
    private String name;
    private Float co2EmissionValue;
    private Float distanceValue;
    private Integer durationValue;
    private RouteType routeType;
    // Associations
    private List<ChargingStation> chargingStations;
//    private Distance distance;
//    private TrafficCondition trafficCondition;
//    private UsagePreference usagePreference;
//    private Vehicle vehicle;
    private Speed speed;
    // Constructors, getters, and setters

    public Route() {}

    public Route(String id, String name, Float co2EmissionValue, Float distanceValue, Integer durationValue, RouteType routeType, Speed speed) {
        this.id = id;
        this.name = name;
        this.co2EmissionValue = co2EmissionValue;
        this.distanceValue = distanceValue;
        this.durationValue = durationValue;
        this.routeType = routeType;
        this.speed = speed;
    }

    // Getter and setter methods

    public RouteType getRouteType() { return routeType; }
    public void setRouteType(RouteType routeType) { this.routeType = routeType; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Float getCo2EmissionValue() { return co2EmissionValue; }
    public void setCo2EmissionValue(Float co2EmissionValue) { this.co2EmissionValue = co2EmissionValue; }

    public Float getDistanceValue() { return distanceValue; }
    public void setDistanceValue(Float distanceValue) { this.distanceValue = distanceValue; }

    public Integer getDurationValue() { return durationValue; }
    public void setDurationValue(Integer durationValue) { this.durationValue = durationValue; }

    public List<ChargingStation> getChargingStations() { return chargingStations; }
    public void setChargingStations(List<ChargingStation> chargingStations) { this.chargingStations = chargingStations; }
    public Speed getSpeed() { return speed; }  // Getter for Speed
    public void setSpeed(Speed speed) { this.speed = speed; }
//    public Distance getDistance() { return distance; }
//    public void setDistance(Distance distance) { this.distance = distance; }
//
//    public TrafficCondition getTrafficCondition() { return trafficCondition; }
//    public void setTrafficCondition(TrafficCondition trafficCondition) { this.trafficCondition = trafficCondition; }
//
//    public UsagePreference getUsagePreference() { return usagePreference; }
//    public void setUsagePreference(UsagePreference usagePreference) { this.usagePreference = usagePreference; }
//
//    public Vehicle getVehicle() { return vehicle; }
//    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
}

// Subclass for UrbanRoute
class UrbanRoute extends Route {
    public UrbanRoute(String id, String name, Float co2EmissionValue, Float distanceValue, Integer durationValue, Speed speed) {
        super(id, name, co2EmissionValue, distanceValue, durationValue, RouteType.valueOf("URBAN_ROUTE"), speed);
    }
}

class Highway extends Route {
    public Highway(String id, String name, Float co2EmissionValue, Float distanceValue, Integer durationValue, Speed speed) {
        super(id, name, co2EmissionValue, distanceValue, durationValue, RouteType.valueOf("HIGHWAY"), speed);
    }
}
