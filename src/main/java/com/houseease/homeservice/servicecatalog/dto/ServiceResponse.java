package com.houseease.homeservice.servicecatalog.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ServiceResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;

    private Integer durationMinutes;
    private Boolean supportsHomeService;
}
