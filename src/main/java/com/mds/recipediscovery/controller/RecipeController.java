package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.services.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<Recipe>> getAvailableRecipes(Authentication authentication) {
        return ResponseEntity.ok(recipeService.findAvailableRecipes(getCurrentUserId(authentication)));
    }

    @GetMapping("/sorted-by-match")
    public ResponseEntity<List<Recipe>> getRecipesSortedByMatchingIngredients(Authentication authentication) {
        return ResponseEntity.ok(recipeService.findRecipesSortedByMatchingIngredients(getCurrentUserId(authentication)));
    }

    @GetMapping("/match-percentage")
    public ResponseEntity<List<RecipeMatchDTO>> getRecipesWithMatchPercentage(Authentication authentication) {
        return ResponseEntity.ok(recipeService.getRecipesWithMatchPercentage(getCurrentUserId(authentication)));
    }

    @PostMapping("/{recipeId}/cook")
    public ResponseEntity<String> cookRecipe(@PathVariable Integer recipeId, Authentication authentication) {
        try {
            recipeService.cookRecipe(getCurrentUserId(authentication), recipeId);
            return ResponseEntity.ok("Recipe cooked successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Utilizator neautentificat");
        }

        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token invalid");
        }
    }
}
