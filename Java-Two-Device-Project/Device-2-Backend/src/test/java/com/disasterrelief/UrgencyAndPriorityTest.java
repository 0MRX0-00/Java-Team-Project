package com.disasterrelief;

import com.disasterrelief.service.UrgencyCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DDUS Calculation & Priority Classification Tests")
class UrgencyAndPriorityTest {

    private UrgencyCalculationService urgencyService;

    @BeforeEach
    void setUp() {
        // Defaults: Wu = 35.0, Wp = 35.0, Wt = 30.0
        urgencyService = new UrgencyCalculationService(35.0, 35.0, 30.0);
    }

    @Test
    @DisplayName("Should evaluate Critical Disaster as Priority Alpha (90–100)")
    void testPriorityAlphaClassification() {
        // Max urgency (10), high population (50,000), low terrain risk (0)
        double score = urgencyService.calculateDDUS(10.0, 50000, 0.0);
        assertTrue(score >= 90.0, "Score should be >= 90.0, was: " + score);
        assertEquals("PRIORITY_ALPHA", urgencyService.determinePriorityLevel(score));

        // High values: Urgency 9.8, Pop 60000, Terrain 20
        double score2 = urgencyService.calculateDDUS(9.8, 60000, 20.0);
        assertTrue(score2 >= 90.0);
        assertEquals("PRIORITY_ALPHA", urgencyService.determinePriorityLevel(score2));
    }

    @Test
    @DisplayName("Should evaluate High Disaster as Priority Bravo (75–89)")
    void testPriorityBravoClassification() {
        // Urgency 9.0, Pop 45000, Terrain 40 -> 31.5 + 31.5 + 18.0 = 81.0
        double score = urgencyService.calculateDDUS(9.0, 45000, 40.0);
        assertTrue(score >= 75.0 && score < 90.0, "Expected Bravo score [75-89], got: " + score);
        assertEquals("PRIORITY_BRAVO", urgencyService.determinePriorityLevel(score));
    }

    @Test
    @DisplayName("Should evaluate Moderate Disaster as Priority Charlie (60–74)")
    void testPriorityCharlieClassification() {
        // Urgency 8.0, Pop 30000, Terrain 40 -> 28.0 + 21.0 + 18.0 = 67.0
        double score = urgencyService.calculateDDUS(8.0, 30000, 40.0);
        assertTrue(score >= 60.0 && score < 75.0, "Expected Charlie score [60-74], got: " + score);
        assertEquals("PRIORITY_CHARLIE", urgencyService.determinePriorityLevel(score));
    }

    @Test
    @DisplayName("Should evaluate Low Disaster below 60 as Mission Aborted")
    void testMissionAbortedClassification() {
        // Low urgency (2.0), low population (2,000), high terrain risk (80)
        double score = urgencyService.calculateDDUS(2.0, 2000, 80.0);
        assertTrue(score < 60.0, "Expected Aborted score (< 60.0), got: " + score);
        assertEquals("MISSION_ABORTED", urgencyService.determinePriorityLevel(score));
    }

    @Test
    @DisplayName("Should clamp score within 0.0 to 100.0 boundary")
    void testBoundaryClamping() {
        double maxScore = urgencyService.calculateDDUS(10.0, 200000, 0.0);
        assertEquals(100.0, maxScore, 0.1);

        double minScore = urgencyService.calculateDDUS(0.0, 0, 100.0);
        assertEquals(0.0, minScore, 0.1);
    }
}

