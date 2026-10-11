package com.houseease.homeservice.availability.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AvailabilityResponse {
    private UUID providerId;
    private List<AvailabilityDto> weeklyAvailability;
    private List<BlockedSlotDto> blockedSlots;
}
