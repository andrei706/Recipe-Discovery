package com.mds.recipediscovery.dto;

import com.mds.recipediscovery.models.Recipe;

public class RecipeMatchDTO {
    private Recipe recipe;
    private int matchedIngredients;
    private int totalIngredients;
    private double matchPercentage;

    public RecipeMatchDTO(Recipe recipe, int matchedIngredients, int totalIngredients) {
        this.recipe = recipe;
        this.matchedIngredients = matchedIngredients;
        this.totalIngredients = totalIngredients;
        if (totalIngredients > 0) {
            // Calculează procentul cu două zecimale
            this.matchPercentage = Math.round((double) matchedIngredients / totalIngredients * 100.0 * 100.0) / 100.0;
        } else {
            this.matchPercentage = 100.0;
        }
    }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public int getMatchedIngredients() { return matchedIngredients; }
    public void setMatchedIngredients(int matchedIngredients) { this.matchedIngredients = matchedIngredients; }
    public int getTotalIngredients() { return totalIngredients; }
    public void setTotalIngredients(int totalIngredients) { this.totalIngredients = totalIngredients; }
    public double getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(double matchPercentage) { this.matchPercentage = matchPercentage; }
}
