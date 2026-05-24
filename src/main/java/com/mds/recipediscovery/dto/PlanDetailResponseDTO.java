package com.mds.recipediscovery.dto;

import com.mds.recipediscovery.models.MealType;

public class PlanDetailResponseDTO {
    private Integer planDetailId;
    private Integer recipeId;
    private String recipeName;
    private MealType mealType;
    private int dayNumber;
    private boolean isFollowed;
    private int quantity;

    public PlanDetailResponseDTO(
            Integer planDetailId,
            Integer recipeId,
            String recipeName,
            MealType mealType,
            int dayNumber,
            boolean isFollowed,
            int quantity
    ) {
        this.planDetailId = planDetailId;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.mealType = mealType;
        this.dayNumber = dayNumber;
        this.isFollowed = isFollowed;
        this.quantity = quantity;
    }

    public Integer getPlanDetailId() {
        return planDetailId;
    }

    public void setPlanDetailId(Integer planDetailId) {
        this.planDetailId = planDetailId;
    }

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
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

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}