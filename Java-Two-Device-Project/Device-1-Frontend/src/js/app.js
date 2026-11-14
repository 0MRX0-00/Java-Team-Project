/**
 * ==============================================================================
 * DREMLDS COMMAND CENTER FRONT-END ENGINE (DEVICE 1 - CLIENT)
 * Full-Featured, High-Performance Two-Device Architecture Client
 * Handles real-time telemetry, Chart.js visualizations, and asynchronous REST APIs.
 * Connects to Device 2 Backend over Local Wi-Fi / LAN via window.apiUrl().
 * ==============================================================================
 */

// Helper to resolve backend base URL
function apiUrl(path) {
    if (typeof window.apiUrl === 'function') {
        return window.apiUrl(path);
    }
    return path;
}

// Robust fetch helper with timeout
async function apiFetch(endpoint, options = {}, timeoutMs = 4000) {
    if (typeof window.apiFetch === 'function') {
        return window.apiFetch(endpoint, options, timeoutMs);
    }
    const url = apiUrl(endpoint);
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    try {
        const res = await fetch(url, { ...options, signal: controller.signal });
        clearTimeout(timeoutId);
        return res;
    } catch (err) {
        clearTimeout(timeoutId);
        throw err;
    }
}

// Chart instances
let chartEventTypes = null;
let chartTransportCategories = null;
let chartMissionPriorities = null;
let chartReliefCoverage = null;

// Global State
let rawMissions = [];
let rawZones = [];
let rawCargo = [];
let rawAssets = [];
let activeMissionFilter = 'ALL';
let autoRefreshTimer = null;
let autoRefreshSeconds = 8;
let autoRefreshPaused = false;
window.currentActiveReport = 'manifests';

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initZoneCodeValidation();
    initCargoCalculator();
    initUrgencyEstimator();
    initEventHandlers();
    initNetworkSettings();
    updateReportLinks();
    initAutoRefresh();
    loadAllData();
});

// =============================================================================
// TOAST NOTIFICATION SYSTEM
// =============================================================================

function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const icons = {
        success: 'fa-circle-check text-success',
        danger: 'fa-circle-exclamation text-danger',
        warning: 'fa-triangle-exclamation text-warning',
        info: 'fa-circle-info text-info'
    };

    const toast = document.createElement('div');
    toast.className = `toast-custom toast-${type}`;
    toast.innerHTML = `
        <i class="fa-solid ${icons[type] || icons.info} fa-lg"></i>
        <div class="flex-grow-1">
            <strong class="d-block small" style="color: #fff;">${escapeHtml(title)}</strong>
            <span class="small text-secondary">${escapeHtml(message)}</span>
        </div>
        <button type="button" class="btn-close btn-close-white btn-sm" aria-label="Close"></button>
    `;

    const closeBtn = toast.querySelector('.btn-close');
    closeBtn.addEventListener('click', () => {
        toast.remove();
    });

    container.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.transition = 'opacity 0.4s, transform 0.4s';
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 400);
        }
    }, 4500);
}

// =============================================================================
// NAVIGATION & TABS
// =============================================================================

function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    const tabViews = document.querySelectorAll('.tab-view');
    const viewTitle = document.getElementById('view-title');
    const viewSubtitle = document.getElementById('view-subtitle');

    const titles = {
        'tab-dashboard': ['Disaster Operations Command Center', 'Real-time situational telemetry and emergency dispatch pipeline'],
        'tab-dispatch': ['Intelligent Dispatch Center', 'Algorithmic urgency scoring, weather safety filters, and asset allocation'],
        'tab-missions': ['Real-Time Mission Tracking', 'Fleet mission progress, duration, transport telemetry, and grounding alerts'],
        'tab-intake': ['Phase 1: Disaster Event Registration', 'Standardized disaster zone entry with regex zone code validation'],
        'tab-cargo': ['Phase 2: Emergency Relief Cargo Staging', 'Itemized payload ingestion and cargo manifest string processing'],
        'tab-audit': ['Phase 3: Field Confirmation & Delivery Audit', 'On-ground verification, precision measurement, and relief coverage tracking'],
        'tab-fleet': ['Transport Fleet Management', 'Air and ground asset operational status and payload capabilities'],
        'tab-reports': ['Operational Reports & Live Console', 'Exportable flight manifests, supply drop logs, and execution audit trails'],
        'tab-settings': ['System Settings & Two-Device Architecture', 'Distributed Client-Server network topology and DDUS algorithmic parameters']
    };

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetTab = link.getAttribute('data-tab');

            navLinks.forEach(l => l.classList.remove('active'));
            tabViews.forEach(v => v.classList.remove('active'));

            link.classList.add('active');
            const activeView = document.getElementById(targetTab);
            if (activeView) activeView.classList.add('active');

            if (titles[targetTab]) {
                viewTitle.textContent = titles[targetTab][0];
                viewSubtitle.textContent = titles[targetTab][1];
            }

            // Tab-specific initializers
            if (targetTab === 'tab-dashboard') {
                loadDashboardData();
            } else if (targetTab === 'tab-reports') {
                window.viewReportInConsole(window.currentActiveReport || 'manifests');
            }
        });
    });
}

// =============================================================================
// AUTO-REFRESH ENGINE
// =============================================================================

function initAutoRefresh() {
    const toggleBtn = document.getElementById('btn-toggle-auto-refresh');
    const label = document.getElementById('auto-refresh-label');

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            autoRefreshPaused = !autoRefreshPaused;
            if (autoRefreshPaused) {
                toggleBtn.classList.remove('btn-secondary-custom');
                toggleBtn.classList.add('btn-outline-warning');
                label.textContent = 'Paused';
                showToast('Auto-Refresh', 'Live telemetry auto-refresh paused.', 'warning');
            } else {
                toggleBtn.classList.remove('btn-outline-warning');
                toggleBtn.classList.add('btn-secondary-custom');
                autoRefreshSeconds = 8;
                label.textContent = `Live (${autoRefreshSeconds}s)`;
                showToast('Auto-Refresh', 'Live telemetry auto-refresh resumed.', 'info');
            }
        });
    }

    if (autoRefreshTimer) clearInterval(autoRefreshTimer);
    autoRefreshTimer = setInterval(() => {
        if (autoRefreshPaused) return;

        autoRefreshSeconds--;
        if (label) label.textContent = `Live (${autoRefreshSeconds}s)`;

        if (autoRefreshSeconds <= 0) {
            autoRefreshSeconds = 8;
            loadDashboardData(true);
        }
    }, 1000);
}

// =============================================================================
// PHASE 1: ZONE CODE REGEX VALIDATION & LIVE URGENCY ESTIMATOR
// =============================================================================

function initZoneCodeValidation() {
    const input = document.getElementById('intake-zone-code');
    const feedback = document.getElementById('zone-code-feedback');
    const regex = /^ZONE-[A-Z]{2}-\d{4}$/;

    if (!input) return;

    input.addEventListener('input', () => {
        const val = input.value.trim().toUpperCase();
        input.value = val;

        if (val.length === 0) {
            feedback.textContent = 'Must match regex ZONE-[A-Z]{2}-\\d{4} (e.g. ZONE-TN-1001)';
            feedback.className = 'form-text text-muted';
        } else if (regex.test(val)) {
            feedback.textContent = 'Valid Zone Code format: ' + val;
            feedback.className = 'form-text text-success fw-bold';
        } else {
            feedback.textContent = 'Invalid format. Expected: ZONE-[A-Z]{2}-\\d{4} (e.g., ZONE-TN-1001)';
            feedback.className = 'form-text text-danger fw-bold';
        }
    });
}

