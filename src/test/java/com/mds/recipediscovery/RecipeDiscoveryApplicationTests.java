package com.mds.recipediscovery;

import com.mds.recipediscovery.services.InventoryService;
import com.mds.recipediscovery.services.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RecipeDiscoveryApplicationTests {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void contextLoads() {
        assertNotNull(recipeService, "RecipeService ar trebui să fie injectat corect de Spring");
        assertNotNull(inventoryService, "InventoryService ar trebui să fie injectat corect de Spring");
    }

}
