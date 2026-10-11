package com.houseease.homeservice.booking;

/**
 * Lifecycle states for a Booking.
 *
 * State transitions:
 *   PENDING  → CONFIRMED  (Provider accepts)
 *   PENDING  → CANCELLED  (Customer or Provider cancels)
 *   CONFIRMED → IN_PROGRESS (Provider starts the job)
 *   CONFIRMED → CANCELLED   (Customer or Provider cancels)
 *   IN_PROGRESS → COMPLETED (Provider finishes the job)
 */
public enum BookingStatus {
    PAYMENT_PENDING,
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