function initUrgencyEstimator() {
    const fields = [
        'intake-population', 'intake-urgency', 'intake-terrain',
        'intake-road', 'intake-weather', 'intake-distance'
    ];

    fields.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('input', updateUrgencyEstimator);
    });

    updateUrgencyEstimator();
}

function updateUrgencyEstimator() {
    const pop = parseInt(document.getElementById('intake-population')?.value) || 0;
    const urgency = parseFloat(document.getElementById('intake-urgency')?.value) || 0;
    const terrain = parseFloat(document.getElementById('intake-terrain')?.value) || 0;
    const road = parseFloat(document.getElementById('intake-road')?.value) || 0;
    const weather = parseFloat(document.getElementById('intake-weather')?.value) || 0;
    const dist = parseFloat(document.getElementById('intake-distance')?.value) || 0;

    const scoreEl = document.getElementById('preview-ddus-score');
    const badgeEl = document.getElementById('preview-priority-badge');
    const transportEl = document.getElementById('preview-transport-mode');
    const weatherEl = document.getElementById('preview-weather-status');

    if (!scoreEl) return;

    if (urgency === 0 && pop === 0) {
        scoreEl.textContent = '--';
        badgeEl.className = 'badge-custom badge-aborted';
        badgeEl.textContent = 'PENDING INPUT';
        if (transportEl) transportEl.textContent = '--';
        if (weatherEl) weatherEl.textContent = '--';
        return;
    }

    // Algorithmic DDUS Formula: (Urgency/10)*35 + min(pop/100000, 1)*35 + (max(0, 100-terrain)/100)*30
    const popFactor = Math.min(1.0, pop / 100000.0);
    const terrainFactor = Math.max(0, 100.0 - terrain) / 100.0;
    const ddus = Math.round(((urgency / 10.0) * 35.0 + popFactor * 35.0 + terrainFactor * 30.0) * 10) / 10;

    scoreEl.textContent = ddus.toFixed(1);

    // Priority Classification
    if (ddus >= 90.0) {
        badgeEl.className = 'badge-custom badge-alpha';
        badgeEl.textContent = 'PRIORITY ALPHA';
    } else if (ddus >= 75.0) {
        badgeEl.className = 'badge-custom badge-bravo';
        badgeEl.textContent = 'PRIORITY BRAVO';
    } else if (ddus >= 60.0) {
        badgeEl.className = 'badge-custom badge-charlie';
        badgeEl.textContent = 'PRIORITY CHARLIE';
    } else {
        badgeEl.className = 'badge-custom badge-aborted';
        badgeEl.textContent = 'MISSION ABORTED (< 60)';
    }

    // Recommended Transport Mode
    if (transportEl) {
        if (road < 30.0 || terrain > 70.0) {
            transportEl.textContent = dist < 120.0 && weather <= 5.0 ? 'Helicopter / Drone' : 'Heavy Cargo Helicopter';
        } else {
            transportEl.textContent = 'Tactical Rescue Truck (4x4 / 8x8)';
        }
    }

    // Weather Clearance
    if (weatherEl) {
        if (weather > 8.5) {
            weatherEl.className = 'small fw-bold text-danger';
            weatherEl.textContent = 'Severe Storm: All Grounded';
        } else if (weather > 7.0) {
            weatherEl.className = 'small fw-bold text-warning';
            weatherEl.textContent = 'Air Grounded (Trucks Only)';
        } else if (weather > 5.0) {
            weatherEl.className = 'small fw-bold text-info';
            weatherEl.textContent = 'Heli & Trucks Safe (Drone Ceiling Exceeded)';
        } else {
            weatherEl.className = 'small fw-bold text-success';
            weatherEl.textContent = 'All Fleet Cleared for Launch';
        }
    }
}

// Intake Presets
window.loadIntakePreset = function (type) {
    const presets = {
        'CYCLONE': {
            code: 'ZONE-TN-8001',
            name: 'Nagapattinam Delta Cyclone Sector',
            type: 'CYCLONE',
            pop: 62000,
            urgency: 9.8,
            terrain: 62.0,
            road: 28.0,
            weather: 6.2,
            dist: 290.0
        },
        'FLOOD': {
            code: 'ZONE-TN-8002',
            name: 'Cuddalore Coastal Delta Flood Zone',
            type: 'FLOOD',
            pop: 45000,
            urgency: 9.5,
            terrain: 78.0,
            road: 22.0,
            weather: 5.2,
            dist: 185.0
        },
        'LANDSLIDE': {
            code: 'ZONE-KL-8003',
            name: 'Wayanad Mountain Highland Sector',
            type: 'LANDSLIDE',
            pop: 12500,
            urgency: 9.4,
            terrain: 94.0,
            road: 8.0,
            weather: 4.8,
            dist: 110.0
        },
        'QUAKE': {
            code: 'ZONE-KA-8004',
            name: 'Kodagu Seismic Faultline Relief',
            type: 'QUAKE',
            pop: 32000,
            urgency: 8.9,
            terrain: 55.0,
            road: 40.0,
            weather: 3.5,
            dist: 150.0
        }
    };

    const p = presets[type];
    if (!p) return;

    document.getElementById('intake-zone-code').value = p.code;
    document.getElementById('intake-zone-name').value = p.name;
    document.getElementById('intake-event-type').value = p.type;
    document.getElementById('intake-population').value = p.pop;
    document.getElementById('intake-urgency').value = p.urgency;
    document.getElementById('intake-terrain').value = p.terrain;
    document.getElementById('intake-road').value = p.road;
    document.getElementById('intake-weather').value = p.weather;
    document.getElementById('intake-distance').value = p.dist;

    const feedback = document.getElementById('zone-code-feedback');
    if (feedback) {
        feedback.textContent = 'Valid Zone Code format: ' + p.code;
        feedback.className = 'form-text text-success fw-bold';
    }

    updateUrgencyEstimator();
    showToast('Preset Loaded', `Pre-filled ${p.name} with standard field parameters.`, 'info');
};

// =============================================================================
// PHASE 2: CARGO CALCULATOR & PRESETS
// =============================================================================

function initCargoCalculator() {
    const food = document.getElementById('cargo-food');
    const water = document.getElementById('cargo-water');
    const plasma = document.getElementById('cargo-plasma');
    const manifest = document.getElementById('cargo-manifest-str');
    const payload = document.getElementById('cargo-payload');
    const rations = document.getElementById('cargo-rations');

    function updateManifest() {
        const f = parseInt(food?.value) || 0;
        const w = parseInt(water?.value) || 0;
        const p = parseInt(plasma?.value) || 0;

        if (manifest) manifest.value = `FOOD,${f},WATER,${w},PLASMA,${p}`;
        // Approx payload calculation: Food=2.0kg, Water=1.5kg, Plasma=0.5kg
        const calcPayload = Math.round((f * 2.0 + w * 1.5 + p * 0.5) * 10) / 10;
        if (payload) payload.value = calcPayload;
        if (rations) rations.value = Math.max(100, f * 5 + w * 2);

        updatePayloadMeter(calcPayload);
    }

    [food, water, plasma].forEach(el => {
        if (el) el.addEventListener('input', updateManifest);
    });

    if (payload) payload.addEventListener('input', () => {
        updatePayloadMeter(parseFloat(payload.value) || 0);
    });

    updateManifest();
}

