package com.mds.recipediscovery.services;

import com.mds.recipediscovery.dto.InventoryItemDTO;
import com.mds.recipediscovery.models.Ingredient;
import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.IngredientRepository;
import com.mds.recipediscovery.repository.InventoryRepository;
import com.mds.recipediscovery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            UserRepository userRepository,
                            IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Inventory addIngredientToUser(Integer userId, Integer ingredientId, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        Optional<Inventory> existingInventory = inventoryRepository.findByUserAndIngredient(user, ingredient);

        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            return inventoryRepository.save(inventory);
        } else {
            Inventory newInventory = new Inventory(user, ingredient, quantity);
            return inventoryRepository.save(newInventory);
        }
    }

    public Inventory updateIngredientQuantity(Integer userId, Integer ingredientId, double newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        Inventory inventory = inventoryRepository.findByUserAndIngredient(user, ingredient)
                .orElseThrow(() -> new RuntimeException("Ingredient not found in user's inventory"));

        if (newQuantity == 0) {
            inventoryRepository.delete(inventory);
            return null;
        } else {
            inventory.setQuantity(newQuantity);
            return inventoryRepository.save(inventory);
        }
    }

    public void removeIngredientFromUser(Integer userId, Integer ingredientId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        Inventory inventory = inventoryRepository.findByUserAndIngredient(user, ingredient)
                .orElseThrow(() -> new RuntimeException("Ingredient not found in user's inventory"));

        inventoryRepository.delete(inventory);
    }

    public List<InventoryItemDTO> getInventoryForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return inventoryRepository.findByUser(user).stream()
                .map(item -> new InventoryItemDTO(
                        item.getIngredient().getIngredientId(),
                        item.getIngredient().getName(),
                        item.getIngredient().getMeasurementUnit(),
                        item.getQuantity()))
                .toList();
    }
}
