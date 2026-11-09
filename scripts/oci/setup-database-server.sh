#!/usr/bin/env bash
# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# OCI VM 2: DATABASE SERVER SETUP SCRIPT (Ubuntu 22.04 / 24.04 LTS)
# ==============================================================================

set -euo pipefail

echo "======================================================================"
echo " Setting up OCI VM 2: MySQL 8 Dedicated Database Server"
echo "======================================================================"

# 1. Update packages
sudo apt-get update -y
sudo apt-get upgrade -y

# 2. Install MySQL Server 8 and utilities
sudo apt-get install -y mysql-server net-tools ufw

# 3. Configure MySQL to listen on private network interface (or all interfaces)
echo "Configuring MySQL bind-address to 0.0.0.0 for private subnet traffic..."
sudo sed -i 's/^bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf

# 4. Restart MySQL Service
sudo systemctl restart mysql
sudo systemctl enable mysql

# 5. Create application database and restricted dedicated user
DB_NAME="disaster_relief_db"
DB_USER="relief_app"
DB_PASS="ReliefOciSecurePass2026!" # Change in production to a strong passphrase

echo "Creating database and dedicated application user '${DB_USER}'..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';"
sudo mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'%';"
sudo mysql -e "FLUSH PRIVILEGES;"

# 6. Configure Ubuntu UFW Firewall
echo "Configuring UFW Firewall for Database VM..."
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment 'SSH Administration'
# Allow MySQL 3306 ONLY from the internal OCI VCN private CIDR (e.g., 10.0.0.0/16 or App Subnet)
sudo ufw allow from 10.0.0.0/16 to any port 3306 proto tcp comment 'MySQL from App Subnet'
sudo ufw --force enable

echo "======================================================================"
echo " Database VM Setup Complete!"
echo " MySQL is listening on port 3306 (restricted to private VCN)."
echo " Import database schema next using: mysql -u relief_app -p disaster_relief_db < disaster_relief_schema.sql"
echo "======================================================================"

