#!/usr/bin/env bash
# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# OCI VM 1: APPLICATION SERVER SETUP SCRIPT (Ubuntu 22.04 / 24.04 LTS)
# ==============================================================================

set -euo pipefail

echo "======================================================================"
echo " Setting up OCI VM 1: Java 21 Application Server"
echo "======================================================================"

# 1. Update packages
sudo apt-get update -y
sudo apt-get upgrade -y

# 2. Install OpenJDK 21, net-tools, ufw, curl
sudo apt-get install -y openjdk-21-jdk openjdk-21-jre-headless net-tools ufw curl

# Verify Java 21
java -version

# 3. Create dedicated system user and application directory
sudo useradd -r -s /bin/false reliefapp || true
sudo mkdir -p /opt/disaster-relief /opt/disaster-relief/exports /opt/disaster-relief/logs /opt/disaster-relief/data
sudo chown -R reliefapp:reliefapp /opt/disaster-relief

# 4. Configure UFW Firewall
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment 'SSH Administration'
sudo ufw allow 8080/tcp comment 'DREMLDS Web Console & REST API'
sudo ufw --force enable

echo "======================================================================"
echo " Application VM Setup Complete!"
echo " Java 21 installed. Target directory: /opt/disaster-relief"
echo "======================================================================"

