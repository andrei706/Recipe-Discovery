package com.mds.recipediscovery.dto;

public class InventoryItemDTO {
    private Integer ingredientId;
    private String ingredientName;
    private String measurementUnit;
    private int quantity;

    public InventoryItemDTO(Integer ingredientId, String ingredientName, String measurementUnit, int quantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.measurementUnit = measurementUnit;
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }
}

