package com.disasterrelief.model;

import java.time.LocalDateTime;

public class DispatchMission {
    private Long id;
    private String missionCode;
    private Long disasterZoneId;
    private Long reliefCargoId;
    private Long transportAssetId;
    private double ddusScore;
    private String priorityLevel; // PRIORITY_ALPHA, PRIORITY_BRAVO, PRIORITY_CHARLIE, MISSION_ABORTED
    private String transportCategory;
    private Double transportSuitabilityScore;
    private boolean weatherValidationPassed;
    private String rejectionReason;
    private String status; // PENDING, DISPATCHED, IN_TRANSIT, COMPLETED, FLIGHT_GROUNDED, ABORTED
    private LocalDateTime dispatchedAt;
    private int estimatedDurationMin;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields for display
    private String zoneCode;
    private String zoneName;
    private String assetCode;
    private String assetName;

    public DispatchMission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }

    public Long getDisasterZoneId() { return disasterZoneId; }
    public void setDisasterZoneId(Long disasterZoneId) { this.disasterZoneId = disasterZoneId; }

    public Long getReliefCargoId() { return reliefCargoId; }
    public void setReliefCargoId(Long reliefCargoId) { this.reliefCargoId = reliefCargoId; }

    public Long getTransportAssetId() { return transportAssetId; }
    public void setTransportAssetId(Long transportAssetId) { this.transportAssetId = transportAssetId; }

    public double getDdusScore() { return ddusScore; }
    public void setDdusScore(double ddusScore) { this.ddusScore = ddusScore; }

    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getTransportCategory() { return transportCategory; }
    public void setTransportCategory(String transportCategory) { this.transportCategory = transportCategory; }

    public Double getTransportSuitabilityScore() { return transportSuitabilityScore; }
    public void setTransportSuitabilityScore(Double transportSuitabilityScore) { this.transportSuitabilityScore = transportSuitabilityScore; }

    public boolean isWeatherValidationPassed() { return weatherValidationPassed; }
    public void setWeatherValidationPassed(boolean weatherValidationPassed) { this.weatherValidationPassed = weatherValidationPassed; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public int getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
}

