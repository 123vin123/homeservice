package com.houseease.homeservice.availability;

import com.houseease.homeservice.auth.CustomUserDetails;
import com.houseease.homeservice.availability.dto.AvailabilityDto;
import com.houseease.homeservice.availability.dto.AvailabilityResponse;
import com.houseease.homeservice.availability.dto.BlockedSlotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> getProviderAvailability(@PathVariable UUID id) {
        return ResponseEntity.ok(availabilityService.getProviderAvailability(id));
    }

    @PostMapping("/me/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<AvailabilityDto> addAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AvailabilityDto request) {
        return ResponseEntity.ok(availabilityService.addAvailability(userDetails.getUser().getId(), request));
    }

    @DeleteMapping("/me/availability/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> removeAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID availabilityId) {
        availabilityService.removeAvailability(userDetails.getUser().getId(), availabilityId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/blocked-slots")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BlockedSlotDto> addBlockedSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BlockedSlotDto request) {
        return ResponseEntity.ok(availabilityService.addBlockedSlot(userDetails.getUser().getId(), request));
    }

    @DeleteMapping("/me/blocked-slots/{slotId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> removeBlockedSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID slotId) {
        availabilityService.removeBlockedSlot(userDetails.getUser().getId(), slotId);
        return ResponseEntity.noContent().build();
    }
}
