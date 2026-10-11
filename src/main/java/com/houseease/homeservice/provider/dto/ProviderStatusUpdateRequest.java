package com.houseease.homeservice.provider.dto;

import lombok.Data;

@Data
public class ProviderStatusUpdateRequest {
    private String status; // APPROVED, SUSPENDED
}
