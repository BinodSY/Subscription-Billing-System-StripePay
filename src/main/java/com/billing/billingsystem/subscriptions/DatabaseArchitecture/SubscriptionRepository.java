package com.billing.billingsystem.subscriptions.DatabaseArchitecture;
import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.billingsystem.subscriptions.domain.Subscription;

import java.util.UUID;
import java.util.List;
import java.time.Instant;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByNextMonthBillPaid(boolean nextMonthBillPaid);

    List<UUID> findByCurrentPeriodEndLessThanEqualAndStatus(Instant now, String status);

    List<Subscription> findByAutoRenewTrueAndNextMonthBillPaidFalseAndStatus(String status);
    List<Subscription> findByCancelAtPeriodEnd(boolean cancelAtPeriodEnd);
    
}
