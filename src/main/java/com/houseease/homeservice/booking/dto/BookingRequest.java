package com.houseease.homeservice.booking.dto;

import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class BookingRequest {
    private UUID providerId;
    private UUID serviceId;
    private UUID addressId;
    private ZonedDateTime scheduledTime;
    private String notes;
    private String paymentMethod;
}
