# REST API Specification & Endpoint Documentation

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Backend Engine (Device 2) Exposing Cross-Device RESTful Endpoints*

---

## 1. API Architecture Standards

- **Base Protocol:** HTTP / 1.1 (or HTTP / 2)
- **Data Exchange Format:** `application/json; charset=UTF-8`
- **Text Reports Format:** `text/plain; charset=UTF-8`
- **CORS Support:** Full cross-origin wildcard support (`Access-Control-Allow-Origin: *`)
- **Interactive Documentation:** Available at `http://<DEVICE-2-IP>:8080/swagger-ui.html`

---

## 2. Diagnostics & Health Check Endpoints

### 2.1. System Health & Database Ping
- **Endpoint:** `GET /api/health`
- **Description:** Verifies two-device connectivity, active server IP, service role, and runs `SELECT 1` on MySQL to test database availability.
- **Response Headers:** `Content-Type: application/json`
- **Response Body (200 OK):**
```json
{
  "status": "UP",
  "service": "Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)",
  "role": "Device 2 - Backend & Database Host",
  "databaseStatus": "CONNECTED",
  "databaseMessage": "Active (MySQL/MariaDB connection verified)",
  "serverIp": "192.168.1.100",
  "serverHost": "LAPTOP-DEV2",
  "timestamp": "2026-11-14T10:35:00.123Z",
  "uptimeMillis": 34812
}
```

---

## 3. Operational Dashboard & Telemetry Endpoints

### 3.1. Dashboard Overview Statistics
- **Endpoint:** `GET /api/dashboard/statistics`
- **Description:** Returns real database-backed operational KPIs.
- **Response Body (200 OK):**
```json
{
  "totalDisasterZones": 7,
  "activeMissions": 1,
  "completedMissions": 2,
  "pendingRequests": 0,
  "groundedMissions": 1,
  "abortedMissions": 1,
  "availableTransportAssets": 5,
  "overallReliefCoveragePercentage": 86.4
}
```

### 3.2. Chart.js Visualization Datasets
- **Endpoint:** `GET /api/dashboard/charts`
- **Description:** Returns aggregated categorical distributions for Chart.js doughnut, bar, and pie charts.
- **Response Body (200 OK):**
```json
{
  "eventTypeDistribution": { "FLOOD": 4, "QUAKE": 3 },
  "transportCategoryDistribution": {
    "HEAVY_CARGO_HELICOPTER": 2,
    "ALL_TERRAIN_RESCUE_TRUCK": 2,
    "EMERGENCY_MEDICAL_DRONE": 2
  },
  "missionPriorityDistribution": {
    "PRIORITY_ALPHA": 2,
    "PRIORITY_BRAVO": 2,
    "MISSION_ABORTED": 1
  },
  "zoneCoverages": [
    { "zoneCode": "ZONE-TN-1003", "reliefCoverageRatio": 95.8 },
    { "zoneCode": "ZONE-KL-2001", "reliefCoverageRatio": 100.0 }
  ]
}
```

---

## 4. Phase 1: Disaster Event Intake Endpoints

### 4.1. List All Registered Zones
- **Endpoint:** `GET /api/disasters`
- **Response Body (200 OK):** Array of disaster zone objects.

### 4.2. Register New Disaster Zone
- **Endpoint:** `POST /api/disasters`
- **Request Body:**
```json
{
  "zoneCode": "ZONE-TN-1001",
  "zoneName": "Cuddalore Coastal Delta",
  "eventType": "FLOOD",
  "affectedPopulation": 45000,
  "medicalUrgencyScore": 9.5,
  "terrainRisk": 78.0,
  "roadAccessibility": 22.0,
  "weatherSeverityIndex": 5.8,
  "flightDistanceKm": 185.0
}
```
- **Validation Constraints:**
  - `zoneCode`: Must match regex `ZONE-[A-Z]{2}-\d{4}`
  - `medicalUrgencyScore`: `1.0 <= score <= 10.0`
  - `terrainRisk`, `roadAccessibility`: `0.0 <= val <= 100.0`
  - `weatherSeverityIndex`: `0.0 <= val <= 10.0`
- **Response (201 Created):** Stored disaster zone object with generated database ID.

---

## 5. Phase 2: Cargo Ingestion & Manifest Endpoints

### 5.1. Ingest Emergency Relief Cargo
- **Endpoint:** `POST /api/cargo`
- **Request Body:**
```json
{
  "disasterZoneId": 1,
  "foodKitCount": 500,
  "waterKitCount": 1000,
  "plasmaKitCount": 40,
  "totalPayloadKg": 2800.0,
  "requiredRations": 5000,
  "dispatchLocation": "Chennai Logistics Hub",
  "cargoManifest": "FOOD,500,WATER,1000,PLASMA,40"
}
```
- **Response (201 Created):** Stored cargo object with validated itemized kit breakdown.

---

## 6. Algorithmic Dispatch & Multithreading Endpoints

### 6.1. Execute Single Zone Dispatch
- **Endpoint:** `POST /api/dispatch/{zoneId}`
- **Description:** Runs DDUS Urgency Scoring, evaluates weather restrictions, recommends and allocates the highest-scoring available vehicle from the fleet.
- **Response Body (200 OK):**
```json
{
  "missionCode": "MSN-2026-0006",
  "zoneCode": "ZONE-TN-1001",
  "ddusScore": 86.4,
  "priorityLevel": "PRIORITY_BRAVO",
  "assignedAssetCode": "TRUCK-TATRA-01",
  "assignedAssetName": "Tatra 8x8 Heavy Tactical Rescue Truck",
  "transportCategory": "ALL_TERRAIN_RESCUE_TRUCK",
  "transportSuitabilityScore": 84.0,
  "weatherValidationPassed": true,
  "status": "DISPATCHED",
  "executionMessage": "Dispatched safely via All-Terrain Rescue Truck"
}
```

### 6.2. Bulk Concurrent Multithreaded Dispatch
- **Endpoint:** `POST /api/dispatch/bulk`
- **Description:** Concurrently schedules multiple zones using `ThreadPoolManager` and `DispatchWorker`.
- **Request Body:** Array of Zone IDs, e.g. `[1, 2, 3, 4, 5]`
- **Response (200 OK):** Array of `DispatchResultDto` objects processed in parallel threads.

---

## 7. Phase 3: Field Delivery Audit Endpoints

### 7.1. Submit Field Delivery Confirmation
- **Endpoint:** `POST /api/missions/{missionId}/audit`
- **Request Body:**
```json
{
  "flightTransitDurationMin": 72,
  "dropOffPrecisionMeters": 4.5,
  "fieldOfficerName": "Commander Rajesh Kumar, NDRF",
  "deliveredRations": 11500,
  "deliveryNotes": "Payload dropped safely at staging ground. No casualties.",
  "deliveryVerified": true
}
```
- **Response (200 OK):** Confirmed mission object updated to `COMPLETED` status.

---

## 8. Export & Report Endpoints

| Endpoint | Method | Format | Description |
| :--- | :--- | :--- | :--- |
| `/api/reports/manifests` | `GET` | `text/plain` | Formatted flight manifests report generated via Java NIO. |
| `/api/reports/supply-drop-logs` | `GET` | `text/plain` | Itemized supply drop audit log with precision GPS coordinates. |
| `/api/reports/summary` | `GET` | `text/plain` | Executive disaster operations summary and coverage ratio. |
| `/api/reports/dispatch-logs` | `GET` | `application/json` | Real-time thread pool execution logs (`DispatchWorker-X`). |
| `/api/import/csv` | `POST` | `application/json` | Ingests `data/relief_requests.csv` with regex validation. |
