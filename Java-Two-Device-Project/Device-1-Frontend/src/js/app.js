/**
 * DREMLDS Command Center Front-End Engine (Device 1 - Client)
 * Handles real-time telemetry, Chart.js visualizations, and asynchronous REST APIs.
 * Connects to Device 2 Backend over Local Wi-Fi / LAN via window.apiUrl().
 */

function apiUrl(path) {
    if (typeof window.apiUrl === 'function') {
        return window.apiUrl(path);
    }
    return path;
}

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initZoneCodeValidation();
    initCargoCalculator();
    initEventHandlers();
    initNetworkSettings();
    updateReportLinks();
    loadAllData();
});

// Chart instances
let chartEventTypes = null;
let chartTransportCategories = null;
let chartMissionPriorities = null;
let chartReliefCoverage = null;

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
        'tab-reports': ['Operational Reports & Analytics', 'Exportable flight manifests, supply drop logs, and execution audit trails'],
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

            // Refresh charts when returning to dashboard
            if (targetTab === 'tab-dashboard') {
                loadDashboardData();
            }
        });
    });
}

// =============================================================================
// ZONE CODE REGEX VALIDATION (ZONE-[A-Z]{2}-\d{4})
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

// Auto-sync cargo manifest from kit counts
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
        // Approx payload calculation: Food=2kg, Water=1.5kg, Plasma=0.5kg
        if (payload) payload.value = Math.round((f * 2.0 + w * 1.5 + p * 0.5) * 10) / 10;
        if (rations) rations.value = Math.max(100, f * 5 + w * 2);
    }

    [food, water, plasma].forEach(el => {
        if (el) el.addEventListener('input', updateManifest);
    });
}

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

    btnSave?.addEventListener('click', () => {
        if (!inputBackend || !inputBackend.value) return;
        window.setApiBaseUrl(inputBackend.value);
        updateReportLinks();
        loadAllData();
        showAlert(`Device 2 Backend URL updated to: ${inputBackend.value}`, 'info');
    });

    btnReset?.addEventListener('click', () => {
        window.resetApiBaseUrl();
        if (inputBackend && window.getApiBaseUrl) {
            inputBackend.value = window.getApiBaseUrl();
        }
        updateReportLinks();
        loadAllData();
        showAlert('Reset backend URL to default configuration.', 'info');
    });

    btnPing?.addEventListener('click', async () => {
        showAlert('Pinging Device 2 Backend health check...', 'info');
        const res = await window.checkBackendHealth();
        if (res.success) {
            showAlert(`Device 2 Online! Latency: ${res.latency} ms | Database: ${res.data?.databaseStatus || 'OK'}`, 'success');
        } else {
            showAlert(`Device 2 Unreachable! Check Wi-Fi or firewall: ${res.error || res.status}`, 'danger');
        }
    });

    btnSimulate?.addEventListener('click', () => {
        const originalUrl = window.getApiBaseUrl();
        showAlert('Simulating Device 2 disconnection...', 'warning');
        // Point temporarily to a dead port
        window.setApiBaseUrl('http://192.168.254.254:9999');
        setTimeout(() => {
            const banner = document.getElementById('backend-disconnected-banner');
            if (banner) banner.scrollIntoView({ behavior: 'smooth' });
            // Revert after 6 seconds
            setTimeout(() => {
                showAlert('Restoring active Device 2 backend connection...', 'info');
                window.setApiBaseUrl(originalUrl);
                if (inputBackend) inputBackend.value = originalUrl;
                loadAllData();
            }, 6000);
        }, 1500);
    });
}

// =============================================================================
// DATA FETCHING & UI RENDERING
// =============================================================================

function loadAllData() {
    loadDashboardData();
    loadZones();
    loadCargo();
    loadMissions();
    loadConfirmations();
    loadFleet();
    loadLogs();
    updateReportLinks();
}

