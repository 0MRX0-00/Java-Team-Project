#!/usr/bin/env bash
echo "Allowing TCP port 8080 through UFW firewall on Device 2..."
sudo ufw allow 8080/tcp
sudo ufw reload
sudo ufw status | grep 8080
echo "Done."
