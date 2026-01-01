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
        return subscriptionRepository.save(subscription);
       
        
    }

    // Change subscription plan in the middle of period
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
        subscription.setPlanChangeEffectiveAt(Instant.now());
        return subscriptionRepository.save(subscription);
    }
    //cron or scheduled task can call this method daily to finalize plan changes
    @Transactional
    public void finalizePlanChanges(){
     List<Subscription> subs = subscriptionRepository.findByPendingPlanIsNotNullAndPlanChangeEffectiveAtBefore(Instant.now());
        for(Subscription sub:subs){
            sub.setPlan(sub.getPendingPlan());
            sub.setPendingPlan(null);
            sub.setPlanChangeEffectiveAt(null);
        }
    }

    // Cancel subscription in the middle of period but they will remain active till period end
    @Transactional
    public void cancelSubscription(UUID id){
        Subscription subscription=subscriptionRepository.findById(id)
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
        subscriptionRepository.findExpiredAndMarkedForCancel(Instant.now());

    for (Subscription sub : subs) {
        sub.setStatus("CANCELED");
        sub.setCanceledAt(Instant.now());
        sub.setCancelAtPeriodEnd(false);
    }
    }
}