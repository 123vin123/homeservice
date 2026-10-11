package com.houseease.homeservice.location.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AddressDto {
    private UUID id;
    private String label;
    private String addressLine;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
