package com.billing.billingsystem.payment.application;

import com.billing.billingsystem.payment.domain.PaymentAttempt;
import com.billing.billingsystem.payment.repository.PaymentAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.billing.billingsystem.subscriptions.domain.Subscription;
import com.billing.billingsystem.subscriptions.DatabaseArchitecture.SubscriptionRepository;

import java.time.Duration;
import java.time.Instant;


@Service
public class PaymentAttemptService {

    @Autowired
     private PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    private paymentGateway paymentGateway;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // process attempt for the first time payment
    public void firstPaymentAttempt(UUID subscriptionId){
       Subscription sub=subscriptionRepository.findById(subscriptionId).orElseThrow(()->new RuntimeException("subscription not found"));
        PaymentAttempt attempt=createPaymentAttempt(sub);
        processPaymentAttempt(attempt,sub);
    }

    // retry payment attempts by cron job
    public void retryPaymentAttempt(){
        PaymentAttempt attempt=paymentAttemptRepository.findByNextRetryAtEqual(Instant.now());
        Subscription sub=attempt.getSubscription();
        processPaymentAttempt(attempt,sub);
        
    }

    // processing payment attempt logic
    private void processPaymentAttempt(PaymentAttempt attempt,Subscription sub){
       
        boolean success;

        try{
            success= paymentGateway.charge(attempt.getIdempotencyKey());

        }catch(RuntimeException e){
            if(attempt.getAttemptCount() <3){
                markRetry(attempt);
            } else {
                markPermanentFailure(attempt,sub);
            }
            return ;
        }

        if (success) {
            markSuccess(attempt,sub);
           } else {
            markRetry(attempt);
        }
    
    }


        @Transactional
        private PaymentAttempt createPaymentAttempt(Subscription subscription){
            PaymentAttempt attempt=new PaymentAttempt();
            attempt.setSubscription(subscription);
            attempt.setIdempotencyKey(subscription.getId().toString()+":"+subscription.getCurrentPeriodStart().toString());
            attempt.setAttemptCount(0);
            attempt.setStatus("PENDING");
            return paymentAttemptRepository.save(attempt);
        }


        @Transactional
        private void markSuccess(PaymentAttempt attempt,Subscription sub){
   
            attempt.setStatus("SUCCESS");
            sub.setNextMonthBillPaid(true);
            subscriptionRepository.save(sub);
            paymentAttemptRepository.save(attempt);
        }
        @Transactional
        private void markPermanentFailure(PaymentAttempt attempt,Subscription sub){
            
            attempt.setStatus("FAILED_PERMANENT");
            sub.setStatus("CANCELED");
            subscriptionRepository.save(sub);
            paymentAttemptRepository.save(attempt); 

        }
        @Transactional
        private void markRetry(PaymentAttempt attempt){
            attempt.setStatus("RETRY_SCHEDULED");
            attempt.setAttemptCount(attempt.getAttemptCount()+1);
            attempt.setNextRetryAt(nextRetryTime(attempt.getAttemptCount())); // retry with backoff
            paymentAttemptRepository.save(attempt);
        }

        private Instant nextRetryTime(int attemptCount){
            if(attemptCount==1){
                return Instant.now().plus(Duration.ofMinutes(60)); // 1 hour
            } else if(attemptCount==2){
                return Instant.now().plus(Duration.ofDays(2)); // 2 days after
            } else {
                return Instant.now().plus(Duration.ofDays(4)); //  6 days after
            }
        }



    
}
