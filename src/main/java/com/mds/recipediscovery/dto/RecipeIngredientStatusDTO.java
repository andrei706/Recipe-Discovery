package com.mds.recipediscovery.dto;

public class RecipeIngredientStatusDTO {
    private Integer ingredientId;
    private String ingredientName;
    private String measurementUnit;
    private int requiredQuantity;
    private int availableQuantity;
    private int missingQuantity;

    public RecipeIngredientStatusDTO(Integer ingredientId,
                                     String ingredientName,
                                     String measurementUnit,
                                     int requiredQuantity,
                                     int availableQuantity,
                                     int missingQuantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.measurementUnit = measurementUnit;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
        this.missingQuantity = missingQuantity;
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

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getMissingQuantity() {
        return missingQuantity;
    }
}

