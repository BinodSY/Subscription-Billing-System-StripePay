package com.billing.billingsystem.subscriptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.billingsystem.plans.PlanRepository;
import com.billing.billingsystem.users.User;
import com.billing.billingsystem.users.UserRepository;
import java.time.Instant;
import jakarta.transaction.Transactional;

import com.billing.billingsystem.dto.SubscriptionReqest;
import com.billing.billingsystem.plans.Plan;
import java.util.UUID;
import java.util.List;

@Service
public class SubscriptionService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlanRepository planRepository;
    @Autowired 
    private SubscriptionRepository subscriptionRepository;

    // Business logic for managing subscriptions would go here
    //creating for new subscription by the first time user 
    @Transactional
    public Subscription createSubscription(SubscriptionReqest subscriptionreq){
        
         User user=userRepository.findById(subscriptionreq.userId())
                .orElseThrow(()->new RuntimeException("User not found"));

        Plan plan=planRepository.findById(subscriptionreq.planId())
                .orElseThrow(()->new RuntimeException("Plan not found"));
        Instant now = Instant.now();
        Subscription subscription=new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(now);
        subscription.setCurrentPeriodStart(subscription.getStartDate());
        subscription.setCurrentPeriodEnd(subscription.getCurrentPeriodStart().plusSeconds(30*24*60*60)); // assuming a 30-day period
        subscription.setNextMonthBillPaid(false);
        subscription.setAutoRenew(subscriptionreq.autoRenew());
        return subscriptionRepository.save(subscription);
       
        
    }

    // Change subscription plan in the middle of period ,it will done by manual
    @Transactional
    public Subscription changeSubscriptionPlan(UUID id,UUID newPlanId){
        Subscription subscription=subscriptionRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Subscription not found"));

        Plan newPlan=planRepository.findById(newPlanId)
                .orElseThrow(()->new RuntimeException("Plan not found"));

        //checking idempotent if the new plan is same as pending plan        
        if (newPlan.equals(subscription.getPendingPlan())) {
        return subscription; // No change needed
        }      
        subscription.setPendingPlan(newPlan);
        subscription.setPlanChangeEffectiveAt(subscription.getCurrentPeriodEnd());
        return subscriptionRepository.save(subscription);
    }

    //cron job will call this method daily to rollover billing periods
   @Transactional
    public void rolloverBillingPeriod() {

    Instant now = Instant.now();

    List<Subscription> subs =subscriptionRepository.findByCurrentPeriodEndLessThanEqualAndStatus(now, "ACTIVE");

    for (Subscription sub : subs) {

        // Apply pending plan ONLY at boundary
        if (sub.getPendingPlan() != null) {
            sub.setPlan(sub.getPendingPlan());
            sub.setPendingPlan(null);
            sub.setPlanChangeEffectiveAt(null);
        }

        // Advance billing window
        Instant newStart = sub.getCurrentPeriodEnd();
        Instant newEnd   = newStart.plusSeconds(30L * 24 * 60 * 60);

        sub.setCurrentPeriodStart(newStart);
        sub.setCurrentPeriodEnd(newEnd);

        // Reset payment state for NEXT cycle
        sub.setNextMonthBillPaid(false);

        // Handle cancel-at-period-end
        if (sub.isCancelAtPeriodEnd()) {
            sub.setStatus("CANCELED");
            sub.setAutoRenew(false);
            sub.setCanceledAt(now);
        }

        subscriptionRepository.save(sub);
    }
}


    //if user pay the bill manual before the bill end
    @Transactional
    public String billPay(UUID subscriptionId){
        Subscription subscription=subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new RuntimeException("Subscription not found"));                 
       
        if (subscription.isNextMonthBillPaid()) {
        return "Bill already paid"; // idempotent safety
        }  

        subscription.setNextMonthBillPaid(true);
        
        subscriptionRepository.save(subscription);
        return "Bill paid successfully";
    }

    //automatic payment attempt by cron
    @Transactional
    public void autoPay() {

    List<Subscription> subs =
            subscriptionRepository
                .findByAutoRenewTrueAndNextMonthBillPaidFalseAndStatus("ACTIVE");

    for (Subscription sub : subs) {

        // Safety guard
        if (sub.getCurrentPeriodEnd().isAfter(Instant.now())) {
            // still in current period, OK to pre-pay
        }

        // Attempt payment (integration omitted)
        boolean paymentSuccess = true;

        if (paymentSuccess) {
            sub.setNextMonthBillPaid(true);
            subscriptionRepository.save(sub);
        }
    }
}


    // Cancel subscription in the middle of period but they will remain active till period end
    @Transactional
    public void cancelSubscription(UUID subscriptionId){
        Subscription subscription=subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new RuntimeException("Subscription not found"));
                  // idempotent check
    if (subscription.isCancelAtPeriodEnd()) {
        return;
    }
    subscription.setCancelAtPeriodEnd(true);
        subscriptionRepository.save(subscription);
    }

    //cron or scheduled task can call this method daily to finalize cancellations
    @Transactional
    public void finalizeCancellations() {
    List<Subscription> subs =
        subscriptionRepository.findByCancelAtPeriodEnd(true);

    for (Subscription sub : subs) {
        sub.setStatus("CANCELED");
        sub.setCanceledAt(Instant.now());
        sub.setCancelAtPeriodEnd(false);
        }
    }
}