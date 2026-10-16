package com.houseease.homeservice.review;

import com.houseease.homeservice.auth.CustomUserDetails;
import com.houseease.homeservice.review.dto.CreateReviewRequest;
import com.houseease.homeservice.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Submit a review for a completed booking.
     * Enforces CUSTOMER role and booking completion.
     */
    @PostMapping("/api/bookings/{id}/review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable("id") UUID bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("[Booking ID: {}] POST /api/bookings/{}/review called by user {}", 
                bookingId, bookingId, userDetails.getUser().getId());
        ReviewResponse response = reviewService.createReview(userDetails.getUser().getId(), bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get review for a specific booking.
     * Returns 200 with review if exists, or 404 if booking has not been reviewed yet.
     */
    @GetMapping("/api/bookings/{id}/review")
    public ResponseEntity<ReviewResponse> getBookingReview(@PathVariable("id") UUID bookingId) {
        log.info("[Booking ID: {}] GET /api/bookings/{}/review called", bookingId, bookingId);
        ReviewResponse response = reviewService.getBookingReview(bookingId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get all reviews for a provider.
     * Public endpoint to view provider's ratings and customer reviews.
     */
    @GetMapping({"/api/providers/{id}/reviews", "/api/providers/{Id}/reviews"})
    public ResponseEntity<List<ReviewResponse>> getProviderReviews(@PathVariable("id") UUID providerId) {
        log.info("[Booking ID: N/A] GET /api/providers/{}/reviews called", providerId);
        List<ReviewResponse> reviews = reviewService.getProviderReviews(providerId);
        return ResponseEntity.ok(reviews);
    }
}
