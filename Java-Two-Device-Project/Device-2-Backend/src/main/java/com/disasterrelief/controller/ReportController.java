package com.disasterrelief.controller;

import com.disasterrelief.dto.DashboardStatsDto;
import com.disasterrelief.model.DispatchLog;
import com.disasterrelief.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Reports & Analytics", description = "Operational manifest exports, dispatch logs, and executive reporting")
public class ReportController {

    private final ReportService reportService;
    private final DashboardController dashboardController;

    @Autowired
    public ReportController(ReportService reportService, DashboardController dashboardController) {
        this.reportService = reportService;
        this.dashboardController = dashboardController;
    }

    @GetMapping("/manifests")
    @Operation(summary = "Export and retrieve formatted emergency flight manifests")
    public ResponseEntity<String> getManifests() throws IOException {
        String content = reportService.generateAndReadFlightManifests();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"emergency_flight_manifests.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @GetMapping("/dispatch-logs")
    @Operation(summary = "Export and retrieve multithreaded dispatch execution logs")
    public ResponseEntity<List<DispatchLog>> getDispatchLogs(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reportService.getRecentLogs(limit));
    }

    @GetMapping("/supply-drop-logs")
    @Operation(summary = "Export supply drop logs")
    public ResponseEntity<String> getSupplyDropLogs() throws IOException {
        String content = reportService.generateAndReadSupplyDropLogs();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supply_drop_logs.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @GetMapping("/summary")
    @Operation(summary = "Export and retrieve full operational readiness text summary")
    public ResponseEntity<String> getSummary() throws IOException {
        DashboardStatsDto stats = dashboardController.getStatistics().getBody();
        String content = reportService.generateAndReadSummary(stats);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dispatch_summary.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }
}

