package com.mds.recipediscovery.dto;

public class RecipeIngredientStatusDTO {
    private Integer ingredientId;
    private String ingredientName;
    private String measurementUnit;
    private double requiredQuantity;
    private double availableQuantity;
    private double missingQuantity;

    public RecipeIngredientStatusDTO(Integer ingredientId,
                                     String ingredientName,
                                     String measurementUnit,
                                     double requiredQuantity,
                                     double availableQuantity,
                                     double missingQuantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.measurementUnit = measurementUnit;
        this.requiredQuantity = Math.round(requiredQuantity * 100.0) / 100.0;
        this.availableQuantity = Math.round(availableQuantity * 100.0) / 100.0;
        this.missingQuantity = Math.round(missingQuantity * 100.0) / 100.0;
    }

    public Integer getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public double getMissingQuantity() {
        return missingQuantity;
    }
}

