package com.houseease.homeservice.servicecatalog.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceRequest {
    private UUID categoryId;
    private String name;
    private String description;

    private Integer durationMinutes;
    private Boolean supportsHomeService;
}
