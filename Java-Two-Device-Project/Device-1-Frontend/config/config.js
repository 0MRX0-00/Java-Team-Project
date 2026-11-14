/**
 * ==============================================================================
 * DREMLDS — DISASTER RELIEF LOGISTICS DISPATCH SYSTEM
 * DEVICE 1 (FRONTEND) CONFIGURATION
 * ==============================================================================
 * 
 * This file configures the remote Backend Base URL (Device 2).
 * Both devices must be connected to the same Wi-Fi router or Mobile Hotspot.
 * 
 * INSTRUCTIONS:
 * 1. Find the IPv4 address of Device 2 (the backend laptop) by running:
 *      Windows: ipconfig   (Look for "IPv4 Address", e.g. 192.168.1.100)
 *      Linux:   ip a       or  hostname -I
 * 2. Replace the IP address below with Device 2's IPv4 address.
 * 3. Save this file and refresh your browser on Device 1.
 * 
 * NOTE: You can also change the Backend IP dynamically from the UI Connection Bar
 * at any time during your live presentation without editing this file!
 */

window.APP_CONFIG = {
    // Current Device 2 IPv4 address on your shared Wi-Fi
    API_BASE_URL: "http://10.47.118.217:8080",

    // Background health check polling interval in milliseconds
    HEALTH_POLL_INTERVAL_MS: 8000,

    // Request timeout in milliseconds
    REQUEST_TIMEOUT_MS: 10000,

    // Frontend metadata
    ROLE: "Device 1 - Frontend Presentation Layer",
    VERSION: "1.0.0"
};

