package com.mds.recipediscovery.dto;

import com.mds.recipediscovery.models.Ingredient;

public class IngredientDTO {
    private Integer ingredientId;
    private String name;
    private String measurementUnit;

    public IngredientDTO(Ingredient ingredient) {
        this.ingredientId = ingredient.getIngredientId();
        this.name = ingredient.getName();
        this.measurementUnit = ingredient.getMeasurementUnit();
    }

    public Integer getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }
}

