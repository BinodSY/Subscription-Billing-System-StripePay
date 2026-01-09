package com.billing.billingsystem.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.billingsystem.payment.domain.PaymentAttempt;

import java.time.Instant;
import java.util.UUID;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    boolean existsByIdempotencyKey(String key);

    PaymentAttempt findByNextRetryAtEqual(Instant now);

}
