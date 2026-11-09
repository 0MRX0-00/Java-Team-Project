# ORACLE CLOUD INFRASTRUCTURE (OCI) TWO-VM PRODUCTION DEPLOYMENT GUIDE
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. ARCHITECTURE OVERVIEW

The application is deployed across **two separate Oracle Cloud Ubuntu Compute Instances** within an OCI Virtual Cloud Network (VCN):

```
+-----------------------------------------------------------------------------------+
|                           ORACLE CLOUD INFRASTRUCTURE (OCI)                       |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |                Virtual Cloud Network (VCN): 10.0.0.0/16                     |  |
|  |                                                                             |  |
|  |  +-----------------------------------+   +-------------------------------+  |  |
|  |  |  Application Subnet (10.0.1.0/24) |   | Database Subnet (10.0.2.0/24) |  |  |
|  |  |  (Public with Internet Gateway)   |   | (Private Subnet - No Direct   |  |  |
|  |  |                                   |   |  Public IP)                   |  |  |
|  |  |  [OCI VM 1: App Server]           |   |  [OCI VM 2: DB Server]        |  |  |
|  |  |  - Ubuntu 22.04/24.04 LTS         |   |  - Ubuntu 22.04/24.04 LTS     |  |  |
|  |  |  - Java 21 OpenJDK                |   |  - MySQL Server 8             |  |  |
|  |  |  - Spring Boot JAR (Port 8080)    |   |  - Port: 3306 (Internal Only) |  |  |
|  |  |  - Public IP: Assigned            |   |  - Private IP: 10.0.2.100     |  |  |
|  |  +-----------------+-----------------+   +---------------+---------------+  |  |
|  |                    |                                     ^                  |  |
|  |                    +=== JDBC TCP:3306 (Internal Only) ===+                  |  |
|  +-----------------------------------------------------------------------------+  |
|                                                                                   |
+-----------------------------------------------------------------------------------+
```

---

### 2. STEP-BY-STEP OCI SETUP PROCEDURE

#### Step 1: Generate SSH Key Pair on Windows 11
From PowerShell:
```powershell
ssh-keygen -t rsa -b 4096 -f "$HOME\.ssh\oci_dreml_key" -C "admin@dreml-cloud"
```
This generates:
- Private Key: `$HOME\.ssh\oci_dreml_key` (Keep private! Never commit)
- Public Key: `$HOME\.ssh\oci_dreml_key.pub` (Upload to OCI)

#### Step 2: Create OCI Virtual Cloud Network (VCN)
1. Log in to the **Oracle Cloud Console**.
2. Navigate to **Networking > Virtual Cloud Networks**.
3. Click **Start VCN Wizard > Create VCN with Internet Connectivity**.
4. Configure:
   - **VCN Name:** `dreml-vcn`
   - **VCN CIDR:** `10.0.0.0/16`
   - **Public Subnet (App Subnet):** `10.0.1.0/24`
   - **Private Subnet (DB Subnet):** `10.0.2.0/24`

#### Step 3: Configure Security Lists & Ingress Rules
##### A. Application Subnet Security List:
- **Ingress Rule 1:** Source: `0.0.0.0/0`, Protocol: `TCP`, Port: `22` (SSH - restrict to your local IP if static)
- **Ingress Rule 2:** Source: `0.0.0.0/0`, Protocol: `TCP`, Port: `8080` (DREMLDS Web Console & REST API)

##### B. Database Subnet Security List (Strict Least-Privilege):
- **Ingress Rule 1:** Source: `10.0.1.0/24` (App Subnet only), Protocol: `TCP`, Port: `3306` (MySQL)
- **Ingress Rule 2:** Source: `10.0.1.0/24` (Or Bastion/VPN), Protocol: `TCP`, Port: `22` (SSH)
- **NEVER** expose Port 3306 to `0.0.0.0/0`.

