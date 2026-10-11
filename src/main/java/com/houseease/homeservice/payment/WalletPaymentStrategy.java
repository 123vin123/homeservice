package com.houseease.homeservice.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component("WALLET")
public class WalletPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(BigDecimal amount, String idempotencyKey) throws PaymentTimeoutException {
        System.out.println("Processing Wallet payment of " + amount + " with idempotencyKey: " + idempotencyKey);
        if (Math.random() < 0.2) {
            throw new PaymentTimeoutException("Wallet payment service timed out");
        }
        return "WALLET_TXN_" + idempotencyKey;
    }
}
