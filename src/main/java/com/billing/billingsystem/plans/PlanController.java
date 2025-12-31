package com.billing.billingsystem.plans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody Plan plan){
        Plan newPlan=planService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPlan);
    }


}
