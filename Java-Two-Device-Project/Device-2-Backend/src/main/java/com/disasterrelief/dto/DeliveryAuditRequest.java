package com.disasterrelief.dto;

import jakarta.validation.constraints.*;

public class DeliveryAuditRequest {

    @PositiveOrZero(message = "Flight/Transit duration must be non-negative")
    private int flightTransitDurationMin;

    @DecimalMin(value = "0.0", message = "Drop-off precision meters must be non-negative")
    private double dropOffPrecisionMeters;

    @NotBlank(message = "Field officer name is required")
    private String fieldOfficerName;

    @PositiveOrZero(message = "Delivered rations must be non-negative")
    private int deliveredRations;

    private String deliveryNotes;

    private boolean deliveryVerified = true;

    public DeliveryAuditRequest() {}

    public int getFlightTransitDurationMin() { return flightTransitDurationMin; }
    public void setFlightTransitDurationMin(int flightTransitDurationMin) { this.flightTransitDurationMin = flightTransitDurationMin; }

    public double getDropOffPrecisionMeters() { return dropOffPrecisionMeters; }
    public void setDropOffPrecisionMeters(double dropOffPrecisionMeters) { this.dropOffPrecisionMeters = dropOffPrecisionMeters; }

    public String getFieldOfficerName() { return fieldOfficerName; }
    public void setFieldOfficerName(String fieldOfficerName) { this.fieldOfficerName = fieldOfficerName; }

    public int getDeliveredRations() { return deliveredRations; }
    public void setDeliveredRations(int deliveredRations) { this.deliveredRations = deliveredRations; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public boolean isDeliveryVerified() { return deliveryVerified; }
    public void setDeliveryVerified(boolean deliveryVerified) { this.deliveryVerified = deliveryVerified; }
}

