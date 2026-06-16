package com.mds.recipediscovery.services;
import com.mds.recipediscovery.dto.CreatePlanDTO;
import com.mds.recipediscovery.models.Plan;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.PlanRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock private PlanRepository planRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private PlanService planService;
    private User testUser;
    private LocalDate today;
    private LocalDate tomorrow;
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
    }
    @Test
    void testCreatePlan_Success() {
        CreatePlanDTO dto = new CreatePlanDTO("Test Plan", today, tomorrow);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        Plan savedPlan = new Plan();
        savedPlan.setPlanId(1);
        savedPlan.setName("Test Plan");
        savedPlan.setStartDate(today);
        savedPlan.setEndDate(tomorrow);
        savedPlan.setActive(true);
        when(planRepository.save(any(Plan.class))).thenReturn(savedPlan);
        var result = planService.createPlan(1, dto);
        assertNotNull(result);
        assertEquals("Test Plan", result.getName());
        assertTrue(result.isActive());
        verify(planRepository, times(1)).save(any(Plan.class));
    }
    @Test
    void testCreatePlan_InvalidDates() {
        LocalDate later = today.plusDays(5);
        CreatePlanDTO dto = new CreatePlanDTO("Invalid", later, today);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        assertThrows(IllegalArgumentException.class, () -> planService.createPlan(1, dto));
    }
    @Test
    void testCreatePlan_UserNotFound() {
        CreatePlanDTO dto = new CreatePlanDTO("Plan", today, tomorrow);
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> planService.createPlan(99, dto));
    }
    @Test
    void testDeletePlan_Success() {
        Plan plan = new Plan();
        plan.setPlanId(1);
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));
        planService.deletePlan(1);
        verify(planRepository, times(1)).delete(plan);
    }
    @Test
    void testDeletePlan_Failure_AiProcessing() {
        Plan plan = new Plan();
        plan.setPlanId(1);
        plan.setAiProcessing(true);
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));
        org.springframework.web.server.ResponseStatusException exception =
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> planService.deletePlan(1));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cannot delete a plan while AI is generating meals", exception.getReason());
        verify(planRepository, never()).delete(any(Plan.class));
    }
    @Test
    void testGetPlansByUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        Plan plan1 = new Plan();
        plan1.setPlanId(1);
        plan1.setName("Plan 1");
        when(planRepository.findByUserUserId(1)).thenReturn(new ArrayList<>(List.of(plan1)));
        var result = planService.getPlansByUser(1);
        assertEquals(1, result.size());
    }
}