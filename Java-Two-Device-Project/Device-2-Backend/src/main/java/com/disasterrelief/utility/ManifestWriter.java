package com.disasterrelief.utility;

import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.model.FieldConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ManifestWriter handles formatted text exports using Java NIO and BufferedWriter.
 * Exports emergency flight manifests, supply drop logs, and dispatch summaries.
 */
public class ManifestWriter {

    private static final Logger logger = LoggerFactory.getLogger(ManifestWriter.class);
    private static final Path EXPORTS_DIR = Paths.get("exports");

    public ManifestWriter() {
        ensureExportDirectory();
    }

    private static void ensureExportDirectory() {
        try {
            if (!Files.exists(EXPORTS_DIR)) {
                Files.createDirectories(EXPORTS_DIR);
            }
        } catch (IOException e) {
            logger.error("Failed to initialize exports directory: {}", e.getMessage());
        }
    }

    /**
     * Generates and writes exports/emergency_flight_manifests.txt using String.format() and BufferedWriter.
     */
    public Path writeEmergencyFlightManifests(List<DispatchMission> missions) throws IOException {
        ensureExportDirectory();
        Path targetPath = EXPORTS_DIR.resolve("emergency_flight_manifests.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write("========================================================================================\n");
            writer.write("  DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)        \n");
            writer.write("  OFFICIAL EMERGENCY FLIGHT & DISPATCH MANIFEST REPORT                                  \n");
            writer.write(String.format("  Generated At: %s%n", DateTimeUtils.formatDateTime(LocalDateTime.now())));
            writer.write("========================================================================================\n\n");

            String headerFormat = "| %-14s | %-13s | %-10s | %-24s | %-15s | %-6s | %-14s |%n";
            String divider = "+----------------+---------------+------------+--------------------------+-----------------+--------+----------------+\n";

            writer.write(divider);
            writer.write(String.format(headerFormat, "MISSION ID", "ZONE CODE", "EVENT", "TRANSPORT MODE", "PRIORITY", "DDUS", "STATUS"));
            writer.write(divider);

            for (DispatchMission m : missions) {
                writer.write(String.format(headerFormat,
                        m.getMissionCode(),
                        m.getZoneCode() != null ? m.getZoneCode() : "ZONE-" + m.getDisasterZoneId(),
                        "ACTIVE",
                        m.getTransportCategory() != null ? m.getTransportCategory() : "UNASSIGNED",
                        m.getPriorityLevel(),
                        String.format("%.1f", m.getDdusScore()),
                        m.getStatus()
                ));
            }
            writer.write(divider);
            writer.write(String.format("%nTotal Manifested Missions: %d%n", missions.size()));
        }

        logger.info("Successfully generated emergency flight manifest report at: {}", targetPath.toAbsolutePath());
        return targetPath;
    }

    /**
     * Generates and writes exports/supply_drop_logs.txt.
     */
    public Path writeSupplyDropLogs(List<FieldConfirmation> confirmations) throws IOException {
        ensureExportDirectory();
        Path targetPath = EXPORTS_DIR.resolve("supply_drop_logs.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write("========================================================================================\n");
            writer.write("  DISASTER RELIEF AUDITED SUPPLY DROP LOGS                                              \n");
            writer.write(String.format("  Report Timestamp: %s%n", DateTimeUtils.formatDateTime(LocalDateTime.now())));
            writer.write("========================================================================================\n\n");

            String header = String.format("| %-14s | %-12s | %-10s | %-12s | %-30s |%n",
                    "CONFIRM ID", "TRANSIT MIN", "PRECISION M", "RATIONS", "FIELD OFFICER");
            String line = "+----------------+--------------+------------+--------------+--------------------------------+\n";

            writer.write(line);
            writer.write(header);
            writer.write(line);

            for (FieldConfirmation fc : confirmations) {
                writer.write(String.format("| %-14d | %-12d | %-10.1fm | %-12d | %-30s |%n",
                        fc.getId(),
                        fc.getFlightTransitDurationMin(),
                        fc.getDropOffPrecisionMeters(),
                        fc.getDeliveredRations(),
                        fc.getFieldOfficerName()
                ));
                if (fc.getDeliveryNotes() != null && !fc.getDeliveryNotes().isEmpty()) {
                    writer.write(String.format("  Notes: %s%n", fc.getDeliveryNotes().trim()));
                }
            }
            writer.write(line);
        }

        logger.info("Successfully generated supply drop logs at: {}", targetPath.toAbsolutePath());
        return targetPath;
    }

    /**
     * Generates and writes exports/dispatch_summary.txt.
     */
    public Path writeDispatchSummary(String summaryContent) throws IOException {
        ensureExportDirectory();
        Path targetPath = EXPORTS_DIR.resolve("dispatch_summary.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write(summaryContent);
        }

        logger.info("Successfully generated dispatch summary at: {}", targetPath.toAbsolutePath());
        return targetPath;
    }
}

