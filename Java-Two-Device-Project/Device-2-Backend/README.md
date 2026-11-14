# Device 2 — Backend Engine & Database Host

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Client–Server Architecture: Device 2 (Application & Data Layer)*

---

## 1. Role & Responsibilities

Device 2 is the **Backend REST API Engine and Dedicated Database Host** running on the second physical laptop.

- **Contains:** All Spring Boot Java 21 backend services, controllers, DAOs, multithreaded dispatch executors, and relational MySQL database storage.
- **Independence:** Runs completely independently on Laptop 2.
- **Network Listening:** Binds to `0.0.0.0:8080` (all network interfaces) so Device 1 can connect over Wi-Fi / LAN.
- **Database Management:** Hosts the MySQL 8 / MariaDB database exclusively. Database credentials are NEVER exposed to Device 1.
- **REST APIs:** Exposes RESTful JSON endpoints with global CORS enabled (`CorsConfig.java`) for cross-device requests.
- **Health Diagnostic Endpoint:** Provides `/api/health` for live connection testing, database ping, and latency measurement.

---

## 2. Folder Layout

```
Device-2-Backend/
├── src/
│   ├── main/
│   │   ├── java/com/disasterrelief/
│   │   │   ├── config/             # CorsConfig (LAN CORS) & OpenApiConfig
│   │   │   ├── controller/         # HealthController (/api/health) & 9 operational controllers
│   │   │   ├── dao/                # ReliefDAO (JDBC persistence & aggregate queries)
│   │   │   ├── dispatch/           # DispatchWorker, DispatchExecutor, ThreadPoolManager
│   │   │   ├── dto/                # Request and Response Data Transfer Objects
│   │   │   ├── exception/          # GlobalExceptionHandler and custom exceptions
│   │   │   ├── model/              # Domain models (DisasterZone, ReliefCargo, etc.)
│   │   │   ├── repository/         # Repository interfaces
│   │   │   ├── service/            # DDUS Scoring, Weather Safety, Fleet Allocation, Audit
│   │   │   ├── utility/            # DBConnection, ManifestWriter, CSVImporter, ZoneCodeValidator
│   │   │   └── MainApp.java        # Spring Boot application entry point
│   │   └── resources/
│   │       ├── application.properties # server.address=0.0.0.0, port 8080, DB connection
│   │       └── schema-h2.sql       # In-memory test schema
│   └── test/
│       └── java/com/disasterrelief/ # 47 automated JUnit 5 & Mockito test suites
├── database/
│   └── disaster_relief_schema.sql  # Full MySQL schema with constraints, indexes, sample data
├── sql/
│   ├── schema.sql                  # Clean DDL table creation script
│   └── seed_data.sql               # Seed data for demonstration
├── config/
│   └── .env.example                # Environment variables template for DB credentials
├── scripts/
│   ├── windows/
│   │   ├── find-ip.ps1             # Auto-detects IPv4 and prints Device 1 connection URL
│   │   ├── allow-firewall-port.ps1 # Opens inbound TCP port 8080 in Windows Firewall
│   │   ├── start-backend.ps1       # Checks MySQL and starts Spring Boot backend
│   │   └── run-tests.ps1           # Runs all 47 automated backend tests
│   └── linux/
│       ├── find-ip.sh              # Linux IP detector
│       ├── allow-firewall-port.sh  # Linux UFW firewall rule (8080/tcp)
│       └── start-backend.sh        # Linux startup script
├── mvnw / mvnw.cmd                 # Bundled Maven Wrapper (Zero install required!)
├── pom.xml                         # Maven build file (Java 21, Spring Boot 3.2.5)
└── README.md                       # This documentation file
```

---

## 3. Software Requirements on Device 2

- **Operating System:** Windows 11 / 10, Linux, or macOS.
- **Java:** JDK 17 or 21 (Verify: `java -version`).
- **Database:** MySQL 8 or MariaDB 10+ (Verify: port 3307 or 3306).
- **Maven:** Bundled Maven Wrapper (`mvnw.cmd` on Windows, `./mvnw` on Linux) — no external Maven download needed!

---

## 4. Database Setup on Device 2

1. Open MySQL Workbench or MySQL CLI.
2. Execute the schema script:
   ```sql
   SOURCE database/disaster_relief_schema.sql;
   ```
   Or run `sql/schema.sql` followed by `sql/seed_data.sql`.
3. Verify that the user credentials match `config/.env`:
   - Host: `localhost`
   - Port: `3307` (or `3306`)
   - User: `host` (or `root`)
   - Password: `1234`
   - Database: `disaster_relief_db`

---

## 5. Network Setup: Finding IP & Opening Firewall

Both laptops must be connected to the **same Wi-Fi router or Mobile Hotspot**.

### Step 1: Detect Device 2's IPv4 Address
In PowerShell inside `Device-2-Backend`, run:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\find-ip.ps1
```
Output example:
```
Adapter: [Wi-Fi]
IPv4 Address: 192.168.1.100
===> Backend URL for Device 1: http://192.168.1.100:8080
```

### Step 2: Open Inbound Firewall Port 8080 (One-Time Setup)
Run PowerShell **as Administrator** and execute:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\allow-firewall-port.ps1
```
*Note: On Linux, run `sudo ufw allow 8080/tcp`.*

---

## 6. How to Start the Backend on Device 2

In PowerShell inside `Device-2-Backend`, run:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\start-backend.ps1
```
Or start directly with Maven Wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```
Or start the pre-packaged executable JAR:
```powershell
java -jar target\disaster-relief-dispatch-1.0.0.jar
```

The console will display:
```
DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
Local Command Center: http://localhost:8080
External Network:     http://192.168.1.100:8080
API Documentation:    http://localhost:8080/swagger-ui.html
```

---

## 7. Verifying Backend Health

### Local Verification (on Device 2):
Open browser to:
`http://localhost:8080/api/health`

### Remote Verification (from Device 1):
Open browser on Device 1 to:
`http://<DEVICE-2-IP>:8080/api/health`

**Expected JSON Response:**
```json
{
  "status": "UP",
  "service": "Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)",
  "role": "Device 2 - Backend & Database Host",
  "databaseStatus": "CONNECTED",
  "databaseMessage": "Active (MySQL/MariaDB connection verified)",
  "serverIp": "192.168.1.100",
  "timestamp": "2026-11-14T10:35:00Z"
}
```

---

## 8. Running Automated Tests on Device 2

All 47 unit, algorithm, safety filter, multithreaded, and REST API tests can be executed independently on Device 2:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\run-tests.ps1
```
Result: **47 tests run, 0 failures, 0 errors, 0 skipped. BUILD SUCCESS.**
