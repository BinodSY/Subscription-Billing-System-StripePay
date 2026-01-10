package com.billing.billingsystem.plans.application;

import org.springframework.stereotype.Service;

import com.billing.billingsystem.plans.databaseArchitecture.PlanRepository;
import com.billing.billingsystem.plans.domain.Plan;
import com.billing.billingsystem.subscriptions.application.SubscriptionService;
import com.billing.billingsystem.subscriptions.domain.Subscription;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlanService {

        @Autowired
        private PlanRepository planRepository;
        @Autowired
        private SubscriptionService subscriptionService;
       

        public Plan createPlan(Plan plan){
            return planRepository.save(plan);
          
        }

        public List<Plan> getAllPlans (){
            return planRepository.findAll();
        }

        // Determine the effective plan for a subscription
        public Plan resolveEffectivePlan(UUID subscriptionId) {

            Subscription sub=subscriptionService.getSubscriptionById(subscriptionId);

            Instant now = Instant.now();

            // If current plan is still valid → use it
            if (now.isBefore(sub.getCurrentPeriodEnd())) {
                return sub.getPlan();
            }

            // Current plan expired
            if ("ACTIVE".equals(sub.getStatus())
                && sub.getPendingPlan() != null
                && sub.getPlanChangeEffectiveAt() != null
                && !now.isBefore(sub.getPlanChangeEffectiveAt())) {

                return sub.getPendingPlan();
            }

            // Fallback (expired + no pending plan)
            return sub.getPlan();
    }


}
