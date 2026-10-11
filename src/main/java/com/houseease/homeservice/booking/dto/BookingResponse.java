package com.houseease.homeservice.booking.dto;

import com.houseease.homeservice.booking.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private UUID providerId;
    private String providerName;
    private String serviceName;
    private BigDecimal totalPrice;
    private String addressLine;
    private String city;
    private BookingStatus status;
    private ZonedDateTime scheduledTime;
    private String notes;
    private String paymentMethod;
    private String transactionId;
}
