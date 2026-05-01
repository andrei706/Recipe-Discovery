package com.mds.recipediscovery.models;

import java.io.Serializable;
import java.util.Objects;

public class InventoryId implements Serializable {
    private Integer user;
    private Integer ingredient;

    public InventoryId() {}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryId)) return false;
        InventoryId that = (InventoryId) o;
        return Objects.equals(user, that.user) && Objects.equals(ingredient, that.ingredient);
    }
    @Override
    public int hashCode() { return Objects.hash(user, ingredient); }
}
