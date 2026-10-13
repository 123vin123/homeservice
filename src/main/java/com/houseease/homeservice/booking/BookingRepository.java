package com.houseease.homeservice.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomerIdOrderByScheduledTimeDesc(UUID customerId);

    List<Booking> findByProviderIdOrderByScheduledTimeDesc(UUID providerId);

    Optional<Booking> findFirstByTransactionIdStartingWith(String orderIdPrefix);

    /**
     * Checks if an active (non-cancelled) booking already exists for
     * this provider during the requested time window.
     * Uses PESSIMISTIC_WRITE lock to prevent two concurrent transactions
     * from both inserting for the same slot.
     *
     * The time window is: [scheduledTime, scheduledTime + durationMinutes)
     * An overlap exists if: existing.start < new.end AND new.start < existing.end
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT b FROM Booking b
                WHERE b.provider.id = :providerId
                  AND b.status NOT IN (com.houseease.homeservice.booking.BookingStatus.CANCELLED)
                  AND b.scheduledTime < :requestedEnd
                  AND b.endTime > :requestedStart
            """)
    List<Booking> findConflictingBookings(
            @Param("providerId") UUID providerId,
            @Param("requestedStart") ZonedDateTime requestedStart,
            @Param("requestedEnd") ZonedDateTime requestedEnd);

    /**
     * Read-only version for search/matching algorithms.
     * Does not use pessimistic locks to avoid blocking.
     */
    @Query("""
                SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
                FROM Booking b
                WHERE b.provider.id = :providerId
                  AND b.status NOT IN (com.houseease.homeservice.booking.BookingStatus.CANCELLED)
                  AND b.scheduledTime < :requestedEnd
                  AND b.endTime > :requestedStart
            """)
    boolean hasConflictingBookings(
            @Param("providerId") UUID providerId,
            @Param("requestedStart") ZonedDateTime requestedStart,
            @Param("requestedEnd") ZonedDateTime requestedEnd);
}
