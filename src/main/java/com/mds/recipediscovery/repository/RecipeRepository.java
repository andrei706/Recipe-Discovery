package com.mds.recipediscovery.repository;

import com.mds.recipediscovery.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    Optional<Recipe> findByName(String name);

    @Query("SELECT r FROM Recipe r WHERE NOT EXISTS (" +
           "SELECT rn FROM RecipeNecessities rn WHERE rn.recipe = r AND NOT EXISTS (" +
           "SELECT i FROM Inventory i WHERE i.user.userId = :userId " +
           "AND i.ingredient = rn.ingredient AND i.quantity >= rn.quantity))")
    List<Recipe> findRecipesUserCanCook(@Param("userId") Integer userId);

    @Query("SELECT r FROM Recipe r " +
           "LEFT JOIN RecipeNecessities rn ON rn.recipe = r " +
           "LEFT JOIN Inventory i ON i.ingredient = rn.ingredient AND i.user.userId = :userId AND i.quantity >= rn.quantity " +
           "GROUP BY r " +
           "ORDER BY COUNT(i.ingredient) DESC")
    List<Recipe> findRecipesSortedByMatchingIngredients(@Param("userId") Integer userId);

    @Query("SELECT r, " +
           "COUNT(i.ingredient), " +
           "(SELECT COUNT(rn2) FROM RecipeNecessities rn2 WHERE rn2.recipe = r) " +
           "FROM Recipe r " +
           "LEFT JOIN RecipeNecessities rn ON rn.recipe = r " +
           "LEFT JOIN Inventory i ON i.ingredient = rn.ingredient AND i.user.userId = :userId AND i.quantity >= rn.quantity " +
           "GROUP BY r")
    List<Object[]> findRecipesWithMatchData(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT r FROM Recipe r " +
           "LEFT JOIN RecipeNecessities rn ON rn.recipe = r " +
           "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.preparationSteps) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(rn.ingredient.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Recipe> searchRecipesByTerm(@Param("searchTerm") String searchTerm);
}
