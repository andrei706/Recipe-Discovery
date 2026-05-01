package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.Diet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DietRepository extends JpaRepository<Diet, Integer> {
    Optional<Diet> findByName(String name);
}
