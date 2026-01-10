package com.billing.billingsystem.plans.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.billingsystem.plans.application.PlanService;
import com.billing.billingsystem.plans.domain.Plan;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping
    public ResponseEntity<?> getAllPlans(){
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody Plan plan){
        Plan newPlan=planService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPlan);
    }

    //local host:8080/plan/{subscriptionId}, it is resolving getting current plan or pending plan if avilable for subscription \n
    // helpful for frontend to show correct plan details 
    @PostMapping("/{subscriptionId}")
    public ResponseEntity<?> getPlanDetails(@PathVariable UUID subscriptionId) {
        Plan plan = planService.resolveEffectivePlan(subscriptionId);
        return ResponseEntity.ok(plan);
    }


}
