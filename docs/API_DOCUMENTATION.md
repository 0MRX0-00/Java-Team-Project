# REST API DOCUMENTATION & ENDPOINT CATALOG
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### BASE URL
- Local Windows 11: `http://localhost:8080/api`
- OCI Production: `http://<APP_PUBLIC_IP>:8080/api`
- OpenAPI Specification: `http://localhost:8080/api-docs`
- Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`

---

### ENDPOINTS SUMMARY

#### 1. Dashboard & Telemetry
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/dashboard/statistics` | Retrieves real database-backed counts, fleet status, and coverage metrics |
| `GET` | `/api/dashboard/charts` | Retrieves Chart.js distribution datasets |

#### 2. Disaster Intake (Phase 1)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/disasters` | Registers a new disaster zone with `ZONE-[A-Z]{2}-\d{4}` regex validation |
| `GET` | `/api/disasters` | Lists all registered disaster zones |
| `GET` | `/api/disasters/{id}` | Retrieves disaster zone by primary key |
| `PUT` | `/api/disasters/{id}` | Updates zone parameters |

#### 3. Cargo Management (Phase 2)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/cargo` | Ingests relief cargo payload and validates manifest string |
| `GET` | `/api/cargo` | Lists all ingested cargo records |
| `GET` | `/api/cargo/{id}` | Retrieves cargo record by ID |
| `GET` | `/api/cargo/zone/{zoneId}` | Retrieves cargo for specific disaster zone |

#### 4. Dispatch Center & Multithreading
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/dispatch/{zoneId}` | Executes intelligent dispatch scheduling for single zone |
| `POST` | `/api/dispatch/bulk` | Executes concurrent multithreaded dispatch for list of zone IDs |

#### 5. Mission Tracking
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/missions` | Retrieves all scheduled, active, and completed missions |
| `GET` | `/api/missions/{id}` | Retrieves specific mission by ID |
| `GET` | `/api/missions/status/{status}` | Filters missions by status (`DISPATCHED`, `COMPLETED`, `FLIGHT_GROUNDED`, `ABORTED`) |

#### 6. Field Confirmation & Audit (Phase 3)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/missions/{id}/audit` | Records on-ground field delivery confirmation and releases asset |
| `POST` | `/api/missions/{id}/confirm` | Alias for field delivery confirmation |
| `GET` | `/api/missions/confirmations/all` | Lists all audited field delivery records |

#### 7. Transport Fleet
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/assets` | Lists all helicopters, trucks, and drones in fleet |
| `POST` | `/api/assets` | Registers a new transport vehicle |
| `PUT` | `/api/assets/{id}?status=X` | Updates asset status (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`) |

#### 8. Reports & Manifest Exports
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/reports/manifests` | Exports formatted `emergency_flight_manifests.txt` |
| `GET` | `/api/reports/supply-drop-logs` | Exports formatted `supply_drop_logs.txt` |
| `GET` | `/api/reports/summary` | Exports formatted `dispatch_summary.txt` |
| `GET` | `/api/reports/dispatch-logs` | Returns multithreaded execution logs |

#### 9. Bulk CSV Ingestion
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/import/csv` | Ingests disaster requests in bulk from CSV upload or default file |

