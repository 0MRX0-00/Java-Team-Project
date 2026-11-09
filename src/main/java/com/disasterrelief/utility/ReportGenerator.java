package com.disasterrelief.utility;

import com.disasterrelief.dto.DashboardStatsDto;
import com.disasterrelief.dto.ReliefCoverageDto;
import com.disasterrelief.model.DispatchMission;

import java.time.LocalDateTime;
import java.util.List;

public class ReportGenerator {

    private ReportGenerator() {}

    public static String generateComprehensiveSummary(DashboardStatsDto stats,
                                                      List<DispatchMission> missions,
                                                      List<ReliefCoverageDto> coverages) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("     DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)     \n");
        sb.append("                     COMPREHENSIVE OPERATIONAL READINESS REPORT                         \n");
        sb.append(String.format("                     Generated At: %s%n", DateTimeUtils.formatDateTime(LocalDateTime.now())));
        sb.append("========================================================================================\n\n");

        sb.append("1. EXECUTIVE DISPATCH SUMMARY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Disaster Zones Registered : %d%n", stats.getTotalDisasterZones()));
        sb.append(String.format("Active Relief Missions          : %d%n", stats.getActiveMissions()));
        sb.append(String.format("Completed Missions              : %d%n", stats.getCompletedMissions()));
        sb.append(String.format("Pending Ingested Requests       : %d%n", stats.getPendingRequests()));
        sb.append(String.format("Grounded Aircraft Missions      : %d%n", stats.getGroundedMissions()));
        sb.append(String.format("Aborted Unsafe Missions         : %d%n", stats.getAbortedMissions()));
        sb.append(String.format("Available Fleet Transport Assets: %d%n", stats.getAvailableTransportAssets()));
        sb.append(String.format("Overall Relief Coverage Ratio   : %.2f%%%n%n", stats.getOverallReliefCoveragePercentage()));

        sb.append("2. ZONE-LEVEL RELIEF COVERAGE METRICS\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("| %-15s | %-30s | %-12s | %-12s | %-10s |%n", "ZONE CODE", "SECTOR NAME", "REQUIRED", "DELIVERED", "COVERAGE %"));
        sb.append("+-----------------+--------------------------------+--------------+--------------+------------+\n");
        for (ReliefCoverageDto cov : coverages) {
            sb.append(String.format("| %-15s | %-30s | %12d | %12d | %9.1f%% |%n",
                    cov.getZoneCode(),
                    cov.getZoneName().length() > 30 ? cov.getZoneName().substring(0, 27) + "..." : cov.getZoneName(),
                    cov.getTotalRequiredRations(),
                    cov.getTotalDeliveredRations(),
                    cov.getReliefCoverageRatio()
            ));
        }
        sb.append("+-----------------+--------------------------------+--------------+--------------+------------+\n\n");

        sb.append("3. RECENT DISPATCH MISSIONS STATUS\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        for (DispatchMission m : missions) {
            sb.append(String.format("Mission [%s] | Zone: %s | Priority: %-15s | DDUS: %5.1f | Mode: %-25s | Status: %s%n",
                    m.getMissionCode(),
                    m.getZoneCode() != null ? m.getZoneCode() : "N/A",
                    m.getPriorityLevel(),
                    m.getDdusScore(),
                    m.getTransportCategory() != null ? m.getTransportCategory() : "NONE",
                    m.getStatus()));
            if (m.getRejectionReason() != null && !m.getRejectionReason().isEmpty()) {
                sb.append(String.format("   Rejection / Grounding Note: %s%n", m.getRejectionReason()));
            }
        }
        sb.append("\n========================================================================================\n");
        sb.append("END OF EMERGENCY REPORT\n");
        return sb.toString();
    }
}