function updatePayloadMeter(weightKg) {
    const displayWeight = document.getElementById('cargo-display-weight');
    const weightBar = document.getElementById('cargo-weight-bar');
    const recVehicle = document.getElementById('cargo-recommended-vehicle');

    if (displayWeight) displayWeight.textContent = weightKg.toLocaleString() + ' KG';

    if (weightBar) {
        const pct = Math.min(100, Math.round((weightKg / 8500.0) * 100));
        weightBar.style.width = pct + '%';
        if (weightKg > 5500) {
            weightBar.className = 'progress-bar bg-danger';
        } else if (weightKg > 65) {
            weightBar.className = 'progress-bar bg-info';
        } else {
            weightBar.className = 'progress-bar bg-success';
        }
    }

    if (recVehicle) {
        if (weightKg <= 65) {
            recVehicle.className = 'badge bg-success text-light';
            recVehicle.textContent = 'Rapid Medical Drone (< 65 KG)';
        } else if (weightKg <= 5500) {
            recVehicle.className = 'badge bg-info text-dark';
            recVehicle.textContent = 'Heavy Cargo Helicopter (< 5,500 KG)';
        } else {
            recVehicle.className = 'badge bg-warning text-dark';
            recVehicle.textContent = 'Tactical Rescue Truck (< 8,500 KG)';
        }
    }
}

window.loadCargoPreset = function (type) {
    const presets = {
        'DRONE': {
            food: 10,
            water: 15,
            plasma: 25,
            location: 'Coimbatore Rapid Drone Pad',
            payload: 45.0,
            rations: 100
        },
        'TRUCK': {
            food: 500,
            water: 1000,
            plasma: 40,
            location: 'Trichy Central Logistics Depot',
            payload: 2800.0,
            rations: 5000
        },
        'CHINOOK': {
            food: 1200,
            water: 2500,
            plasma: 100,
            location: 'Chennai Air Command Station',
            payload: 5200.0,
            rations: 12000
        }
    };

    const p = presets[type];
    if (!p) return;

    document.getElementById('cargo-food').value = p.food;
    document.getElementById('cargo-water').value = p.water;
    document.getElementById('cargo-plasma').value = p.plasma;
    document.getElementById('cargo-location').value = p.location;
    document.getElementById('cargo-payload').value = p.payload;
    document.getElementById('cargo-rations').value = p.rations;
    document.getElementById('cargo-manifest-str').value = `FOOD,${p.food},WATER,${p.water},PLASMA,${p.plasma}`;

    updatePayloadMeter(p.payload);
    showToast('Cargo Kit Selected', `Configured payload for ${type} operations.`, 'info');
};

// =============================================================================
// TWO-DEVICE NETWORK SETTINGS & REPORT LINKS
// =============================================================================

function updateReportLinks() {
    const m = document.getElementById('link-report-manifests');
    if (m) m.href = apiUrl('/api/reports/manifests');
    const s = document.getElementById('link-report-supply');
    if (s) s.href = apiUrl('/api/reports/supply-drop-logs');
    const sum = document.getElementById('link-report-summary');
    if (sum) sum.href = apiUrl('/api/reports/summary');
}

function initNetworkSettings() {
    const inputBackend = document.getElementById('setting-backend-url');
    const btnSave = document.getElementById('btn-save-backend-url');
    const btnPing = document.getElementById('btn-ping-backend');
    const btnSimulate = document.getElementById('btn-simulate-disconnect');
    const btnReset = document.getElementById('btn-reset-backend-url');

    if (inputBackend && window.getApiBaseUrl) {
        inputBackend.value = window.getApiBaseUrl();
    }

    if (btnSave) {
        btnSave.addEventListener('click', () => {
            const val = inputBackend?.value?.trim();
            if (val && window.setApiBaseUrl) {
                window.setApiBaseUrl(val);
                updateReportLinks();
                showToast('Backend Configured', `Device 2 base URL set to: ${val}`, 'success');
                loadAllData();
            }
        });
    }

    if (btnReset) {
        btnReset.addEventListener('click', () => {
            if (window.resetApiBaseUrl) {
                window.resetApiBaseUrl();
                if (inputBackend) inputBackend.value = window.getApiBaseUrl();
                updateReportLinks();
                showToast('Reset Config', 'Reset Backend base URL to default configuration.', 'info');
                loadAllData();
            }
        });
    }

    if (btnPing) {
        btnPing.addEventListener('click', async () => {
            if (window.checkBackendHealth) {
                showToast('Ping', 'Pinging Device 2 /api/health...', 'info');
                const result = await window.checkBackendHealth();
                if (result.success) {
                    showToast('Connection Active', `Device 2 responded in ${result.latency} ms! Status: ${result.data?.status || 'UP'}`, 'success');
                } else {
                    showToast('Unreachable', `Cannot connect to Device 2. Error: ${result.error || 'Timed out'}`, 'danger');
                }
            }
        });
    }

    if (btnSimulate) {
        btnSimulate.addEventListener('click', () => {
            const fakeUrl = 'http://192.0.2.1:8080';
            if (window.setApiBaseUrl) {
                window.setApiBaseUrl(fakeUrl);
                if (inputBackend) inputBackend.value = fakeUrl;
                showToast('Disconnect Simulation', 'Switched to unreachable IP to simulate link drop.', 'warning');
            }
        });
    }
}

// =============================================================================
// REPORT IN-BROWSER VIEWER CONSOLE
// =============================================================================

window.viewReportInConsole = async function (reportType) {
    window.currentActiveReport = reportType;
    const bodyEl = document.getElementById('report-terminal-content');
    const titleEl = document.getElementById('terminal-doc-title');
    const countEl = document.getElementById('terminal-char-count');

    const meta = {
        'manifests': { url: '/api/reports/manifests', file: 'emergency_flight_manifests.txt', icon: 'fa-file-lines' },
        'supply': { url: '/api/reports/supply-drop-logs', file: 'supply_drop_logs.txt', icon: 'fa-parachute-box' },
        'summary': { url: '/api/reports/summary', file: 'dispatch_summary.txt', icon: 'fa-chart-pie' }
    };

    const target = meta[reportType] || meta.manifests;

    if (titleEl) {
        titleEl.innerHTML = `<i class="fa-solid ${target.icon} text-info"></i> ${target.file}`;
    }
    if (bodyEl) {
        bodyEl.textContent = `Fetching ${target.file} from Device 2 Backend...`;
    }

    try {
        const res = await apiFetch(target.url);
        if (res.ok) {
            const text = await res.text();
            if (bodyEl) bodyEl.textContent = text || '(Report file is empty)';
            if (countEl) countEl.textContent = `${text.length} chars`;
        } else {
            if (bodyEl) bodyEl.textContent = `HTTP ${res.status}: Failed to read report from ${target.url}`;
        }
    } catch (err) {
        if (bodyEl) bodyEl.textContent = `Network Error: ${err.message}\nEnsure Device 2 is connected.`;
    }
};

window.copyReportConsoleToClipboard = function () {
    const bodyEl = document.getElementById('report-terminal-content');
    if (bodyEl && bodyEl.textContent) {
        navigator.clipboard.writeText(bodyEl.textContent).then(() => {
            showToast('Copied', 'Report content copied to clipboard!', 'success');
        }).catch(err => {
            showToast('Copy Failed', err.message, 'danger');
        });
    }
};

// =============================================================================
// LOAD ALL SYSTEM DATA FROM DEVICE 2
// =============================================================================

window.loadAllData = function () {
    loadDashboardData();
    loadDisasterZones();
    loadCargoRecords();
    loadMissions();
    loadAuditConfirmations();
    loadFleetAssets();
    loadDispatchLogs();
};

// =============================================================================
// DASHBOARD TELEMETRY & CHARTS
// =============================================================================

