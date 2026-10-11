package com.houseease.homeservice.provider.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProviderServiceDto {
    private UUID serviceId;
    private String serviceName;
    private BigDecimal customPrice;
    private Integer estimatedDurationMinutes;

}
