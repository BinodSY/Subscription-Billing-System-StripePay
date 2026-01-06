package com.billing.billingsystem.plans.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.billingsystem.plans.application.PlanService;
import com.billing.billingsystem.plans.domain.Plan;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
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


}
