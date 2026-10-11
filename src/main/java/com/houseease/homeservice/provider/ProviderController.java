package com.houseease.homeservice.provider;

import com.houseease.homeservice.auth.CustomUserDetails;
import com.houseease.homeservice.provider.dto.ProviderProfileResponse;
import com.houseease.homeservice.provider.dto.ProviderProfileUpdateRequest;
import com.houseease.homeservice.provider.dto.ProviderStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderManagementService providerService;

    @GetMapping("/{id}")
    public ResponseEntity<ProviderProfileResponse> getProviderProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getProviderProfile(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(providerService.getProviderProfile(userDetails.getUser().getId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProviderProfileUpdateRequest request) {
        return ResponseEntity.ok(providerService.updateMyProfile(userDetails.getUser().getId(), request));
    }

    @PostMapping("/me/services/{serviceId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> addService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID serviceId,
            @RequestParam(required = false) BigDecimal customPrice,
            @RequestParam(required = false) Integer estimatedDurationMinutes) {
        return ResponseEntity
                .ok(providerService.addServiceToProvider(userDetails.getUser().getId(), serviceId, customPrice, estimatedDurationMinutes));
    }

    @DeleteMapping("/me/services/{serviceId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> removeService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID serviceId) {
        return ResponseEntity.ok(providerService.removeServiceFromProvider(userDetails.getUser().getId(), serviceId));
    }

    // Admin endpoints
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProviderProfileResponse>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderProfileResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody ProviderStatusUpdateRequest request) {
        return ResponseEntity.ok(providerService.updateProviderStatus(id, request.getStatus()));
    }
}