async function loadDashboardData() {
    try {
        const [statsRes, chartsRes] = await Promise.all([
            fetch(apiUrl('/api/dashboard/statistics')),
            fetch(apiUrl('/api/dashboard/charts'))
        ]);

        if (statsRes.ok) {
            const stats = await statsRes.json();
            document.getElementById('stat-zones').textContent = stats.totalDisasterZones;
            document.getElementById('stat-active').textContent = stats.activeMissions;
            document.getElementById('stat-completed').textContent = stats.completedMissions;
            document.getElementById('stat-coverage').textContent = (stats.overallReliefCoveragePercentage || 0).toFixed(1) + '%';
            document.getElementById('stat-assets').textContent = stats.availableTransportAssets;
            document.getElementById('stat-pending').textContent = stats.pendingRequests;
            document.getElementById('stat-grounded').textContent = stats.groundedMissions;
            document.getElementById('stat-aborted').textContent = stats.abortedMissions;
        }

        if (chartsRes.ok) {
            const chartsData = await chartsRes.json();
            renderCharts(chartsData);
        }
    } catch (err) {
        console.error('Failed to load dashboard data:', err);
    }
}

function renderCharts(data) {
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded. Skipping chart rendering.');
        return;
    }

    // 1. Disaster Zone Distribution (Doughnut)
    const ctxEvents = document.getElementById('chart-event-types');
    if (ctxEvents) {
        const labels = Object.keys(data.eventTypeDistribution || {});
        const values = Object.values(data.eventTypeDistribution || {});

        if (chartEventTypes) chartEventTypes.destroy();
        chartEventTypes = new Chart(ctxEvents, {
            type: 'doughnut',
            data: {
                labels: labels.length ? labels : ['None'],
                datasets: [{
                    data: values.length ? values : [1],
                    backgroundColor: ['#3b82f6', '#06b6d4', '#f59e0b', '#ef4444', '#8b5cf6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { color: '#94a3b8', boxWidth: 12 } }
                }
            }
        });
    }

    // 2. Transport Category Distribution (Bar)
    const ctxTransports = document.getElementById('chart-transport-categories');
    if (ctxTransports) {
        const rawLabels = Object.keys(data.transportCategoryDistribution || {});
        const labels = rawLabels.map(l => l.replace(/_/g, ' '));
        const values = Object.values(data.transportCategoryDistribution || {});

        if (chartTransportCategories) chartTransportCategories.destroy();
        chartTransportCategories = new Chart(ctxTransports, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Fleet Count',
                    data: values,
                    backgroundColor: '#06b6d4',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { ticks: { color: '#94a3b8', font: { size: 10 } }, grid: { display: false } },
                    y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: 'rgba(255,255,255,0.05)' } }
                },
                plugins: { legend: { display: false } }
            }
        });
    }

    // 3. Mission Priority Class Distribution (Pie)
    const ctxPriorities = document.getElementById('chart-mission-priorities');
    if (ctxPriorities) {
        const labels = Object.keys(data.missionPriorityDistribution || {});
        const values = Object.values(data.missionPriorityDistribution || {});

        if (chartMissionPriorities) chartMissionPriorities.destroy();
        chartMissionPriorities = new Chart(ctxPriorities, {
            type: 'pie',
            data: {
                labels: labels.map(l => l.replace('PRIORITY_', '')),
                datasets: [{
                    data: values.length ? values : [1],
                    backgroundColor: ['#ef4444', '#f59e0b', '#3b82f6', '#64748b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', boxWidth: 12 } } }
            }
        });
    }

    // 4. Relief Coverage by Zone (Horizontal Bar)
    const ctxCoverage = document.getElementById('chart-relief-coverage');
    if (ctxCoverage) {
        const coverages = data.zoneCoverages || [];
        const labels = coverages.map(c => c.zoneCode);
        const values = coverages.map(c => c.reliefCoverageRatio);

        if (chartReliefCoverage) chartReliefCoverage.destroy();
        chartReliefCoverage = new Chart(ctxCoverage, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Coverage %',
                    data: values,
                    backgroundColor: values.map(v => v >= 80 ? '#10b981' : v >= 50 ? '#f59e0b' : '#ef4444'),
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { max: 100, ticks: { color: '#94a3b8', callback: v => v + '%' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                    y: { ticks: { color: '#94a3b8', font: { size: 10 } }, grid: { display: false } }
                },
                plugins: { legend: { display: false } }
            }
        });
    }
}

