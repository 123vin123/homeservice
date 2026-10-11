package com.houseease.homeservice.payment;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(Map<String , PaymentStrategy>strategies) {
             this.strategies=strategies;
    }

    public PaymentStrategy getStrategy(String paymentMethod ) {
        if (paymentMethod == null) {
          throw new IllegalArgumentException("Payment method can not be null");
        }
        String key = paymentMethod.toUpperCase();

        PaymentStrategy strategy = strategies.get(key);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method " + paymentMethod);
        }
        
        return strategy;
    }
}
