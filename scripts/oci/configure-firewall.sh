#!/usr/bin/env bash
# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# OCI FIREWALL CONFIGURATION UTILITY
# ==============================================================================

set -euo pipefail

MODE="${1:-app}" # 'app' or 'db'

echo "Configuring Linux UFW firewall for mode: ${MODE}..."

sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Always allow SSH
sudo ufw allow 22/tcp comment 'SSH Remote Management'

if [ "${MODE}" = "app" ]; then
    # Application VM: Allow Web Dashboard on 8080
    sudo ufw allow 8080/tcp comment 'Spring Boot DREMLDS Console'
    echo "Opened port 8080 for web traffic."
elif [ "${MODE}" = "db" ]; then
    # Database VM: Allow 3306 ONLY from internal VCN subnet (e.g. 10.0.0.0/16)
    APP_SUBNET="${2:-10.0.0.0/16}"
    sudo ufw allow from "${APP_SUBNET}" to any port 3306 proto tcp comment 'MySQL from App Subnet'
    echo "Restricted MySQL 3306 access strictly to subnet: ${APP_SUBNET}"
fi

sudo ufw --force enable
sudo ufw status verbose