async function loadZones() {
    try {
        const res = await fetch(apiUrl('/api/disasters'));
        if (!res.ok) return;
        const zones = await res.json();

        // Populate table
        const tbody = document.getElementById('zones-table-body');
        if (tbody) {
            tbody.innerHTML = zones.map(z => `
                <tr>
                    <td><strong class="text-cyan">${escapeHtml(z.zoneCode)}</strong></td>
                    <td>${escapeHtml(z.zoneName)}</td>
                    <td><span class="badge bg-secondary">${escapeHtml(z.eventType)}</span></td>
                    <td>${z.affectedPopulation.toLocaleString()}</td>
                    <td><span class="badge ${z.medicalUrgencyScore >= 8 ? 'bg-danger' : 'bg-warning'}">${z.medicalUrgencyScore}</span></td>
                    <td>${z.terrainRisk}</td>
                    <td><span class="${z.weatherSeverityIndex > 7.0 ? 'text-danger fw-bold' : ''}">${z.weatherSeverityIndex}</span></td>
                    <td>${z.flightDistanceKm} km</td>
                    <td>
                        <button class="btn btn-sm btn-outline-info" onclick="quickDispatchZone(${z.id})">
                            <i class="fa-solid fa-paper-plane"></i> Dispatch
                        </button>
                    </td>
                </tr>
            `).join('') || '<tr><td colspan="9" class="text-center py-3 text-muted">No disaster zones registered yet.</td></tr>';
        }

        // Populate dropdown selectors in cargo and dispatch
        const cargoSelect = document.getElementById('cargo-zone-id');
        const dispatchSelect = document.getElementById('dispatch-select-zone');

        if (cargoSelect) {
            cargoSelect.innerHTML = '<option value="">-- Choose Disaster Zone --</option>' +
                zones.map(z => `<option value="${z.id}">${z.zoneCode} — ${z.zoneName}</option>`).join('');
        }
        if (dispatchSelect) {
            dispatchSelect.innerHTML = '<option value="">-- Choose Zone --</option>' +
                zones.map(z => `<option value="${z.id}">${z.zoneCode} — ${z.zoneName} (${z.eventType})</option>`).join('');
        }
    } catch (err) {
        console.error('Error loading disaster zones:', err);
    }
}

async function loadCargo() {
    try {
        const res = await fetch(apiUrl('/api/cargo'));
        if (!res.ok) return;
        const cargoList = await res.json();

        const tbody = document.getElementById('cargo-table-body');
        if (tbody) {
            tbody.innerHTML = cargoList.map(c => `
                <tr>
                    <td>#${c.id}</td>
                    <td><span class="text-cyan">Zone #${c.disasterZoneId}</span></td>
                    <td>${c.foodKitCount.toLocaleString()}</td>
                    <td>${c.waterKitCount.toLocaleString()}</td>
                    <td><span class="${c.plasmaKitCount > 0 ? 'text-danger fw-bold' : ''}">${c.plasmaKitCount}</span></td>
                    <td>${c.totalPayloadKg.toLocaleString()} kg</td>
                    <td>${c.requiredRations.toLocaleString()}</td>
                    <td><code class="small text-info">${escapeHtml(c.cargoManifest)}</code></td>
                </tr>
            `).join('') || '<tr><td colspan="8" class="text-center py-3 text-muted">No cargo registered yet.</td></tr>';
        }
    } catch (err) {
        console.error('Error loading cargo:', err);
    }
}

