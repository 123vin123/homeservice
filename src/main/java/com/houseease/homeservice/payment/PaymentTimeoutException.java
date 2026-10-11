package com.houseease.homeservice.payment;

public class PaymentTimeoutException extends Exception {
    public PaymentTimeoutException(String message) {
        super(message);
    }
}
