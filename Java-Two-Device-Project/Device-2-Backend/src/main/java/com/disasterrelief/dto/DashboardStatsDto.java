package com.disasterrelief.dto;

import java.util.Map;

public class DashboardStatsDto {
    private int totalDisasterZones;
    private int activeMissions;
    private int completedMissions;
    private int pendingRequests;
    private int groundedMissions;
    private int abortedMissions;
    private int availableTransportAssets;
    private double overallReliefCoveragePercentage;

    private Map<String, Integer> zoneEventTypeDistribution;
    private Map<String, Integer> transportCategoryDistribution;
    private Map<String, Integer> missionPriorityDistribution;
    private Map<String, Integer> missionStatusDistribution;

    public DashboardStatsDto() {}

    public int getTotalDisasterZones() { return totalDisasterZones; }
    public void setTotalDisasterZones(int totalDisasterZones) { this.totalDisasterZones = totalDisasterZones; }

    public int getActiveMissions() { return activeMissions; }
    public void setActiveMissions(int activeMissions) { this.activeMissions = activeMissions; }

    public int getCompletedMissions() { return completedMissions; }
    public void setCompletedMissions(int completedMissions) { this.completedMissions = completedMissions; }

    public int getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(int pendingRequests) { this.pendingRequests = pendingRequests; }

    public int getGroundedMissions() { return groundedMissions; }
    public void setGroundedMissions(int groundedMissions) { this.groundedMissions = groundedMissions; }

    public int getAbortedMissions() { return abortedMissions; }
    public void setAbortedMissions(int abortedMissions) { this.abortedMissions = abortedMissions; }

    public int getAvailableTransportAssets() { return availableTransportAssets; }
    public void setAvailableTransportAssets(int availableTransportAssets) { this.availableTransportAssets = availableTransportAssets; }

    public double getOverallReliefCoveragePercentage() { return overallReliefCoveragePercentage; }
    public void setOverallReliefCoveragePercentage(double overallReliefCoveragePercentage) { this.overallReliefCoveragePercentage = overallReliefCoveragePercentage; }

    public Map<String, Integer> getZoneEventTypeDistribution() { return zoneEventTypeDistribution; }
    public void setZoneEventTypeDistribution(Map<String, Integer> zoneEventTypeDistribution) { this.zoneEventTypeDistribution = zoneEventTypeDistribution; }

    public Map<String, Integer> getTransportCategoryDistribution() { return transportCategoryDistribution; }
    public void setTransportCategoryDistribution(Map<String, Integer> transportCategoryDistribution) { this.transportCategoryDistribution = transportCategoryDistribution; }

    public Map<String, Integer> getMissionPriorityDistribution() { return missionPriorityDistribution; }
    public void setMissionPriorityDistribution(Map<String, Integer> missionPriorityDistribution) { this.missionPriorityDistribution = missionPriorityDistribution; }

    public Map<String, Integer> getMissionStatusDistribution() { return missionStatusDistribution; }
    public void setMissionStatusDistribution(Map<String, Integer> missionStatusDistribution) { this.missionStatusDistribution = missionStatusDistribution; }
}

