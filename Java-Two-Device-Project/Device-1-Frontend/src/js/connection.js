/**
 * ==============================================================================
 * DREMLDS — TWO-DEVICE NETWORK CONNECTION MANAGER (DEVICE 1)
 * Manages communication with Device 2 Backend, health monitoring,
 * dynamic IP reconfiguration, and offline/disconnect alerting.
 * ==============================================================================
 */

(function () {
    const STORAGE_KEY = 'DREMLDS_BACKEND_URL';

    // Retrieve active backend base URL
    window.getApiBaseUrl = function () {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored && stored.trim().length > 0) {
            return stored.trim().replace(/\/+$/, '');
        }
        if (window.APP_CONFIG && window.APP_CONFIG.API_BASE_URL) {
            return window.APP_CONFIG.API_BASE_URL.trim().replace(/\/+$/, '');
        }
        return 'http://localhost:8080';
    };

    // Helper to form full API URL
    window.apiUrl = function (endpoint) {
        const base = window.getApiBaseUrl();
        const cleanEndpoint = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        return `${base}${cleanEndpoint}`;
    };

    // Update the stored Backend URL
    window.setApiBaseUrl = function (newUrl) {
        if (!newUrl) return;
        const clean = newUrl.trim().replace(/\/+$/, '');
        localStorage.setItem(STORAGE_KEY, clean);
        updateConnectionBadge('CHECKING', clean, 'Testing connection...');
        checkBackendHealth();
    };

    // Reset to default config
    window.resetApiBaseUrl = function () {
        localStorage.removeItem(STORAGE_KEY);
        const def = (window.APP_CONFIG && window.APP_CONFIG.API_BASE_URL) ? window.APP_CONFIG.API_BASE_URL : 'http://localhost:8080';
        updateConnectionBadge('CHECKING', def, 'Testing connection...');
        checkBackendHealth();
    };

    // Check backend health endpoint (/api/health)
    window.checkBackendHealth = async function () {
        const base = window.getApiBaseUrl();
        const healthUrl = `${base}/api/health`;
        const startTime = performance.now();

        const banner = document.getElementById('backend-disconnected-banner');
        const badge = document.getElementById('network-status-badge');
        const latencyEl = document.getElementById('network-latency');

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 4000);

            const res = await fetch(healthUrl, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            const latency = Math.round(performance.now() - startTime);

            if (res.ok) {
                const data = await res.json();
                updateConnectionBadge('CONNECTED', base, latency, data);
                if (banner) banner.style.display = 'none';
                return { success: true, latency, data };
            } else {
                updateConnectionBadge('DEGRADED', base, latency);
                if (banner) {
                    banner.style.display = 'block';
                    banner.innerHTML = `<strong>⚠️ Backend Degraded (HTTP ${res.status}):</strong> Connected to Device 2 at <code>${base}</code> but database or services are returning errors.`;
                }
                return { success: false, status: res.status };
            }
        } catch (err) {
            updateConnectionBadge('DISCONNECTED', base, null, null, err.message);
            if (banner) {
                banner.style.display = 'block';
                banner.innerHTML = `
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div>
                            <strong><i class="fa-solid fa-triangle-exclamation"></i> Cannot Connect to Device 2 Backend:</strong>
                            Unable to reach <code>${base}</code>. Ensure Device 2 is running, connected to the same Wi-Fi, and firewall port 8080 is open.
                        </div>
                        <button class="btn btn-sm btn-outline-light" onclick="window.promptChangeBackendUrl()">Change IP</button>
                    </div>`;
            }
            return { success: false, error: err.message };
        }
    };

    function updateConnectionBadge(state, url, latency, data, errorMsg) {
        const badge = document.getElementById('network-status-badge');
        const urlDisplay = document.getElementById('current-backend-url');
        const latencyEl = document.getElementById('network-latency');

        if (urlDisplay) urlDisplay.textContent = url;

        if (!badge) return;

        if (state === 'CONNECTED') {
            badge.className = 'badge bg-success-subtle text-success border border-success d-inline-flex align-items-center gap-1';
            badge.innerHTML = `<span class="status-indicator-dot bg-success"></span> <span>Device 2: Connected</span>`;
            if (latencyEl) {
                latencyEl.textContent = `${latency} ms`;
                latencyEl.className = 'text-success small';
            }
        } else if (state === 'CHECKING') {
            badge.className = 'badge bg-warning-subtle text-warning border border-warning d-inline-flex align-items-center gap-1';
            badge.innerHTML = `<span class="status-indicator-dot bg-warning"></span> <span>Testing Link...</span>`;
            if (latencyEl) latencyEl.textContent = '...';
        } else if (state === 'DEGRADED') {
            badge.className = 'badge bg-warning-subtle text-warning border border-warning d-inline-flex align-items-center gap-1';
            badge.innerHTML = `<span class="status-indicator-dot bg-warning"></span> <span>Device 2: Degraded</span>`;
            if (latencyEl) latencyEl.textContent = `${latency} ms`;
        } else {
            badge.className = 'badge bg-danger-subtle text-danger border border-danger d-inline-flex align-items-center gap-1';
            badge.innerHTML = `<span class="status-indicator-dot bg-danger"></span> <span>Device 2: Offline</span>`;
            if (latencyEl) {
                latencyEl.textContent = 'Unreachable';
                latencyEl.className = 'text-danger small';
            }
        }
    }

    // Modal or prompt to dynamically change backend IP
    window.promptChangeBackendUrl = function () {
        const current = window.getApiBaseUrl();
        const newUrl = prompt(
            "Enter Device 2 Backend URL (IPv4 address on shared Wi-Fi):\n\nExample: http://192.168.1.100:8080",
            current
        );
        if (newUrl !== null && newUrl.trim().length > 0) {
            window.setApiBaseUrl(newUrl);
            if (window.loadAllData) {
                setTimeout(() => window.loadAllData(), 500);
            }
        }
    };

    // Auto-initialize on load
    document.addEventListener('DOMContentLoaded', () => {
        // Initial health check
        window.checkBackendHealth();

        // Periodic background poll
        const interval = (window.APP_CONFIG && window.APP_CONFIG.HEALTH_POLL_INTERVAL_MS) || 8000;
        setInterval(() => {
            window.checkBackendHealth();
        }, interval);
    });
})();

