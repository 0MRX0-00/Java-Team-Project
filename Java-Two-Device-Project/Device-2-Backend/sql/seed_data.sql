-- ==============================================================================
-- DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
-- DEMONSTRATION SEED DATA FOR DEVICE 2 (BACKEND DATABASE)
-- ==============================================================================

USE disaster_relief_db;

-- Clear previous test data safely
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE field_confirmations;
TRUNCATE TABLE dispatch_logs;
TRUNCATE TABLE dispatch_missions;
TRUNCATE TABLE relief_cargo;
TRUNCATE TABLE transport_assets;
TRUNCATE TABLE csv_import_history;
TRUNCATE TABLE disaster_zones;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Disaster Zones
INSERT INTO disaster_zones (id, zone_code, zone_name, event_type, affected_population, medical_urgency_score, terrain_risk, road_accessibility, weather_severity_index, flight_distance_km, status)
VALUES
(1, 'ZONE-TN-1001', 'Cuddalore Coastal Delta', 'FLOOD', 45000, 9.5, 78.0, 22.0, 5.8, 185.0, 'ACTIVE'),
(2, 'ZONE-TN-1002', 'Nilgiris Mountain Ridge', 'QUAKE', 18500, 8.8, 88.0, 15.0, 7.8, 310.0, 'ACTIVE'),
(3, 'ZONE-TN-1003', 'Nagapattinam Delta Basin', 'FLOOD', 62000, 9.8, 62.0, 28.0, 4.2, 290.0, 'ACTIVE'),
(4, 'ZONE-TN-1004', 'Madurai Southern Plains', 'QUAKE', 28000, 7.4, 42.0, 72.0, 3.2, 420.0, 'ACTIVE'),
(5, 'ZONE-TN-1005', 'Chennai Urban Perimeter', 'FLOOD', 14000, 6.2, 25.0, 84.0, 2.5, 48.0, 'ACTIVE'),
(6, 'ZONE-KL-2001', 'Wayanad Isolated Highlands', 'QUAKE', 7200, 9.6, 92.0, 12.0, 4.0, 95.0, 'ACTIVE'),
(7, 'ZONE-KA-3001', 'Karwar Severe Storm Basin', 'FLOOD', 2100, 3.2, 45.0, 50.0, 9.2, 240.0, 'ACTIVE');

-- 2. Insert Transport Assets
INSERT INTO transport_assets (id, asset_code, asset_name, category, max_payload_kg, max_range_km, weather_risk_limit, status, current_location)
VALUES
(1, 'HELI-CH47-01', 'Boeing CH-47 Chinook Heavy Lift', 'HEAVY_CARGO_HELICOPTER', 5500.0, 650.0, 7.0, 'AVAILABLE', 'Chennai Air Hub'),
(2, 'HELI-MI17-02', 'Mil Mi-17V5 Multi-role Transport', 'HEAVY_CARGO_HELICOPTER', 4000.0, 520.0, 7.0, 'AVAILABLE', 'Tambaram Air Station'),
(3, 'TRUCK-TATRA-01', 'Tatra 8x8 Heavy Tactical Rescue Truck', 'ALL_TERRAIN_RESCUE_TRUCK', 8500.0, 900.0, 8.5, 'AVAILABLE', 'Arakkonam NDRF Depot'),
(4, 'TRUCK-ASHOK-02', 'Ashok Leyland Stallion 4x4 Logistics', 'ALL_TERRAIN_RESCUE_TRUCK', 5000.0, 750.0, 8.5, 'AVAILABLE', 'Trichy Central Depot'),
(5, 'DRONE-SKYMED-01', 'SkyMed-X Long-Range eVTOL Drone', 'EMERGENCY_MEDICAL_DRONE', 50.0, 160.0, 5.0, 'AVAILABLE', 'Coimbatore Medical Center'),
(6, 'DRONE-LIFEWING-02', 'LifeWing Cryo-Courier Medical Drone', 'EMERGENCY_MEDICAL_DRONE', 65.0, 180.0, 5.0, 'AVAILABLE', 'Madurai Red Cross Base');

