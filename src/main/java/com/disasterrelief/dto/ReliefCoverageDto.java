package com.disasterrelief.dto;

public class ReliefCoverageDto {
    private Long disasterZoneId;
    private String zoneCode;
    private String zoneName;
    private int totalRequiredRations;
    private int totalDeliveredRations;
    private double reliefCoverageRatio; // (Delivered / Required) * 100

    public ReliefCoverageDto() {}

    public ReliefCoverageDto(Long disasterZoneId, String zoneCode, String zoneName,
                             int totalRequiredRations, int totalDeliveredRations,
                             double reliefCoverageRatio) {
        this.disasterZoneId = disasterZoneId;
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
        this.totalRequiredRations = totalRequiredRations;
        this.totalDeliveredRations = totalDeliveredRations;
        this.reliefCoverageRatio = reliefCoverageRatio;
    }

    public Long getDisasterZoneId() { return disasterZoneId; }
    public void setDisasterZoneId(Long disasterZoneId) { this.disasterZoneId = disasterZoneId; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public int getTotalRequiredRations() { return totalRequiredRations; }
    public void setTotalRequiredRations(int totalRequiredRations) { this.totalRequiredRations = totalRequiredRations; }

    public int getTotalDeliveredRations() { return totalDeliveredRations; }
    public void setTotalDeliveredRations(int totalDeliveredRations) { this.totalDeliveredRations = totalDeliveredRations; }

    public double getReliefCoverageRatio() { return reliefCoverageRatio; }
    public void setReliefCoverageRatio(double reliefCoverageRatio) { this.reliefCoverageRatio = reliefCoverageRatio; }
}

