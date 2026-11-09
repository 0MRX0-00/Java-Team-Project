package com.disasterrelief;

import com.disasterrelief.utility.ZoneCodeValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Zone Code Regex Validation Tests (Pattern & Matcher)")
class ZoneCodeValidatorTest {

    @ParameterizedTest(name = "Valid zone code: {0}")
    @ValueSource(strings = {
            "ZONE-TN-1001",
            "ZONE-KL-2002",
            "ZONE-KA-3003",
            "ZONE-AP-4004",
            "ZONE-MH-5005"
    })
    void testValidZoneCodes(String validCode) {
        assertTrue(ZoneCodeValidator.isValid(validCode),
                "Should accept valid pattern ZONE-[A-Z]{2}-\\d{4}: " + validCode);
        assertEquals("VALID", ZoneCodeValidator.getValidationReport(validCode));
    }

    @ParameterizedTest(name = "Invalid zone code: {0}")
    @ValueSource(strings = {
            "ZONE-123-1001",   // State is numeric
            "ZONE-TN-ABCD",   // Sequence is alphabetic
            "ZONE-TN-100",    // Only 3 digits
            "zone-tn-1001",   // Lowercase
            "SECTOR-TN-1001", // Wrong prefix
            "ZONE-T-1001",    // State has 1 char
            "ZONE-TNN-1001",  // State has 3 chars
            "",               // Empty
            "   "             // Blank
    })
    void testInvalidZoneCodes(String invalidCode) {
        assertFalse(ZoneCodeValidator.isValid(invalidCode),
                "Should reject invalid zone code: " + invalidCode);
        assertNotEquals("VALID", ZoneCodeValidator.getValidationReport(invalidCode));
    }

    @Test
    void testNullZoneCode() {
        assertFalse(ZoneCodeValidator.isValid(null));
        assertNotNull(ZoneCodeValidator.getValidationReport(null));
    }
}

