package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByUserUserId(Integer userId);
}

