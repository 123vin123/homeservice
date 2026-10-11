package com.houseease.homeservice.location;

import com.houseease.homeservice.auth.CustomUserDetails;
import com.houseease.homeservice.location.dto.AddressDto;
import com.houseease.homeservice.location.dto.ProviderServiceAreaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Customer Endpoints
    @GetMapping("/addresses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<AddressDto>> getMyAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(locationService.getMyAddresses(userDetails.getUser().getId()));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<AddressDto> addAddress(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddressDto request) {
        return ResponseEntity.ok(locationService.addAddress(userDetails.getUser().getId(), request));
    }

    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<AddressDto> updateAddress(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID id, @RequestBody AddressDto request) {
        return ResponseEntity.ok(locationService.updateAddress(userDetails.getUser().getId(), id, request));
    }

    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID id) {
        locationService.deleteAddress(userDetails.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    // Provider Endpoints
    @GetMapping("/service-areas")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ProviderServiceAreaDto>> getMyServiceAreas(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(locationService.getMyServiceAreas(userDetails.getUser().getId()));
    }

    @PostMapping("/service-areas")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderServiceAreaDto> addServiceArea(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ProviderServiceAreaDto request) {
        return ResponseEntity.ok(locationService.addServiceArea(userDetails.getUser().getId(), request));
    }

    @DeleteMapping("/service-areas/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deleteServiceArea(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID id) {
        locationService.deleteServiceArea(userDetails.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }
}
