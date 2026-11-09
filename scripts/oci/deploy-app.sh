#!/usr/bin/env bash
# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# OCI VM 1: DEPLOY APPLICATION & SYSTEMD SERVICE
# ==============================================================================

set -euo pipefail

APP_DIR="/opt/disaster-relief"
JAR_NAME="disaster-relief-dispatch-1.0.0.jar"
SERVICE_NAME="disaster-relief.service"

echo "Deploying ${JAR_NAME} on OCI Application Server..."

# 1. Verify JAR exists
if [ ! -f "${APP_DIR}/${JAR_NAME}" ]; then
    echo "ERROR: ${APP_DIR}/${JAR_NAME} not found. Please SCP JAR from Windows before running deploy."
    exit 1
fi

sudo chown -R reliefapp:reliefapp "${APP_DIR}"
sudo chmod 500 "${APP_DIR}/${JAR_NAME}"

# 2. Check for environment file
if [ ! -f "${APP_DIR}/.env" ]; then
    echo "Creating template ${APP_DIR}/.env..."
    sudo bash -c "cat << 'EOF' > ${APP_DIR}/.env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=oci
DB_HOST=10.0.2.100
DB_PORT=3306
DB_NAME=disaster_relief_db
DB_USER=relief_app
DB_PASSWORD=ReliefOciSecurePass2026!
DDUS_WEIGHT_URGENCY=35.0
DDUS_WEIGHT_POPULATION=35.0
DDUS_WEIGHT_TERRAIN=30.0
DISPATCH_THREAD_POOL_SIZE=8
EOF"
    sudo chown reliefapp:reliefapp "${APP_DIR}/.env"
    sudo chmod 400 "${APP_DIR}/.env"
    echo "NOTICE: Update DB_HOST in ${APP_DIR}/.env with the private IP of your database VM."
fi

# 3. Create systemd unit file
echo "Configuring systemd service /etc/systemd/system/${SERVICE_NAME}..."
sudo bash -c "cat << 'EOF' > /etc/systemd/system/${SERVICE_NAME}
[Unit]
Description=Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)
After=network.target

[Service]
Type=simple
User=reliefapp
Group=reliefapp
WorkingDirectory=/opt/disaster-relief
EnvironmentFile=/opt/disaster-relief/.env
ExecStart=/usr/bin/java -Xms256m -Xmx1024m -jar /opt/disaster-relief/disaster-relief-dispatch-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF"

# 4. Reload and restart systemd
sudo systemctl daemon-reload
sudo systemctl enable "${SERVICE_NAME}"
sudo systemctl restart "${SERVICE_NAME}"

echo "======================================================================"
echo " Service ${SERVICE_NAME} deployed and started!"
echo " Check status with: sudo systemctl status ${SERVICE_NAME}"
echo " View live logs with: sudo journalctl -u ${SERVICE_NAME} -f"
echo "======================================================================"

