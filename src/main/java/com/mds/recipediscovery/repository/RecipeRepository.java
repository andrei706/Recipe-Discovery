package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    Optional<Recipe> findByName(String name);
}
