package com.disasterrelief.dto;

import jakarta.validation.constraints.*;

public class CargoIngestionRequest {

    @NotNull(message = "Disaster Zone ID is required")
    private Long disasterZoneId;

    @Min(value = 0, message = "Food kit count cannot be negative")
    private int foodKitCount;

    @Min(value = 0, message = "Water kit count cannot be negative")
    private int waterKitCount;

    @Min(value = 0, message = "Plasma kit count cannot be negative")
    private int plasmaKitCount;

    @Positive(message = "Total payload weight must be greater than zero")
    private double totalPayloadKg;

    @Positive(message = "Required rations count must be greater than zero")
    private int requiredRations;

    @NotBlank(message = "Dispatch staging location is required")
    private String dispatchLocation;

    @NotBlank(message = "Cargo manifest is required")
    private String cargoManifest; // Format: "FOOD,100,WATER,200,PLASMA,20"

    public CargoIngestionRequest() {}

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
}

