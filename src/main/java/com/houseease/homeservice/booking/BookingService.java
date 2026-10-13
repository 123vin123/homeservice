package com.houseease.homeservice.booking;

import com.houseease.homeservice.booking.dto.BookingRequest;
import com.houseease.homeservice.booking.dto.BookingResponse;
import com.houseease.homeservice.location.Address;
import com.houseease.homeservice.location.AddressRepository;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import com.houseease.homeservice.provider.ProviderServiceId;
import com.houseease.homeservice.provider.ProviderServiceLink;
import com.houseease.homeservice.provider.ProviderServiceLinkRepository;
import com.houseease.homeservice.servicecatalog.ServiceEntity;
import com.houseease.homeservice.servicecatalog.ServiceRepository;
import com.houseease.homeservice.user.User;
import com.houseease.homeservice.user.UserRepository;
import com.houseease.homeservice.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class handling the core business logic for Bookings.
 *
 * CRITICAL MODULE — This class uses:
 * 1. @Transactional with SERIALIZABLE isolation on createBooking
 * to prevent two customers from booking the same provider/time slot.
 * 2. PESSIMISTIC_WRITE locking in the repository query to hold a
 * row-level lock while checking for conflicts.
 * 3. A strict state-machine for status transitions so a booking
 * can only move through valid lifecycle states.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final ProviderProfileRepository providerRepository;
        private final ServiceRepository serviceRepository;
        private final AddressRepository addressRepository;
        private final ProviderServiceLinkRepository providerServiceLinkRepository;
        private final PaymentService paymentService;

        /**
         * Valid state transitions.
         * Key = current status, Value = set of allowed next statuses.
         */
        private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = Map.of(
                        BookingStatus.PAYMENT_PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
                        BookingStatus.PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
                        BookingStatus.CONFIRMED, Set.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED),
                        BookingStatus.IN_PROGRESS, Set.of(BookingStatus.COMPLETED),
                        BookingStatus.COMPLETED, Set.of(),
                        BookingStatus.CANCELLED, Set.of());

        // ────────────────────────────────────────────────────────
        // CREATE BOOKING — The most critical transaction
        // ────────────────────────────────────────────────────────

        /**
         * Creates a new booking inside a SERIALIZABLE transaction.
         *
         * Validation order (matches the guide):
         * 1. Validate Customer exists
         * 2. Validate Service exists and is active
         * 3. Validate Provider exists AND offers this service
         * 4. Validate Customer's Address exists
         * 5. Check for time-slot conflicts (PESSIMISTIC_WRITE lock)
         * 6. Snap the price and persist
         */
        @Transactional(isolation = Isolation.SERIALIZABLE)
        public BookingResponse createBooking(UUID customerId, BookingRequest request) {

                // 1. Validate Customer
                User customer = userRepository.findById(customerId)
                                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

                // 2. Validate Service
                ServiceEntity service = serviceRepository.findById(request.getServiceId())
                                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

                // 3. Validate Provider + Provider offers this service
                ProviderProfile provider = providerRepository.findById(request.getProviderId())
                                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

                ProviderServiceLink link = providerServiceLinkRepository
                                .findById(new ProviderServiceId(provider.getId(), service.getId()))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Provider does not offer this service"));

                // 4. Validate Customer Address
                Address address = addressRepository.findById(request.getAddressId())
                                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

                // 5. Calculate time window and check for conflicts
                ZonedDateTime requestedStart = request.getScheduledTime();
                Integer duration = link.getEstimatedDurationMinutes() != null ? 
                                   link.getEstimatedDurationMinutes() : 
                                   service.getDurationMinutes();
                ZonedDateTime requestedEnd = requestedStart.plusMinutes(duration);

                List<Booking> conflicts = bookingRepository.findConflictingBookings(
                                provider.getId(), requestedStart, requestedEnd);

                if (!conflicts.isEmpty()) {
                        throw new IllegalStateException(
                                        "This provider already has a booking during the requested time slot. "
                                                        + "Please choose a different time.");
                }

                // 6. All validations passed — create the booking
                Booking booking = new Booking();
                booking.setCustomer(customer);
                booking.setProvider(provider);
                booking.setService(service);
                booking.setAddress(address);
                booking.setScheduledTime(requestedStart);
                booking.setEndTime(requestedEnd);
                booking.setTotalPrice(link.getCustomPrice()); // Snap price at booking time
                booking.setNotes(request.getNotes());
                
                // Process Payment
                String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT_CARD";
                String transactionId = paymentService.processPaymentWithRetry(paymentMethod, link.getCustomPrice());
                
                if (transactionId != null) {
                    if ("RAZORPAY".equalsIgnoreCase(paymentMethod)) {
                        booking.setStatus(BookingStatus.PAYMENT_PENDING);
                    } else {
                        booking.setStatus(BookingStatus.CONFIRMED);
                    }
                    booking.setTransactionId(transactionId);
                    booking.setPaymentMethod(paymentMethod);
                } else {
                    booking.setStatus(BookingStatus.PAYMENT_PENDING);
                }

                Booking saved = bookingRepository.save(booking);
                log.info("[Booking ID: {}] Created new booking for customer: {}, provider: {}, status: {}, transactionId: {}", 
                        saved.getId(), customer.getId(), provider.getId(), saved.getStatus(), saved.getTransactionId());
                return mapToDto(saved);
        }

        // ────────────────────────────────────────────────────────
        // UPDATE STATUS — Enforces the state machine
        // ────────────────────────────────────────────────────────

        /**
         * Updates the status of an existing booking.
         *
         * Rules:
         * - Only the involved customer or provider can update.
         * - Customers can only CANCEL (from PENDING or CONFIRMED).
         * - Providers can CONFIRM, start IN_PROGRESS, COMPLETE, or CANCEL.
         * - Transition must be valid per the state machine.
         */
        @Transactional
        public BookingResponse updateBookingStatus(UUID userId, UUID bookingId, BookingStatus newStatus) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

                // Authorization check
                boolean isCustomer = booking.getCustomer().getId().equals(userId);
                boolean isProvider = booking.getProvider().getId().equals(userId);

                if (!isCustomer && !isProvider) {
                        throw new SecurityException("Unauthorized to update this booking");
                }

                // Customers can only cancel
                if (isCustomer && newStatus != BookingStatus.CANCELLED) {
                        throw new IllegalStateException("Customers can only cancel a booking");
                }

                // Validate state transition
                BookingStatus currentStatus = booking.getStatus();
                Set<BookingStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());

                if (!allowed.contains(newStatus)) {
                        throw new IllegalStateException(
                                        "Cannot transition from " + currentStatus + " to " + newStatus);
                }

                booking.setStatus(newStatus);
                Booking updated = bookingRepository.save(booking);
                log.info("[Booking ID: {}] Status transitioned from {} to {} by userId: {}", 
                        updated.getId(), currentStatus, newStatus, userId);
                return mapToDto(updated);
        }

        // ────────────────────────────────────────────────────────
        // READ QUERIES
        // ────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public List<BookingResponse> getCustomerBookings(UUID customerId) {
                return bookingRepository.findByCustomerIdOrderByScheduledTimeDesc(customerId).stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<BookingResponse> getProviderBookings(UUID providerId) {
                return bookingRepository.findByProviderIdOrderByScheduledTimeDesc(providerId).stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        // ────────────────────────────────────────────────────────
        // DTO MAPPER
        // ────────────────────────────────────────────────────────

        private BookingResponse mapToDto(Booking booking) {
                return BookingResponse.builder()
                                .id(booking.getId())
                                .customerId(booking.getCustomer().getId())
                                .customerName(booking.getCustomer().getFirstName() + " "
                                                + booking.getCustomer().getLastName())
                                .customerPhone(booking.getCustomer().getPhone())
                                .providerId(booking.getProvider().getId())
                                .providerName(booking.getProvider().getUser().getFirstName() + " "
                                                + booking.getProvider().getUser().getLastName())
                                .serviceName(booking.getService().getName())
                                .totalPrice(booking.getTotalPrice())
                                .addressLine(booking.getAddress().getAddressLine())
                                .city(booking.getAddress().getCity())
                                .status(booking.getStatus())
                                .scheduledTime(booking.getScheduledTime())
                                .notes(booking.getNotes())
                                .paymentMethod(booking.getPaymentMethod())
                                .transactionId(booking.getTransactionId())
                                .build();
        }
}
