package com.houseease.homeservice.review;

import com.houseease.homeservice.booking.Booking;
import com.houseease.homeservice.booking.BookingRepository;
import com.houseease.homeservice.booking.BookingStatus;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import com.houseease.homeservice.review.dto.CreateReviewRequest;
import com.houseease.homeservice.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ProviderProfileRepository providerProfileRepository;

    /**
     * Submit a review for a completed booking.
     * Enforces customer ownership, COMPLETED status, and uniqueness.
     */
    @Transactional
    public ReviewResponse createReview(UUID customerId, UUID bookingId, CreateReviewRequest request) {
        log.info("[Booking ID: {}] Processing review submission from customer {}", bookingId, customerId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("[Booking ID: {}] Booking not found during review submission", bookingId);
                    return new IllegalArgumentException("Booking not found: " + bookingId);
                });

        // 1. Validate customer ownership
        if (!booking.getCustomer().getId().equals(customerId)) {
            log.warn("[Booking ID: {}] Review rejected: Customer {} does not own this booking", bookingId, customerId);
            throw new SecurityException("You can only review your own bookings");
        }

        // 2. Validate booking status is COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            log.warn("[Booking ID: {}] Review rejected: Booking status is {}, only COMPLETED bookings can be reviewed",
                    bookingId, booking.getStatus());
            throw new IllegalStateException("Only completed bookings can be reviewed. Current status is " + booking.getStatus());
        }

        // 3. Prevent duplicate reviews for the same booking
        if (reviewRepository.existsByBookingId(bookingId)) {
            log.warn("[Booking ID: {}] Review rejected: Booking has already been reviewed", bookingId);
            throw new IllegalStateException("You have already reviewed this booking");
        }

        // 4. Save review
        Review review = Review.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .provider(booking.getProvider())
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .build();

        review = reviewRepository.save(review);

        // 5. Update provider rating statistics
        ProviderProfile provider = booking.getProvider();
        Double averageRating = reviewRepository.getAverageRatingByProviderId(provider.getId());
        Long totalReviews = reviewRepository.countByProviderId(provider.getId());

        if (averageRating != null) {
            provider.setAverageRating(BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP));
        } else {
            provider.setAverageRating(BigDecimal.ZERO);
        }
        provider.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);
        providerProfileRepository.save(provider);

        log.info("[Booking ID: {}] Review saved successfully. Rating: {}, Provider: {}, New Average: {}, Total Reviews: {}",
                bookingId, request.getRating(), provider.getId(), provider.getAverageRating(), provider.getTotalReviews());

        return mapToResponse(review);
    }

    /**
     * Get review associated with a booking.
     */
    @Transactional(readOnly = true)
    public ReviewResponse getBookingReview(UUID bookingId) {
        log.info("[Booking ID: {}] Querying review for booking", bookingId);
        return reviewRepository.findByBookingId(bookingId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Get all reviews for a provider.
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProviderReviews(UUID providerId) {
        log.info("[Booking ID: N/A] Fetching reviews for provider ID: {}", providerId);
        return reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review r) {
        String customerName = "Customer";
        if (r.getCustomer() != null) {
            String first = r.getCustomer().getFirstName() != null ? r.getCustomer().getFirstName() : "";
            String last = r.getCustomer().getLastName() != null ? r.getCustomer().getLastName() : "";
            customerName = (first + " " + last).trim();
            if (customerName.isEmpty()) {
                customerName = r.getCustomer().getEmail();
            }
        }

        String providerName = "Provider";
        if (r.getProvider() != null && r.getProvider().getUser() != null) {
            String first = r.getProvider().getUser().getFirstName() != null ? r.getProvider().getUser().getFirstName() : "";
            String last = r.getProvider().getUser().getLastName() != null ? r.getProvider().getUser().getLastName() : "";
            providerName = (first + " " + last).trim();
            if (providerName.isEmpty()) {
                providerName = r.getProvider().getUser().getEmail();
            }
        }

        String serviceName = (r.getBooking() != null && r.getBooking().getService() != null)
                ? r.getBooking().getService().getName()
                : "Service";

        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .customerId(r.getCustomer().getId())
                .customerName(customerName)
                .providerId(r.getProvider().getId())
                .providerName(providerName)
                .serviceName(serviceName)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
