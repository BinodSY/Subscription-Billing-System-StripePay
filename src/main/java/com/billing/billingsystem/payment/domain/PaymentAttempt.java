package com.billing.billingsystem.payment.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.UUID;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import com.billing.billingsystem.subscriptions.domain.Subscription;
import java.time.Instant;

@Entity
@Table(name = "payment_attempts")
@Getter
@Setter
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name="idempotency_key", nullable = false)
    private String idempotencyKey; // subId + period

    @Column(name="status", nullable = false)
    private String status; // PENDING, SUCCESS, RETRY_SCHEDULED, FAILED_PERMANENT

    @Column(name="next_retry_time")
    private Instant nextRetryAt;

    @Column(name="attempt_count", nullable = false)
    private int attemptCount = 0;

}
