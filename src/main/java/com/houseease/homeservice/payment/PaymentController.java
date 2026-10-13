package com.houseease.homeservice.payment;

import com.houseease.homeservice.booking.Booking;
import com.houseease.homeservice.booking.BookingRepository;
import com.houseease.homeservice.booking.BookingStatus;
import com.razorpay.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final BookingRepository bookingRepository;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:houseease_webhook_secret_key_123}")
    private String webhookSecret;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        UUID bookingId = request.getBookingId();
        log.info("[Booking ID: {}] Verifying payment for orderId: {}, paymentId: {}", 
                bookingId, request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        try {
            // Verify Signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                Booking booking = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
                
                // Confirm the booking
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setTransactionId(request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId());
                bookingRepository.save(booking);

                log.info("[Booking ID: {}] Successfully verified and CONFIRMED via /api/payments/verify", bookingId);
                return ResponseEntity.ok().body("{\"status\":\"success\"}");
            } else {
                log.warn("[Booking ID: {}] Verification failed: Invalid Razorpay signature", bookingId);
                return ResponseEntity.badRequest().body("{\"status\":\"failed\", \"message\":\"Invalid signature\"}");
            }
        } catch (Exception e) {
            log.error("[Booking ID: {}] Error verifying payment: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Razorpay Server-to-Server Webhook handler.
     * Receives event payloads directly from Razorpay's servers.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        if (signature == null || signature.isBlank()) {
            log.warn("[Booking ID: N/A] Missing X-Razorpay-Signature header in webhook request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature header");
        }

        try {
            // 1. Cryptographically verify signature using Webhook Secret
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                log.warn("[Booking ID: N/A] Invalid Razorpay webhook signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook signature");
            }

            JSONObject eventJson = new JSONObject(payload);
            String event = eventJson.optString("event");

            JSONObject payloadObj = eventJson.optJSONObject("payload");
            if (payloadObj == null) {
                log.warn("[Booking ID: N/A] Received webhook event {} with empty payload object", event);
                return ResponseEntity.ok("No payload object found");
            }

            String orderId = null;
            String paymentId = null;
            String errorDescription = null;

            if (payloadObj.has("payment")) {
                JSONObject paymentEntity = payloadObj.getJSONObject("payment").optJSONObject("entity");
                if (paymentEntity != null) {
                    orderId = paymentEntity.optString("order_id", null);
                    paymentId = paymentEntity.optString("id", null);
                    errorDescription = paymentEntity.optString("error_description", null);
                }
            }

            if ((orderId == null || orderId.isBlank()) && payloadObj.has("order")) {
                JSONObject orderEntity = payloadObj.getJSONObject("order").optJSONObject("entity");
                if (orderEntity != null) {
                    orderId = orderEntity.optString("id", null);
                }
            }

            if (orderId == null || orderId.isBlank()) {
                log.info("[Booking ID: N/A] Webhook event {} did not contain order_id. Skipping booking lookup.", event);
                return ResponseEntity.ok("Ignored event without order_id");
            }

            // Look up booking by order_id prefix
            Optional<Booking> bookingOpt = bookingRepository.findFirstByTransactionIdStartingWith(orderId);
            if (bookingOpt.isEmpty()) {
                log.warn("[Booking ID: NOT_FOUND] No booking found for order_id: {} in event: {}", orderId, event);
                return ResponseEntity.ok("Booking not found for order");
            }

            Booking booking = bookingOpt.get();
            UUID bookingId = booking.getId();

            log.info("[Booking ID: {}] Processing Razorpay webhook event: {} (order_id: {}, payment_id: {})", 
                    bookingId, event, orderId, paymentId);

            // 2. Handle Payment Success
            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                // If PAYMENT_PENDING, or if a previous retry attempt failed (CANCELLED), the customer has now paid:
                if (booking.getStatus() == BookingStatus.PAYMENT_PENDING || booking.getStatus() == BookingStatus.CANCELLED) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    if (paymentId != null) {
                        booking.setTransactionId(orderId + "|" + paymentId);
                    }
                    // Clean up any failed notes from earlier retry attempts
                    if (booking.getNotes() != null && booking.getNotes().contains("[Payment Failed")) {
                        booking.setNotes(booking.getNotes().replaceAll(" \\[Payment Failed[^\\]]*\\]", ""));
                    }
                    bookingRepository.save(booking);
                    log.info("[Booking ID: {}] Successfully CONFIRMED via webhook event {} (order: {}, payment: {})", 
                            bookingId, event, orderId, paymentId);
                } else if (booking.getStatus() == BookingStatus.CONFIRMED) {
                    // Idempotent: Razorpay sends both payment.captured and order.paid
                    if (paymentId != null && !booking.getTransactionId().contains(paymentId)) {
                        booking.setTransactionId(orderId + "|" + paymentId);
                        bookingRepository.save(booking);
                    }
                    log.info("[Booking ID: {}] Is already CONFIRMED. Acknowledged duplicate webhook event {} (order: {}, payment: {})", 
                            bookingId, event, orderId, paymentId);
                } else {
                    log.info("[Booking ID: {}] Is in status {}, ignoring success event {}", 
                            bookingId, booking.getStatus(), event);
                }
            }
            // 3. Handle Payment Failure
            else if ("payment.failed".equals(event)) {
                if (booking.getStatus() == BookingStatus.PAYMENT_PENDING) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    String reason = (errorDescription != null && !errorDescription.isBlank())
                            ? " [Payment Failed: " + errorDescription + "]"
                            : " [Payment Failed]";
                    booking.setNotes((booking.getNotes() != null ? booking.getNotes() : "") + reason);
                    bookingRepository.save(booking);
                    log.info("[Booking ID: {}] CANCELLED via webhook payment.failed. Slot released back to public. Reason: {}", 
                            bookingId, reason);
                } else {
                    log.info("[Booking ID: {}] Is in status {}, ignoring failed event", 
                            bookingId, booking.getStatus());
                }
            }

            // Always respond with 200 OK so Razorpay knows event was handled
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("[Booking ID: ERROR] Exception processing Razorpay webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }
}

@Data
class PaymentVerificationRequest {
    private UUID bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
