package com.billing.billingsystem.subscriptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.billing.billingsystem.dto.SubscriptionReqest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    @PostMapping
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionReqest subscription){

        Subscription newSubscription=subscriptionService.createSubscription(subscription);
        return ResponseEntity.ok(newSubscription);
    }

    //local host:8080/subscriptions/{id}/planchange
    @PostMapping("/{id}/planchange")
    public ResponseEntity<?> changeSubcriptionPlan(@PathVariable UUID subscriptionId, @RequestBody UUID newPlanId) {
        Subscription updatedSubscription = subscriptionService.changeSubscriptionPlan(subscriptionId, newPlanId);
        return ResponseEntity.ok(updatedSubscription);
    }

    //local host:8080/subscriptions/{id}/cancel
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable UUID subscriptionId) {
        // Implementation for canceling a subscription
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.ok().body("Subscription canceled successfully");
    }


}