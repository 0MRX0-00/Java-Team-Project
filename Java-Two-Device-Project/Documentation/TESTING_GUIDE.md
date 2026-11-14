# Comprehensive Two-Device Testing & Verification Guide

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Validation Procedures for Physical Client–Server Separation*

---

## Testing Matrix Overview

| Test ID | Objective | Primary Device | Expected Result | Pass Criteria |
| :--- | :--- | :--- | :--- | :--- |
| **TEST-01** | Start Backend Independently | Device 2 | Binds to `0.0.0.0:8080`, MySQL active | Console displays banner & external URL |
| **TEST-02** | Verify Backend Health Endpoint | Device 2 & 1 | `GET /api/health` returns HTTP 200 | `status: "UP"`, `databaseStatus: "CONNECTED"` |
| **TEST-03** | Start Frontend Independently | Device 1 | Serves UI on port 3000 | Browser opens dark command center UI |
| **TEST-04** | Verify Cross-Device Connection | Device 1 | Device 1 pings Device 2 over Wi-Fi | Green badge: `Device 2: Connected (<20 ms)` |
| **TEST-05** | Test End-to-End CRUD Operations | Device 1 -> 2 | Register Zone -> Ingest Cargo -> Dispatch -> Audit | Records saved in MySQL on Device 2 |
| **TEST-06** | Test Database Connectivity & Persistence | Device 2 | Query MySQL directly via Workbench | Newly inserted records visible on Device 2 |
| **TEST-07** | Test Backend Disconnection Behavior | Both | Stop backend on Device 2 | Device 1 turns red, shows disconnect banner |
| **TEST-08** | Verify Offline LAN Operation (No Internet) | Both | Turn off internet on Wi-Fi router / hotspot | Full UI, charts, and REST APIs work offline |

---

## Detailed Step-by-Step Test Procedures

### TEST-01: Start Backend Independently on Device 2
1. On **Laptop 2** (Backend), open PowerShell inside `Device-2-Backend`.
2. Ensure MySQL 8 / MariaDB is active on port 3307 or 3306.
3. Run the startup script:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\windows\start-backend.ps1
   ```
4. **Verification:**
   - Console logs `Listening on 0.0.0.0:8080`.
   - Displays detected IPv4 address: `http://192.168.1.100:8080`.
   - Shows database connection pool initialized (`ReliefHikariPool - Start completed`).

---

### TEST-02: Verify Backend Health Endpoint (`/api/health`)
1. On **Device 2**, open browser or curl:
   ```text
   http://localhost:8080/api/health
   ```
2. On **Device 1**, open browser:
   ```text
   http://192.168.1.100:8080/api/health
   ```
3. **Verification:**
   Both requests return HTTP 200 with JSON payload:
   ```json
   {
     "status": "UP",
     "service": "Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)",
     "role": "Device 2 - Backend & Database Host",
     "databaseStatus": "CONNECTED",
     "databaseMessage": "Active (MySQL/MariaDB connection verified)",
     "serverIp": "192.168.1.100"
   }
   ```

---

### TEST-03: Start Frontend Independently on Device 1
1. On **Laptop 1** (Frontend), open PowerShell inside `Device-1-Frontend`.
2. Run:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\start-frontend.ps1
   ```
3. **Verification:**
   - A local lightweight server starts on `http://localhost:3000/`.
   - Default browser automatically opens `http://localhost:3000/src/index.html`.
   - The dark navy Command Center loads cleanly.

---

### TEST-04: Cross-Device Link Establishment
1. Look at the top-right header on Device 1.
2. If the URL does not match Device 2's IP:
   - Click the **Gear Icon** or the status badge.
   - Enter `http://192.168.1.100:8080`.
   - Click **OK**.
3. **Verification:**
   - The status badge changes from Amber to **Green**: `● Device 2: Connected`.
   - Latency displays in milliseconds (e.g. `14 ms`).
   - The dashboard automatically fetches real statistics and charts from Device 2.

---

### TEST-05: Test End-to-End CRUD Operations (Phase 1, 2, 3)

