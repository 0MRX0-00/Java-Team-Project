# Project Presentation & Demonstration Guide

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Step-by-Step Script & Demonstration Strategy for Professor Review*

---

## 1. Physical Hardware Setup Before the Presentation

Place the two laptops side by side on the presentation table:

```
+------------------------------------+      +------------------------------------+
|        LAPTOP 1 (LEFT SIDE)        |      |       LAPTOP 2 (RIGHT SIDE)        |
|        DEVICE 1: FRONTEND          |      |        DEVICE 2: BACKEND           |
|                                    |      |                                    |
| - Running: Google Chrome / Edge    |      | - Running: Spring Boot Console     |
| - Shows: Command Center UI         |      | - Running: MySQL Workbench         |
| - Browser DevTools (Network Tab)   |      | - Shows: REST Logs & SQL Tables    |
| - IP: 192.168.1.101                |      | - IP: 192.168.1.100:8080           |
+------------------------------------+      +------------------------------------+
                   ▲                                           ▲
                   │                                           │
                   └─────────── SAME WI-FI / HOTSPOT ──────────┘
```

1. **Network:** Connect both laptops to the same Wi-Fi router or phone mobile hotspot.
2. **Laptop 2 (Backend):**
   - Start MySQL 8.
   - Run `.\scripts\windows\start-backend.ps1` (or `java -jar target\disaster-relief-dispatch-1.0.0.jar`).
   - Open MySQL Workbench connected to `disaster_relief_db`.
3. **Laptop 1 (Frontend):**
   - Run `.\start-frontend.ps1`.
   - Open browser to `http://localhost:3000/src/index.html`.
   - Verify green connection badge: `● Device 2: Connected`.
   - Press `F12` to open DevTools -> select the **Network** tab.

---

## 2. Seven-Minute Presentation Script

### Minute 0 to 1: Architecture Introduction
**What to say:**
> *"Good morning Professor. Today our team presents the Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS). As required by your specification, our project is deployed in a physical Two-Device Client–Server Architecture across two independent laptops over a shared local area network, without reliance on Oracle Cloud, AWS, or any paid cloud infrastructure."*

**What to point out:**
- Point to **Laptop 1**: *"This laptop is the pure Presentation Client running HTML5, CSS3, ES6 JavaScript, Bootstrap 5, and Chart.js. It contains zero business logic and has zero direct database connectivity."*
- Point to **Laptop 2**: *"This laptop is the Application and Data Host running Java 21 LTS with Spring Boot and an independent MySQL 8 database. It binds to `0.0.0.0:8080` to accept incoming requests from Laptop 1 across our Wi-Fi network."*

---

### Minute 1 to 2: Proof of Physical Decoupling (Network Tab & Health Endpoint)
**What to do:**
1. On **Laptop 1**, open the browser DevTools **Network tab**.
2. Click the **Refresh Data** button.
3. Click on the request `statistics` or `charts`.
4. Point to the **Request URL**:
   ```text
   Request URL: http://192.168.1.100:8080/api/dashboard/statistics
   ```
**What to say:**
> *"Notice that the request is NOT going to localhost. It is addressed across the Wi-Fi network to Laptop 2's physical IPv4 address: `192.168.1.100` on port `8080`. Furthermore, our `/api/health` diagnostic endpoint continuously monitors the link latency (currently 12 ms) and tests the remote MySQL database connection using `SELECT 1`."*

---

### Minute 2 to 4: The 3-Phase Disaster Logistics Pipeline

#### Phase 1: Disaster Event Intake with Regex Validation
1. Click **Phase 1: Intake** on Laptop 1.
2. Type an invalid code: `ZONE-1234` -> Point to the red error: *Invalid format. Expected: ZONE-[A-Z]{2}-\d{4}*.
3. Type a valid code: `ZONE-TN-8008` (Cuddalore Sector Delta). Fill in population: `50,000`, Urgency: `9.5`, Weather: `5.0`.
4. Click **Register Disaster Zone**.
5. Switch to **Laptop 2**, open MySQL Workbench, run:
   ```sql
   SELECT * FROM disaster_zones WHERE zone_code = 'ZONE-TN-8008';
   ```
   Show the professor the record that just arrived from Laptop 1!

#### Phase 2: Cargo Ingestion & Manifest String Processing
1. Click **Phase 2: Cargo** on Laptop 1.
2. Select `ZONE-TN-8008`.
3. Enter Kits: Food = `500`, Water = `1000`, Plasma = `40`.
4. Point to the auto-generated manifest string: `FOOD,500,WATER,1000,PLASMA,40`.
5. Click **Ingest Relief Cargo**.

