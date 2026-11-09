package com.disasterrelief.utility;

import com.disasterrelief.dto.CsvImportResultDto;
import com.disasterrelief.dto.DisasterZoneRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * CSVImporter provides resilient, validated bulk ingestion of disaster requests.
 */
public class CSVImporter {

    private static final Logger logger = LoggerFactory.getLogger(CSVImporter.class);

    private static final String[] EXPECTED_HEADERS = {
            "zone_code", "zone_name", "event_type", "affected_population",
            "medical_urgency_score", "terrain_risk", "road_accessibility",
            "weather_severity_index", "flight_distance_km"
    };

    /**
     * Ingests CSV from an InputStream or local Path.
     */
    public static class IngestionOutcome {
        private final CsvImportResultDto summary;
        private final List<DisasterZoneRequest> validRequests;

        public IngestionOutcome(CsvImportResultDto summary, List<DisasterZoneRequest> validRequests) {
            this.summary = summary;
            this.validRequests = validRequests;
        }

        public CsvImportResultDto getSummary() { return summary; }
        public List<DisasterZoneRequest> getValidRequests() { return validRequests; }
    }

    public static IngestionOutcome importFromPath(Path csvPath, Set<String> existingZoneCodes) throws IOException {
        try (InputStream is = Files.newInputStream(csvPath)) {
            return importFromStream(is, csvPath.getFileName().toString(), existingZoneCodes);
        }
    }

    public static IngestionOutcome importFromStream(InputStream is, String filename, Set<String> existingZoneCodes) throws IOException {
        CsvImportResultDto result = new CsvImportResultDto();
        result.setFilename(filename);
        List<DisasterZoneRequest> validRequests = new ArrayList<>();
        Set<String> encounteredInFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.getErrorMessages().add("CSV file is empty");
                return new IngestionOutcome(result, validRequests);
            }

            // Validate header tokens
            String[] headers = headerLine.split(",");
            if (headers.length < EXPECTED_HEADERS.length) {
                result.getErrorMessages().add(String.format("Invalid header column count. Expected %d but got %d",
                        EXPECTED_HEADERS.length, headers.length));
                return new IngestionOutcome(result, validRequests);
            }

            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                if (!headers[i].trim().equalsIgnoreCase(EXPECTED_HEADERS[i])) {
                    result.getErrorMessages().add(String.format("Header column %d mismatch: expected '%s', found '%s'",
                            i + 1, EXPECTED_HEADERS[i], headers[i].trim()));
                    return new IngestionOutcome(result, validRequests);
                }
            }

            String line;
            int lineNumber = 1;
            int total = 0;
            int success = 0;
            int rejected = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip blank lines safely
                }
                total++;

                String[] tokens = line.split(",", -1);
                if (tokens.length < EXPECTED_HEADERS.length) {
                    rejected++;
                    result.getErrorMessages().add(String.format("Line %d: Malformed row (found %d columns, expected %d)",
                            lineNumber, tokens.length, EXPECTED_HEADERS.length));
                    continue;
                }

                String zoneCode = tokens[0].trim();
                String zoneName = tokens[1].trim();
                String eventType = tokens[2].trim().toUpperCase();
                String popStr = tokens[3].trim();
                String urgencyStr = tokens[4].trim();
                String terrainStr = tokens[5].trim();
                String roadStr = tokens[6].trim();
                String weatherStr = tokens[7].trim();
                String distStr = tokens[8].trim();

                // Validate Zone Code Regex
                if (!ZoneCodeValidator.isValid(zoneCode)) {
                    rejected++;
                    result.getErrorMessages().add(String.format("Line %d: Invalid zone code '%s'. Must match ZONE-[A-Z]{2}-\\d{4}",
                            lineNumber, zoneCode));
                    continue;
                }

                // Duplicate Check
                if (existingZoneCodes != null && existingZoneCodes.contains(zoneCode)) {
                    rejected++;
                    result.getErrorMessages().add(String.format("Line %d: Zone code '%s' already exists in database",
                            lineNumber, zoneCode));
                    continue;
                }
                if (encounteredInFile.contains(zoneCode)) {
                    rejected++;
                    result.getErrorMessages().add(String.format("Line %d: Duplicate zone code '%s' within CSV file",
                            lineNumber, zoneCode));
                    continue;
                }

                // Numeric Validations
                try {
                    int population = Integer.parseInt(popStr);
                    if (population <= 0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Affected population must be > 0 (found %d)", lineNumber, population));
                        continue;
                    }

                    double urgency = Double.parseDouble(urgencyStr);
                    if (urgency < 1.0 || urgency > 10.0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Medical urgency score must be between 1.0 and 10.0 (found %.1f)", lineNumber, urgency));
                        continue;
                    }

                    double terrain = Double.parseDouble(terrainStr);
                    if (terrain < 0.0 || terrain > 100.0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Terrain risk must be between 0.0 and 100.0 (found %.1f)", lineNumber, terrain));
                        continue;
                    }

                    double road = Double.parseDouble(roadStr);
                    if (road < 0.0 || road > 100.0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Road accessibility must be between 0.0 and 100.0 (found %.1f)", lineNumber, road));
                        continue;
                    }

                    double weather = Double.parseDouble(weatherStr);
                    if (weather < 0.0 || weather > 10.0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Weather severity index must be between 0.0 and 10.0 (found %.1f)", lineNumber, weather));
                        continue;
                    }

                    double dist = Double.parseDouble(distStr);
                    if (dist <= 0) {
                        rejected++;
                        result.getErrorMessages().add(String.format("Line %d: Flight distance must be positive (found %.1f)", lineNumber, dist));
                        continue;
                    }

                    DisasterZoneRequest req = new DisasterZoneRequest();
                    req.setZoneCode(zoneCode);
                    req.setZoneName(zoneName);
                    req.setEventType(eventType);
                    req.setAffectedPopulation(population);
                    req.setMedicalUrgencyScore(urgency);
                    req.setTerrainRisk(terrain);
                    req.setRoadAccessibility(road);
                    req.setWeatherSeverityIndex(weather);
                    req.setFlightDistanceKm(dist);

                    encounteredInFile.add(zoneCode);
                    validRequests.add(req);
                    result.getImportedZoneCodes().add(zoneCode);
                    success++;

                } catch (NumberFormatException nfe) {
                    rejected++;
                    result.getErrorMessages().add(String.format("Line %d: Numeric parse failure - %s", lineNumber, nfe.getMessage()));
                }
            }

            result.setTotalRows(total);
            result.setSuccessfulRows(success);
            result.setRejectedRows(rejected);
            logger.info("CSV ingestion completed: total={}, success={}, rejected={}", total, success, rejected);
        }

        return new IngestionOutcome(result, validRequests);
    }
}

