package com.mds.recipediscovery;

import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.services.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class RecipeDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecipeDiscoveryApplication.class, args);
    }
    @Bean
    public CommandLineRunner testLogic(RecipeService recipeService) {
        return args -> {
            System.out.println("\n=== VERIFICARE LOGICĂ REȚETE ===");

            // Presupunem că vrei să vezi ce poate găti utilizatorul cu ID 1 (Andrei)
            Integer testUserId = 1;
            List<Recipe> availableRecipes = recipeService.findAvailableRecipes(testUserId);

            if (availableRecipes.isEmpty()) {
                System.out.println("Utilizatorul cu ID " + testUserId + " nu poate găti nimic cu ce are în frigider.");
            } else {
                System.out.println("Utilizatorul " + testUserId + " poate găti următoarele rețete:");
                availableRecipes.forEach(recipe ->
                        System.out.println(" -> " + recipe.getName() + " (" + recipe.getCaloriesKcal() + " calorii)")
                );
            }

            System.out.println("==============================\n");
        };
    }
}
