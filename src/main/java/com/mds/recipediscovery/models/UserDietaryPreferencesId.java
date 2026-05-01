package com.mds.recipediscovery.models;

import java.io.Serializable;
import java.util.Objects;

public class UserDietaryPreferencesId implements Serializable {
    private Integer user;
    private Integer diet;

    public UserDietaryPreferencesId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDietaryPreferencesId)) return false;
        UserDietaryPreferencesId that = (UserDietaryPreferencesId) o;
        return Objects.equals(user, that.user) && Objects.equals(diet, that.diet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, diet);
    }
}
