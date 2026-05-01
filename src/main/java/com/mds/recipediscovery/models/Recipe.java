package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recipeId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "total_prep_time_minutes")
    private int totalPrepTimeMinutes;

    @Column(name = "calories_kcal", nullable = false)
    private float caloriesKcal;

    @Column(name = "fats_g", nullable = false)
    private float fatsG;

    @Column(name = "proteins_g", nullable = false)
    private float proteinsG;

    @Column(name = "carbohydrates_g", nullable = false)
    private float carbohydratesG;

    @ManyToMany
    @JoinTable(
            name = "recipe_diet_classifications",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "diet_id")
    )
    private Set<Diet> dietClassifications;

    public Recipe() {}

    public Integer getRecipeId() { return recipeId; }
    public void setRecipeId(Integer recipeId) { this.recipeId = recipeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(float caloriesKcal) { this.caloriesKcal = caloriesKcal; }
}