package com.mds.recipediscovery.dto;
import java.time.LocalDate;
import java.util.Set;
public class PlanResponseDTO {
    private Integer planId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private Set<PlanDetailResponseDTO> details;
    public PlanResponseDTO(Integer planId, String name, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.planId = planId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }
    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Set<PlanDetailResponseDTO> getDetails() { return details; }
    public void setDetails(Set<PlanDetailResponseDTO> details) { this.details = details; }
}