async function loadMissions() {
    try {
        const res = await fetch(apiUrl('/api/missions'));
        if (!res.ok) return;
        const missions = await res.json();

        // 1. Dashboard summary table
        const dashBody = document.getElementById('dashboard-missions-body');
        if (dashBody) {
            dashBody.innerHTML = missions.slice(0, 7).map(m => `
                <tr>
                    <td><strong>${escapeHtml(m.missionCode)}</strong></td>
                    <td>${escapeHtml(m.zoneCode || 'ZONE-' + m.disasterZoneId)}</td>
                    <td><strong>${m.ddusScore.toFixed(1)}</strong></td>
                    <td>${formatPriorityBadge(m.priorityLevel)}</td>
                    <td>${formatTransportCategory(m.transportCategory)}</td>
                    <td>${m.weatherValidationPassed ? '<span class="text-success"><i class="fa-solid fa-circle-check"></i> Safe</span>' : '<span class="text-danger"><i class="fa-solid fa-triangle-exclamation"></i> Grounded</span>'}</td>
                    <td>${formatStatusBadge(m.status)}</td>
                </tr>
            `).join('') || '<tr><td colspan="7" class="text-center py-3 text-muted">No missions recorded.</td></tr>';
        }

        // 2. Full tracking table
        const fullBody = document.getElementById('missions-full-table-body');
        if (fullBody) {
            fullBody.innerHTML = missions.map(m => `
                <tr>
                    <td><strong>${escapeHtml(m.missionCode)}</strong></td>
                    <td>${escapeHtml(m.zoneCode || 'ZONE-' + m.disasterZoneId)}</td>
                    <td><strong>${m.ddusScore.toFixed(1)}</strong></td>
                    <td>${formatPriorityBadge(m.priorityLevel)}</td>
                    <td>${formatTransportCategory(m.transportCategory)} ${m.assetCode ? '(' + m.assetCode + ')' : ''}</td>
                    <td>${m.transportSuitabilityScore != null ? m.transportSuitabilityScore.toFixed(1) : 'N/A'}</td>
                    <td>${m.estimatedDurationMin ? m.estimatedDurationMin + ' min' : 'N/A'}</td>
                    <td>${m.weatherValidationPassed ? '<span class="text-success">Passed</span>' : '<span class="text-danger">Failed</span>'}</td>
                    <td>${formatStatusBadge(m.status)}</td>
                    <td class="small text-muted">${escapeHtml(m.rejectionReason || 'Normal operations')}</td>
                </tr>
            `).join('') || '<tr><td colspan="10" class="text-center py-3 text-muted">No missions found.</td></tr>';
        }

        // 3. Dropdown for audit tab (only dispatched / in-transit)
        const auditSelect = document.getElementById('audit-mission-id');
        if (auditSelect) {
            const auditable = missions.filter(m => m.status === 'DISPATCHED' || m.status === 'IN_TRANSIT');
            auditSelect.innerHTML = '<option value="">-- Choose Assigned Mission --</option>' +
                auditable.map(m => `<option value="${m.id}">${m.missionCode} [${m.zoneCode || 'Zone ' + m.disasterZoneId}] - ${m.status}</option>`).join('');
        }
    } catch (err) {
        console.error('Error loading missions:', err);
    }
}

async function loadConfirmations() {
    try {
        const res = await fetch(apiUrl('/api/missions/confirmations/all'));
        if (!res.ok) return;
        const list = await res.json();

        const tbody = document.getElementById('confirmations-table-body');
        if (tbody) {
            tbody.innerHTML = list.map(c => `
                <tr>
                    <td>#${c.id}</td>
                    <td><strong>${escapeHtml(c.missionCode || 'MSN-' + c.missionId)}</strong></td>
                    <td>${escapeHtml(c.zoneCode || 'N/A')}</td>
                    <td>${c.flightTransitDurationMin} min</td>
                    <td>${c.dropOffPrecisionMeters} m</td>
                    <td><strong class="text-success">${c.deliveredRations.toLocaleString()}</strong></td>
                    <td>${escapeHtml(c.fieldOfficerName)}</td>
                    <td><span class="badge bg-success"><i class="fa-solid fa-check"></i> Verified</span></td>
                    <td class="small text-muted">${escapeHtml(c.fieldConfirmationTimestamp || '')}</td>
                </tr>
            `).join('') || '<tr><td colspan="9" class="text-center py-3 text-muted">No field confirmations recorded yet.</td></tr>';
        }
    } catch (err) {
        console.error('Error loading confirmations:', err);
    }
}

async function loadFleet() {
    try {
        const res = await fetch(apiUrl('/api/assets'));
        if (!res.ok) return;
        const fleet = await res.json();

        const tbody = document.getElementById('fleet-table-body');
        if (tbody) {
            tbody.innerHTML = fleet.map(a => `
                <tr>
                    <td><strong class="text-cyan">${escapeHtml(a.assetCode)}</strong></td>
                    <td>${escapeHtml(a.assetName)}</td>
                    <td>${formatTransportCategory(a.category)}</td>
                    <td>${a.maxPayloadKg.toLocaleString()} kg</td>
                    <td>${a.maxRangeKm.toLocaleString()} km</td>
                    <td>&le; ${a.weatherRiskLimit}</td>
                    <td>${escapeHtml(a.currentLocation)}</td>
                    <td>${formatAssetStatus(a.status)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-warning" onclick="toggleAssetStatus(${a.id}, '${a.status}')">
                            Toggle Status
                        </button>
                    </td>
                </tr>
            `).join('') || '<tr><td colspan="9" class="text-center py-3 text-muted">No fleet assets recorded.</td></tr>';
        }
    } catch (err) {
        console.error('Error loading fleet:', err);
    }
}

