package com.disasterrelief.model;

import java.time.LocalDateTime;

public class FieldConfirmation {
    private Long id;
    private Long missionId;
    private int flightTransitDurationMin;
    private double dropOffPrecisionMeters;
    private String fieldOfficerName;
    private int deliveredRations;
    private LocalDateTime fieldConfirmationTimestamp;
    private String deliveryNotes;
    private boolean deliveryVerified;
    private LocalDateTime createdAt;

    // Joined fields
    private String missionCode;
    private String zoneCode;

    public FieldConfirmation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public int getFlightTransitDurationMin() { return flightTransitDurationMin; }
    public void setFlightTransitDurationMin(int flightTransitDurationMin) { this.flightTransitDurationMin = flightTransitDurationMin; }

    public double getDropOffPrecisionMeters() { return dropOffPrecisionMeters; }
    public void setDropOffPrecisionMeters(double dropOffPrecisionMeters) { this.dropOffPrecisionMeters = dropOffPrecisionMeters; }

    public String getFieldOfficerName() { return fieldOfficerName; }
    public void setFieldOfficerName(String fieldOfficerName) { this.fieldOfficerName = fieldOfficerName; }

    public int getDeliveredRations() { return deliveredRations; }
    public void setDeliveredRations(int deliveredRations) { this.deliveredRations = deliveredRations; }

    public LocalDateTime getFieldConfirmationTimestamp() { return fieldConfirmationTimestamp; }
    public void setFieldConfirmationTimestamp(LocalDateTime fieldConfirmationTimestamp) { this.fieldConfirmationTimestamp = fieldConfirmationTimestamp; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public boolean isDeliveryVerified() { return deliveryVerified; }
    public void setDeliveryVerified(boolean deliveryVerified) { this.deliveryVerified = deliveryVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }
}

