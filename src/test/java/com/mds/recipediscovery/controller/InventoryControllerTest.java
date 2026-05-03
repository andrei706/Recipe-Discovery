package com.mds.recipediscovery.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
