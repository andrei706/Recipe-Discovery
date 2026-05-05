package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.RecipeIngredientStatusDTO;
import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.dto.RecipeMatchDetailsDTO;
import com.mds.recipediscovery.models.*;
import com.mds.recipediscovery.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final RecipeNecessitiesRepository recipeIngredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
            UserRepository userRepository,
            InventoryRepository inventoryRepository,
            RecipeNecessitiesRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    public List<Recipe> findAvailableRecipes(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return recipeRepository.findRecipesUserCanCook(userId);
    }

    public List<Recipe> findRecipesSortedByMatchingIngredients(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return recipeRepository.findRecipesSortedByMatchingIngredients(userId);
    }

    public List<RecipeMatchDTO> getRecipesWithMatchPercentage(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> rawResults = recipeRepository.findRecipesWithMatchData(userId);

        return rawResults.stream()
                .map(obj -> {
                    Recipe recipe = (Recipe) obj[0];
                    int matchedCount = ((Number) obj[1]).intValue();
                    int totalCount = ((Number) obj[2]).intValue();
                    return new RecipeMatchDTO(recipe, matchedCount, totalCount);
                })
                .sorted(Comparator.comparingDouble(RecipeMatchDTO::getMatchPercentage).reversed())
                .collect(Collectors.toList());
    }

    public void cookRecipe(Integer userId, Integer recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        List<RecipeNecessities> requirements = recipeIngredientRepository.findByRecipe(recipe);

        List<Ingredient> neededIngredients = requirements.stream()
                .map(RecipeNecessities::getIngredient)
                .collect(Collectors.toList());

        List<Inventory> relevantInventory = inventoryRepository.findByUserAndIngredientIn(user, neededIngredients);
        Map<Integer, Inventory> userInventoryMap = relevantInventory.stream()
                .collect(Collectors.toMap(
                        inv -> inv.getIngredient().getIngredientId(),
                        inv -> inv));

        for (RecipeNecessities req : requirements) {
            Inventory inventory = userInventoryMap.get(req.getIngredient().getIngredientId());
            if (inventory == null || inventory.getQuantity() < req.getQuantity()) {
                throw new RuntimeException(
                        "Nu ai suficiente ingrediente pentru a găti această rețetă: " + recipe.getName());
            }
        }

        for (RecipeNecessities req : requirements) {
            Inventory inventory = userInventoryMap.get(req.getIngredient().getIngredientId());
            int newQuantity = inventory.getQuantity() - req.getQuantity();

            if (newQuantity <= 0) {
                inventoryRepository.delete(inventory);
            } else {
                inventory.setQuantity(newQuantity);
                inventoryRepository.save(inventory);
            }
        }
    }

    public List<RecipeMatchDetailsDTO> getRecipeDetailsWithInventory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return recipeRepository.findAll().stream()
                .map(recipe -> {
                    List<RecipeNecessities> requirements = recipeIngredientRepository.findByRecipe(recipe);
                    if (requirements.isEmpty()) {
                        return new RecipeMatchDetailsDTO(recipe, 0, 0, 100.0, List.of());
                    }

                    List<Ingredient> neededIngredients = requirements.stream()
                            .map(RecipeNecessities::getIngredient)
                            .collect(Collectors.toList());

                    Map<Integer, Inventory> inventoryMap = inventoryRepository
                            .findByUserAndIngredientIn(user, neededIngredients)
                            .stream()
                            .collect(Collectors.toMap(
                                    inv -> inv.getIngredient().getIngredientId(),
                                    inv -> inv));

                    List<RecipeIngredientStatusDTO> ingredientStatuses = new ArrayList<>();
                    int matchedCount = 0;

                    for (RecipeNecessities requirement : requirements) {
                        Ingredient ingredient = requirement.getIngredient();
                        int requiredQuantity = requirement.getQuantity();
                        int availableQuantity = inventoryMap.containsKey(ingredient.getIngredientId())
                                ? inventoryMap.get(ingredient.getIngredientId()).getQuantity()
                                : 0;
                        int missingQuantity = Math.max(0, requiredQuantity - availableQuantity);
                        if (missingQuantity == 0) {
                            matchedCount += 1;
                        }

                        ingredientStatuses.add(new RecipeIngredientStatusDTO(
                                ingredient.getIngredientId(),
                                ingredient.getName(),
                                ingredient.getMeasurementUnit(),
                                requiredQuantity,
                                availableQuantity,
                                missingQuantity));
                    }

                    int totalCount = requirements.size();
                    double matchPercentage = totalCount == 0
                            ? 100.0
                            : Math.round((double) matchedCount / totalCount * 100.0 * 100.0) / 100.0;

                    return new RecipeMatchDetailsDTO(recipe, matchedCount, totalCount, matchPercentage, ingredientStatuses);
                })
                .sorted(Comparator.comparingDouble(RecipeMatchDetailsDTO::getMatchPercentage).reversed())
                .collect(Collectors.toList());
    }
}