async function loadDashboardData(silent = false) {
    try {
        const [statsRes, chartsRes] = await Promise.all([
            apiFetch('/api/dashboard/statistics'),
            apiFetch('/api/dashboard/charts')
        ]);

        if (statsRes.ok) {
            const stats = await statsRes.json();
            updateStatCounters(stats);
        }

        if (chartsRes.ok) {
            const charts = await chartsRes.json();
            renderCharts(charts);
        }

        loadRecentMissions();
    } catch (err) {
        if (!silent) console.warn('Could not load dashboard data from Device 2:', err);
    }
}

function updateStatCounters(stats) {
    setCount('stat-zones', stats.totalDisasterZones);
    setCount('stat-active', stats.activeMissions);
    setCount('stat-completed', stats.completedMissions);
    setCount('stat-coverage', `${stats.overallReliefCoveragePercentage?.toFixed(1) || 0.0}%`);
    setCount('stat-assets', stats.availableTransportAssets);
    setCount('stat-pending', stats.pendingRequests);
    setCount('stat-grounded', stats.groundedMissions);
    setCount('stat-aborted', stats.abortedMissions);
}

function setCount(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val !== undefined && val !== null ? val : '0';
}

function renderCharts(data) {
    if (!window.Chart) return;

    // 1. Disaster Zone Distribution (Doughnut)
    if (data.eventTypeDistribution) {
        const ctx = document.getElementById('chart-event-types');
        if (ctx) {
            if (chartEventTypes) chartEventTypes.destroy();
            chartEventTypes = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: Object.keys(data.eventTypeDistribution),
                    datasets: [{
                        data: Object.values(data.eventTypeDistribution),
                        backgroundColor: ['#06b6d4', '#3b82f6', '#f59e0b', '#ef4444'],
                        borderColor: '#0a0f1d',
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'right', labels: { color: '#94a3b8', font: { size: 11 } } }
                    }
                }
            });
        }
    }

    // 2. Transport Fleet Category Distribution (Bar)
    if (data.transportCategoryDistribution) {
        const ctx = document.getElementById('chart-transport-categories');
        if (ctx) {
            if (chartTransportCategories) chartTransportCategories.destroy();
            chartTransportCategories = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: Object.keys(data.transportCategoryDistribution).map(formatCategory),
                    datasets: [{
                        label: 'Fleet Units',
                        data: Object.values(data.transportCategoryDistribution),
                        backgroundColor: '#3b82f6',
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: { ticks: { color: '#94a3b8', font: { size: 10 } }, grid: { display: false } },
                        y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: 'rgba(255,255,255,0.06)' } }
                    },
                    plugins: { legend: { display: false } }
                }
            });
        }
    }

    // 3. Mission Priority Class Distribution (Bar)
    if (data.missionPriorityDistribution) {
        const ctx = document.getElementById('chart-mission-priorities');
        if (ctx) {
            if (chartMissionPriorities) chartMissionPriorities.destroy();
            chartMissionPriorities = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: Object.keys(data.missionPriorityDistribution).map(k => k.replace('PRIORITY_', '')),
                    datasets: [{
                        label: 'Missions Count',
                        data: Object.values(data.missionPriorityDistribution),
                        backgroundColor: ['#ef4444', '#f59e0b', '#3b82f6', '#94a3b8'],
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: { ticks: { color: '#94a3b8', font: { size: 11 } }, grid: { display: false } },
                        y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: 'rgba(255,255,255,0.06)' } }
                    },
                    plugins: { legend: { display: false } }
                }
            });
        }
    }

    // 4. Zone Relief Coverage Ratio (Horizontal Bar)
    if (data.zoneCoverages && data.zoneCoverages.length > 0) {
        const ctx = document.getElementById('chart-relief-coverage');
        if (ctx) {
            if (chartReliefCoverage) chartReliefCoverage.destroy();
            const topZones = data.zoneCoverages.slice(0, 6);
            chartReliefCoverage = new Chart(ctx, {
                type: 'bar',
                indexAxis: 'y',
                data: {
                    labels: topZones.map(z => z.zoneCode),
                    datasets: [{
                        label: 'Coverage Ratio (%)',
                        data: topZones.map(z => z.reliefCoverageRatio),
                        backgroundColor: '#10b981',
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: { min: 0, max: 100, ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.06)' } },
                        y: { ticks: { color: '#94a3b8', font: { size: 10 } }, grid: { display: false } }
                    },
                    plugins: { legend: { display: false } }
                }
            });
        }
    }
}

// =============================================================================
// MISSIONS TABLE & FILTERING
// =============================================================================

async function loadMissions() {
    try {
        const res = await apiFetch('/api/missions');
        if (res.ok) {
            rawMissions = await res.json();
            renderMissionsTable();
            populateAuditMissionDropdown(rawMissions);
        } else {
            renderMissionsError(`HTTP ${res.status}`);
        }
    } catch (err) {
        renderMissionsError(err.message);
    }
}

function renderMissionsError(msg) {
    const tbody = document.getElementById('missions-full-table-body');
    const base = window.getApiBaseUrl();
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-warning">
            <i class="fa-solid fa-triangle-exclamation"></i> Cannot reach Device 2 Backend at <code>${base}</code>.<br>
            <span class="small text-muted">${escapeHtml(msg || 'Connection error')}</span><br>
            <button class="btn btn-sm btn-outline-info mt-2" onclick="window.promptChangeBackendUrl()"><i class="fa-solid fa-gear"></i> Change IP</button>
            <button class="btn btn-sm btn-outline-secondary mt-2 ms-2" onclick="window.loadAllData()"><i class="fa-solid fa-rotate"></i> Retry</button>
        </td></tr>`;
    }
    populateAuditMissionDropdown([]);
}

async function loadRecentMissions() {
    try {
        const res = await apiFetch('/api/missions');
        if (res.ok) {
            const missions = await res.json();
            renderRecentMissions(missions.slice(0, 6));
        }
    } catch (err) {
        console.warn('Error fetching recent missions:', err);
    }
}

function renderRecentMissions(missions) {
    const tbody = document.getElementById('dashboard-missions-body');
    if (!tbody) return;

    if (!missions || missions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No missions registered yet in database.</td></tr>';
        return;
    }

    tbody.innerHTML = missions.map(m => `
        <tr>
            <td><code class="text-info">${m.missionCode || '--'}</code></td>
            <td><strong>${m.zoneCode || ('Zone #' + m.disasterZoneId)}</strong></td>
            <td><span class="font-monospace">${m.ddusScore ? m.ddusScore.toFixed(1) : '--'}</span></td>
            <td>${renderPriorityBadge(m.priorityLevel)}</td>
            <td>${m.assetCode ? `<span class="badge bg-dark border text-light">${m.assetCode}</span>` : '<span class="text-muted">None</span>'}</td>
            <td>${m.weatherValidationPassed ? '<span class="text-success"><i class="fa-solid fa-check"></i> Passed</span>' : '<span class="text-danger"><i class="fa-solid fa-xmark"></i> Failed</span>'}</td>
            <td>${renderStatusBadge(m.status)}</td>
        </tr>
    `).join('');
}

window.filterMissionsByStatus = function (status) {
    activeMissionFilter = status;
    const btnGroup = document.querySelectorAll('#tab-missions .btn-group button');
    btnGroup.forEach(btn => {
        if (btn.textContent.trim().toUpperCase() === status || (status === 'ALL' && btn.textContent.trim() === 'All')) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    renderMissionsTable();
};

function renderMissionsTable() {
    const tbody = document.getElementById('missions-full-table-body');
    if (!tbody) return;

    const searchTerm = (document.getElementById('missions-search-input')?.value || '').toLowerCase().trim();

    let filtered = rawMissions;
    if (activeMissionFilter !== 'ALL') {
        filtered = filtered.filter(m => m.status === activeMissionFilter);
    }

    if (searchTerm) {
        filtered = filtered.filter(m =>
            (m.missionCode && m.missionCode.toLowerCase().includes(searchTerm)) ||
            (m.zoneCode && m.zoneCode.toLowerCase().includes(searchTerm)) ||
            (m.priorityLevel && m.priorityLevel.toLowerCase().includes(searchTerm)) ||
            (m.status && m.status.toLowerCase().includes(searchTerm))
        );
    }

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center py-4 text-muted">No missions match current filter criteria.</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(m => `
        <tr>
            <td><code class="text-info">${m.missionCode || '--'}</code></td>
            <td><strong>${m.zoneCode || ('Zone #' + m.disasterZoneId)}</strong></td>
            <td><span class="font-monospace">${m.ddusScore ? m.ddusScore.toFixed(1) : '--'}</span></td>
            <td>${renderPriorityBadge(m.priorityLevel)}</td>
            <td><span class="small">${formatCategory(m.transportCategory) || '--'}</span></td>
            <td>${m.transportSuitabilityScore ? `<span class="badge bg-secondary">${m.transportSuitabilityScore.toFixed(1)}</span>` : '--'}</td>
            <td>${m.estimatedDurationMin ? `${m.estimatedDurationMin} min` : '--'}</td>
            <td>${m.weatherValidationPassed ? '<span class="text-success"><i class="fa-solid fa-check"></i> Safe</span>' : '<span class="text-danger"><i class="fa-solid fa-xmark"></i> Hazard</span>'}</td>
            <td>${renderStatusBadge(m.status)}</td>
            <td>
                ${m.status === 'DISPATCHED' ? `
                    <button class="btn btn-sm btn-outline-info py-0 px-2" onclick="window.markMissionTransit(${m.id})" title="Launch mission">
                        <i class="fa-solid fa-plane"></i> Transit
                    </button>
                ` : m.status === 'IN_TRANSIT' ? `
                    <button class="btn btn-sm btn-outline-success py-0 px-2" onclick="window.jumpToAudit(${m.id})" title="Perform Field Audit">
                        <i class="fa-solid fa-clipboard-check"></i> Audit
                    </button>
                ` : `
                    <span class="text-muted small">--</span>
                `}
            </td>
        </tr>
    `).join('');
}

window.markMissionTransit = function (missionId) {
    showToast('Mission Transit', `Mission #${missionId} airborne and navigating to target coordinates.`, 'info');
    setTimeout(() => {
        window.jumpToAudit(missionId);
    }, 600);
};

