package com.houseease.homeservice.availability.dto;

import lombok.Builder;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class BlockedSlotDto {
    private UUID id;
    private ZonedDateTime startDatetime;
    private ZonedDateTime endDatetime;
    private String reason;
}
