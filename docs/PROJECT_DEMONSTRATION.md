# FICTIONAL DEMONSTRATION SCENARIOS & OPERATIONAL WALKTHROUGH
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

> [!NOTE]
> All scenarios, casualty figures, locations, and mission records documented herein are **entirely fictional academic demonstration datasets** designed to test and showcase the DREMLDS dispatch algorithms.

---

### 1. SUMMARY OF DEMONSTRATION SCENARIOS (SOUTH INDIA REGION)

| Zone Code | Region / Sector Name | Event Type | Population | Urgency | Weather Index | Target Outcome |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`ZONE-TN-1003`** | Nagapattinam Delta Basin | FLOOD | 62,000 | 9.8 | 4.2 | **Priority Alpha** (Heavy Helicopter CH-47 dispatch) |
| **`ZONE-TN-1001`** | Cuddalore Coastal Delta | FLOOD | 45,000 | 9.5 | 5.8 | **Priority Bravo** (All-Terrain Rescue Truck Tatra dispatch) |
| **`ZONE-KL-2001`** | Wayanad Isolated Highlands | QUAKE | 7,200 | 9.6 | 4.0 | **Critical Plasma Drone** (SkyMed-X eVTOL dispatch) |
| **`ZONE-TN-1002`** | Nilgiris Mountain Ridge | QUAKE | 18,500 | 8.8 | 7.8 | **Grounded Aircraft & Ground Reassignment** (Weather 7.8 > 7.0 Heli ceiling &rarr; Reassigned to Rescue Truck) |
| **`ZONE-TN-1005`** | Chennai Urban Perimeter | FLOOD | 14,000 | 6.2 | 2.5 | **Priority Charlie** (Standard supply replenishment) |
| **`ZONE-KA-3001`** | Karwar Severe Storm Basin | FLOOD | 2,100 | 3.2 | 9.2 | **Mission Aborted** (DDUS 48.6 < 60 threshold; Storm 9.2 > 8.5 safety ceiling) |

---

### 2. DEMONSTRATION SCRIPT FOR EVALUATORS / FACULTY

Follow this sequence to present all requirements of the system:

#### Demonstration 1: Dashboard Telemetry
1. Open `http://localhost:8080` in Chrome or Edge.
2. Observe the 8 live database cards: Disaster Zones, Active Missions, Completed Missions, Relief Coverage %, Available Fleet, Grounded Missions.
3. Observe the four dynamic Chart.js visualizations showing real database distributions.

#### Demonstration 2: Phase 1 Disaster Intake & Regex Validation
1. Click **Phase 1: Intake** in the sidebar.
2. Enter an invalid zone code: `ZONE-123-1001` &rarr; Notice instant warning: *"Invalid format. Expected: ZONE-[A-Z]{2}-\d{4}"*.
3. Correct the code to `ZONE-TN-1009`.
4. Enter Population `35000`, Medical Urgency `9.2`, Terrain Risk `70.0`, Road Accessibility `30.0`, Weather `4.5`, Flight Distance `190.0`.
5. Click **Register Disaster Zone**. Notice database persistence and table update.

#### Demonstration 3: Phase 2 Cargo Ingestion & Manifest String Validation
1. Click **Phase 2: Cargo** in the sidebar.
2. Select `ZONE-TN-1009`.
3. Adjust Food Kits (400), Water Kits (800), Plasma Kits (50).
4. Notice the manifest string updates dynamically: `FOOD,400,WATER,800,PLASMA,50`.
5. Click **Register & Review Cargo**. The backend parses and validates key-value tokens using Java String operations.

#### Demonstration 4: Intelligent Dispatch & Multithreaded Processing
1. Click **Dispatch Center** in the sidebar.
2. Select `ZONE-TN-1009` and click **Execute Dispatch Worker**.
3. Inspect the live evaluation card: DDUS calculated, Priority classified, candidate asset matched, weather validated, asset reserved.
4. Click **Run Bulk Concurrent Dispatch (5+ Threads)**. Observe background worker threads (`DispatchWorker-1` ... `DispatchWorker-8`) processing requests in parallel without race conditions.

#### Demonstration 5: Weather Grounding & Ground Reassignment
1. Select **`ZONE-TN-1002` (Nilgiris Mountain Ridge)** which has a weather severity index of `7.8`.
2. Notice the helicopter ceiling is 7.0. The weather filter flags the helicopter as `FLIGHT_GROUNDED` and automatically queries for an available All-Terrain Rescue Truck (ceiling 8.5).
3. The mission is safely reassigned to the rescue truck, and the reassignment reason is logged.

#### Demonstration 6: Phase 3 Field Confirmation & Delivery Audit
1. Click **Phase 3: Field Audit** in the sidebar.
2. Select the newly dispatched mission.
3. Enter Transit Duration `68` minutes, Drop Precision `2.4` meters, Officer `Commander Rajesh Kumar, NDRF`, Delivered Rations `4200`.
4. Click **Record Field Confirmation**.
5. Observe: Mission status transitions to `COMPLETED`, the assigned transport asset is released back to `AVAILABLE`, and the Relief Coverage ratio updates in real time.

#### Demonstration 7: Reports & TXT File Exports
1. Click **Reports & Analytics** in the sidebar.
2. Click **Download Manifests TXT** &rarr; Inspect `exports/emergency_flight_manifests.txt` generated using Java NIO and `String.format()`.
3. Click **Download Summary TXT** &rarr; Inspect `exports/dispatch_summary.txt`.
4. View the table of multithreaded dispatch audit logs.

