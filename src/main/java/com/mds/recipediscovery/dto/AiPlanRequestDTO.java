package com.mds.recipediscovery.dto;

public class AiPlanRequestDTO {
    private Integer planId;
    private String prompt;
    private int numDays;

    public AiPlanRequestDTO() {}

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public int getNumDays() { return numDays; }
    public void setNumDays(int numDays) { this.numDays = numDays; }
}
