package com.houseease.homeservice.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID bookingId;
    private UUID customerId;
    private String customerName;
    private UUID providerId;
    private String providerName;
    private String serviceName;
    private Integer rating;
    private String comment;
    private ZonedDateTime createdAt;
}
