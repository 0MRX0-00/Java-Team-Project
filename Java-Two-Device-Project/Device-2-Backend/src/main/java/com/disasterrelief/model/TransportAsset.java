package com.disasterrelief.model;

import java.time.LocalDateTime;

public class TransportAsset {
    private Long id;
    private String assetCode;
    private String assetName;
    private String category; // HEAVY_CARGO_HELICOPTER, ALL_TERRAIN_RESCUE_TRUCK, EMERGENCY_MEDICAL_DRONE
    private double maxPayloadKg;
    private double maxRangeKm;
    private double weatherRiskLimit;
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private String currentLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransportAsset() {}

    public TransportAsset(Long id, String assetCode, String assetName, String category,
                          double maxPayloadKg, double maxRangeKm, double weatherRiskLimit,
                          String status, String currentLocation, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.assetCode = assetCode;
        this.assetName = assetName;
        this.category = category;
        this.maxPayloadKg = maxPayloadKg;
        this.maxRangeKm = maxRangeKm;
        this.weatherRiskLimit = weatherRiskLimit;
        this.status = status;
        this.currentLocation = currentLocation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getMaxPayloadKg() { return maxPayloadKg; }
    public void setMaxPayloadKg(double maxPayloadKg) { this.maxPayloadKg = maxPayloadKg; }

    public double getMaxRangeKm() { return maxRangeKm; }
    public void setMaxRangeKm(double maxRangeKm) { this.maxRangeKm = maxRangeKm; }

    public double getWeatherRiskLimit() { return weatherRiskLimit; }
    public void setWeatherRiskLimit(double weatherRiskLimit) { this.weatherRiskLimit = weatherRiskLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

