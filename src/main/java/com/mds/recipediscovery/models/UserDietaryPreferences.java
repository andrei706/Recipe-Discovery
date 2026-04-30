package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

class UserDietaryPreferenceId implements Serializable {
    private Integer user;
    private Integer diet;

    public UserDietaryPreferenceId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDietaryPreferenceId)) return false;
        UserDietaryPreferenceId that = (UserDietaryPreferenceId) o;
        return Objects.equals(user, that.user) && Objects.equals(diet, that.diet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, diet);
    }
}

@Entity
@Table(name = "user_dietary_preferences")
@IdClass(UserDietaryPreferenceId.class)
public class UserDietaryPreferences {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "diet_id")
    private Diet diet;

    public UserDietaryPreferences() {}

    public UserDietaryPreferences(User user, Diet diet) {
        this.user = user;
        this.diet = diet;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Diet getDiet() { return diet; }
    public void setDiet(Diet diet) { this.diet = diet; }
}