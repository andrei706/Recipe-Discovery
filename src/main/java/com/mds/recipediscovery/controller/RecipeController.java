package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.services.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeService recipeService;

    // user1 for now until the userservice is done
    private final Integer CURRENT_USER_ID = 1;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<Recipe>> getAvailableRecipes() {
        return ResponseEntity.ok(recipeService.findAvailableRecipes(CURRENT_USER_ID));
    }

    @GetMapping("/sorted-by-match")
    public ResponseEntity<List<Recipe>> getRecipesSortedByMatchingIngredients() {
        return ResponseEntity.ok(recipeService.findRecipesSortedByMatchingIngredients(CURRENT_USER_ID));
    }

    @GetMapping("/match-percentage")
    public ResponseEntity<List<RecipeMatchDTO>> getRecipesWithMatchPercentage() {
        return ResponseEntity.ok(recipeService.getRecipesWithMatchPercentage(CURRENT_USER_ID));
    }

    @PostMapping("/{recipeId}/cook")
    public ResponseEntity<String> cookRecipe(@PathVariable Integer recipeId) {
        try {
            recipeService.cookRecipe(CURRENT_USER_ID, recipeId);
            return ResponseEntity.ok("Recipe cooked successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
