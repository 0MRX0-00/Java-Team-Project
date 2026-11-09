# SYSTEM ARCHITECTURE SPECIFICATION
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. HIGH-LEVEL ARCHITECTURAL OVERVIEW

DREMLDS is engineered with an enterprise layered service-oriented architecture (SOA) emphasizing **separation of concerns**, **transactional integrity**, and **thread-safe concurrent dispatch execution**.

```
+-------------------------------------------------------------------------------+
|                            PRESENTATION LAYER                                 |
|  - Enterprise Dark Navy Command Center SPA (HTML5 / Bootstrap 5 / Chart.js)   |
|  - OpenAPI 3 / Swagger UI Interface (/swagger-ui.html)                        |
+---------------------------------------+---------------------------------------+
                                        | JSON REST APIs (HTTP / HTTPS)
                                        v
+-------------------------------------------------------------------------------+
|                            CONTROLLER LAYER                                   |
|  - DashboardController      - DisasterController      - CargoController       |
|  - DispatchController       - MissionController       - AssetController       |
|  - AuditController          - ReportController        - ImportController      |
|  - GlobalExceptionHandler   (Standardized HTTP error envelopes)               |
+---------------------------------------+---------------------------------------+
                                        | DTOs & Domain Commands
                                        v
+-------------------------------------------------------------------------------+
|                              SERVICE LAYER                                    |
|  - UrgencyCalculationService (DDUS score calculation & priority bands)        |
|  - TransportScoringService   (Three-category suitability normalization)       |
|  - WeatherValidationService  (Ceilings & ground rescue truck reassignment)    |
|  - AssetAllocationService    (Atomic reservation & contention avoidance)      |
|  - CoverageService           (Fulfillment ratio & division-by-zero safety)    |
|  - DisasterService           - CargoService           - MissionService        |
|  - DeliveryAuditService      - ReportService                                  |
+---------------------------------------+---------------------------------------+
                                        |
                 +----------------------+----------------------+
                 |                                             |
                 v                                             v
+----------------------------------+       +------------------------------------+
|     MULTITHREADING ENGINE        |       |        DATA ACCESS LAYER           |
|  - ThreadPoolManager (FixedPool) |       |  - ReliefDAO (PreparedStatement)   |
|  - DispatchWorker (Callable)     |       |  - DisasterZoneRepository          |
|  - DispatchExecutor              |       |  - CargoRepository                 |
|  - ConcurrentHashMap Tracking    |       |  - DispatchMissionRepository       |
|  - AtomicInteger Metrics         |       |  - FieldConfirmationRepository     |
+----------------------------------+       |  - TransportAssetRepository        |
                                           +-----------------+------------------+
                                                             | JDBC Connections
                                                             v
+-------------------------------------------------------------------------------+
|                             PERSISTENCE LAYER                                 |
|  - MySQL 8 / MariaDB (HikariCP Connection Pool: DREMLDS-HikariPool)           |
|  - Embedded H2 (MySQL Mode) for Zero-Config Automated Surefire Test Runs      |
+-------------------------------------------------------------------------------+
```

---

### 2. CORE SUBSYSTEMS

#### A. Multithreaded Dispatch Engine
The concurrency system employs a **FixedThreadPool** of 8 background workers (configurable via `DISPATCH_THREAD_POOL_SIZE`):
1. **Thread Safety:** Atomic status updates (`UPDATE transport_assets SET status = 'OCCUPIED' WHERE id = ? AND status = 'AVAILABLE'`) eliminate race conditions and double-dispatch.
2. **Lifecycle Steps:** Each `DispatchWorker` executes a 12-step validation, scoring, safety check, and logging pipeline.
3. **Structured Audit Logs:** All actions are persisted to `dispatch_logs` with worker thread names, request IDs, and ISO-8601 timestamps.

#### B. Phased Disaster Entry Pipeline
- **Phase 1: Disaster Event Intake:** Validates zone code using `ZoneCodeValidator` regex (`ZONE-[A-Z]{2}-\d{4}`), population, and severity parameters.
- **Phase 2: Cargo Ingestion:** Validates itemized manifest key-value strings (`FOOD,100,WATER,200,PLASMA,20`) via Java string operations.
- **Phase 3: Delivery Audit:** Records on-ground field confirmation, drop-off precision in meters, marks missions `COMPLETED`, and releases transport assets back to `AVAILABLE`.

#### C. Cloud Deployment Topology
- **OCI Virtual Cloud Network (VCN):** Dual-subnet architecture separating the public Application Server (VM 1, port 8080) from the private Database Server (VM 2, port 3306).

