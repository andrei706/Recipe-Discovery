package com.mds.recipediscovery.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    private FakeLlmClient llmClient = new FakeLlmClient();

    @Mock
    private ChatToolsService chatToolsService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecipeRepository recipeRepository;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(llmClient, chatToolsService, objectMapper, recipeRepository);
    }

    @Test
    void testGenerateMealPlan_Success() {
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeId(5);
        recipe1.setName("Tofu with Spinach");
        recipe1.setCaloriesKcal(280.0f);
        recipe1.setTotalPrepTimeMinutes(25);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe1));
        when(chatToolsService.inventorySnapshot(1)).thenReturn("Tofu, Spinach");

        String mockLlmResponse = "[{\"dayNumber\":1,\"mealType\":\"breakfast\",\"recipeId\":5}]";
        llmClient.setResponse(mockLlmResponse);

        List<Map<String, Object>> result = aiService.generateMealPlan(1, "Plan breakfast on day 1", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> entry = result.get(0);
        assertEquals(1, entry.get("dayNumber"));
        assertEquals("breakfast", entry.get("mealType"));
        assertEquals(5, entry.get("recipeId"));

        verify(recipeRepository).findAll();
        verify(chatToolsService).inventorySnapshot(1);
    }

    @Test
    void testGenerateMealPlan_InvalidDataFiltered() {
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeId(5);
        recipe1.setName("Tofu with Spinach");
        recipe1.setCaloriesKcal(280.0f);
        recipe1.setTotalPrepTimeMinutes(25);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe1));
        when(chatToolsService.inventorySnapshot(1)).thenReturn("");

        // dayNumber is 2 which is greater than numDays=1
        // mealType is invalid "lunch-time"
        // recipeId is 0 or negative
        String mockLlmResponse = "[" +
                "{\"dayNumber\":2,\"mealType\":\"lunch\",\"recipeId\":5}," +
                "{\"dayNumber\":1,\"mealType\":\"lunch-time\",\"recipeId\":5}," +
                "{\"dayNumber\":1,\"mealType\":\"breakfast\",\"recipeId\":-1}," +
                "{\"dayNumber\":1,\"mealType\":\"lunch\",\"recipeId\":5}" +
                "]";
        llmClient.setResponse(mockLlmResponse);

        List<Map<String, Object>> result = aiService.generateMealPlan(1, "Plan meals", 1);

        assertNotNull(result);
        assertEquals(1, result.size()); // Only the last one should be valid
        Map<String, Object> entry = result.get(0);
        assertEquals(1, entry.get("dayNumber"));
        assertEquals("lunch", entry.get("mealType"));
        assertEquals(5, entry.get("recipeId"));
    }

    @Test
    void testGenerateMealPlan_NestedDayMapSuccess() {
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeId(5);
        recipe1.setName("Tofu with Spinach");
        recipe1.setCaloriesKcal(280.0f);
        recipe1.setTotalPrepTimeMinutes(25);

        Recipe recipe2 = new Recipe();
        recipe2.setRecipeId(12);
        recipe2.setName("Grilled Tofu");
        recipe2.setCaloriesKcal(200.0f);
        recipe2.setTotalPrepTimeMinutes(15);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe1, recipe2));
        when(chatToolsService.inventorySnapshot(1)).thenReturn("");

        String mockLlmResponse = "{\n" +
                "  \"1\": {\n" +
                "    \"breakfast\": 5,\n" +
                "    \"lunch\": 12\n" +
                "  },\n" +
                "  \"day 2\": {\n" +
                "    \"dinner\": 5\n" +
                "  }\n" +
                "}";
        llmClient.setResponse(mockLlmResponse);

        List<Map<String, Object>> result = aiService.generateMealPlan(1, "Plan meals", 2);

        assertNotNull(result);
        assertEquals(3, result.size());

        // Validate first assignment: day 1 breakfast -> recipe 5
        Map<String, Object> a1 = result.stream()
                .filter(a -> (int) a.get("dayNumber") == 1 && "breakfast".equals(a.get("mealType")))
                .findFirst().orElseThrow();
        assertEquals(5, a1.get("recipeId"));

        // Validate second assignment: day 1 lunch -> recipe 12
        Map<String, Object> a2 = result.stream()
                .filter(a -> (int) a.get("dayNumber") == 1 && "lunch".equals(a.get("mealType")))
                .findFirst().orElseThrow();
        assertEquals(12, a2.get("recipeId"));

        // Validate third assignment: day 2 dinner -> recipe 5
        Map<String, Object> a3 = result.stream()
                .filter(a -> (int) a.get("dayNumber") == 2 && "dinner".equals(a.get("mealType")))
                .findFirst().orElseThrow();
        assertEquals(5, a3.get("recipeId"));
    }

    @Test
    void testGetChatResponse_StripsAsterisks() {
        com.mds.recipediscovery.models.User mockUser = new com.mds.recipediscovery.models.User();
        mockUser.setUsername("Andrei");
        when(chatToolsService.findUserOrThrow(1)).thenReturn(mockUser);
        when(chatToolsService.inventorySnapshot(1)).thenReturn("Spinach");
        when(chatToolsService.topRecipesForPrompt(eq(1), anyString(), eq(5))).thenReturn(List.of());
        when(chatToolsService.recipeContext(eq(1), anyList())).thenReturn("Some context");

        String mockLlmResponse = "{\n" +
                "  \"agent1Response\": \"I suggest cooking **Chia Pudding** and **Tofu with Spinach**!\",\n" +
                "  \"recommendedRecipeIds\": [5, 12]\n" +
                "}";
        llmClient.setResponse(mockLlmResponse);

        Map<String, Object> response = aiService.getChatResponse(1, "What should I eat?");

        assertNotNull(response);
        assertEquals("I suggest cooking Chia Pudding and Tofu with Spinach!", response.get("agent1Response"));
        assertEquals(List.of(5, 12), response.get("recommendedRecipeIds"));
    }

    @Test
    void testGenerateMealPlan_ConversationalPrefix() {
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeId(5);
        recipe1.setName("Tofu with Spinach");
        recipe1.setCaloriesKcal(280.0f);
        recipe1.setTotalPrepTimeMinutes(25);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe1));
        when(chatToolsService.inventorySnapshot(1)).thenReturn("");

        String mockLlmResponse = "Here is the meal plan you requested:\n" +
                "```json\n" +
                "[{\"dayNumber\":1,\"mealType\":\"breakfast\",\"recipeId\":5}]\n" +
                "```\n" +
                "Hope you like it!";
        llmClient.setResponse(mockLlmResponse);

        List<Map<String, Object>> result = aiService.generateMealPlan(1, "Plan breakfast on day 1", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> entry = result.get(0);
        assertEquals(1, entry.get("dayNumber"));
        assertEquals("breakfast", entry.get("mealType"));
        assertEquals(5, entry.get("recipeId"));
    }

    private static class FakeLlmClient extends LlmClient {
        private String response;

        public FakeLlmClient() {
            super(new ObjectMapper());
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public String generate(String systemPrompt, String userPrompt) {
            return response;
        }
    }
}
