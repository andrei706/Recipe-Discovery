package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.util.Set;
import java.util.List;
import com.mds.recipediscovery.models.converters.StringListConverter;

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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "preparation_steps", columnDefinition = "TEXT")
    private String preparationSteps;

    @Column(name = "calories_kcal", nullable = false)
    private float caloriesKcal;

    @Column(name = "fats_g", nullable = false)
    private float fatsG;

    @Column(name = "proteins_g", nullable = false)
    private float proteinsG;

    @Column(name = "carbohydrates_g", nullable = false)
    private float carbohydratesG;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recipe_diet_classifications",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "diet_id")
    )
    private Set<Diet> dietClassifications;

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter.class)
    private List<String> features;

    public Recipe() {}

    public Integer getRecipeId() { return recipeId; }
    public void setRecipeId(Integer recipeId) { this.recipeId = recipeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTotalPrepTimeMinutes() { return totalPrepTimeMinutes; }
    public void setTotalPrepTimeMinutes(int totalPrepTimeMinutes) { this.totalPrepTimeMinutes = totalPrepTimeMinutes; }
    public float getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(float caloriesKcal) { this.caloriesKcal = caloriesKcal; }
    public float getFatsG() { return fatsG; }
    public void setFatsG(float fatsG) { this.fatsG = fatsG; }
    public float getProteinsG() { return proteinsG; }
    public void setProteinsG(float proteinsG) { this.proteinsG = proteinsG; }
    public float getCarbohydratesG() { return carbohydratesG; }
    public void setCarbohydratesG(float carbohydratesG) { this.carbohydratesG = carbohydratesG; }
    public Set<Diet> getDietClassifications() { return dietClassifications; }
    public void setDietClassifications(Set<Diet> dietClassifications) { this.dietClassifications = dietClassifications; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPreparationSteps() { return preparationSteps; }
    public void setPreparationSteps(String preparationSteps) { this.preparationSteps = preparationSteps; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}