window.jumpToAudit = function (missionId) {
    const auditTabLink = document.querySelector('a[data-tab="tab-audit"]');
    if (auditTabLink) auditTabLink.click();

    const select = document.getElementById('audit-mission-id');
    if (select) {
        select.value = missionId;
        select.dispatchEvent(new Event('change'));
    }
};

// =============================================================================
// DISASTER ZONES TABLE & DROPDOWNS
// =============================================================================

async function loadDisasterZones() {
    try {
        const res = await apiFetch('/api/disasters');
        if (res.ok) {
            rawZones = await res.json();
            renderZonesTable();
            populateZoneDropdowns(rawZones);
        } else {
            renderZonesError(`HTTP ${res.status}`);
        }
    } catch (err) {
        renderZonesError(err.message);
    }
}

function renderZonesError(msg) {
    const tbody = document.getElementById('zones-table-body');
    const base = window.getApiBaseUrl();
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-warning">
            <i class="fa-solid fa-triangle-exclamation"></i> Cannot reach Device 2 Backend at <code>${base}</code>.<br>
            <span class="small text-muted">${escapeHtml(msg || 'Connection error')}</span><br>
            <button class="btn btn-sm btn-outline-info mt-2" onclick="window.promptChangeBackendUrl()"><i class="fa-solid fa-gear"></i> Change IP</button>
            <button class="btn btn-sm btn-outline-secondary mt-2 ms-2" onclick="window.loadAllData()"><i class="fa-solid fa-rotate"></i> Retry</button>
        </td></tr>`;
    }
    populateZoneDropdowns([]);
}

function renderZonesTable() {
    const tbody = document.getElementById('zones-table-body');
    if (!tbody) return;

    const searchTerm = (document.getElementById('zones-search-input')?.value || '').toLowerCase().trim();

    let filtered = rawZones;
    if (searchTerm) {
        filtered = filtered.filter(z =>
            (z.zoneCode && z.zoneCode.toLowerCase().includes(searchTerm)) ||
            (z.zoneName && z.zoneName.toLowerCase().includes(searchTerm)) ||
            (z.eventType && z.eventType.toLowerCase().includes(searchTerm))
        );
    }

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">No disaster zones registered yet.</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(z => `
        <tr>
            <td><code class="text-info">${z.zoneCode}</code></td>
            <td><strong>${z.zoneName}</strong></td>
            <td><span class="badge bg-secondary">${z.eventType}</span></td>
            <td>${z.affectedPopulation?.toLocaleString() || 0}</td>
            <td><span class="badge bg-danger">${z.medicalUrgencyScore}</span></td>
            <td>${z.terrainRisk}</td>
            <td>${z.weatherSeverityIndex}</td>
            <td>${z.flightDistanceKm} km</td>
            <td>
                <button class="btn btn-sm btn-outline-info py-0 px-2" onclick="window.dispatchSpecificZone(${z.id})">
                    <i class="fa-solid fa-paper-plane"></i> Dispatch
                </button>
            </td>
        </tr>
    `).join('');
}

window.dispatchSpecificZone = async function (zoneId) {
    const dispatchTab = document.querySelector('a[data-tab="tab-dispatch"]');
    if (dispatchTab) dispatchTab.click();

    const select = document.getElementById('dispatch-select-zone');
    if (select) {
        select.value = zoneId;
        const btn = document.getElementById('btn-trigger-dispatch');
        if (btn) btn.click();
    }
};

function populateZoneDropdowns(zones) {
    const cargoSelect = document.getElementById('cargo-zone-id');
    const dispatchSelect = document.getElementById('dispatch-select-zone');

    if (!zones || zones.length === 0) {
        const msg = '<option value="">-- No Zones Available (Backend Unreachable) --</option>';
        if (cargoSelect) cargoSelect.innerHTML = msg;
        if (dispatchSelect) dispatchSelect.innerHTML = msg;
        return;
    }

    const options = zones.map(z => `<option value="${z.id}">${z.zoneCode} - ${z.zoneName} (${z.eventType})</option>`).join('');

    if (cargoSelect) {
        cargoSelect.innerHTML = '<option value="">-- Choose Disaster Zone --</option>' + options;
    }
    if (dispatchSelect) {
        dispatchSelect.innerHTML = '<option value="">-- Choose Disaster Zone --</option>' + options;
    }
}

// =============================================================================
// CARGO INGESTION RECORDS
// =============================================================================

async function loadCargoRecords() {
    try {
        const res = await apiFetch('/api/cargo');
        if (res.ok) {
            rawCargo = await res.json();
            renderCargoTable(rawCargo);
        } else {
            renderCargoError(`HTTP ${res.status}`);
        }
    } catch (err) {
        renderCargoError(err.message);
    }
}

function renderCargoError(msg) {
    const tbody = document.getElementById('cargo-table-body');
    const base = window.getApiBaseUrl();
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-warning">
            <i class="fa-solid fa-triangle-exclamation"></i> Cannot reach Device 2 Backend at <code>${base}</code>.<br>
            <span class="small text-muted">${escapeHtml(msg || 'Connection error')}</span><br>
            <button class="btn btn-sm btn-outline-info mt-2" onclick="window.promptChangeBackendUrl()"><i class="fa-solid fa-gear"></i> Change IP</button>
            <button class="btn btn-sm btn-outline-secondary mt-2 ms-2" onclick="window.loadAllData()"><i class="fa-solid fa-rotate"></i> Retry</button>
        </td></tr>`;
    }
}

