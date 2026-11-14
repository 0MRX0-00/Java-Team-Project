package com.disasterrelief;

import com.disasterrelief.service.CoverageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Relief Coverage Ratio Calculation Tests")
class ReliefCoverageTest {

    private final CoverageService coverageService = new CoverageService(null, null, null);

    @Test
    @DisplayName("Should accurately calculate relief coverage percentage")
    void testStandardCoverageCalculation() {
        // Delivered: 4,000, Required: 5,000 -> 80.0%
        double ratio = coverageService.computeRatio(4000, 5000);
        assertEquals(80.0, ratio, 0.01);

        // Delivered: 11,500, Required: 12,000 -> 95.83%
        double ratio2 = coverageService.computeRatio(11500, 12000);
        assertEquals(95.83, ratio2, 0.01);
    }

    @Test
    @DisplayName("Should handle division by zero safely when required rations is zero")
    void testZeroDivisionHandling() {
        double ratioZero = coverageService.computeRatio(0, 0);
        assertEquals(0.0, ratioZero, 0.01);

        double ratioNonZeroDelivered = coverageService.computeRatio(500, 0);
        assertEquals(100.0, ratioNonZeroDelivered, 0.01);
    }

    @Test
    @DisplayName("Should clamp coverage to maximum 100.0%")
    void testOverdeliveryClamping() {
        double ratioOver = coverageService.computeRatio(6000, 5000);
        assertEquals(100.0, ratioOver, 0.01);
    }
}

