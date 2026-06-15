package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.models.RecipeNecessities;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.models.Ingredient;
import com.mds.recipediscovery.repository.InventoryRepository;
import com.mds.recipediscovery.repository.RecipeNecessitiesRepository;
import com.mds.recipediscovery.repository.RecipeRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ChatToolsService {

    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeNecessitiesRepository recipeNecessitiesRepository;

    private static final Set<String> STOP_WORDS = Set.of(
            "want", "something", "cook", "ignore", "other", "ingredients", "recipe", "recipes",
            "make", "find", "suggest", "show", "list", "with", "for", "please", "can", "you",
            "would", "like", "search", "have", "need", "about", "some", "any", "dish", "dishes",
            "meal", "meals", "food", "kitchen", "assistant", "prepare", "ready", "minute", "minutes",
            "sauce", "prep", "easy", "quick", "simple", "delicious", "healthy", "good", "great",
            "vrea", "vreau", "ceva", "gatit", "gatesc", "ignora", "ingrediente", "ingredientele",
            "reteta", "retete", "retetei", "fa", "gaseste", "sugereaza", "arata", "lista", "cu",
            "pentru", "te rog", "poti", "sa", "mi", "arate", "recomanda", "mancare", "preparat",
            "rapid", "simplu", "gustos"
    );


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
                .map(inv -> String.format("- %s: %s %s",
                        inv.getIngredient().getName(),
                        formatQuantity(inv.getQuantity()),
                        inv.getIngredient().getMeasurementUnit()))
                .collect(Collectors.joining("\n"));
    }

    public List<RecipeMatchDTO> topRecipesForPrompt(Integer userId, String prompt, int limit) {
        List<String> keywords = extractKeywords(prompt);
        Map<Integer, Integer> recipeKeywordMatchCounts = new HashMap<>();

        for (String kw : keywords) {
            List<Recipe> termMatches = recipeRepository.searchRecipesByTerm(kw);
            for (Recipe r : termMatches) {
                recipeKeywordMatchCounts.put(r.getRecipeId(), recipeKeywordMatchCounts.getOrDefault(r.getRecipeId(), 0) + 1);
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
                .toList();

        List<RecipeMatchDTO> filteredMatches;
        if (!recipeKeywordMatchCounts.isEmpty()) {
            filteredMatches = allMatches.stream()
                    .filter(m -> recipeKeywordMatchCounts.containsKey(m.getRecipe().getRecipeId()))
                    .sorted((m1, m2) -> {
                        int count1 = recipeKeywordMatchCounts.get(m1.getRecipe().getRecipeId());
                        int count2 = recipeKeywordMatchCounts.get(m2.getRecipe().getRecipeId());
                        if (count1 != count2) {
                            return Integer.compare(count2, count1); // count desc
                        }
                        return Double.compare(m2.getMatchPercentage(), m1.getMatchPercentage()); // match percentage desc
                    })
                    .toList();
        } else {
            filteredMatches = allMatches.stream()
                    .sorted(Comparator.comparingDouble(RecipeMatchDTO::getMatchPercentage).reversed())
                    .toList();
        }

        return filteredMatches.stream()
                .limit(limit)
                .toList();
    }

    public String recipeContext(Integer userId, List<RecipeMatchDTO> matches) {
        User user = findUserOrThrow(userId);
        StringBuilder recipesBuilder = new StringBuilder();
        for (RecipeMatchDTO match : matches) {
            Recipe r = match.getRecipe();
            recipesBuilder.append(String.format("ID: %d | Name: %s | Prep: %d mins | Calories: %.1f kcal | Match: %d/%d (%.1f%%)\n",
                    r.getRecipeId(), r.getName(), r.getTotalPrepTimeMinutes(),
                    r.getCaloriesKcal(), match.getMatchedIngredients(), match.getTotalIngredients(), match.getMatchPercentage()));

            List<RecipeNecessities> necessities = recipeNecessitiesRepository.findByRecipe(r);
            List<Ingredient> neededIngredients = necessities.stream()
                    .map(RecipeNecessities::getIngredient)
                    .toList();
            Map<Integer, Inventory> inventoryMap = inventoryRepository
                    .findByUserAndIngredientIn(user, neededIngredients)
                    .stream()
                    .collect(Collectors.toMap(
                            inv -> inv.getIngredient().getIngredientId(),
                            inv -> inv));

        String ingredients = necessities.stream()
                    .map(n -> {
                        double req = n.getQuantity();
                        double avail = inventoryMap.containsKey(n.getIngredient().getIngredientId())
                                ? inventoryMap.get(n.getIngredient().getIngredientId()).getQuantity()
                                : 0;
                        String statusStr;
                        if (avail >= req) {
                            statusStr = "Available";
                        } else if (avail > 0) {
                            statusStr = "Insufficient (have " + formatQuantity(avail) + ", need " + formatQuantity(req) + ")";
                        } else {
                            statusStr = "Missing (need " + formatQuantity(req) + ")";
                        }
                        return String.format("%s (%s %s) [%s]",
                                n.getIngredient().getName(),
                                formatQuantity(req),
                                n.getIngredient().getMeasurementUnit(),
                                statusStr);
                    })
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
            if (STOP_WORDS.contains(w)) continue;
            keywords.add(w);
        }
        return keywords;
    }

    private String formatQuantity(double quantity) {
        double rounded = Math.round(quantity * 100.0) / 100.0;
        if (rounded == (long) rounded) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}