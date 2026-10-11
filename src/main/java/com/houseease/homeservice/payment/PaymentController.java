package com.houseease.homeservice.payment;

import com.houseease.homeservice.booking.Booking;
import com.houseease.homeservice.booking.BookingRepository;
import com.houseease.homeservice.booking.BookingStatus;
import com.razorpay.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingRepository bookingRepository;

    @Value("${razorpay.key.secret}")
    private String keySecret;

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
}

@Data
class PaymentVerificationRequest {
    private UUID bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
