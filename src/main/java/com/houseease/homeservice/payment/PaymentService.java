package com.houseease.homeservice.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentStrategyFactory paymentStrategyFactory;

    public String processPaymentWithRetry(String method, BigDecimal amount) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        int retries = 3;
        String idempotencyKey = java.util.UUID.randomUUID().toString();
        
        while (retries > 0) {
            try {
                return strategy.processPayment(amount, idempotencyKey);
            } catch (PaymentTimeoutException e) {
                System.out.println("Payment timeout. Retries left: " + (retries - 1));
                retries--;
                if (retries == 0) {
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Payment failed with unrecoverable error: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
}
