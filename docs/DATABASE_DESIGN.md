# DATABASE DESIGN SPECIFICATION
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. RELATIONAL ENTITY-RELATIONSHIP ARCHITECTURE

The DREMLDS database (`disaster_relief_db`) utilizes an InnoDB relational schema optimized for high concurrency, ACID transaction guarantees, and analytical telemetry aggregations.

```mermaid
erDiagram
    DISASTER_ZONES ||--o{ RELIEF_CARGO : has
    DISASTER_ZONES ||--o{ DISPATCH_MISSIONS : targets
    RELIEF_CARGO ||--o| DISPATCH_MISSIONS : manifests
    TRANSPORT_ASSETS ||--o{ DISPATCH_MISSIONS : assigned_to
    DISPATCH_MISSIONS ||--o| FIELD_CONFIRMATIONS : audited_by
    DISPATCH_MISSIONS ||--o{ DISPATCH_LOGS : records

    DISASTER_ZONES {
        BIGINT id PK
        VARCHAR zone_code UK
        VARCHAR zone_name
        VARCHAR event_type
        INT affected_population
        DOUBLE medical_urgency_score
        DOUBLE terrain_risk
        DOUBLE road_accessibility
        DOUBLE weather_severity_index
        DOUBLE flight_distance_km
        VARCHAR status
        TIMESTAMP created_at
    }

    RELIEF_CARGO {
        BIGINT id PK
        BIGINT disaster_zone_id FK
        INT food_kit_count
        INT water_kit_count
        INT plasma_kit_count
        DOUBLE total_payload_kg
        INT required_rations
        VARCHAR dispatch_location
        TEXT cargo_manifest
        TIMESTAMP created_at
    }

    TRANSPORT_ASSETS {
        BIGINT id PK
        VARCHAR asset_code UK
        VARCHAR asset_name
        VARCHAR category
        DOUBLE max_payload_kg
        DOUBLE max_range_km
        DOUBLE weather_risk_limit
        VARCHAR status
        VARCHAR current_location
        TIMESTAMP created_at
    }

    DISPATCH_MISSIONS {
        BIGINT id PK
        VARCHAR mission_code UK
        BIGINT disaster_zone_id FK
        BIGINT relief_cargo_id FK
        BIGINT transport_asset_id FK
        DOUBLE ddus_score
        VARCHAR priority_level
        VARCHAR transport_category
        DOUBLE transport_suitability_score
        BOOLEAN weather_validation_passed
        VARCHAR rejection_reason
        VARCHAR status
        INT estimated_duration_min
        TIMESTAMP dispatched_at
        TIMESTAMP completed_at
    }

    FIELD_CONFIRMATIONS {
        BIGINT id PK
        BIGINT mission_id FK,UK
        INT flight_transit_duration_min
        DOUBLE drop_off_precision_meters
        VARCHAR field_officer_name
        INT delivered_rations
        TEXT delivery_notes
        BOOLEAN delivery_verified
        TIMESTAMP field_confirmation_timestamp
    }

    DISPATCH_LOGS {
        BIGINT id PK
        BIGINT mission_id FK
        VARCHAR thread_name
        VARCHAR request_id
        VARCHAR log_level
        VARCHAR action
        TEXT message
        TIMESTAMP timestamp
    }

    CSV_IMPORT_HISTORY {
        BIGINT id PK
        VARCHAR filename
        INT total_rows
        INT successful_rows
        INT rejected_rows
        TEXT error_summary
        TIMESTAMP imported_at
    }
```

---

### 2. TABLE DEFINITIONS & CONSTRAINTS

1. **`disaster_zones`**:
   - `zone_code`: Unique identifier adhering to `ZONE-[A-Z]{2}-\d{4}` regex.
   - Indexed on `zone_code`, `event_type`, and `status`.

2. **`relief_cargo`**:
   - Links to `disaster_zones(id)` with `ON DELETE CASCADE`.
   - Stores payload weight, required rations, and raw string manifest.

3. **`transport_assets`**:
   - Stores operating ceilings (Helicopters &le; 7.0, Trucks &le; 8.5, Drones &le; 5.0).
   - Atomic reservation state transitions: `AVAILABLE` &rarr; `OCCUPIED` &rarr; `AVAILABLE`.

4. **`dispatch_missions`**:
   - Links zone, cargo, and allocated asset.
   - Tracks operational states: `PENDING`, `DISPATCHED`, `IN_TRANSIT`, `COMPLETED`, `FLIGHT_GROUNDED`, `ABORTED`.

5. **`field_confirmations`**:
   - 1:1 foreign key relation to `dispatch_missions(id)` (`UNIQUE`).
   - Prevents duplicate delivery confirmations.

6. **`dispatch_logs`**:
   - Captures concurrent worker thread diagnostics (`DispatchWorker-1` ... `DispatchWorker-8`).

7. **`csv_import_history`**:
   - Tracks batch file ingestion successes and line-by-line syntax defects.

