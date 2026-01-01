package com.billing.billingsystem.plans;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class PlanService {

        @Autowired
        private PlanRepository planRepository;
       

        public Plan createPlan(Plan plan){
            return planRepository.save(plan);
          
        }

        public List<Plan> getAllPlans (){
            return planRepository.findAll();
        }


}
