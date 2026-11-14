# Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

## Two-Device Physical Client–Server Architecture

This project is configured for deployment and execution across **two separate physical laptops** communicating over a **shared local area network (Wi-Fi or Mobile Hotspot)** without any dependence on cloud services (Oracle Cloud, AWS, Azure, Firebase) or the public internet.

---

## Directory Structure Overview

```
Java-Two-Device-Project/
│
├── Device-1-Frontend/          # ===> COPY THIS ENTIRE FOLDER TO LAPTOP 1 (FRONTEND)
│   ├── src/                    # HTML5, CSS3, JavaScript ES6+ application code
│   │   ├── index.html          # Main Command Center UI Single Page Application
│   │   └── js/
│   │       ├── connection.js   # Dynamic IP manager, live health monitor, disconnect alert
│   │       └── app.js          # REST API event handlers, Chart.js rendering, forms
│   ├── assets/                 # 100% Offline bundled stylesheets, icons, and Chart.js
│   │   ├── css/                # bootstrap.min.css, all.min.css (FontAwesome), style.css
│   │   └── js/                 # bootstrap.bundle.min.js, chart.umd.min.js
│   ├── config/                 # config.js (configurable Device 2 backend IPv4 address)
│   ├── start-frontend.ps1      # Native Windows 11 PowerShell static server (Zero-install!)
│   ├── start-frontend.sh       # Linux/macOS static web server
│   ├── server.js               # Node.js static server (optional)
│   └── README.md               # Device 1 setup & execution instructions
│
├── Device-2-Backend/           # ===> COPY THIS ENTIRE FOLDER TO LAPTOP 2 (BACKEND + DB)
│   ├── src/                    # Spring Boot 3.2.5 & Java 21 REST API source code
│   │   ├── main/java/          # MainApp, Controllers, Services, DAOs, ThreadPool
│   │   │   └── com/disasterrelief/
│   │   │       ├── config/     # CorsConfig (LAN CORS enabled) & OpenApiConfig
│   │   │       ├── controller/ # HealthController (/api/health) & 9 API controllers
│   │   │       ├── dao/        # ReliefDAO (JDBC database persistence)
│   │   │       ├── dispatch/   # DispatchWorker, DispatchExecutor, ThreadPoolManager
│   │   │       ├── utility/    # DBConnection, ManifestWriter, CSVImporter, ZoneCodeValidator
│   │   │       └── MainApp.java# Application main entry point
│   │   └── resources/          # application.properties (0.0.0.0:8080) & schema-h2.sql
│   ├── database/               # Complete disaster_relief_schema.sql (MySQL 8)
│   ├── sql/                    # schema.sql & seed_data.sql for easy initialization
│   ├── config/                 # .env.example (template for MySQL credentials)
│   ├── scripts/
│   │   ├── windows/            # find-ip.ps1, allow-firewall-port.ps1, start-backend.ps1, run-tests.ps1
│   │   └── linux/              # find-ip.sh, allow-firewall-port.sh, start-backend.sh
│   ├── mvnw / mvnw.cmd         # Bundled Maven Wrapper (Zero install required!)
│   ├── pom.xml                 # Maven build file (Java 21, Spring Boot, MySQL Connector)
│   └── README.md               # Device 2 setup & execution instructions
│
├── Documentation/              # ===> COMPREHENSIVE ARCHITECTURE & DEMO GUIDES
│   ├── ARCHITECTURE.md         # Detailed Client-Server topology, sequence diagrams, separation of concerns
│   ├── NETWORK_SETUP.md        # Step-by-step Wi-Fi setup, IP discovery, Windows Defender firewall rules
│   ├── API_DOCUMENTATION.md    # Complete REST API reference, request/response schemas, error codes
│   ├── TESTING_GUIDE.md        # 8-step test procedures (CRUD, DB persistence, disconnect test, offline test)
│   └── PRESENTATION_GUIDE.md   # Rehearsed 7-minute demonstration script for your professor
│
└── README.md                   # This master documentation file
```

---

## Quick-Start: 3 Simple Steps to Run on Two Laptops

### Step 1: Connect Both Laptops to the Same Wi-Fi
- Connect Laptop 1 and Laptop 2 to the same Wi-Fi router or Mobile Hotspot.
- *(Works 100% offline — you can turn cellular mobile data OFF).*

---

### Step 2: Start Backend & Database on Laptop 2
1. Copy the `Device-2-Backend` folder to Laptop 2.
2. Start MySQL 8 / MariaDB (port 3307 or 3306) and ensure `disaster_relief_db` is imported (`database/disaster_relief_schema.sql`).
3. Open PowerShell as Administrator inside `Device-2-Backend` and run:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\windows\allow-firewall-port.ps1
   ```
4. Find Laptop 2's IPv4 address:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\windows\find-ip.ps1
   ```
   *Note the URL printed, e.g.: `http://192.168.1.100:8080`*.
5. Start the backend:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\windows\start-backend.ps1
   ```

---

### Step 3: Start Frontend on Laptop 1
1. Copy the `Device-1-Frontend` folder to Laptop 1.
2. Open PowerShell inside `Device-1-Frontend` and run:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\start-frontend.ps1
   ```
3. Your browser will open to `http://localhost:3000/src/index.html`.
4. In the top-right header, click the **Gear Icon** or the **Device 2 Status Badge**, enter Laptop 2's URL (e.g. `http://192.168.1.100:8080`), and click **OK**.
5. The badge will turn **Green** (`● Device 2: Connected`), displaying the live latency in milliseconds.
6. **You're ready!** All data entry, dispatching, chart rendering, and field audits will now communicate across laptops in real time.

---

## Documentation Links

- [System Architecture Specification](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/ARCHITECTURE.md)
- [Local Network & Firewall Setup Guide](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/NETWORK_SETUP.md)
- [Complete REST API Reference](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/API_DOCUMENTATION.md)
- [8-Step Testing & Verification Matrix](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/TESTING_GUIDE.md)
- [Professor Presentation & Demo Script](file:///g:/Java%20Team%20Project/Java-Two-Device-Project/Documentation/PRESENTATION_GUIDE.md)

