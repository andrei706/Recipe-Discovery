package com.mds.recipediscovery.services;
import com.mds.recipediscovery.dto.CreatePlanDetailDTO;
import com.mds.recipediscovery.models.*;
import com.mds.recipediscovery.repository.PlanDetailsRepository;
import com.mds.recipediscovery.repository.PlanRepository;
import com.mds.recipediscovery.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PlanDetailsServiceTest {
    @Mock private PlanDetailsRepository planDetailsRepository;
    @Mock private PlanRepository planRepository;
    @Mock private RecipeRepository recipeRepository;
    @InjectMocks private PlanDetailsService planDetailsService;
    private Plan testPlan;
    private Recipe testRecipe;
    @BeforeEach
    void setUp() {
        testPlan = new Plan();
        testPlan.setPlanId(1);
        testRecipe = new Recipe();
        testRecipe.setRecipeId(1);
        testRecipe.setName("Test Recipe");
    }
    @Test
    void testAddDetail_Success() {
        CreatePlanDetailDTO dto = new CreatePlanDetailDTO(1, MealType.lunch, 1, 3);
        when(planRepository.findById(1)).thenReturn(Optional.of(testPlan));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(planDetailsRepository.findByPlanPlanId(1)).thenReturn(new ArrayList<>());
        PlanDetails savedDetail = new PlanDetails();
        savedDetail.setPlanDetailId(1);
        savedDetail.setRecipe(testRecipe);
        savedDetail.setMealType(MealType.lunch);
        savedDetail.setDayNumber(1);
        savedDetail.setQuantity(3);
        when(planDetailsRepository.save(any(PlanDetails.class))).thenReturn(savedDetail);
        var result = planDetailsService.addDetail(1, dto);
        assertNotNull(result);
        assertEquals(1, result.getDayNumber());
        assertEquals(MealType.lunch, result.getMealType());
    }
    @Test
    void testAddDetail_InvalidDay() {
        CreatePlanDetailDTO dto = new CreatePlanDetailDTO(1, MealType.lunch, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> planDetailsService.addDetail(1, dto));
    }
    @Test
    void testAddDetail_InvalidQuantity() {
        CreatePlanDetailDTO dto = new CreatePlanDetailDTO(1, MealType.lunch, 1, 0);
        assertThrows(IllegalArgumentException.class, () -> planDetailsService.addDetail(1, dto));
    }
    @Test
    void testDeleteDetail_Success() {
        PlanDetails detail = new PlanDetails();
        detail.setPlanDetailId(1);
        when(planDetailsRepository.findById(1)).thenReturn(Optional.of(detail));
        planDetailsService.deleteDetail(1);
        verify(planDetailsRepository, times(1)).delete(detail);
    }
    @Test
    void testGetDetailsByPlan_Success() {
        when(planRepository.findById(1)).thenReturn(Optional.of(testPlan));
        PlanDetails detail = new PlanDetails();
        detail.setRecipe(testRecipe);
        detail.setMealType(MealType.lunch);
        when(planDetailsRepository.findByPlanPlanId(1)).thenReturn(new ArrayList<>(List.of(detail)));
        var result = planDetailsService.getDetailsByPlan(1);
        assertEquals(1, result.size());
    }
}