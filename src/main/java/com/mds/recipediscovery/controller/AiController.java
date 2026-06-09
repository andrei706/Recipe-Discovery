package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.dto.AiPlanRequestDTO;
import com.mds.recipediscovery.dto.AiRequestDTO;
import com.mds.recipediscovery.dto.CreatePlanDetailDTO;
import com.mds.recipediscovery.dto.PlanDetailResponseDTO;
import com.mds.recipediscovery.models.MealType;
import com.mds.recipediscovery.services.AiService;
import com.mds.recipediscovery.services.PlanDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;
    private final PlanDetailsService planDetailsService;

    public AiController(AiService aiService, PlanDetailsService planDetailsService) {
        this.aiService = aiService;
        this.planDetailsService = planDetailsService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> getChatResponse(
            @RequestBody AiRequestDTO request,
            Authentication authentication) {
        try {
            Integer userId = getCurrentUserId(authentication);
            Map<String, Object> response = aiService.getChatResponse(userId, request.getPrompt());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<?> generatePlan(
            @RequestBody AiPlanRequestDTO request,
            Authentication authentication) {
        try {
            Integer userId = getCurrentUserId(authentication);
            List<Map<String, Object>> assignments = aiService.generateMealPlan(
                    userId, request.getPrompt(), request.getNumDays());

            // Bulk-create plan details from the AI assignments
            List<PlanDetailResponseDTO> created = new ArrayList<>();
            for (Map<String, Object> a : assignments) {
                try {
                    CreatePlanDetailDTO dto = new CreatePlanDetailDTO();
                    dto.setRecipeId((Integer) a.get("recipeId"));
                    dto.setMealType(MealType.valueOf(((String) a.get("mealType")).toLowerCase()));
                    dto.setDayNumber((Integer) a.get("dayNumber"));
                    dto.setQuantity(1);
                    created.add(planDetailsService.addDetail(request.getPlanId(), dto));
                } catch (Exception ignored) {
                    // Skip duplicate slots silently — backend already enforces one per slot
                }
            }
            return ResponseEntity.ok(created);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
        }
    }
}
