package com.billing.billingsystem.billing.scheduler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.billingsystem.subscriptions.DatabaseArchitecture.SubscriptionRepository;
import com.billing.billingsystem.subscriptions.application.SubscriptionService;
// import org.springframework.scheduling.annotation.Scheduled;


@Component
public class BillingCronJob {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionService subscriptionService;

   public void rolloverBillingPeriod(){
    Instant now = Instant.now();

     List<UUID> expiredSubs = subscriptionRepository
        .findByCurrentPeriodEndLessThanEqualAndStatus(now, "ACTIVE");
        for (UUID subId : expiredSubs) {
            subscriptionService.rolloverSingleSubscription(subId);
        }
   }

   public void processAutoPayments(){
    subscriptionService.autoBillPay();
   }

   public void proccessCancelledSubscrtriptions(){
    subscriptionService.finalizeCancellations();
   }

//    @Scheduled(fixedDelay = 5000)
   public void testScheduler(){
    System.out.println("Cron is working");
   }

}
