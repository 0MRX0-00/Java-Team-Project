package com.disasterrelief.controller;

import com.disasterrelief.MainApp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HealthController provides real-time health diagnostic endpoints for
 * verifying two-device network connectivity, database ping, and server status.
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
@Tag(name = "Health Diagnostics", description = "Device 2 Backend & Database Diagnostic Endpoint for Two-Device Architecture")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "System health check and database ping for two-device verification")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean dbOk = false;
        String dbMessage;

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbOk = (result != null && result == 1);
            dbMessage = "Active (MySQL/MariaDB connection verified)";
        } catch (Exception ex) {
            dbMessage = "Database unreachable: " + ex.getMessage();
        }

        String hostIp = "127.0.0.1";
        String hostName = "localhost";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostIp = localHost.getHostAddress();
            hostName = localHost.getHostName();
        } catch (Exception ignored) {
        }

        response.put("status", dbOk ? "UP" : "DEGRADED");
        response.put("service", "Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)");
        response.put("role", "Device 2 - Backend & Database Host");
        response.put("databaseStatus", dbOk ? "CONNECTED" : "DISCONNECTED");
        response.put("databaseMessage", dbMessage);
        response.put("serverIp", hostIp);
        response.put("serverHost", hostName);
        response.put("timestamp", Instant.now().toString());
        response.put("uptimeMillis", System.currentTimeMillis() - MainApp.START_TIME);

        return ResponseEntity.status(dbOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
