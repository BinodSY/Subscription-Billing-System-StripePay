package com.billing.billingsystem.subscriptions;

import java.time.Instant;
import java.util.UUID;

import com.billing.billingsystem.users.User;
import com.billing.billingsystem.plans.Plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
     @Id
     @GeneratedValue(strategy=GenerationType.UUID)
     @Column(columnDefinition = "uuid")
     private UUID id;

     @ManyToOne
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
     private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_plan_id")
    private Plan pendingPlan; 

     @Column(name="status",nullable=false)
     private String status;

     @Column(name="start_date",nullable=false)
     private Instant startDate;

     @Column(name="current_period_start",nullable=false)
     private Instant currentPeriodStart;

     @Column(name="current_period_end",nullable=false)
    private Instant currentPeriodEnd;

    @Column(name="cancel_at_period_end")
    private boolean cancelAtPeriodEnd;
    //it will help cron to find the subscription to attempt payment
    @Column(name="auto_renew")
    private boolean autoRenew;
    //it will help cron to find the subscription to attempt payment
    @Column(name="next_month_bill_paid",nullable=false) 
    private boolean nextMonthBillPaid;

    @Column(name = "plan_change_effective_at")
    private Instant planChangeEffectiveAt;
    
    @Column(name="canceled_at")
    private Instant canceledAt;

}
