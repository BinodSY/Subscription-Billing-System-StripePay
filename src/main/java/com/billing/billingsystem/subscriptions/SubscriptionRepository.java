package com.billing.billingsystem.subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.time.Instant;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findExpiredAndMarkedForCancel(Instant now);
    List<Subscription> findByPendingPlanIsNotNullAndPlanChangeEffectiveAtBefore(Instant now);
}
