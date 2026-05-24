package com.mds.recipediscovery.models;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_details", uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "day_number", "meal_type"}))
public class PlanDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planDetailId;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(name = "day_number", nullable = false)
    private int dayNumber;

    @Column(name = "is_followed", nullable = false)
    private boolean isFollowed = false;

    @Column(nullable = false)
    private int quantity;

    public PlanDetails() {}

    public Integer getPlanDetailId() { return planDetailId; }
    public void setPlanDetailId(Integer planDetailId) { this.planDetailId = planDetailId; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
    public boolean isFollowed() { return isFollowed; }
    public void setFollowed(boolean followed) { isFollowed = followed; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

