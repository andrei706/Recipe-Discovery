package com.mds.recipediscovery.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@WithMockUser(username = "1")
class InventoryControllerTest {

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
    //NOTE: This test will not work if the user aleardy has the ingredient 1
    void testAddIngredient() throws Exception {
        mockMvc.perform(post("/api/inventory/add")
                .param("ingredientId", "1")
                .param("quantity", "1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, "Expected status 200 or 400, but was " + status);
                });
    }

    @Test
    void testUpdateIngredientQuantity() throws Exception {
        mockMvc.perform(put("/api/inventory/update")
                .param("ingredientId", "1")
                .param("newQuantity", "2"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, "Expected status 200 or 400, but was " + status);
                });
    }

    @Test
    void testRemoveIngredient() throws Exception {
        mockMvc.perform(delete("/api/inventory/remove")
                .param("ingredientId", "1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, "Expected status 200 or 400, but was " + status);
                });
    }
}