function renderCargoTable(cargoList) {
    const tbody = document.getElementById('cargo-table-body');
    if (!tbody) return;

    if (!cargoList || cargoList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No cargo records registered yet.</td></tr>';
        return;
    }

    tbody.innerHTML = cargoList.map(c => `
        <tr>
            <td><code class="text-info">CRG-${String(c.id).padStart(4, '0')}</code></td>
            <td>Zone #${c.disasterZoneId}</td>
            <td>${c.foodKitCount} kits</td>
            <td>${c.waterKitCount} kits</td>
            <td>${c.plasmaKitCount} kits</td>
            <td><strong>${c.totalPayloadKg} kg</strong></td>
            <td>${c.requiredRations}</td>
            <td><code class="small text-secondary">${c.cargoManifest}</code></td>
        </tr>
    `).join('');
}

// =============================================================================
// TRANSPORT FLEET MANAGEMENT & STATUS TOGGLES
// =============================================================================

async function loadFleetAssets() {
    try {
        const res = await apiFetch('/api/assets');
        if (res.ok) {
            rawAssets = await res.json();
            renderFleetCards(rawAssets);
            renderFleetTable(rawAssets);
        }
    } catch (err) {
        console.warn('Error fetching assets:', err);
    }
}

function renderFleetCards(assets) {
    const container = document.getElementById('fleet-cards-container');
    if (!container) return;

    const icons = {
        'HEAVY_CARGO_HELICOPTER': { icon: 'fa-helicopter', color: 'icon-cyan' },
        'ALL_TERRAIN_RESCUE_TRUCK': { icon: 'fa-truck-monster', color: 'icon-amber' },
        'EMERGENCY_MEDICAL_DRONE': { icon: 'fa-paper-plane', color: 'icon-green' }
    };

    container.innerHTML = assets.map(a => {
        const meta = icons[a.category] || { icon: 'fa-plane', color: 'icon-blue' };
        const statusClass = a.status === 'AVAILABLE' ? 'bg-success text-light' : a.status === 'OCCUPIED' ? 'bg-warning text-dark' : 'bg-danger text-light';

        return `
            <div class="col-xl-4 col-md-6">
                <div class="fleet-card">
                    <div>
                        <div class="fleet-header">
                            <div class="fleet-icon-box ${meta.color}">
                                <i class="fa-solid ${meta.icon}"></i>
                            </div>
                            <span class="badge ${statusClass}">${a.status}</span>
                        </div>
                        <h5 class="text-light mb-1" style="font-size: 15px;">${a.assetName}</h5>
                        <p class="font-monospace text-info small mb-3">${a.assetCode} | ${formatCategory(a.category)}</p>

                        <div class="fleet-stat-row">
                            <span>Max Payload Capacity:</span>
                            <strong class="text-light">${a.maxPayloadKg.toLocaleString()} KG</strong>
                        </div>
                        <div class="fleet-stat-row">
                            <span>Operating Range:</span>
                            <strong class="text-light">${a.maxRangeKm} KM</strong>
                        </div>
                        <div class="fleet-stat-row">
                            <span>Weather Severity Limit:</span>
                            <strong class="text-light">&le; ${a.weatherRiskLimit}</strong>
                        </div>
                        <div class="fleet-stat-row">
                            <span>Current Staging Base:</span>
                            <span class="text-muted">${a.currentLocation || 'Base Station'}</span>
                        </div>
                    </div>

                    <div class="mt-3 pt-2 border-top border-secondary d-flex justify-content-between align-items-center">
                        <span class="small text-muted">Vehicle State:</span>
                        <button class="btn btn-sm ${a.status === 'AVAILABLE' ? 'btn-outline-warning' : 'btn-outline-success'} py-0 px-2"
                                onclick="window.toggleAssetStatus(${a.id}, '${a.status}')">
                            <i class="fa-solid fa-sliders"></i> ${a.status === 'AVAILABLE' ? 'Set Maintenance' : 'Set Available'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderFleetTable(assets) {
    const tbody = document.getElementById('fleet-table-body');
    if (!tbody) return;

    if (!assets || assets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">No transport fleet assets found in database.</td></tr>';
        return;
    }

    tbody.innerHTML = assets.map(a => `
        <tr>
            <td><code class="text-info">${a.assetCode}</code></td>
            <td><strong>${a.assetName}</strong></td>
            <td><span class="badge bg-secondary">${formatCategory(a.category)}</span></td>
            <td>${a.maxPayloadKg.toLocaleString()} kg</td>
            <td>${a.maxRangeKm} km</td>
            <td>&le; ${a.weatherRiskLimit}</td>
            <td>${a.currentLocation}</td>
            <td>${renderAssetStatusBadge(a.status)}</td>
            <td>
                <button class="btn btn-sm btn-outline-info py-0 px-2" onclick="window.toggleAssetStatus(${a.id}, '${a.status}')">
                    Toggle Status
                </button>
            </td>
        </tr>
    `).join('');
}

window.toggleAssetStatus = async function (assetId, currentStatus) {
    const nextStatus = currentStatus === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    try {
        const res = await apiFetch(`/api/assets/${assetId}?status=${nextStatus}`, { method: 'PUT' });
        if (res.ok) {
            showToast('Asset Updated', `Asset #${assetId} status set to: ${nextStatus}`, 'success');
            loadFleetAssets();
            loadDashboardData(true);
        } else {
            showToast('Update Failed', `HTTP ${res.status}: Could not update asset status.`, 'danger');
        }
    } catch (err) {
        showToast('Network Error', err.message, 'danger');
    }
};

// =============================================================================
// PHASE 3: FIELD DELIVERY AUDIT
// =============================================================================

function populateAuditMissionDropdown(missions) {
    const select = document.getElementById('audit-mission-id');
    if (!select) return;

    if (!missions || missions.length === 0) {
        select.innerHTML = '<option value="">-- No Missions Available (Backend Unreachable) --</option>';
        return;
    }

    // Filter missions that can be audited (DISPATCHED, IN_TRANSIT, or COMPLETED)
    const auditable = missions.filter(m => m.status === 'DISPATCHED' || m.status === 'IN_TRANSIT' || m.status === 'COMPLETED');
    const listToUse = auditable.length > 0 ? auditable : missions;

    select.innerHTML = '<option value="">-- Choose Assigned Mission --</option>' +
        listToUse.map(m => `<option value="${m.id}" data-zone="${m.zoneCode || m.disasterZoneId}" data-duration="${m.estimatedDurationMin || 60}">
            ${m.missionCode} - Zone: ${m.zoneCode || m.disasterZoneId} [${m.status}]
        </option>`).join('');

    select.addEventListener('change', () => {
        const selected = select.options[select.selectedIndex];
        if (selected && selected.value) {
            const dur = selected.getAttribute('data-duration');
            const durInput = document.getElementById('audit-duration');
            if (durInput && dur) durInput.value = dur;
        }
    });
}

window.autoFillAuditPerfect = function () {
    const durInput = document.getElementById('audit-duration');
    const precInput = document.getElementById('audit-precision');
    const offInput = document.getElementById('audit-officer');
    const ratInput = document.getElementById('audit-rations');
    const notesInput = document.getElementById('audit-notes');

    if (durInput) durInput.value = '42';
    if (precInput) precInput.value = '1.2';
    if (offInput) offInput.value = 'Commander Rajesh Kumar, NDRF 4th Bn';
    if (ratInput) ratInput.value = '5000';
    if (notesInput) notesInput.value = 'Field confirmation verified. Critical cargo delivered safely at designated GPS coordinates.';

    showToast('Auto-Filled', 'Pre-filled 100% successful field drop audit parameters.', 'info');
};

