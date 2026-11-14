-- H2 In-Memory Schema for Automated Tests and Local Fallback

DROP TABLE IF EXISTS field_confirmations;
DROP TABLE IF EXISTS dispatch_logs;
DROP TABLE IF EXISTS dispatch_missions;
DROP TABLE IF EXISTS relief_cargo;
DROP TABLE IF EXISTS transport_assets;
DROP TABLE IF EXISTS csv_import_history;
DROP TABLE IF EXISTS disaster_zones;

CREATE TABLE disaster_zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_code VARCHAR(30) NOT NULL UNIQUE,
    zone_name VARCHAR(120) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    affected_population INT NOT NULL,
    medical_urgency_score DOUBLE NOT NULL,
    terrain_risk DOUBLE NOT NULL,
    road_accessibility DOUBLE NOT NULL,
    weather_severity_index DOUBLE NOT NULL,
    flight_distance_km DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE relief_cargo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disaster_zone_id BIGINT NOT NULL,
    food_kit_count INT NOT NULL DEFAULT 0,
    water_kit_count INT NOT NULL DEFAULT 0,
    plasma_kit_count INT NOT NULL DEFAULT 0,
    total_payload_kg DOUBLE NOT NULL,
    required_rations INT NOT NULL,
    dispatch_location VARCHAR(120) NOT NULL,
    cargo_manifest CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_zone_id) REFERENCES disaster_zones(id) ON DELETE CASCADE
);

CREATE TABLE transport_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_code VARCHAR(50) NOT NULL UNIQUE,
    asset_name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    max_payload_kg DOUBLE NOT NULL,
    max_range_km DOUBLE NOT NULL,
    weather_risk_limit DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    current_location VARCHAR(120) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dispatch_missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_code VARCHAR(50) NOT NULL UNIQUE,
    disaster_zone_id BIGINT NOT NULL,
    relief_cargo_id BIGINT NULL,
    transport_asset_id BIGINT NULL,
    ddus_score DOUBLE NOT NULL,
    priority_level VARCHAR(30) NOT NULL,
    transport_category VARCHAR(50) NULL,
    transport_suitability_score DOUBLE NULL,
    weather_validation_passed BOOLEAN NOT NULL DEFAULT TRUE,
    rejection_reason VARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    dispatched_at TIMESTAMP NULL,
    estimated_duration_min INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_zone_id) REFERENCES disaster_zones(id) ON DELETE CASCADE,
    FOREIGN KEY (relief_cargo_id) REFERENCES relief_cargo(id) ON DELETE SET NULL,
    FOREIGN KEY (transport_asset_id) REFERENCES transport_assets(id) ON DELETE SET NULL
);

CREATE TABLE field_confirmations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL UNIQUE,
    flight_transit_duration_min INT NOT NULL,
    drop_off_precision_meters DOUBLE NOT NULL,
    field_officer_name VARCHAR(120) NOT NULL,
    delivered_rations INT NOT NULL,
    field_confirmation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_notes CLOB NULL,
    delivery_verified BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mission_id) REFERENCES dispatch_missions(id) ON DELETE CASCADE
);

CREATE TABLE dispatch_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NULL,
    thread_name VARCHAR(100) NOT NULL,
    request_id VARCHAR(100) NOT NULL,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    action VARCHAR(100) NOT NULL,
    message CLOB NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE csv_import_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    successful_rows INT NOT NULL DEFAULT 0,
    rejected_rows INT NOT NULL DEFAULT 0,
    error_summary CLOB NULL,
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Test Scenarios
INSERT INTO disaster_zones (zone_code, zone_name, event_type, affected_population, medical_urgency_score, terrain_risk, road_accessibility, weather_severity_index, flight_distance_km, status)
VALUES
('ZONE-TN-1001', 'Cuddalore Coastal Delta', 'FLOOD', 45000, 9.5, 78.0, 22.0, 5.8, 185.0, 'ACTIVE'),
('ZONE-TN-1002', 'Nilgiris Mountain Ridge', 'QUAKE', 18500, 8.8, 88.0, 15.0, 7.8, 310.0, 'ACTIVE'),
('ZONE-TN-1003', 'Nagapattinam Delta Basin', 'FLOOD', 62000, 9.8, 62.0, 28.0, 4.2, 290.0, 'ACTIVE'),
('ZONE-TN-1004', 'Madurai Southern Plains', 'QUAKE', 28000, 7.4, 42.0, 72.0, 3.2, 420.0, 'ACTIVE'),
('ZONE-TN-1005', 'Chennai Urban Perimeter', 'FLOOD', 14000, 6.2, 25.0, 84.0, 2.5, 48.0, 'ACTIVE'),
('ZONE-KL-2001', 'Wayanad Isolated Highlands', 'QUAKE', 7200, 9.6, 92.0, 12.0, 4.0, 95.0, 'ACTIVE'),
('ZONE-KA-3001', 'Karwar Severe Storm Basin', 'FLOOD', 2100, 3.2, 45.0, 50.0, 9.2, 240.0, 'ACTIVE');

