#!/usr/bin/env bash
# ==============================================================================
# DREMLDS — DEVICE 2 BACKEND STARTUP SCRIPT (LINUX)
# ==============================================================================

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
BACKEND_ROOT="$(cd "$DIR/../.." && pwd)"
cd "$BACKEND_ROOT" || exit 1

echo "======================================================================"
echo " DREMLDS — Starting Device 2 Backend & Database Server (Linux)"
echo "======================================================================"

IP_ADDR=$(hostname -I | awk '{print $1}')
echo " Device 2 IPv4: $IP_ADDR"
echo " ===> Backend URL for Device 1: http://$IP_ADDR:8080"
echo ""

chmod +x ./mvnw
./mvnw spring-boot:run

