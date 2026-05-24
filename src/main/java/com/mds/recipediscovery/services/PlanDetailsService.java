package com.mds.recipediscovery.services;
import com.mds.recipediscovery.dto.CreatePlanDetailDTO;
import com.mds.recipediscovery.dto.PlanDetailResponseDTO;
import com.mds.recipediscovery.models.*;
import com.mds.recipediscovery.repository.PlanDetailsRepository;
import com.mds.recipediscovery.repository.PlanRepository;
import com.mds.recipediscovery.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class PlanDetailsService {
    private final PlanDetailsRepository planDetailsRepository;
    private final PlanRepository planRepository;
    private final RecipeRepository recipeRepository;
    public PlanDetailsService(PlanDetailsRepository planDetailsRepository, PlanRepository planRepository, RecipeRepository recipeRepository) {
        this.planDetailsRepository = planDetailsRepository;
        this.planRepository = planRepository;
        this.recipeRepository = recipeRepository;
    }
    public PlanDetailResponseDTO addDetail(Integer planId, CreatePlanDetailDTO dto) {
        if (dto.getDayNumber() < 1 || dto.getDayNumber() > 31) {
            throw new IllegalArgumentException("Day number must be between 1 and 31");
        }
        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        Recipe recipe = recipeRepository.findById(dto.getRecipeId()).orElseThrow(() -> new RuntimeException("Recipe not found"));
        List<PlanDetails> existing = planDetailsRepository.findByPlanPlanId(planId);
        boolean isDuplicate = existing.stream().anyMatch(d -> d.getDayNumber() == dto.getDayNumber() && d.getMealType() == dto.getMealType());
        if (isDuplicate) {
            throw new IllegalArgumentException(String.format("A meal of type %s already exists for day %d in this plan", dto.getMealType(), dto.getDayNumber()));
        }
        PlanDetails detail = new PlanDetails();
        detail.setPlan(plan);
        detail.setRecipe(recipe);
        detail.setMealType(dto.getMealType());
        detail.setDayNumber(dto.getDayNumber());
        detail.setQuantity(dto.getQuantity());
        detail.setFollowed(false);
        PlanDetails saved = planDetailsRepository.save(detail);
        return new PlanDetailResponseDTO(saved.getPlanDetailId(), saved.getRecipe().getRecipeId(), saved.getRecipe().getName(), saved.getMealType(), saved.getDayNumber(), saved.isFollowed(), saved.getQuantity());
    }
    public PlanDetailResponseDTO updateDetail(Integer detailId, boolean isFollowed) {
        PlanDetails detail = planDetailsRepository.findById(detailId).orElseThrow(() -> new RuntimeException("Plan detail not found"));
        detail.setFollowed(isFollowed);
        PlanDetails updated = planDetailsRepository.save(detail);
        return new PlanDetailResponseDTO(updated.getPlanDetailId(), updated.getRecipe().getRecipeId(), updated.getRecipe().getName(), updated.getMealType(), updated.getDayNumber(), updated.isFollowed(), updated.getQuantity());
    }
    public void deleteDetail(Integer detailId) {
        PlanDetails detail = planDetailsRepository.findById(detailId).orElseThrow(() -> new RuntimeException("Plan detail not found"));
        planDetailsRepository.delete(detail);
    }
    public List<PlanDetailResponseDTO> getDetailsByPlan(Integer planId) {
        planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        return planDetailsRepository.findByPlanPlanId(planId).stream().map(detail -> new PlanDetailResponseDTO(detail.getPlanDetailId(), detail.getRecipe().getRecipeId(), detail.getRecipe().getName(), detail.getMealType(), detail.getDayNumber(), detail.isFollowed(), detail.getQuantity())).collect(Collectors.toList());
    }
}