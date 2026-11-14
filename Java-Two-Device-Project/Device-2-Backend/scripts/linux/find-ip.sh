#!/usr/bin/env bash
echo "======================================================================"
echo " DREMLDS — Device 2 (Backend) Local Network IP Detector (Linux)"
echo "======================================================================"

IP_ADDR=$(hostname -I | awk '{print $1}')
echo " Found active IPv4: $IP_ADDR"
echo " ===> Backend URL for Device 1: http://$IP_ADDR:8080"
echo "======================================================================"
