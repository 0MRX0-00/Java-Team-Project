package com.disasterrelief.controller;

import com.disasterrelief.dto.DisasterZoneRequest;
import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.service.DisasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disasters")
@CrossOrigin(origins = "*")
@Tag(name = "Disaster Intake", description = "Disaster zone registration and lifecycle management")
public class DisasterController {

    private final DisasterService disasterService;

    @Autowired
    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    @PostMapping
    @Operation(summary = "Register a new disaster zone (Phase 1 Intake)")
    public ResponseEntity<DisasterZone> registerDisaster(@Valid @RequestBody DisasterZoneRequest req) {
        DisasterZone saved = disasterService.registerDisasterZone(req);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Retrieve all registered disaster zones")
    public ResponseEntity<List<DisasterZone>> getAllDisasters() {
        return ResponseEntity.ok(disasterService.getAllZones());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve specific disaster zone by ID")
    public ResponseEntity<DisasterZone> getDisasterById(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getZoneById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing disaster zone")
    public ResponseEntity<DisasterZone> updateDisaster(@PathVariable Long id, @Valid @RequestBody DisasterZoneRequest req) {
        return ResponseEntity.ok(disasterService.updateZone(id, req));
    }
}

