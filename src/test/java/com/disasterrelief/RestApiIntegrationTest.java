package com.disasterrelief;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("REST API Controller Integration Tests")
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/dashboard/statistics returns real database telemetry")
    void testGetDashboardStatistics() throws Exception {
        mockMvc.perform(get("/api/dashboard/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalDisasterZones", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.overallReliefCoveragePercentage", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("GET /api/disasters returns list of registered zones")
    void testGetAllDisasters() throws Exception {
        mockMvc.perform(get("/api/disasters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].zoneCode", startsWith("ZONE-")));
    }

    @Test
    @DisplayName("POST /api/disasters registers valid disaster zone")
    void testRegisterDisasterZone() throws Exception {
        String jsonPayload = """
                {
                    "zoneCode": "ZONE-TN-9999",
                    "zoneName": "Test Coastal Sector",
                    "eventType": "FLOOD",
                    "affectedPopulation": 32000,
                    "medicalUrgencyScore": 8.9,
                    "terrainRisk": 45.0,
                    "roadAccessibility": 30.0,
                    "weatherSeverityIndex": 4.5,
                    "flightDistanceKm": 150.0
                }
                """;

        mockMvc.perform(post("/api/disasters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.zoneCode", is("ZONE-TN-9999")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/disasters rejects invalid zone code regex")
    void testRegisterDisasterZoneInvalidRegex() throws Exception {
        String invalidPayload = """
                {
                    "zoneCode": "INVALID-1234",
                    "zoneName": "Bad Sector",
                    "eventType": "FLOOD",
                    "affectedPopulation": 10000,
                    "medicalUrgencyScore": 5.0,
                    "terrainRisk": 50.0,
                    "roadAccessibility": 50.0,
                    "weatherSeverityIndex": 5.0,
                    "flightDistanceKm": 100.0
                }
                """;

        mockMvc.perform(post("/api/disasters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reports/manifests returns exported text")
    void testGetManifestsReport() throws Exception {
        mockMvc.perform(get("/api/reports/manifests"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("OFFICIAL EMERGENCY FLIGHT & DISPATCH MANIFEST REPORT")));
    }

    @Test
    @DisplayName("GET /api/health returns UP and database status for two-device verification")
    void testGetHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.databaseStatus", is("CONNECTED")))
                .andExpect(jsonPath("$.role", containsString("Device 2")));
    }
}

