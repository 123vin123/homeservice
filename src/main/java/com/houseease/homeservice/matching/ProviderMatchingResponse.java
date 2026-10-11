package com.houseease.homeservice.matching;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProviderMatchingResponse {
    private UUID providerId;
    private String firstName;
    private String lastName;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private BigDecimal price; // Might be custom_price or base_price
    private Double distanceKm; // Calculated distance from customer
}
