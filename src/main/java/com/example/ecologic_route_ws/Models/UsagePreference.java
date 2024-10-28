package com.example.ecologic_route_ws.Models;

// Base class for Usage Preference
public class UsagePreference {
    private int id; // Unique ID for the usage preference
    private boolean costEffectivePreference; // Preference for cost-effectiveness
    private boolean ecoFriendlyPreference; // Preference for eco-friendliness
    private boolean fastPreference; // Preference for speed

    // Constructor
    public UsagePreference(int id, boolean costEffectivePreference, boolean ecoFriendlyPreference, boolean fastPreference) {
        this.id = id;
        this.costEffectivePreference = costEffectivePreference;
        this.ecoFriendlyPreference = ecoFriendlyPreference;
        this.fastPreference = fastPreference;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCostEffectivePreference() {
        return costEffectivePreference;
    }

    public void setCostEffectivePreference(boolean costEffectivePreference) {
        this.costEffectivePreference = costEffectivePreference;
    }

    public boolean isEcoFriendlyPreference() {
        return ecoFriendlyPreference;
    }

    public void setEcoFriendlyPreference(boolean ecoFriendlyPreference) {
        this.ecoFriendlyPreference = ecoFriendlyPreference;
    }

    public boolean isFastPreference() {
        return fastPreference;
    }

    public void setFastPreference(boolean fastPreference) {
        this.fastPreference = fastPreference;
    }
}
