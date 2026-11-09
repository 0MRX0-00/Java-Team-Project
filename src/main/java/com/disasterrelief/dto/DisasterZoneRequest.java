package com.disasterrelief.dto;

import jakarta.validation.constraints.*;

public class DisasterZoneRequest {

    @NotBlank(message = "Disaster Zone Code is required")
    @Pattern(regexp = "ZONE-[A-Z]{2}-\\d{4}", message = "Zone code must match format ZONE-XX-0000 (e.g., ZONE-TN-1001)")
    private String zoneCode;

    @NotBlank(message = "Zone Name is required")
    @Size(max = 120, message = "Zone name must not exceed 120 characters")
    private String zoneName;

    @NotBlank(message = "Event Type is required")
    @Pattern(regexp = "FLOOD|QUAKE|CYCLONE|LANDSLIDE", message = "Event type must be FLOOD, QUAKE, CYCLONE, or LANDSLIDE")
    private String eventType;

    @Positive(message = "Affected population must be greater than zero")
    private int affectedPopulation;

    @DecimalMin(value = "1.0", message = "Medical urgency score must be at least 1.0")
    @DecimalMax(value = "10.0", message = "Medical urgency score must not exceed 10.0")
    private double medicalUrgencyScore;

    @DecimalMin(value = "0.0", message = "Terrain risk must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Terrain risk must not exceed 100.0")
    private double terrainRisk;

    @DecimalMin(value = "0.0", message = "Road accessibility must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Road accessibility must not exceed 100.0")
    private double roadAccessibility;

    @DecimalMin(value = "0.0", message = "Weather severity index must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Weather severity index must not exceed 10.0")
    private double weatherSeverityIndex;

    @Positive(message = "Flight distance must be positive")
    private double flightDistanceKm;

    public DisasterZoneRequest() {}

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getAffectedPopulation() { return affectedPopulation; }
    public void setAffectedPopulation(int affectedPopulation) { this.affectedPopulation = affectedPopulation; }

    public double getMedicalUrgencyScore() { return medicalUrgencyScore; }
    public void setMedicalUrgencyScore(double medicalUrgencyScore) { this.medicalUrgencyScore = medicalUrgencyScore; }

    public double getTerrainRisk() { return terrainRisk; }
    public void setTerrainRisk(double terrainRisk) { this.terrainRisk = terrainRisk; }

    public double getRoadAccessibility() { return roadAccessibility; }
    public void setRoadAccessibility(double roadAccessibility) { this.roadAccessibility = roadAccessibility; }

    public double getWeatherSeverityIndex() { return weatherSeverityIndex; }
    public void setWeatherSeverityIndex(double weatherSeverityIndex) { this.weatherSeverityIndex = weatherSeverityIndex; }

    public double getFlightDistanceKm() { return flightDistanceKm; }
    public void setFlightDistanceKm(double flightDistanceKm) { this.flightDistanceKm = flightDistanceKm; }
}

