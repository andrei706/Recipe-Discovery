package com.mds.recipediscovery.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@WithMockUser(username = "1")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
        jdbcTemplate.execute("INSERT INTO users (user_id, username, email, password) VALUES (1, 'testuser', 'testuser@email.com', 'password123')");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void testGetAvailableRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes/available"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRecipesSortedByMatchingIngredients() throws Exception {
        mockMvc.perform(get("/api/recipes/sorted-by-match"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRecipesWithMatchPercentage() throws Exception {
        mockMvc.perform(get("/api/recipes/match-percentage"))
                .andExpect(status().isOk());
    }

    @Test
    void testCookRecipe() throws Exception {
        mockMvc.perform(post("/api/recipes/1/cook"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, "Expected status 200 or 400, but was " + status);
                });
    }
}