async function loadAuditConfirmations() {
    try {
        const res = await apiFetch('/api/missions/confirmations/all');
        if (res.ok) {
            const list = await res.json();
            renderConfirmationsTable(list);
        }
    } catch (err) {
        console.warn('Error fetching confirmations:', err);
    }
}

function renderConfirmationsTable(list) {
    const tbody = document.getElementById('confirmations-table-body');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">No field confirmations recorded yet in database.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(c => `
        <tr>
            <td><code class="text-info">AUD-${String(c.id).padStart(4, '0')}</code></td>
            <td><code class="text-light">${c.missionCode || ('Mission #' + c.missionId)}</code></td>
            <td><strong>${c.zoneCode || '--'}</strong></td>
            <td>${c.flightTransitDurationMin} min</td>
            <td><span class="badge ${c.dropOffPrecisionMeters <= 5 ? 'bg-success' : 'bg-warning'}">${c.dropOffPrecisionMeters} m</span></td>
            <td>${c.deliveredRations?.toLocaleString() || 0}</td>
            <td><span class="small">${c.fieldOfficerName}</span></td>
            <td>${c.deliveryVerified ? '<span class="text-success"><i class="fa-solid fa-check-circle"></i> Yes</span>' : '<span class="text-danger">No</span>'}</td>
            <td><span class="text-muted small">${c.fieldConfirmationTimestamp ? c.fieldConfirmationTimestamp.replace('T', ' ') : '--'}</span></td>
        </tr>
    `).join('');
}

// =============================================================================
// DISPATCH ENGINE AUDIT LOGS
// =============================================================================

async function loadDispatchLogs() {
    try {
        const res = await apiFetch('/api/reports/dispatch-logs?limit=50');
        if (res.ok) {
            const logs = await res.json();
            renderDispatchLogs(logs);
        }
    } catch (err) {
        console.warn('Error fetching dispatch logs:', err);
    }
}

function renderDispatchLogs(logs) {
    const tbody = document.getElementById('logs-table-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No multithreaded dispatch logs generated yet.</td></tr>';
        return;
    }

    tbody.innerHTML = logs.map(l => {
        const levelBadge = l.logLevel === 'INFO' ? 'bg-info text-dark' : l.logLevel === 'WARN' ? 'bg-warning text-dark' : 'bg-danger text-light';
        return `
            <tr>
                <td><span class="font-monospace text-muted small">${l.timestamp ? l.timestamp.replace('T', ' ') : '--'}</span></td>
                <td><code class="text-cyan small">${l.threadName}</code></td>
                <td><span class="font-monospace text-secondary small">${l.requestId}</span></td>
                <td><span class="badge ${levelBadge} py-0">${l.logLevel}</span></td>
                <td><strong>${l.action}</strong></td>
                <td><span class="small text-light">${escapeHtml(l.message)}</span></td>
            </tr>
        `;
    }).join('');
}

// =============================================================================
// FORM SUBMISSIONS & EVENT HANDLERS
// =============================================================================

function initEventHandlers() {
    // 1. Disaster Zone Form
    const formDisaster = document.getElementById('form-disaster-intake');
    if (formDisaster) {
        formDisaster.addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                zoneCode: document.getElementById('intake-zone-code').value.trim(),
                zoneName: document.getElementById('intake-zone-name').value.trim(),
                eventType: document.getElementById('intake-event-type').value,
                affectedPopulation: parseInt(document.getElementById('intake-population').value),
                medicalUrgencyScore: parseFloat(document.getElementById('intake-urgency').value),
                terrainRisk: parseFloat(document.getElementById('intake-terrain').value),
                roadAccessibility: parseFloat(document.getElementById('intake-road').value),
                weatherSeverityIndex: parseFloat(document.getElementById('intake-weather').value),
                flightDistanceKm: parseFloat(document.getElementById('intake-distance').value)
            };

            try {
                const res = await apiFetch('/api/disasters', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                if (res.ok) {
                    const saved = await res.json();
                    showToast('Zone Registered', `Successfully added ${saved.zoneCode} (${saved.zoneName}) to database!`, 'success');
                    formDisaster.reset();
                    updateUrgencyEstimator();
                    loadDisasterZones();
                    loadDashboardData(true);
                } else {
                    const err = await res.json();
                    showToast('Registration Error', err.message || 'Validation failed on Device 2.', 'danger');
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            }
        });
    }

    // 2. Cargo Ingestion Form
    const formCargo = document.getElementById('form-cargo-ingestion');
    if (formCargo) {
        formCargo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const zoneVal = document.getElementById('cargo-zone-id').value;
            if (!zoneVal) {
                showToast('Zone Required', 'Please select a disaster zone first.', 'warning');
                return;
            }

            const body = {
                disasterZoneId: parseInt(zoneVal),
                foodKitCount: parseInt(document.getElementById('cargo-food').value),
                waterKitCount: parseInt(document.getElementById('cargo-water').value),
                plasmaKitCount: parseInt(document.getElementById('cargo-plasma').value),
                totalPayloadKg: parseFloat(document.getElementById('cargo-payload').value),
                requiredRations: parseInt(document.getElementById('cargo-rations').value),
                dispatchLocation: document.getElementById('cargo-location').value.trim(),
                cargoManifest: document.getElementById('cargo-manifest-str').value.trim()
            };

            try {
                const res = await apiFetch('/api/cargo', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                if (res.ok) {
                    const saved = await res.json();
                    showToast('Cargo Staged', `Cargo #${saved.id} successfully staged in database!`, 'success');
                    loadCargoRecords();
                    loadDashboardData(true);
                } else {
                    const err = await res.json();
                    showToast('Cargo Error', err.message || 'Failed to stage cargo.', 'danger');
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            }
        });
    }

    // 3. Single Zone Dispatch
    const btnSingle = document.getElementById('btn-trigger-dispatch');
    if (btnSingle) {
        btnSingle.addEventListener('click', async () => {
            const zoneId = document.getElementById('dispatch-select-zone').value;
            if (!zoneId) {
                showToast('Selection Required', 'Please choose a disaster zone to dispatch.', 'warning');
                return;
            }

            animateWorkerChips(true);
            try {
                const res = await apiFetch(`/api/dispatch/${zoneId}`, { method: 'POST' });
                if (res.ok) {
                    const r = await res.json();
                    displayDispatchOutcome(r);
                    showToast('Dispatch Complete', `Mission ${r.missionCode || 'Created'} processed: ${r.status}`, r.status === 'DISPATCHED' ? 'success' : 'warning');
                    loadMissions();
                    loadFleetAssets();
                    loadDispatchLogs();
                    loadDashboardData(true);
                } else {
                    showToast('Dispatch Failed', `HTTP ${res.status}: Backend dispatch worker failed.`, 'danger');
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            } finally {
                animateWorkerChips(false);
            }
        });
    }

    // 4. Bulk Multi-Threaded Dispatch
    const btnBulk = document.getElementById('btn-bulk-concurrent');
    if (btnBulk) {
        btnBulk.addEventListener('click', async () => {
            animateWorkerChips(true);
            showToast('Concurrent Dispatch', 'Firing 8 parallel worker threads on Device 2...', 'info');

            try {
                // Get all active disaster zones
                const zonesRes = await apiFetch('/api/disasters');
                const zones = await zonesRes.json();
                const ids = zones.map(z => z.id);

                const res = await apiFetch('/api/dispatch/bulk', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(ids)
                });

                if (res.ok) {
                    const results = await res.json();
                    const dispatched = results.filter(r => r.status === 'DISPATCHED').length;
                    const grounded = results.filter(r => r.status === 'FLIGHT_GROUNDED').length;
                    const aborted = results.filter(r => r.status === 'ABORTED').length;

                    showToast('Bulk Execution Finished', `Dispatched: ${dispatched} | Grounded: ${grounded} | Aborted: ${aborted}`, 'success');
                    loadMissions();
                    loadFleetAssets();
                    loadDispatchLogs();
                    loadDashboardData(true);
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            } finally {
                animateWorkerChips(false);
            }
        });
    }

    // 5. Field Delivery Audit Form
    const formAudit = document.getElementById('form-delivery-audit');
    if (formAudit) {
        formAudit.addEventListener('submit', async (e) => {
            e.preventDefault();
            const missionId = document.getElementById('audit-mission-id').value;
            if (!missionId) {
                showToast('Selection Required', 'Please select a mission to audit.', 'warning');
                return;
            }

            const body = {
                flightTransitDurationMin: parseInt(document.getElementById('audit-duration').value),
                dropOffPrecisionMeters: parseFloat(document.getElementById('audit-precision').value),
                fieldOfficerName: document.getElementById('audit-officer').value.trim(),
                deliveredRations: parseInt(document.getElementById('audit-rations').value),
                deliveryNotes: document.getElementById('audit-notes').value.trim(),
                deliveryVerified: document.getElementById('audit-verified').checked
            };

            try {
                const res = await apiFetch(`/api/missions/${missionId}/audit`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                if (res.ok) {
                    const saved = await res.json();
                    showToast('Audit Recorded', `Delivery confirmed for Mission #${saved.missionId}! Relief coverage updated.`, 'success');
                    formAudit.reset();
                    loadAuditConfirmations();
                    loadMissions();
                    loadFleetAssets();
                    loadDashboardData(true);
                } else {
                    const err = await res.json();
                    showToast('Audit Error', err.message || 'Failed to submit audit.', 'danger');
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            }
        });
    }

    // 6. Bulk Ingest CSV
    const btnCsv = document.getElementById('btn-quick-import');
    if (btnCsv) {
        btnCsv.addEventListener('click', async () => {
            showToast('Ingesting CSV', 'Reading data/relief_requests.csv into database...', 'info');
            try {
                const res = await apiFetch('/api/import/csv', { method: 'POST' });
                if (res.ok) {
                    const r = await res.json();
                    showToast('CSV Ingested', `Processed: ${r.recordsProcessed} | Created: ${r.recordsCreated} | Skipped: ${r.recordsSkipped}`, 'success');
                    loadAllData();
                } else {
                    showToast('CSV Import Error', `HTTP ${res.status}: Failed to import CSV.`, 'danger');
                }
            } catch (err) {
                showToast('Network Error', err.message, 'danger');
            }
        });
    }

    // 7. Refresh Telemetry Button
    const btnRefresh = document.getElementById('btn-refresh-data');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            showToast('Refreshing', 'Fetching latest database telemetry from Device 2...', 'info');
            loadAllData();
        });
    }

    // 8. Search Filter Handlers
    const zonesSearch = document.getElementById('zones-search-input');
    if (zonesSearch) zonesSearch.addEventListener('input', renderZonesTable);

    const missionsSearch = document.getElementById('missions-search-input');
    if (missionsSearch) missionsSearch.addEventListener('input', renderMissionsTable);

    const dashMissionsSearch = document.getElementById('dashboard-search-mission');
    if (dashMissionsSearch) dashMissionsSearch.addEventListener('input', () => {
        const val = dashMissionsSearch.value.toLowerCase().trim();
        const filtered = rawMissions.filter(m =>
            (m.missionCode && m.missionCode.toLowerCase().includes(val)) ||
            (m.zoneCode && m.zoneCode.toLowerCase().includes(val)) ||
            (m.status && m.status.toLowerCase().includes(val))
        );
        renderRecentMissions(filtered.slice(0, 6));
    });
}

