package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredients")
@IdClass(RecipeNecessitiesId.class)
public class RecipeNecessities {
    @Id
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false)
    private int quantity;

    public RecipeNecessities() {}

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}