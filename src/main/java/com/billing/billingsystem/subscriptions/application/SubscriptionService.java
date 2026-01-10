package com.billing.billingsystem.subscriptions.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.billing.billingsystem.payment.repository.PaymentAttemptRepository;
import com.billing.billingsystem.plans.databaseArchitecture.PlanRepository;
import com.billing.billingsystem.plans.domain.Plan;
import com.billing.billingsystem.subscriptions.DatabaseArchitecture.SubscriptionRepository;
import com.billing.billingsystem.subscriptions.domain.Subscription;
import com.billing.billingsystem.subscriptions.domain.SubscriptionReqest;
import com.billing.billingsystem.users.databaseArchitecture.UserRepository;
import com.billing.billingsystem.users.domain.User;
import com.billing.billingsystem.invoices.application.InvoiceService;
import com.billing.billingsystem.payment.application.PaymentAttemptService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class SubscriptionService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlanRepository planRepository;
    @Autowired 
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;
    
    public SubscriptionService(PaymentAttemptRepository paymentAttemptRepository) {
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    // Business logic for managing subscriptions would go here
    //creating for new subscription by the first time user 
    @Transactional
    public Subscription createSubscription(SubscriptionReqest subscriptionreq){
        Boolean payment=true;// Assume payment is successful for this example
        if(payment!=true){
            throw new RuntimeException("Payment failed, cannot create subscription");
        }
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
        Instant billingEndDate=calculateNextPeriodEnd(subscription.getCurrentPeriodStart(),plan.getBillingInterval());
        subscription.setCurrentPeriodEnd((billingEndDate)); 
        subscription.setNextMonthBillPaid(false);
        subscription.setAutoRenew(subscriptionreq.autoRenew());

        invoiceService.createInvoice(subscription); // Create initial invoice

        return subscriptionRepository.save(subscription);

        }

    public Subscription getSubscriptionById(UUID subscriptionId){
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new RuntimeException("Subscription not found"));
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
        Instant billingEndDate=calculateNextPeriodEnd(subscription.getCurrentPeriodEnd(),newPlan.getBillingInterval());
        subscription.setPlanChangeEffectiveAt(billingEndDate);
        return subscriptionRepository.save(subscription);
    }

    // Rollover a single subscription, used by cron job
    
    public void rolloverSingleSubscription(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

           handlePeriodEnd(sub);
    }

    public void handlePeriodEnd(Subscription sub) {
        Subscription subc=applyPendingPlanIfAny(sub);
        resetPaymentStatus(saveBillingDuration(subc));
    }

    //assign new plan or upcomming plan
    @Transactional
    public Subscription applyPendingPlanIfAny(Subscription sub) {
    // Apply pending plan ONLY at boundary
        if (sub.getPendingPlan() != null) {
            sub.setPlan(sub.getPendingPlan());
            sub.setPendingPlan(null);
            sub.setPlanChangeEffectiveAt(null);
        }

        if(sub.isCancelAtPeriodEnd()){
            sub.setStatus("CANCELED");
            sub.setAutoRenew(false);
            sub.setCanceledAt(Instant.now());
        }
        return subscriptionRepository.save(sub);
    }

    //save new billing duration after period end
    @Transactional
    public Subscription saveBillingDuration(Subscription sub){
        sub.setCurrentPeriodStart(sub.getCurrentPeriodEnd());
        Instant billEndingDate=calculateNextPeriodEnd(sub.getCurrentPeriodStart(),sub.getPlan().getBillingInterval());
        sub.setCurrentPeriodEnd(billEndingDate);
        return subscriptionRepository.save(sub);
    }

    //calculate next billing period end based on billing interval
    public Instant calculateNextPeriodEnd(
            Instant periodStart,
            String billingInterval
    ) {
        ZonedDateTime zdt = periodStart.atZone(ZoneOffset.UTC);

        ZonedDateTime nextEnd = switch (billingInterval) {
            case "MONTHLY" -> zdt.plusMonths(1);
            case "YEARLY"  -> zdt.plusYears(1);
            case "TRIAL"   -> zdt.plusDays(14);
            default -> throw new IllegalArgumentException("Invalid billing interval");
        };

        return nextEnd.toInstant();
    }

    
    //reset payment status for next billing cycle
    @Transactional
    public void resetPaymentStatus(Subscription sub){
        sub.setNextMonthBillPaid(false);
        subscriptionRepository.save(sub);
    }

    //if user pay the bill manual before the bill end
    @Transactional
    public String manualBillPay(UUID subscriptionId){
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
    public void autoBillPay() {

    List<Subscription> subs =
            subscriptionRepository
                .findByAutoRenewTrueAndNextMonthBillPaidFalseAndStatus("ACTIVE");

    for (Subscription sub : subs) {

            String key=sub.getId().toString()+":"+sub.getCurrentPeriodStart().toString();

            if(paymentAttemptRepository.existsByIdempotencyKey(key)){
                continue; // idempotent check
            }else{
                paymentAttemptService.firstPaymentAttempt(sub.getId());
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