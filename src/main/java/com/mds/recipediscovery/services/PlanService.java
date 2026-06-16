package com.mds.recipediscovery.services;
import com.mds.recipediscovery.dto.CreatePlanDTO;
import com.mds.recipediscovery.dto.PlanResponseDTO;
import com.mds.recipediscovery.models.Plan;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.PlanDetailsRepository;
import com.mds.recipediscovery.repository.PlanRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class PlanService {
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PlanDetailsRepository planDetailsRepository;
    public PlanService(PlanRepository planRepository, UserRepository userRepository, PlanDetailsRepository planDetailsRepository) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.planDetailsRepository = planDetailsRepository;
    }
    public PlanResponseDTO createPlan(Integer userId, CreatePlanDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        Plan plan = new Plan();
        plan.setUser(user);
        plan.setName(dto.getName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setActive(true);
        plan.setAiProcessing(false);
        Plan savedPlan = planRepository.save(plan);
        return new PlanResponseDTO(savedPlan.getPlanId(), savedPlan.getName(), savedPlan.getStartDate(), savedPlan.getEndDate(), savedPlan.isActive(), savedPlan.isAiProcessing());
    }
    public PlanResponseDTO updatePlan(Integer planId, CreatePlanDTO dto) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        // Calculate new plan duration and remove details that fall outside it
        long newDayCount = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        planDetailsRepository.deleteByPlanPlanIdAndDayNumberGreaterThan(planId, (int) newDayCount);
        plan.setName(dto.getName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        Plan updated = planRepository.save(plan);
        return new PlanResponseDTO(updated.getPlanId(), updated.getName(), updated.getStartDate(), updated.getEndDate(), updated.isActive(), updated.isAiProcessing());
    }
    public void deletePlan(Integer planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (plan.isAiProcessing()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a plan while AI is generating meals");
        }
        planRepository.delete(plan);
    }
    public List<PlanResponseDTO> getPlansByUser(Integer userId) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return planRepository.findByUserUserId(userId).stream().map(plan -> new PlanResponseDTO(plan.getPlanId(), plan.getName(), plan.getStartDate(), plan.getEndDate(), plan.isActive(), plan.isAiProcessing())).collect(Collectors.toList());
    }
    public void deactivatePlan(Integer planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setActive(false);
        planRepository.save(plan);
    }
    public void setPlanAiProcessing(Integer planId, boolean processing) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setAiProcessing(processing);
        planRepository.save(plan);
    }
}