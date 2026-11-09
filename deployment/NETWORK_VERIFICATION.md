# NETWORK VERIFICATION & TROUBLESHOOTING GUIDE
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. NETWORK CONNECTIVITY MATRIX

| Source | Destination | Protocol / Port | Purpose | Permitted Scope |
| :--- | :--- | :--- | :--- | :--- |
| Windows 11 Admin PC | OCI VM 1 (App Server) | TCP: 22 (SSH) | Server Administration | Restricted Admin IP / 0.0.0.0/0 |
| Windows 11 Browser | OCI VM 1 (App Server) | TCP: 8080 (HTTP) | Web Dashboard & REST API | Public (0.0.0.0/0) |
| Windows 11 Admin PC | OCI VM 2 (DB Server) | TCP: 22 (SSH) | Database Administration | Restricted Admin IP |
| OCI VM 1 (App Server) | OCI VM 2 (DB Server) | TCP: 3306 (MySQL) | HikariCP Connection Pool | **Private Subnet Only (10.0.1.0/24)** |
| Internet | OCI VM 2 (DB Server) | TCP: 3306 (MySQL) | Direct MySQL Access | **BLOCKED (DENY ALL)** |

---

### 2. NETWORK VERIFICATION COMMANDS

#### A. From Windows 11 PowerShell

1. **Verify SSH Access to Application Server:**
   ```powershell
   ssh -i "$HOME\.ssh\oci_dreml_key" ubuntu@<APP_PUBLIC_IP>
   ```

2. **Verify SCP File Transfer:**
   ```powershell
   scp -i "$HOME\.ssh\oci_dreml_key" target/disaster-relief-dispatch-1.0.0.jar ubuntu@<APP_PUBLIC_IP>:/tmp/
   ```

3. **Verify App Server Port 8080 from Windows:**
   ```powershell
   Test-NetConnection <APP_PUBLIC_IP> -Port 8080
   ```
   *Expected Output: `TcpTestSucceeded : True`*

4. **Verify Database Port (From within private network/VPN):**
   ```powershell
   Test-NetConnection <DATABASE_PRIVATE_IP> -Port 3306
   ```

---

#### B. From OCI VM 1 (Application Server) to VM 2 (Database Server)

Log in to VM 1:
```bash
ssh -i ~/.ssh/oci_dreml_key ubuntu@<APP_PUBLIC_IP>
```

1. **Test TCP Socket Connectivity with Netcat:**
   ```bash
   nc -vz <DATABASE_PRIVATE_IP> 3306
   ```
   *Expected Output: `Connection to 10.0.2.100 3306 port [tcp/mysql] succeeded!`*

2. **Test Direct MySQL Handshake:**
   ```bash
   mysql -h <DATABASE_PRIVATE_IP> -u relief_app -p -e "SELECT @@version, DATABASE();"
   ```

3. **Ping (ICMP) Warning:**
   ```bash
   ping -c 3 <DATABASE_PRIVATE_IP>
   ```
   > [!IMPORTANT]
   > **Why `ping` may fail while TCP works:**
   > Default cloud security lists and Ubuntu firewalls frequently block ICMP (Internet Control Message Protocol) packets to mitigate echo flood attacks.
   > A failed `ping` does **NOT** indicate database outage if `nc -vz` or `Test-NetConnection` reports port 3306 is open!

---

### 3. ACADEMIC SUBMISSION SCREENSHOT CHECKLIST

For project demonstration and academic grading, capture clear screenshots of the following:

- [ ] **Checklist Item 1: OCI Compute Instances Overview**
  - Shows two running Ubuntu VMs (`dreml-app-server` and `dreml-db-server`) in the OCI Console.
- [ ] **Checklist Item 2: OCI VCN Subnets & Route Tables**
  - Shows `dreml-vcn` with public Application Subnet (`10.0.1.0/24`) and private Database Subnet (`10.0.2.0/24`).
- [ ] **Checklist Item 3: Security Lists & Ingress Rules**
  - Displays App Subnet rules (Ports 22 & 8080 open).
  - Displays DB Subnet rules (Port 3306 restricted strictly to `10.0.1.0/24`).
- [ ] **Checklist Item 4: Linux UFW Firewall Verification**
  - Shows `sudo ufw status verbose` on both VM 1 and VM 2.
- [ ] **Checklist Item 5: Inter-VM Database Connectivity Test**
  - Shows output of `nc -vz <DB_PRIVATE_IP> 3306` executed from VM 1.
- [ ] **Checklist Item 6: Systemd Service Active Status**
  - Shows `sudo systemctl status disaster-relief.service` running as `active (running)`.
- [ ] **Checklist Item 7: DREMLDS Web Console Dashboard in Browser**
  - Chrome or Edge screenshot of `http://<APP_PUBLIC_IP>:8080` displaying real statistics and Chart.js graphs.
- [ ] **Checklist Item 8: Swagger / OpenAPI Documentation Page**
  - Screenshot of `http://<APP_PUBLIC_IP>:8080/swagger-ui.html` displaying live REST endpoints.

