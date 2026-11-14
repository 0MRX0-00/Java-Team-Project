package com.disasterrelief.model;

import java.time.LocalDateTime;

public class ReliefCargo {
    private Long id;
    private Long disasterZoneId;
    private int foodKitCount;
    private int waterKitCount;
    private int plasmaKitCount;
    private double totalPayloadKg;
    private int requiredRations;
    private String dispatchLocation;
    private String cargoManifest; // e.g. "FOOD,100,WATER,200,PLASMA,20"
    private LocalDateTime createdAt;

    public ReliefCargo() {}

    public ReliefCargo(Long id, Long disasterZoneId, int foodKitCount, int waterKitCount,
                       int plasmaKitCount, double totalPayloadKg, int requiredRations,
                       String dispatchLocation, String cargoManifest, LocalDateTime createdAt) {
        this.id = id;
        this.disasterZoneId = disasterZoneId;
        this.foodKitCount = foodKitCount;
        this.waterKitCount = waterKitCount;
        this.plasmaKitCount = plasmaKitCount;
        this.totalPayloadKg = totalPayloadKg;
        this.requiredRations = requiredRations;
        this.dispatchLocation = dispatchLocation;
        this.cargoManifest = cargoManifest;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDisasterZoneId() { return disasterZoneId; }
    public void setDisasterZoneId(Long disasterZoneId) { this.disasterZoneId = disasterZoneId; }

    public int getFoodKitCount() { return foodKitCount; }
    public void setFoodKitCount(int foodKitCount) { this.foodKitCount = foodKitCount; }

    public int getWaterKitCount() { return waterKitCount; }
    public void setWaterKitCount(int waterKitCount) { this.waterKitCount = waterKitCount; }

    public int getPlasmaKitCount() { return plasmaKitCount; }
    public void setPlasmaKitCount(int plasmaKitCount) { this.plasmaKitCount = plasmaKitCount; }

    public double getTotalPayloadKg() { return totalPayloadKg; }
    public void setTotalPayloadKg(double totalPayloadKg) { this.totalPayloadKg = totalPayloadKg; }

    public int getRequiredRations() { return requiredRations; }
    public void setRequiredRations(int requiredRations) { this.requiredRations = requiredRations; }

    public String getDispatchLocation() { return dispatchLocation; }
    public void setDispatchLocation(String dispatchLocation) { this.dispatchLocation = dispatchLocation; }

    public String getCargoManifest() { return cargoManifest; }
    public void setCargoManifest(String cargoManifest) { this.cargoManifest = cargoManifest; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

