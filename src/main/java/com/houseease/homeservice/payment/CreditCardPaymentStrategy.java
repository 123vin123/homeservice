package com.houseease.homeservice.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component("CREDIT_CARD")
public class CreditCardPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(BigDecimal amount, String idempotencyKey) throws PaymentTimeoutException {
        System.out.println("Processing credit card payment of " + amount + " with idempotencyKey: " + idempotencyKey);
        // Simulate random timeout failure
        if (Math.random() < 0.2) {
            throw new PaymentTimeoutException("Credit card payment timed out");
        }
        return "CC_TXN_" + idempotencyKey;
    }
}
