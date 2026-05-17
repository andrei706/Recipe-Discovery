package com.mds.recipediscovery.services;

import com.mds.recipediscovery.models.*;
import com.mds.recipediscovery.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private RecipeNecessitiesRepository recipeIngredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    private User testUser;
    private Ingredient flour;
    private Ingredient sugar;
    private Ingredient eggs;

    private Recipe pancake;
    private Recipe cake;

    @BeforeEach
    void setUp() {
        // Setup mock entities
        testUser = new User();
        testUser.setUserId(1);

        flour = new Ingredient();
        flour.setIngredientId(1);

        sugar = new Ingredient();
        sugar.setIngredientId(2);

        eggs = new Ingredient();
        eggs.setIngredientId(3);

        pancake = new Recipe();
        pancake.setRecipeId(1);

        cake = new Recipe();
        cake.setRecipeId(2);
    }

    @Test
    void testFindAvailableRecipes_UserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recipeService.findAvailableRecipes(99);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testFindAvailableRecipes_ReturnsOnlyCookableRecipes() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        when(recipeRepository.findRecipesUserCanCook(1)).thenReturn(List.of(pancake));

        List<Recipe> result = recipeService.findAvailableRecipes(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRecipeId(), "Only the pancake recipe should be available");
    }

    @Test
    void testFindRecipesSortedByMatchingIngredients() {
        // Setup User
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Mock the new repository method
        when(recipeRepository.findRecipesSortedByMatchingIngredients(1)).thenReturn(Arrays.asList(cake, pancake));

        // Execute
        List<Recipe> result = recipeService.findRecipesSortedByMatchingIngredients(1);

        // Verify: It should just return whatever the repository returned
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getRecipeId(), "Cake should be first");
        assertEquals(1, result.get(1).getRecipeId(), "Pancake should be second");
        
        org.mockito.Mockito.verify(recipeRepository, org.mockito.Mockito.times(1)).findRecipesSortedByMatchingIngredients(1);
    }

    @Test
    void testCookRecipe_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(pancake));

        Inventory invFlour = new Inventory(testUser, flour, 500);
        Inventory invEggs = new Inventory(testUser, eggs, 2);

        RecipeNecessities req1 = new RecipeNecessities();
        req1.setRecipe(pancake);
        req1.setIngredient(flour);
        req1.setQuantity(200);

        RecipeNecessities req2 = new RecipeNecessities();
        req2.setRecipe(pancake);
        req2.setIngredient(eggs);
        req2.setQuantity(2);

        when(recipeIngredientRepository.findByRecipe(pancake)).thenReturn(Arrays.asList(req1, req2));
        when(inventoryRepository.findByUserAndIngredientIn(org.mockito.ArgumentMatchers.eq(testUser), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Arrays.asList(invFlour, invEggs));

        recipeService.cookRecipe(1, 1);

        assertEquals(300, invFlour.getQuantity());
        org.mockito.Mockito.verify(inventoryRepository, org.mockito.Mockito.times(1)).save(invFlour);
        org.mockito.Mockito.verify(inventoryRepository, org.mockito.Mockito.times(1)).delete(invEggs);
    }

    @Test
    void testCookRecipe_NotEnoughIngredients() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(pancake));

        Inventory invFlour = new Inventory(testUser, flour, 100);
        Inventory invEggs = new Inventory(testUser, eggs, 2);

        RecipeNecessities req1 = new RecipeNecessities();
        req1.setRecipe(pancake);
        req1.setIngredient(flour);
        req1.setQuantity(200);

        RecipeNecessities req2 = new RecipeNecessities();
        req2.setRecipe(pancake);
        req2.setIngredient(eggs);
        req2.setQuantity(2);

        when(recipeIngredientRepository.findByRecipe(pancake)).thenReturn(Arrays.asList(req1, req2));
        when(inventoryRepository.findByUserAndIngredientIn(org.mockito.ArgumentMatchers.eq(testUser), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Arrays.asList(invFlour, invEggs));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recipeService.cookRecipe(1, 1);
        });

        assertTrue(exception.getMessage().contains("You do not have enough ingredients to cook this recipe"));
        org.mockito.Mockito.verify(inventoryRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(inventoryRepository, org.mockito.Mockito.never())
                .delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testGetRecipesWithMatchPercentage() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Pancake: 1 out of 2 = 50%
        Object[] row1 = new Object[] { pancake, 1, 2 };
        // Cake: 3 out of 3 = 100%
        Object[] row2 = new Object[] { cake, 3, 3 };

        when(recipeRepository.findRecipesWithMatchData(1)).thenReturn(Arrays.asList(row1, row2));

        List<com.mds.recipediscovery.dto.RecipeMatchDTO> result = recipeService.getRecipesWithMatchPercentage(1);

        // Cake should be first because it has 100%
        assertEquals(2, result.size());

        assertEquals(cake.getRecipeId(), result.get(0).getRecipe().getRecipeId());
        assertEquals(100.0, result.get(0).getMatchPercentage());
        assertEquals(3, result.get(0).getMatchedIngredients());
        assertEquals(3, result.get(0).getTotalIngredients());

        assertEquals(pancake.getRecipeId(), result.get(1).getRecipe().getRecipeId());
        assertEquals(50.0, result.get(1).getMatchPercentage());
        assertEquals(1, result.get(1).getMatchedIngredients());
        assertEquals(2, result.get(1).getTotalIngredients());
    }
}
