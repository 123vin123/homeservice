package com.houseease.homeservice.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component("UPI")
public class UpiPaymentStrategy implements PaymentStrategy {
    @Value("${upi.id:6395814983@ptsbi}")
    private String upiId;

    @Override
    public String processPayment(BigDecimal amount, String idempotencyKey) throws PaymentTimeoutException {
        System.out.println("Processing UPI payment of " + amount + " with idempotencyKey: " + idempotencyKey);
        if (Math.random() < 0.2) {
            throw new PaymentTimeoutException("UPI payment gateway timed out");
        }

        // Generate and return UPI intent URL, adding idempotencyKey as a transaction reference (tr)
        String upiUrl = String.format("upi://pay?pa=%s&pn=HouseEase&am=%s&cu=INR&tr=%s", upiId, amount.toString(), idempotencyKey);
        return upiUrl;
    }
}
