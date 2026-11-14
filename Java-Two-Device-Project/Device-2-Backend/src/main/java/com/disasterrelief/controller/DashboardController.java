package com.disasterrelief.controller;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.dto.DashboardStatsDto;
import com.disasterrelief.service.CoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Real database-backed statistics and chart telemetry")
public class DashboardController {

    private final ReliefDAO reliefDAO;
    private final CoverageService coverageService;

    @Autowired
    public DashboardController(ReliefDAO reliefDAO, CoverageService coverageService) {
        this.reliefDAO = reliefDAO;
        this.coverageService = coverageService;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get real database-backed operational statistics")
    public ResponseEntity<DashboardStatsDto> getStatistics() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalDisasterZones(reliefDAO.countRows("disaster_zones", null));
        stats.setActiveMissions(reliefDAO.countRows("dispatch_missions", "status IN ('DISPATCHED', 'IN_TRANSIT')"));
        stats.setCompletedMissions(reliefDAO.countRows("dispatch_missions", "status = 'COMPLETED'"));
        stats.setPendingRequests(reliefDAO.countRows("dispatch_missions", "status = 'PENDING'"));
        stats.setGroundedMissions(reliefDAO.countRows("dispatch_missions", "status = 'FLIGHT_GROUNDED'"));
        stats.setAbortedMissions(reliefDAO.countRows("dispatch_missions", "status = 'ABORTED'"));
        stats.setAvailableTransportAssets(reliefDAO.countRows("transport_assets", "status = 'AVAILABLE'"));
        stats.setOverallReliefCoveragePercentage(coverageService.getOverallCoveragePercentage());

        stats.setZoneEventTypeDistribution(reliefDAO.getCountsByColumn("disaster_zones", "event_type"));
        stats.setTransportCategoryDistribution(reliefDAO.getCountsByColumn("transport_assets", "category"));
        stats.setMissionPriorityDistribution(reliefDAO.getCountsByColumn("dispatch_missions", "priority_level"));
        stats.setMissionStatusDistribution(reliefDAO.getCountsByColumn("dispatch_missions", "status"));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/charts")
    @Operation(summary = "Get Chart.js datasets for distribution visualizations")
    public ResponseEntity<Map<String, Object>> getCharts() {
        Map<String, Object> charts = new HashMap<>();
        charts.put("eventTypeDistribution", reliefDAO.getCountsByColumn("disaster_zones", "event_type"));
        charts.put("transportCategoryDistribution", reliefDAO.getCountsByColumn("transport_assets", "category"));
        charts.put("missionPriorityDistribution", reliefDAO.getCountsByColumn("dispatch_missions", "priority_level"));
        charts.put("missionStatusDistribution", reliefDAO.getCountsByColumn("dispatch_missions", "status"));
        charts.put("zoneCoverages", coverageService.getZoneCoverages());
        return ResponseEntity.ok(charts);
    }
}

