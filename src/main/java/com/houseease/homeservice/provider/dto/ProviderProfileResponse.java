package com.houseease.homeservice.provider.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProviderProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String bio;
    private String status;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private List<ProviderServiceDto> services;
}
