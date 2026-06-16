package com.mds.recipediscovery.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_ai_processing", nullable = false)
    private boolean isAiProcessing = false;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlanDetails> details;

    public Plan() {}

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isAiProcessing() { return isAiProcessing; }
    public void setAiProcessing(boolean aiProcessing) { isAiProcessing = aiProcessing; }
    public Set<PlanDetails> getDetails() { return details; }
    public void setDetails(Set<PlanDetails> details) { this.details = details; }
}