#### Step 4: Provision OCI Compute Instances
##### VM 1: Application Server
- **Name:** `dreml-app-server`
- **Image:** Canonical Ubuntu 22.04 or 24.04 LTS
- **Shape:** `VM.Standard.E2.1.Micro` or `VM.Standard.A1.Flex` (Always Free Eligible)
- **Subnet:** Select Public `App Subnet (10.0.1.0/24)`
- **SSH Keys:** Paste the contents of `$HOME\.ssh\oci_dreml_key.pub`
- **Assign Public IP:** Yes

##### VM 2: Database Server
- **Name:** `dreml-db-server`
- **Image:** Canonical Ubuntu 22.04 or 24.04 LTS
- **Shape:** `VM.Standard.E2.1.Micro` or `VM.Standard.A1.Flex` (Always Free Eligible)
- **Subnet:** Select `Database Subnet (10.0.2.0/24)`
- **SSH Keys:** Paste the contents of `$HOME\.ssh\oci_dreml_key.pub`
- **Assign Public IP:** Select Public IP temporarily during initial provisioning OR use Bastion/Private routing.

---

### 3. DEPLOYING FROM WINDOWS 11 POWERSHELL

#### Step A: Configure and Provision Database VM
Transfer and execute the setup script:
```powershell
# 1. SSH into DB Server
.\scripts\windows\ssh-db-server.ps1 -DbPublicIp <DB_PUBLIC_IP> -PrivateKeyPath "$HOME\.ssh\oci_dreml_key"

# 2. On DB VM, run the setup script:
curl -fsSL https://raw.githubusercontent.com/.../scripts/oci/setup-database-server.sh | sudo bash
# OR upload directly:
scp -i "$HOME\.ssh\oci_dreml_key" scripts/oci/setup-database-server.sh ubuntu@<DB_PUBLIC_IP>:/tmp/
ssh -i "$HOME\.ssh\oci_dreml_key" ubuntu@<DB_PUBLIC_IP> "chmod +x /tmp/setup-database-server.sh && sudo /tmp/setup-database-server.sh"

# 3. Upload database schema from Windows:
.\scripts\windows\upload-sql.ps1 -DbPublicIp <DB_PUBLIC_IP> -PrivateKeyPath "$HOME\.ssh\oci_dreml_key"
```

#### Step B: Build and Deploy Application Server
```powershell
# 1. Setup App Server:
scp -i "$HOME\.ssh\oci_dreml_key" scripts/oci/setup-app-server.sh ubuntu@<APP_PUBLIC_IP>:/tmp/
ssh -i "$HOME\.ssh\oci_dreml_key" ubuntu@<APP_PUBLIC_IP> "chmod +x /tmp/setup-app-server.sh && sudo /tmp/setup-app-server.sh"

# 2. Build local JAR and deploy to OCI VM 1:
.\scripts\windows\deploy-to-oci.ps1 -AppPublicIp <APP_PUBLIC_IP> -PrivateKeyPath "$HOME\.ssh\oci_dreml_key"
```

#### Step C: Configure App Server Connection to DB Server
On VM 1, edit `/opt/disaster-relief/.env`:
```bash
sudo nano /opt/disaster-relief/.env
```
Ensure the configuration points to the DB VM's private IP:
```properties
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=oci
DB_HOST=10.0.2.100  # Replace with DB Server's actual private IP
DB_PORT=3306
DB_NAME=disaster_relief_db
DB_USER=relief_app
DB_PASSWORD=ReliefOciSecurePass2026!
```
Restart the service:
```bash
sudo systemctl restart disaster-relief.service
sudo systemctl status disaster-relief.service
```

---

### 4. POST-DEPLOYMENT VERIFICATION

Open your browser:
- **Command Center Dashboard:** `http://<APP_PUBLIC_IP>:8080`
- **Swagger REST API Docs:** `http://<APP_PUBLIC_IP>:8080/swagger-ui.html`
- **Actuator Health Endpoint:** `http://<APP_PUBLIC_IP>:8080/actuator/health`

