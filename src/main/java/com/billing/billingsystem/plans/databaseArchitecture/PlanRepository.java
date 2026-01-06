package com.billing.billingsystem.plans.databaseArchitecture;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.billingsystem.plans.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

}