INSERT INTO transport_assets (asset_code, asset_name, category, max_payload_kg, max_range_km, weather_risk_limit, status, current_location)
VALUES
('HELI-CH47-01', 'Boeing CH-47 Chinook Heavy Lift', 'HEAVY_CARGO_HELICOPTER', 5500.0, 650.0, 7.0, 'AVAILABLE', 'Chennai Air Hub'),
('HELI-MI17-02', 'Mil Mi-17V5 Multi-role Transport', 'HEAVY_CARGO_HELICOPTER', 4000.0, 520.0, 7.0, 'AVAILABLE', 'Tambaram Air Station'),
('TRUCK-TATRA-01', 'Tatra 8x8 Heavy Tactical Rescue Truck', 'ALL_TERRAIN_RESCUE_TRUCK', 8500.0, 900.0, 8.5, 'AVAILABLE', 'Arakkonam NDRF Depot'),
('TRUCK-ASHOK-02', 'Ashok Leyland Stallion 4x4 Logistics', 'ALL_TERRAIN_RESCUE_TRUCK', 5000.0, 750.0, 8.5, 'AVAILABLE', 'Trichy Central Depot'),
('DRONE-SKYMED-01', 'SkyMed-X Long-Range eVTOL Drone', 'EMERGENCY_MEDICAL_DRONE', 50.0, 160.0, 5.0, 'AVAILABLE', 'Coimbatore Medical Center'),
('DRONE-LIFEWING-02', 'LifeWing Cryo-Courier Medical Drone', 'EMERGENCY_MEDICAL_DRONE', 65.0, 180.0, 5.0, 'AVAILABLE', 'Madurai Red Cross Base');

INSERT INTO relief_cargo (disaster_zone_id, food_kit_count, water_kit_count, plasma_kit_count, total_payload_kg, required_rations, dispatch_location, cargo_manifest)
VALUES
(1, 500, 1000, 40, 2800.0, 5000, 'Chennai Logistics Hub', 'FOOD,500,WATER,1000,PLASMA,40'),
(2, 300, 600, 25, 1750.0, 3000, 'Coimbatore Base', 'FOOD,300,WATER,600,PLASMA,25'),
(3, 1200, 2500, 100, 5200.0, 12000, 'Trichy Regional Depot', 'FOOD,1200,WATER,2500,PLASMA,100'),
(6, 15, 30, 45, 42.0, 600, 'Calicut Rapid Dispensary', 'FOOD,15,WATER,30,PLASMA,45');

INSERT INTO dispatch_missions (mission_code, disaster_zone_id, relief_cargo_id, transport_asset_id, ddus_score, priority_level, transport_category, transport_suitability_score, weather_validation_passed, rejection_reason, status, dispatched_at, estimated_duration_min, completed_at)
VALUES
('MSN-2026-0001', 3, 3, 1, 95.2, 'PRIORITY_ALPHA', 'HEAVY_CARGO_HELICOPTER', 91.5, TRUE, NULL, 'COMPLETED', CURRENT_TIMESTAMP, 75, CURRENT_TIMESTAMP),
('MSN-2026-0002', 1, 1, 3, 86.4, 'PRIORITY_BRAVO', 'ALL_TERRAIN_RESCUE_TRUCK', 84.0, TRUE, NULL, 'IN_TRANSIT', CURRENT_TIMESTAMP, 120, NULL),
('MSN-2026-0003', 6, 4, 5, 93.8, 'PRIORITY_ALPHA', 'EMERGENCY_MEDICAL_DRONE', 94.2, TRUE, NULL, 'COMPLETED', CURRENT_TIMESTAMP, 40, CURRENT_TIMESTAMP),
('MSN-2026-0004', 2, 2, NULL, 77.5, 'PRIORITY_BRAVO', 'HEAVY_CARGO_HELICOPTER', 0.0, FALSE, 'Weather severity 7.8 exceeds Helicopter safety ceiling 7.0', 'FLIGHT_GROUNDED', NULL, 0, NULL),
('MSN-2026-0005', 7, NULL, NULL, 48.6, 'MISSION_ABORTED', NULL, 0.0, FALSE, 'DDUS score 48.6 below threshold (60) and weather severity 9.2 unsafe', 'ABORTED', NULL, 0, NULL);

INSERT INTO field_confirmations (mission_id, flight_transit_duration_min, drop_off_precision_meters, field_officer_name, delivered_rations, delivery_notes, delivery_verified)
VALUES
(1, 72, 4.5, 'Commander Rajesh Kumar, NDRF 4th Bn', 11500, 'Payload dropped safely at Nagapattinam Relief Staging Ground. 95.8% rations received intact.', TRUE),
(3, 38, 1.2, 'Dr. Ananya Nair, Medical Relief Camp', 600, 'Critical plasma units delivered with intact cold-chain temperature (3.2C). 100% rations fulfilled.', TRUE);

