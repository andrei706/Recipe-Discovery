package com.mds.recipediscovery.controller;

import com.mds.recipediscovery.models.Inventory;
import com.mds.recipediscovery.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    // user1 for now until the userservice is done
    private final Integer CURRENT_USER_ID = 1;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addIngredient(
            @RequestParam Integer ingredientId,
            @RequestParam int quantity) {
        try {
            Inventory inventory = inventoryService.addIngredientToUser(CURRENT_USER_ID, ingredientId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateIngredientQuantity(
            @RequestParam Integer ingredientId,
            @RequestParam int newQuantity) {
        try {
            Inventory inventory = inventoryService.updateIngredientQuantity(CURRENT_USER_ID, ingredientId, newQuantity);
            if (inventory == null) {
                return ResponseEntity.ok().body("Ingredient removed from inventory since quantity became 0");
            }
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeIngredient(
            @RequestParam Integer ingredientId) {
        try {
            inventoryService.removeIngredientFromUser(CURRENT_USER_ID, ingredientId);
            return ResponseEntity.ok().body("Ingredient removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
