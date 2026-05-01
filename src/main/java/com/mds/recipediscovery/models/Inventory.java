package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

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
    private int quantity;

    public Inventory() {}

    public Inventory(User user, Ingredient ingredient, int quantity) {
        this.user = user;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}