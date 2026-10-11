package com.houseease.homeservice.payment;

import java.math.BigDecimal;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component("RAZORPAY")
public class RazorPayStrategy implements PaymentStrategy {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public String processPayment(BigDecimal amount, String idempotencyKey) throws PaymentTimeoutException {
        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (multiply by 100)
            orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue()); 
            orderRequest.put("currency", "INR");
            // Use idempotencyKey as the receipt identifier to prevent double charges
            orderRequest.put("receipt", idempotencyKey); 
            
            Order order = razorpay.orders.create(orderRequest);
            return order.get("id");
        } catch (Exception e) {
            throw new PaymentTimeoutException("Razorpay payment failed: " + e.getMessage());
        }
    }

}
