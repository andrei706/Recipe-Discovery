package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeDietClassificationRepository extends JpaRepository<RecipeDietClassification, RecipeDietClassificationId> {
    List<RecipeDietClassification> findByRecipe(Recipe recipe);
}