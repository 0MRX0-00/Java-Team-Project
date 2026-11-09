# TESTING & VERIFICATION GUIDE
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. RUNNING AUTOMATED TESTS ON WINDOWS 11

The test suite runs automatically via the bundled Maven Wrapper:

```powershell
# Run from PowerShell in project root:
.\mvnw.cmd test

# OR use the dedicated PowerShell test script:
.\scripts\windows\run-tests.ps1
```

---

### 2. TEST SUITE ARCHITECTURE

| Test Class | Focus & Coverage |
| :--- | :--- |
| **`ZoneCodeValidatorTest`** | Enforces assignment regex `ZONE-[A-Z]{2}-\d{4}` via Java Pattern and Matcher. Tests valid and invalid zone codes. |
| **`CargoManifestProcessingTest`** | Tests String `split(",")`, `trim()`, `format()`. Tests key-value pair parsing, duplicate key detection, negative quantities, table generation. |
| **`UrgencyAndPriorityTest`** | Verifies DDUS formula, configurable weights ($W_u=35, W_p=35, W_t=30$), clamping $[0.0, 100.0]$, and priority classification (Alpha/Bravo/Charlie/Aborted). |
| **`TransportScoringAndSafetyTest`** | Tests 3 transport categories (Helicopter 40/40/20, Truck 50/30/20, Drone 60/30/10), weather ceilings (Heli 7.0, Truck 8.5, Drone 5.0), and ground truck reassignment. |
| **`ReliefCoverageTest`** | Tests `(DeliveredRations / TotalRequiredRations) * 100` and division-by-zero prevention. |
| **`CSVImportAndExportTest`** | Tests CSV header validation, row parsing, syntax errors rejection, and `ManifestWriter` TXT generation. |
| **`MultithreadedDispatchTest`** | Concurrently fires 6 simultaneous worker threads using `CountDownLatch` and `FixedThreadPool`. Verifies that no asset can be double-allocated. |
| **`RestApiIntegrationTest`** | MockMvc integration tests for REST endpoints, HTTP status codes, and JSON serialization. |

---

### 3. VERIFICATION METRICS

- **Database Independence:** Automated test profiles leverage in-memory H2 running in MySQL mode (`application-test.properties` and `schema-h2.sql`), ensuring zero-dependency, 100% deterministic test execution on any machine.
- **Reports Location:** Test execution results and Surefire XML reports are saved to `target/surefire-reports/`.

