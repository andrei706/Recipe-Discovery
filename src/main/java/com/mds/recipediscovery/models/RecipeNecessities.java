package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

class RecipeNecessitiesId implements Serializable {
    private Integer recipe;
    private Integer ingredient;
}

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
    private BigDecimal quantity;

    public RecipeNecessities() {}

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}