package com.billing.billingsystem.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
    @Id
     private UUID id;

     @ManyToOne
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
     private Plan plan;

     private String status;
     private Instant startDate;
     private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private Instant canceledAt;

}
