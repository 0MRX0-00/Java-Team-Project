# Network Setup & Local Connectivity Guide

**Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)**  
*Configuring Two Laptops on the Same Local Area Network (Zero Cloud / 100% Offline)*

---

## 1. Network Topology Requirements

To run this application across two physical laptops, both devices must be connected to the **same local subnet**.

```
                        +----------------------------+
                        |  Wi-Fi Router OR Hotspot   |
                        | (No Internet Access Needed)|
                        +----------------------------+
                                      │
              ┌───────────────────────┴───────────────────────┐
              ▼                                               ▼
+---------------------------+                   +---------------------------+
|    LAPTOP 1 (FRONTEND)    |                   |    LAPTOP 2 (BACKEND)     |
|   IP: 192.168.1.101/24    |                   |   IP: 192.168.1.100/24    |
|   Role: Presentation UI   |                   |   Role: Spring Boot + DB  |
|   Port: 3000 (Local Web)  |                   |   Port: 8080 (REST API)   |
+---------------------------+                   +---------------------------+
```

### Supported Network Types:
1. **Home / Classroom Wi-Fi Router:** Connect both laptops to the same SSID (avoid "Guest" Wi-Fi networks which enable AP Isolation).
2. **Smartphone Mobile Hotspot:** Turn on hotspot on any smartphone and connect both laptops. **Tip:** You can turn OFF cellular mobile data on the phone to prove to your professor that the system operates 100% locally without internet!
3. **Direct Ethernet Cable or Switch:** Connect both laptops via an RJ-45 Ethernet cable or switch with static IPs (e.g. `192.168.1.1` and `192.168.1.2`).

---

## 2. Step 1: Detect Device 2's IPv4 Address

You need the local IP address of Device 2 (the backend laptop) so Device 1 knows where to send requests.

### On Windows 11 / 10:
Open PowerShell inside `Device-2-Backend` and run the helper script:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\find-ip.ps1
```
Or use the standard Windows command:
```powershell
ipconfig
```
Look for your active Wi-Fi adapter:
```text
Wireless LAN adapter Wi-Fi:
   IPv4 Address. . . . . . . . . . . : 192.168.1.100
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . : 192.168.1.1
```
*(In this example, Device 2's IP is `192.168.1.100`)*.

### On Linux / macOS:
```bash
hostname -I | awk '{print $1}'
```
Or:
```bash
ip -4 addr show
```

---

## 3. Step 2: Open Inbound Firewall Port 8080 on Device 2

By default, Windows Defender Firewall and Linux UFW block incoming connections from other computers on the LAN. You must permit port 8080 on Device 2.

### On Windows 11 / 10:
Open PowerShell **as Administrator** and run:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\windows\allow-firewall-port.ps1
```
Or execute the PowerShell cmdlet directly:
```powershell
New-NetFirewallRule -DisplayName "DREMLDS Backend (Port 8080)" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

### On Linux (Ubuntu / Debian):
```bash
sudo ufw allow 8080/tcp
sudo ufw reload
```

---

## 4. Step 3: Test Cross-Device Network Reachability

Before starting the application, verify that Device 1 can talk to Device 2.

### Test A: ICMP Ping Test
On **Device 1** (Frontend Laptop), open PowerShell or Terminal and ping Device 2:
```powershell
ping 192.168.1.100
```
Expected output:
```text
Pinging 192.168.1.100 with 32 bytes of data:
Reply from 192.168.1.100: bytes=32 time=4ms TTL=128
Reply from 192.168.1.100: bytes=32 time=3ms TTL=128
```
*Note: If ping times out, Device 2 may have ICMP Echo disabled in its firewall, but HTTP port 8080 may still work.*

### Test B: HTTP Health Check Test
Once the backend is started on Device 2:
On **Device 1**, open Google Chrome or Edge and navigate to:
```text
http://192.168.1.100:8080/api/health
```
Expected JSON Response:
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

## 5. Step 4: Configure Device 1 Frontend

On Device 1, open `Device-1-Frontend/config/config.js` and set:
```javascript
window.APP_CONFIG = {
    API_BASE_URL: "http://192.168.1.100:8080" // Replace with Device 2's actual IPv4
};
```
*Alternatively, you can change it dynamically at any time in the browser UI without touching files.*

---

## 6. Common Network Issues & Troubleshooting

### Issue 1: "Request Timed Out" or Cannot Reach Device 2
- **Cause 1: Wi-Fi AP Client Isolation:** Some public or university Wi-Fi routers prevent wireless devices from talking directly to each other (Access Point Isolation).
  - **Fix:** Disconnect both laptops from university Wi-Fi and connect both to a personal smartphone hotspot.
- **Cause 2: Subnet Mismatch:** One laptop is on `192.168.1.x` and the other is on `192.168.0.x`.
  - **Fix:** Ensure both laptops are connected to the exact same Wi-Fi network and receive IPs in the same subnet.
- **Cause 3: Windows Network Profile set to "Public":**
  - **Fix:** In Windows Settings -> Network & Internet -> Wi-Fi -> Click your network name -> Change network profile from **Public** to **Private**.

### Issue 2: Third-Party Antivirus Firewall Blocking Port 8080
- Software such as Norton, McAfee, Bitdefender, or Kaspersky overrides Windows Firewall.
  - **Fix:** In your antivirus firewall settings, add an "Inbound Allow Rule" for TCP port 8080.

### Issue 3: IP Address Changed After Reconnecting
- When laptops reconnect to Wi-Fi, the DHCP server may assign a new IP address.
  - **Fix:** Re-run `find-ip.ps1` on Device 2 to check if the IP changed. Then in the Device 1 UI, click the top-right status badge, enter the new IP, and click OK.