// Animate worker thread chips
function animateWorkerChips(active) {
    const chips = document.querySelectorAll('.thread-chip');
    const poolStatus = document.getElementById('thread-pool-status');

    if (active) {
        if (poolStatus) {
            poolStatus.className = 'badge bg-warning-subtle text-warning border border-warning small';
            poolStatus.textContent = 'Processing Tasks...';
        }
        chips.forEach((c, idx) => {
            setTimeout(() => c.classList.add('active'), idx * 80);
        });
    } else {
        if (poolStatus) {
            poolStatus.className = 'badge bg-success-subtle text-success border border-success small';
            poolStatus.textContent = 'Ready';
        }
        setTimeout(() => {
            chips.forEach(c => c.classList.remove('active'));
        }, 1200);
    }
}

function displayDispatchOutcome(r) {
    const card = document.getElementById('dispatch-evaluation-result');
    if (!card) return;

    card.style.display = 'block';
    document.getElementById('eval-code').textContent = r.missionCode || '--';
    document.getElementById('eval-ddus').textContent = r.ddusScore ? r.ddusScore.toFixed(1) : '--';
    document.getElementById('eval-priority').innerHTML = renderPriorityBadge(r.priorityLevel);
    document.getElementById('eval-transport').textContent = r.assignedTransportAssetCode ? `${r.assignedTransportAssetCode} (${formatCategory(r.assignedTransportCategory)})` : 'None Allocated';
    document.getElementById('eval-suitability').textContent = r.transportSuitabilityScore ? `${r.transportSuitabilityScore.toFixed(1)} / 100` : '--';
    document.getElementById('eval-status').innerHTML = renderStatusBadge(r.status);
    document.getElementById('eval-msg').textContent = r.rejectionReason || 'Mission cleared and queued for deployment.';
}

// =============================================================================
// FORMATTING HELPERS
// =============================================================================

function renderPriorityBadge(priority) {
    if (!priority) return '<span class="badge-custom badge-aborted">N/A</span>';
    if (priority.includes('ALPHA')) return '<span class="badge-custom badge-alpha">PRIORITY ALPHA</span>';
    if (priority.includes('BRAVO')) return '<span class="badge-custom badge-bravo">PRIORITY BRAVO</span>';
    if (priority.includes('CHARLIE')) return '<span class="badge-custom badge-charlie">PRIORITY CHARLIE</span>';
    return '<span class="badge-custom badge-aborted">ABORTED</span>';
}

function renderStatusBadge(status) {
    if (!status) return '<span class="badge-custom badge-aborted">UNKNOWN</span>';
    if (status === 'COMPLETED') return '<span class="badge-custom badge-completed">COMPLETED</span>';
    if (status === 'DISPATCHED') return '<span class="badge-custom badge-dispatched">DISPATCHED</span>';
    if (status === 'IN_TRANSIT') return '<span class="badge-custom badge-transit">IN TRANSIT</span>';
    if (status === 'FLIGHT_GROUNDED') return '<span class="badge-custom badge-grounded">GROUNDED</span>';
    if (status === 'ABORTED') return '<span class="badge-custom badge-aborted">ABORTED</span>';
    return `<span class="badge-custom badge-aborted">${status}</span>`;
}

function renderAssetStatusBadge(status) {
    if (status === 'AVAILABLE') return '<span class="badge bg-success">AVAILABLE</span>';
    if (status === 'OCCUPIED') return '<span class="badge bg-warning text-dark">OCCUPIED</span>';
    return '<span class="badge bg-danger">MAINTENANCE</span>';
}

function formatCategory(cat) {
    if (!cat) return '';
    return cat.replace(/_/g, ' ');
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
