package com.houseease.homeservice.booking;

import com.houseease.homeservice.auth.CustomUserDetails;
import com.houseease.homeservice.booking.dto.BookingRequest;
import com.houseease.homeservice.booking.dto.BookingResponse;
import com.houseease.homeservice.booking.dto.BookingStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller exposing all the REST API endpoints for Booking management.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Customers call this to create a new booking for a selected provider and service.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all bookings made by the currently logged-in customer.
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyCustomerBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BookingResponse> responses = bookingService.getCustomerBookings(userDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all bookings assigned to the currently logged-in provider.
     */
    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<BookingResponse>> getMyProviderBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BookingResponse> responses = bookingService.getProviderBookings(userDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * Updates the status of a booking.
     * Accessible by either the CUSTOMER (e.g., to cancel) or the PROVIDER (to confirm/complete).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody BookingStatusUpdateRequest request) {
        BookingResponse response = bookingService.updateBookingStatus(userDetails.getUser().getId(), id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
