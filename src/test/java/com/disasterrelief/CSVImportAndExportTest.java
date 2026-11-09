package com.disasterrelief;

import com.disasterrelief.dto.CsvImportResultDto;
import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.utility.CSVImporter;
import com.disasterrelief.utility.ManifestWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CSV Bulk Ingestion & TXT Manifest Export Tests")
class CSVImportAndExportTest {

    @Test
    @DisplayName("Should successfully ingest valid CSV stream")
    void testValidCsvIngestion() throws IOException {
        String csv = """
                zone_code,zone_name,event_type,affected_population,medical_urgency_score,terrain_risk,road_accessibility,weather_severity_index,flight_distance_km
                ZONE-TN-1010,Salem Sector,FLOOD,25000,8.5,60.0,40.0,4.0,200.0
                ZONE-KL-2020,Munnar Highlands,LANDSLIDE,14000,9.0,85.0,15.0,5.0,120.0
                """;

        ByteArrayInputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        CSVImporter.IngestionOutcome outcome = CSVImporter.importFromStream(is, "test.csv", Collections.emptySet());

        CsvImportResultDto summary = outcome.getSummary();
        assertEquals(2, summary.getTotalRows());
        assertEquals(2, summary.getSuccessfulRows());
        assertEquals(0, summary.getRejectedRows());
        assertEquals(2, outcome.getValidRequests().size());
    }

    @Test
    @DisplayName("Should detect invalid regex and reject malformed CSV rows")
    void testMalformedCsvRows() throws IOException {
        String csv = """
                zone_code,zone_name,event_type,affected_population,medical_urgency_score,terrain_risk,road_accessibility,weather_severity_index,flight_distance_km
                INVALID-CODE,Salem Sector,FLOOD,25000,8.5,60.0,40.0,4.0,200.0
                ZONE-TN-1011,Short Row,FLOOD,25000
                ZONE-TN-1012,Bad Urgency,FLOOD,25000,99.9,60.0,40.0,4.0,200.0
                """;

        ByteArrayInputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        CSVImporter.IngestionOutcome outcome = CSVImporter.importFromStream(is, "malformed.csv", Collections.emptySet());

        CsvImportResultDto summary = outcome.getSummary();
        assertEquals(3, summary.getTotalRows());
        assertEquals(0, summary.getSuccessfulRows());
        assertEquals(3, summary.getRejectedRows());
        assertTrue(summary.getErrorMessages().size() >= 3);
    }

    @Test
    @DisplayName("Should detect duplicate zone codes in database and CSV")
    void testDuplicateDetection() throws IOException {
        String csv = """
                zone_code,zone_name,event_type,affected_population,medical_urgency_score,terrain_risk,road_accessibility,weather_severity_index,flight_distance_km
                ZONE-TN-1001,Existing Zone,FLOOD,25000,8.5,60.0,40.0,4.0,200.0
                """;

        Set<String> existing = Set.of("ZONE-TN-1001");
        ByteArrayInputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        CSVImporter.IngestionOutcome outcome = CSVImporter.importFromStream(is, "dup.csv", existing);

        assertEquals(1, outcome.getSummary().getRejectedRows());
        assertTrue(outcome.getSummary().getErrorMessages().get(0).contains("already exists"));
    }

    @Test
    @DisplayName("Should export formatted flight manifests via ManifestWriter")
    void testManifestWriterExport() throws IOException {
        ManifestWriter writer = new ManifestWriter();

        DispatchMission m = new DispatchMission();
        m.setMissionCode("MSN-TEST-001");
        m.setZoneCode("ZONE-TN-1001");
        m.setTransportCategory("HEAVY_CARGO_HELICOPTER");
        m.setPriorityLevel("PRIORITY_ALPHA");
        m.setDdusScore(94.5);
        m.setStatus("DISPATCHED");

        Path manifestPath = writer.writeEmergencyFlightManifests(List.of(m));
        assertTrue(Files.exists(manifestPath));

        String content = Files.readString(manifestPath);
        assertTrue(content.contains("OFFICIAL EMERGENCY FLIGHT & DISPATCH MANIFEST REPORT"));
        assertTrue(content.contains("MSN-TEST-001"));
        assertTrue(content.contains("ZONE-TN-1001"));
        assertTrue(content.contains("PRIORITY_ALPHA"));
    }
}

