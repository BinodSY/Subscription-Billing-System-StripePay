package com.billing.billingsystem.model;

import java.math.BigDecimal;
// import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class Plan {
   @Id
   private UUID id;

   @Column(nullable = false)
   private String name;

    @Column(nullable = false)
   private BigDecimal price;

    @Column(nullable = false)
   private String currency;

   @Column(name="billing_interval", nullable = false)
   private String billingInterval;

   @Column(name="is_active", nullable = false)
   private boolean isActive;


}