#### Phase 3: Intelligent Dispatch Scoring Engine
1. Click **Dispatch Center** on Laptop 1.
2. Select `ZONE-TN-8008`.
3. Click **Evaluate & Dispatch Mission**.
4. Show the professor the result card:
   - **DDUS Score:** Calculated based on Urgency (35%), Population (35%), and Terrain Inverse Risk (30%).
   - **Priority:** Classified into `PRIORITY_ALPHA` (Score >= 90.0).
   - **Fleet Recommendation:** Assigned Heavy Cargo Helicopter `HELI-CH47-01` after evaluating weather ceilings (Heli limit: 7.0 vs Zone weather: 5.0).

---

### Minute 4 to 5: Multithreaded Processing Demonstration
**What to do:**
1. On Laptop 1, navigate to the Dispatch tab and click **Bulk Concurrent Dispatch**.
2. Point to the terminal output on **Laptop 2**:
   Show the console printing:
   ```text
   [DispatchWorker-1] Processing Zone ID [1] on Thread pool worker 1
   [DispatchWorker-2] Processing Zone ID [2] on Thread pool worker 2
   [DispatchWorker-3] Processing Zone ID [3] on Thread pool worker 3
   ```
**What to say:**
> *"Here you can see our multithreaded dispatch executor in action. Laptop 1 triggers a batch request, and Laptop 2 dispatches an 8-worker thread pool (`DispatchWorker.java`) to calculate DDUS and allocate fleet assets concurrently without blocking the main application thread."*

---

### Minute 5 to 6: Phase 3 Delivery Audit & Coverage Tracking
1. Click **Phase 3: Field Audit** on Laptop 1.
2. Select the mission created for `ZONE-TN-8008`.
3. Enter: Flight Duration = `52 min`, Drop Precision = `3.2 m`, Officer = `Major Sandeep Unnikrishnan`, Delivered Rations = `4800`.
4. Check **Delivery Verified** and click **Submit Delivery Audit**.
5. Return to **Dashboard**: Show that the **Relief Coverage Percentage** has dynamically recalculated from MySQL data.

---

### Minute 6 to 7: Disconnection & Fault-Tolerance Test
**What to do:**
1. On Laptop 1, go to **Tab 9: System Settings**.
2. Click **Test Disconnect Alert** (or temporarily stop the backend on Laptop 2 with CTRL+C).
3. Point out that Laptop 1 immediately turns its badge **Red** (`Device 2: Offline`) and renders a prominent warning banner:
   *⚠️ Cannot Connect to Device 2 Backend at http://192.168.1.100:8080.*
4. Show that the UI does NOT crash; it gracefully manages network partition.
5. Reconnect/restart backend: within seconds, the banner disappears and the badge turns green again.

---

## 3. Anticipated Professor Questions & Model Answers

### Q1: "Why does the backend listen on `0.0.0.0` instead of `localhost` or `127.0.0.1`?"
> **Answer:** *"Binding to `127.0.0.1` limits the socket to the loopback interface, meaning only processes running on Laptop 2 could communicate with it. By setting `server.address=0.0.0.0`, the Spring Boot server binds to all network interfaces, allowing incoming TCP packets from Laptop 1 across our Wi-Fi subnet to reach port 8080."*

### Q2: "What is CORS and why was it necessary here?"
> **Answer:** *"Because Laptop 1's frontend runs on origin `http://localhost:3000` (or `http://192.168.1.101:3000`) and makes asynchronous fetch requests to `http://192.168.1.100:8080`, the browser's Same-Origin Policy triggers a cross-origin check. We implemented `CorsConfig.java` on Laptop 2 using Spring's `WebMvcConfigurer` to allow cross-origin requests, methods, and headers from our local LAN."*

### Q3: "Does this setup require any internet access or cloud services?"
> **Answer:** *"No, sir. We deliberately bundled all frontend CSS and JavaScript dependencies—Bootstrap 5, Chart.js, and FontAwesome—locally inside `Device-1-Frontend/assets/`. Even if we turn off the cellular data on our hotspot and isolate the local network completely from the internet, both laptops communicate seamlessly over LAN."*

### Q4: "Where are the database credentials stored? Can Laptop 1 see them?"
> **Answer:** *"The database credentials exist exclusively on Laptop 2 in `config/.env` or system environment variables. Laptop 1 only ever interacts with the public REST endpoints over HTTP JSON. It never sees database connection strings, passwords, or JDBC drivers, preserving strict multi-tier security separation."*

### Q5: "How does the DDUS Urgency Scoring formula work?"
> **Answer:** *"The Dynamic Disaster Urgency Score (DDUS) uses a weighted multi-criteria normalization formula:
> $$\text{DDUS} = \left(\frac{\text{Urgency}}{10}\right) \times 35 + \left(\frac{\text{Population}}{50,000}\right) \times 35 + \left(\frac{100 - \text{TerrainRisk}}{100}\right) \times 30$$
> If DDUS $\ge 90.0$, the mission is classified as **PRIORITY_ALPHA**; between $75.0$ and $89.9$ is **PRIORITY_BRAVO**; between $60.0$ and $74.9$ is **PRIORITY_CHARLIE**; below $60.0$ is **MISSION_ABORTED**."*
