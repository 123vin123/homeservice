package com.houseease.homeservice.review;

import com.houseease.homeservice.booking.Booking;
import com.houseease.homeservice.booking.BookingRepository;
import com.houseease.homeservice.booking.BookingStatus;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import com.houseease.homeservice.review.dto.CreateReviewRequest;
import com.houseease.homeservice.review.dto.ReviewResponse;
import com.houseease.homeservice.servicecatalog.ServiceEntity;
import com.houseease.homeservice.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ProviderProfileRepository providerProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID customerId;
    private UUID providerId;
    private UUID bookingId;
    private User customer;
    private ProviderProfile providerProfile;
    private Booking completedBooking;
    private ServiceEntity service;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        customer = new User();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        User providerUser = new User();
        providerUser.setId(providerId);
        providerUser.setFirstName("Jane");
        providerUser.setLastName("Electrician");
        providerUser.setEmail("jane@example.com");

        providerProfile = ProviderProfile.builder()
                .id(providerId)
                .user(providerUser)
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        service = new ServiceEntity();
        service.setName("AC Repair");

        completedBooking = new Booking();
        completedBooking.setId(bookingId);
        completedBooking.setCustomer(customer);
        completedBooking.setProvider(providerProfile);
        completedBooking.setService(service);
        completedBooking.setStatus(BookingStatus.COMPLETED);
    }

    @Test
    void testCreateReview_Success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Outstanding service!")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);

        Review savedReview = Review.builder()
                .id(UUID.randomUUID())
                .booking(completedBooking)
                .customer(customer)
                .provider(providerProfile)
                .rating(5)
                .comment("Outstanding service!")
                .createdAt(ZonedDateTime.now())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.getAverageRatingByProviderId(providerId)).thenReturn(5.0);
        when(reviewRepository.countByProviderId(providerId)).thenReturn(1L);

        ReviewResponse response = reviewService.createReview(customerId, bookingId, request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Outstanding service!", response.getComment());
        assertEquals("AC Repair", response.getServiceName());
        assertEquals("John Doe", response.getCustomerName());

        // Verify provider statistics updated
        assertEquals(BigDecimal.valueOf(5.0).setScale(1), providerProfile.getAverageRating());
        assertEquals(1, providerProfile.getTotalReviews());
        verify(providerProfileRepository, times(1)).save(providerProfile);
    }

    @Test
    void testCreateReview_ThrowsException_WhenBookingNotCompleted() {
        completedBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));

        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(4)
                .comment("Not finished yet")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                reviewService.createReview(customerId, bookingId, request));

        assertTrue(exception.getMessage().contains("Only completed bookings can be reviewed"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_ThrowsException_WhenUserNotBookingCustomer() {
        UUID strangerId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));

        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(4)
                .comment("Good")
                .build();

        SecurityException exception = assertThrows(SecurityException.class, () ->
                reviewService.createReview(strangerId, bookingId, request));

        assertTrue(exception.getMessage().contains("You can only review your own bookings"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_ThrowsException_WhenDuplicateReview() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Second attempt")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                reviewService.createReview(customerId, bookingId, request));

        assertTrue(exception.getMessage().contains("You have already reviewed this booking"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetProviderReviews_ReturnsReviews() {
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .booking(completedBooking)
                .customer(customer)
                .provider(providerProfile)
                .rating(5)
                .comment("Excellent job")
                .createdAt(ZonedDateTime.now())
                .build();

        when(reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId)).thenReturn(List.of(review));

        List<ReviewResponse> reviews = reviewService.getProviderReviews(providerId);

        assertEquals(1, reviews.size());
        assertEquals("Excellent job", reviews.get(0).getComment());
        assertEquals(5, reviews.get(0).getRating());
    }
}
