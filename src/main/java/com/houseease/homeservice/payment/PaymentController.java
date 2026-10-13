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
        try {
            // Verify Signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                Booking booking = bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                
                // Confirm the booking
                booking.setStatus(BookingStatus.CONFIRMED);
                // We can append the payment id to transaction id
                booking.setTransactionId(request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId());
                bookingRepository.save(booking);

                return ResponseEntity.ok().body("{\"status\":\"success\"}");
            } else {
                return ResponseEntity.badRequest().body("{\"status\":\"failed\", \"message\":\"Invalid signature\"}");
            }
        } catch (Exception e) {
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

        log.info("Received Razorpay webhook notification");

        if (signature == null || signature.isBlank()) {
            log.warn("Missing X-Razorpay-Signature header in webhook request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature header");
        }

        try {
            // 1. Cryptographically verify signature using Webhook Secret
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                log.warn("Invalid Razorpay webhook signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook signature");
            }

            JSONObject eventJson = new JSONObject(payload);
            String event = eventJson.optString("event");
            log.info("Processing Razorpay webhook event: {}", event);

            JSONObject payloadObj = eventJson.optJSONObject("payload");
            if (payloadObj == null) {
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
                log.info("Webhook event {} did not contain order_id. Skipping booking lookup.", event);
                return ResponseEntity.ok("Ignored event without order_id");
            }

            Optional<Booking> bookingOpt = bookingRepository.findFirstByTransactionIdStartingWith(orderId);
            if (bookingOpt.isEmpty()) {
                log.warn("No booking found for order_id: {}", orderId);
                return ResponseEntity.ok("Booking not found for order");
            }

            Booking booking = bookingOpt.get();

            // 2. Handle Payment Success
            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                if (booking.getStatus() == BookingStatus.PAYMENT_PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    if (paymentId != null) {
                        booking.setTransactionId(orderId + "|" + paymentId);
                    }
                    bookingRepository.save(booking);
                    log.info("Booking {} confirmed via webhook event {}", booking.getId(), event);
                } else {
                    log.info("Booking {} is already in status {}, ignoring success event", booking.getId(), booking.getStatus());
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
                    log.info("Booking {} cancelled via webhook payment.failed. Slot released back to public.", booking.getId());
                } else {
                    log.info("Booking {} is in status {}, ignoring failed event", booking.getId(), booking.getStatus());
                }
            }

            // Always respond with 200 OK so Razorpay knows event was handled
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
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
