package com.mds.recipediscovery.models;

import jakarta.persistence.*;

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
    private double quantity;

    public Inventory() {}

    public Inventory(User user, Ingredient ingredient, double quantity) {
        this.user = user;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}