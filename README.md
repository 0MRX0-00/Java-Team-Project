# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)

[![Build & Tests](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-JDK%2017%20%7C%2021-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)]()
[![OS](https://img.shields.io/badge/OS-Windows%2011%20%7C%20Ubuntu%20LTS-blue.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Two--Device%20LAN%20Client--Server-success.svg)]()

> [!IMPORTANT]
> ### 🌐 TWO-DEVICE PHYSICAL CLIENT–SERVER ARCHITECTURE (LOCAL WI-FI / LAN)
> For running this application across **two separate physical laptops** (Laptop 1 = Frontend UI, Laptop 2 = Spring Boot Backend & MySQL Database) connected over the same local Wi-Fi or mobile hotspot with **zero cloud dependencies and 100% offline capability**, navigate to:
> - **[Java-Two-Device-Project Directory](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/README.md)**
> - **[Device 1 (Frontend Laptop Instructions)](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Device-1-Frontend/README.md)**
> - **[Device 2 (Backend Laptop Instructions)](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Device-2-Backend/README.md)**
> - **[Complete Architecture & Presentation Documentation](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/ARCHITECTURE.md)**

---

## 1. PROJECT OVERVIEW

The **Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)** is an enterprise emergency operations command platform designed for disaster zone intake, relief cargo ingestion, algorithmic urgency dispatching, transport vehicle matching, multithreaded concurrent mission processing, and field delivery auditing.

Engineered for **Windows 11** local development with automated deployment to **two separate Oracle Cloud Infrastructure (OCI) Ubuntu compute instances**.

### Key Capabilities:
1. **Disaster Registration:** Multi-stage intake with regex enforcement (`ZONE-[A-Z]{2}-\d{4}`).
2. **Emergency Cargo Management:** Itemized manifests parsed via Java string operations (`FOOD,100,WATER,200,PLASMA,20`).
3. **Disaster Dispatch Urgency Score (DDUS):** Quantitative prioritization based on configurable medical, demographic, and terrain weights.
4. **Transport Scoring Engine:** Independent suitability scoring for Heavy Cargo Helicopters (40/40/20), All-Terrain Rescue Trucks (50/30/20), and Emergency Medical Drones (60/30/10).
5. **Weather & Safety Filter:** Ceilings (Heli &le; 7.0, Truck &le; 8.5, Drone &le; 5.0) with automated ground rescue truck reassignment for grounded aircraft.
6. **Multithreaded Mission Processing:** `ThreadPoolManager` FixedThreadPool (5+ workers) with atomic asset reservation preventing double-allocation and race conditions.
7. **Real-Time Mission Tracking:** Real database-backed mission tracking and status telemetry.
8. **Field Delivery Confirmation:** Audited on-ground verification, drop precision measurement, and asset deallocation.
9. **Relief Coverage Ratio:** Dynamic zone and overall coverage tracking $( \frac{\text{Delivered}}{\text{Required}} \times 100 )$.
10. **Reports & Exports:** Java NIO `BufferedWriter` export of flight manifests, supply drop logs, and summary reports.

---

## 2. TECHNOLOGY STACK

- **Operating System:** Windows 11 (Development & Testing), Ubuntu 22.04/24.04 LTS (OCI Production)
- **Backend Framework:** Java JDK 17/21, Spring Boot 3.2.5 (Spring Web, Spring JDBC, Spring Validation, Spring Boot Actuator)
- **Database:** MySQL 8 / MariaDB (HikariCP connection pooling), In-memory H2 (MySQL Mode) for zero-dependency automated testing
- **Frontend:** HTML5, CSS3, JavaScript ES6+, Bootstrap 5, FontAwesome, Chart.js (Dark Navy Command Center SPA)
- **API Documentation:** OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
- **Testing:** JUnit 5, Mockito, Spring Boot Test, MockMvc
- **Cloud Infrastructure:** Oracle Cloud Infrastructure (OCI) Virtual Cloud Network (VCN), dual Ubuntu VMs, UFW firewalls

---

## 3. EXACT ASSIGNMENT ARTIFACTS INCLUDED

| Required Filename | Location in Project | Purpose |
| :--- | :--- | :--- |
| **`DBConnection.java`** | `src/main/java/com/disasterrelief/utility/DBConnection.java` | Pooled DataSource & environment database configuration |
| **`ReliefDAO.java`** | `src/main/java/com/disasterrelief/dao/ReliefDAO.java` | Core JDBC Data Access Object with parameterized SQL |
| **`DispatchWorker.java`** | `src/main/java/com/disasterrelief/dispatch/DispatchWorker.java` | 12-step concurrent dispatch worker (`Callable` / `Runnable`) |
| **`ManifestWriter.java`** | `src/main/java/com/disasterrelief/utility/ManifestWriter.java` | Formatted TXT manifest exports via Java NIO |
| **`MainApp.java`** | `src/main/java/com/disasterrelief/MainApp.java` | Primary Spring Boot entry point |

---

## 4. WINDOWS 11 QUICK START GUIDE

### Prerequisites:
- Windows 11
- Java JDK 17 or 21 on `PATH`
- PowerShell

### Step 1: Clone and Setup Workspace
```powershell
# Open Windows PowerShell in the project directory:
cd "g:\Java Team Project"

# Run the automated Windows setup script:
.\scripts\windows\setup.ps1
```

### Step 2: Build the Application
```powershell
# Compiles, runs checks, and creates the JAR using the Maven Wrapper:
.\mvnw.cmd clean package

# Or using the PowerShell script:
.\scripts\windows\build.ps1
```

### Step 3: Run Automated Tests
```powershell
# Executes full JUnit 5 and Mockito test suite:
.\mvnw.cmd test

# Or using the PowerShell script:
.\scripts\windows\run-tests.ps1
```

### Step 4: Start the Application
```powershell
# Launches Spring Boot application on port 8080:
.\scripts\windows\start-app.ps1
```

### Step 5: Access the Command Center
- **Emergency Operations Web Console:** [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger REST API:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Actuator Health Telemetry:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 5. POWERSHELL SCRIPTS REFERENCE

All development tasks are automated with native Windows 11 PowerShell scripts in `scripts/windows/`:

- `check-environment.ps1`: Diagnoses Java, Maven wrapper, database port, and environment variables.
- `setup.ps1`: Prepares runtime directories (`exports`, `logs`, `data`) and configuration.
- `build.ps1`: Packages the JAR artifact (`target/disaster-relief-dispatch-1.0.0.jar`).
- `run-tests.ps1`: Runs all automated unit, integration, and multithreading tests.
- `start-app.ps1`: Starts the Spring Boot server in interactive or background mode.
- `stop-app.ps1`: Safely halts the running server using its recorded PID.
- `import-csv.ps1`: Ingests `data/relief_requests.csv` via the REST API.
- `deploy-to-oci.ps1`: Transfers the JAR and scripts to OCI VM 1 via Windows OpenSSH and SCP.
- `upload-sql.ps1`: Transfers and executes the database schema on OCI VM 2.
- `ssh-app-server.ps1`: One-click SSH connection to the OCI Application Server.
- `ssh-db-server.ps1`: One-click SSH connection to the OCI Database Server.

---

## 6. ORACLE CLOUD INFRASTRUCTURE (OCI) TWO-VM DEPLOYMENT

The system deploys across two Ubuntu compute instances inside an OCI Virtual Cloud Network (VCN):

```
[Windows 11 Admin PC]
       │
       ├── SSH (22) / HTTP (8080)
       ▼
[OCI VM 1: App Server] (Ubuntu 22.04 LTS, Java 21, Port 8080)
       │
       ├── MySQL TCP (3306) (Private Subnet Internal Route Only)
       ▼
[OCI VM 2: DB Server]  (Ubuntu 22.04 LTS, MySQL 8, Port 3306)
```

For complete instructions, refer to:
- [OCI Deployment Guide](file:///g:/Java%20Team%20Project/deployment/OCI_DEPLOYMENT_GUIDE.md)
- [Network Verification Guide](file:///g:/Java%20Team%20Project/deployment/NETWORK_VERIFICATION.md)

---

## 7. DOCUMENTATION CATALOG

- [System Architecture Specification](file:///g:/Java%20Team%20Project/docs/SYSTEM_ARCHITECTURE.md)
- [Database Schema & ER Design](file:///g:/Java%20Team%20Project/docs/DATABASE_DESIGN.md)
- [Algorithmic & Mathematical Specification](file:///g:/Java%20Team%20Project/docs/DISPATCH_ALGORITHM.md)
- [REST API Endpoint Catalog](file:///g:/Java%20Team%20Project/docs/API_DOCUMENTATION.md)
- [Testing & Verification Guide](file:///g:/Java%20Team%20Project/docs/TESTING_GUIDE.md)
- [Fictional Demonstration Scenarios](file:///g:/Java%20Team%20Project/docs/PROJECT_DEMONSTRATION.md)

---

## 8. ACADEMIC SUBMISSION CHECKLIST

- [x] Exact required filenames present (`DBConnection.java`, `ReliefDAO.java`, `DispatchWorker.java`, `ManifestWriter.java`, `MainApp.java`).
- [x] Java JDK 17/21 compatibility verified.
- [x] Maven Wrapper (`mvnw.cmd`) operational from Windows 11 PowerShell.
- [x] Zone code regex validation (`ZONE-[A-Z]{2}-\d{4}`) strictly enforced.
- [x] Cargo manifest parsed using `split(",")`, `trim()`, and `format()`.
- [x] DDUS calculated with configurable weights ($W_u=35, W_p=35, W_t=30$).
- [x] Priority classification bands enforced (Alpha, Bravo, Charlie, Aborted).
- [x] 3-category transport scoring engine implemented with feasibility checks.
- [x] Weather ceilings enforced (Heli &le; 7.0, Truck &le; 8.5, Drone &le; 5.0) with ground rescue truck reassignment.
- [x] Multithreaded dispatch engine prevents duplicate asset allocation.
- [x] Relief coverage ratio calculated with division-by-zero safety.
- [x] Real database-backed statistics and Chart.js dashboards.
- [x] CSV batch ingestion and formatted TXT file exports.
- [x] Windows 11 PowerShell scripts and OCI Ubuntu deployment scripts prepared.
- [x] Automated JUnit 5 and Mockito test suite passing.

