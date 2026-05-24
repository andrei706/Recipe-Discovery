package com.mds.recipediscovery.dto;

import com.mds.recipediscovery.models.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreatePlanDetailDTO {
    @NotNull(message = "Recipe ID cannot be null")
    private Integer recipeId;

    @NotNull(message = "Meal type cannot be null")
    private MealType mealType;

    @Min(value = 1, message = "Day number must be between 1 and 31")
    @Max(value = 31, message = "Day number must be between 1 and 31")
    private int dayNumber;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

    public CreatePlanDetailDTO() {
    }

    public CreatePlanDetailDTO(Integer recipeId, MealType mealType, int dayNumber, int quantity) {
        this.recipeId = recipeId;
        this.mealType = mealType;
        this.dayNumber = dayNumber;
        this.quantity = quantity;
    }

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}