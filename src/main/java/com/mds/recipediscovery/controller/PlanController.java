package com.mds.recipediscovery.controller;
import com.mds.recipediscovery.dto.CreatePlanDTO;
import com.mds.recipediscovery.dto.CreatePlanDetailDTO;
import com.mds.recipediscovery.dto.PlanDetailResponseDTO;
import com.mds.recipediscovery.dto.PlanResponseDTO;
import com.mds.recipediscovery.services.PlanDetailsService;
import com.mds.recipediscovery.services.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanController {
    private final PlanService planService;
    private final PlanDetailsService planDetailsService;
    public PlanController(PlanService planService, PlanDetailsService planDetailsService) {
        this.planService = planService;
        this.planDetailsService = planDetailsService;
    }
    @PostMapping
    public ResponseEntity<PlanResponseDTO> createPlan(@RequestBody CreatePlanDTO dto, Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(userId, dto));
    }
    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> getUserPlans(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(planService.getPlansByUser(userId));
    }
    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@PathVariable Integer planId, @RequestBody CreatePlanDTO dto, Authentication authentication) {
        getCurrentUserId(authentication);
        return ResponseEntity.ok(planService.updatePlan(planId, dto));
    }
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer planId, Authentication authentication) {
        getCurrentUserId(authentication);
        planService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{planId}/details")
    public ResponseEntity<PlanDetailResponseDTO> addPlanDetail(@PathVariable Integer planId, @RequestBody CreatePlanDetailDTO dto, Authentication authentication) {
        getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(planDetailsService.addDetail(planId, dto));
    }
    @GetMapping("/{planId}/details")
    public ResponseEntity<List<PlanDetailResponseDTO>> getPlanDetails(@PathVariable Integer planId, Authentication authentication) {
        getCurrentUserId(authentication);
        return ResponseEntity.ok(planDetailsService.getDetailsByPlan(planId));
    }
    @PutMapping("/details/{detailId}")
    public ResponseEntity<PlanDetailResponseDTO> updatePlanDetail(@PathVariable Integer detailId, @RequestParam boolean isFollowed, Authentication authentication) {
        getCurrentUserId(authentication);
        return ResponseEntity.ok(planDetailsService.updateDetail(detailId, isFollowed));
    }
    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<Void> deletePlanDetail(@PathVariable Integer detailId, Authentication authentication) {
        getCurrentUserId(authentication);
        planDetailsService.deleteDetail(detailId);
        return ResponseEntity.noContent().build();
    }
    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "User not authenticated");
        }
        return Integer.parseInt(authentication.getName());
    }
}