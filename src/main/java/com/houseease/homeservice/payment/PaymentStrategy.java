package com.houseease.homeservice.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String processPayment(BigDecimal amount, String idempotencyKey) throws PaymentTimeoutException;
}