async function loadLogs() {
    try {
        const res = await fetch(apiUrl('/api/reports/dispatch-logs?limit=50'));
        if (!res.ok) return;
        const logs = await res.json();

        const tbody = document.getElementById('logs-table-body');
        if (tbody) {
            tbody.innerHTML = logs.map(l => `
                <tr>
                    <td class="small text-muted">${escapeHtml(l.timestamp || '')}</td>
                    <td><span class="badge bg-dark border border-secondary">${escapeHtml(l.threadName)}</span></td>
                    <td><code>${escapeHtml(l.requestId)}</code></td>
                    <td>${formatLogLevel(l.logLevel)}</td>
                    <td><strong>${escapeHtml(l.action)}</strong></td>
                    <td class="small">${escapeHtml(l.message)}</td>
                </tr>
            `).join('') || '<tr><td colspan="6" class="text-center py-3 text-muted">No multithreaded logs recorded.</td></tr>';
        }
    } catch (err) {
        console.error('Error loading logs:', err);
    }
}

// =============================================================================
// FORM HANDLERS & ACTIONS
// =============================================================================

function initEventHandlers() {
    // 1. Refresh Button
    document.getElementById('btn-refresh-data')?.addEventListener('click', () => {
        loadAllData();
        showAlert('Command center telemetry refreshed from Device 2 database.', 'info');
    });

    // 2. Quick Import CSV
    document.getElementById('btn-quick-import')?.addEventListener('click', triggerCsvImport);

    // 3. Phase 1 Disaster Intake Form
    document.getElementById('form-disaster-intake')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            zoneCode: document.getElementById('intake-zone-code').value.trim().toUpperCase(),
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
            const res = await fetch(apiUrl('/api/disasters'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                showAlert(`Disaster Zone ${payload.zoneCode} successfully registered on Device 2!`, 'success');
                document.getElementById('form-disaster-intake').reset();
                loadZones();
                loadDashboardData();
            } else {
                const err = await res.json();
                showAlert(`Registration Error: ${err.message || 'Check validation parameters'}`, 'danger');
            }
        } catch (err) {
            showAlert('Network error submitting disaster intake to Device 2: ' + err.message, 'danger');
        }
    });

    // 4. Phase 2 Cargo Ingestion Form
    document.getElementById('form-cargo-ingestion')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            disasterZoneId: parseInt(document.getElementById('cargo-zone-id').value),
            foodKitCount: parseInt(document.getElementById('cargo-food').value),
            waterKitCount: parseInt(document.getElementById('cargo-water').value),
            plasmaKitCount: parseInt(document.getElementById('cargo-plasma').value),
            totalPayloadKg: parseFloat(document.getElementById('cargo-payload').value),
            requiredRations: parseInt(document.getElementById('cargo-rations').value),
            dispatchLocation: document.getElementById('cargo-location').value.trim(),
            cargoManifest: document.getElementById('cargo-manifest-str').value.trim()
        };

        try {
            const res = await fetch(apiUrl('/api/cargo'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                showAlert('Relief Cargo ingested and manifest validated on Device 2!', 'success');
                loadCargo();
                loadDashboardData();
            } else {
                const err = await res.json();
                showAlert(`Cargo Ingestion Error: ${err.message || 'Manifest validation failed'}`, 'danger');
            }
        } catch (err) {
            showAlert('Network error during cargo ingestion: ' + err.message, 'danger');
        }
    });

    // 5. Trigger Single Dispatch
    document.getElementById('btn-trigger-dispatch')?.addEventListener('click', async () => {
        const zoneId = document.getElementById('dispatch-select-zone').value;
        if (!zoneId) {
            showAlert('Please select an active disaster zone to evaluate', 'warning');
            return;
        }
        await executeDispatch(zoneId);
    });

    // 6. Bulk Concurrent Dispatch (Multithreaded demonstration)
    document.getElementById('btn-bulk-concurrent')?.addEventListener('click', async () => {
        try {
            const zonesRes = await fetch(apiUrl('/api/disasters'));
            const zones = await zonesRes.json();
            if (!zones || zones.length === 0) {
                showAlert('No registered disaster zones available for bulk dispatch', 'warning');
                return;
            }

            const zoneIds = zones.map(z => z.id);
            showAlert(`Triggering multithreaded dispatch on Device 2 for ${zoneIds.length} zones...`, 'info');

            const res = await fetch(apiUrl('/api/dispatch/bulk'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(zoneIds)
            });

            if (res.ok) {
                const results = await res.json();
                showAlert(`Bulk dispatch finished! Device 2 processed ${results.length} missions concurrently.`, 'success');
                loadAllData();
            } else {
                showAlert('Bulk dispatch processing failed on Device 2', 'danger');
            }
        } catch (err) {
            showAlert('Error triggering bulk concurrent dispatch: ' + err.message, 'danger');
        }
    });

    // 7. Phase 3 Delivery Audit Form
    document.getElementById('form-delivery-audit')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const missionId = document.getElementById('audit-mission-id').value;
        if (!missionId) {
            showAlert('Please select a mission to audit', 'warning');
            return;
        }

        const payload = {
            flightTransitDurationMin: parseInt(document.getElementById('audit-duration').value),
            dropOffPrecisionMeters: parseFloat(document.getElementById('audit-precision').value),
            fieldOfficerName: document.getElementById('audit-officer').value.trim(),
            deliveredRations: parseInt(document.getElementById('audit-rations').value),
            deliveryNotes: document.getElementById('audit-notes').value.trim(),
            deliveryVerified: document.getElementById('audit-verified').checked
        };

        try {
            const res = await fetch(apiUrl(`/api/missions/${missionId}/audit`), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                showAlert(`Mission audited & delivery verified on Device 2! Relief coverage updated.`, 'success');
                loadAllData();
            } else {
                const err = await res.json();
                showAlert(`Audit Error: ${err.message || 'Verification rejected'}`, 'danger');
            }
        } catch (err) {
            showAlert('Network error recording field audit: ' + err.message, 'danger');
        }
    });
}

