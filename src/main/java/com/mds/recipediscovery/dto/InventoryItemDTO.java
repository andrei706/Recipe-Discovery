package com.mds.recipediscovery.dto;

public class InventoryItemDTO {
    private Integer ingredientId;
    private String ingredientName;
    private String measurementUnit;
    private double quantity;

    public InventoryItemDTO(Integer ingredientId, String ingredientName, String measurementUnit, double quantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.measurementUnit = measurementUnit;
        this.quantity = Math.round(quantity * 100.0) / 100.0;
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

    public double getQuantity() {
        return quantity;
    }
}

