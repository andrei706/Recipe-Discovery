package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_dietary_preferences")
@IdClass(UserDietaryPreferencesId.class)
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