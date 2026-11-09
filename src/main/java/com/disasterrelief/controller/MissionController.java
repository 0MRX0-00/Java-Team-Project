package com.disasterrelief.controller;

import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*")
@Tag(name = "Mission Tracking", description = "Real-time dispatch mission tracking and status telemetry")
public class MissionController {

    private final MissionService missionService;

    @Autowired
    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all scheduled and executed dispatch missions")
    public ResponseEntity<List<DispatchMission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve specific mission details by ID")
    public ResponseEntity<DispatchMission> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter missions by operational status (DISPATCHED, IN_TRANSIT, COMPLETED, FLIGHT_GROUNDED, ABORTED)")
    public ResponseEntity<List<DispatchMission>> getMissionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(missionService.getMissionsByStatus(status));
    }
}

