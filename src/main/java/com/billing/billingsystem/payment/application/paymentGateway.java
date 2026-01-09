package com.billing.billingsystem.payment.application;

import org.springframework.stereotype.Service;
 

@Service
public class paymentGateway {

    public boolean charge(String idempotencyKey){
        // Simulate payment processing logic
        // This is a stub implementation. Replace with actual payment gateway integration.
        double random = Math.random();
        if (random < 0.7) {
            return true; // Payment succeeded
        } else if (random < 0.9) {
            throw new RuntimeException("Temporary issue, please retry.");
        } else {
            throw new RuntimeException("Permanent failure, do not retry.");
        }
    }
}
