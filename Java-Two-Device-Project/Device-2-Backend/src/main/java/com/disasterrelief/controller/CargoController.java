package com.disasterrelief.controller;

import com.disasterrelief.dto.CargoIngestionRequest;
import com.disasterrelief.model.ReliefCargo;
import com.disasterrelief.service.CargoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargo")
@CrossOrigin(origins = "*")
@Tag(name = "Cargo Management", description = "Relief cargo ingestion, manifest validation, and payload staging")
public class CargoController {

    private final CargoService cargoService;

    @Autowired
    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @PostMapping
    @Operation(summary = "Register relief cargo payload for a disaster zone (Phase 2 Ingestion)")
    public ResponseEntity<ReliefCargo> registerCargo(@Valid @RequestBody CargoIngestionRequest req) {
        ReliefCargo saved = cargoService.registerCargo(req);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Retrieve all registered relief cargo records")
    public ResponseEntity<List<ReliefCargo>> getAllCargo() {
        return ResponseEntity.ok(cargoService.getAllCargo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve specific cargo record by ID")
    public ResponseEntity<ReliefCargo> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(cargoService.getCargoById(id));
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Retrieve cargo assigned to specific disaster zone ID")
    public ResponseEntity<ReliefCargo> getCargoByZoneId(@PathVariable Long zoneId) {
        ReliefCargo cargo = cargoService.getCargoByZoneId(zoneId);
        return cargo != null ? ResponseEntity.ok(cargo) : ResponseEntity.notFound().build();
    }
}

