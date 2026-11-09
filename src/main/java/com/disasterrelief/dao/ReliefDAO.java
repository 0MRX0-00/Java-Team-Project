package com.disasterrelief.dao;

import com.disasterrelief.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ReliefDAO is the core Data Access Object fulfilling the academic assignment requirement.
 * Encapsulates parameterized SQL queries, transaction-safe updates, and analytical aggregations.
 */
@Repository
public class ReliefDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReliefDAO.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReliefDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // =========================================================================
    // ROW MAPPERS
    // =========================================================================

    private final RowMapper<DisasterZone> zoneRowMapper = (rs, rowNum) -> {
        DisasterZone z = new DisasterZone();
        z.setId(rs.getLong("id"));
        z.setZoneCode(rs.getString("zone_code"));
        z.setZoneName(rs.getString("zone_name"));
        z.setEventType(rs.getString("event_type"));
        z.setAffectedPopulation(rs.getInt("affected_population"));
        z.setMedicalUrgencyScore(rs.getDouble("medical_urgency_score"));
        z.setTerrainRisk(rs.getDouble("terrain_risk"));
        z.setRoadAccessibility(rs.getDouble("road_accessibility"));
        z.setWeatherSeverityIndex(rs.getDouble("weather_severity_index"));
        z.setFlightDistanceKm(rs.getDouble("flight_distance_km"));
        z.setStatus(rs.getString("status"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) z.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) z.setUpdatedAt(ut.toLocalDateTime());
        return z;
    };

    private final RowMapper<ReliefCargo> cargoRowMapper = (rs, rowNum) -> {
        ReliefCargo c = new ReliefCargo();
        c.setId(rs.getLong("id"));
        c.setDisasterZoneId(rs.getLong("disaster_zone_id"));
        c.setFoodKitCount(rs.getInt("food_kit_count"));
        c.setWaterKitCount(rs.getInt("water_kit_count"));
        c.setPlasmaKitCount(rs.getInt("plasma_kit_count"));
        c.setTotalPayloadKg(rs.getDouble("total_payload_kg"));
        c.setRequiredRations(rs.getInt("required_rations"));
        c.setDispatchLocation(rs.getString("dispatch_location"));
        c.setCargoManifest(rs.getString("cargo_manifest"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) c.setCreatedAt(ct.toLocalDateTime());
        return c;
    };

    private final RowMapper<TransportAsset> assetRowMapper = (rs, rowNum) -> {
        TransportAsset a = new TransportAsset();
        a.setId(rs.getLong("id"));
        a.setAssetCode(rs.getString("asset_code"));
        a.setAssetName(rs.getString("asset_name"));
        a.setCategory(rs.getString("category"));
        a.setMaxPayloadKg(rs.getDouble("max_payload_kg"));
        a.setMaxRangeKm(rs.getDouble("max_range_km"));
        a.setWeatherRiskLimit(rs.getDouble("weather_risk_limit"));
        a.setStatus(rs.getString("status"));
        a.setCurrentLocation(rs.getString("current_location"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) a.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) a.setUpdatedAt(ut.toLocalDateTime());
        return a;
    };

    private final RowMapper<DispatchMission> missionRowMapper = (rs, rowNum) -> {
        DispatchMission m = new DispatchMission();
        m.setId(rs.getLong("id"));
        m.setMissionCode(rs.getString("mission_code"));
        m.setDisasterZoneId(rs.getLong("disaster_zone_id"));
        long cargoId = rs.getLong("relief_cargo_id");
        if (!rs.wasNull()) m.setReliefCargoId(cargoId);
        long assetId = rs.getLong("transport_asset_id");
        if (!rs.wasNull()) m.setTransportAssetId(assetId);
        m.setDdusScore(rs.getDouble("ddus_score"));
        m.setPriorityLevel(rs.getString("priority_level"));
        m.setTransportCategory(rs.getString("transport_category"));
        double suitScore = rs.getDouble("transport_suitability_score");
        if (!rs.wasNull()) m.setTransportSuitabilityScore(suitScore);
        m.setWeatherValidationPassed(rs.getBoolean("weather_validation_passed"));
        m.setRejectionReason(rs.getString("rejection_reason"));
        m.setStatus(rs.getString("status"));
        m.setEstimatedDurationMin(rs.getInt("estimated_duration_min"));

        Timestamp da = rs.getTimestamp("dispatched_at");
        if (da != null) m.setDispatchedAt(da.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("completed_at");
        if (ca != null) m.setCompletedAt(ca.toLocalDateTime());
        Timestamp cr = rs.getTimestamp("created_at");
        if (cr != null) m.setCreatedAt(cr.toLocalDateTime());
        Timestamp up = rs.getTimestamp("updated_at");
        if (up != null) m.setUpdatedAt(up.toLocalDateTime());

        // Join columns if present in SELECT query
        try { m.setZoneCode(rs.getString("zone_code")); } catch (Exception ignored) {}
        try { m.setZoneName(rs.getString("zone_name")); } catch (Exception ignored) {}
        try { m.setAssetCode(rs.getString("asset_code")); } catch (Exception ignored) {}
        try { m.setAssetName(rs.getString("asset_name")); } catch (Exception ignored) {}
        return m;
    };

    private final RowMapper<FieldConfirmation> confirmationRowMapper = (rs, rowNum) -> {
        FieldConfirmation fc = new FieldConfirmation();
        fc.setId(rs.getLong("id"));
        fc.setMissionId(rs.getLong("mission_id"));
        fc.setFlightTransitDurationMin(rs.getInt("flight_transit_duration_min"));
        fc.setDropOffPrecisionMeters(rs.getDouble("drop_off_precision_meters"));
        fc.setFieldOfficerName(rs.getString("field_officer_name"));
        fc.setDeliveredRations(rs.getInt("delivered_rations"));
        Timestamp ft = rs.getTimestamp("field_confirmation_timestamp");
        if (ft != null) fc.setFieldConfirmationTimestamp(ft.toLocalDateTime());
        fc.setDeliveryNotes(rs.getString("delivery_notes"));
        fc.setDeliveryVerified(rs.getBoolean("delivery_verified"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) fc.setCreatedAt(ct.toLocalDateTime());
        try { fc.setMissionCode(rs.getString("mission_code")); } catch (Exception ignored) {}
        try { fc.setZoneCode(rs.getString("zone_code")); } catch (Exception ignored) {}
        return fc;
    };

    private Long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;
        try {
            Number num = keyHolder.getKey();
            if (num != null) return num.longValue();
        } catch (Exception ignored) {
            // Handles multi-column generated keys (e.g. id + timestamps)
        }
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null) {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number n) {
                    return n.longValue();
                }
            }
        }
        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> firstRow = keyHolder.getKeyList().get(0);
            for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number n) {
                    return n.longValue();
                }
            }
        }
        return null;
    }

    // =========================================================================
    // DISASTER ZONES
    // =========================================================================

    public DisasterZone saveDisasterZone(DisasterZone zone) {
        String sql = "INSERT INTO disaster_zones (zone_code, zone_name, event_type, affected_population, " +
                "medical_urgency_score, terrain_risk, road_accessibility, weather_severity_index, flight_distance_km, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, zone.getZoneCode());
            ps.setString(2, zone.getZoneName());
            ps.setString(3, zone.getEventType());
            ps.setInt(4, zone.getAffectedPopulation());
            ps.setDouble(5, zone.getMedicalUrgencyScore());
            ps.setDouble(6, zone.getTerrainRisk());
            ps.setDouble(7, zone.getRoadAccessibility());
            ps.setDouble(8, zone.getWeatherSeverityIndex());
            ps.setDouble(9, zone.getFlightDistanceKm());
            ps.setString(10, zone.getStatus() != null ? zone.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        Long genId = extractGeneratedId(keyHolder);
        if (genId != null) {
            zone.setId(genId);
        }
        return zone;
    }

    public List<DisasterZone> findAllDisasterZones() {
        String sql = "SELECT * FROM disaster_zones ORDER BY id DESC";
        return jdbcTemplate.query(sql, zoneRowMapper);
    }

    public Optional<DisasterZone> findDisasterZoneById(Long id) {
        String sql = "SELECT * FROM disaster_zones WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, zoneRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<DisasterZone> findDisasterZoneByCode(String zoneCode) {
        String sql = "SELECT * FROM disaster_zones WHERE zone_code = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, zoneRowMapper, zoneCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateDisasterZone(DisasterZone zone) {
        String sql = "UPDATE disaster_zones SET zone_name = ?, event_type = ?, affected_population = ?, " +
                "medical_urgency_score = ?, terrain_risk = ?, road_accessibility = ?, weather_severity_index = ?, " +
                "flight_distance_km = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, zone.getZoneName(), zone.getEventType(), zone.getAffectedPopulation(),
                zone.getMedicalUrgencyScore(), zone.getTerrainRisk(), zone.getRoadAccessibility(),
                zone.getWeatherSeverityIndex(), zone.getFlightDistanceKm(), zone.getStatus(), zone.getId());
    }

    // =========================================================================
    // RELIEF CARGO
    // =========================================================================

    public ReliefCargo saveCargo(ReliefCargo cargo) {
        String sql = "INSERT INTO relief_cargo (disaster_zone_id, food_kit_count, water_kit_count, " +
                "plasma_kit_count, total_payload_kg, required_rations, dispatch_location, cargo_manifest) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, cargo.getDisasterZoneId());
            ps.setInt(2, cargo.getFoodKitCount());
            ps.setInt(3, cargo.getWaterKitCount());
            ps.setInt(4, cargo.getPlasmaKitCount());
            ps.setDouble(5, cargo.getTotalPayloadKg());
            ps.setInt(6, cargo.getRequiredRations());
            ps.setString(7, cargo.getDispatchLocation());
            ps.setString(8, cargo.getCargoManifest());
            return ps;
        }, keyHolder);

        Long genId = extractGeneratedId(keyHolder);
        if (genId != null) {
            cargo.setId(genId);
        }
        return cargo;
    }

    public List<ReliefCargo> findAllCargo() {
        String sql = "SELECT * FROM relief_cargo ORDER BY id DESC";
        return jdbcTemplate.query(sql, cargoRowMapper);
    }

    public Optional<ReliefCargo> findCargoById(Long id) {
        String sql = "SELECT * FROM relief_cargo WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, cargoRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ReliefCargo> findCargoByZoneId(Long zoneId) {
        String sql = "SELECT * FROM relief_cargo WHERE disaster_zone_id = ? ORDER BY id DESC LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, cargoRowMapper, zoneId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // =========================================================================
    // TRANSPORT ASSETS
    // =========================================================================

    public List<TransportAsset> findAllTransportAssets() {
        String sql = "SELECT * FROM transport_assets ORDER BY id ASC";
        return jdbcTemplate.query(sql, assetRowMapper);
    }

    public List<TransportAsset> findAvailableTransportAssets() {
        String sql = "SELECT * FROM transport_assets WHERE status = 'AVAILABLE' ORDER BY max_payload_kg DESC";
        return jdbcTemplate.query(sql, assetRowMapper);
    }

    public Optional<TransportAsset> findTransportAssetById(Long id) {
        String sql = "SELECT * FROM transport_assets WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, assetRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public TransportAsset saveTransportAsset(TransportAsset asset) {
        String sql = "INSERT INTO transport_assets (asset_code, asset_name, category, max_payload_kg, " +
                "max_range_km, weather_risk_limit, status, current_location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, asset.getAssetCode());
            ps.setString(2, asset.getAssetName());
            ps.setString(3, asset.getCategory());
            ps.setDouble(4, asset.getMaxPayloadKg());
            ps.setDouble(5, asset.getMaxRangeKm());
            ps.setDouble(6, asset.getWeatherRiskLimit());
            ps.setString(7, asset.getStatus() != null ? asset.getStatus() : "AVAILABLE");
            ps.setString(8, asset.getCurrentLocation());
            return ps;
        }, keyHolder);

        Long genId = extractGeneratedId(keyHolder);
        if (genId != null) {
            asset.setId(genId);
        }
        return asset;
    }

    public void updateTransportAssetStatus(Long assetId, String status) {
        String sql = "UPDATE transport_assets SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, assetId);
    }

    /**
     * Atomically reserves an available asset to prevent race conditions during multithreaded dispatch.
     * @return true if reservation succeeded, false if already reserved/occupied by another thread.
     */
    public synchronized boolean reserveAssetAtomically(Long assetId) {
        String sql = "UPDATE transport_assets SET status = 'OCCUPIED' WHERE id = ? AND status = 'AVAILABLE'";
        int rowsUpdated = jdbcTemplate.update(sql, assetId);
        return rowsUpdated > 0;
    }

    public void releaseAsset(Long assetId) {
        String sql = "UPDATE transport_assets SET status = 'AVAILABLE' WHERE id = ?";
        jdbcTemplate.update(sql, assetId);
    }

    // =========================================================================
    // DISPATCH MISSIONS
    // =========================================================================

    public DispatchMission saveMission(DispatchMission m) {
        String sql = "INSERT INTO dispatch_missions (mission_code, disaster_zone_id, relief_cargo_id, transport_asset_id, " +
                "ddus_score, priority_level, transport_category, transport_suitability_score, weather_validation_passed, " +
                "rejection_reason, status, dispatched_at, estimated_duration_min, completed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, m.getMissionCode());
            ps.setLong(2, m.getDisasterZoneId());
            if (m.getReliefCargoId() != null) ps.setLong(3, m.getReliefCargoId()); else ps.setNull(3, java.sql.Types.BIGINT);
            if (m.getTransportAssetId() != null) ps.setLong(4, m.getTransportAssetId()); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setDouble(5, m.getDdusScore());
            ps.setString(6, m.getPriorityLevel());
            ps.setString(7, m.getTransportCategory());
            if (m.getTransportSuitabilityScore() != null) ps.setDouble(8, m.getTransportSuitabilityScore()); else ps.setNull(8, java.sql.Types.DOUBLE);
            ps.setBoolean(9, m.isWeatherValidationPassed());
            ps.setString(10, m.getRejectionReason());
            ps.setString(11, m.getStatus());
            if (m.getDispatchedAt() != null) ps.setTimestamp(12, Timestamp.valueOf(m.getDispatchedAt())); else ps.setNull(12, java.sql.Types.TIMESTAMP);
            ps.setInt(13, m.getEstimatedDurationMin());
            if (m.getCompletedAt() != null) ps.setTimestamp(14, Timestamp.valueOf(m.getCompletedAt())); else ps.setNull(14, java.sql.Types.TIMESTAMP);
            return ps;
        }, keyHolder);

        Long genId = extractGeneratedId(keyHolder);
        if (genId != null) {
            m.setId(genId);
        }
        return m;
    }

    public List<DispatchMission> findAllMissions() {
        String sql = "SELECT m.*, z.zone_code, z.zone_name, a.asset_code, a.asset_name " +
                "FROM dispatch_missions m " +
                "LEFT JOIN disaster_zones z ON m.disaster_zone_id = z.id " +
                "LEFT JOIN transport_assets a ON m.transport_asset_id = a.id " +
                "ORDER BY m.id DESC";
        return jdbcTemplate.query(sql, missionRowMapper);
    }

    public Optional<DispatchMission> findMissionById(Long id) {
        String sql = "SELECT m.*, z.zone_code, z.zone_name, a.asset_code, a.asset_name " +
                "FROM dispatch_missions m " +
                "LEFT JOIN disaster_zones z ON m.disaster_zone_id = z.id " +
                "LEFT JOIN transport_assets a ON m.transport_asset_id = a.id " +
                "WHERE m.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, missionRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<DispatchMission> findMissionsByStatus(String status) {
        String sql = "SELECT m.*, z.zone_code, z.zone_name, a.asset_code, a.asset_name " +
                "FROM dispatch_missions m " +
                "LEFT JOIN disaster_zones z ON m.disaster_zone_id = z.id " +
                "LEFT JOIN transport_assets a ON m.transport_asset_id = a.id " +
                "WHERE m.status = ? ORDER BY m.id DESC";
        return jdbcTemplate.query(sql, missionRowMapper, status);
    }

    public void updateMissionStatus(Long missionId, String status, LocalDateTime completedAt) {
        String sql = "UPDATE dispatch_missions SET status = ?, completed_at = ? WHERE id = ?";
        Timestamp compTs = completedAt != null ? Timestamp.valueOf(completedAt) : null;
        jdbcTemplate.update(sql, status, compTs, missionId);
    }

    // =========================================================================
    // FIELD CONFIRMATIONS
    // =========================================================================

    public FieldConfirmation saveFieldConfirmation(FieldConfirmation fc) {
        String sql = "INSERT INTO field_confirmations (mission_id, flight_transit_duration_min, drop_off_precision_meters, " +
                "field_officer_name, delivered_rations, delivery_notes, delivery_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, fc.getMissionId());
            ps.setInt(2, fc.getFlightTransitDurationMin());
            ps.setDouble(3, fc.getDropOffPrecisionMeters());
            ps.setString(4, fc.getFieldOfficerName());
            ps.setInt(5, fc.getDeliveredRations());
            ps.setString(6, fc.getDeliveryNotes());
            ps.setBoolean(7, fc.isDeliveryVerified());
            return ps;
        }, keyHolder);

        Long genId = extractGeneratedId(keyHolder);
        if (genId != null) {
            fc.setId(genId);
        }
        return fc;
    }

    public List<FieldConfirmation> findAllFieldConfirmations() {
        String sql = "SELECT fc.*, m.mission_code, z.zone_code " +
                "FROM field_confirmations fc " +
                "JOIN dispatch_missions m ON fc.mission_id = m.id " +
                "JOIN disaster_zones z ON m.disaster_zone_id = z.id " +
                "ORDER BY fc.id DESC";
        return jdbcTemplate.query(sql, confirmationRowMapper);
    }

    public Optional<FieldConfirmation> findFieldConfirmationByMissionId(Long missionId) {
        String sql = "SELECT fc.*, m.mission_code, z.zone_code " +
                "FROM field_confirmations fc " +
                "JOIN dispatch_missions m ON fc.mission_id = m.id " +
                "JOIN disaster_zones z ON m.disaster_zone_id = z.id " +
                "WHERE fc.mission_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, confirmationRowMapper, missionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // =========================================================================
    // DISPATCH LOGS
    // =========================================================================

    public void insertLog(DispatchLog log) {
        String sql = "INSERT INTO dispatch_logs (mission_id, thread_name, request_id, log_level, action, message) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, log.getMissionId(), log.getThreadName(), log.getRequestId(),
                log.getLogLevel(), log.getAction(), log.getMessage());
    }

    public List<DispatchLog> getRecentLogs(int limit) {
        String sql = "SELECT * FROM dispatch_logs ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DispatchLog dl = new DispatchLog();
            dl.setId(rs.getLong("id"));
            long mId = rs.getLong("mission_id");
            if (!rs.wasNull()) dl.setMissionId(mId);
            dl.setThreadName(rs.getString("thread_name"));
            dl.setRequestId(rs.getString("request_id"));
            dl.setLogLevel(rs.getString("log_level"));
            dl.setAction(rs.getString("action"));
            dl.setMessage(rs.getString("message"));
            Timestamp ts = rs.getTimestamp("timestamp");
            if (ts != null) dl.setTimestamp(ts.toLocalDateTime());
            return dl;
        }, limit);
    }

    // =========================================================================
    // CSV IMPORT HISTORY
    // =========================================================================

    public void recordImportHistory(CsvImportHistory history) {
        String sql = "INSERT INTO csv_import_history (filename, total_rows, successful_rows, rejected_rows, error_summary) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, history.getFilename(), history.getTotalRows(), history.getSuccessfulRows(),
                history.getRejectedRows(), history.getErrorSummary());
    }

    // =========================================================================
    // DASHBOARD & ANALYTICS AGGREGATIONS
    // =========================================================================

    public Map<String, Integer> getCountsByColumn(String table, String column) {
        String sql = String.format("SELECT %s, COUNT(*) AS cnt FROM %s GROUP BY %s", column, table, column);
        Map<String, Integer> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString(1);
            if (key == null) key = "UNKNOWN";
            int count = rs.getInt(2);
            result.put(key, count);
        });
        return result;
    }

    public int countRows(String table, String whereClause) {
        String sql = "SELECT COUNT(*) FROM " + table + (whereClause != null ? " WHERE " + whereClause : "");
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}

