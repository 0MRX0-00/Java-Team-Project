package com.disasterrelief.controller;

import com.disasterrelief.exception.ResourceNotFoundException;
import com.disasterrelief.model.TransportAsset;
import com.disasterrelief.repository.TransportAssetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
@Tag(name = "Transport Fleet", description = "Management of helicopters, all-terrain rescue trucks, and drones")
public class AssetController {

    private final TransportAssetRepository assetRepository;

    @Autowired
    public AssetController(TransportAssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @GetMapping
    @Operation(summary = "Retrieve entire transport fleet assets and operational statuses")
    public ResponseEntity<List<TransportAsset>> getAllAssets() {
        return ResponseEntity.ok(assetRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Register a new transport asset into the fleet")
    public ResponseEntity<TransportAsset> registerAsset(@RequestBody TransportAsset asset) {
        TransportAsset saved = assetRepository.save(asset);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset status (AVAILABLE, OCCUPIED, MAINTENANCE)")
    public ResponseEntity<TransportAsset> updateStatus(@PathVariable Long id, @RequestParam String status) {
        TransportAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + id));
        assetRepository.updateStatus(id, status.toUpperCase());
        asset.setStatus(status.toUpperCase());
        return ResponseEntity.ok(asset);
    }
}

