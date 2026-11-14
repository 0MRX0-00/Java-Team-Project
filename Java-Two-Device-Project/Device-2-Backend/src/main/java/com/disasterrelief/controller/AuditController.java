package com.disasterrelief.controller;

import com.disasterrelief.dto.DeliveryAuditRequest;
import com.disasterrelief.model.FieldConfirmation;
import com.disasterrelief.service.DeliveryAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*")
@Tag(name = "Delivery Audit", description = "Field officer delivery confirmation, drop-off verification, and asset deallocation")
public class AuditController {

    private final DeliveryAuditService auditService;

    @Autowired
    public AuditController(DeliveryAuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/{id}/audit")
    @Operation(summary = "Submit field confirmation for an assigned mission (Phase 3 Audit)")
    public ResponseEntity<FieldConfirmation> recordAudit(@PathVariable Long id, @Valid @RequestBody DeliveryAuditRequest req) {
        FieldConfirmation confirmation = auditService.recordConfirmation(id, req);
        return new ResponseEntity<>(confirmation, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Alias for field delivery confirmation")
    public ResponseEntity<FieldConfirmation> confirmDelivery(@PathVariable Long id, @Valid @RequestBody DeliveryAuditRequest req) {
        return recordAudit(id, req);
    }

    @GetMapping("/confirmations/all")
    @Operation(summary = "Retrieve all audited field delivery confirmations")
    public ResponseEntity<List<FieldConfirmation>> getAllConfirmations() {
        return ResponseEntity.ok(auditService.getAllConfirmations());
    }
}