// Single dispatch executor
async function executeDispatch(zoneId) {
    try {
        const res = await fetch(apiUrl(`/api/dispatch/${zoneId}`), { method: 'POST' });
        const data = await res.json();

        const card = document.getElementById('dispatch-evaluation-result');
        if (card) {
            card.style.display = 'block';
            document.getElementById('eval-code').textContent = data.missionCode || 'N/A';
            document.getElementById('eval-ddus').textContent = data.ddusScore ? data.ddusScore.toFixed(1) : 'N/A';
            document.getElementById('eval-priority').innerHTML = formatPriorityBadge(data.priorityLevel);
            document.getElementById('eval-transport').textContent = data.assignedAssetName ? `${data.assignedAssetName} (${data.assignedAssetCode})` : 'None';
            document.getElementById('eval-suitability').textContent = data.transportSuitabilityScore ? data.transportSuitabilityScore.toFixed(1) : 'N/A';
            document.getElementById('eval-status').innerHTML = formatStatusBadge(data.status);
            document.getElementById('eval-msg').textContent = data.executionMessage || data.rejectionReason || 'Dispatched safely';
        }

        showAlert(`Dispatch worker completed: ${data.status} for ${data.zoneCode || 'Zone ' + zoneId}`,
            data.status === 'DISPATCHED' ? 'success' : data.status === 'FLIGHT_GROUNDED' ? 'warning' : 'danger');

        loadAllData();
    } catch (err) {
        showAlert('Dispatch execution error: ' + err.message, 'danger');
    }
}

window.quickDispatchZone = function(zoneId) {
    const navDispatch = document.querySelector('[data-tab="tab-dispatch"]');
    if (navDispatch) navDispatch.click();
    const select = document.getElementById('dispatch-select-zone');
    if (select) select.value = zoneId;
    executeDispatch(zoneId);
};

window.toggleAssetStatus = async function(assetId, currentStatus) {
    const nextStatus = currentStatus === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    try {
        const res = await fetch(apiUrl(`/api/assets/${assetId}?status=${nextStatus}`), { method: 'PUT' });
        if (res.ok) {
            showAlert(`Asset #${assetId} status changed to ${nextStatus}`, 'info');
            loadFleet();
            loadDashboardData();
        }
    } catch (err) {
        showAlert('Error updating asset status', 'danger');
    }
};

