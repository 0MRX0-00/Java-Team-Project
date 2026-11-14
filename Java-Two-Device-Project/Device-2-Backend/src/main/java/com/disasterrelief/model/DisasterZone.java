package com.disasterrelief.model;

import java.time.LocalDateTime;

public class DisasterZone {
    private Long id;
    private String zoneCode;
    private String zoneName;
    private String eventType; // FLOOD, QUAKE, CYCLONE, LANDSLIDE
    private int affectedPopulation;
    private double medicalUrgencyScore; // 1.0 to 10.0
    private double terrainRisk; // 0.0 to 100.0
    private double roadAccessibility; // 0.0 to 100.0
    private double weatherSeverityIndex; // 0.0 to 10.0
    private double flightDistanceKm;
    private String status; // ACTIVE, RESOLVED, EVACUATED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DisasterZone() {
    }

    public DisasterZone(Long id, String zoneCode, String zoneName, String eventType, int affectedPopulation,
                        double medicalUrgencyScore, double terrainRisk, double roadAccessibility,
                        double weatherSeverityIndex, double flightDistanceKm, String status,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
        this.eventType = eventType;
        this.affectedPopulation = affectedPopulation;
        this.medicalUrgencyScore = medicalUrgencyScore;
        this.terrainRisk = terrainRisk;
        this.roadAccessibility = roadAccessibility;
        this.weatherSeverityIndex = weatherSeverityIndex;
        this.flightDistanceKm = flightDistanceKm;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

