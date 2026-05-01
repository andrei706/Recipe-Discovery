package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    Optional<Ingredient> findByNameIgnoreCase(String name);
}
