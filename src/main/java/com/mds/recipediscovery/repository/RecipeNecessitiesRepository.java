package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeNecessitiesRepository extends JpaRepository<RecipeNecessities, RecipeNecessitiesId> {
    List<RecipeNecessities> findByRecipe(Recipe recipe);
}