package com.disasterrelief.controller;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.dto.CsvImportResultDto;
import com.disasterrelief.dto.DisasterZoneRequest;
import com.disasterrelief.model.CsvImportHistory;
import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.service.DisasterService;
import com.disasterrelief.utility.CSVImporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
@Tag(name = "Data Ingestion", description = "Bulk CSV ingestion of disaster relief requests")
public class ImportController {

    private final DisasterService disasterService;
    private final ReliefDAO reliefDAO;

    @Autowired
    public ImportController(DisasterService disasterService, ReliefDAO reliefDAO) {
        this.disasterService = disasterService;
        this.reliefDAO = reliefDAO;
    }

    @PostMapping("/csv")
    @Operation(summary = "Import disaster requests from uploaded CSV or default data/relief_requests.csv")
    public ResponseEntity<CsvImportResultDto> importCsv(
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        Set<String> existingCodes = disasterService.getAllZones().stream()
                .map(DisasterZone::getZoneCode)
                .collect(Collectors.toSet());

        CSVImporter.IngestionOutcome outcome;

        if (file != null && !file.isEmpty()) {
            try (InputStream is = file.getInputStream()) {
                outcome = CSVImporter.importFromStream(is, file.getOriginalFilename(), existingCodes);
            }
        } else {
            Path defaultPath = Paths.get("data", "relief_requests.csv");
            if (!Files.exists(defaultPath)) {
                CsvImportResultDto res = new CsvImportResultDto();
                res.setFilename("relief_requests.csv");
                res.getErrorMessages().add("Default file data/relief_requests.csv not found");
                return ResponseEntity.badRequest().body(res);
            }
            outcome = CSVImporter.importFromPath(defaultPath, existingCodes);
        }

        // Save valid requests
        for (DisasterZoneRequest req : outcome.getValidRequests()) {
            disasterService.registerDisasterZone(req);
        }

        // Record import history in database
        CsvImportResultDto summary = outcome.getSummary();
        String errorSummary = String.join("; ", summary.getErrorMessages());
        if (errorSummary.length() > 2000) {
            errorSummary = errorSummary.substring(0, 1990) + "...";
        }

        CsvImportHistory history = new CsvImportHistory(
                summary.getFilename(),
                summary.getTotalRows(),
                summary.getSuccessfulRows(),
                summary.getRejectedRows(),
                errorSummary.isEmpty() ? "All rows valid" : errorSummary
        );
        reliefDAO.recordImportHistory(history);

        return ResponseEntity.ok(summary);
    }
}