async function triggerCsvImport() {
    try {
        showAlert('Ingesting default data/relief_requests.csv on Device 2...', 'info');
        const res = await fetch(apiUrl('/api/import/csv'), { method: 'POST' });
        if (res.ok) {
            const summary = await res.json();
            showAlert(`CSV Import Completed: ${summary.successfulRows} rows imported, ${summary.rejectedRows} rejected.`,
                summary.rejectedRows === 0 ? 'success' : 'warning');
            loadAllData();
        } else {
            const err = await res.json();
            showAlert('CSV Import Failed: ' + (err.message || 'File parsing error'), 'danger');
        }
    } catch (err) {
        showAlert('Error during CSV import: ' + err.message, 'danger');
    }
}

// =============================================================================
// FORMATTING HELPERS
// =============================================================================

function formatPriorityBadge(priority) {
    if (!priority) return '<span class="badge bg-secondary">UNKNOWN</span>';
    switch (priority.toUpperCase()) {
        case 'PRIORITY_ALPHA':
            return '<span class="badge-priority-alpha"><i class="fa-solid fa-fire"></i> ALPHA (Critical)</span>';
        case 'PRIORITY_BRAVO':
            return '<span class="badge-priority-bravo"><i class="fa-solid fa-clock"></i> BRAVO (High)</span>';
        case 'PRIORITY_CHARLIE':
            return '<span class="badge-priority-charlie"><i class="fa-solid fa-calendar-check"></i> CHARLIE (Standard)</span>';
        case 'MISSION_ABORTED':
        default:
            return '<span class="badge bg-danger"><i class="fa-solid fa-ban"></i> ABORTED</span>';
    }
}

function formatStatusBadge(status) {
    if (!status) return '<span class="badge bg-secondary">UNKNOWN</span>';
    switch (status.toUpperCase()) {
        case 'DISPATCHED':
            return '<span class="badge-status-dispatched"><i class="fa-solid fa-paper-plane"></i> DISPATCHED</span>';
        case 'IN_TRANSIT':
            return '<span class="badge-status-intransit"><i class="fa-solid fa-plane"></i> IN TRANSIT</span>';
        case 'COMPLETED':
            return '<span class="badge-status-completed"><i class="fa-solid fa-circle-check"></i> COMPLETED</span>';
        case 'FLIGHT_GROUNDED':
            return '<span class="badge-status-grounded"><i class="fa-solid fa-cloud-showers-heavy"></i> FLIGHT GROUNDED</span>';
        case 'ABORTED':
            return '<span class="badge bg-danger"><i class="fa-solid fa-ban"></i> ABORTED</span>';
        case 'PENDING':
        default:
            return '<span class="badge bg-warning text-dark"><i class="fa-solid fa-hourglass-start"></i> PENDING</span>';
    }
}

function formatTransportCategory(cat) {
    if (!cat) return '<span class="text-muted">None</span>';
    switch (cat.toUpperCase()) {
        case 'HEAVY_CARGO_HELICOPTER':
            return '<span class="text-info"><i class="fa-solid fa-helicopter"></i> Helicopter</span>';
        case 'ALL_TERRAIN_RESCUE_TRUCK':
            return '<span class="text-amber"><i class="fa-solid fa-truck-pickup"></i> Rescue Truck</span>';
        case 'EMERGENCY_MEDICAL_DRONE':
            return '<span class="text-cyan"><i class="fa-solid fa-robot"></i> Medical Drone</span>';
        default:
            return escapeHtml(cat);
    }
}

function formatAssetStatus(status) {
    if (!status) return '';
    switch (status.toUpperCase()) {
        case 'AVAILABLE':
            return '<span class="badge-status-available">AVAILABLE</span>';
        case 'OCCUPIED':
            return '<span class="badge-status-occupied">OCCUPIED</span>';
        case 'MAINTENANCE':
        default:
            return '<span class="badge bg-danger">MAINTENANCE</span>';
    }
}

function formatLogLevel(level) {
    switch ((level || '').toUpperCase()) {
        case 'INFO': return '<span class="badge bg-info text-dark">INFO</span>';
        case 'WARN': return '<span class="badge bg-warning text-dark">WARN</span>';
        case 'ERROR': return '<span class="badge bg-danger">ERROR</span>';
        default: return `<span class="badge bg-secondary">${escapeHtml(level)}</span>`;
    }
}

function showAlert(message, type = 'info') {
    const container = document.getElementById('live-alert-container');
    if (!container) return;

    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.role = 'alert';
    alert.innerHTML = `
        <strong>${type.toUpperCase()}:</strong> ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    container.prepend(alert);

    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
