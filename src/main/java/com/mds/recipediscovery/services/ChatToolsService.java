package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.models.RecipeNecessities;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.InventoryRepository;
import com.mds.recipediscovery.repository.RecipeNecessitiesRepository;
import com.mds.recipediscovery.repository.RecipeRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatToolsService {

    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeNecessitiesRepository recipeNecessitiesRepository;


    public ChatToolsService(UserRepository userRepository,
                            InventoryRepository inventoryRepository,
                            RecipeRepository recipeRepository,
                            RecipeNecessitiesRepository recipeNecessitiesRepository) {
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.recipeRepository = recipeRepository;
        this.recipeNecessitiesRepository = recipeNecessitiesRepository;
    }

    public User findUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String inventorySnapshot(Integer userId) {
        User user = findUserOrThrow(userId);
        List<Inventory> inventoryList = inventoryRepository.findByUser(user);
        return inventoryList.stream()
                .map(inv -> String.format("- %s: %d %s",
                        inv.getIngredient().getName(),
                        inv.getQuantity(),
                        inv.getIngredient().getMeasurementUnit()))
                .collect(Collectors.joining("\n"));
    }

    public List<RecipeMatchDTO> topRecipesForPrompt(Integer userId, String prompt, int limit) {
        List<String> keywords = extractKeywords(prompt);
        Set<Integer> matchingRecipeIds = new HashSet<>();

        for (String kw : keywords) {
            List<Recipe> termMatches = recipeRepository.searchRecipesByTerm(kw);
            for (Recipe r : termMatches) {
                matchingRecipeIds.add(r.getRecipeId());
            }
        }

        List<Object[]> rawResults = recipeRepository.findRecipesWithMatchData(userId);
        List<RecipeMatchDTO> allMatches = rawResults.stream()
                .map(obj -> {
                    Recipe recipe = (Recipe) obj[0];
                    int matchedCount = ((Number) obj[1]).intValue();
                    int totalCount = ((Number) obj[2]).intValue();
                    return new RecipeMatchDTO(recipe, matchedCount, totalCount);
                })
                .sorted(Comparator.comparingDouble(RecipeMatchDTO::getMatchPercentage).reversed())
                .toList();

        List<RecipeMatchDTO> filteredMatches;
        if (!matchingRecipeIds.isEmpty()) {
            filteredMatches = allMatches.stream()
                    .filter(m -> matchingRecipeIds.contains(m.getRecipe().getRecipeId()))
                    .toList();
        } else {
            filteredMatches = allMatches;
        }

        return filteredMatches.stream()
                .limit(limit)
                .toList();
    }

    public String recipeContext(List<RecipeMatchDTO> matches) {
        StringBuilder recipesBuilder = new StringBuilder();
        for (RecipeMatchDTO match : matches) {
            Recipe r = match.getRecipe();
            recipesBuilder.append(String.format("ID: %d | Name: %s | Prep: %d mins | Calories: %.1f kcal | Match: %d/%d (%.1f%%)\n",
                    r.getRecipeId(), r.getName(), r.getTotalPrepTimeMinutes(),
                    r.getCaloriesKcal(), match.getMatchedIngredients(), match.getTotalIngredients(), match.getMatchPercentage()));

            List<RecipeNecessities> necessities = recipeNecessitiesRepository.findByRecipe(r);
            String ingredients = necessities.stream()
                    .map(n -> String.format("%s (%d %s)",
                            n.getIngredient().getName(),
                            n.getQuantity(),
                            n.getIngredient().getMeasurementUnit()))
                    .collect(Collectors.joining(", "));
            recipesBuilder.append("   Ingredients: ").append(ingredients).append("\n\n");
        }
        return recipesBuilder.toString();
    }

    private List<String> extractKeywords(String prompt) {
        if (prompt == null) return List.of();
        String cleaned = prompt.toLowerCase().replaceAll("[.,!?()\"';:]", " ");
        String[] words = cleaned.split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String w : words) {
            if (w.length() < 3) continue;
            keywords.add(w);
        }
        return keywords;
    }
}