package com.mds.recipediscovery.services;

import com.mds.recipediscovery.models.Ingredient;
import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.IngredientRepository;
import com.mds.recipediscovery.repository.InventoryRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private User testUser;
    private Ingredient testIngredient;
    private Inventory existingInventory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);

        testIngredient = new Ingredient();
        testIngredient.setIngredientId(1);

        existingInventory = new Inventory(testUser, testIngredient, 500.0);
    }

    @Test
    void testAddIngredientToUser_NewIngredient() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.findByUserAndIngredient(testUser, testIngredient)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventory result = inventoryService.addIngredientToUser(1, 1, 200);

        assertEquals(200.0, result.getQuantity(), 0.001);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testAddIngredientToUser_ExistingIngredient() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.findByUserAndIngredient(testUser, testIngredient))
                .thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventory result = inventoryService.addIngredientToUser(1, 1, 200);

        assertEquals(700.0, result.getQuantity(), 0.001);
        verify(inventoryRepository, times(1)).save(existingInventory);
    }

    @Test
    void testUpdateIngredientQuantity_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.findByUserAndIngredient(testUser, testIngredient))
                .thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventory result = inventoryService.updateIngredientQuantity(1, 1, 300);

        assertEquals(300.0, result.getQuantity(), 0.001);
        verify(inventoryRepository, times(1)).save(existingInventory);
    }

    @Test
    void testUpdateIngredientQuantity_ZeroDeletesInventory() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.findByUserAndIngredient(testUser, testIngredient))
                .thenReturn(Optional.of(existingInventory));

        Inventory result = inventoryService.updateIngredientQuantity(1, 1, 0);

        assertNull(result);
        verify(inventoryRepository, times(1)).delete(existingInventory);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testRemoveIngredientFromUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.findByUserAndIngredient(testUser, testIngredient))
                .thenReturn(Optional.of(existingInventory));

        inventoryService.removeIngredientFromUser(1, 1);

        verify(inventoryRepository, times(1)).delete(existingInventory);
    }
}
