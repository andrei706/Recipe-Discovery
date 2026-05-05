package com.mds.recipediscovery.dto;

import com.mds.recipediscovery.models.Recipe;

import java.util.List;

public class RecipeMatchDetailsDTO {
    private Recipe recipe;
    private int matchedIngredients;
    private int totalIngredients;
    private double matchPercentage;
    private List<RecipeIngredientStatusDTO> ingredients;

    public RecipeMatchDetailsDTO(Recipe recipe,
                                 int matchedIngredients,
                                 int totalIngredients,
                                 double matchPercentage,
                                 List<RecipeIngredientStatusDTO> ingredients) {
        this.recipe = recipe;
        this.matchedIngredients = matchedIngredients;
        this.totalIngredients = totalIngredients;
        this.matchPercentage = matchPercentage;
        this.ingredients = ingredients;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public int getMatchedIngredients() {
        return matchedIngredients;
    }

    public int getTotalIngredients() {
        return totalIngredients;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public List<RecipeIngredientStatusDTO> getIngredients() {
        return ingredients;
    }
}