#### Part A: Phase 1 Intake
1. On Device 1, click **Phase 1: Intake** in the sidebar.
2. Enter:
   - Zone Code: `ZONE-KA-9001`
   - Zone Name: `Chikkamagaluru Valley Flood Sector`
   - Event Type: `FLOOD`
   - Affected Population: `35000`
   - Medical Urgency: `9.2`
   - Terrain Risk: `65.0`
   - Road Accessibility: `25.0`
   - Weather Severity: `4.5`
   - Flight Distance: `180` km
3. Click **Register Disaster Zone**.
4. **Verification:** A green alert toast confirms registration. The table displays `ZONE-KA-9001`.

#### Part B: Phase 2 Cargo Ingestion
1. Click **Phase 2: Cargo** in the sidebar.
2. Select Zone: `ZONE-KA-9001`.
3. Enter Kits: Food = `400`, Water = `800`, Plasma = `30`.
4. Manifest auto-populates: `FOOD,400,WATER,800,PLASMA,30`.
5. Dispatch Location: `Bangalore Depot`.
6. Click **Ingest Relief Cargo**.
7. **Verification:** Green alert confirms cargo stored with calculated payload (approx. 2015 kg).

#### Part C: Intelligent Dispatch
1. Click **Dispatch Center** in the sidebar.
2. Select Zone `ZONE-KA-9001`.
3. Click **Evaluate & Dispatch Mission**.
4. **Verification:**
   - DDUS Score calculates: `~88.5` -> **PRIORITY_ALPHA** or **BRAVO**.
   - Fleet Evaluator assigns best available asset: `HELI-CH47-01` or `TRUCK-TATRA-01`.
   - Status updates to `DISPATCHED`.

#### Part D: Phase 3 Delivery Audit
1. Click **Phase 3: Field Audit** in the sidebar.
2. Select the active mission for `ZONE-KA-9001`.
3. Enter Flight Duration: `45` min, Precision: `2.5` m, Officer: `Major Vikram Sharma`, Delivered Rations: `3500`, Check **Delivery Verified**.
4. Click **Submit Delivery Audit**.
5. **Verification:** Mission marked `COMPLETED`. Overall Relief Coverage percentage increases.

---

### TEST-06: Database Persistence on Device 2
1. On **Laptop 2** (Backend), open MySQL Workbench or terminal:
   ```sql
   USE disaster_relief_db;
   SELECT * FROM disaster_zones WHERE zone_code = 'ZONE-KA-9001';
   SELECT * FROM dispatch_missions ORDER BY id DESC LIMIT 1;
   ```
2. **Verification:** The records submitted from Laptop 1 are physically present in Laptop 2's MySQL database tables.

---

### TEST-07: Backend Disconnection Simulation
1. On **Laptop 1**, navigate to **Tab 9: System Settings**.
2. Click **Test Disconnect Alert** (or temporarily press CTRL+C on Laptop 2's backend terminal).
3. **Verification:**
   - Within 4-8 seconds, the green connection badge on Laptop 1 turns **Red**: `● Device 2: Offline`.
   - A prominent red warning banner drops down at the top:
     `⚠️ Cannot Connect to Device 2 Backend: Unable to reach http://...`
   - Notice: The UI does **not** crash or display blank screens; it gracefully catches the error and informs the user.
4. Restart the backend on Laptop 2 (or wait for the test to auto-restore).
5. **Verification:** The badge turns **Green** again and the banner automatically disappears.

---

### TEST-08: 100% Offline LAN Operation (Zero Internet)
1. Turn off the cellular data connection on your mobile hotspot phone (or unplug the WAN cable from your Wi-Fi router).
2. Ensure both laptops remain connected to the hotspot / router Wi-Fi.
3. Open a fresh browser window on Device 1 in Incognito mode.
4. Open: `http://localhost:3000/src/index.html`.
5. **Verification:**
   - The UI loads completely using bundled offline CSS and JavaScript in `assets/`.
   - Charts render without missing libraries.
   - REST API requests complete successfully over the local LAN.
   - Proof that no cloud, external API, or internet connection is used!

