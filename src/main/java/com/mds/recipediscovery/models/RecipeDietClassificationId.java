package com.mds.recipediscovery.models;

import java.io.Serializable;
import java.util.Objects;

public class RecipeDietClassificationId implements Serializable {
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
