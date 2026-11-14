# Device 1 — Frontend Command Center

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Client–Server Architecture: Device 1 (Frontend Presentation Layer)*

---

## 1. Role & Responsibilities

Device 1 is the **Presentation Client** running on the first physical laptop.

- **Contains:** All HTML5, CSS3, JavaScript ES6+, Bootstrap 5, and Chart.js telemetry code.
- **Independence:** Runs completely independently on Laptop 1.
- **Separation of Concerns:** Contains **zero** backend business logic and has **zero** direct database access.
- **Communication:** Communicates with Device 2 (the backend laptop) strictly via asynchronous HTTP REST calls with JSON payloads.
- **Offline Capable:** Includes bundled local assets (Bootstrap CSS, Bootstrap JS, Chart.js, FontAwesome) so the application functions 100% offline on a local Wi-Fi or hotspot network without internet.

---

## 2. Folder Layout

```
Device-1-Frontend/
├── src/
│   ├── index.html          # Main Command Center Single Page Application
│   └── js/
│       ├── connection.js   # Live network health monitor, dynamic IP manager, disconnect alerts
│       └── app.js          # REST API event handlers, Chart.js rendering, form processing
├── assets/
│   ├── css/
│   │   ├── bootstrap.min.css # Bundled offline Bootstrap 5 CSS
│   │   ├── all.min.css       # Bundled offline FontAwesome icons
│   │   └── style.css         # Dark Command Center styling & network badges
│   └── js/
│       ├── bootstrap.bundle.min.js # Bundled offline Bootstrap 5 JavaScript
│       └── chart.umd.min.js        # Bundled offline Chart.js library
├── config/
│   └── config.js           # Configurable Device 2 Backend IPv4 address
├── start-frontend.ps1      # Native Windows 11 PowerShell static web server (Zero install required!)
├── start-frontend.sh       # Linux/macOS static web server (Python / Node)
├── server.js               # Lightweight Node.js server (optional)
└── README.md               # This documentation file
```

---

## 3. Software Requirements on Device 1

Device 1 requires **NO Java**, **NO Maven**, and **NO MySQL** installed.

- **Operating System:** Windows 10 / 11, macOS, or Linux.
- **Web Browser:** Google Chrome, Microsoft Edge, Mozilla Firefox, or Brave.
- **Local Web Server (Choose Any One):**
  - **Option A (Recommended for Windows):** Native PowerShell (Included in Windows 10/11 — zero download required).
  - **Option B (Cross-platform):** Python 3 (`python -m http.server 3000`).
  - **Option C (Cross-platform):** Node.js (`node server.js` or `npx serve`).

---

## 4. How to Configure Device 2 Backend IP

Both laptops must be connected to the **same Wi-Fi router or Mobile Hotspot**.

### Step 1: Obtain Device 2's IPv4 Address
On Laptop 2 (Backend), find its IPv4 address:
- **Windows:** Run `ipconfig` in PowerShell (Look for `IPv4 Address`, e.g., `192.168.1.100`).
- **Linux:** Run `ip a` or `hostname -I`.

### Step 2: Configure the Backend Base URL
You have two easy ways to set the Backend IP:

#### Method A: Via Configuration File (`config/config.js`)
Open `config/config.js` in any text editor and edit the `API_BASE_URL`:
```javascript
window.APP_CONFIG = {
    API_BASE_URL: "http://192.168.1.100:8080", // Replace with Device 2's IPv4 address
    HEALTH_POLL_INTERVAL_MS: 8000
};
```

#### Method B: Dynamically in the Browser UI (Zero file editing!)
1. Open the frontend in your browser.
2. Click the **Device 2 Status Badge** in the top-right header (or click the **Gear Icon**).
3. A prompt will appear: Enter the new URL (e.g., `http://192.168.1.100:8080`).
4. Click **OK**. The UI will instantly ping Device 2, save the address to `localStorage`, and refresh all dashboards and charts!

---

## 5. How to Start the Frontend on Device 1

### On Windows 11 / 10 (Zero Installation)
Open PowerShell inside the `Device-1-Frontend` folder and run:
```powershell
powershell -ExecutionPolicy Bypass -File .\start-frontend.ps1
```
This automatically launches your default browser to `http://localhost:3000/src/index.html`.

### On Linux or macOS
Open Terminal inside `Device-1-Frontend` and run:
```bash
chmod +x start-frontend.sh
./start-frontend.sh
```
Then open: `http://localhost:3000/src/index.html`

### Alternative: Using Node.js
```bash
node server.js
```

---

## 6. Verifying Two-Device Communication

Once both devices are running on the same network:

1. Look at the top-right header on Device 1:
   - **Green Badge:** `● Device 2: Connected (12 ms)` — Communication verified!
   - **Amber Badge:** `● Checking...` — Pinging `/api/health`.
   - **Red Badge / Banner:** `⚠️ Cannot Connect to Device 2 Backend` — Device 2 unreachable.
2. In the sidebar, navigate to **Tab 9: System Settings**:
   - Click **Ping Health Check** to test latency and see the remote database status.
   - Click **Test Disconnect Alert** to simulate a lost connection and show the professor how the UI responds gracefully.

---

## 7. Troubleshooting Network Errors

| Symptom | Cause | Solution |
| :--- | :--- | :--- |
| **Red Offline Badge** | Device 2 is not reachable | 1. Ensure both laptops are on the exact same Wi-Fi.<br>2. Run `ping <DEVICE_2_IP>` from Device 1 terminal.<br>3. Check if Device 2's firewall allows port 8080. |
| **CORS Policy Error** | Cross-Origin blocked | Device 2's Spring Boot backend is already configured with `CorsConfig.java` to allow all LAN origins. Verify backend is running the latest build. |
| **HTTP 404 on API calls** | Wrong port or path | Ensure URL format is `http://<IP>:8080` (without trailing `/api`). The app appends `/api/...` automatically. |
| **No Internet Warning** | Isolated LAN / Hotspot | Normal! DREMLDS works 100% offline using the bundled offline assets in `assets/`. |

