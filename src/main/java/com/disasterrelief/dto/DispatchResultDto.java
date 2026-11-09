package com.disasterrelief.dto;

public class DispatchResultDto {
    private Long missionId;
    private String missionCode;
    private String zoneCode;
    private String zoneName;
    private double ddusScore;
    private String priorityLevel;
    private String transportCategory;
    private String assignedAssetCode;
    private String assignedAssetName;
    private double transportSuitabilityScore;
    private boolean weatherValidationPassed;
    private String rejectionReason;
    private String status;
    private String executionMessage;

    public DispatchResultDto() {}

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public double getDdusScore() { return ddusScore; }
    public void setDdusScore(double ddusScore) { this.ddusScore = ddusScore; }

    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getTransportCategory() { return transportCategory; }
    public void setTransportCategory(String transportCategory) { this.transportCategory = transportCategory; }

    public String getAssignedAssetCode() { return assignedAssetCode; }
    public void setAssignedAssetCode(String assignedAssetCode) { this.assignedAssetCode = assignedAssetCode; }

    public String getAssignedAssetName() { return assignedAssetName; }
    public void setAssignedAssetName(String assignedAssetName) { this.assignedAssetName = assignedAssetName; }

    public double getTransportSuitabilityScore() { return transportSuitabilityScore; }
    public void setTransportSuitabilityScore(double transportSuitabilityScore) { this.transportSuitabilityScore = transportSuitabilityScore; }

    public boolean isWeatherValidationPassed() { return weatherValidationPassed; }
    public void setWeatherValidationPassed(boolean weatherValidationPassed) { this.weatherValidationPassed = weatherValidationPassed; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExecutionMessage() { return executionMessage; }
    public void setExecutionMessage(String executionMessage) { this.executionMessage = executionMessage; }
}

