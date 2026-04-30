package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

class InventoryId implements Serializable {
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

@Entity
@Table(name = "inventory")
@IdClass(InventoryId.class)
public class Inventory {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false)
    private BigDecimal quantity;

    public Inventory() {}

    public Inventory(User user, Ingredient ingredient, BigDecimal quantity) {
        this.user = user;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}