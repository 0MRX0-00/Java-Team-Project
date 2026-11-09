package com.disasterrelief.controller;

import com.disasterrelief.dto.DispatchResultDto;
import com.disasterrelief.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
@CrossOrigin(origins = "*")
@Tag(name = "Dispatch Center", description = "Intelligent multithreaded dispatch scheduling and concurrent worker orchestration")
public class DispatchController {

    private final DispatchService dispatchService;

    @Autowired
    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/{zoneId}")
    @Operation(summary = "Execute intelligent dispatch evaluation and asset allocation for a disaster zone")
    public ResponseEntity<DispatchResultDto> dispatchSingleZone(@PathVariable Long zoneId) {
        DispatchResultDto result = dispatchService.dispatchZone(zoneId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Trigger concurrent multithreaded dispatch across multiple zones simultaneously")
    public ResponseEntity<List<DispatchResultDto>> dispatchBulk(@RequestBody List<Long> zoneIds) {
        List<DispatchResultDto> results = dispatchService.dispatchBulk(zoneIds);
        return ResponseEntity.ok(results);
    }
}

