package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

class RecipeDietClassificationId implements Serializable {
    private Integer recipe;
    private Integer diet;

    public RecipeDietClassificationId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeDietClassificationId)) return false;
        RecipeDietClassificationId that = (RecipeDietClassificationId) o;
        return Objects.equals(recipe, that.recipe) && Objects.equals(diet, that.diet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, diet);
    }
}

@Entity
@Table(name = "recipe_diet_classifications")
@IdClass(RecipeDietClassificationId.class)
public class RecipeDietClassification {

    @Id
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "diet_id")
    private Diet diet;

    public RecipeDietClassification() {}

    public RecipeDietClassification(Recipe recipe, Diet diet) {
        this.recipe = recipe;
        this.diet = diet;
    }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public Diet getDiet() { return diet; }
    public void setDiet(Diet diet) { this.diet = diet; }
}