-- 3. Insert Relief Cargo Pre-Configurations
INSERT INTO relief_cargo (id, disaster_zone_id, food_kit_count, water_kit_count, plasma_kit_count, total_payload_kg, required_rations, dispatch_location, cargo_manifest)
VALUES
(1, 1, 500, 1000, 40, 2800.0, 5000, 'Chennai Logistics Hub', 'FOOD,500,WATER,1000,PLASMA,40'),
(2, 2, 300, 600, 25, 1750.0, 3000, 'Coimbatore Base', 'FOOD,300,WATER,600,PLASMA,25'),
(3, 3, 1200, 2500, 100, 5200.0, 12000, 'Trichy Regional Depot', 'FOOD,1200,WATER,2500,PLASMA,100'),
(4, 6, 15, 30, 45, 42.0, 600, 'Calicut Rapid Dispensary', 'FOOD,15,WATER,30,PLASMA,45');

-- 4. Insert Initial Dispatch Missions (Simulated Demonstrations)
INSERT INTO dispatch_missions (id, mission_code, disaster_zone_id, relief_cargo_id, transport_asset_id, ddus_score, priority_level, transport_category, transport_suitability_score, weather_validation_passed, rejection_reason, status, dispatched_at, estimated_duration_min, completed_at)
VALUES
(1, 'MSN-2026-0001', 3, 3, 1, 95.2, 'PRIORITY_ALPHA', 'HEAVY_CARGO_HELICOPTER', 91.5, TRUE, NULL, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 HOUR), 75, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'MSN-2026-0002', 1, 1, 3, 86.4, 'PRIORITY_BRAVO', 'ALL_TERRAIN_RESCUE_TRUCK', 84.0, TRUE, NULL, 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 1 HOUR), 120, NULL),
(3, 'MSN-2026-0003', 6, 4, 5, 93.8, 'PRIORITY_ALPHA', 'EMERGENCY_MEDICAL_DRONE', 94.2, TRUE, NULL, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 HOUR), 40, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(4, 'MSN-2026-0004', 2, 2, NULL, 77.5, 'PRIORITY_BRAVO', 'HEAVY_CARGO_HELICOPTER', 0.0, FALSE, 'Weather severity 7.8 exceeds Helicopter safety ceiling 7.0', 'FLIGHT_GROUNDED', NULL, 0, NULL),
(5, 'MSN-2026-0005', 7, NULL, NULL, 48.6, 'MISSION_ABORTED', NULL, 0.0, FALSE, 'DDUS score 48.6 below threshold (60) and weather severity 9.2 unsafe', 'ABORTED', NULL, 0, NULL);

-- 5. Insert Completed Field Delivery Confirmations
INSERT INTO field_confirmations (id, mission_id, flight_transit_duration_min, drop_off_precision_meters, field_officer_name, delivered_rations, delivery_notes, delivery_verified)
VALUES
(1, 1, 72, 4.5, 'Commander Rajesh Kumar, NDRF 4th Bn', 11500, 'Payload dropped safely at Nagapattinam Relief Staging Ground. 95.8% rations received intact.', TRUE),
(2, 3, 38, 1.2, 'Dr. Ananya Nair, Medical Relief Camp', 600, 'Critical plasma units delivered with intact cold-chain temperature (3.2C). 100% rations fulfilled.', TRUE);

-- 6. Insert Multithreaded Dispatch Log Demonstrations
INSERT INTO dispatch_logs (mission_id, thread_name, request_id, log_level, action, message)
VALUES
(1, 'DispatchWorker-1', 'REQ-TN-1003-01', 'INFO', 'CALCULATE_DDUS', 'Evaluated DDUS for Nagapattinam: 95.2 -> PRIORITY_ALPHA'),
(1, 'DispatchWorker-1', 'REQ-TN-1003-01', 'INFO', 'ALLOCATE_ASSET', 'Reserved HELI-CH47-01 (Heavy Helicopter) with suitability score 91.5'),
(2, 'DispatchWorker-2', 'REQ-TN-1001-01', 'INFO', 'CALCULATE_DDUS', 'Evaluated DDUS for Cuddalore: 86.4 -> PRIORITY_BRAVO'),
(2, 'DispatchWorker-2', 'REQ-TN-1001-01', 'INFO', 'ALLOCATE_ASSET', 'Assigned TRUCK-TATRA-01 (All-Terrain Truck). Suitability score: 84.0'),
(4, 'DispatchWorker-3', 'REQ-TN-1002-01', 'WARN', 'WEATHER_HAZARD', 'Helicopter flight rejected: weather index 7.8 exceeds limit 7.0. Mission marked FLIGHT_GROUNDED'),
(5, 'DispatchWorker-4', 'REQ-KA-3001-01', 'ERROR', 'MISSION_ABORT', 'DDUS 48.6 < 60 and storm index 9.2 unsafe. Mission aborted.');
