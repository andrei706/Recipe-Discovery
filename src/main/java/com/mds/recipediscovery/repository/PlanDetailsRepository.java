package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.PlanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanDetailsRepository extends JpaRepository<PlanDetails, Integer> {
    List<PlanDetails> findByPlanPlanId(Integer planId);
}

