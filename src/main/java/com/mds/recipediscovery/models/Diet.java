package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "diets")
public class Diet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dietId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "healthy_score", nullable = false)
    private int healthyScore;

    public Diet() {}

    public Integer getDietId() { return dietId; }
    public void setDietId(Integer dietId) { this.dietId = dietId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHealthyScore() { return healthyScore; }
    public void setHealthyScore(int healthyScore) { this.healthyScore = healthyScore; }
}