package com.houseease.homeservice.payment;

import com.razorpay.Utils;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentWebhookTest {

    @Test
    void testWebhookSignatureVerification() throws Exception {
        String secret = "test_webhook_secret_xyz";
        String payload = "{\"event\":\"payment.failed\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_123\",\"order_id\":\"order_123\"}}}}";

        // Generate HMAC SHA256 hex signature
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String validSignature = hexString.toString();

        // Verify with Razorpay Utils
        boolean isValid = Utils.verifyWebhookSignature(payload, validSignature, secret);
        assertTrue(isValid, "Signature should be valid");

        // Verify tampered signature fails
        boolean isTampered = Utils.verifyWebhookSignature(payload, "invalid_signature", secret);
        assertFalse(isTampered, "Tampered signature should fail");
    }
}
