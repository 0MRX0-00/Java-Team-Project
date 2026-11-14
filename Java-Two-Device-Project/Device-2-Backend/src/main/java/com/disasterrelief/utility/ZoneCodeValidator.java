package com.disasterrelief.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ZoneCodeValidator enforces the academic assignment regex: ZONE-[A-Z]{2}-\d{4}
 * Uses Java Pattern and Matcher explicitly.
 */
public class ZoneCodeValidator {

    public static final String ZONE_CODE_REGEX = "^ZONE-[A-Z]{2}-\\d{4}$";
    private static final Pattern PATTERN = Pattern.compile(ZONE_CODE_REGEX);

    private ZoneCodeValidator() {}

    /**
     * Validates a zone code string against the required Pattern.
     * @param zoneCode the candidate code to test
     * @return true if matches ZONE-XX-0000 exactly, false otherwise.
     */
    public static boolean isValid(String zoneCode) {
        if (zoneCode == null) {
            return false;
        }
        Matcher matcher = PATTERN.matcher(zoneCode.trim());
        return matcher.matches();
    }

    /**
     * Provides a diagnostic validation message explaining validity or formatting defects.
     */
    public static String getValidationReport(String zoneCode) {
        if (zoneCode == null || zoneCode.trim().isEmpty()) {
            return "Zone code cannot be empty. Expected format: ZONE-XX-0000 (e.g. ZONE-TN-1001)";
        }
        String clean = zoneCode.trim();
        Matcher matcher = PATTERN.matcher(clean);
        if (matcher.matches()) {
            return "VALID";
        }
        if (!clean.startsWith("ZONE-")) {
            return "Invalid prefix. Zone code must start with uppercase 'ZONE-'";
        }
        String[] parts = clean.split("-");
        if (parts.length != 3) {
            return "Invalid structure. Zone code must have exactly two hyphens: ZONE-[STATE]-[CODE]";
        }
        if (!parts[1].matches("^[A-Z]{2}$")) {
            return "Invalid state region code '" + parts[1] + "'. Must be exactly 2 uppercase letters (e.g., TN, KL, KA)";
        }
        if (!parts[2].matches("^\\d{4}$")) {
            return "Invalid sector sequence '" + parts[2] + "'. Must be exactly 4 digits (e.g., 1001)";
        }
        return "Invalid zone code format. Expected format: ZONE-[A-Z]{2}-\\d{4}";
    }
}

