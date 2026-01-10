package com.billing.billingsystem.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.billingsystem.payment.domain.PaymentAttempt;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    boolean existsByIdempotencyKey(String key);

   List<PaymentAttempt> findByStatusInAndNextRetryAtLessThanEqual(List<String> statuses,Instant now);


}
